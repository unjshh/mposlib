package com.cxycxx.mposcore.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 针对android 的工具
 */

public class UtilC {
    /**
     * 从view 获取 Bitmap
     *
     * @param view
     * @param activity
     * @return
     */
    public static Bitmap getBitmapFromView(View view, Activity activity) {
        if (view == null) return null;
//        view.setDrawingCacheEnabled(true);
//        view.buildDrawingCache();  //启用DrawingCache并创建位图
//        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache()); //创建一个DrawingCache的拷贝，因为DrawingCache得到的位图在禁用后会被回收
//        view.setDrawingCacheEnabled(false);  //禁用DrawingCahce否则会影响性能
//
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;     // 屏幕宽度（像素）
        int height = metric.heightPixels;   // 屏幕高度（像素）
        view.layout(0, 0, width, height);// 整个View的大小 参数是左上角 和右下角的坐标
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(10000, View.MeasureSpec.AT_MOST);
        //measure完后，并不会实际改变View的尺寸，需要调用View.layout方法去进行布局
        //按示例调用layout函数后，View的大小将会变成你想要设置成的大小
        view.measure(measuredWidth, measuredHeight);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        c.drawColor(Color.WHITE);
        /** 如果不设置canvas画布为白色，则生成透明 */
        view.layout(0, 0, view.getWidth(), view.getHeight());
        view.draw(c);
        return bitmap;
    }

    /**
     * 显示对话框（有一个关闭按钮）
     * @param context
     * @param title 标题
     * @param message 信息
     */
    public static void showAlertDialog(Context context,String title,String message){
        JsonObject cfg=new JsonObject();
        cfg.addProperty("title",title);
        cfg.addProperty("message",title);
        JsonArray buttons=new JsonArray();
        JsonObject button=new JsonObject();
        button.addProperty("text","关闭");
        buttons.add(button);
        cfg.add("buttons",buttons);
        showAlertDialog(context,cfg);
    }


    /**
     * 显示带有[确定 和 取消 两个按钮]，可多个输入框的对话框
     * @param context
     * @param pl 确定按钮监听
     * @param nl 取消按钮监听
     * @param hints 输入框的提示语,也是监听中返回的,[0]是标题,[1]开始才是输入框
     * @return [0]是dialog,[1]是inputs数组
     */
    public static Object[] showAlertDialog(Context context,DialogInterfaceClickListener pl,DialogInterfaceClickListener nl,String... hints){
        JsonObject cfg=new JsonObject();
        cfg.addProperty("title",hints[0]);
        JsonArray buttons=new JsonArray();
        JsonObject b1=new JsonObject();
        b1.addProperty("type","积极");
        b1.addProperty("text","确定");
        buttons.add(b1);
        JsonObject b2=new JsonObject();
        b2.addProperty("type","消极");
        b2.addProperty("text","取消");
        buttons.add(b2);
        cfg.add("buttons",buttons);

        JsonArray inputs=new JsonArray();
        for (int i = 1; i < hints.length; i++) {
            JsonObject input=new JsonObject();
            input.addProperty("hint",hints[i]);
            inputs.add(input);
        }
        cfg.add("inputs",inputs);
        return showAlertDialog(context,cfg,pl,nl);
    }
    /**
     * 显示对话框
     * @param context
     * @param config
     * @param listeners
     * @return [0]是dialog,[1]是inputs数组
     */
    public static Object[] showAlertDialog(Context context, JsonObject config, DialogInterfaceClickListener... listeners){
        Map<String,String> kv=new HashMap<>();
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle(Util.joAsString(config,"title"));
        if(config.has("message"))builder.setMessage(Util.joAsString(config,"message"));
        LinearLayout ll = new LinearLayout(context);
        EditText[] inputs=new EditText[0];
        if(config.has("inputs")){
            ll.setOrientation(LinearLayout.VERTICAL);
            List<EditText> ets=new ArrayList<>();
            Stream.of(config.getAsJsonArray("inputs")).map(p->p.getAsJsonObject()).forEach(p->{
                EditText et = new EditText(context);
                et.setHint(Util.joAsString(p,"hint"));
                if("数字".equals(Util.joAsString(p,"inputType")))et.setInputType(InputType.TYPE_CLASS_NUMBER);
                ll.addView(et);
                ets.add(et);
            });
            builder.setView(ll);
            inputs=ets.toArray(new  EditText[0]);
        }
        JsonArray buttons=config.getAsJsonArray("buttons");
        for (int i = 0; i < buttons.size(); i++) {
            String type=Util.joAsString(buttons.get(i),"type");
            String text=Util.joAsString(buttons.get(i),"text");
            int index=i;
            DialogInterface.OnClickListener lis=(dialog, which) -> {
                for (int j = 0,l=ll.getChildCount(); j < l; j++) {
                    TextView tv= (TextView) ll.getChildAt(j);
                    kv.put(tv.getHint().toString(),Util.getText(tv));
                }
                if(listeners[index]!=null)listeners[index].onClick(dialog,which,kv);
            };
            if("积极".equals(type))builder.setPositiveButton(text,listeners.length<=i?null:lis);
            else if("消极".equals(type))builder.setNegativeButton(text,listeners.length<=i?null:lis);
            else builder.setNeutralButton(text,listeners.length<=i?null:lis);
        }
        Dialog dialog=builder.create();
        dialog.show();
        return new Object[]{dialog,inputs} ;
    }
    /**
     * 获取应用程序名称
     */
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return context.getResources().getString(packageInfo.applicationInfo.labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
    /**
     * 对话框回调接口
     */
    public interface DialogInterfaceClickListener {
        /**
         * 对话框回调
         * @param dialog  对话框
         * @param which
         * @param res 返回的数据,比如输入框的内容
         */
        void onClick(DialogInterface dialog, int which,Object res);
    }

}
