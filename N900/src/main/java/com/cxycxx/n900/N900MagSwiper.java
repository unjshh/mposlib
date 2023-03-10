package com.cxycxx.n900;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.util.Util;
import com.google.gson.JsonObject;
import com.newland.me.ConnUtils;
import com.newland.me.DeviceManager;
import com.newland.mtype.Device;
import com.newland.mtype.ModuleType;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.cardreader.CardReader;
import com.newland.mtype.module.common.cardreader.OpenCardReaderEvent;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.module.common.swiper.SwipResultType;
import com.newland.mtype.module.common.swiper.Swiper;
import com.newland.mtype.module.common.swiper.SwiperReadModel;
import com.newland.mtype.util.Dump;
import com.newland.mtypex.nseries.NSConnV100ConnParams;

import java.util.concurrent.TimeUnit;


/**
 * N900磁条扫描器
 */
public class N900MagSwiper extends FBCommu {
    /**
     * @param context 上下文
     * @param taskId  任务id
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     */
    public N900MagSwiper(Context context, String taskId, OnFBCommuFinish dealer) {
        super(context, taskId, dealer);
        deviceManager = ConnUtils.getDeviceManager();
        deviceManager.init(mContext, "com.newland.me.K21Driver", new NSConnV100ConnParams(), null);
    }

    @Override
    public void launch() {
        new Thread(() -> {
            try {
                deviceManager.connect();
                Device device = deviceManager.getDevice();
                if (device == null) return;
                CardReader cardReader = (CardReader) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_CARDREADER);
                cardReader.openCardReader("请刷卡或者插入IC卡", new ModuleType[]{ModuleType.COMMON_SWIPER, ModuleType.COMMON_ICCARDREADER, ModuleType.COMMON_RFCARDREADER}, null, true, true, 60, TimeUnit.SECONDS, new DeviceEventListener<OpenCardReaderEvent>() {
                    @Override
                    public void onEvent(OpenCardReaderEvent openCardReaderEvent, Handler h) {
                        if (openCardReaderEvent.isSuccess()) {
                            String showMsg = "";
                            switch (openCardReaderEvent.getOpenCardReaderResult().getResponseCardTypes()[0]) {
                                case MSCARD:
                                    showMsg = "读卡器识别到【刷卡】操作";
                                    read("明文");
                                    break;
                                case ICCARD:
                                    showMsg = "读卡器识别到【插卡】操作";
                                    break;
                                case RFCARD:
                                    showMsg = "读卡器识别到【非接】操作";
                                    break;
                                default:
                                    break;
                            }
                        } else if (openCardReaderEvent.isFailed()) {
                            Util.sendSimple(handler, 1, "读卡器开启失败");
                        }
                    }

                    @Override
                    public Handler getUIHandler() {
                        return handler;
                    }
                });
            } catch (Exception ex) {
                Util.sendSimple(handler, 1, "读卡器开启失败" + ex);
            }
        }).start();
    }

    @Override
    public void stop() {
        if (deviceManager == null) return;
        new Thread(() -> {
            deviceManager.destroy();
            deviceManager = null;
        }).start();
    }

    private void read(String type) {
        try {
            Swiper swiper = (Swiper) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_SWIPER);
            SwipResult swipRslt = swiper.readPlainResult(new SwiperReadModel[]{SwiperReadModel.READ_FIRST_TRACK,SwiperReadModel.READ_SECOND_TRACK, SwiperReadModel.READ_THIRD_TRACK});

            if (null != swipRslt && swipRslt.getRsltType() == SwipResultType.SUCCESS) {
                String track1 = swipRslt.getFirstTrackData()==null?"": Dump.getHexDump(swipRslt.getFirstTrackData());
                byte[] track2B=swipRslt.getSecondTrackData();
                String track2 = track2B==null?"":new String(track2B,0,track2B.length);
                String track3 =swipRslt.getThirdTrackData()==null?"": Dump.getHexDump(swipRslt.getThirdTrackData());
                Message msg = new Message();
                msg.what = 0;
                Bundle bundle = new Bundle();
                bundle.putString("track1", track1);
                bundle.putString("track2", track2);
                bundle.putString("track3", track3);
                msg.setData(bundle);
               handler.sendMessage(msg);
            }
        }catch (Exception ex){
            Util.sendSimple(handler, 1, "读取磁道数据失败" + ex);
        }
    }
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) errCallbackDealer(msg.obj.toString());
            else if (msg.what == 0) {
                //MediaPlayer.create(mContext, R.raw.beep).start();
                Bundle bundle = msg.getData();
                JsonObject data = new JsonObject();
                data.addProperty("result", "成功");
                data.addProperty("track1", msg.getData().getString("track1"));
                data.addProperty("track2", msg.getData().getString("track2"));
                data.addProperty("track3", msg.getData().getString("track3"));
                callbackDealer(data);
            }
        }
    };
    private DeviceManager deviceManager;
}
