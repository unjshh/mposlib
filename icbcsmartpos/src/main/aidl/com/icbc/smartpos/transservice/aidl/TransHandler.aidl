package com.icbc.smartpos.transservice.aidl;

/**
 * 交易流程回调接口
 * @author: icbc
 */
interface TransHandler {

    /**
    * 交易结束
    * @param transResult - 交易结果数据
    * <ul>
    * <li>RESULT(int) - 结果类型：(0) - 成功; (1) - 失败; (-1) - 异常</li>
    * <li>DESCRIPTION(String) - 交易结果/失败信息描述 </li>
    * <li>RSP_NO(String) -      主机返回码 </li>
    * <li>TRACE(String) -       终端流水号 </li>
    * <li>REF_NO(String) -      交易参考号 </li>
    * <li>PAN(String) -         交易卡号 </li>
    * <li>DATETIME(String) -    日期时间 </li>
    * <li>AUTH_ID(String) -     授权码 </li>
    * <li>AMOUNT(String) -      交易金额 </li>
    * <li>RET_AMT(String) -     清算金额 </li>
    * </ul>
    */
    void onFinish(in Bundle baseResult,
                     in Bundle transResult,
                     in Bundle extraInfo);

}