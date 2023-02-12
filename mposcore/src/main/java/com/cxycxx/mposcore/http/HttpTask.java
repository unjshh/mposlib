package com.cxycxx.mposcore.http;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.util.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * 网络请求
 */
public class HttpTask extends FBCommu {
    public static final Map<String, JsonElement> comParams = new HashMap<>();//公共参数
    private static String serviceUrl = "";//服务器URL
    private String tempServiceUri = "";//临时服务器URL
    private boolean islog = true;//是否记日志
    private boolean needShowWaitDialog = true;//是否需要等待框
    private Dialog waitDialog;
    private static final int DEFAULT_TIMEOUT = 120;
    private Runnable mFinally;

    /**
     * 是否需要等待框
     *
     * @return
     */
    public boolean isNeedShowWaitDialog() {
        return needShowWaitDialog;
    }

    /**
     * 是否需要等待框
     *
     * @param needShowWaitDialog
     */
    public void setNeedShowWaitDialog(boolean needShowWaitDialog) {
        this.needShowWaitDialog = needShowWaitDialog;
    }

    /**
     * @param holder  接收处理者，如果不接收可以为null(比如只输出的情况下)
     * @param context 上下文
     */
    public HttpTask(Context context, Consumer<JsonObject> holder) {
        super(context, null, null);
        OnFBCommuFinish temp = null;
        if (holder != null) {
            temp = (response, taskDescribe) -> holder.accept(response);
        }
        setDealer(temp);
    }

    /**
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     * @param taskId  任务id
     * @param context 上下文
     */
    public HttpTask(Context context, String taskId, OnFBCommuFinish dealer) {
        super(context, taskId, dealer);
    }

    /**
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     * @param taskId  任务id
     * @param context 上下文
     * @param fn      最终都会执行
     */
    public HttpTask(Context context, String taskId, OnFBCommuFinish dealer, Runnable fn) {
        super(context, taskId, dealer);
        this.mFinally = fn;
    }

    /**
     * 获取请求数据
     *
     * @param methodName 方法名
     * @return 数据
     */
    @Deprecated
    public static JsonObject getDefualtPostData(String methodName) {
        return getPostData("MPosLib", "MPosLib.ErpProc", methodName);
    }

    /**
     * 获取请求数据
     *
     * @param assemblyName 动态库名称，不用写扩展名(例如写成Lib,不要写成Lib.dll)
     * @param className    类名
     * @param methodName   方法名
     * @return 数据
     */
    @Deprecated
    public static JsonObject getPostData(String assemblyName, String className, String methodName) {
        JsonObject postData = new JsonObject();
        //----------------系统参数------------------
        postData.addProperty("assemblyName", assemblyName);//不用写扩展名(例如写成Lib,不要写成Lib.dll)
        postData.addProperty("className", className);
        postData.addProperty("methodName", methodName);
        //-----------------公共参数---------------------
        for (Map.Entry<String, JsonElement> entry : comParams.entrySet())
            postData.add(entry.getKey(), entry.getValue());
        return postData;
    }

    /**
     * 直接访问
     *
     * @param context 上下文
     * @param post    发送数据
     * @param taskId  任务id
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     */
    public static void launch2(Context context, JsonObject post, String taskId, OnFBCommuFinish dealer) {
        HttpTask task = new HttpTask(context, taskId, dealer);
        task.launch(post);
    }

    /**
     * 直接访问
     *
     * @param context    上下文
     * @param methodName 方法名称
     * @param taskId     任务id
     * @param dealer     接收处理者，如果不接收可以为null(比如只输出的情况下)
     */
    public static void launch2(Context context, String methodName, String taskId, OnFBCommuFinish dealer) {
        if (Util.isEmpty(methodName)) {
            Util.showMsg(context, "方法名称不能为空");
            return;
        }
        HttpTask task = new HttpTask(context, taskId, dealer);
        JsonObject post = new JsonObject();
        post.addProperty("methodName", methodName);
        task.launch(post);
    }

    /**
     * 直接访问
     *
     * @param context 上下文
     * @param post    发送数据
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     */
    public static void launch2(Context context, JsonObject post, OnFBCommuFinish dealer) {
        String method = Util.joAsString(post, "methodName");
        HttpTask task = new HttpTask(context, Util.isEmpty(method) ? "1" : method, dealer);
        task.launch(post);
    }

    /**
     * 直接访问
     *
     * @param context 上下文
     * @param post    发送数据
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     */
    public static void launch3(Context context, JsonObject post, Consumer<JsonObject> dealer) {
        HttpTask task = new HttpTask(context, dealer);
        task.launch(post);
    }

    /**
     * 设置服务的URL. 比如 http://192.168.0.67:8001/HttpHandler.ashx
     *
     * @param uri 这个URL只需设置一次
     */
    public static void setServiceUrl(String uri) {
        serviceUrl = uri;
        if (!Util.isEmpty(serviceUrl)) serviceUrl = serviceUrl.replace("\n", "");
    }


    /**
     * 获取地址
     *
     * @return
     */
    public static String getServiceUrl() {
        return serviceUrl;
    }

