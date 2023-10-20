package com.cxycxx.mposcore.mpos.bean

import com.annimon.stream.Stream

/**
 * 子票
 */
class Subticket {
    /**
     * 本子票的总销售额
     */
    val saleAmount get() = goodses.sumOf { it.saleAmount }

    /**
     * 本子票的总折扣
     */
    val discounts get() = goodses.sumOf { it.discounts }

    /**
     * 本子票预结算返回的总折扣
     */
    val calDiscount get() = goodses.sumOf { it.calDiscount }


    /**
     * 获取本子票退货金额
     *
     * @return 单价 x 退货数量
     */
    val returnMoney get() = goodses.sumOf { it.returnMoney }

    /**
     * 本子票退货总折扣
     */
    val returnDiscounts get() = goodses.sumOf { it.returnDiscounts }

    /**
     * 营业员
     */
    var clerk: Staff? = null
        get() {
            if (field == null) field = Staff()
            return field
        }

    /**
     * 获取商品列表
     *
     * @return 子票中的商品列表
     */
    val goodses = mutableListOf<Goods>()
}