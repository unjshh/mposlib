package com.cxycxx.mposcore.log

import android.os.Build
import android.text.TextUtils

object RLogger {
    /**
     * 是否记日志
     */
    var isOpen = true
    /**
     * 日志表名
     */
    var logCollection: String = ""

    /**
     * 接口过滤
     */
    val interfaceFilter = mutableSetOf<String>()
    var recordImp: ((LogModel) -> Unit)? = null

    /**
     * 记日志
     */
    fun recordLog(log: LogModel) {
        if (TextUtils.isEmpty(logCollection) || !isOpen) return
        if (interfaceFilter.contains(log.Interface)) return
        recordImp?.invoke(log)
    }

    /**
     * 记日志
     */
    fun recordLog(postData: String, inf: String, state: String = "成功") {
        val log = createLogModel()
        log.PostData = postData
        log.Interface = inf
        log.State = state
        recordLog(log)
    }

    /**
     * 创建日志
     * @return
     */
    fun createLogModel(): LogModel {
        val log = LogModel()
        log.DeviceType = Build.MODEL
        log.DeviceCode = "Android " + Build.VERSION.RELEASE
        log.Key = Build.BRAND
        return log
    }
}