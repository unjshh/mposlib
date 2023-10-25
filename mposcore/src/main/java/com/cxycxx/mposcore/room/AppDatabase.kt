package com.cxycxx.mposcore.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cxycxx.mposcore.room.daos.TradeRecordDao
import com.cxycxx.mposcore.room.entities.TradeRecord

/**
 * 数据库
 */
@Database(entities = [TradeRecord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    /**
     * 交易记录
     */
    abstract fun tradeRecordDao(): TradeRecordDao
}