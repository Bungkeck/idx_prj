package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val symbol: String,
    val name: String,
    val sector: String,
    val targetAlertHigh: Double = 0.0,
    val targetAlertLow: Double = 0.0,
    val notes: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_requests")
data class SavedRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val path: String,
    val method: String,
    val headersJson: String,
    val queryParamsJson: String,
    val bodyJson: String?,
    val savedAt: Long = System.currentTimeMillis()
)
