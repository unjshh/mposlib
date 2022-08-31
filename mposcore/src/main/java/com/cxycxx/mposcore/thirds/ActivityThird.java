package com.cxycxx.mposcore.thirds;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.annimon.stream.Stream;
import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.http.HttpTask;
import com.cxycxx.mposcore.http.RetrofitPost;
import com.cxycxx.mposcore.mpos.MposPub;
import com.cxycxx.mposcore.util.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 用Activity 方式与第三方通信
 */

public class ActivityThird extends FBCommu {
    /**
     * @param context 上下文
     * @param taskId  任务id
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     */
    public ActivityThird(Context context, String taskId, OnFBCommuFinish dealer) {
        super(context, taskId, dealer);
    }

    @Override
    public void launch() {
        logoutLandi();//联迪登出
        JsonObject req = (JsonObject) mPostDatas[0];
        if (Util.isEmpty(Util.joAsString(req, "methodName")))
            req.addProperty("methodName", "ActivityThird");
        req.addProperty("step", "请求转化");
        req.addProperty("keyCode", "回应翻译");
        HttpTask.launch2(mContext, req, "请求转化", (reqPara, t1) -> {
            String action = Util.joAsString(reqPara, "action");
            String uri = Util.joAsString(reqPara, "uri");
            Intent it = Util.isEmpty(action) ? new Intent() : new Intent(action);
            if (reqPara.has("component")) {
                String pkg = Util.joAsString(reqPara.getAsJsonObject("component"), "pkg");
                String cl = Util.joAsString(reqPara.getAsJsonObject("component"), "cls");
                it.setComponent(new ComponentName(pkg, cl));
            } else if (!Util.isEmpty(uri)) {
                it = new Intent(Util.isEmpty(action) ? Intent.ACTION_VIEW : action, Uri.parse(uri));
            }
            try {
                JsonArray arr = reqPara.getAsJsonArray("params");
                for (JsonElement e : arr) loadReq(it, e.getAsJsonObject());
                ((Activity) mContext).startActivityForResult(it, Integer.parseInt(mTaskId));
//                JsonObject log=GsonHelper.fromObject(it.getExtras());
//                log.addProperty("inCase","传给银行的参数");
//                MposPub.clientLog(log.toString());
//                debug(mContext, Integer.parseInt(mTaskId));
            } catch (Exception ex) {
                Util.showMsg(mContext, "调用第三方异常:" + ex);
            }
        });

    }

    @Override
    public void stop() {
        logoutLandi();
    }

    /**
     * 联迪登出
     */
    private static void logoutLandi() {
        try {
            Class<?> cl = Class.forName("com.landicorp.android.eptapi.DeviceService");
            if (cl != null) {
                Method method = cl.getMethod("logout");//关闭联迪
                if (method != null) {
                    method.invoke(null);
                }
            }
        } catch (Exception ex) {

        }
    }

