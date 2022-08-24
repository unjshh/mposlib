package com.cxycxx.mposcore.util

import android.graphics.Color
import java.lang.Exception

object ColorHelper {
    @JvmStatic
    fun parseColor(rgb: Any?): Int {
        return try {
            Color.parseColor(rgb?.toString())
        } catch (e: Exception) {
            Color.BLACK
        }
    }
}