package com.cxycxx.p990;

import android.content.Context;

import com.google.gson.JsonObject;
import com.landicorp.android.eptapi.DeviceService;
import com.landicorp.android.eptapi.device.InnerScanner;
import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.util.Util;

import java.util.Calendar;

/**
 *P990扫描器
 */
public class PosScanner extends FBCommu {
	public PosScanner(Context context, String taskId, OnFBCommuFinish dealer){
		super(context, taskId, dealer);
		//mStopAfterFinish=false;
	}
	@Override
	public void launch(){
		try{
			DeviceService.logout();
			DeviceService.login(mContext);
			InnerScanner.getInstance().setOnScanListener(mOnScanListener);
			InnerScanner.getInstance().start(20);
		}
		catch(Exception e){
			Util.showMsg(mContext, "无法开启扫描器");
		}
		
	}
	@Override
	public void stop(){
		isFinished=true;
		try {
			InnerScanner.getInstance().stop();
			InnerScanner.getInstance().stopListen();
			DeviceService.logout();
		}catch (Exception ex){

		}

	}
	/**
	 * 提交结果
	 * @param result 结果
	 */
	private synchronized void commit(JsonObject result){
		if(result==null||isFinished)return;
		String cn=result.has("content")?result.get("content").getAsString():"";
		long now= Calendar.getInstance().getTimeInMillis();
		if(lastContent.equals(cn)&&lastTime>0&&now-lastTime<3000)return;
		isFinished=true;
		lastContent=cn;
		lastTime= now;
		callbackDealerOnUiTread(result);
	}
	private InnerScanner.OnScanListener mOnScanListener=new InnerScanner.OnScanListener(){
		@Override
		public void onScanSuccess(String arg0){
			JsonObject data=new JsonObject();
			data.addProperty("result", "成功");
			data.addProperty("content", arg0);
			data.addProperty("barcodeType", "条形码");//类型是条形码，对应CRM

			commit(data);
			//MediaPlayer.create(mContext, R.raw.beep).start();
		}
		@Override
		public void onScanFail(int arg0){
			errCallbackDealer(arg0==ERROR_TIMEOUT?"扫描超时,请重试":"扫描失败，请重试");
		}
		@Override
		public void onCrash(){
			launch();
		}
	};
	private boolean isFinished = false;//是否完成
	private static String lastContent="";//上次内容
	private static long lastTime=0;//上次时候
}