    private void disableConnectionReuseIfNecessary() {
        // Work around pre-Froyo bugs in HTTP connection reuse.
        if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    /**
     * 开启（这个类调用带参数的方法）
     */
    @Override
    public void launch() {
        JsonObject postData = (JsonObject) mPostDatas[0];
        int timeOut = Util.joAsInt(postData, "timeOut");
        if (mContext != null) {
            if (needShowWaitDialog) showWaitDialog(null);
            postData.addProperty("mposVersion", Util.getCurrentVersionName(mContext));
            postData.addProperty("Ip", Util.getIp(mContext));
            postData.addProperty("Mac", Util.getMac(mContext));
        }
        Stream.of(comParams.entrySet()).filter(p -> !postData.has(p.getKey())).forEach(p -> postData.add(p.getKey(), p.getValue()));
        HttpTask.sign(postData);

        String urlStr = Util.isEmpty(tempServiceUri) ? serviceUrl.trim() : tempServiceUri.trim();
        RetrofitPost inf = getNewRetrofit(timeOut).create(RetrofitPost.class);
        inf.rxPost2(urlStr, RequestBody.create(MediaType.parse("text/plain"), postData.toString()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> {
                    if (mFinally != null) {
                        mFinally.run();
                    }
                    tempServiceUri = "";
                    dismissWaitDialog();
                })
                .subscribe(result -> {
                    if (result.has("success")) {
                        result.addProperty("result", result.get("success").getAsBoolean() ? "成功" : "失败");
                    }
                    if ("成功".equals(Util.joAsString(result, "result")) || mDealErrorModel == ALWAYS_CALLBACK)
                        callbackDealer(result);
                    else if (mDealErrorModel == AUTO_SHOW_ERROR) {
                        if (mContext != null) {
                            Util.showMsg(mContext, Util.joAsString(result, "message"));
                        }
                    }
                }, error -> {
                    errCallbackDealer("异常：" + error.getMessage());
                });
    }

    /**
     * 设置临时服务器URL
     *
     * @param uri 服务器URL
     */
    public void setTempServiseUri(String uri) {
        tempServiceUri = uri;
        if (!Util.isEmpty(tempServiceUri)) tempServiceUri = tempServiceUri.replace("\n", "");
    }

    /**
     * 停止
     */
    @Override
    public void stop() {

    }

    /**
     * 按设置的超时生成一个新连接
     *
     * @param timeOutSecond 超时时间（单位是秒）
     * @return
     */
    public static Retrofit getNewRetrofit(int timeOutSecond) {
        if (timeOutSecond <= 8) return getRetrofit();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)//避免网络差时框架主动请求
                .connectTimeout(timeOutSecond, TimeUnit.SECONDS)
                .readTimeout(timeOutSecond, TimeUnit.SECONDS)
                .writeTimeout(timeOutSecond, TimeUnit.SECONDS)
                .build();
        Gson gson = new GsonBuilder().setLenient().create();
        return new Retrofit.Builder()
                //设置baseUrl
                .baseUrl(getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)//
                .build();
    }

    /**
     * 初始化 Retrofit
     *
     * @return Retrofit实例
     */
    public static Retrofit getRetrofit() {
        if (mRetrofit != null) return mRetrofit;
//        System.setProperty("http.keepAlive", "false");
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)//避免网络差时框架主动请求
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)//连接超时时间
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)//写操作 超时时间
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)//读操作超时时间
                .build();

        mRetrofit = new Retrofit.Builder()
                //设置baseUrl
                .baseUrl(getBaseUrl())
//                .addConverterFactory(StringConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)//
                .build();
        return mRetrofit;
    }

    private static String getBaseUrl() {
        String baseUrl = StringUtils.substring(serviceUrl, 0, StringUtils.lastIndexOf(serviceUrl, "/") + 1);
        if (isBaseUrl(baseUrl)) return baseUrl;
        return "http://192.168.1.1/";
    }

    /**
     * @param url
     * @return
     */
    private static boolean isBaseUrl(String url) {
        if (url == null) return false;
        Pattern pattern = Pattern.compile("^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\/])+$");
        return pattern.matcher(url).matches();
    }

    /**
     * 签名
     *
     * @param ps
     */
    public static void sign(JsonObject ps) {
        if (ps == null) return;
        ps.remove("sign");
        Optional<String> op = Stream.of(ps.entrySet()).filter(p -> p.getValue().isJsonPrimitive())
                .sortBy(p -> p.getKey().toLowerCase())
                .filter(p -> !TextUtils.isEmpty(p.getValue().getAsString()))
                .map(p -> p.getKey() + "=" + p.getValue().getAsString())
                .reduce((p, q) -> p + "&" + q);
        if (!op.isPresent()) return;
        String signStr = appSecret + op.get().toLowerCase() + appSecret;
        String sign = Util.md5(signStr);
        ps.addProperty("sign", sign);
    }

    public static void setAppSecret(String appSecret) {
        if (TextUtils.isEmpty(appSecret)) return;
        HttpTask.appSecret = appSecret;
    }

    /**
     * 不记日志,默认记日志
     */
//    public void closeLog() {
//        this.islog = false;
//    }
    private static Retrofit mRetrofit;


    private static String appSecret = "";
}
