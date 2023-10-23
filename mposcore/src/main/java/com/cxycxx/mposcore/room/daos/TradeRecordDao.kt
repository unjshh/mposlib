package com.cxycxx.mposcore.room.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cxycxx.room.entities.TradeRecord

@Dao
interface TradeRecordDao {
    @Insert
    fun insertAll(vararg records: TradeRecord)

    @Update
    fun updateRecords(vararg records: TradeRecord)

    @Query("SELECT * FROM TradeRecord WHERE requestId = :requestId")
    fun queryRecord(requestId: String): TradeRecord?

}