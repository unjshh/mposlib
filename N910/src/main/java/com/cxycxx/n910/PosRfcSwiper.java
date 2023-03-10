package com.cxycxx.n910;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;

import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.google.gson.JsonObject;
import com.newland.aidl.deviceService.AidlDeviceService;
import com.newland.aidl.rfcard.AidlRFCard;
import com.newland.aidl.rfcard.PowerOnRFResult;

/**
 * 非接卡扫描器
 */

public class PosRfcSwiper extends FBCommu {
    /**
     * @param context 上下文
     * @param taskId  任务id
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     */
    public PosRfcSwiper(Context context, String taskId, OnFBCommuFinish dealer) {
        super(context, taskId, dealer);
        connecter = new AIDLConnecter(context);
    }

    @Override
    public void launch() {
        connecter.bindService(new AIDLConnecter.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(ComponentName name, final IBinder service) {
                try {
                    System.out.println("rfc服务已连接");
                    AidlDeviceService device = AidlDeviceService.Stub.asInterface(service);
                    reader = AidlRFCard.Stub.asInterface(device.getRFCard());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                byte[] keyA = hexString2Bytes("FFFFFFFFFFFF");
                                int[] cardType = new int[3];
                                cardType[0] = 1;//A卡
                                cardType[1] = 2;//B卡
                                cardType[2] = 4;//M1卡

                                PowerOnRFResult rst = reader.powerOn(cardType, 15);
                                if (rst == null) {
                                    errCallbackDealerOnUiTread("上电失败");
                                    return;
                                }
                                boolean flag = reader.authenticate(1, rst.getCardSerialNo(), 40, keyA);
                                if (!flag) {
                                    errCallbackDealerOnUiTread("验证块40失败");
                                    return;
                                }
                                flag = reader.authenticate(1, rst.getCardSerialNo(), 41, keyA);
                                if (!flag) {
                                    errCallbackDealerOnUiTread("验证块41失败");
                                    return;
                                }
                                byte[] b40 = reader.readData(40), b41 = reader.readData(41);
                                if (b40 == null) b40 = new byte[0];
                                if (b41 == null) b41 = new byte[0];
                                String block40 = "", block41 = "";
                                for (byte b : b40) {
                                    block40 += String.format("%02x", b);
                                }
                                for (byte b : b41) {
                                    block41 += String.format("%02x", b);
                                }
                                JsonObject reso = new JsonObject();
                                reso.addProperty("result", "成功");
                                reso.addProperty("cardType", rst.getRfcardType() == 4 ? "M1卡" : (rst.getRfcardType() == 2 ? "B卡" : "A卡"));
                                reso.addProperty("block40", block40);
                                reso.addProperty("block41", block41);
                                int[] bs40 = strToToHexByte(block40);
                                int[] bs41 = strToToHexByte(block41.substring(0, block41.length() >= 16 ? 16 : block41.length()));
                                String track1 = Decrypt(bs40) + Decrypt(bs41);
                                reso.addProperty("track1", track1);
                                callbackDealerOnUiTread(reso);

                            } catch (Exception ex) {
                                errCallbackDealerOnUiTread("异常：" + ex);
                            }
                        }
                    }).start();

                } catch (Exception ex) {
                    errCallbackDealer("异常：" + ex);
                }
            }
        });
    }

    @Override
    public void stop() {
        try {
            if (reader != null) reader.powerOff();
            connecter.unbindService();
        } catch (Exception ex) {

        }

    }

    private byte[] hexString2Bytes(String data) {
        byte[] result = new byte[(data.length() + 1) / 2];
        if ((data.length() & 1) == 1) {
            data = data + "0";
        }

        for (int i = 0; i < result.length; ++i) {
            result[i] = (byte) (hex2byte(data.charAt(i * 2 + 1)) | hex2byte(data.charAt(i * 2)) << 4);
        }

        return result;
    }

    private byte hex2byte(char hex) {
        return hex <= 102 && hex >= 97 ? (byte) (hex - 97 + 10) : (hex <= 70 && hex >= 65 ? (byte) (hex - 65 + 10) : (hex <= 57 && hex >= 48 ? (byte) (hex - 48) : 0));
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

    private AIDLConnecter connecter;
    private AidlRFCard reader;
}
