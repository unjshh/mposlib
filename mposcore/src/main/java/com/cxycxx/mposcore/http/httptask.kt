package com.cxycxx.mposcore.http

import android.content.Context
import com.google.gson.JsonObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * 启动网络请求
 */
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun launchHttpTask(ps: JsonObject? = null, context: Context? = null): JsonObject =
    suspendCancellableCoroutine { scc ->
        HttpTask(context) {
            scc.resume(it) {
                //取消协程时
            }
        }.launch(ps)
    }