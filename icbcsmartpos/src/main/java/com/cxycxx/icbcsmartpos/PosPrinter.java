package com.cxycxx.icbcsmartpos;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.mpos.MposPub;
import com.cxycxx.mposcore.util.Util;
import com.icbc.smartpos.deviceservice.aidl.IDeviceService;
import com.icbc.smartpos.deviceservice.aidl.IPrinter;
import com.icbc.smartpos.deviceservice.aidl.PrinterListener;

import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 打印机
 */

public class PosPrinter extends FBCommu {
    /**
     * @param context 上下文
     * @param taskId  任务id
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     */
    public PosPrinter(Context context, String taskId, OnFBCommuFinish dealer) {
        super(context, taskId, dealer);
        connecter= new AIDLConnecter(context,"设备");
    }

    @Override
    public void launch() {
        if (mPostDatas.length==0||mContext == null) return;
        List<String> printContent= Collections.emptyList();
        Bitmap bitmap=null;
        try {
            if(mPostDatas[0] instanceof Bitmap) bitmap=(Bitmap)mPostDatas[0];
            else printContent= (List<String>) mPostDatas[0];
        }catch (Exception e){
            errCallbackDealer("PosPrinter 中传递的参数只能是List<String>");
            return;
        }
        if (bitmap==null&&(printContent == null||printContent.isEmpty())) {
            Util.showMsg(mContext,"打印内容为空，请检查服务打印转换");
            return;
        }
        final List<String> pc=printContent;
        connecter.bindService(new AIDLConnecter.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(ComponentName name, final IBinder service) {
                try {
                    IPrinter printer = IDeviceService.Stub.asInterface(service).getPrinter();
                    String style=StringUtils.substring(pc.get(0),0,pc.get(0).indexOf("$",1)+1);
                    int width=Util.getDecimal(parseStyle("s_width",style)).intValue();
                    Bundle format = new Bundle();
                    format.putBoolean("newline", true);//newline(bool)：是否换行，默认换行，true:换行，false:不换行
                    for (String line:pc) {
                        if (!line.startsWith("$")||line.length()<2||line.indexOf("$",1)<0) {
                            format.putInt("font", 1);//正常大小
                            format.putInt("align", 0);//居左
                            printer.addText(format, line);
                            continue;
                        }
                        style=StringUtils.substring(line,0,line.indexOf("$",1)+1);
                        String value=line.replace(style,"");
                        String type=parseStyle("type",style);
                        if("feed".equals(type)) {
                            printer.feedLine(Util.getDecimal(value).intValue());
                            continue;
                        }
                        //---------
                        String font =parseStyle("font",style);//字体大小
                        if("1".equals(font))format.putInt("font", 0);//小
                        else if("3".equals(font))format.putInt("font", 2);//大
                        else format.putInt("font", 1);//正常
                        //------
                        String align =parseStyle("align",style);//对齐方式
                        if("right".equals(align))format.putInt("align", 2);//居右
                        else if("center".equals(align))format.putInt("align", 1);//居中
                        else if("col".equals(align)&&width>0) {
                            format.putInt("align", 0);//居左
                            value = MposPub.alignPrint(value, width);
                        }
                        else format.putInt("align", 0);//居左
                        if("text".equals(type)||Util.isEmpty(type)){
                            printer.addText(format, value);
                            continue;
                        }
                        Bundle ft = new Bundle();
                        String height=parseStyle("height",style);
                        if("qrcode".equals(type)){//二维码
                            String offset=parseStyle("offset",style);
                            ft.putInt("offset",Util.getDecimal(offset).intValue());
                            ft.putInt("expectedHeight",Util.getDecimal(height, BigDecimal.valueOf(200)).intValue());
                            printer.addQrCode(ft,value);
                        }else if("barcode".equals(type)){//条形码
                            String wd=parseStyle("width",style);
                            ft.putInt("align", format.getInt("align"));
                            ft.putInt("width",Util.getDecimal(wd, BigDecimal.valueOf(200)).intValue());
                            ft.putInt("height",Util.getDecimal(height, BigDecimal.valueOf(100)).intValue());
                            printer.addBarCode(ft,value);
                        }
                    }
                    printer.startPrint(new PrinterListener() {
                        @Override
                        public void onFinish() throws RemoteException {

                        }

                        @Override
                        public void onError(int error) throws RemoteException {
                            Util.showMsg(mContext, "打印失败 " + error);
                        }

                        @Override
                        public IBinder asBinder() {
                            return service;//注意这儿要返回实例【自动生成的是null】
                        }
                    });
                }catch (Exception ex){
                    Util.showMsg(mContext,"打印异常:"+ex);
                }
            }
        });
    }

    @Override
    public void stop() {
        connecter.unbindService();
    }

    private String parseStyle(String type,String styles){
        if(Util.isEmpty(type))return "";
        for (String kv: StringUtils.split(styles.replace("$", ""),";")) {
            if(Util.isEmpty(kv))continue;
            String[] ss=StringUtils.split(kv,":");
            if(ss.length<2)continue;
            if(ss[0].trim().equals(type))return ss[1];
        }
        return "";
    }

    private AIDLConnecter connecter;
}
