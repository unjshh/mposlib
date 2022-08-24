package com.cxycxx.mposcore.util

import java.lang.Exception
import java.math.BigDecimal

object NumHelper {
    fun toDecimal(x: Any?, dft: BigDecimal = BigDecimal.ZERO): BigDecimal {
        if (x == null) return dft
        return try {
            BigDecimal(x.toString().trim())
        } catch (e: Exception) {
            dft
        }
    }

    fun toInt(x: Any?, dft: Int = 0): Int {
        return toDecimal(x, BigDecimal(dft)).toInt()
    }
}