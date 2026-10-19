package com.example.sanich.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val method: String,
    val result: String,
    val details: String,
    val createdAt: Long = System.currentTimeMillis()
)
