package com.cxycxx.icbcsmartpos;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;

import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.util.Util;
import com.google.gson.JsonObject;
import com.icbc.smartpos.deviceservice.aidl.IDeviceService;
import com.icbc.smartpos.deviceservice.aidl.IRFCardReader;
import com.icbc.smartpos.deviceservice.aidl.RFSearchListener;

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
        connecter= new AIDLConnecter(context,"设备");
    }

    @Override
    public void launch() {
        connecter.bindService(new AIDLConnecter.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(ComponentName name, final IBinder service) {
                try{
                    System.out.println("rfc服务已连接");
                    final IRFCardReader reader= IDeviceService.Stub.asInterface(service).getRFCardReader();
                    reader.searchCard(new RFSearchListener.Stub() {
                        @Override
                        public void onCardPass(int cardType) throws RemoteException {
                            try {
                                System.out.println("读卡" + cardType);
                                String driverName = "";
                                if (cardType == 0x00) driverName = "S50";
                                else if (cardType == 0x01) driverName = "S70";
                                else if (cardType == 0x02 || cardType == 0x03 || cardType == 0x04)
                                    driverName = "PRO";
                                else if (cardType == 0x05) driverName = "CPU";
                                byte[] res = new byte[16];
                                JsonObject reso = new JsonObject();
                                reso.addProperty("result", "失败");
                                int flag = reader.activate(driverName, res);
                                if (flag == 0) {
                                    byte[] keyA = hexString2Bytes("FFFFFFFFFFFF");
                                    flag = reader.authBlock(40, 1, keyA) + reader.authBlock(41, 1, keyA);
                                    if (flag == 0) {
                                        byte[] b40 = new byte[16], b41 = new byte[16];
                                        flag = reader.readBlock(40, b40) + reader.readBlock(41, b41);
                                        if (flag == 0) {
                                            String block40 = "", block41 = "";
                                            for (byte b : b40) {
                                                block40 += String.format("%02x", b);
                                            }
                                            for (byte b : b41) {
                                                block41 += String.format("%02x", b);
                                            }

                                            reso.addProperty("result", "成功");
                                            reso.addProperty("block40", block40);
                                            reso.addProperty("block41", block41);
                                            int[] bs40 = strToToHexByte(block40);
                                            int[] bs41 = strToToHexByte(block41.substring(0, 16));
                                            String track1 = Decrypt(bs40) + Decrypt(bs41);
                                            reso.addProperty("track1", track1);
                                        } else {
                                            reso.addProperty("message", "读卡数据失败");
                                        }
                                    } else {
                                        reso.addProperty("message", "卡密钥校验失败");
                                    }
                                } else {
                                    reso.addProperty("message", "卡激活失败");
                                }
                                callbackDealerOnUiTread(reso);
                            }catch (Exception ex){
                                errCallbackDealerOnUiTread("取数异常："+ex);
                            }
                        }

                        @Override
                        public void onFail(int error, String message) throws RemoteException {
                            reader.stopSearch();
                            errCallbackDealerOnUiTread(message);
                            //Util.showMsg(mContext,message);
                        }
                    });
                }catch (Exception ex) {
                    Util.showMsg(mContext,"异常："+ex);
                }
            }
        });
    }

    @Override
    public void stop() {
        connecter.unbindService();
    }
    private byte[] hexString2Bytes(String data) {
        byte[] result = new byte[(data.length() + 1) / 2];
        if((data.length() & 1) == 1) {
            data = data + "0";
        }

        for(int i = 0; i < result.length; ++i) {
            result[i] = (byte)(hex2byte(data.charAt(i * 2 + 1)) | hex2byte(data.charAt(i * 2)) << 4);
        }

        return result;
    }
    private byte hex2byte(char hex) {
        return hex <= 102 && hex >= 97?(byte)(hex - 97 + 10):(hex <= 70 && hex >= 65?(byte)(hex - 65 + 10):(hex <= 57 && hex >= 48?(byte)(hex - 48):0));
    }
    //将object对象转换为字符串
    public String Decrypt(int[] s)//26493, 21469, 12347
    {
        int key = 26493;
        int  Var1 = 21469;
        int  Var2 = 12347;

        byte[] bt2 = new byte[s.length];
        for (int i = 0; i < s.length; i++)
        {
            bt2[i] = (byte)(s[i] ^ (key >> 8));
            key = (int)((s[i] + key) * Var1 + Var2);
        }
        try {
            return new String(bt2,"utf-8");
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
    private int[] strToToHexByte(String hexString)
    {
        hexString = hexString.replace(" ", "");
        if ((hexString.length() % 2) != 0)
            hexString += " ";
        int[] returnBytes = new int[hexString.length() / 2];
        for (int i = 0; i < returnBytes.length; i++){
            String s=hexString.substring(i * 2, i * 2+2);
            int n= "".equals(s)?0:Integer.parseInt(s, 16);
            returnBytes[i] =n& 0xff;
        }
        return returnBytes;
    }
    private AIDLConnecter connecter;
}
