package com.cxycxx.n900;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.log.LogModel;
import com.cxycxx.mposcore.log.PosLogger;
import com.cxycxx.mposcore.util.Util;
import com.google.gson.JsonObject;

/**
 * N900 银行
 */
public class N900Banker extends FBCommu {
    /**
     * @param context 上下文
     * @param taskId  任务id
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     */
    public N900Banker(Context context, String taskId, OnFBCommuFinish dealer) {
        super(context, taskId, dealer);
    }

    @Override
    public void launch() {
        if(mPostDatas.length==0)return;
        JsonObject params=(JsonObject)mPostDatas[0];
        String transType= Util.joAsString(params,"transType");

        Intent intent = new Intent();
        //前三项每个接口都传
        if(params.has("operatorNo"))intent.putExtra("operNo",Util.joAsString(params, "operatorNo"));
        if(params.has("posClientNo"))intent.putExtra("posClientNo",Util.joAsString(params, "posClientNo"));
        if(params.has("misTradeNo"))intent.putExtra("misTradeNo", Util.joAsString(params, "misTradeNo"));
        if(params.has("amount"))intent.putExtra("amount",Util.joAsInt(params,"amount"));
        if(params.has("oldReferceNo"))intent.putExtra("oldRefnum",Util.joAsString(params, "oldReferceNo"));
        if(params.has("oldTradeTime"))intent.putExtra("oldDate",Util.joAsString(params, "oldTradeTime"));
        if(params.has("tradeNo"))intent.putExtra("traceNo",Util.joAsString(params, "tradeNo"));

        switch (transType){
            case "签到":
                intent.putExtra("transType",50);
                break;
            case "消费":
                intent.putExtra("transType",101);
                break;
            case "撤销消费":
                intent.putExtra("transType",106);
                break;
            case "结算":
                intent.putExtra("transType",54);
                break;
            case "查询余额":
                intent.putExtra("transType",100);
                break;
            case "退货":
                intent.putExtra("transType",105);
                break;
            case "补打小票":
                intent.putExtra("transType",200);
                if(Util.isEmpty(Util.joAsString(params, "tradeNo")))intent.putExtra("traceNo","000000");
                break;
            default:break;
        }
        intent.setClassName("com.newland.sxnx", "com.newland.payment.ui.activity.MainActivity");
        try {
            ((Activity)mContext).startActivityForResult(intent,Integer.parseInt(mTaskId));
        }catch (Exception e){
            Util.showMsg(mContext,e.toString());
        }
        LogModel logModel= PosLogger.getLogModel("银行");
        logModel.setInterface(transType);
        logModel.setPostData(intent.getExtras().toString());
        PosLogger.writeLog(logModel);
    }

    @Override
    public void stop() {

    }

    /**
     *转换传出的参数
     * @param data
     * @return
     */
    public static void obtainValues(Context context,Intent data,JsonObject param,OnFBCommuFinish dealer){
        LogModel logModel= PosLogger.getLogModel("银行");
        JsonObject map=new JsonObject();
        if(data==null){
            map.addProperty("result", "失败");
            map.addProperty("message", "返回数据为空");
            dealer.onFBCommuFinish(map,"");
            return ;
        }
        String result=data.getStringExtra("transResult");
        String responseCode=data.getStringExtra("responseCode");
        map.addProperty("result", "0".equals(result) && "00".equals(responseCode) ? "成功" : "失败");
        map.addProperty("message", responseCode);
        if(data.hasExtra("pan"))map.addProperty("cardNo", data.getStringExtra("pan"));
        if(data.hasExtra("referNum"))map.addProperty("referceNo", data.getStringExtra("referNum"));
        if(data.hasExtra("amount"))map.addProperty("amount",data.getLongExtra("amount",0));
        logModel.setResponse(map.toString());
        PosLogger.writeLog(logModel);
        dealer.onFBCommuFinish(map,"");
    }
}
