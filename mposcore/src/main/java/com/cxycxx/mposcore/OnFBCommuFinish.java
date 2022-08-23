package com.cxycxx.mposcore;


import com.google.gson.JsonObject;

/**
 * 当 FBCommu 对象处理完之后会返回处理完之后
 */
public interface OnFBCommuFinish {
    /**
     * 处理返回
     * @param response  后台返回值
     * @param taskDescribe  任务描述
     */
    void onFBCommuFinish(JsonObject response, String taskDescribe);
}
