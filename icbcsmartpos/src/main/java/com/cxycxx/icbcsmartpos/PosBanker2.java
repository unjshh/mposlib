package com.cxycxx.icbcsmartpos;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icbc.smartpos.transservice.aidl.ITransService;
import com.icbc.smartpos.transservice.aidl.TransHandler;
import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.http.HttpTask;
import com.cxycxx.mposcore.util.Util;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 银行
 */

public class PosBanker2 extends FBCommu {
    /**
     * @param context 上下文
     * @param taskId  任务id
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     */
    public PosBanker2(Context context, String taskId, OnFBCommuFinish dealer) {
        super(context, taskId, dealer);
        connecter = new AIDLConnecter(context, "应收");
    }

    @Override
    public void launch() {
        if (mPostDatas.length == 0) return;
        final JsonObject params = (JsonObject) mPostDatas[0];
        final JsonObject post = Util.fromObject(params);
        String methodName = Util.joAsString(params, "methodName");
        methodName = Util.isEmpty(methodName) ? "ICBCSmart" : methodName;
        post.addProperty("methodName", methodName);
        post.addProperty("step", "请求转化");
        HttpTask.launch2(mContext, post, "请求转化", new OnFBCommuFinish() {
            @Override
            public void onFBCommuFinish(JsonObject response, String taskDescribe) {
                try {
                    final String transType = Util.joAsString(response, "transType");
                    final Bundle ctrlData = response.has("ctrlData") ? new Bundle() : null;
                    final Bundle transData = response.has("transData") ? new Bundle() : null;
                    if (response.has("ctrlData")) {
                        for (JsonElement e : response.getAsJsonArray("ctrlData"))
                            loadReq(ctrlData, e.getAsJsonObject());
                    }
                    if (response.has("transData")) {
                        for (JsonElement e : response.getAsJsonArray("transData"))
                            loadReq(transData, e.getAsJsonObject());
                    }
                    connecter.bindService(new AIDLConnecter.OnServiceConnectedListener() {
                        @Override
                        public void onServiceConnected(ComponentName name, IBinder service) {
                            try {
                                ITransService.Stub.asInterface(service).startTrans(transType, ctrlData, transData, new TransHandler.Stub() {
                                    @Override
                                    public void onFinish(final Bundle baseResult, final Bundle transResult, final Bundle extraInfo) throws RemoteException {
                                        post.addProperty("step", "回应转化");
                                        JsonObject sendThird = new JsonObject();
                                        sendThird.addProperty("transType", transType);
                                        sendThird.add("ctrlData", Util.fromObject(ctrlData));
                                        sendThird.add("transData", Util.fromObject(transData));
                                        post.add("params", params);
                                        post.add("sendThird", sendThird);
                                        JsonObject thirdRes = new JsonObject();
                                        thirdRes.add("baseResult", Util.fromObject(baseResult));
                                        thirdRes.add("transResult", Util.fromObject(transResult));
                                        thirdRes.add("extraInfo", Util.fromObject(extraInfo));
                                        post.add("thirdRes", thirdRes);
                                        ((Activity) mContext).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                HttpTask.launch2(mContext, post, "回应转化", new OnFBCommuFinish() {
                                                    @Override
                                                    public void onFBCommuFinish(JsonObject resp2, String t2) {
                                                        callbackDealer(resp2);
                                                    }
                                                });
                                            }
                                        });


                                    }
                                });
                            } catch (Exception ex) {
                                Util.showMsg(mContext, "调用应收服务异常：" + ex);
                            }
                        }
                    });
                } catch (Exception ex) {
                    Util.showMsg(mContext, "数据装载异常:" + ex);
                }
            }
        });
    }

    /**
     * 装载请求数据
     *
     * @param parent 数据接收者
     * @param param  转化参数及数据提供者
     * @throws Exception
     */
    private void loadReq(Bundle parent, JsonObject param) throws Exception {
        if (parent == null || param == null) return;
        String type = Util.joAsString(param, "type");
        String name = Util.joAsString(param, "name");
        String cl = Util.joAsString(param, "cls");
        JsonElement value = param.get("value");
        switch (type) {
            case "int":
                parent.putInt(name, value.getAsInt());
                break;
            case "float":
                parent.putFloat(name, value.getAsFloat());
                break;
            case "long":
                parent.putLong(name, value.getAsLong());
                break;
            case "double":
                parent.putDouble(name, value.getAsDouble());
                break;
            case "bool":
                parent.putBoolean(name, value.getAsBoolean());
                break;
            case "StringArrayList": {
                ArrayList<String> list=new ArrayList<>();
                for (JsonElement ele:value.getAsJsonArray()) {
                    list.add(ele.getAsString());
                }
                parent.putStringArrayList(name,list);
            }
            break;
            case "serial": {
                //这儿putSerializable中传入的类型要和第三方的类要一模一样，包括空间名，不能用父类
                Object obj = Util.GSON.fromJson(value, Class.forName(cl));
                parent.putSerializable(name, (Serializable) obj);
            }
            break;
            case "bundle": {
                Bundle bundle = new Bundle();
                for (JsonElement e : value.getAsJsonArray()) {
                    loadReq(bundle, e.getAsJsonObject());
                }
                parent.putBundle(name, bundle);
            }
            break;
            default:
                parent.putString(name, value.getAsString());
                break;
        }
    }

    @Override
    public void stop() {
        connecter.unbindService();
    }

    private AIDLConnecter connecter;
}
