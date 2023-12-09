package com.cxycxx.mposcore.util

import android.text.TextUtils
import com.cxycxx.mposcore.mpos.MposPub
import com.google.gson.JsonObject
import com.squareup.duktape.Duktape

object JsHelper {
    /**
     * 执行Js
     *
     * @param src 传入的参数
     * @param js js内容
     * @param jsProName js文件名
     * @return 执行结果
     */
    fun execJs(src: Any, keyCode: String, js: String, jsProName: String? = ""): Any? {
        val log = JsonObject()
        val jo = Gsoner.fromObject(src)
        if (!TextUtils.isEmpty(jsProName)) {
            log.addProperty("jsProName", jsProName)
        }
        log.add("req", GsonHelper.fromObject(src))
        return try {
            //---------
            val srcJson = jo.toString()
            val exeJs = String.format("exeTrans(%s);", srcJson)
            val duktape = Duktape.create()
            val res = duktape.evaluate(
                """
                    $js
                    $exeJs
                    """.trimIndent()
            )
            duktape.close()
            log.add("res", GsonHelper.fromObject(res))
            MposPub.clientLog(log, keyCode)
            res
        } catch (ex: Exception) {
            log.addProperty("exception", ex.toString())
            MposPub.clientLog(log, keyCode)
            null
        }
    }

    /**
     * 执行Js
     * @param src 传入的参数
     * @param js
     * @return
     */
    fun execJSO(src: Any, keyCode: String, js: String, jsProName: String? = ""): JsonObject {
        val rst = execJs(src, keyCode, js, jsProName)
        return Gsoner.fromObject(rst)
    }
}