package com.example.sanich.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "ped")
data class PedEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int,

    val manufacturer: String,

    val name: String,

    val diameter: Int,

    val power: Int,

    val speed: Double,

    val casing: Int,

    val wall: Double,

    val flow: Double

)