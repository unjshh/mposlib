package com.cxycxx.mposcore.mpos

import com.cxycxx.mposcore.util.joAsInt
import com.cxycxx.mposcore.util.joAsString


/**
 * MPOS的配置
 */
object MposConfig {
    private val cfg get() = MposPub.mposConfig
    val payCfg get() = cfg.getAsJsonObject("pay")

    /**
     * 是否提示刷会员
     */
    val isRemindVip get() = cfg.joAsInt("isRemindVip") == 1

    /**
     * 会员模式【比如是否打折】
     */
    val vipModel get() = cfg.joAsString("vipModel")

    /**
     *ERP 类型
     */
    val erpType get() = cfg.joAsString("erpType")

    //关于【银行支付】的配置
    val payBankCfg get() = payCfg.getAsJsonObject("bank")
    val payBankTaskClass get() = payBankCfg.joAsString("taskClass")
    val payBankType get() = payBankCfg.joAsString("type")
    val payBankMethodName get() = payBankCfg.joAsString("methodName")

}
