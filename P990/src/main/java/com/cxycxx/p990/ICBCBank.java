package com.cxycxx.p990;

import android.content.Context;
import android.content.Intent;

import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.log.LogModel;
import com.cxycxx.mposcore.log.PosLogger;
import com.cxycxx.mposcore.util.Util;
import com.google.gson.JsonObject;
import com.landicorp.android.eptapi.DeviceService;


import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;

/**
 * 联迪工商银行
 */

public class ICBCBank extends FBCommu {
    /**
     * @param context 上下文
     * @param taskId  任务id
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     */
    public ICBCBank(Context context, String taskId, OnFBCommuFinish dealer) {
        super(context, taskId, dealer);
    }

    @Override
    public void launch() {
        String callStr="";
        JsonObject params=(JsonObject)mPostDatas[0];
        String transType = Util.joAsString(params,"transType");
//        if (mPostDatas[0] instanceof String)
//            transType = mPostDatas[0].toString();//第一个参数是String则表示接口名称
//        else {//
//            params = (JsonObject) mPostDatas[0];
//            transType = Util.joAsString(params, "transType");
//        }
        switch (transType) {
            case "签到":
                callStr+="4001";
                break;
            case "消费":
                callStr+="1001";
                break;
            case "撤销消费":
                callStr+="1101";
                break;
            case "结算":
                callStr+="4002";
                break;
            case "查询余额":
                callStr+="2002";
                break;
            case "退货":
                callStr+="1102";
                break;
            case "补打小票":
                callStr+="4005";
                break;
            case "查流水":
                callStr+="11033";
                break;
            default:
                break;
        }
        if(params.has("amount"))callStr+="|004"+String.format("%1$012d", Util.joAsInt(params,"amount"));
        if(params.has("oldReferceNo"))callStr+="|037"+Util.joAsString(params, "oldReferceNo");
        if(params.has("oldTradeTime"))callStr+="|800"+Util.joAsString(params, "oldTradeTime");
        DeviceService.logout();//调银行之前这句不能少
        /*
        Intent in = new Intent("ICBCScript");
        Bundle bundle = new Bundle();
        bundle.putString("CallStr", callStr);
        in.putExtras(bundle);
        ((Activity)mContext).startActivityForResult(in,Integer.parseInt(mTaskId));
        LogModel logModel= PosLogger.getLogModel("银行");
        logModel.setInterface(transType);
        logModel.setPostData(callStr);
        PosLogger.writeLog(logModel);
         */
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
            return;
        }
        String retStr = data.getExtras().getString("ReturnStr");
        for (String slipt:StringUtils.split(retStr,"|")) {
            String tag=StringUtils.substring(slipt,0,3);
            String val=StringUtils.substring(slipt,3);
            switch (tag){
                case "039":map.addProperty("result",val.equals("00")?"成功":"失败");break;
                case "004":map.addProperty("amount", new BigDecimal(val).intValue());break;
                case "012":map.addProperty("tradeTime",val);break;
                case "013":map.addProperty("tradeTime",val+" " +Util.joAsString(map,"tradeTime"));break;
                case "002":map.addProperty("cardNo",val);break;
                case "038":map.addProperty("authCode",val);break;
                case "902":map.addProperty("referceNo",val);break;
                case "041":map.addProperty("terminalId",val);break;
                case "042":map.addProperty("merchantId",val);break;
                case "903":map.addProperty("batchNo",val);break;
                default:break;
            }
        }
        logModel.setResponse(retStr +" 转化后:"+ map.toString());
        PosLogger.writeLog(logModel);
        dealer.onFBCommuFinish(map,"");
    }
}
