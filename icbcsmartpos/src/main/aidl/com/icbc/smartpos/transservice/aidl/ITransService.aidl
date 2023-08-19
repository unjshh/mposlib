package com.icbc.smartpos.transservice.aidl;

import com.icbc.smartpos.transservice.aidl.TransHandler;

/**
 * 交易调用接口
 * @author: icbc
 */
interface ITransService {

      /**
       * 启动银行卡交易
       * @param transType - 交易类型
       * <ul>
       * <li>LOGIN(4001) -     签到</li>
       * <li>INQUERY(2002) -   查余额</li>
       * <li>PURCHASE(1001) -  消费</li>
       * <li>POS_VOID(1101) -  撤销</li>
       * <li>REFUND(1102) -    退货</li>
       * </ul>
       *
       * @param transData - 交易数据
       * <ul>
       * <li>AMOUNT(long)：交易金额</li>
       * <li>OLD_TRANS_DATE(long)：原交易日期(退货交易使用)</li>
       * </ul>
       *
       * @param handler - 交易回调处理器
       */
       long startTrans(String transType, in Bundle ctrlData,
                        in Bundle transData, in TransHandler handler);

}