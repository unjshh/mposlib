package com.cxycxx.mposcore.device;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;


import androidx.core.app.ActivityCompat;

import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.mpos.MposPub;
import com.cxycxx.mposcore.util.Util;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 通用蓝牙打印
 */

public class CommonBluetoothPrinter extends FBCommu {

    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    /**
     * 打印机指令
     */
    private static final Map<String, byte[]> commands = new HashMap<>();

    private BluetoothSocket socket;
    private OutputStream outputStream;
    /**
     * 蓝牙是否连接
     */
    private boolean isConnection = false;

    /**
     * @param context 上下文
     * @param taskId  任务id
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     */
    public CommonBluetoothPrinter(Context context, String taskId, OnFBCommuFinish dealer) {
        super(context, taskId, dealer);
        if (commands.isEmpty()) {
            commands.put("复位", new byte[]{0x1b, 0x40});// 复位打印机
            commands.put("标准字", new byte[]{0x1b, 0x4d, 0x00});// 标准ASCII字体
            commands.put("压缩字", new byte[]{0x1b, 0x4d, 0x01});// 压缩ASCII字体
            commands.put("不放大", new byte[]{0x1d, 0x21, 0x00});// 字体不放大
            commands.put("倍高宽", new byte[]{0x1d, 0x21, 0x11});// 宽高加倍
            commands.put("倍宽", new byte[]{0x1d, 0x21, 0x10});//宽加倍
            commands.put("倍高", new byte[]{0x1d, 0x21, 0x01});//宽加倍
            commands.put("加粗", new byte[]{0x1b, 0x45, 0x01});//选择加粗模式
            commands.put("反加粗", new byte[]{0x1b, 0x45, 0x00});//取消加粗模式
            commands.put("倒置", new byte[]{0x1b, 0x7b, 0x01});// 选择倒置打印
            commands.put("反倒置", new byte[]{0x1b, 0x7b, 0x00});// 取消倒置打印
            commands.put("黑白", new byte[]{0x1d, 0x42, 0x00});// 取消黑白反显
            commands.put("反黑白", new byte[]{0x1d, 0x42, 0x01});// 选择黑白反显
            commands.put("旋转", new byte[]{0x1b, 0x56, 0x01});// 选择顺时针旋转90°
            commands.put("反旋转", new byte[]{0x1b, 0x56, 0x00});// 取消顺时针旋转90°
            commands.put("居中", new byte[]{0x1b, 0x61, 0x01});//中间对齐
            commands.put("居左", new byte[]{0x1b, 0x61, 0x00});//左对齐
            commands.put("居右", new byte[]{0x1b, 0x61, 0x02});//右对齐
            commands.put("默认行间距", new byte[]{0x1b, 0x32});//默认行间距
        }
    }

    @Override
    public void launch() {
        List<String> printContent = null;
        try {
            printContent = (List<String>) mPostDatas[0];
        } catch (Exception e) {
            errCallbackDealer("I9000SPrinter 中传递的参数只能是List<String>");
            return;
        }
        if (printContent == null || printContent.isEmpty()) {
            Util.showMsg(mContext, "打印内容为空，请检查服务打印转换");
            return;
        }
        final List<String> temp = printContent;
        String charset = Util.joAsString(Util.getJsonMember(MposPub.mposConfig, "device.printerCharset"), null);
        SharedPreferences pref = mContext.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String address = pref.getString("bluetoothAddress", "");
        if (!Util.isEmpty(address)) {
            connect(address);
            if (isConnection) {
                new Thread(() -> print(temp, "")).start();
                return;
            }
        }
        FBCommu fb = new SearchBluetooth(mContext, "蓝牙搜索", (response, taskDescribe) -> {
            String deviceAddress = Util.joAsString(response, "deviceAddress");
            connect(deviceAddress);
            if (!isConnection) return;
            SharedPreferences.Editor prefEdit = pref.edit();
            prefEdit.putString("bluetoothAddress", deviceAddress);
            prefEdit.commit();
            new Thread(() -> print(temp, charset)).start();
        });
        fb.launch(address);
    }

