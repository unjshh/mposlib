package com.cxycxx.ld_a8;

import com.annimon.stream.function.Consumer;
import com.cxycxx.mposcore.mpos.MposPub;
import com.cxycxx.mposcore.util.GsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.landicorp.android.eptapi.card.MifareDriver;
import com.landicorp.android.eptapi.exception.RequestException;
import com.landicorp.android.eptapi.utils.BytesUtil;

/**
 * This card reader can do some data operations.
 *
 * @author chenwei
 */
public abstract class MifareOneCardReader {
    private MifareDriver driver;

    public MifareOneCardReader(MifareDriver cardDriver) {
        this.driver = cardDriver;
    }

    protected abstract void onDeviceServiceException();

    protected abstract void showErrorMessage(String msg);

    protected abstract void onDataRead(JsonObject info);

    public void startRead() {
        readBlocks();
    }

    private void readBlocks() {
        if (MposPub.mposConfig == null || !MposPub.mposConfig.has("rfcBlocks")) return;
        JsonElement temp = MposPub.mposConfig.get("rfcBlocks");
        if (!temp.isJsonArray()) return;
        JsonArray rfcBlocks = temp.getAsJsonArray();
        JsonObject data = new JsonObject();
        int[] exeCount = new int[]{0};
        boolean[] exeFlag = new boolean[]{true};
        for (int i = 0, l = rfcBlocks.size(); i < l; i++) {
            if (!exeFlag[0]) break;
            JsonObject block = rfcBlocks.get(i).getAsJsonObject();
            String blockKey = GsonHelper.joAsString(block, "blockKey");
            int keyType = GsonHelper.joAsInt(block, "keyType");
            int blockNo = GsonHelper.joAsInt(block, "blockNo");
            byte[] pwd = BytesUtil.hexString2Bytes(blockKey);
            readBlock(blockNo, keyType, pwd, s -> {
                data.addProperty("block" + blockNo, s);
                exeCount[0] = exeCount[0] + 1;
                if (exeCount[0] == l) {
                    onDataRead(data);
                    halt();
                }
            }, err -> {
                exeFlag[0] = false;
                showErrorMessage("读块" + blockNo + "时失败：" + err);
                halt();
            });
        }
    }

