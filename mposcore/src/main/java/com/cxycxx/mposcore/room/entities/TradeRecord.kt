package com.cxycxx.mposcore.room.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 交易记录
 */
@Entity(
    indices = [Index(value = ["requestId"]), Index(value = ["deviceCode", "ticketId"]), Index(value = ["status"]), Index(
        value = ["requestTime"]
    )]
)
class TradeRecord {
    companion object {
        /**
         * 状态之默认
         */
        const val STATUS_DEFAULT = 0

        /**
         * 状态之失败
         */
        const val STATUS_FAIL = -1

        /**
         * 状态之成功
         */
        const val STATUS_SUCCESS = 1

        /**
         * 状态之未知
         */
        const val STATUS_UNKNOWN = 2
    }

    @PrimaryKey(autoGenerate = true)
    var rowid = 0

    /**
     * 请求的唯一标识
     */
    var requestId = ""

    /**
     * 关联的款台号
     */
    var deviceCode=""

    /**
     * 关联的小票号
     */
    var ticketId = ""

    /**
     * 状态(0-默认；-1-失败；1-成功；2-未知)
     */
    var status = 0

    /**
     * 请求时间(毫秒时间戳)
     */
    var requestTime = System.currentTimeMillis()

    /**
     * 回应时间(毫秒时间戳)
     */
    var responseTime = 0L

    /**
     * 请求数据
     */
    var request = ""

    /**
     * 回应数据
     */
    var response = ""

    /**
     * 交易类型
     */
    var transType = ""


}