package com.example.sanich.data

object Tables {

    private val vkpTable = mapOf(

        "139.7-60" to 0.0091,
        "139.7-73" to 0.0100,
        "139.7-89" to 0.0058,

        "146-60" to 0.0100,
        "146-73" to 0.0087,
        "146-89" to 0.0067,

        "168-60" to 0.0150,
        "168-73" to 0.0136,
        "168-89" to 0.0114,

        "178-60" to 0.0168,
        "178-73" to 0.0155,
        "178-89" to 0.0134
    )

    private val vnktTable = mapOf(
        "60" to 0.0020,
        "73" to 0.0030,
        "89" to 0.0045
    )

    fun getVkp(casing: String, tubing: String): Double {
        return vkpTable["$casing-$tubing"] ?: 0.0
    }

    fun getVnkt(tubing: String): Double {
        return vnktTable[tubing] ?: 0.0
    }
}