package com.cxycxx.icbcsmartpos;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;

import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.util.Util;
import com.icbc.smartpos.deviceservice.aidl.IDeviceService;
import com.icbc.smartpos.deviceservice.aidl.IInsertCardReader;

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
        connecter= new AIDLConnecter(context,"设备");
    }

    @Override
    public void launch() {
        connecter.bindService(new AIDLConnecter.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                try{
                    final IInsertCardReader reader=IDeviceService.Stub.asInterface(service).getInsertCardReader();
                    byte[] apdu=new byte[10];
                    apdu=reader.exchangeApdu(apdu);
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
