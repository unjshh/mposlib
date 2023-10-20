package com.cxycxx.mposcore.mpos.bean

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.math.BigDecimal

/**
 * 会员
 */
class Member {
    var id = -1
    var typeId = 0

    /**
     * 会员卡号
     */
    var code = ""

    /**
     * 会员姓名
     */
    var name = ""

    /**
     * 会员类型名称
     */
    var typeName = ""

    /**
     * 手机号
     */
    var telephone = ""

    /**
     * 可用积分
     */
    var validCent = BigDecimal.ZERO //可用积分

    /**
     * 累计积分
     */
    var totalCent = BigDecimal.ZERO //累计积分


    /**
     * 登录内容
     */
    var loginContent = ""

    /**
     * 登录方式
     */
    var loginType = ""

    /**
     * 有效期
     */
    var validity = ""

    /**
     * 是否折扣
     */
    var isDiscount = true //是否折扣(默认折扣)

    /**
     * 储值卡
     */
    var cashCard: JsonObject? = null

    /**
     * 券
     */
    var coupons: JsonArray? = null
    val cardStatusTxt = ""
        get() = field.ifBlank { code }
}