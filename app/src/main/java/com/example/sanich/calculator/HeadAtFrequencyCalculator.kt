package com.example.sanich.calculator

object HeadAtFrequencyCalculator {
    /** Passport head is specified at 50 Hz; H = H50 * (f / 50)^2. */
    fun calculate(headAt50: Double, frequency: Double): Double {
        require(headAt50.isFinite() && headAt50 > 0.0) { "Напор при 50 Гц должен быть больше нуля" }
        require(frequency.isFinite() && frequency >= 0.0) { "Частота должна быть неотрицательной" }
        val ratio = frequency / 50.0
        val result = headAt50 * (ratio * ratio)
        require(result.isFinite()) { "Результат выходит за числовой диапазон" }
        return result
    }
}
