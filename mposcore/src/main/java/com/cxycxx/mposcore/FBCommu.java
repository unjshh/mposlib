package com.cxycxx.mposcore;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import com.cxycxx.mposcore.util.Util;
import com.google.gson.JsonObject;

/**
 * 前后台交流接口 front_backgroud_communication
 */

public abstract class FBCommu {
    /**
     * （这是关于处理回应模式的常量）表示访问失败时自动显示错误信息，这是默认的处理方式。注意：这种模式下只有回应成功时才会回调.
     */
    public static final int AUTO_SHOW_ERROR = 1;
    /**
     * （这是关于处理回应模式的常量）表示访问失败时自动隐藏错误信息。注意：这种模式下只有回应成功时才会回调.
     */
    public static final int AUTO_HIDE_ERROR = 2;
    /**
     * （这是关于处理回应模式的常量）表示总是回调.
     */
    public static final int ALWAYS_CALLBACK = 3;

    public boolean isWaitDialogCancelable() {
        return isWaitDialogCancelable;
    }

    public void setWaitDialogCancelable(boolean waitDialogCancelable) {
        isWaitDialogCancelable = waitDialogCancelable;
    }

    private boolean isWaitDialogCancelable = true;

    /**
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     * @param taskId  任务id
     * @param context 上下文
     */
    public FBCommu(Context context, String taskId, OnFBCommuFinish dealer) {
        mDealer = dealer;
        mContext = context;
        mTaskId = taskId;
    }


    /**
     * @param postDatas 发送的数据
     */
    public final void launch(Object... postDatas) {
        if (postDatas != null) mPostDatas = postDatas;
        launch();
    }

    /**
     * 设置超时时间
     *
     * @param timeOut (以毫秒为单位，默认值是8000)
     */
    public void setTimeOut(int timeOut) {
        mTimeOut = timeOut;
    }

    /**
     * 设置处理者(只能放在launch之前)
     *
     * @param dealer
     */
    public void setDealer(OnFBCommuFinish dealer) {
        mDealer = dealer;
    }

    /**
     * 设置处理错误的模式
     *
     * @param mode 只能是AUTO_SHOW_ERROR(默认), AUTO_HIDE_ERROR, ALWAYS_CALLBACK之一.
     *             AUTO_SHOW_ERROR这个模式下必须确保context传入.
     */
    public void setDealErrorModel(int mode) {
        if (mode < AUTO_SHOW_ERROR || mode > ALWAYS_CALLBACK) return;
        mDealErrorModel = mode;
    }

    /**
     * 设置等待对话框的提示信息
     * @param hint  提示信息
     */
    public void setWaitHint(String hint) {
       if(!Util.isEmpty(hint))mWaitHint=hint;
    }

    /**
     * 显示等待对话框
     *
     * @param hint 提示,空时显示默认值“请稍等...”
     */
    public void showWaitDialog(String hint) {
        if (mContext == null) return;
        if (mWaitDialog == null) {
            mWaitDialog = ProgressDialog.show(mContext, "", Util.isEmpty(hint) ? mWaitHint : hint.trim(), false, isWaitDialogCancelable);
            return;
        }
        if (mWaitDialog.isShowing()) return;
        mWaitDialog.setMessage(Util.isEmpty(hint) ? mWaitHint : hint.trim());
        mWaitDialog.show();
    }

    /**
     * 关闭等待对话框
     */
    public void dismissWaitDialog() {
        if (mWaitDialog == null || !mWaitDialog.isShowing()) return;
        mWaitDialog.dismiss();
    }

    /**
     * 开启
     */
    public abstract void launch();

    /**
     * 停止
     */
    public abstract void stop();

    /**
     * 回调处理者(只能在主线程中),会调用stop()
     *
     * @param response
     */
    protected final void callbackDealer(JsonObject response) {
        stop();
        if(response==null){
            errCallbackDealer("FBCommu 回调参数respose为null");
            return;
        }
        if (mDealer == null) return;
        if (!"成功".equals(Util.joAsString(response,"result"))) {
            errCallbackDealer(response.has("message")?Util.joAsString(response,"message"):response.toString());
            return;
        }
        mDealer.onFBCommuFinish(response, mTaskId);
    }

    /**
     * 失败时回调(只能在主线程中),会调用stop()
     *
     * @param msg 失败信息
     */
    protected final void errCallbackDealer(String msg) {
        stop();
        if (mDealErrorModel == AUTO_SHOW_ERROR) {
            if (mContext != null) Util.showMsg(mContext, msg);
            return;
        }
        if (mDealer == null || mDealErrorModel == AUTO_HIDE_ERROR) return;
        JsonObject err = new JsonObject();
        err.addProperty("result", "失败");
        err.addProperty("message", msg);
        mDealer.onFBCommuFinish(err, mTaskId);
    }

    /**
     * 回调处理者,会调用stop()
     *
     * @param response
     */
    protected final void callbackDealerOnUiTread(JsonObject response) {
        //stop();
        if (!(mContext instanceof Activity)) {
            Util.showMsg(mContext, "只有Activity才能用");
            return;
        }
        ((Activity) mContext).runOnUiThread(() -> callbackDealer(response));
    }

    /**
     * 失败时回调,会调用stop()
     *
     * @param msg 失败信息
     */
    protected final void errCallbackDealerOnUiTread(String msg) {
        stop();
        if (!(mContext instanceof Activity)) {
            Util.showMsg(mContext, "只有Activity才能用");
            return;
        }
        ((Activity) mContext).runOnUiThread(() -> errCallbackDealer(msg));
    }

    protected OnFBCommuFinish mDealer;// 接收处理者
    protected String mTaskId;// 任务id
    protected Context mContext;// 上下文
    protected Object[] mPostDatas = new Object[0];// 传递的参数
    protected String mWaitHint = "请稍等...";//等待信息
    protected int mTimeOut = 8000;//超时时间
    protected int mDealErrorModel = AUTO_SHOW_ERROR;//处理失败方式
    private ProgressDialog mWaitDialog; //等待框
}
