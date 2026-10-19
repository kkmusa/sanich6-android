package com.example.sanich.calculator

import kotlin.math.PI

object AnnularVolumeCalculator {
    fun casingInnerDiameter(
        casingOuterDiameterMm: Double,
        wallThicknessMm: Double
    ): Double {
        require(casingOuterDiameterMm > 0.0) { "Диаметр ЭК должен быть больше нуля" }
        require(wallThicknessMm > 0.0) { "Толщина стенки должна быть больше нуля" }
        val inner = casingOuterDiameterMm - 2.0 * wallThicknessMm
        require(inner > 0.0) { "Толщина стенки слишком большая" }
        return inner
    }

    fun volumePerMeter(
        tubingOuterDiameterMm: Double,
        casingOuterDiameterMm: Double,
        wallThicknessMm: Double
    ): Double {
        require(tubingOuterDiameterMm > 0.0) { "Диаметр НКТ должен быть больше нуля" }
        val casingInner = casingInnerDiameter(casingOuterDiameterMm, wallThicknessMm)
        require(casingInner > tubingOuterDiameterMm) {
            "Внутренний диаметр ЭК должен быть больше наружного диаметра НКТ"
        }
        return PI / 4.0 * (casingInner * casingInner - tubingOuterDiameterMm * tubingOuterDiameterMm) * 1e-6
    }
}
