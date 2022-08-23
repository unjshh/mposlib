package com.cxycxx.mposcore.util;

import android.os.Bundle;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GsonHelper {
    /**
     * Gson 实例
     */
    public static final Gson GSON = new Gson();

    /**
     * 合并JsonObject[如果obj1和obj2有相同的属性,保留obj2的]
     *
     * @param obj1
     * @param obj2
     * @return
     */
    public static JsonObject mergeJsonObject(JsonObject obj1, JsonObject obj2) {
        JsonObject res = new JsonObject();
        if (obj1 != null) for (Map.Entry<String, JsonElement> mem : obj1.entrySet())
            res.add(mem.getKey(), mem.getValue());
        if (obj2 != null) for (Map.Entry<String, JsonElement> mem : obj2.entrySet())
            res.add(mem.getKey(), mem.getValue());
        return res;
    }

    /**
     * 把json转换成对象
     *
     * @param json
     * @param classOfT
     * @return
     */
    public static <T> T fromJson(JsonElement json, Class<T> classOfT) {
        try {
            return GSON.fromJson(json, classOfT);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 把json转换成对象
     *
     * @param json
     * @param classOfT
     * @return
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        try {
            return GSON.fromJson(json, classOfT);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 把json转换成对象
     *
     * @param json
     * @param token
     * @return
     */
    public static <T> T fromJson(String json, TypeToken<T> token) {
        try {
            return GSON.fromJson(json, token.getType());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 把json转换成对象
     *
     * @param json
     * @param token
     * @return
     */
    public static <T> T fromJson(JsonElement json, TypeToken<T> token) {
        try {
            return GSON.fromJson(json, token.getType());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 把对象转换成JsonObject
     *
     * @param scr 源
     * @return JsonObject对象
     */
    public static JsonObject fromObject(Object scr) {
        if (scr == null) return new JsonObject();
        if (scr instanceof Bundle) {
            JsonObject result = new JsonObject();
            for (String p : ((Bundle) scr).keySet()) {
                Object val = ((Bundle) scr).get(p);
                if (val == null) continue;
                if (val instanceof Long) {
                    result.add(p, new JsonPrimitive((long) val));
                }else if (val instanceof Float) {
                    result.add(p, new JsonPrimitive((float) val));
                } else if (val instanceof Integer) {
                    result.add(p, new JsonPrimitive((int) val));
                } else if (val instanceof Double) {
                    result.add(p, new JsonPrimitive((double) val));
                } else if (val instanceof String || val.getClass().isPrimitive()) {
                    result.add(p, new JsonPrimitive(val.toString()));
                } else if (!(val instanceof Bundle) && !(val instanceof Serializable)) {
                    result.add(p, new JsonPrimitive(val.toString()));
                } else result.add(p, fromObject(val));
            }
            return result;
        } else if (scr instanceof Map) {
            JsonObject result = new JsonObject();
            for (Object p : ((Map) scr).keySet()) {
                Object val = ((Map) scr).get(p);
                if (val == null) continue;
                if (val instanceof Long) {
                    result.add(p.toString(), new JsonPrimitive((long) val));
                } else if (val instanceof Float) {
                    result.add(p.toString(), new JsonPrimitive((float) val));
                } else if (val instanceof Integer) {
                    result.add(p.toString(), new JsonPrimitive((int) val));
                } else if (val instanceof Double) {
                    result.add(p.toString(), new JsonPrimitive((double) val));
                }else if (val instanceof String || val.getClass().isPrimitive()) {
                    result.add(p.toString(), new JsonPrimitive(val.toString()));
                } else if (!(val instanceof Bundle) && !(val instanceof Serializable)) {
                    result.add(p.toString(), new JsonPrimitive(val.toString()));
                } else result.add(p.toString(), fromObject(val));
            }
            return result;
        }
        return fromJson(scr instanceof String ? scr.toString() : GSON.toJson(scr), JsonObject.class);
    }

    /**
     * 从parent中获取property对应的属性值
     *
     * @param parent
     * @param property 为null时parent转成字符串
     * @return 异常时返回 ""
     */
    public static String joAsString(JsonElement parent, String property) {
        if (parent == null || parent.isJsonNull()) return "";
        try {
            if (TextUtils.isEmpty(property)) return parent.getAsString();
            JsonElement obj = getJsonMember(parent, property);
            return obj == null ? "" : obj.getAsString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 从parent中获取property对应的属性值
     *
     * @param parent
     * @param property
     * @return 异常时返回 0
     */
    public static int joAsInt(JsonElement parent, String property) {
        if (parent == null || parent.isJsonNull()) return 0;
        try {
            if (TextUtils.isEmpty(property)) return parent.getAsInt();
            JsonElement obj = getJsonMember(parent, property);
            return obj == null ? 0 : obj.getAsInt();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 从parent中获取property对应的属性值
     *
     * @param parent
     * @param property
     * @return 异常时返回 0
     */
    public static boolean joAsBool(JsonElement parent, String property) {
        if (parent == null || parent.isJsonNull()) return false;
        try {
            if (TextUtils.isEmpty(property)) return parent.getAsBoolean();
            JsonElement obj = getJsonMember(parent, property);
            return obj != null && obj.getAsBoolean();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从parent中获取property对应的属性值
     *
     * @param parent
     * @param property
     * @return 异常时返回 0
     */
    public static BigDecimal joAsBigDecimal(JsonElement parent, String property) {
        if (parent == null || parent.isJsonNull()) return BigDecimal.ZERO;
        try {
            if (TextUtils.isEmpty(property)) return parent.getAsBigDecimal();
            JsonElement obj = getJsonMember(parent, property);
            return obj == null ? BigDecimal.ZERO : obj.getAsBigDecimal();
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 从parent中获取property对应的属性值
     *
     * @param parent
     * @param property
     * @return 异常时返回 0
     */
    public static double joAsDouble(JsonElement parent, String property) {
        return joAsBigDecimal(parent, property).doubleValue();
    }

    /**
     * 从parent中获取property对应的属性值
     *
     * @param parent
     * @param property
     * @return 异常时返回 0
     */
    public static long joAsLong(JsonElement parent, String property) {
        return joAsBigDecimal(parent, property).longValue();
    }

    /**
     * 从parent中获取property对应的属性值
     *
     * @param parent
     * @param property
     * @return 两位小数的金额
     */
    public static BigDecimal joAsMoney(JsonElement parent, String property) {
        return joAsBigDecimal(parent, property).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 从parent中获取property对应的属性值
     *
     * @param parent
     * @param property
     * @return
     */
    public static JsonArray joAsJsonArray(JsonElement parent, String property) {
        JsonElement je = getJsonMember(parent, property);
        return je == null || !je.isJsonArray() ? new JsonArray() : je.getAsJsonArray();
    }

    /**
     * 获取json属性
     *
     * @param src        资源
     * @param memberPath 属性路径(比如 pay.bank.module)
     * @return 属性
     */
    public static JsonElement getJsonMember(JsonElement src, String memberPath) {
        if (TextUtils.isEmpty(memberPath) || src == null) return null;
        memberPath = memberPath.trim();
        JsonElement current = src;
        try {
            if (!memberPath.contains(".")) return current.getAsJsonObject().get(memberPath);
            for (String mem : memberPath.split("\\.")) {
                if (current == null) return null;
                if (TextUtils.isEmpty(mem)) continue;
                if (mem.contains("[")) {
                    int s = mem.indexOf("[");
                    int index = Integer.parseInt(mem.substring(s, mem.indexOf("]")).trim());
                    current = current.getAsJsonObject().get(mem.substring(0, s)).getAsJsonArray().get(index);
                } else {
                    current = current.getAsJsonObject().get(mem);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return current;
    }

    /**
     * 把json转换成map（只转第一层）
     *
     * @param src             json对象
     * @param exceptPrimitive 是否去除不是基本类型的属性
     * @return map对象
     */
    public static Map<String, String> toMap(JsonObject src, boolean exceptPrimitive) {
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : src.entrySet()) {
            if (exceptPrimitive && !entry.getValue().isJsonPrimitive()) continue;
            map.put(entry.getKey(), entry.getValue().getAsString());
        }
        return map;
    }

    /**
     * 把json转换成map
     *
     * @param src json对象
     * @return map对象
     */
    public static Map<String, Object> toMap(JsonObject src) {
        if (src == null) return Collections.emptyMap();
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : src.entrySet()) {
            JsonElement val = entry.getValue();
            if (val.isJsonPrimitive()) {
                map.put(entry.getKey(), val.getAsString());
            } else if (val.isJsonObject()) {
                map.put(entry.getKey(), toMap(val.getAsJsonObject()));
            } else if (val.isJsonArray()) {
                map.put(entry.getKey(), jsonArrayToList(val.getAsJsonArray()));
            }
        }
        return map;
    }

    /**
     * 把json转换成map
     *
     * @param src json对象
     * @return map对象
     */
    public static Map<String, Object> toMap(JSONObject src) {
        return toMap(fromObject(src.toString()));
    }

    /**
     * 把JsonArray转成list
     *
     * @param array
     * @return
     */
    public static List<Object> jsonArrayToList(JsonArray array) {
        if (array == null) return null;
        if (array.size() == 0) return Collections.emptyList();
        List<Object> list = new ArrayList<>();
        for (JsonElement ele : array) {
            if (ele.isJsonPrimitive()) list.add(ele.getAsString());
            else if (ele.isJsonObject()) {
                list.add(toMap(ele.getAsJsonObject()));
            } else if (ele.isJsonArray()) {
                list.add(jsonArrayToList(ele.getAsJsonArray()));
            }
        }
        return list;
    }
}
