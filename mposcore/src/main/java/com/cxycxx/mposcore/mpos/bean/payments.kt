package com.cxycxx.mposcore.mpos.bean

/**
 * 储值卡收款方式
 */
class CashCardPmt : ExplicitPayment<CashCard>() {
    val cardId get() = explicit.id
    val cardNo get() = explicit.code
}
/**
 * 卡券收款方式
 */
typealias CouponPmt = ExplicitPayment<Coupon>