    /**
     * 停止
     */
    private void halt() {
        try {
            driver.halt();
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读一个块
     *
     * @param blockNo 块号
     * @param keyType 类型
     * @param pwd     密码
     * @param onSuc   成功后
     */
    private void readBlock(int blockNo, int keyType, byte[] pwd, Consumer<String> onSuc, Consumer<String> onFail) {
        execute(
                new Op() {
                    @Override
                    public void onStart(byte[] lastDataRead) throws RequestException {
                        driver.authBlock(blockNo, keyType, pwd, this);
                    }

                    @Override
                    public void onFail(int code) {
                        String err = getErrorDescription(code);
                        onFail.accept(err);
                    }

                    @Override
                    public void onCrash() {
                        onFail.accept("DeviceServiceException");
                    }
                },

                new ReadOp() {
                    @Override
                    public void onStart(byte[] lastDataRead) throws RequestException {
                        driver.readBlock(blockNo, this);
                    }

                    @Override
                    public void onFail(int error) {
                        String err = getErrorDescription(error);
                        onFail.accept(err);
                    }

                    @Override
                    public void onCrash() {
                        onFail.accept("DeviceServiceException");
                    }
                },

                new Op() {
                    @Override
                    public void onStart(byte[] lastDataRead) throws RequestException {
                        String content = "";
                        for (byte by : lastDataRead) content += String.format("%02x", by);
                        onSuc.accept(content);
                    }
                }
        );

    }

    /**
     * Connect all operations and execute them.
     *
     * @param operations
     */
    private void execute(MifareOneOperation... operations) {
        // Like chain of responsibility
        int len = operations.length - 1;
        for (int i = 0; i < len; i++) {
            operations[i].setNextOperation(operations[i + 1]);
        }
        // Start
        operations[0].start(null);
    }

    //将object对象转换为字符串
    public String Decrypt(int[] s)//26493, 21469, 12347
    {
        int key = 26493;
        int Var1 = 21469;
        int Var2 = 12347;

        byte[] bt2 = new byte[s.length];
        for (int i = 0; i < s.length; i++) {
            bt2[i] = (byte) (s[i] ^ (key >> 8));
            key = (int) ((s[i] + key) * Var1 + Var2);
        }
        try {
            return new String(bt2, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /// <summary>
    /// 字符串转16进制字节数组
    /// </summary>
    /// <param name="hexString"></param>
    /// <returns></returns>
    private int[] strToToHexByte(String hexString) {
        hexString = hexString.replace(" ", "");
        if ((hexString.length() % 2) != 0)
            hexString += " ";
        int[] returnBytes = new int[hexString.length() / 2];
        for (int i = 0; i < returnBytes.length; i++) {
            String s = hexString.substring(i * 2, i * 2 + 2);
            int n = "".equals(s) ? 0 : Integer.parseInt(s, 16);
            returnBytes[i] = n & 0xff;
        }
        return returnBytes;
    }

    /**
     * A simplified non blocking Mifare One driver operation
     *
     * @author chenwei
     */
    interface MifareOneOperation {
        /**
         * Start this operation object.
         *
         * @param lastDataRead
         */
        void start(byte[] lastDataRead);

        /**
         * You can do one operation in this place.
         *
         * @throws RequestException
         */
        void onStart(byte[] lastDataRead) throws RequestException;

        /**
         * Set the next operation and it will be start on this operation success.
         *
         * @param operation
         */
        void setNextOperation(MifareOneOperation operation);
    }

    /**
     * It's a template of commen driver operation based on 'OnResultListener' such as 'increase', 'decrease'.
     *
     * @author chenwei
     */
    abstract class Op extends MifareDriver.OnResultListener implements MifareOneOperation {
        private MifareOneOperation nextOperation;

        @Override
        public void onFail(int code) {
            showErrorMessage("Mifare One Operation Error - " + getErrorDescription(code));
        }

        public String getErrorDescription(int code) {
            switch (code) {
                case ERROR_ERRPARAM:
                    return "Parameter error";
                case ERROR_FAILED:
                    return "Other error(OS error,etc)";
                case ERROR_NOTAGERR:
                    return "Operating range without card or card is not responding";
                case ERROR_CRCERR:
                    return "The data CRC parity error";
                case ERROR_AUTHERR:
                    return "Authentication failed";
                case ERROR_PARITYERR:
                    return "Data parity error";
                case ERROR_CODEERR:
                    return "The wrong card response data content";
                case ERROR_SERNRERR:
                    return "Data in the process of conflict protection error";
                case ERROR_NOTAUTHERR:
                    return "Card not authentication";
                case ERROR_BITCOUNTERR:
                    return "The length of data bits card return is wrong";
                case ERROR_BYTECOUNTERR:
                    return "The length of data bytes card return is wrong";
                case ERROR_OVFLERR:
                    return "The card return data overflow";
                case ERROR_FRAMINGERR:
                    return "Data frame error";
                case ERROR_UNKNOWN_COMMAND:
                    return "The terminal sends illegal command";
                case ERROR_COLLERR:
                    return "Multiple cards conflict";
                case ERROR_RESETERR:
                    return "RF card module reset failed";
                case ERROR_INTERFACEERR:
                    return "RF card module interface error";
                case ERROR_RECBUF_OVERFLOW:
                    return "Receive buffer overflow";
                case ERROR_VALERR:
                    return "Numerical block operation on the Mifare card, block error";
                case ERROR_ERRTYPE:
                    return "Card type of error";
                case ERROR_SWDIFF:
                    return "Data exchange with MifarePro card or TypeB card, card loopback status byte SW1! = 0x90, =0x00 SW2.";
                case ERROR_TRANSERR:
                    return "Communication error";
                case ERROR_PROTERR:
                    return "The card return data does not meet the requirements of the protocal";
                case ERROR_MULTIERR:
                    return "There are multiple cards in the induction zone";
                case ERROR_NOCARD:
                    return "There is no card in the induction zone";
                case ERROR_CARDEXIST:
                    return "The card is still in the induction zone";
                case ERROR_CARDTIMEOUT:
                    return "Response timeout";
                case ERROR_CARDNOACT:
                    return "Pro card or TypeB card is not activated";
            }
            return "unknown error [" + code + "]";
        }

        @Override
        public void onCrash() {
            // This class does not know how to handle this exception.
            onDeviceServiceException();
        }

        @Override
        public void onSuccess() {
            if (nextOperation != null) {
                nextOperation.start(null);
            }
        }

        @Override
        public void start(byte[] lastDataRead) {
            try {
                onStart(lastDataRead);
            } catch (RequestException e) {
                e.printStackTrace();
                onDeviceServiceException();
            }
        }

        @Override
        public void setNextOperation(MifareOneOperation operation) {
            this.nextOperation = operation;
        }
    }

    /**
     * It's a template of read operation based on OnReadListener.
     *
     * @author chenwei
     */
    abstract class ReadOp extends MifareDriver.OnReadListener implements MifareOneOperation {
        private MifareOneOperation nextOperation;

        @Override
        public void onFail(int error) {
            showErrorMessage("Mifare One Operation Error - " + getErrorDescription(error));
        }

        String getErrorDescription(int code) {
            switch (code) {
                case ERROR_ERRPARAM:
                    return "Parameter error";
                case ERROR_FAILED:
                    return "Other error(OS error,etc)";
                case ERROR_NOTAGERR:
                    return "Operating range without card or card is not responding";
                case ERROR_CRCERR:
                    return "The data CRC parity error";
                case ERROR_AUTHERR:
                    return "Authentication failed";
                case ERROR_PARITYERR:
                    return "Data parity error";
                case ERROR_CODEERR:
                    return "The wrong card response data content";
                case ERROR_SERNRERR:
                    return "Data in the process of conflict protection error";
                case ERROR_NOTAUTHERR:
                    return "Card not authentication";
                case ERROR_BITCOUNTERR:
                    return "The length of data bits card return is wrong";
                case ERROR_BYTECOUNTERR:
                    return "The length of data bytes card return is wrong";
                case ERROR_OVFLERR:
                    return "The card return data overflow";
                case ERROR_FRAMINGERR:
                    return "Data frame error";
                case ERROR_UNKNOWN_COMMAND:
                    return "The terminal sends illegal command";
                case ERROR_COLLERR:
                    return "Multiple cards conflict";
                case ERROR_RESETERR:
                    return "RF card module reset failed";
                case ERROR_INTERFACEERR:
                    return "RF card module interface error";
                case ERROR_RECBUF_OVERFLOW:
                    return "Receive buffer overflow";
                case ERROR_VALERR:
                    return "Numerical block operation on the Mifare card, block error";
                case ERROR_ERRTYPE:
                    return "Card type of error";
                case ERROR_SWDIFF:
                    return "Data exchange with MifarePro card or TypeB card, card loopback status byte SW1! = 0x90, =0x00 SW2.";
                case ERROR_TRANSERR:
                    return "Communication error";
                case ERROR_PROTERR:
                    return "The card return data does not meet the requirements of the protocal";
                case ERROR_MULTIERR:
                    return "There are multiple cards in the induction zone";
                case ERROR_NOCARD:
                    return "There is no card in the induction zone";
                case ERROR_CARDEXIST:
                    return "The card is still in the induction zone";
                case ERROR_CARDTIMEOUT:
                    return "Response timeout";
                case ERROR_CARDNOACT:
                    return "Pro card or TypeB card is not activated";
            }
            return "unknown error [" + code + "]";
        }

        @Override
        public void onSuccess(byte[] data) {
            if (nextOperation != null) {
                nextOperation.start(data);
            }
        }

        @Override
        public void onCrash() {
            onDeviceServiceException();
        }

        @Override
        public void start(byte[] lastDataRead) {
            try {
                onStart(lastDataRead);
            } catch (RequestException e) {
                e.printStackTrace();
                onDeviceServiceException();
            }
        }

        @Override
        public void setNextOperation(MifareOneOperation operation) {
            this.nextOperation = operation;
        }
    }

}
