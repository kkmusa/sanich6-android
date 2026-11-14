package com.example.sanich.calculator

data class ForeignPumpResult(
    val barrelsPerDay: Double,
    val cubicMetersPerDay: Double
)

object ForeignPumpCalculator {
    private const val CUBIC_METERS_PER_BARREL = 0.158987294928

    /** Pump flow follows the affinity law Q2 = Q1 * f2 / f1. */
    fun calculate(rateBarrelsPerDay: Double, baseFrequency: Double, targetFrequency: Double): ForeignPumpResult {
        require(rateBarrelsPerDay.isFinite() && rateBarrelsPerDay > 0.0) {
            "Производительность должна быть больше нуля"
        }
        require(baseFrequency.isFinite() && baseFrequency > 0.0) {
            "Исходная частота должна быть больше нуля"
        }
        require(targetFrequency.isFinite() && targetFrequency > 0.0) {
            "Требуемая частота должна быть больше нуля"
        }
        val barrels = rateBarrelsPerDay * targetFrequency / baseFrequency
        return ForeignPumpResult(barrels, barrels * CUBIC_METERS_PER_BARREL)
    }
}