    /**
     * 装载请求数据
     *
     * @param parent 数据接收者
     * @param param  转化参数及数据提供者
     * @throws Exception
     */
    private void loadReq(Object parent, JsonObject param) throws Exception {
        if (parent == null || param == null) return;
        String type = Util.joAsString(param, "type");
        String name = Util.joAsString(param, "name");
        String cl = Util.joAsString(param, "cls");
        JsonElement value = param.get("value");
        if (parent instanceof Intent) {
            Intent pt = (Intent) parent;
            switch (type) {
                case "int":
                    pt.putExtra(name, value.getAsInt());
                    break;
                case "float":
                    pt.putExtra(name, value.getAsFloat());
                    break;
                case "double":
                    pt.putExtra(name, value.getAsDouble());
                    break;
                case "bool":
                    pt.putExtra(name, value.getAsBoolean());
                    break;
                case "long":
                    pt.putExtra(name, value.getAsLong());
                    break;
                case "serial": {

                    //这儿putSerializable中传入的类型要和第三方的类要一模一样，包括空间名，不能用父类
                    Object obj = Util.GSON.fromJson(value, Class.forName(cl));
                    pt.putExtra(name, (Serializable) obj);
                }
                break;
                case "bundle": {
                    Bundle bundle = new Bundle();
                    for (JsonElement e : value.getAsJsonArray()) {
                        loadReq(bundle, e.getAsJsonObject());
                        /*String name1 = Util.joAsString(e, "name");
                        if ("transData".equals(name1) && MposPub.mposConfig.has("ccbAuthCode")) {
                            JsonElement value1 = e.getAsJsonObject().get("value");
                            String[] result = AuthHelper.getAuthCode(mContext, value1.getAsString());
                            String authIndex = "", authCode = "";
                            if (result != null) {
                                authIndex = result[1];
                                authCode = result[0];
                            }
                            pt.putExtra("authIndex", authIndex);
                            pt.putExtra("authCode", authCode);
                        }*/
                    }
                    if (Util.isEmpty(name)) pt.putExtras(bundle);
                    else pt.putExtra(name, bundle);
                }
                break;
                default:
                    pt.putExtra(name, value.getAsString());
                    break;
            }
        } else if (parent instanceof Bundle) {
            Bundle pt = (Bundle) parent;
            switch (type) {
                case "int":
                    pt.putInt(name, value.getAsInt());
                    break;
                case "float":
                    pt.putFloat(name, value.getAsFloat());
                    break;
                case "double":
                    pt.putDouble(name, value.getAsDouble());
                    break;
                case "bool":
                    pt.putBoolean(name, value.getAsBoolean());
                    break;
                case "long":
                    pt.putLong(name, value.getAsLong());
                    break;
                case "serial": {
                    String s = value.toString();
                    if ("java.lang.String".equals(cl)) pt.putSerializable(name, value.toString());
                    else {
                        //这儿putSerializable中传入的类型要和第三方的类要一模一样，包括空间名，不能用父类
                        Object obj = Util.GSON.fromJson(value, Class.forName(cl));
                        pt.putSerializable(name, (Serializable) obj);
                    }
                }
                break;
                case "bundle": {
                    Bundle bundle = new Bundle();
                    for (JsonElement e : value.getAsJsonArray()) {
                        loadReq(bundle, e.getAsJsonObject());
                    }
                    pt.putBundle(name, bundle);
                }
                break;
                default:
                    pt.putString(name, value.getAsString());
                    break;
            }
        } else if (parent instanceof Map) {
            HashMap<String, Object> pt = (HashMap<String, Object>) parent;
            switch (type) {
                case "int":
                    pt.put(name, value.getAsInt());
                    break;
                case "float":
                    pt.put(name, value.getAsFloat());
                    break;
                case "double":
                    pt.put(name, value.getAsDouble());
                    break;
                case "bool":
                    pt.put(name, value.getAsBoolean());
                    break;
                case "long":
                    pt.put(name, value.getAsLong());
                    break;
                case "bundle": {
                    Bundle bundle = new Bundle();
                    for (JsonElement e : value.getAsJsonArray()) {
                        loadReq(bundle, e.getAsJsonObject());
                    }
                    pt.put(name, bundle);
                }
                break;

                default:
                    pt.put(name, value.getAsString());
                    break;
            }
        }
    }


