package com.cxycxx.icbcsmartpos;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.util.Util;
import com.google.gson.JsonObject;
import com.icbc.smartpos.deviceservice.aidl.IDeviceService;
import com.icbc.smartpos.deviceservice.aidl.IMagCardReader;
import com.icbc.smartpos.deviceservice.aidl.MagCardListener;

/**
 * 磁条扫描器
 */

public class PosMagSwiper extends FBCommu {
    /**
     * @param context 上下文
     * @param taskId  任务id
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     */
    public PosMagSwiper(Context context, String taskId, OnFBCommuFinish dealer) {
        super(context, taskId, dealer);
        connecter= new AIDLConnecter(context,"设备");
    }

    @Override
    public void launch() {
        connecter.bindService(new AIDLConnecter.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(ComponentName name, final IBinder service) {
                try{
                    final IMagCardReader reader= IDeviceService.Stub.asInterface(service).getMagCardReader();
                    reader.searchCard(2 * 60, new MagCardListener.Stub() {
                        @Override
                        public void onSuccess(Bundle track) throws RemoteException {
                            JsonObject rst=Util.fromObject(track);
                            rst.addProperty("result", "成功");
                            callbackDealerOnUiTread(rst);
                            reader.stopSearch();
                        }

                        @Override
                        public void onError(int error, String message) throws RemoteException {
                            errCallbackDealerOnUiTread(message);
                            reader.stopSearch();
                        }

                        @Override
                        public void onTimeout() throws RemoteException {
                            errCallbackDealerOnUiTread("刷磁卡超时");
                            reader.stopSearch();
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
    private AIDLConnecter connecter;
}
