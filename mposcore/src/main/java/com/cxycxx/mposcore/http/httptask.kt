package com.cxycxx.mposcore.http

import android.content.Context
import com.cxycxx.mposcore.util.Util
import com.cxycxx.mposcore.util.joAsString
import com.google.gson.JsonObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

const val HTTP_TASK_MSG_NAME = "message"
const val HTTP_TASK_RESULT_NAME = "result"
const val HTTP_TASK_RESULT_OK = "成功"
typealias HttpTaskRequest = JsonObject
typealias HttpTaskResponse = JsonObject

/**
 * 方法名称
 */
var HttpTaskRequest.methodName: String
    set(value) = addProperty("methodName", value)
    get() = joAsString("methodName")

/**
 * 响应是否成功
 */
val HttpTaskResponse.isOKInRes get() = joAsString(HTTP_TASK_RESULT_NAME) == HTTP_TASK_RESULT_OK

/**
 * 回应信息
 */
val HttpTaskResponse.msgInRes get() = joAsString(HTTP_TASK_MSG_NAME)

/**
 * 启动网络请求
 */
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun launchHttpTask(
    ps: HttpTaskRequest,
    context: Context? = null,
    dealErrorModel: Int = HttpTask.AUTO_SHOW_ERROR,
    isNeedShowWaitDialog: Boolean = true
): HttpTaskResponse? =
    suspendCancellableCoroutine { scc ->
        HttpTask(context) {
            if (it.isOKInRes || dealErrorModel == HttpTask.ALWAYS_CALLBACK) {
                scc.resume(it)
                return@HttpTask
            }
            if (dealErrorModel == HttpTask.AUTO_SHOW_ERROR) {
                Util.showMsg(context, it.msgInRes)
            }
            scc.resume(null)
        }.apply {
            this.isNeedShowWaitDialog = isNeedShowWaitDialog
            setDealErrorModel(HttpTask.ALWAYS_CALLBACK)
        }.launch(ps)
    }