    @Override
    public void stop() {
        isConnection = false;
        try {
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接蓝牙设备
     */
    private void connect(String bluetoothAddress) {
        if (Util.isEmpty(bluetoothAddress)) {
            stop();
            return;
        }
        if (isConnection) return;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        try {
            BluetoothDevice device = adapter.getRemoteDevice(bluetoothAddress);
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
            socket = device.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
            outputStream = socket.getOutputStream();
        } catch (Exception e) {
            Util.showMsg(mContext, "蓝牙连接失败" + e);
            isConnection = false;
        }
        if (adapter.isDiscovering()) adapter.cancelDiscovery();//关闭搜索
        isConnection = true;
    }

    /**
     * 打印
     *
     * @param printContent 内容
     * @param charSet      打印机字符集
     */
    private void print(List<String> printContent, String charSet) {
        charSet = Util.isEmpty(charSet) ? "gbk" : charSet;
        try {
            outputStream.write(commands.get("默认行间距"));
            for (String line : printContent) {
                if (!line.startsWith("$") || line.length() < 2 || line.indexOf("$", 1) < 0) {
                    printText("","\n" + line,charSet);
                    continue;
                }
                String styles = StringUtils.substring(line, 0, line.indexOf("$", 1) + 1);
                String value = StringUtils.substring(line, styles.length());
                String type = parseStyle("type", styles);
                switch (type) {
                    case "feed": {
                        for (int i = 0, l = Util.getDecimal(value).intValue(); i < l; i++)
                            outputStream.write(("\n").getBytes(charSet));
                    }
                    break;
                    case "text": printText(styles,value,charSet);
                    break;
                    default:
                        outputStream.write(("\n" + value).getBytes(charSet));
                        break;
                }
            }
            outputStream.flush();
        } catch (Exception ex) {
//            Util.showMsg(mContext,"打印异常"+ex);
            ex.printStackTrace();
            errCallbackDealerOnUiTread("打印异常" + ex);
        }
    }

    /**
     * 打印文字
     * @param styles 样式
     * @param value 文字
     */
    private void printText(String styles, String value,String charSet) throws IOException {
        if(Util.isEmpty(value))return;
        outputStream.write(value.getBytes(charSet));
//        String align = parseStyle("align", styles);//对齐
//        String bold = parseStyle("bold", styles);//是否粗体
//        int font = Util.getDecimal(parseStyle("font", styles)).intValue();//字体大小
//        if ("center".equalsIgnoreCase(align))
//            outputStream.write(commands.get("居中"));
//        else if ("right".equalsIgnoreCase(align))
//            outputStream.write(commands.get("居右"));
//        else outputStream.write(commands.get("居左"));
//        if(font>8) outputStream.write(commands.get("倍高宽"));
//        else if(font>6)outputStream.write(commands.get("倍高"));
//        else if(font>5)outputStream.write(commands.get("倍宽"));
//        else if(font<5)outputStream.write(commands.get("压缩字"));
//        else outputStream.write(commands.get("标准字"));
//        outputStream.write(commands.get("1".equals(bold)?"加粗":"反加粗"));
//        outputStream.write(("\n" + value).getBytes(charSet));
//        outputStream.write(commands.get("居左"));
    }

    /**
     * 获取样式的值
     *
     * @param type   样式名称
     * @param styles 样式集
     * @return 样式的值
     */
    private String parseStyle(String type, String styles) {
        if (Util.isEmpty(type)) return "";
        for (String kv : StringUtils.split(styles.replace("$", ""), ";")) {
            if (Util.isEmpty(kv)) continue;
            String[] ss = StringUtils.split(kv, ":");
            if (ss.length < 2) continue;
            if (ss[0].trim().equals(type)) return ss[1];
        }
        return "";
    }
}
