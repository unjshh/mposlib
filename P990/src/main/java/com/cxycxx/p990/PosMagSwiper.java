package com.cxycxx.p990;

import android.content.Context;

import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.util.Util;
import com.google.gson.JsonObject;
import com.landicorp.android.eptapi.DeviceService;
import com.landicorp.android.eptapi.device.MagCardReader;

/**
 * P990 刷磁条器
 */
public class PosMagSwiper extends FBCommu {
	public PosMagSwiper(Context context, String taskId, OnFBCommuFinish dealer){
		super(context, taskId, dealer);
	}
	@Override
	public void launch(){
		try{
			DeviceService.logout();
			DeviceService.login(mContext);
			MagCardReader.getInstance().setLRCCheckEnabled(true);
			//MagCardReader.getInstance().enableTrack(MagCardReader.TRK1 | MagCardReader.TRK2 | MagCardReader.TRK3);
			MagCardReader.getInstance().enableTrack(MagCardReader.TRK2 | MagCardReader.TRK3);
			if(!mIsSearching){
				mIsSearching=true;
				MagCardReader.getInstance().searchCard(listener);
			}
		}
		catch(Exception e){
			Util.showMsg(mContext, "P990无法开启磁条扫描器");
		}
	}
	@Override
	public void stop(){
		try{
			MagCardReader.getInstance().searchCard((MagCardReader.OnSearchListener) null);
			mIsSearching=false;
			MagCardReader.getInstance().stopSearch();
			DeviceService.logout();
		}
		catch(Exception e){}
	}
	private MagCardReader.OnSearchListener listener = new MagCardReader.OnSearchListener(){
		@Override
		public void onCardStriped(boolean[] hasTrack, String[] track){
			if(!mIsSearching)return;
			String track1="",track2="",track3="";
			//if(hasTrack[0] && track[0] != null)track1=track[0].split("=")[0];
			if(hasTrack[1] && track[1]!=  null)track2=track[1].split("=")[0];
			if(hasTrack[2] && track[2] != null)track3=track[2].split("=")[0];
			boolean isAllEmpty=Util.isEmpty(track1) && Util.isEmpty(track2) && Util.isEmpty(track3);
			if(!isAllEmpty){
				JsonObject data=new JsonObject();
				data.addProperty("result", "成功");
				data.addProperty("msg", track2);
				data.addProperty("track1", track1);
				data.addProperty("track2", track2);
				data.addProperty("track3", track3);
				callbackDealer(data);
				//MediaPlayer.create(mContext, R.raw.beep).start();
				stop();//停止扫描
			}
		}
		@Override
		public boolean checkValid(int[] trackStates, String[] track){
			for(int i = 0;i < trackStates.length;++i){
				if(trackStates[i] != TRACK_STATE_NULL){
					break;
				}else if(i == 2){ return false; }
			}
			return super.checkValid(trackStates, track);
		}
		@Override
		public void onFail(int code){
			Util.showMsg(mContext, "刷卡失败，请重试");
		}
		@Override
		public void onCrash(){}
	};
	private boolean mIsSearching=false;//是否正在寻卡
}
