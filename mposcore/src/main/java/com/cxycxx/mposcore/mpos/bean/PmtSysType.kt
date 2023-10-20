package com.cxycxx.mposcore.mpos.bean

/**
 * 系统收款方式
 */
enum class PmtSysType(val typeName: String) {
    CASH("现金"),
    FAKE_CASH("伪现金"),
    BANK("银行卡"),
    AJ_COUPON("AJ优惠券"),
    WORTH_CARD("面值卡"),
    POINT("积分抵扣"),
    WEI_XIN("微信支付"),
    ALIPAY("支付宝"),
    DIRECTLY("直录1"),
    WEI_ZHONG("微众"),
    DEPOSIT("订金"),
    VOUCHER("代金券"),
    CASH_CARD("储值卡"),
    COUPON("卡券");

    companion object {
        fun fromTypeName(typeName: String): PmtSysType? {
            return values().firstOrNull { it.typeName == typeName }
        }
    }
}