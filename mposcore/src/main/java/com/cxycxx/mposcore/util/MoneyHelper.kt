package com.cxycxx.mposcore.util

import java.math.BigDecimal

object MoneyHelper {
    val HUNDRED = BigDecimal.valueOf(100)

    /**
     * 钱的转换
     *
     * @param money 以分为单位
     * @return 以元为单位，格式是0.00
     */
    @JvmStatic
    fun moneyI2S(money: Any?, zeroEmpty: Boolean = false): String {
        val fen = moneyI(money)
        if (zeroEmpty && fen == 0) return ""
        return moneyI2D(fen).toString()
    }

    @JvmStatic
    fun moneyI2S(money: Any?): String {
        return moneyI2S(money, false)
    }

    /**
     * 钱的转换
     *
     * @param money 以元为单位
     * @return 以分为单位
     */
    fun moneyD2I(money: Any?): Int {
        return moneyD(money).multiply(HUNDRED).toInt()
    }

    /**
     * 钱的转换
     *
     * @param money 以分为单位
     * @return 以元为单位
     */
    fun moneyI2D(money: Any?): BigDecimal {
        return NumHelper.toDecimal(money).divide(HUNDRED, 2, BigDecimal.ROUND_HALF_UP)
    }

    /**
     * 钱的转换
     *
     * @param money 以元为单位
     * @return 以元为单位
     */
    fun moneyD2S(money: Any?): String {
        return moneyD(money).toString()
    }

    /**
     * 单位：分
     */
    fun moneyI(money: Any?): Int {
        return NumHelper.toDecimal(money).toInt()
    }

    /**
     * 单位：元
     */
    fun moneyD(money: Any?): BigDecimal {
        return NumHelper.toDecimal(money).setScale(2, BigDecimal.ROUND_HALF_UP)
    }


}