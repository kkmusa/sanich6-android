package com.example.sanich.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM calculation_history ORDER BY createdAt DESC, id DESC")
    fun observeAll(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM calculation_history ORDER BY createdAt DESC, id DESC LIMIT 1")
    suspend fun latest(): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: HistoryEntity): Long

    @Query("""
        UPDATE calculation_history
        SET title = :title, method = :method, result = :result, details = :details, createdAt = :createdAt
        WHERE id = :id
    """)
    suspend fun updateById(
        id: Long,
        title: String,
        method: String,
        result: String,
        details: String,
        createdAt: Long
    )

    @Delete
    suspend fun delete(entry: HistoryEntity)

    @Query("DELETE FROM calculation_history")
    suspend fun clear()
}
