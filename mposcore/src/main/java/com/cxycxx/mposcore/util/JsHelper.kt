package com.dycx.p.core.util

import com.cxycxx.mposcore.util.GsonHelper
import com.dycx.p.core.log.RLogger
import com.google.gson.JsonObject
import com.squareup.duktape.Duktape
import java.lang.Exception

object JsHelper {
    /**
     * 执行Js
     *
     * @param src 传入的参数
     * @param js
     * @return 执行结果
     */
    fun execJs(src: Any, js: String): Any? {
        return try {
            val srcJson = when (src) {
                is JsonObject -> src.toString()
                else -> GsonHelper.fromObject(src).toString()
            }
            val exeJs = String.format("exeTrans(%s);", srcJson)
            val duktape: Duktape = Duktape.create()
            val res = duktape.evaluate(js + exeJs)
            duktape.close()
            res
        } catch (ex: Exception) {
            RLogger.recordLog("执行JS异常：$ex", "exeJsTrans", "异常")
            null
        }
    }

    /**
     * 执行Js
     * @param src 传入的参数
     * @param js
     * @return
     */
    fun execJSO(src: Any, js: String): JsonObject {
        val rst = execJs(src, js)
        return GsonHelper.fromObject(rst)
    }
}