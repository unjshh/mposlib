package com.cxycxx.mposcore.mpos.bean

import android.util.Pair
import com.cxycxx.mposcore.util.GsonHelper
import com.google.gson.JsonObject
import java.math.BigDecimal
import kotlin.math.max

/**
 * 小票
 */
class Ticket {
    /**
     * 一个商品所属的子票
     *
     * @param goods 商品
     * @return 子票
     */
    fun getGoodsSubticket(goods: Goods): Subticket? {
        return subtickets.firstOrNull { subTkt ->
            subTkt.goodses.firstOrNull { goods === it } != null
        }
    }

    /**
     * 查找商品索引
     *
     * @param goods
     * @return <子票索引，商品在子票中的索引>
     */
    fun findGoods(goods: Goods?): Pair<Int, Int> {
        if (goods == null) return Pair.create(-1, -1)
        var i = 0
        val s = subtickets.size
        while (i < s) {
            val goodsList = subtickets[i].goodses
            var j = 0
            val t = goodsList.size
            while (j < t) {
                if (goods === goodsList[j]) return Pair.create(i, j)
                j++
            }
            i++
        }
        return Pair.create(-1, -1)
    }

    fun findGoodsInAll(goods: Goods?): Int {
        if (goods == null) return -1
        var index = -1
        var i = 0
        val s = subtickets.size
        while (i < s) {
            val goodsList = subtickets[i].goodses
            var j = 0
            val t = goodsList.size
            while (j < t) {
                index++
                if (goods === goodsList[j]) return index
                j++
            }
            i++
        }
        return -1
    }

    /**
     * 所有商品
     *
     * @return
     */
    val allGoodses: List<Goods>
        get() = subtickets.flatMap { it.goodses }.toList()


    /**
     * 已经支付的收款方式
     *
     * @param pmtId 收款方式id
     * @return
     */
    fun getPayment(pmtId: Int): Payment? {
        return payments.firstOrNull { it.id == pmtId }
    }


    /**
     * 按显示类型收款方式列表
     */
    fun getPaymentsByShowType(showType: String): List<Payment> {
        return payments.filter { it.sysShowType == showType }.toList()
    }

    /**
     * 对应的收款金额
     */
    fun calcPaidAmountByShowType(showType: String): Int {
        val pmts = getPaymentsByShowType(showType)
        return pmts.sumOf { it.amount }
    }

    /**
     * 是否存在支付
     *
     * @param pmtId        收款方式id
     * @param transitionId 交易id
     * @return
     */
    fun hasPayRecord(pmtId: Int, transitionId: String): Boolean {
        return if (transitionId.isBlank()) false else payments
            .any { it.id == pmtId && it.hasPayRecord(transitionId) }
    }

    /**
     * 替换型同一类型
     *
     * @param replaceTypeId
     * @return
     */
    fun getPaidAmountByReplaceTypeId(replaceTypeId: Int): Int {
        val re = getPaymentByReplaceTypeId(replaceTypeId)
        return re?.amount ?: 0
    }

    /**
     * 已经支付的收款方式
     *
     * @param replaceTypeId 收款方式id
     * @return
     */
    fun getPaymentByReplaceTypeId(replaceTypeId: Int): Payment? {
        return payments.firstOrNull { it.replaceTypeId == replaceTypeId }
    }

    /**
     * 总销售额
     *
     * @return 小票的总销售额
     */
    val saleAmount: Int
        get() = subtickets.sumOf { it.saleAmount }

    /**
     * 总折扣
     *
     * @return 小票的总折扣
     */
    val discounts: Int
        get() = subtickets.sumOf { it.discounts }

    /**
     * 小票的预结算返回的总折扣
     *
     * @return 小票的预结算返回的总折扣
     */
    val calDiscount: Int
        get() = subtickets.sumOf { it.calDiscount }

    /**
     * 合计金额
     *
     * @return 总销售额 - 总折扣
     */
    val amount: Int
        get() {
            if (earnestMoney > 0) return earnestMoney
            val saleAmount = saleAmount
            val discounts = discounts
            return saleAmount - discounts
        }

    /**
     * 总退货金额
     *
     * @return 退货金额 - 折扣
     */
    val refund: Int
        get() = if (earnestMoney > 0) earnestMoney else returnMoney - returnDiscounts