    /**
     * 转换传出的参数
     *
     * @param data
     * @return
     */
    public static void obtainValues(Context context, Intent data, JsonObject param, OnFBCommuFinish dealer) {
        logoutLandi();
        if (data == null) {
            JsonObject map = new JsonObject();
            map.addProperty("result", "失败");
            map.addProperty("message", "返回数据为空");
            dealer.onFBCommuFinish(map, "");
            return;
        }
        JsonObject postData = Util.GSON.fromJson(param.toString(), JsonObject.class);
        Stream.of(HttpTask.comParams.entrySet()).filter(p -> !postData.has(p.getKey())).forEach(p -> postData.add(p.getKey(), p.getValue()));
        String methodName = Util.joAsString(postData, "methodName");
        if (Util.isEmpty(methodName)) postData.addProperty("methodName", "ActivityThird");
        postData.addProperty("step", "回应翻译");
        postData.addProperty("keyCode", "回应翻译");
        // 初始化Retrofit
        String baseUrl = StringUtils.substring(HttpTask.getServiceUrl(), 0, StringUtils.lastIndexOf(HttpTask.getServiceUrl(), "/") + 1);
        Retrofit retrofit = new Retrofit.Builder()
                .client(new OkHttpClient.Builder().build())
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofit.create(RetrofitPost.class).rxPost(HttpTask.getServiceUrl(), postData)
                .flatMap(new Function<JsonObject, Observable<JsonObject>>() {
                    @Override
                    public Observable<JsonObject> apply(JsonObject lastRes) {
                        if (!"成功".equals(Util.joAsString(lastRes, "result")))
                            return Observable.just(lastRes);
                        JsonArray arr = lastRes.getAsJsonArray("params");
                        try {
                            JsonObject outres = new JsonObject();
                            for (JsonElement e : arr) {
                                translateRes(data, e.getAsJsonObject(), outres);
                            }
                            postData.add("externalReturns", outres);//外部回应
                            postData.add("param", param);
                            postData.addProperty("step", "回应转化");
                            postData.addProperty("keyCode", "回应转化");
                            return retrofit.create(RetrofitPost.class).rxPost(HttpTask.getServiceUrl(), postData);
                        } catch (Exception ex) {
                            JsonObject err = new JsonObject();
                            err.addProperty("result", "失败");
                            err.addProperty("message", "异常：" + ex);
                            return Observable.just(err);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(p -> dealer.onFBCommuFinish(p, ""));
    }


    /**
     * 翻译回应数据
     *
     * @param parent   数据提供者
     * @param param    转化参数
     * @param receiver 数据接收者
     * @return
     * @throws Exception
     */
    private static JsonObject translateRes(Object parent, JsonObject param, JsonObject receiver) throws Exception {
        if (parent == null || param == null) return null;
        String type = Util.joAsString(param, "type");
        String name = Util.joAsString(param, "name");
        String dft = Util.joAsString(param, "dft");//默认值
        JsonElement val = null;//和请求转换不同，值从第三方返回
        if (parent instanceof Intent) {
            Intent pt = (Intent) parent;
            switch (type) {
                case "int":
                    val = new JsonPrimitive(pt.getIntExtra(name, Util.isEmpty(dft) ? 0 : Integer.parseInt(dft)));
                    break;
                case "long":
                    val = new JsonPrimitive(pt.getLongExtra(name, Util.isEmpty(dft) ? 0 : Integer.parseInt(dft)));
                    break;
                case "float":
                    val = new JsonPrimitive(pt.getFloatExtra(name, Util.isEmpty(dft) ? 0 : Float.parseFloat(dft)));
                    break;
                case "double":
                    val = new JsonPrimitive(pt.getDoubleExtra(name, Util.isEmpty(dft) ? 0 : Double.parseDouble(dft)));
                    break;
                case "bool":
                    val = new JsonPrimitive(pt.getBooleanExtra(name, Util.isEmpty(dft) ? false : Boolean.parseBoolean(dft)));
                    break;
                case "serial":
                    val = Util.fromObject(pt.getSerializableExtra(name));
                    break;
                case "bundle": {
                    Bundle bundle = Util.isEmpty(name) ? pt.getExtras() : pt.getBundleExtra(name);
                    val = new JsonObject();
                    if (bundle == null) break;
                    if (param.has("value") && param.get("value").getAsJsonArray().size() > 0) {
                        for (JsonElement e : param.get("value").getAsJsonArray()) {
                            translateRes(bundle, e.getAsJsonObject(), (JsonObject) val);
                        }
                    } else val = Util.fromObject(bundle);

                }
                break;
                default:
                    val = new JsonPrimitive(pt.getStringExtra(name) == null ? "" : pt.getStringExtra(name));
                    break;
            }
        } else if (parent instanceof Bundle) {
            Bundle pt = (Bundle) parent;
            switch (type) {
                case "int":
                    val = new JsonPrimitive(pt.getInt(name, Util.isEmpty(dft) ? 0 : Integer.parseInt(dft)));
                    break;
                case "float":
                    val = new JsonPrimitive(pt.getFloat(name, Util.isEmpty(dft) ? 0 : Float.parseFloat(dft)));
                    break;
                case "double":
                    val = new JsonPrimitive(pt.getDouble(name, Util.isEmpty(dft) ? 0 : Double.parseDouble(dft)));
                    break;
                case "bool":
                    val = new JsonPrimitive(pt.getBoolean(name, Util.isEmpty(dft) ? false : Boolean.parseBoolean(dft)));
                    break;
                case "serial":
                    val = Util.fromObject(pt.getSerializable(name));
                    break;
                case "json":
                    val = Util.fromObject(pt.get(name));
                    break;
                case "bundle": {
                    Bundle bundle = pt.getBundle(name);
                    val = new JsonObject();
                    if (bundle == null) break;
                    if (param.has("value") && param.get("value").getAsJsonArray().size() > 0) {
                        for (JsonElement e : param.get("value").getAsJsonArray()) {
                            translateRes(bundle, e.getAsJsonObject(), (JsonObject) val);
                        }
                    } else val = Util.fromObject(bundle);
                }
                break;
                default:
                    val = new JsonPrimitive(pt.getString(name, dft));
                    break;
            }
        }
        if (receiver == null) receiver = new JsonObject();
        if (val != null)
            receiver.add(Util.isEmpty(name) ? "_autoCreateMember" + receiver.entrySet().size() : name, val);
        return receiver;
    }

    private class RESPONSE implements Serializable {
        public String CARDNO;
        public String REJCODE_CN;
        public String CUPS;
        public String MERCH_ID;
        public String TRANS_CHANNEL;
        public String BATCH_NO;
        public String TIME;
        public String REJCODE;
        public String SIGN;
        public String DATE;
        public String TER_ID;
        public String AMOUNT;
        public String TRACE_NO;
        public String PRINT_FLAG;
        public String CARDTYPE;
        public String BUSINESS_ID;
        public String TRANS_TICKET_NO;
        public String MERCH_NAME;
        public String CARD_TYPE_IDENTY;
        public String OPER_NO;
        public String WILD_CARD_SIGN;
        public String TRANSTYPE;
    }

}
