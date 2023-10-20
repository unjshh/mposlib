package com.cxycxx.mposcore.mpos.bean

import com.cxycxx.mposcore.mpos.MposConfig
import com.cxycxx.mposcore.util.Util
import com.cxycxx.mposcore.util.joAsInt
import com.cxycxx.mposcore.util.joAsJsonArray
import com.google.gson.JsonObject
import java.util.*


/* 属性：1、操作方式[a.调用第三方的处理类及设备驱动]；2、显示分类*/
/**
 * 收款方式
 */
open class BasePayment {
    /**
     * 收款方式ID
     */
    var id = 0

    /**
     * 设置收款方式代码
     */
    var code = ""

    /**
     * 设置收款方式名称
     */
    var name = ""

    /**
     * 设置收款方式类型id
     */
    var typeId = 0
    val replaceTypeId = 0 //同种类型的收款存在则替换

    /**
     * 设置收款方式类型代码
     */
    var typeCode = ""

    /**
     * 设置收款方式类型名称
     */
    var typeName = ""

    /**
     * 设置客户端自己用的收款方式（操作）类型
     */
    var sysType = "" //操作类型
    val enumSysType get() = PmtSysType.fromTypeName(sysType)

    /**
     * GetMposConfigs 中的标签
     */
    var sysTag = ""

    /**
     * 设置系统显示类型
     */
    var sysShowType = "" //显示类型
    val imgUrl = "" //图片地址

    /**
     * 设置余额
     */
    var balance = 0

    /**
     * 设置本次可用金额
     *
     */
    var availableBalance = 0
    /**
     * 获取支付金额
     *
     * @return 支付金额
     */
    /**
     * 设置支付金额
     *
     * @param amount 支付金额
     */
    var amount = 0

    /**
     * 调用方式
     *
     * @return 调用方式
     */
    val callModel = 0 //调用方式[0是按普通模式；1是不用先输入金额]

    /**
     * 是否可以取消
     *
     * @return
     */
    val isCanCancel = true //是否可以取消【产生支付后是否可以退出付款界面，退出会清空付款列表，等于取消】
    val isCanModify = false //是否可以修改


    var extender = JsonObject()


    /**
     * 添加外来(扩充)的属性
     *
     * @param extender
     */
    fun addExtender(extender: JsonObject) {
        this.extender = Util.mergeJsonObject(this.extender, extender)
    }

    /**
     * 是否有【自动退款】标识
     */
    fun hasAutoRefundFlag(): Boolean {
        return extender.joAsInt("autoRefund") == 1
    }

    /**
     * 删除【自动退款】标识
     */
    fun removeAutoRefundFlag() {
        extender.remove("autoRefund")
    }

    val showName: String
        get() = if (!extender.has("pmtShowName")) name else extender["pmtShowName"].asString
    val config: JsonObject?
        get() {
            val payCfg = MposConfig.payCfg
            if (sysTag.isNotBlank()) {
                return payCfg.getAsJsonObject(sysTag)
            }
            if (sysType == "银行卡") {
                return MposConfig.payBankCfg
            }
            if (sysType == "微信支付") {
                return payCfg.joAsJsonArray("weChat")[0].asJsonObject
            }
            if (sysType == "支付宝") {
                return payCfg.joAsJsonArray("alipay")[0].asJsonObject
            }
            return null
        }
}

/**
 * 收款
 */
class Payment : BasePayment() {
    var payRecords = arrayListOf<PayRecord>() //交易记录(一般不要修改内容，可以查询)
        private set

    /**
     * 是否存在支付
     *
     * @param transitionId 交易id
     * @return
     */
    fun hasPayRecord(transitionId: String): Boolean {
        return if (Util.isEmpty(transitionId)) false else payRecords
            .any { p -> transitionId == p.transitionId }
    }

    /**
     * 移除支付记录
     * @param transitionId 交易id
     */
    fun removePayRecord(transitionId: String): PayRecord? {
        if (transitionId.isBlank()) return null
        val pr = payRecords.firstOrNull { it.transitionId == transitionId } ?: return null
        return if (payRecords.remove(pr)) pr else null
    }
}


/**
 * 明确的收款方式
 */
open class ExplicitPayment<T : Any> : BasePayment() {
    lateinit var explicit: T


}

/**
 * 一次支付
 */
class OncePayment<T : Any> : ExplicitPayment<T>() {
    /**
     * 支付记录
     */
    lateinit var payRecord: PayRecord
}

typealias ExplicitPmt = ExplicitPayment<*>
