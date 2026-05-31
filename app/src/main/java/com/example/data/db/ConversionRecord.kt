package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversion_records")
data class ConversionRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amountUSD: Double,
    val amountBDT: Double,
    val isUsdToBdt: Boolean,
    val rate: Double,
    val timestamp: Long = System.currentTimeMillis()
)
