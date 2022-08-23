package com.cxycxx.mposcore.mpos;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;

import com.annimon.stream.Optional;
import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.http.HttpTask;
import com.cxycxx.mposcore.util.GsonHelper;
import com.cxycxx.mposcore.util.Util;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 移动POS酒吧
 */

public class MposPub {
    /**
     * 设备型号
     */
    public static String deviceType = "";

    /**
     * 移动POS的配置（对应服务器上的mposconfig.xml）deviceType这些信息不要从这儿取
     */
    public static JsonObject mposConfig = new JsonObject();

    /**
     * 打印可用的样式
     */
    public static final String[] printStyles = new String[]{"type", "align", "font", "bold"};//对齐方式、字体大小、是否加粗

    /**
     * 销售数量是否可以为负数
     *
     * @return
     */
    public static boolean canSaleCountNegative() {
        if (!MposPub.mposConfig.has("canSaleCountNegative")) return false;
        String flag = GsonHelper.joAsString(MposPub.mposConfig, "canSaleCountNegative");
        return "1".equals(flag) || "是".equals(flag);
    }

    /**
     * 销售数量是否可以为小数
     *
     * @return
     */
    public static boolean canSaleCountDecimal() {
        if (!MposPub.mposConfig.has("canSaleCountDecimal")) return false;
        String flag = GsonHelper.joAsString(MposPub.mposConfig, "canSaleCountDecimal");
        return "1".equals(flag) || "是".equals(flag);
    }

    public static int discountRelation() {
        return GsonHelper.joAsInt(MposPub.mposConfig, "discountRelation");
    }
    /**
     * 收款方式使用js
     *
     * @return
     */
    public static String paymentUseJs() {
        if (!mposConfig.has("paymentUseJs")) return "";
        return GsonHelper.joAsString(mposConfig, "paymentUseJs");
    }
    /**
     * 记日志
     *
     * @param msg
     */
    public static void clientLog(String msg) {
        JsonObject post = new JsonObject();
        post.addProperty("methodName", "ClientLog");
        post.addProperty("clientLog", msg);
        HttpTask.launch2(null, post, "上传日志", null);
    }
    /**
     * 记日志
     *
     * @param msg
     */
    public static void clientLog(JsonObject msg, String keyCode) {
        JsonObject post = new JsonObject();
        post.addProperty("methodName", "ClientLog");
        if (!TextUtils.isEmpty(keyCode)) {
            post.addProperty("keyCode", keyCode);
        }
        post.add("clientLog", msg);
        HttpTask.launch2(null, post, "上传日志", null);
    }

    /**
     * 打印（运行在主线程）
     *
     * @param context
     * @param printContent 打印内容
     */
    public static void devicePrint(Context context, List<String> printContent) {
        try {
            Optional<FBCommu> printer = getDeviceFitting(context, "Printer", "打印", null);
            if (printer.isPresent()) {
                printer.get().launch(printContent);
            } else {
                Util.showMsg(context, "没有实现此设备类型的打印");
            }
        } catch (Exception ex) {
            Util.showMsg(context, "打印异常：" + ex);
        }

    }

    /**
     * 打印图片
     *
     * @param context
     * @param bitmap  打印内容
     */
    public static void devicePrint(Context context, Bitmap bitmap) {
        Optional<FBCommu> printer = getDeviceFitting(context, "Printer", "打印", null);
        if (printer.isPresent()) {
            printer.get().launch(bitmap);
        } else {
            Util.showMsg(context, "没有实现此设备类型的打印");
        }
    }

