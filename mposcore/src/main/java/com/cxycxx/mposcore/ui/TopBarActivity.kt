package com.cxycxx.mposcore.ui

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.cxycxx.mposcore.R
import com.cxycxx.mposcore.BR
import com.cxycxx.mposcore.custom.OnRvItemClickListener
import com.cxycxx.mposcore.util.GSON
import com.cxycxx.mposcore.util.toMap
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.util.HashMap

open class TopBarActivity : AppCompatActivity(), OnRvItemClickListener, View.OnClickListener , CoroutineScope by MainScope() {
    val DETAIL = "detail"
    protected var binding: ViewDataBinding? = null
    protected val detailMap: MutableMap<String, Any> = HashMap()
    protected val controlMap = mutableMapOf<String, Any>()
    protected val dataMap = mutableMapOf<String, Any>()
    protected var detailData: JsonObject? = null

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setAndroidNativeLightStatusBar(true)
        hideTopRightIcon()
        immergeBar()
    }

    /**
     * 绑定布局
     *
     * @param layoutId 布局
     */
    protected fun bindContentView(layoutId: Int) {
        binding = DataBindingUtil.setContentView(this, layoutId)
        detailData = GSON.fromJson(intent.getStringExtra(DETAIL), JsonObject::class.java)
        if (detailData != null) {
            detailMap.clear()
            detailData?.toMap()?.let { detailMap.putAll(it) }
            rebindDetail()
        }
        setAndroidNativeLightStatusBar(true)
    }

    /**
     * 隐藏输入法
     */
    fun hideSoftInputFromWindow() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            ?: return
        val view = currentFocus ?: return
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * 显示顶栏左侧图标
     */
    fun showTopLeftIcon() {
        val fivRight = findViewById<View>(R.id.fivTopLeft)
        fivRight?.visibility = View.VISIBLE //默认不显示右侧图标
    }

    /**
     * 设置顶栏左侧图标颜色
     *
     * @param color 颜色
     */
    fun setTopLeftIconColor(color: Int) {
        val fivLeft = findViewById<TextView>(R.id.fivTopLeft)
        fivLeft?.setTextColor(color)
    }

    /**
     * 隐藏顶栏左侧图标
     */
    fun hideTopLeftIcon() {
        val fivRight = findViewById<View>(R.id.fivTopLeft)
        fivRight?.visibility = View.GONE
    }

    /**
     * 显示顶栏右侧图标
     */
    fun showTopRightIcon() {
        val fivRight = findViewById<View>(R.id.fivTopRight)
        fivRight?.visibility = View.VISIBLE //默认不显示右侧图标
    }

    /**
     * 隐藏顶栏右侧图标
     */
    fun hideTopRightIcon() {
        val fivRight = findViewById<View>(R.id.fivTopRight)
        fivRight?.visibility = View.GONE
    }

    /**
     * 显示分割行
     */
    fun showSpitLine() {
        val vSpitLine = findViewById<View>(R.id.vSpitLine)
        vSpitLine?.visibility = View.VISIBLE
    }

    /**
     * 隐藏分割行
     */
    fun hideSpitLine() {
        val vSpitLine = findViewById<View>(R.id.vSpitLine)
        vSpitLine?.visibility = View.GONE
    }

    /**
     * 显示分割间隔
     */
    fun showSpitGap() {
        val vSpitLine = findViewById<View>(R.id.vSpitGap)
        vSpitLine?.visibility = View.VISIBLE
    }

    /**
     * 隐藏分割间隔
     */
    fun hideSpitGap() {
        val vSpitLine = findViewById<View>(R.id.vSpitGap)
        vSpitLine?.visibility = View.GONE
    }

    /**
     * 设置标题
     *
     * @param title 标题
     */
    protected fun setTopBarTitle(title: CharSequence?) {
        val tvTopTitle = findViewById<View>(R.id.tvTopTitle)
        if (tvTopTitle != null && tvTopTitle is TextView) {
            tvTopTitle.paint.isFakeBoldText = true //粗体
            tvTopTitle.text = title
        }
    }

    protected fun getTopBarTitle(): String {
        val tvTopTitle = findViewById<View>(R.id.tvTopTitle)
        if (tvTopTitle != null && tvTopTitle is TextView) {
            return tvTopTitle.text.toString();
        }
        return ""
    }

    /**
     * 设置顶栏标题颜色
     *
     * @param color 颜色
     */
    fun setTopBarTitleColor(color: Int) {
        val tvTopTitle = findViewById<TextView>(R.id.tvTopTitle)
        tvTopTitle?.setTextColor(color)
    }

    /**
     * 浸入状态栏
     */
    protected fun immergeBar() {
        val vBar = findViewById<TextView>(R.id.tvBar)
        vBar?.let { immergeBar(it) }
    }

    /**
     * 浸入式状态栏
     *
     * @param vBar
     */
    protected fun immergeBar(vBar: TextView?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (vBar != null) vBar.height = getStatusBarHeight(this)
            val window = window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            if (vBar != null) vBar.height = getStatusBarHeight(this)
        }
        vBar?.clearFocus()
    }

    /**
     * 获取状态栏高度
     */
    private fun getStatusBarHeight(context: Context): Int {
        // 获得状态栏高度
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return context.resources.getDimensionPixelSize(resourceId)
    }

    /**
     * 设置状态栏背景颜色(这儿也会替换标题和左边图标的颜色)【针对浸入状态栏的情况】
     *
     * @param color 背景颜色
     * @param dark  字体颜色
     */
    protected fun setBarBackgroundColor(color: Int, dark: Boolean) {
        val vBar = findViewById<View>(R.id.vBar)
        if (vBar != null) {
            vBar.setBackgroundColor(color)
            setAndroidNativeLightStatusBar(dark)
        }
        val black = Color.parseColor("#12161C");
        setTopLeftIconColor(if (dark) black else Color.WHITE)
        setTopBarTitleColor(if (dark) black else Color.WHITE)
    }

    protected fun getThisActivity(): Activity? {
        return this@TopBarActivity
    }

    /**
     * 设置状态栏字体颜色
     *
     * @param dark 是否深色
     */
    protected fun setAndroidNativeLightStatusBar(dark: Boolean) {
        val decor = window.decorView
        if (dark) {
            decor.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            decor.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    /**
     * 设置状态栏背景颜色【针对未浸入状态栏的情况】
     *
     * @param color 颜色
     */
    protected fun setBarBackgroundColor(color: Int) {
        val window = window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                        or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            )
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            // 设置状态栏颜色
            window.statusBarColor = color
            // 设置导航栏颜色
            //window.setNavigationBarColor(color);
            val contentView = findViewById<View>(R.id.content)
            if (contentView is ViewGroup) {
                val childAt = contentView.getChildAt(0)
                if (childAt != null) {
                    childAt.fitsSystemWindows = true
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 透明状态栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            // 透明导航栏
            //window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            // 设置contentView为fitsSystemWindows
            val contentView = findViewById<View>(R.id.content)
            if (contentView is ViewGroup) {
                val childAt = contentView.getChildAt(0)
                if (childAt != null) {
                    childAt.fitsSystemWindows = true
                }

                // 给statusBar着色
                val view = View(this)
                view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    getStatusBarHeight(this)
                )
                view.setBackgroundColor(color)
                contentView.addView(view)
            }
        }
    }

    /**
     * 设置导航栏颜色
     *
     * @param color
     */
    fun setNavigationBarColor(color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = color
        }
    }

    /**
     * 获取资源颜色
     *
     * @param resId
     * @return
     */
    fun getResColor(resId: Int): Int {
        return resources.getColor(resId)
    }

    /**
     * 获取资源大小
     *
     * @param resId
     * @return
     */
    fun getResDimension(resId: Int): Int {
        return resources.getDimensionPixelSize(resId)
    }

    /**
     * 重新绑定详情
     */
    protected fun rebindDetail() {
        binding?.setVariable(BR.detailMap, detailMap)
    }

    /**
     * 重新绑定数据
     */
    protected fun rebindData() {
        binding?.setVariable(BR.dataMap, dataMap)
    }

    /**
     * 重新绑定控制
     */
    protected fun rebindControl() {
        binding?.setVariable(BR.controlMap, controlMap)
    }

    fun toastMsg(msg: String?) {
        if (TextUtils.isEmpty(msg)) return
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onItemClick(view: View?, item: Any?, type: Int): Boolean {
        return true
    }

    override fun onClick(v: View?) {
        if (v == null) return
        if (v.id == R.id.fivTopLeft) finish()
    }
}