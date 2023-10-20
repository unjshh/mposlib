package com.cxycxx.mposcore.mpos.bean

import com.cxycxx.mposcore.util.Util
import com.google.gson.JsonObject

/**
 * 支付细节(一般只是用来记录使用)
 */
class PayRecord {
    /**
     * 交易主键
     */
    var transitionId = ""

    /**
     * 交易金额
     */
    var amount = 0

    /**
     * 总优惠金额
     */
    var discount = 0

    /**
     * 卖家优惠金额
     */
    var sellerDiscount = 0

    /**
     * 余额
     */
    var balance = 0

    /**
     * 交易时间
     */
    var tradeTime = ""

    /**
     * 内部交易号
     */
    var tradeNo = ""

    /**
     * 外部交易号
     */
    var outTradeNo = ""

    /**
     * 交易类型
     */
    var transType = "" //交易类型[如消费、撤销、退款...]

    /**
     * 买家ID
     */
    var buyerId = ""

    /**
     * 支付渠道
     */
    var payPass = ""

    /**
     * 卡号
     */
    var account = ""

    /**
     * 终端号
     */
    var terminalId = ""

    /**
     * 卡号
     */
    var cardNo = ""

    /**
     * 卡ID
     */
    var cardId = ""

    /**
     * 凭证号、券号
     */
    var voucherNo = ""

    /**
     * 参考号
     */
    var referenceNo = ""

    /**
     * 授权码
     */
    var authCode = ""

    /**
     * 批次号
     */
    var batchNo = ""

    /**
     * 发卡行代码
     */
    var iisCode = ""

    /**
     * 发卡行名称
     */
    var iisName = ""

    /**
     * 商户号
     */
    var merchantId = ""

    /**
     * 商户名称
     */
    var merchantName = ""

    /**
     * 备注
     */
    var remark = ""

    /**
     * 扩充属性
     */
    var extender: JsonObject? = null
        get() {
            if (field == null) field = JsonObject()
            return field
        }


    /**
     * 添加外来(扩充)的属性
     *
     * @param extender
     */
    fun addExtender(extender: JsonObject) {
        this.extender = Util.mergeJsonObject(this.extender, extender)
    }
}