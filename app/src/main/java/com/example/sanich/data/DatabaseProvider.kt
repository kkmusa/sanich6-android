package com.example.sanich.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "ped_database.db"
            )
                .createFromAsset("ped_database.db")
                .build()
            INSTANCE = instance
            instance
        }
    }
}
