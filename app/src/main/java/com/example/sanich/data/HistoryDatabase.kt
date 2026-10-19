package com.example.sanich.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [HistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HistoryDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile private var INSTANCE: HistoryDatabase? = null

        fun getInstance(context: Context): HistoryDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    HistoryDatabase::class.java,
                    "calculation_history.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
