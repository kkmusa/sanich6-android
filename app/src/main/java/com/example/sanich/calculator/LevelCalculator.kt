package com.example.sanich.calculator

import kotlin.math.round

object LevelCalculator {
    fun calculate(depth: Double, pz: Double, pd: Double, density: Double): Double {
        require(depth.isFinite() && pz.isFinite() && pd.isFinite()) {
            "Входные значения должны быть конечными числами"
        }
        require(density.isFinite() && density > 0.0) { "Плотность должна быть больше нуля" }
        val densityGcm3 = density
        val level = depth - ((pd - pz) * 10.0) / densityGcm3
        return round(level * 100) / 100
    }
}
