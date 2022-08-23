package com.cxycxx.mposcore.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Optional;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by PuJiaLin on 2016/2/13.
 */
public final class Util {
    /**
     * 日期时间格式
     */
    public static final String DATETIMEFORMT = "yyyy-MM-dd HH:mm:ss";
    /**
     * Gson 实例
     */
    public static final Gson GSON = new Gson();
    /**
     * BigDecimal 类型的 100
     */
    public static final BigDecimal ONEHUNDRED = new BigDecimal(100);

    /**
     * 合并JsonObject[如果obj1和obj2有相同的属性,保留obj2的]
     *
     * @param obj1
     * @param obj2
     * @return
     */
    public static JsonObject mergeJsonObject(JsonObject obj1, JsonObject obj2) {
        JsonObject res = new JsonObject();
        if (obj1 != null) for (Map.Entry<String, JsonElement> mem : obj1.entrySet())
            res.add(mem.getKey(), mem.getValue());
        if (obj2 != null) for (Map.Entry<String, JsonElement> mem : obj2.entrySet())
            res.add(mem.getKey(), mem.getValue());
        return res;
    }

    /**
     * 向Handler发送消息
     *
     * @param handler
     * @param what
     * @param data
     */
    public static void sendSimple(Handler handler, int what, Object data) {
        if (handler == null) return;
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = data;
        handler.sendMessage(msg);
    }

