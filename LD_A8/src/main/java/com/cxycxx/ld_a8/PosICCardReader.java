package com.cxycxx.ld_a8;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.google.gson.JsonObject;
import com.landicorp.android.eptapi.DeviceService;
import com.landicorp.android.eptapi.card.InsertCpuCardDriver;
import com.landicorp.android.eptapi.card.Sim4428Driver;
import com.landicorp.android.eptapi.card.Sim4442Driver;
import com.landicorp.android.eptapi.device.InsertCardReader;
import com.landicorp.android.eptapi.exception.ReloginException;
import com.landicorp.android.eptapi.exception.RequestException;
import com.landicorp.android.eptapi.exception.ServiceOccupiedException;
import com.landicorp.android.eptapi.exception.UnsupportMultiProcess;

/**
 * 插入式卡读卡器
 */

public class PosICCardReader extends FBCommu {
    /**
     * @param context 上下文
     * @param taskId  任务id
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     */
    public PosICCardReader(Context context, String taskId, OnFBCommuFinish dealer) {
        super(context, taskId, dealer);
    }

    @Override
    public void launch() {
        try {
            stop();
            bindDeviceService();
            InsertCardReader.getInstance().searchCard(onSearchListener);
        } catch (RequestException e) {
            e.printStackTrace();
            bindDeviceService();
        }
    }

    @Override
    public void stop() {
        try {
            InsertCardReader.getInstance().stopSearch();
        } catch (RequestException e) {
            e.printStackTrace();
        }
        DeviceService.logout();
    }

    private void bindDeviceService() {
        try {
            DeviceService.login((Activity) mContext);
        } catch (RequestException e) {
            // Rebind after a few milliseconds,
            // If you want this application keep the right of the device service
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bindDeviceService();
                }
            }, 300);
            e.printStackTrace();
        } catch (ServiceOccupiedException e) {
            e.printStackTrace();
        } catch (ReloginException e) {
            e.printStackTrace();
        } catch (UnsupportMultiProcess e) {
            e.printStackTrace();
        }
    }

    private String bytes2HexString(byte[] data) {
        StringBuilder buffer = new StringBuilder();
        for (byte b : data) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                buffer.append('0');
            }
            buffer.append(hex);
        }
        return buffer.toString().toUpperCase();
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

    private InsertCardReader.OnSearchListener onSearchListener = new InsertCardReader.OnSearchListener() {

        @Override
        public void onCrash() {
            bindDeviceService();
        }

        @Override
        public void onFail(int code) {
            errCallbackDealer(code == ERROR_TIMEOUT ? "读卡超时" : "读卡失败");
        }

        @Override
        public void onCardInsert() {
            SIM4442CardReaderImpl reader442=new SIM4442CardReaderImpl("USERCARD");
            if(!reader442.exists()){
                errCallbackDealer("卡不存在,驱卡类型4442");
                return;
            }
            int flag=reader442.powerUp(Sim4442Driver.VOL_5);//上电
            byte[] keyA = hexString2Bytes("FFFFFF");
            byte[] bs=null;
            int flag1=flag;
            if(flag!= SIM4442CardReaderImpl.SUCCESS){
                SIM4428CardReaderImpl reader428=new SIM4428CardReaderImpl("USERCARD");
                if(!reader428.exists()){
                    errCallbackDealer("卡不存在,驱卡类型4428");
                    return;
                }
                flag=reader428.powerUp(Sim4428Driver.VOL_5);
                if(flag== SIM4442CardReaderImpl.SUCCESS)bs=reader428.read(keyA,32,60);
            }else bs=reader442.read(keyA,32,60);
            if(bs==null){
                errCallbackDealer("读卡失败:SIM4442CardReader为"+flag1+" ,SIM4428CardReader为"+flag);
                return;
            }
            JsonObject reso = new JsonObject();
            reso.addProperty("result", "成功");
            reso.addProperty("track1", new String(bs));
            callbackDealer(reso);
        }


    };

    // 自定义错误码:接口调用失败或成功
    private static final int FAIL = 0xff;
    private static final int SUCCESS = 0x00;
    //private InsertCardReader reader = InsertCardReader.getInstance();
    private InsertCpuCardDriver driver = new InsertCpuCardDriver();
}
