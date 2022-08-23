package com.cxycxx.ld_a8;

import android.content.Context;
import android.graphics.Bitmap;

import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.mpos.MposPub;
import com.cxycxx.mposcore.util.Util;
import com.landicorp.android.eptapi.DeviceService;
import com.landicorp.android.eptapi.device.Printer;
import com.landicorp.android.eptapi.device.Printer.Format;
import com.landicorp.android.eptapi.utils.QrCode;

import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * P990打印器
 */
public class PosPrinter extends FBCommu {
    public PosPrinter(Context context, String taskId, OnFBCommuFinish dealer) {
        super(context, taskId, dealer);
    }

    @Override
    public void launch() {
        if (mPostDatas.length == 0 || mContext == null) return;
        List<String> printContent = Collections.emptyList();
        Bitmap bitmap = null;
        try {
            if (mPostDatas[0] instanceof Bitmap) bitmap = (Bitmap) mPostDatas[0];
            else printContent = (List<String>) mPostDatas[0];
        } catch (Exception e) {
            errCallbackDealer("PosPrinter 中传递的参数只能是List<String>");
            return;
        }
        if (bitmap == null && (printContent == null || printContent.isEmpty())) {
            Util.showMsg(mContext, "打印内容为空，请检查服务打印转换");
            return;
        }
        try {
            DeviceService.login(mContext);
            if (bitmap != null) printImage(bitmap);
            else print(printContent);
        } catch (Exception e) {
            Util.showMsg(mContext, "打印机打开失败");
        }
    }

    @Override
    public void stop() {
        //DeviceService.logout();
    }

    private void printImage(final Bitmap bitmap) {
        try {
            new Printer.Progress() {
                @Override
                public void doPrint(Printer printer) throws Exception {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    printer.printImage(0, 384, 200, baos.toByteArray());
                }

                @Override
                public void onFinish(int code) {
                    if (Printer.ERROR_NONE != code) Util.showMsg(mContext, getMsg(code));
                    stop();
                    System.out.println("打印完毕");
                }

                @Override
                public void onCrash() {
                    Util.showMsg(mContext, "打印机异常");
                    stop();
                }
            }.start();
        } catch (Exception e) {
            Util.showMsg(mContext, "打印机异常");
            stop();
        }
    }

