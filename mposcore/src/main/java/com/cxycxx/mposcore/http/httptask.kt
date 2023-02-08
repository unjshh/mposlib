package com.cxycxx.mposcore.http

import android.content.Context
import com.cxycxx.mposcore.util.Util
import com.google.gson.JsonObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 启动网络请求
 */
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun launchHttpTask(
    ps: JsonObject? = null,
    context: Context? = null,
    dealErrorModel: Int = HttpTask.AUTO_SHOW_ERROR
): JsonObject? =
    suspendCancellableCoroutine { scc ->
        HttpTask(context) {
            if (it["result"]?.asString == "成功" || dealErrorModel == HttpTask.ALWAYS_CALLBACK) {
                scc.resume(it)
                return@HttpTask
            }
            if (dealErrorModel == HttpTask.AUTO_SHOW_ERROR) {
                Util.showMsg(context, it["message"]?.asString ?: "")
            }
            scc.resume(null)
        }.apply {
            setDealErrorModel(HttpTask.ALWAYS_CALLBACK)
        }.launch(ps)
    }