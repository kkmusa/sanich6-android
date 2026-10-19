package com.example.sanich.calculator

import kotlin.math.sqrt

object FrequencyCalculator {
    fun calculate(level: Double, head: Double): Double {
        require(level.isFinite()) { "Уровень должен быть конечным числом" }
        require(head.isFinite() && head > 0.0) { "Напор должен быть больше нуля" }
        val radicand = (40.0 * 9.81 + level) / head
        require(radicand >= 0.0) { "Подкоренное выражение не может быть отрицательным" }
        return 50.0 * sqrt(radicand)
    }
}