    /**
     * 把json转换成对象
     *
     * @param json
     * @param classOfT
     * @return
     */
    public static <T> Optional<T> fromJson(JsonElement json, Class<T> classOfT) {
        try {
            return Optional.ofNullable(GSON.fromJson(json, classOfT));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 把json转换成对象
     *
     * @param json
     * @param classOfT
     * @return
     */
    public static <T> Optional<T> fromJson(String json, Class<T> classOfT) {
        try {
            return Optional.ofNullable(GSON.fromJson(json, classOfT));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 把json转换成对象
     *
     * @param json
     * @param token
     * @return
     */
    public static <T> Optional<T> fromJson(String json, TypeToken<T> token) {
        try {
            return Optional.ofNullable(GSON.fromJson(json, token.getType()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 把json转换成对象
     *
     * @param json
     * @param token
     * @return
     */
    public static <T> Optional<T> fromJson(JsonElement json, TypeToken<T> token) {
        try {
            return Optional.ofNullable(GSON.fromJson(json, token.getType()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 把对象转换成JsonObject
     *
     * @param scr 源
     * @return JsonObject对象
     */
    public static JsonObject fromObject(Object scr) {
        if (scr == null) return new JsonObject();
        if (scr instanceof Bundle) {
            JsonObject result = new JsonObject();
            Bundle b = (Bundle) scr;
            for (String p : b.keySet()) {
                Object val = b.get(p);
                if (val == null) continue;
                if (val instanceof String || val.getClass().isPrimitive())
                    result.add(p, new JsonPrimitive(val.toString()));
                else if (val instanceof Long)
                    result.add(p, new JsonPrimitive((long) val));
                else if (!(val instanceof Bundle) && !(val instanceof Serializable))
                    result.add(p, new JsonPrimitive(val.toString()));
                else result.add(p, fromObject(val));
            }
            return result;
        }
        Optional<JsonObject> optional = fromJson(scr instanceof String ? scr.toString() : GSON.toJson(scr), JsonObject.class);
        return optional.isPresent() ? optional.get() : new JsonObject();
    }

    /**
     * 从parent中获取property对应的属性值
     *
     * @param parent
     * @param property 为null时parent转成字符串
     * @return 异常时返回 ""
     */
    public static String joAsString(JsonElement parent, String property) {
        if (parent == null || parent.isJsonNull()) return "";
        try {
            if (isEmpty(property)) return parent.getAsString();
//            JsonObject obj = parent.getAsJsonObject();
//            return  obj.has(property) ? obj.get(property).getAsString() : "";
            JsonElement obj = getJsonMember(parent, property);
            return obj == null ? "" : obj.getAsString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 从parent中获取property对应的属性值
     *
     * @param parent
     * @param property
     * @return 异常时返回 0
     */
    public static int joAsInt(JsonElement parent, String property) {
        if (parent == null || parent.isJsonNull()) return 0;
        try {
            if (isEmpty(property)) return parent.getAsInt();
            JsonObject obj = parent.getAsJsonObject();
            return obj.has(property) ? obj.get(property).getAsInt() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 从parent中获取property对应的属性值
     *
     * @param parent
     * @param property
     * @return 异常时返回 false
     */
    public static boolean joAsBool(JsonElement parent, String property) {
        if (parent == null || parent.isJsonNull()) return false;
        try {
            if (isEmpty(property)) return parent.getAsBoolean();
            JsonObject obj = parent.getAsJsonObject();
            return obj.has(property) ? obj.get(property).getAsBoolean() : false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从parent中获取property对应的属性值
     *
     * @param parent
     * @param property
     * @return 异常时返回 0
     */
    public static BigDecimal joAsBigDecimal(JsonElement parent, String property) {
        if (parent == null || parent.isJsonNull()) return BigDecimal.ZERO;
        try {
            if (isEmpty(property)) return BigDecimal.ZERO;
            JsonObject obj = parent.getAsJsonObject();
            return obj.has(property) ? obj.get(property).getAsBigDecimal() : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 字符串是否数字
     *
     * @param s
     * @return
     */
    public static boolean isNumeric(String s) {
        if (s != null && !"".equals(s.trim()))
            return s.matches("^[0-9]*$");
        else
            return false;
    }

    /**
     * 获取json属性
     *
     * @param src        资源
     * @param memberPath 属性路径(比如 pay.bank.module)
     * @return 属性
     */
    public static JsonElement getJsonMember(JsonElement src, String memberPath) {
        if (isEmpty(memberPath) || src == null) return null;
        memberPath = memberPath.trim();
        JsonElement current = src;
        try {
            if (!memberPath.contains(".")) return current.getAsJsonObject().get(memberPath);
            for (String mem : memberPath.split("\\.")) {
                if (current == null) return null;
                if (isEmpty(mem)) continue;
                if (mem.contains("[")) {
                    int s = mem.indexOf("[");
                    int index = Integer.parseInt(mem.substring(s, mem.indexOf("]")).trim());
                    current = current.getAsJsonObject().get(mem.substring(0, s)).getAsJsonArray().get(index);
                } else {
                    current = current.getAsJsonObject().get(mem);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return current;
    }

    /**
     * 把Calendar转化成Date
     *
     * @param calendar
     * @return
     */
    public static Date calendarToDate(Calendar calendar) {
        return calendar == null ? null : calendar.getTime();
    }

    /**
     * 把Date转化成Calendar
     *
     * @param date
     * @return
     */
    public static Calendar dateToCalendar(Date date) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    /**
     * 把 calendar 转换成 字符串
     *
     * @param calendar
     * @return
     */
    public static String calendar2S(Calendar calendar) {
        SimpleDateFormat df = new SimpleDateFormat(DATETIMEFORMT);
        return calendar == null ? "" : df.format(calendar.getTime());
    }

    /**
     * 把 calendar 转换成 字符串
     *
     * @param calendar
     * @param formt    格式
     * @return
     */
    public static String calendar2S(Calendar calendar, String formt) {
        SimpleDateFormat df = new SimpleDateFormat(formt);
        return calendar == null ? "" : df.format(calendar.getTime());
    }

    /**
     * 把 date 转换成 字符串
     *
     * @param date
     * @return
     */
    public static String date2S(Date date) {
        return calendar2S(dateToCalendar(date));
    }

    /**
     * 把 date 转换成 字符串
     *
     * @param date
     * @param formt 格式
     * @return
     */
    public static String date2S(Date date, String formt) {
        return calendar2S(dateToCalendar(date), formt);
    }

    /**
     * 判断字符串ip是否是一个正确的IP地址
     *
     * @param ip
     * @return 如果参数ip是一个正确的IP则返回true, 否则返回false.
     */
    public static boolean isIp(String ip) {
        return Pattern.matches("^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$", ip);
    }

    /**
     * 验证手机号码是否合法
     * 176, 177, 178;
     * 180, 181, 182, 183, 184, 185, 186, 187, 188, 189;
     * 145, 147;
     * 130, 131, 132, 133, 134, 135, 136, 137, 138, 139;
     * 150, 151, 152, 153, 155, 156, 157, 158, 159;
     * <p>
     * "13"代表前两位为数字13,
     * "[0-9]"代表第二位可以为0-9中的一个,
     * "[^4]" 代表除了4
     * "\\d{8}"代表后面是可以是0～9的数字, 有8位。
     */
    public static boolean isMobileNumber(String mobiles) {
        String telRegex = "^((13[0-9])|(15[^4])|(18[0-9])|(17[0-8])|(147,145))\\d{8}$";
        return !TextUtils.isEmpty(mobiles) && mobiles.matches(telRegex);
    }

    /**
     * 用 Toast 显示信息 msg
     *
     * @param context
     * @param msg
     */
    public static void showMsg(Context context, Object msg) {
        if (context == null) return;
        Toast toast = Toast.makeText(context, msg == null ? "信息为null" : msg.toString(), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0); // eventX - 100使显示字体在触控点正下方显示
        toast.show();
    }

    /**
     * @param context
     * @return 返回本机的MAC地址ַ
     */
    public static String getMac(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifi.getConnectionInfo().getMacAddress();
    }

    /**
     * 返回本机的IP地址ַ
     */
    public static String getIp(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        int ipInt = info.getIpAddress();
        return (ipInt & 0xFF) + "." + ((ipInt >> 8) & 0xFF) + "." + ((ipInt >> 16) & 0xFF) + "." + (ipInt >> 24 & 0xFF);
    }

    /**
     * 判断WIFI是否可用
     */
    public static boolean isWifiEnable(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }


    /**
     * 获取当前版本号
     *
     * @param context
     * @return 当前版本号
     */
    public static int getCurrentVersionCode(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (Exception e) {
            System.out.println("获取当前版本的版本号出错：" + e);
            return 0;
        }
    }

    /**
     * 获取当前版本名称
     *
     * @param context
     * @return 当前版本号
     */
    public static String getCurrentVersionName(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            System.out.println("获取当前版本的版本名称出错：" + e);
            return "";
        }
    }

    /**
     * 判断SD卡是否插入
     *
     * @return 已经准备好则返回true, 否则返回false.
     */
    public static boolean isSdcardReady() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 钱的转换
     *
     * @param money 以分为单位
     * @return 以元为单位，格式是0.00
     */
    public static String getMoneyI2S(int money, boolean... zeroEmpty) {
        if (money == 0 && zeroEmpty.length > 0 && zeroEmpty[0]) return "";
        return getMoneyD2S(getMoneyI2D(money));
    }

    /**
     * 钱的转换(省去最后的0和点号)
     *
     * @param money 以分为单位
     * @return 以元为单位，格式是0.00
     */
    public static String getMoneyI2s(int money) {
        if ((money / 100) * 100 == money) {
            return (money / 100) + "";
        } else if ((money / 10) * 10 == money) {
            BigDecimal fen = BigDecimal.valueOf(money).setScale(2, BigDecimal.ROUND_HALF_UP);
            return fen.divide(BigDecimal.valueOf(100), BigDecimal.ROUND_HALF_UP).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }
        return getMoneyI2S(money, true);
    }

    /**
     * 钱的转换
     *
     * @param money 以元为单位
     * @return 以分为单位
     */
    public static int getMoneyS2I(String money) {
        return getMoneyD2I(getMoneyS2D(money));
    }

    /**
     * 钱的转换
     *
     * @param money 以元为单位
     * @return 以分为单位
     */
    public static int getMoneyD2I(BigDecimal money) {
        return money.multiply(ONEHUNDRED).intValue();
    }

    /**
     * 钱的转换
     *
     * @param money 以分为单位
     * @return 以元为单位
     */
    public static BigDecimal getMoneyI2D(int money) {
        return new BigDecimal(money).divide(ONEHUNDRED);
    }

    /**
     * 钱的转换
     *
     * @param money 以元为单位
     * @return 以元为单位
     */
    public static String getMoneyD2S(BigDecimal money) {
        if (money == null) return "";
        BigDecimal money2 = money.setScale(2, BigDecimal.ROUND_HALF_UP);
        return (money2 == null ? money : money2).toPlainString();
    }

    /**
     * 钱的转换
     *
     * @param money 以元为单位
     * @return 以元为单位
     */
    public static BigDecimal getMoneyS2D(String money) {
        return getDecimal(money, BigDecimal.ZERO);
    }

    /**
     * 取小数
     *
     * @param x
     * @param dft 默认值
     * @return
     */
    public static BigDecimal getDecimal(String x, BigDecimal dft) {
        try {
            return new BigDecimal(x.trim());
        } catch (Exception e) {
            return dft;
        }
    }

    /**
     * 取小数
     *
     * @param x
     * @return 如果解析错误将会返回 0
     */
    public static BigDecimal getDecimal(String x) {
        return getDecimal(x, BigDecimal.ZERO);
    }

    public static int getInt(Object obj, int dft) {
        return getDecimal(obj + "", BigDecimal.valueOf(dft)).intValue();
    }

    public static int getInt(Object obj) {
        return getInt(obj, 0);
    }

    /**
     * 从TextView中取值
     *
     * @param parent
     * @param tvId   TextView的id
     * @return 内容
     */
    public static String getText(View parent, int tvId) {
        return getText((TextView) parent.findViewById(tvId));
    }

    /**
     * 从TextView中取值
     *
     * @param activity
     * @param tvId     TextView的id
     * @return 内容
     */
    public static String getText(Activity activity, int tvId) {
        return getText((TextView) activity.findViewById(tvId));
    }

    /**
     * 获取tv中的值
     *
     * @param tv
     * @return 内容
     */
    public static String getText(TextView tv) {
        return tv == null ? "" : tv.getText().toString().trim();
    }

    /**
     * 给TextView设置内容
     *
     * @param text     内容
     * @param activity
     * @param tvId     TextView的id
     */
    public static void setText(CharSequence text, Activity activity, int tvId) {
        TextView tv = (TextView) activity.findViewById(tvId);
        tv.setText(text);
    }

    /**
     * 给TextView设置内容
     *
     * @param text   内容
     * @param parent
     * @param tvId   TextView的id
     */
    public static void setText(CharSequence text, View parent, int tvId) {
        TextView tv = (TextView) parent.findViewById(tvId);
        tv.setText(text);
    }

    /**
     * 字符串是否为空
     *
     * @param obj
     * @return true obj==null 或者 str.trim().equals("")
     */
    public static boolean isEmpty(Object obj) {
        if (null == obj) return true;

        boolean result = false;
        String _t = obj.toString().trim();
        if (_t.length() < 1) result = true;
        else if ("{}".equals(_t)) result = true;
        else if ("[]".equals(_t)) result = true;
        else if ("null".equalsIgnoreCase(_t)) result = true;
        return result;
    }

    /**
     * 把字符串转换成时间
     *
     * @param str    时间字符串
     * @param format 时间格式
     * @return
     */
    public static Optional<Date> s2Date(String str, String format) {
        DateFormat df = new SimpleDateFormat(format);
        try {
            return Optional.of(df.parse(str));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    /**
     * 隐藏输入法
     *
     * @param et
     */
    public static void hideSoftInput(EditText et) {
        if (et == null) return;
        InputMethodManager imm = (InputMethodManager) et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }

    /**
     * Inflate a new view hierarchy from the specified xml resource
     *
     * @param context
     * @param resource ID for an XML layout resource to load (e.g.,
     *                 R.layout.main_page)
     * @param root     Optional view to be the parent of the generated hierarchy.
     * @return The root View of the inflated hierarchy. If root was supplied,
     * this is the root View; otherwise it is the root of the inflated
     * XML file.
     */
    public static View inflate(Context context, int resource, ViewGroup root) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(resource, null);
    }

    /**
     * dp 转换成 px
     *
     * @param context
     * @param dp
     * @return
     */
    public static int dp2px(Context context, int dp) {
        return (int) context.getResources().getDisplayMetrics().density * dp;
    }

    /**
     * 网络类型
     */
    private String getNetType(Context context) {
        if (context == null) return "";
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo.State state = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            if (NetworkInfo.State.CONNECTED == state) return "WIFI";
            state = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
            if (NetworkInfo.State.CONNECTED == state) return "GPRS";
            return "NONE";
        }
        return "NONE";
    }

    /**
     * 基于硬件信息获得唯一PsuedoID
     */
    public static String getUniquePsuedoID() {
        String serial;
        String m_szDevIDShort = "35" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                Build.USER.length() % 10; // 13 位

        try {
            // API>=9 使用serial号
            serial = Build.class.getField("SERIAL").get(null).toString();
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            serial = "serial";
        }
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }

    /**
     * HttpURLConnection 请求
     *
     * @param bundle http配置及请求
     * @return
     */
    public static Object[] httpUrl(JsonObject bundle) {
        if (bundle == null) return new Object[]{false, "参数bundle不能为null"};
        String url = Util.joAsString(bundle, "url");
        if (Util.isEmpty(url)) return new Object[]{false, "url参数不能为空"};
        String method = Util.joAsString(bundle, "method");//请求方式
        int timeout = Util.joAsInt(bundle, "timeout");//超时时间
        HttpURLConnection conn = null;
        try {
            String urlStr = URLEncoder.encode(url, "utf-8").replaceAll("\\+", "%20");
            urlStr = urlStr.replaceAll("%3A", ":").replaceAll("%2F", "/");
            URL realurl = new URL(urlStr);
            conn = (HttpURLConnection) realurl.openConnection();
            conn.setDoOutput(true);//这个是当然不能少的，不然怎么传送数据呢
            conn.setDoInput(true);//这句不能少，不然什么出现500-Internal Server Error 错误
            conn.setUseCaches(false);
            conn.setRequestMethod(Util.isEmpty(method) ? "POST" : method);//请求方式
            if (timeout > 0) conn.setConnectTimeout(timeout);//超时时间
            String charset = Util.joAsString(bundle, "charset");//字符集
            if (bundle.has("data")) {//有数据要传送
                BufferedOutputStream bos = new BufferedOutputStream(conn.getOutputStream());
                bos.write(Util.joAsString(bundle, "data").getBytes(Util.isEmpty(charset) ? "UTF-8" : charset));
                bos.flush();
                bos.close();
            }
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
                return new Object[]{false, conn.getResponseMessage()};
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int le = 0;
            while ((le = bis.read(buffer)) > 0) {
                baos.write(buffer, 0, le);
            }
            String response = baos.toString(Util.isEmpty(charset) ? "UTF-8" : charset);
            baos.close();
            bis.close();
            return new Object[]{true, response};
        } catch (Exception eio) {
            return new Object[]{false, eio.getMessage()};
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * HttpURLConnection 请求
     *
     * @param url
     * @param post 发送数据
     * @return [bool, response]  bool 成功时为true，response为异常信息或服务器返回的内容
     */
    public static Object[] httpUrl(String url, String post) {
        HttpURLConnection conn = null;
        try {
            String urlStr = URLEncoder.encode(url, "utf-8").replaceAll("\\+", "%20");
            urlStr = urlStr.replaceAll("%3A", ":").replaceAll("%2F", "/");
            URL realurl = new URL(urlStr);
            conn = (HttpURLConnection) realurl.openConnection();
            conn.setDoOutput(true);//这个是当然不能少的，不然怎么传送数据呢
            conn.setDoInput(true);//这句不能少，不然什么出现500-Internal Server Error 错误
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(8000);
            BufferedOutputStream bos = new BufferedOutputStream(conn.getOutputStream());
            bos.write(post.getBytes("UTF-8"));
            bos.flush();
            bos.close();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
                return new Object[]{false, conn.getResponseMessage()};
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int le = 0;
            while ((le = bis.read(buffer)) > 0) {
                baos.write(buffer, 0, le);
            }
            String response = baos.toString("UTF-8");
            baos.close();
            bis.close();
            return new Object[]{true, response};
        } catch (Exception eio) {
            return new Object[]{false, eio.getMessage()};
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public static void loaderImage(ImageView view, String url, String defaultUrl, Bitmap defaultImage) {
        if (view == null) return;
        if (isEmpty(url) && isEmpty(defaultUrl) && defaultImage == null) return;
        if (isEmpty(url) && isEmpty(defaultUrl) && defaultImage != null) {
            view.setImageBitmap(defaultImage);
            return;
        }
        if (!ImageLoader.getInstance().isInited()) {
            ImageLoaderConfiguration config = new ImageLoaderConfiguration
                    .Builder(view.getContext())
                    .memoryCacheExtraOptions(480, 800) // maxwidth, max height，即保存的每个缓存文件的最大长宽
                    .threadPoolSize(3)//线程池内加载的数量
                    .threadPriority(Thread.NORM_PRIORITY - 2)
                    .denyCacheImageMultipleSizesInMemory()
                    .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // You can pass your own memory cache implementation/你可以通过自己的内存缓存实现
                    .memoryCacheSize(2 * 1024 * 1024)//内存缓存1M
                    .diskCacheSize(30 * 1024 * 1024)//磁盘缓存30M
                    .diskCacheFileNameGenerator(new Md5FileNameGenerator())//将保存的时候的URI名称用MD5 加密
                    .tasksProcessingOrder(QueueProcessingType.LIFO)
                    .diskCacheFileCount(100) //缓存的文件数量
                    //.diskCache(new UnlimitedDiscCache(cacheDir))//自定义缓存路径
                    .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                    .imageDownloader(new BaseImageDownloader(view.getContext(), 5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
                    //.writeDebugLogs() // Remove for releaseapp
                    .build();//开始构建
            ImageLoader.getInstance().init(config);
        }
        if (isEmpty(defaultUrl) && defaultImage == null)
            ImageLoader.getInstance().displayImage(url, view, (ImageLoadingListener) null);
        else ImageLoader.getInstance().displayImage(url, view, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (!isEmpty(defaultUrl))
                    ImageLoader.getInstance().displayImage(defaultUrl, (ImageView) view);//如果没有这行会重叠
                else if (defaultImage != null) ((ImageView) view).setImageBitmap(defaultImage);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
    }

    /**
     * md5
     *
     * @param str
     * @return
     */
    public static String md5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(str.getBytes("UTF-8"));
            byte[] hash = md.digest();
            StringBuffer outStrBuf = new StringBuffer(32);
            for (int i = 0; i < hash.length; i++) {
                int v = hash[i] & 0xFF;
                if (v < 16) {
                    outStrBuf.append('0');
                }
                outStrBuf.append(Integer.toString(v, 16).toLowerCase());
            }
            return outStrBuf.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
