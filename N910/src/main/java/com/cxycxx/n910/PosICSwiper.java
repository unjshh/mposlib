package com.cxycxx.n910;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;

import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.util.Util;
import com.google.gson.JsonObject;
import com.newland.aidl.deviceService.AidlDeviceService;
import com.newland.aidl.iccard.AidlICCard;


/**
 * 接触式IC卡读卡器
 */

public class PosICSwiper extends FBCommu {
    /**
     * @param context 上下文
     * @param taskId  任务id
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     */
    public PosICSwiper(Context context, String taskId, OnFBCommuFinish dealer) {
        super(context, taskId, dealer);
        connecter= new AIDLConnecter(context);
    }

    @Override
    public void launch() {
        connecter.bindService(new AIDLConnecter.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                try{
                    AidlDeviceService device= AidlDeviceService.Stub.asInterface(service);
                    final AidlICCard reader=AidlICCard.Stub.asInterface(device.getICCard());
                    byte[] apdu=reader.powerOn(0x01,0x01);//SLE44X2
                    JsonObject reso = new JsonObject();
                    reso.addProperty("result", "成功");
                    reso.addProperty("track1", new String(apdu));
                    callbackDealer(reso);
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
    private AIDLConnecter connecter;
}
/*
cardSlot -卡槽
(0x01:IC 卡槽,0x02:SAM 卡槽)
卡类型:
CPUCARD = 0x00;
SLE44X2 = 0x01;
SLE44X8 = 0x02;
AT88SC102 = 0x03;
AT88SC1604 = 0x04;
AT88SC1608 = 0x05;
ISO7816 = 0x06;
AT88SC153 = 0x07;
AT24C01 = 0x08;
AT24C02 = 0x09;
AT24C04 = 0x0A;
AT24C08 = 0x0B;
AT24C16 = 0x0C;
AT24C33 = 0x0D;
AT24C64 = 0x0E;
*/