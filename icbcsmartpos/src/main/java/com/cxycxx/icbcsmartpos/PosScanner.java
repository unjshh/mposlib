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
import com.icbc.smartpos.deviceservice.aidl.IScanner;
import com.icbc.smartpos.deviceservice.aidl.ScannerListener;

/**
 * 扫描器
 */

public class PosScanner extends FBCommu {
    /**
     * @param context 上下文
     * @param taskId  任务id
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     */
    public PosScanner(Context context, String taskId, OnFBCommuFinish dealer) {
        super(context, taskId, dealer);
        connecter= new AIDLConnecter(context,"设备");
    }

    @Override
    public void launch() {
        connecter.bindService(new AIDLConnecter.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(ComponentName name, final IBinder service) {
                System.out.println("扫描服务已连接");
                try{
                    final IScanner scanner= IDeviceService.Stub.asInterface(service).getScanner(1);
                    Bundle param=new Bundle();
                    param.putString("upPromptString","扫描");
                    param.putString("downPromptString","扫描");
                    scanner.startScan(param, 2 * 60, new ScannerListener.Stub() {
                        @Override
                        public void onSuccess(String barcode) throws RemoteException {
                            System.out.println("扫描成功"+barcode);
                            JsonObject data=new JsonObject();
                            data.addProperty("result", "成功");
                            data.addProperty("content", barcode);
                            data.addProperty("barcodeType", "条形码");//类型是条形码，对应CRM
                            callbackDealerOnUiTread(data);
                        }
                        @Override
                        public void onError(int error, String message) throws RemoteException {
                            scanner.stopScan();
                            errCallbackDealerOnUiTread(message);
                        }

                        @Override
                        public void onTimeout() throws RemoteException {
                            scanner.stopScan();
                            errCallbackDealerOnUiTread("扫描超时");
                        }

                        @Override
                        public void onCancel() throws RemoteException {
                            scanner.stopScan();
                            errCallbackDealerOnUiTread("扫描已取消");
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