    /**
     * 本子票退货金额
     *
     * @return 单价 x 退货数量
     */
    val returnMoney: Int
        get() = subtickets.sumOf { it.returnMoney }

    /**
     * 本子票退货总折扣
     *
     * @return
     */
    val returnDiscounts: Int
        get() = subtickets.sumOf { it.returnDiscounts }

    /**
     * 已付金额
     *
     * @return 已经支付的金额
     */
    val paidAmount
        get() = payments.sumOf { it.amount }

    /**
     * 应付金额
     *
     * @return 还需要支付的金额
     */
    val payableAmount: Int
        get() = (if (isRefund) refund else amount) - paidAmount

    /**
     * 找零金额
     *
     * @return
     */
    val change get() = max(paidAmount - amount, 0)

    /**
     * 是否是退货
     *
     * @return true--是退货，false--正常销售
     */
    val isRefund get() = oldTicket != null

    /**
     * 总销售数量
     *
     * @return
     */
    val saleCount: BigDecimal
        get() = allGoodses.sumOf { it.saleCount }

    /**
     * 总退货数量
     */
    val returnCount get() = allGoodses.sumOf { it.returnCount }

    /**
     * 商品条数
     */
    val goodsSize get() = allGoodses.size


    /**
     * 是否可以取消支付
     */
    val canCancelPay: Boolean
        get() = payments.none { !it.isCanCancel && it.amount != 0 }


    /**
     * 重置小票内容
     */
    fun reset() {
        tradeId = 0
        subtickets.clear()
        payments.clear()
        member = null
        crmBillId = 0
        earnestMoney = 0
        extender = null
        oldTicket = null
        hasRemindVip = false
    }

    /**
     * 从给定的小票复制
     */
    fun setWith(ticket: Ticket) {
        tradeId = ticket.tradeId
        deviceCode = ticket.deviceCode
        cashier = ticket.cashier
        subtickets.clear()
        subtickets.addAll(ticket.subtickets)
        payments.clear()
        payments.addAll(ticket.payments)
        member = ticket.member
        crmBillId = ticket.crmBillId
        tradeId = ticket.tradeId
        earnestMoney = ticket.earnestMoney
        extender = ticket.extender
        oldTicket = ticket.oldTicket
        hasRemindVip = ticket.hasRemindVip
    }

    /**
     * 转化成json
     *
     * @return
     */
    fun toJsonObject(): JsonObject {
        val rst = GsonHelper.fromObject(this)
        rst.add("calcPros", calcPros)
        return rst
    }

    /**
     * 计算字段
     */
    val calcPros
        get() = JsonObject().apply {
            addProperty("amount", amount)
            addProperty("calDiscount", calDiscount)
            addProperty("change", change)
            addProperty("discounts", discounts)
            addProperty("paidAmount", paidAmount)
            addProperty("payableAmount", payableAmount)
            addProperty("refund", refund)
            addProperty("saleCount", saleCount)
            addProperty("returnCount", returnCount)
            addProperty("returnMoney", returnMoney)
            addProperty("saleAmount", saleAmount)
            addProperty("isRefund", isRefund)
            addProperty("canCancelPay", canCancelPay)
        }

    /**
     * 小票号
     */
    var tradeId = 0

    /**
     * 款台号
     */
    var deviceCode = ""

    /**
     * 配送单号
     */
    var deliveryCode = ""

    /**
     * 收款员
     */
    var cashier: Staff? = null

    /**
     * 子票
     */
    val subtickets = mutableListOf<Subticket>()

    /**
     * 收款
     */
    val payments = mutableListOf<Payment>()

    /**
     * 会员
     */
    var member: Member? = null // 会员

    /**
     * CRM交易号
     */
    var crmBillId = 0

    /**
     * 是否已经预结算
     */
    val hasPreSettled get() = crmBillId > 0

    /**
     * 交易时间（只在退货时使用）
     *
     * @return
     */
    val settleTime = "" //结账时间【交易时间】

    /**
     * 定金金额
     */
    var earnestMoney = 0

    /**
     * 是否已提示刷会员
     */
    var hasRemindVip = false

    /**
     * 扩充属性
     */
    var extender: JsonObject? = null
        get() {
            if (field == null) field = JsonObject()
            return field
        }

    /**
     * 原交易小票
     */
    var oldTicket: Ticket? = null
}