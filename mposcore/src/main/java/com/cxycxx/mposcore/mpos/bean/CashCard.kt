package com.cxycxx.mposcore.mpos.bean

/**
 * 储值卡
 */
class CashCard {
    /**
     * 储值卡id
     */
    var id = 0

    /**
     * 储值卡卡号
     */
    var code = ""

    /**
     * 储值卡类型
     */
    var typeId = 0

    /**
     * 储值卡名称
     */
    var name = ""

    /**
     * 展示名称
     */
    var showName = ""
        get() = field.ifBlank { name }

    /**
     * 余额
     */
    var balance = 0

    /**
     * 消费
     */
    var amount = 0

    /**
     * 对应的收款方式id
     */
    var paymentId = 0
}