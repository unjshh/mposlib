package com.cxycxx.mposcore.device;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.google.gson.JsonObject;

import java.lang.reflect.Method;

/**
 * 搜索蓝牙
 */

public class SearchBluetooth extends FBCommu {

    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    /**
     * 蓝牙广播接收器
     */
    /*
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        ProgressDialog progressDialog = null;
        private List<BluetoothDevice> devices = new ArrayList<>();

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Optional<BluetoothDevice> optional = Stream.of(devices).filter(p -> p.getName().equals(device.getName())).findFirst();
                if(!optional.isPresent())devices.add(device);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                devices.clear();
                progressDialog = ProgressDialog.show(context, "请稍等...", "搜索蓝牙设备中...", true);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressDialog.dismiss();
                Optional<BluetoothDevice> optional = Stream.of(devices).filter(p -> p.getAddress().equals(mPostDatas[0] + "")).findFirst();
                if (optional.isPresent()&&optional.get().getBondState() == BluetoothDevice.BOND_BONDED) {
                    selectBluetooth(optional.get());
                    return;
                }
                String[] items = Stream.of(devices).map(p -> p.getName()).collect(Collectors.toList()).toArray(new String[]{});
                new AlertDialog.Builder(context).setTitle("选择蓝牙设备").setItems(items, (dialog, which) -> {
                    selectBluetooth(devices.get(which));
                    dialog.dismiss();
                }).create().show();
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (adapter.getState() == BluetoothAdapter.STATE_OFF) {//关闭蓝牙
                    devices.clear();
                } else if (adapter.getState() == BluetoothAdapter.STATE_ON) {//打开蓝牙
                    devices.clear();
                }
            }
        }
    };
*/

    /**
     * @param context 上下文
     * @param taskId  任务id
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     */
    public SearchBluetooth(Context context, String taskId, OnFBCommuFinish dealer) {
        super(context, taskId, dealer);
    }

    @Override
    public void launch() {
        /*
        // 设置广播信息过滤
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        // 注册广播接收器，接收并处理搜索结果
        mContext.registerReceiver(receiver, filter);
        if (!adapter.isEnabled())
            mContext.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));//打开蓝牙
        adapter.startDiscovery();
         */
    }

    @Override
    public void stop() {
//        if (adapter.isDiscovering()) adapter.cancelDiscovery();
//        adapter.disable();
//        mContext.unregisterReceiver(receiver);
    }

    /**
     * 蓝牙选择
     *
     * @param device
     * @return
     */
    private void selectBluetooth(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            JsonObject re = new JsonObject();
            re.addProperty("result", "成功");
            re.addProperty("deviceName", device.getName());
            re.addProperty("deviceAddress", device.getAddress());
            callbackDealer(re);
        }
        if (device.getBondState() == BluetoothDevice.BOND_NONE) {
            new Thread(()->{
                try {
                    Method method = BluetoothDevice.class.getMethod("createBond");
                    method.invoke(device);
                } catch (Exception e) {
                }
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    JsonObject re = new JsonObject();
                    re.addProperty("result", "成功");
                    re.addProperty("deviceName", device.getName());
                    re.addProperty("deviceAddress", device.getAddress());
                    callbackDealerOnUiTread(re);
                }else errCallbackDealerOnUiTread("配对失败");
            }).start();
        }

    }
}
