package com.cxycxx.icbcsmartpos;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.http.HttpTask;
import com.cxycxx.mposcore.util.Util;
import com.cxycxx.mposcore.util.UtilC;
import com.google.gson.JsonObject;
import com.icbc.smartpos.transservice.aidl.ITransService;
import com.icbc.smartpos.transservice.aidl.TransHandler;


/**
 * 银行
 */

public class PosBanker extends FBCommu {
    /**
     * @param context 上下文
     * @param taskId  任务id
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     */
    public PosBanker(Context context, String taskId, OnFBCommuFinish dealer) {
        super(context, taskId, dealer);
        connecter= new AIDLConnecter(context,"应收");
    }

    @Override
    public void launch() {
        if(mPostDatas.length==0)return;
        connecter.bindService(new AIDLConnecter.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                final JsonObject params=(JsonObject)mPostDatas[0];
                String transType= Util.joAsString(params,"transType");
                switch (transType){
                    case "签到":
                        Util.showMsg(mContext,"没有签到接口");
                        return;
                    case "消费":
                        transType="MULTI_PURCHASE";
                        break;
                    case "撤销消费":
                        transType="POS_VOID";
                        break;
                    case "结算":
                        Util.showMsg(mContext,"没有结算接口");
                        return;
                    case "查询余额":
                        transType="INQUERY";
                        break;
                    case "退货":
                        transType="REFUND";
                        break;
                    case "查流水":
                        transType="QUERY_TRANS_REC";
                        break;
                    case "补打小票":
                        transType="REPRINT";
                        break;
                    default:break;
                }
                final Bundle ctrlData = new Bundle();//控制参数
                ctrlData.putString("APP_NAME", UtilC.getAppName(mContext));
                ctrlData.putString("AIDL_VER","V1.0.2");
                final Bundle transData = new Bundle();//交易数据
                if(params.has("amount"))transData.putLong("AMOUNT",Util.joAsInt(params,"amount"));//交易金额（人民币），单位：分
                if(params.has("outTradeNo")) {
                    String outTradeNo=Util.joAsString(params, "outTradeNo");
                    if(!Util.isEmpty(outTradeNo))transData.putString("OLD_REF_NO", outTradeNo);//原交易的检索参考号
                }
                if("REPRINT".equals(transType))params.remove("AMOUNT");
//                if(params.has("outTradeNo")) {
//                   long outTradeNo =Util.getDecimal(Util.joAsString(params, "outTradeNo")).longValue();
//                    transData.putLong("OLD_TRANS_SEQUENCE", outTradeNo);//交易顺序号
//                }
                try {
                    final String tt=transType;
                    final JsonObject post=new JsonObject();
                    post.addProperty("methodName","ClientLog");
                    JsonObject log=new JsonObject();
                    log.addProperty("type","工行请求数据");
                    log.add("ctrlData",Util.fromObject(ctrlData));
                    log.add("transData",Util.fromObject(transData));
                    log.add("params",params);
                    post.add("clientLog",log);
                    HttpTask.launch2(mContext,post,"上传日志",null) ;
                    ITransService.Stub.asInterface(service).startTrans(tt, ctrlData, transData, new TransHandler.Stub() {
                        @Override
                        public void onFinish(final Bundle baseResult, final Bundle transResult, final Bundle extraInfo) throws RemoteException {
                            if(baseResult==null){
                                Util.showMsg(mContext,"未返回基本结果");
                                return;
                            }
                            final JsonObject rst=new JsonObject();
                            rst.addProperty("result",baseResult.getLong("RESULT",1)==0?"成功":"失败");
                            rst.addProperty("message",baseResult.getString("DESCRIPTION",""));//交易描述 交易失败时将会输出对应的失败信息描述
                            rst.addProperty("tradeTime",baseResult.getString("TRANS_TIME",""));//交易时间 格式：yyyyMMddHHmmss
                            rst.addProperty("merchantName",baseResult.getString("CUST_NAME",""));//终端对应的商户名称
                            rst.addProperty("merchantId",baseResult.getString("CUST_NO",""));//终端对应的 12 位商户号
                            rst.addProperty("terminalId",baseResult.getString("TERM_NO",""));//终端对应的 15 位终端号
                            rst.addProperty("outTradeNo",baseResult.getString("TRANS_SEQUENCE",""));//交易顺序号
                            if(params.has("payment"))rst.addProperty("payment",Util.joAsString(params,"payment"));//
                            if(params.has("paymentId"))rst.addProperty("paymentId",Util.joAsString(params,"paymentId"));//
                            if(transResult!=null) {//交易结果数据
                                rst.addProperty("outPayType", transResult.getString("PAY_TYPE", ""));//用户在可选页面上最终选择的付款方式
                                rst.addProperty("amount", transResult.getLong("AMOUNT", 0));//交易金额（人民币），单位：分
                                rst.addProperty("discount", transResult.getLong("AMOUNT", 0)-baseResult.getLong("RET_AMT", 0));//优惠
                                rst.addProperty("referceNo", transResult.getString("REF_NO", ""));//交易后台返回的检索参考号
                            }
                            if(extraInfo!=null) {//交易附加数据
                                rst.addProperty("cardNo", extraInfo.getString("PAN", ""));//完整的银行卡号
                                rst.addProperty("issue", extraInfo.getString("CARD_ISSUER", ""));//银行卡的对应发卡行名称
                            }
                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callbackDealer(rst);
                                    JsonObject log=new JsonObject();
                                    log.addProperty("type","工行返回数据");
                                    log.add("baseResult",Util.fromObject(baseResult));
                                    log.add("transResult",Util.fromObject(transResult));
                                    log.add("extraInfo",Util.fromObject(extraInfo));
                                    log.add("mposObtain",rst);
                                    post.add("clientLog",log);
                                    HttpTask.launch2(mContext,post,"上传日志",null) ;
                                }
                            });

                        }
                    });
                }catch(Exception ex){
                    Util.showMsg(mContext,"调用应收服务异常："+ex);
                }
            }
        });
    }

    @Override
    public void stop() {
        connecter.unbindService();
    }
    private AIDLConnecter connecter;
}
