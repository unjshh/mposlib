package com.cxycxx.mposcore.mpos.bean

import com.google.gson.JsonObject
import java.io.Serializable
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.sign

/**
 * 商品
 */
class Goods : Serializable {

    /**
     * Id
     */
    var id = 0

    /**
     * 代码
     */
    var code = "" //商品代码

    /**
     * 条形码
     */
    var barCode = "" //商品条形码

    /**
     * 名称
     */
    var name = "" //商品名称

    /**
     * 种类
     */
    var classCode = "" //商品类型代码

    /**
     * 单价
     */
    var price = 0 //单价

    /**
     * 最低单价
     */
    var limitedPrice = 0 //可销最低价

    /**
     * 数量
     */
    var saleCount = BigDecimal.ZERO //销售数量

    /**
     * 退货数量
     */
    var returnCount = BigDecimal.ZERO //退货数量

    /**
     * 优惠金额
     */
    var preferentialMoney = 0 //优惠金额

    /**
     * 前台折扣
     */
    var frontDiscount = 0 //前台折扣

    /**
     * 设备后台折扣
     */
    var perBackDiscount = 0 //后台折扣(对单个商品而言)

    /**
     * 会员折扣
     */
    var perVipDiscount = 0 //会员折扣(对单个商品而言)

    /**
     * 会员折扣（会在预提交的时候直接用服务端返回的值替换）
     */
    var calVipDiscount = 0
        get() {
            if (field != 0) return field
            return if (perVipDiscount != 0) perVipDiscount.toBigDecimal()
                .multiply(saleCount)
                .toInt()
            else BigDecimal.valueOf((saleAmount - frontDiscount - calBackDiscount).toLong())
                .multiply(
                    BigDecimal.ONE.subtract(vipDiscountRate)
                ).toInt()
        }

    /**
     * 后台折扣
     */
    var calBackDiscount = 0 //后台折扣（会在预提交的时候直接用服务端返回的值替换）

    /**
     * 预结算返回的总折扣
     */
    @JvmField
    var calDiscount = 0 //总折扣（会在预提交的时候直接用服务端返回的值替换）

    /**
     * 省零折扣
     */
    var calChangeDiscount = 0 //省零折扣

    /**
     * 满百折扣
     */
    var calDecreaseDiscount = 0 //满百减折扣

    /**
     * 前台折扣率
     */
    var frontDiscountRate = BigDecimal.ZERO //前台折扣率

    /**
     * 后台折扣率
     */
    var backDiscountRate = BigDecimal.ZERO //后台折扣率

    /**
     * 会员折扣率
     */
    var vipDiscountRate = BigDecimal.ZERO //会员折扣率

    /**
     * 是否可以编辑单价
     */
    var isCanEditPrice = false //是否可以编辑单价

    /**
     * 是否可以编辑金额
     */
    var isCanEditAmount = false //是否可以编辑金额

    /**
     * 总折扣
     */
    val discounts: Int
        get() = if (calDiscount != 0) calDiscount else frontDiscount + calBackDiscount + calChangeDiscount + calDecreaseDiscount + calVipDiscount

    /**
     * 销售金额
     *
     * @return 单价 x 销售数量
     */
    val saleAmount get() = price.toBigDecimal().multiply(saleCount).toInt()

    /**
     * 总金额
     *
     * @return 销售金额 - 总折扣
     */
    val amount: Int
        get() {
            val saleAmount = saleAmount
            val dis = discounts
            if (saleCount.signum() < 0) { //负销售
                val temp = abs(saleAmount) - abs(dis)
                val sign =
                    if (temp > 0) sign(saleAmount.toFloat()) else sign(dis.toFloat())
                return (sign * temp).toInt()
            }
            return saleAmount - dis
        }

    /**
     * 退货金额【单价 x 退货数量】
     */
    val returnMoney get() = price.toBigDecimal().multiply(returnCount).toInt()

    /**
     * 总退货金额（退货金额 - 折扣）
     */
    val refund get() = returnMoney - returnDiscounts

    /**
     * 退货总折扣
     */
    val returnDiscounts: Int
        get() = if (saleCount == returnCount) discounts else discounts.toBigDecimal()
            .divide(saleCount, 2, BigDecimal.ROUND_HALF_UP).multiply(returnCount).toInt()

    /**
     * 扩充属性
     */
    var extender: JsonObject? = null
        get() {
            if (field == null) field = JsonObject()
            return field
        }

    /**
     * 清空预提交时返回的折扣
     */
    fun clearCalDiscount() {
        calBackDiscount = 0
        calDecreaseDiscount = 0
        calVipDiscount = 0
        calDiscount = 0
        calChangeDiscount = 0
    }


}