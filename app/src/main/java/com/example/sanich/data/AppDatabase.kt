package com.example.sanich.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PedEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pedDao(): PedDao
}
