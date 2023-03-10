package com.cxycxx.n900;

import android.content.Context;
import android.graphics.Bitmap;

import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.mpos.MposPub;
import com.cxycxx.mposcore.util.Util;
import com.newland.me.ConnUtils;
import com.newland.me.DeviceManager;
import com.newland.mtype.ModuleType;
import com.newland.mtype.module.common.printer.PrintContext;
import com.newland.mtype.module.common.printer.Printer;
import com.newland.mtype.module.common.printer.PrinterStatus;
import com.newland.mtypex.nseries.NSConnV100ConnParams;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * N900打印机
 */
public class N900Printer extends FBCommu {
    public N900Printer(Context context, String taskId, OnFBCommuFinish dealer) {
        super(context, taskId, dealer);
        deviceManager = ConnUtils.getDeviceManager();
        deviceManager.init(mContext, "com.newland.me.K21Driver", new NSConnV100ConnParams(), null);
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
            errCallbackDealer("N900Printer 中传递的参数只能是List<String>");
            return;
        }
        if (bitmap == null && (printContent == null || printContent.isEmpty())) {
            Util.showMsg(mContext, "打印内容为空，请检查服务打印转换");
            return;
        }
        final List<String> printHolder = new ArrayList<>(printContent);
        final Bitmap bitmap1 = bitmap;
        new Thread(() -> {
            try {
                deviceManager.connect();//不能少
                Printer printer = (Printer) deviceManager.getDevice().getStandardModule(ModuleType.COMMON_PRINTER);
                //printer.print()
                printer.init();
                if (printer.getStatus() == PrinterStatus.NORMAL) {
                    if (bitmap1 != null) printer.print(0, bitmap1, 30, TimeUnit.SECONDS);//打印图片
                    else printTexts(printHolder, printer);
                }
            } catch (Exception e) {
                errCallbackDealerOnUiTread("打印异常:" + e);
                e.printStackTrace();
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

    /**
     * 打印字符串
     *
     * @param printContent 字符串数组
     * @param printer
     * @throws Exception
     */
    private void printTexts(List<String> printContent, Printer printer) throws Exception {
        StringBuilder builder = new StringBuilder();
        String wstyle = StringUtils.substring(printContent.get(0), 0, printContent.get(0).indexOf("$", 1) + 1);
        int width = Util.getDecimal(parseStyle("s_width", wstyle)).intValue();
        String lineSpace = "6";
        builder.append(String.format("!yspace %1$s\n", lineSpace));//行间距
        for (String line : printContent) {
            if (!line.startsWith("$") || line.length() < 2 || line.indexOf("$", 1) < 0) {
                String gs=String.format("!NLFONT %1$s %2$s %3$s\n", "1", "12", "3");//默认格式
                builder.append(gs+String.format("*text %1$s %2$s\n", "l", line));
                continue;
            }
            String style = StringUtils.substring(line, 0, line.indexOf("$", 1) + 1);//样式部分
            String value = line.replace(style, "");//内容部分
            String type = parseStyle("type", style);//内容类型
            if("feed".equals(type)){
                builder.append(String.format("*feedline %1$d\n", Util.getDecimal(value).intValue()));
                continue;
            }
            String align = parseStyle("align", style);
            if (Util.isEmpty(align)) align = "left";
            String position = "l";
            if ("right".equals(align)) position = "r";
            else if ("center".equals(align)) position = "c";
            else if ("col".equals(align) && width > 0) {
                value = MposPub.alignPrint(value, width);
            }
            if("text".equals(type)||Util.isEmpty(type)){//文本
                String attFont = parseStyle("font", style);
                int font = Util.getDecimal(Util.isEmpty(attFont)?"2":attFont).intValue();
                String attBold = parseStyle("bold", style);
                int bold = Util.getDecimal(Util.isEmpty(attBold)?"0":attBold).intValue();
                builder.append(getFont(font, bold) + String.format("*text %1$s %2$s\n", position, value));
                continue;
            }
            if("qrcode".equals(type)){//二维码
               builder.append(String.format("*qrcode %1$s %2$s\n", position, value));
            }else if("barcode".equals(type)){//条形码
                builder.append(String.format("*barcode %1$s %2$s\n", position, value));
            }
        }
        printer.printByScript(PrintContext.defaultContext(), builder.toString().getBytes("GBK"), 30L, TimeUnit.SECONDS);
    }

    /**
     * 获取样式的值
     *
     * @param type   样式名称
     * @param styles 样式集
     * @return 样式的值
     */
    private String parseStyle(String type, String styles) {
        if (Util.isEmpty(type)) return "";
        for (String kv : StringUtils.split(styles.replace("$", ""), ";")) {
            if (Util.isEmpty(kv)) continue;
            String[] ss = StringUtils.split(kv, ":");
            if (ss.length < 2) continue;
            if (ss[0].trim().equals(type)) return ss[1];
        }
        return "";
    }

    /**
     * 获取字体
     *
     * @param font
     * @param bold
     * @return
     */
    private String getFont(int font, int bold) {
        String c = "6";//中文
        String e = "1";//英文
        String gray = "3";//灰度
        if (font >= 3) {//最大
            c = "3";
            e = "13";
        } else if (2 == font) {//中等
            c = "1";
            e = "12";
        }
        if (bold>=1) gray = "2";
        return String.format("!NLFONT %1$s %2$s %3$s\n", c, e, gray);
    }

    private DeviceManager deviceManager;
}
