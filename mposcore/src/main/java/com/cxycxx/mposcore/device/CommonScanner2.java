package com.cxycxx.mposcore.device;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.cxycxx.mposcore.FBCommu;
import com.cxycxx.mposcore.OnFBCommuFinish;
import com.cxycxx.mposcore.device.zxing.activity.CaptureActivity;
import com.cxycxx.mposcore.device.zxing.util.Constant;

/**
 * Created by unjsh on 2018/9/7.
 */

public class CommonScanner2 extends FBCommu {
    /**
     * @param context 上下文
     * @param taskId  任务id
     * @param dealer  接收处理者，如果不接收可以为null(比如只输出的情况下)
     */
    public CommonScanner2(Context context, String taskId, OnFBCommuFinish dealer) {
        super(context, taskId, dealer);
    }

    @Override
    public void launch() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(mContext, CaptureActivity.class);
        ((Activity) mContext).startActivityForResult(intent, Constant.REQ_QR_CODE);
    }

    @Override
    public void stop() {

    }
}
