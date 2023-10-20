package com.cxycxx.mposcore.mpos.bean

/**
 * 券
 */
class Coupon {
    /**
     * 券id
     */
    var id = 0

    /**
     * 券类型
     */
    var typeId = 0

    /**
     * 券名称
     */
    var name = ""

    /**
     * 展示名称
     */
    var showName = ""
        get() = field.ifBlank { name }

    /**
     * 所属卡id
     */
    var cardId = 0

    /**
     * 所属卡卡号
     */
    var cardNo = ""

    /**
     * 余额
     */
    var balance = 0

    /**
     * 消费
     */
    var amount = 0

    /**
     * 本次交易还可用余额
     */
    var availableBalance = 0

    /**
     * 对应的收款方式id
     */
    var paymentId = 0

    /**
     * 券号
     */
    var number = ""

    /**
     * 有效期
     */
    var validity = ""
}