package com.cxycxx.p990;

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

    protected abstract void onDataRead(String info);

    public void startRead() {
        execute(
                new Op() {
                    @Override
                    public void onStart(byte[] lastDataRead) throws RequestException {
                        driver.authBlock(2, MifareDriver.KEY_A, BytesUtil.hexString2Bytes("FFFFFFFFFFFF"), this);
                    }
                },

                new ReadOp() {
                    @Override
                    public void onStart(byte[] lastDataRead) throws RequestException {
                        driver.readBlock(2, this);
                    }
                },

                new Op() {
                    @Override
                    public void onStart(byte[] lastDataRead)throws RequestException {
                        String content="";
                        for (byte by:lastDataRead) content+=String.format("%02x",by);
                        driver.halt();
                        onDataRead(content);
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
