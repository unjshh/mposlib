package com.cxycxx.n910;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;

import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.google.gson.JsonObject;
import com.newland.aidl.constant.Const;
import com.newland.aidl.deviceService.AidlDeviceService;
import com.newland.aidl.scanner.AidlScanner;
import com.newland.aidl.scanner.AidlScannerListener;

import java.util.Calendar;

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
        connecter = new AIDLConnecter(context);
    }

    @Override
    public void launch() {
        connecter.bindService(new AIDLConnecter.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(ComponentName name, final IBinder service) {
                System.out.println("扫描服务已连接");
                try {
                    AidlDeviceService device = AidlDeviceService.Stub.asInterface(service);
                    final AidlScanner scanner = AidlScanner.Stub.asInterface(device.getScanner());
                    scanner.startScan(Const.ScanType.BACK, 15000, new AidlScannerListener.Stub() {
                        @Override
                        public void onScanResult(String strings) {
                            JsonObject data=new JsonObject();
                            data.addProperty("result", "成功");
                            data.addProperty("content", strings);
                            data.addProperty("barcodeType", "未知");//类型是条形码，对应CRM
                            commit(data);
                       }

                        @Override
                        public void onTimeout() throws RemoteException {
                            errCallbackDealer("扫描超时");
                        }

                        @Override
                        public void onCancel() throws RemoteException {
                            errCallbackDealer("扫描取消");
                        }

                        @Override
                        public void onError(int arg0,String detail) {
                            errCallbackDealer("扫描错误:" + detail);
                        }

                    });
                } catch (Exception ex) {
                    errCallbackDealer("异常：" + ex);
                }
            }
        });
    }


    @Override
    public void stop() {
        connecter.unbindService();
    }
    /**
     * 提交结果
     * @param result 结果
     */
    private synchronized void commit(JsonObject result){
        if(result==null||isFinished)return;
        String cn=result.has("content")?result.get("content").getAsString():"";
        long now= Calendar.getInstance().getTimeInMillis();
        if(lastContent.equals(cn)&&lastTime>0&&now-lastTime<3000)return;
        isFinished=true;
        lastContent=cn;
        lastTime= now;
        callbackDealerOnUiTread(result);
    }
    private AIDLConnecter connecter;
    private boolean isFinished = false;//是否完成
    private static String lastContent="";//上次内容
    private static long lastTime=0;//上次时候
}
