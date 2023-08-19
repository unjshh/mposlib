package com.cxycxx.p990;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.google.gson.JsonObject;
import com.landicorp.android.eptapi.DeviceService;
import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.log.LogModel;
import com.cxycxx.mposcore.log.PosLogger;
import com.cxycxx.mposcore.util.Util;
/**
 * P990请求银行
 */
public class P990Banker extends FBCommu {
	/**
	 * @param dealer 为null因为最后结果从Activity的onActivityResult(int, int, Intent)返回了
	 * @param taskId 对应Activity的requstCode
	 * @param context 必须是Activity
	 */
	public P990Banker(Context context, String taskId, OnFBCommuFinish dealer){
		super(context, taskId, dealer);
	}
	@Override
	public void launch(){
		String transType = "";
		JsonObject params = new JsonObject();
		if (mPostDatas[0] instanceof String)
			transType = mPostDatas[0].toString();//第一个参数是String则表示接口名称
		else {//
			params = (JsonObject) mPostDatas[0];
			transType = Util.joAsString(params, "transType");
		}
		Intent in=new Intent();
		switch (transType){
			case "签到":
				in.putExtra("transName","签到");
				break;
			case "消费":
				in.putExtra("transName","消费");
				break;
			case "撤销消费":
				in.putExtra("transName","撤销消费");
				break;
			case "结算":
				in.putExtra("transName","结算");
				break;
			case "查询余额":
				in.putExtra("transName","查询余额");
				break;
			case "退货":
				in.putExtra("transName","退货");
				break;
			case "补打小票":
				in.putExtra("transName","打印");
				break;
			default:break;
		}
		if(params.has("amount"))in.putExtra("amount",String.format("%1$012d", Util.joAsInt(params,"amount")));
		if(params.has("oldTrace"))in.putExtra("traceNo",Util.joAsString(params, "tradeNo"));
		DeviceService.logout();//调银行之前这句不能少
		in.setComponent(new ComponentName("com.icbc.android.landi", "com.icbc.android.landi.ICBCScriptActivity"));
		((Activity)mContext).startActivityForResult(in,Integer.parseInt(mTaskId));

		LogModel log= PosLogger.getLogModel("银行");
		log.setInterface(transType);
		log.setPostData(in.getExtras().toString());
		PosLogger.writeLog(log);
	}
	@Override
	public void stop(){
	}
	/**
	 *转换传出的参数
	 * @param data
	 * @return
	 */
	public static void obtainValues(Context context,Intent data,JsonObject param,OnFBCommuFinish dealer){
		DeviceService.logout();
		LogModel log= PosLogger.getLogModel("银行");
		JsonObject map=new JsonObject();
		if(data==null){
			map.addProperty("result", "失败");
			map.addProperty("message", "返回数据为空");
			dealer.onFBCommuFinish(map,"");
			return;
		}
		map.addProperty("result",Util.joAsInt(param,"resultCode") == Activity.RESULT_OK?"成功":"失败");
		map.addProperty("message", data.getStringExtra("reason"));
		if(data.hasExtra("referenceNo"))map.addProperty("referceNo", data.getStringExtra("referenceNo"));
		if(data.hasExtra("traceNo"))map.addProperty("traceNo", data.getStringExtra("traceNo"));
		if(data.hasExtra("cardNo"))map.addProperty("cardNo", data.getStringExtra("cardNo"));
		if(data.hasExtra("date"))map.addProperty("tradeTime", data.getStringExtra("date"));
		if(data.hasExtra("time"))map.addProperty("tradeTime",Util.joAsString(map,"tradeTime")+" "+data.getStringExtra("time"));
		if(data.hasExtra("issue"))map.addProperty("issue", data.getStringExtra("issue"));
		if(data.hasExtra("terminalId"))map.addProperty("terminalId", data.getStringExtra("terminalId"));
		if(data.hasExtra("merchantId"))map.addProperty("merchantId", data.getStringExtra("merchantId"));
		if(data.hasExtra("merchantName"))map.addProperty("merchantName", data.getStringExtra("merchantName"));
		if(data.hasExtra("batchNo"))map.addProperty("batchNo", data.getStringExtra("batchNo"));
		if(data.hasExtra("amount"))map.addProperty("amount",Util.getDecimal(data.getStringExtra("amount")).intValue());
		log.setResponse(map.toString());
		PosLogger.writeLog(log);
		dealer.onFBCommuFinish(map,"");
	}
}
