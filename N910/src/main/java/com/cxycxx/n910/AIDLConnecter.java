package com.cxycxx.n910;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * AIDL连接管理器
 */

public class AIDLConnecter {
    public AIDLConnecter(Context context) {
        mContext = context;
    }

    /**
     * 绑定服务
     */
    public void bindService(final OnServiceConnectedListener listener) {
        if (mContext == null || listener == null) return;
        Intent intent = new Intent();
        intent.setAction("com.shanxixinhe.DeviceService");
        mDeviceSC = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {}

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if(count++==0)listener.onServiceConnected(name, service);
            }
        };
        mContext.bindService(intent, mDeviceSC, Context.BIND_AUTO_CREATE);
    }

    /**
     * 解绑服务
     */
    public void unbindService() {
        try {
            if (mDeviceSC!=null) mContext.unbindService(mDeviceSC);
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
    private ServiceConnection mDeviceSC;//设备服务连接器
    private int count=0;
}