    /**
     * 获取设备配件(按配置)
     *
     * @param fitting 配件
     * @param taskTag 任务码
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     * @return
     */
    public static Optional<FBCommu> getDeviceFitting(Context context, String fitting, String taskTag, OnFBCommuFinish dealer) {
        if (Util.isEmpty(fitting)) return Optional.empty();
        JsonElement el = Util.getJsonMember(mposConfig, "device." + fitting.trim().toLowerCase());
        boolean isFullPath = Util.joAsString(el, null).startsWith("com.");
        String path = "com.unjshh.";
        if (el == null) {
            String className = deviceType.toUpperCase() + fitting;//fitting首字母大写
            path = String.format("com.unjshh.%1$s.%2$s", deviceType.toLowerCase(), className);
        } else {
            path = (isFullPath ? "" : path) + Util.joAsString(el, null);
        }
        return getFBCommuBase(context, path, taskTag, dealer);
    }

    /**
     * 按类型反射生成 FBCommu
     *
     * @param classPath 完整的类路径
     * @param taskTag   任务码
     * @param dealer    接收处理者，如果不接收可以为null(比如只输出的情况下)
     * @return
     */
    public static Optional<FBCommu> getFBCommuBase(Context context, String classPath, String taskTag, OnFBCommuFinish dealer) {
        Optional<FBCommu> optional = Optional.empty();
        try {
            Class<?> cl = Class.forName(classPath);
            Constructor<?> cor = cl.getConstructor(Context.class, String.class, OnFBCommuFinish.class);
            Object obj = cor.newInstance(context, taskTag, dealer);
            if (obj instanceof FBCommu) optional = Optional.of((FBCommu) obj);
        } catch (Exception ex) {
            Util.showMsg(context, ex);
        }
        return optional;
    }

    /**
     * 用普通摄像头扫描
     */
    public static void scanByCamera(Activity context, String taskTag, OnFBCommuFinish dealer) {
        String scaner = Util.joAsString(MposPub.mposConfig, "device.scaner");
        if (!scaner.startsWith("com.unjshh")) scaner = "com.unjshh." + scaner;
        Optional<FBCommu> optional = getFBCommuBase(context, scaner, taskTag, dealer);
        if (!optional.isPresent()) {
            Util.showMsg(context, "不能启动自带摄像头");
            return;
        }
        SurfaceView surfaceView = new SurfaceView(context);
        Dialog dialog = new AlertDialog.Builder(context).setTitle("扫描")
                .setNegativeButton("取消", (dlg, which) -> optional.get().stop()).setView(surfaceView).create();
        Display d = ((Activity) context).getWindowManager().getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes(); // 获取对话框当前的参数值
        p.height = (int) (d.getHeight() * 0.6); // 高度设置为屏幕的0.5
        p.width = (int) (d.getWidth() * 0.9); // 宽度设置为屏幕的0.8
        dialog.getWindow().setAttributes(p);
        dialog.show();
        optional.get().setDealer((response, taskDescribe) -> {
            dialog.dismiss();
            dealer.onFBCommuFinish(response, taskDescribe);
        });
        optional.get().launch(surfaceView);
    }

    /**
     * 按类型反射生成 FBCommu
     *
     * @param module  模块名称
     * @param taskTag 任务编码
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     * @return
     */
    @Deprecated
    public Optional<FBCommu> getFBCommu(Context context, String module, String taskTag, OnFBCommuFinish dealer) {
        String className = deviceType.toUpperCase() + module;
        Optional<FBCommu> optional = Optional.empty();
        try {
            String classPath = String.format("com.unjshh.%1$s.%2$s", deviceType.toLowerCase(), className);
            if ("N900".equalsIgnoreCase(deviceType) && "Scaner".equals(module)) {
                classPath = "com.unjshh.device.CommonScaner";
            }
            if ("智极付".equals(module)) classPath = "com.unjshh.spay.SPay";
            else if ("米雅".equals(module)) classPath = "com.unjshh.pay.mobile.miya.Miya";
            else if ("信合".equals(module)) classPath = "com.unjshh.pay.bank.hexin.HeXin";
            Class<?> cl = Class.forName(classPath);
            Constructor<?> cor = cl.getConstructor(Context.class, String.class, OnFBCommuFinish.class);
            Object obj = cor.newInstance(context, taskTag, dealer);
            if (obj instanceof FBCommu) optional = Optional.of((FBCommu) obj);
        } catch (Exception ex) {
            Log.e("getFBCommu", ex.toString());
            Util.showMsg(context, ex);
        }
        return optional;
    }

