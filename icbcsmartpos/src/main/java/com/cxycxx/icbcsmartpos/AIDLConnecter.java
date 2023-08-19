package com.cxycxx.icbcsmartpos;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * AIDL连接管理器
 */

public class AIDLConnecter {
    public AIDLConnecter(Context context, String type) {
        mContext = context;
        mType = type;
    }

    /**
     * 绑定服务
     */
    public void bindService(final OnServiceConnectedListener listener) {
        if (mContext == null || listener == null) return;
        Intent intent = new Intent();
        if ("设备".equals(mType)) {
            intent.setAction("com.icbc.smartpos.device_service");
            intent.setPackage("com.icbc.smartpos.deviceservice");
            mDeviceSC = new ServiceConnection() {
                @Override
                public void onServiceDisconnected(ComponentName name) {}

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    if(count++==0)listener.onServiceConnected(name, service);
                    //deviceService = IDeviceService.Stub.asInterface(service);
                }
            };
            mContext.bindService(intent, mDeviceSC, Context.BIND_AUTO_CREATE);
        } else if ("应收".equals(mType)) {
            intent.setAction("com.icbc.smartpos.transservice.TransService");
            intent.setPackage("com.icbc.smartpos.bankpay");
            mTransSC = new ServiceConnection() {
                @Override
                public void onServiceDisconnected(ComponentName name) {}

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    if(count++==0)listener.onServiceConnected(name, service);
//            transService = ITransService.Stub.asInterface(service);
                }
            };
            mContext.bindService(intent, mTransSC, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * 解绑服务
     */
    public void unbindService() {
        try {
            if (mDeviceSC!=null) mContext.unbindService(mDeviceSC);
            else if (mTransSC!=null) mContext.unbindService(mTransSC);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 处理服务连接成功时
     */
    interface OnServiceConnectedListener {
        void onServiceConnected(ComponentName name, IBinder service);
    }
    private Context mContext;
    private String mType;//类型【设备、应收】
    private ServiceConnection mDeviceSC,mTransSC;//设备服务连接器、收单服务连接器
    private int count=0;
}
