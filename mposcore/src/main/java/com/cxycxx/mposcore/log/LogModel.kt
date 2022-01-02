package com.cxycxx.mposcore.log

import java.text.SimpleDateFormat
import java.util.*

class LogModel {
    var DeviceType = ""//设备类型
    var DeviceCode = ""//设备号
    var Operator = ""//操作号
    var Interface = ""//接口名称
    var PostData = ""//发送数据
    var Response = ""//回应数据
    var StartTime = DATE_FORMAT_DEFAULT.format(Calendar.getInstance().time)
    var AllTime = ""//总耗时
    var State = ""//执行结果
    var TimeToLive = ""//过期时间
    var Key = ""//关键字
    var ClientIP = ""
    var WriterIP = ""
    var ServiceURL = ""//接口地址

    companion object {
        val DATE_FORMAT_DEFAULT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    }
}