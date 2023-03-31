package com.cxycxx.mposcore.device;

import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;

import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.util.Util;
import com.google.gson.JsonObject;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

public class CameraHolder {
    //	static {
//        System.loadLibrary("iconv");
//    } 

    public CameraHolder() {
    }

    public CameraHolder(OnFBCommuFinish dealer, String taskTag) {
        this.dealer = dealer;
        this.taskTag=taskTag;
    }

    public Camera getCamera() {
        if(mCamera!=null)return mCamera;
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mCamera == null) return null;

        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        return mCamera;
    }

    public PreviewCallback getPreviewCallback() {
        return preview;
    }

    public AutoFocusCallback getAutoFocusCallback() {
        return autoFocusCB;
    }

    /**
     * 关闭相机
     */
    public void releaseCamera() {
        //isPreviewing=false;
        if (mCamera == null) return;
        try {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }catch (Exception ex){
            ex.printStackTrace();
        }

        //scanner=null;
    }

    /**
     * 提交结果
     * @param result 结果
     */
    private synchronized void commit(JsonObject result){
        if(dealer==null||result==null||isFinished)return;
        isFinished=true;
        dealer.onFBCommuFinish(result,taskTag);
    }
    private PreviewCallback preview = new PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);
            if (scanner.scanImage(barcode) == 0) return;
            isPreviewing = false;
            SymbolSet syms = scanner.getResults();
            String content = null;
            int contentType=0;
            for (Symbol sym : syms) {
                content = sym.getData();
                contentType=sym.getType();
            }
            releaseCamera();
            if(Util.isEmpty(content))return;
            JsonObject result=new JsonObject();
            result.addProperty("result", "成功");
            result.addProperty("content", content);
            result.addProperty("contentType", contentType==Symbol.QRCODE?"二维码":"条形码");
            commit(result);
        }
    };
    private AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (mCamera == null) return;
            if (isPreviewing) mCamera.autoFocus(autoFocusCB);
        }
    };
    private ImageScanner scanner;
    private Camera mCamera;
    private OnFBCommuFinish dealer;
    private boolean isPreviewing = true;
    private boolean isFinished = false;//是否完成
    private String taskTag="";


}
