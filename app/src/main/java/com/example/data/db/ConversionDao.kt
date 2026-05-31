package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversionDao {
    @Query("SELECT * FROM conversion_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<ConversionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: ConversionRecord)

    @Query("DELETE FROM conversion_records WHERE id = :id")
    suspend fun deleteRecordById(id: Int)

    @Query("DELETE FROM conversion_records")
    suspend fun clearAllRecords()
}