    /**
     * 打印字符串对齐
     *
     * @param str   字符串(按空格切分)
     * @param width 纸张宽度
     * @return
     */
    public static String alignPrint(String str, int width) {
        if (str == null || StringUtils.isEmpty(str)) return str;
        return alignPrint(StringUtils.split(str.trim(), " "), width);
    }

    /**
     * 打印字符串对齐
     *
     * @param arr   字符串数组（数组大小就是列数）
     * @param width 纸张宽度
     * @return
     */
    public static String alignPrint(String[] arr, int width) {
        if (arr == null || arr.length == 0) return null;
        int per = width / arr.length;
        String rst = "";
        for (String txt : arr) rst += centerStr(txt.trim(), per);
        return rst;
    }

    /**
     * 居中字符串
     *
     * @param str  字符串
     * @param size 宽度
     * @return
     */
    public static String centerStr(String str, int size) {
        if (str == null || size < 0) return str;
        int strLen = str.getBytes(Charset.forName("GB2312")).length;
        int pads = size - strLen;
        if (pads <= 0) return str;
        for (int i = 0, l = pads / 2; i < l; ++i) str = "\u0020" + str;
        for (int i = 0, l = pads - pads / 2; i < l; ++i) str = str + "\u0020";
        return str;
    }

    /**
     * 右居字符串
     *
     * @param str  字符串
     * @param size 宽度
     * @return
     */
    public static String rightStr(String str, int size) {
        if (str == null || size < 0) return str;
        int strLen = str.getBytes(Charset.forName("GB2312")).length;
        if (strLen >= size) return str;
        for (int i = 0, l = size - strLen; i < l; ++i) str = " " + str;
        return str;
    }

    /**
     * 转换打印字符串为对象
     *
     * @param printContent 字符串
     * @return 对象
     */
    public static List<JsonObject> transPrintContent(List<String> printContent) {
        if (printContent == null) return Collections.emptyList();
        List<JsonObject> result = new ArrayList<>();
        for (String line : printContent) {
            JsonObject obj = new JsonObject();
            boolean flag = !line.startsWith("$") || line.length() < 2 || line.indexOf("$", 1) < 0;//没有样式
            obj.addProperty("content", flag ? line : StringUtils.substring(line, line.indexOf("$", 1) + 1));
            String styleDesc = flag ? "" : StringUtils.substring(line, 0, line.indexOf("$", 1) + 1);//样式部分
            for (String style : printStyles) {
                obj.addProperty(style, parseStyle(style, styleDesc));
            }
            result.add(obj);
        }
        return result;
    }

    /**
     * 获取样式的值
     *
     * @param type   样式名称
     * @param styles 样式集
     * @return 样式的值
     */
    public static String parseStyle(String type, String styles) {
        if (Util.isEmpty(type)) return "";
        String val = "";
        for (String kv : StringUtils.split(styles.replace("$", ""), ";")) {
            if (Util.isEmpty(kv)) continue;
            String[] ss = StringUtils.split(kv, ":");
            if (ss.length < 2) continue;
            if (ss[0].trim().equals(type)) {
                val = ss[1];
                break;
            }
        }
        if ("type".equals(type) && Util.isEmpty(val)) return "text";
        if ("align".equals(type) && Util.isEmpty(val)) return "left";
        if ("font".equals(type) && Util.isEmpty(val))
            return "n";//总的分4种：s->small的简写；n->normal的简写；b->big的简写；具体的整数值
        if ("bold".equals(type) && Util.isEmpty(val)) return "0";//0是正常体；1是粗体
        return val;//默认为文本
    }
}