    private void print(final List<String> content) {
        try {
            new Printer.Progress() {
                @Override
                public void doPrint(Printer printer) {
                    printer.setMode(Printer.MODE_REAL);
                    try {
                        printer.setAutoTrunc(false);//设置打印内容超过一行是否截断
                        String wstyle = StringUtils.substring(content.get(0), 0, content.get(0).indexOf("$", 1) + 1);
                        int width = Util.getDecimal(MposPub.parseStyle("s_width", wstyle)).intValue();
                        for (String line : content) {
                            if (!line.startsWith("$") || line.length() < 2 || line.indexOf("$", 1) < 0) {
                                printer.setFormat(getFormat(0));
                                printer.println(line);
                                continue;
                            }
                            String style = StringUtils.substring(line, 0, line.indexOf("$", 1) + 1);
                            String value = line.replace(style, "");
                            String type = MposPub.parseStyle("type", style);
                            if ("feed".equals(type)) {//走纸
                                printer.feedLine(Util.getDecimal(value).intValue());
                                continue;
                            }
                            String align = MposPub.parseStyle("align", style);
                            int font = Util.getInt(MposPub.parseStyle("font", style));
                            printer.setFormat(getFormat(font));
                            if ("text".equals(type) || Util.isEmpty(type)) {//文本
                                if ("right".equals(align)) value = MposPub.rightStr(value, width);
                                else if ("center".equals(align))
                                    value = MposPub.centerStr(value, width);
                                else if ("col".equals(align) && width > 0)
                                    value = MposPub.alignPrint(value, width);
                                printer.println(value);
                                continue;
                            }
                            String height = MposPub.parseStyle("height", style);
                            String wd = MposPub.parseStyle("width", style);
                            if ("qrcode".equals(type)) {//二维码
                                String offset = MposPub.parseStyle("offset", style);
                                QrCode qrCode = new QrCode(value, QrCode.ECLEVEL_Q);
                                printer.printQrCode(Util.getDecimal(offset).intValue(), qrCode, Util.getDecimal(height, BigDecimal.valueOf(200)).intValue());
                            } else if ("barcode".equals(type)) {//条形码
                                //codeWidth 单条条码宽度（从效果来看，建议宽度>=2），有效值为 1~8，可使用-1 指定默认值。
                                //codeHeight - 1~320 且必须是 8 的倍数，可使用-1 指定默认值。
                                //columnOff - 列偏移， 0~一个点行可打印的像素点数－当前设置的条码宽度 X46，如果设置太大可能使整个条码无法打出，可使用-1 指定默认值。
                                //lineOff - 行偏移,0~当前设置的页长，可使用-1 指定默认值。
                                int codeWidth = Util.getInt(wd);//4 比较好
                                int codeHeight = Util.getInt(height);//120 比较好
                                if (codeWidth < 1 || codeWidth > 8) codeWidth = -1;
                                if (codeHeight < 8 || codeWidth > 320) codeHeight = -1;
                                else if (codeHeight % 8 != 0)
                                    codeHeight = ((codeHeight + 7) / 8) * 8;
                                int columnOff = Util.getInt(MposPub.parseStyle("columnOff", style));// 50 比较好
                                if (columnOff < 0) columnOff = -1;
                                printer.printBarCode(codeWidth, codeHeight, columnOff, -1, value);
                            }
                        }
                    } catch (Exception ex) {
                        errCallbackDealerOnUiTread("打印异常：" + ex);
                    }
                }

                @Override
                public void onFinish(int code) {
                    if (Printer.ERROR_NONE != code) Util.showMsg(mContext, getMsg(code));
                    stop();
                }

                @Override
                public void onCrash() {
                    errCallbackDealerOnUiTread("打印机异常");
                    stop();
                }
            }.start();
        } catch (Exception e) {
            Util.showMsg(mContext, "打印机异常");
            stop();
        }
    }

    private Format getFormat(int font) {
        Format format = new Format();
        switch (font) {
            case 2:
                format.setAscSize(Format.ASC_DOT5x7);
                format.setAscScale(Format.ASC_SC1x2);
                format.setHzScale(Format.HZ_SC1x2);
                break;
            default://默认
                format.setAscSize(Format.ASC_DOT5x7);
                format.setAscScale(Format.ASC_SC1x1);
                format.setHzScale(Format.HZ_SC1x1);
                break;
        }
        return format;
    }

    /**
     * 根据错误码获取信息
     *
     * @param code 错误码
     * @return 错误信息
     */
    private String getMsg(int code) {
        switch (code) {
            case Printer.ERROR_PAPERENDED:
                return "缺纸，不能打印";
            case Printer.ERROR_HARDERR:
                return "硬件错误";
            case Printer.ERROR_OVERHEAT:
                return "打印头过热";
            case Printer.ERROR_BUFOVERFLOW:
                return "缓冲模式下所操作的位置超出范围";
            case Printer.ERROR_LOWVOL:
                return "低压保护";
            case Printer.ERROR_PAPERENDING:
                return "纸张将要用尽，还允许打印*(单步进针打特有返回值)";
            case Printer.ERROR_MOTORERR:
                return "打印机芯故障(过快或者过慢)";
            case Printer.ERROR_PENOFOUND:
                return "自动定位没有找到对齐位置,纸张回到原来位置";
            case Printer.ERROR_PAPERJAM:
                return "卡纸";
            case Printer.ERROR_NOBM:
                return "没有找到黑标";
            case Printer.ERROR_BUSY:
                return "打印机处于忙状态";
            case Printer.ERROR_BMBLACK:
                return "黑标探测器检测到黑色信号";
            case Printer.ERROR_WORKON:
                return "打印机电源处于打开状态";
            case Printer.ERROR_LIFTHEAD:
                return "打印头抬起*(自助热敏打印机特有返回值)";
            case Printer.ERROR_LOWTEMP:
                return "低温保护或AD出错*(自助热敏打印机特有返回值)";
        }
        return "未知错误";
    }


}
