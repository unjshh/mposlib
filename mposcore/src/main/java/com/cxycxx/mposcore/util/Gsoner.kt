package com.cxycxx.mposcore.util

import android.os.Bundle
import android.text.TextUtils
import com.google.gson.*
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*


val GSON = Gson()

object Gsoner {
    /**
     *
     * @param src
     * @return
     */
    fun toJsonElement(src: Any?): JsonElement {
        if (src == null) return JsonNull.INSTANCE
        if (src is JsonElement) {
            return src
        }
        if (src is Number) {
            return JsonPrimitive(src)
        }
        if (src.javaClass.isPrimitive) {
            return JsonPrimitive(src.toString())
        }
        if (src is List<*>) {
            val arr = JsonArray(src.size)
            src.forEach { arr.add(toJsonElement(it)) }
            return arr
        }
        if (src is Map<*, *>) {
            val job = JSONObject(src)
            return GSON.fromJson(job.toString(), JsonObject::class.java)
        }
        if (src is String) {
            return try {
                GSON.fromJson(src, JsonObject::class.java)
            } catch (ex: Exception) {
                JsonPrimitive(src)
            }
        }
        if (src is Bundle) {
            val rst = JsonObject()
            for (p in src.keySet()) {
                rst.add(p, toJsonElement(src[p]))
            }
            return rst
        }
        return GSON.fromJson(GSON.toJson(src), JsonObject::class.java)
    }

    /**
     * 把对象转换成JsonObject
     *
     * @param src 源
     * @return JsonObject对象
     */
    fun fromObject(src: Any?): JsonObject {
        if (src == null) return JsonObject()
        if (src is JsonObject) {
            return GSON.fromJson(src.toString(), JsonObject::class.java)
        }
        val je = toJsonElement(src)
        return if (je is JsonObject) je else JsonObject()
    }

    /**json字符串转成T对象**/
    inline fun <reified T> fromJson(json: String): T = GSON.fromJson(json, T::class.java)

    /**json字符串转成T对象**/
    inline fun <reified T> fromJson(json: JsonElement): T = GSON.fromJson(json, T::class.java)
}

/**
 * 为了JsonObject能使用[]操作符
 */
operator fun JsonObject?.set(property: String?, element: Any?) {
    if (this == null || element == null || property == null || property.isBlank()) return
    this.add(property, Gsoner.toJsonElement(element))
}

/**
 * 把json转换成map
 *
 * @return map对象
 */
fun JsonObject?.toMap(
    onlyPrimitive: Boolean = true
): MutableMap<String, Any> {
    if (this == null || this.isJsonNull) return mutableMapOf()
    val map: MutableMap<String, Any> = HashMap()
    entrySet().filter { it.value != null && !it.value.isJsonNull }
        .filter { !onlyPrimitive || it.value.isJsonPrimitive }
        .forEach { (k, v) ->
            if (v.isJsonPrimitive) {
                map[k] = v.asString
            } else if (v.isJsonObject) {
                map[k] = v.asJsonObject.toMap()
            } else if (v.isJsonArray) {
                map[k] = v.asJsonArray.toList()
            }
        }

    return map
}

/**
 * 把json转换成map（只转第一层）
 */
fun JsonObject?.toMapS(): MutableMap<String, String> {
    if (this == null || this.isJsonNull) return mutableMapOf()
    val map: MutableMap<String, String> = HashMap()
    for ((key, value) in this.entrySet()) {
        if (value == null || value.isJsonNull || !value.isJsonPrimitive) continue
        map[key] = value.asString
    }
    return map
}

/**
 * 把JsonArray转成list
 *
 * @param array
 * @return
 */
fun JsonArray?.toList(): List<Any> {
    if (this == null || this.isJsonNull) return emptyList()
    if (size() == 0) return emptyList()
    val list: MutableList<Any> = ArrayList()
    forEach {
        if (it.isJsonPrimitive) {
            list.add(it.asString)
        } else if (it.isJsonObject) {
            list.add(it.asJsonObject.toMap())
        } else if (it.isJsonArray) {
            list.add(it.asJsonArray.toList())
        }
    }
    return list
}

/**
 * 从parent中获取property对应的属性值
 *
 * @param parent
 * @param property 为null时parent转成字符串
 * @return 异常时返回 ""
 */
