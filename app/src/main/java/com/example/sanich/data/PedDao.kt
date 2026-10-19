package com.example.sanich.data

import androidx.room.Dao
import androidx.room.Query

@Dao
interface PedDao {


    @Query("""
        SELECT *
        FROM ped
        WHERE diameter = :diameter
        AND power = :power
        AND casing = :casing
        AND wall = :wall
        AND flow > 0
    """)
    suspend fun findPed(
        diameter: Int,
        power: Int,
        casing: Int,
        wall: Double
    ): List<PedEntity>


    @Query("SELECT COUNT(*) FROM ped")
    suspend fun getCount(): Int


    @Query("SELECT * FROM ped LIMIT 10")
    suspend fun testAll(): List<PedEntity>


    @Query("SELECT DISTINCT diameter FROM ped ORDER BY diameter")
    suspend fun getDiameters(): List<Int>


    @Query("""
        SELECT DISTINCT power
        FROM ped
        WHERE diameter = :diameter
        ORDER BY power
    """)
    suspend fun getPowers(
        diameter: Int
    ): List<Int>


    @Query("""
        SELECT DISTINCT casing
        FROM ped
        WHERE diameter = :diameter
        AND power = :power
        ORDER BY casing
    """)
    suspend fun getCasings(
        diameter: Int,
        power: Int
    ): List<Int>


    @Query("""
        SELECT DISTINCT wall
        FROM ped
        WHERE diameter = :diameter
        AND power = :power
        AND casing = :casing
        AND flow > 0
        ORDER BY wall
    """)
    suspend fun getWalls(
        diameter: Int,
        power: Int,
        casing: Int
    ): List<Double>


    @Query("""
        SELECT *
        FROM ped
        WHERE diameter = :diameter
        AND power = :power
        AND casing = :casing
        AND wall = :wall
        AND flow > 0
        ORDER BY flow DESC
    """)
    suspend fun getResultPeds(
        diameter: Int,
        power: Int,
        casing: Int,
        wall: Double
    ): List<PedEntity>

}