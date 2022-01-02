package com.cxycxx.mposcore.custom

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class FontIconView : AppCompatTextView {
    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context)
    }

    /**
     * 初始化
     * @param context
     */
    private fun init(context: Context) {
        //设置字体图标
        val font = Typeface.createFromAsset(context.assets, "iconfont.ttf")
        this.typeface = font
    }
}