package com.cxycxx.mposcore.mpos

import com.cxycxx.mposcore.util.joAsInt
import com.cxycxx.mposcore.util.joAsJsonArray
import com.cxycxx.mposcore.util.joAsString


/**
 * MPOS的配置
 */
object MposConfig {
    val instance get() = MposPub.mposConfig
    val payCfg get() = instance.getAsJsonObject("pay")

    /**
     * 是否提示刷会员
     */
    val isRemindVip get() = instance.joAsInt("isRemindVip") == 1

    /**
     * 会员模式【比如是否打折】
     */
    val vipModel get() = instance.joAsString("vipModel")

    /**
     *ERP 类型
     */
    val erpType get() = instance.joAsString("erpType")

    /**
     *退货 类型
     */
    val returnModel get() = instance.joAsString("returnModel")

    //关于【银行支付】的配置
    val payBankCfg get() = payCfg.getAsJsonObject("bank")
    val payBankTaskClass get() = payBankCfg.joAsString("taskClass")
    val payBankType get() = payBankCfg.joAsString("type")
    val payBankMethodName get() = payBankCfg.joAsString("methodName")

    //关于【微信支付】的配置
    val payWeChatCfgs get() = payCfg.joAsJsonArray("weChat")
    val payWeChatCfg get() = payWeChatCfgs[0].asJsonObject
    val payWeChatTaskClass get() = payWeChatCfg.joAsString("taskClass")
    val payWeChatType get() = payWeChatCfg.joAsString("type")
    val payWeChatMethodName get() = payWeChatCfg.joAsString("methodName")

    //关于【支付宝】的配置
    val payAliPayCfgs get() = payCfg.joAsJsonArray("alipay")
    val payAliPayCfg get() = payAliPayCfgs[0].asJsonObject
    val payAliPayTaskClass get() = payAliPayCfg.joAsString("taskClass")
    val payAliPayType get() = payAliPayCfg.joAsString("type")
    val payAliPayMethodName get() = payAliPayCfg.joAsString("methodName")
}