fun JsonElement?.joAsString(property: String? = ""): String {
    if (this == null || this.isJsonNull) return ""
    try {
        if (TextUtils.isEmpty(property)) return this.asString
        val obj = this.getJsonMember(property!!)
        return if (obj == null) "" else obj.asString
    } catch (e: Exception) {
        return ""
    }
}

/**
 * 获取json属性
 *
 * @param src        资源
 * @param memberPath 属性路径(比如 pay.bank.module)
 * @return 属性
 */
fun JsonElement?.getJsonMember(memberPath: String? = ""): JsonElement? {
    if (this == null || this.isJsonNull) return null
    val mp = memberPath?.trim { it <= ' ' }
    if (TextUtils.isEmpty(mp)) return this
    var current = this
    try {
        if (!mp!!.contains(".")) return current.asJsonObject[mp]
        for (mem in mp.split("\\.".toRegex()).toTypedArray()) {
            if (current == null) return null
            if (TextUtils.isEmpty(mem)) continue
            current = if (mem.contains("[")) {
                val s = mem.indexOf("[")
                val index = mem.substring(s, mem.indexOf("]")).trim { it <= ' ' }.toInt()
                current.asJsonObject[mem.substring(0, s)].asJsonArray[index]
            } else {
                current.asJsonObject[mem]
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return current
}

/**
 * 从parent中获取property对应的属性值
 *
 * @param parent
 * @param property
 * @return 异常时返回 0
 */
fun JsonElement?.joAsBigDecimal(
    property: String? = "",
    dft: BigDecimal = BigDecimal.ZERO
): BigDecimal {
    if (this == null || this.isJsonNull) return dft
    try {
        if (TextUtils.isEmpty(property)) return this.asBigDecimal
        val obj = this.getJsonMember(property)
        return if (obj == null) dft else obj.asBigDecimal
    } catch (e: Exception) {
        return dft
    }
}

/**
 * 从parent中获取property对应的属性值
 *
 * @param parent
 * @param property
 * @return 异常时返回 0
 */
fun JsonElement?.joAsInt(property: String? = ""): Int {
    return this.joAsBigDecimal(property).toInt()
}

/**
 * 从parent中获取property对应的属性值
 *
 * @param parent
 * @param property
 * @return 异常时返回 0
 */
fun JsonElement?.joAsBool(property: String? = ""): Boolean {
    return if (this == null || this.isJsonNull) false else try {
        if (TextUtils.isEmpty(property)) return this.asBoolean
        val obj = this.getJsonMember(property)
        obj != null && obj.asBoolean
    } catch (e: Exception) {
        false
    }
}

/**
 * 从parent中获取property对应的属性值
 *
 * @param parent
 * @param property
 * @return 异常时返回 0
 */
fun JsonElement?.joAsDouble(property: String? = ""): Double {
    return this.joAsBigDecimal(property).toDouble()
}

/**
 * 从parent中获取property对应的属性值
 *
 * @param parent
 * @param property
 * @return 异常时返回 0
 */
fun JsonElement?.joAsLong(property: String? = ""): Long {
    return this.joAsBigDecimal(property).toLong()
}

/**
 * 从parent中获取property对应的属性值
 *
 * @param parent
 * @param property
 * @return 两位小数的金额
 */
fun JsonElement?.joAsMoney(property: String? = ""): BigDecimal {
    return this.joAsBigDecimal(property)
        .setScale(2, RoundingMode.HALF_UP)
}

/**
 * 从parent中获取property对应的属性值
 *
 * @param parent
 * @param property
 * @return
 */
fun JsonElement?.joAsJsonArray(property: String? = ""): JsonArray {
    val je = this.getJsonMember(property)
    return if (je == null || !je.isJsonArray) JsonArray() else je.asJsonArray
}

/**
 * 合并JsonObject[如果obj1和obj2有相同的属性,保留obj2的]
 *
 * @param obj1
 * @param obj2
 * @return
 */
fun JsonObject?.mergeJsonObject(obj2: JsonObject?): JsonObject {
    val res = JsonObject()
    if (this != null) for ((key, value) in this.entrySet()) res.add(
        key, value
    )
    if (obj2 != null) for ((key, value) in obj2.entrySet()) res.add(
        key, value
    )
    return res
}

