package com.cxycxx.ld_a8;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.util.Util;
import com.google.gson.JsonObject;
import com.landicorp.android.eptapi.DeviceService;
import com.landicorp.android.eptapi.card.MifareDriver;
import com.landicorp.android.eptapi.card.RFDriver;
import com.landicorp.android.eptapi.device.RFCardReader;
import com.landicorp.android.eptapi.exception.ReloginException;
import com.landicorp.android.eptapi.exception.RequestException;
import com.landicorp.android.eptapi.exception.ServiceOccupiedException;
import com.landicorp.android.eptapi.exception.UnsupportMultiProcess;


/**
 * P990射频卡读卡器
 */

public class PosRfcSwiper extends FBCommu {
    private Handler handler = new Handler();

    /**
     * @param context 上下文
     * @param taskId  任务id
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     */
    public PosRfcSwiper(Context context, String taskId, OnFBCommuFinish dealer) {
        super(context, taskId, dealer);
    }

    @Override
    public void launch() {
        try {
            stop();
            bindDeviceService();
            RFCardReader.getInstance().searchCard(onSearchListener);
        } catch (RequestException e) {
            e.printStackTrace();
            bindDeviceService();
        }
    }

    @Override
    public void stop() {
        try {
            RFCardReader.getInstance().stopSearch();
        } catch (RequestException e) {
            e.printStackTrace();
        }
        DeviceService.logout();
    }

    /**
     * To gain control of the device service,
     * you need invoke this method before any device operation.
     */
    private void bindDeviceService() {
        try {
            DeviceService.login((Activity) mContext);
        } catch (RequestException e) {
            // Rebind after a few milliseconds,
            // If you want this application keep the right of the device service
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bindDeviceService();
                }
            }, 300);
            e.printStackTrace();
        } catch (ServiceOccupiedException e) {
            e.printStackTrace();
        } catch (ReloginException e) {
            e.printStackTrace();
        } catch (UnsupportMultiProcess e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a listener to listen the result of search card.
     */
    private RFCardReader.OnSearchListener onSearchListener = new RFCardReader.OnSearchListener() {
        @Override
        public void onCrash() {
            bindDeviceService();
        }

        @Override
        public void onFail(int error) {
            errCallbackDealer(getErrorDescription(error));
        }

        @Override
        public void onCardPass(int cardType) {
            // Choose the right card driver .
            String driverName = "S50";
            switch (cardType) {
                case S50_CARD:
                    driverName = "S50";
                    break;
                case S70_CARD:
                    driverName = "S70";
                    break;
                case CPU_CARD:
                case PRO_CARD:
                case S50_PRO_CARD: // The card of this type can use 'S50' driver too.
                case S70_PRO_CARD: // The card of this type can use 'S70' driver too.
                    driverName = "PRO";
                    break;
                default:
                    errCallbackDealer("Search card fail, unknown card type!");
                    return;
            }
            driverName = "S50";//这儿写死
            System.out.println("rf card detected, and use " + driverName + " driver to read it!");
            try {
                RFCardReader.getInstance().activate(driverName, listener);
            } catch (RequestException e) {
                e.printStackTrace();
                bindDeviceService();
            }
        }

        public String getErrorDescription(int code) {
            switch (code) {
                case ERROR_CARDNOACT:
                    return "Pro card or TypeB card is not activated";
                case ERROR_CARDTIMEOUT:
                    return "No response";
                case ERROR_PROTERR:
                    return "The card return data does not meet the requirements of the protocal";
                case ERROR_TRANSERR:
                    return "Communication error";
            }
            return "unknown error [" + code + "]";
        }
    };

    private RFCardReader.OnActiveListener listener = new RFCardReader.OnActiveListener() {
        @Override
        public void onCardActivate(RFDriver cardDriver) {
            if (cardDriver == null) {
                Util.showMsg(mContext, "未能获取驱动");
                stop();
                return;
            }
            MifareOneCardReader card = new MifareOneCardReader((MifareDriver) cardDriver) {
                @Override
                protected void showErrorMessage(String msg) {
                    errCallbackDealer(msg);
                }

                @Override
                protected void onDeviceServiceException() {
                    errCallbackDealer("DeviceServiceException");
                }

                @Override
                protected void onDataRead(JsonObject info) {
                    info.addProperty("result", "成功");
                    callbackDealer(info);
                }
            };
            card.startRead();
            /*if(cardDriver instanceof RFCpuCardDriver) {
                // It is assumed to be UP card
                UPCardReader reader = new UPCardReader((RFCpuCardDriver) cardDriver) {
                    @Override
                    protected void showErrorMessage(String msg) {
                        errCallbackDealer(msg);
                    }
                    @Override
                    protected void onServiceCrash() {
                    }

                    @Override
                    protected void onDataRead(String pan, String track2, String track3,
                                              String expiredDate, byte[] serialNo, String readTime) {
                        JsonObject data=new JsonObject();
                        data.addProperty("result", "成功");
                        data.addProperty("pan", pan);
                        data.addProperty("track2", track2);
                        data.addProperty("track3", track3);
                        data.addProperty("expiredDate", expiredDate);
                        callbackDealer(data);
                    }
                };
                reader.startRead();
            }
            else if(cardDriver instanceof MifareDriver) {
                // Use MifareOneCardReader to do some operations.
                MifareOneCardReader card = new MifareOneCardReader((MifareDriver)cardDriver) {
                    @Override
                    protected void showErrorMessage(String msg) {
                        errCallbackDealer(msg);
                    }

                    @Override
                    protected void onDeviceServiceException() {
                    }

                    @Override
                    protected void onDataRead(String info) {
                        JsonObject data=new JsonObject();
                        data.addProperty("result", "成功");
                        data.addProperty("track1", info);
                        callbackDealer(data);
                    }
                };
                card.startRead();
            }*/
        }

        @Override
        public void onActivateError(int i) {
            stop();
        }

        @Override
        public void onUnsupport(String s) {
            stop();
        }

        @Override
        public void onCrash() {

        }
    };
}
