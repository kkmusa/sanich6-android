package com.example.sanich.calculator

import kotlin.math.ceil

data class UprResult(
    val productiveRunMinutes: Double,
    val totalRunMinutes: Double,
    val stopMinutes: Double
)

data class ApvResult(
    val productiveRunMinutes: Double,
    val totalRunMinutes: Double,
    val requiredCycleBeforeRoundingMinutes: Double,
    val minimumRequiredCycleMinutes: Int,
    val stopMinutes: Double,
    val cycleIsSuitable: Boolean
)

object OperatingModeCalculator {
    fun toUpr(
        pumpRateM3Day: Double,
        wellPotentialM3Day: Double,
        supplyAppearanceMinutes: Double,
        selectedCycleMinutes: Double
    ): UprResult {
        validateCommon(pumpRateM3Day, wellPotentialM3Day, supplyAppearanceMinutes, selectedCycleMinutes)
        val productive = selectedCycleMinutes * wellPotentialM3Day / pumpRateM3Day
        val totalRun = productive + supplyAppearanceMinutes
        require(totalRun < selectedCycleMinutes) { "Выбранный цикл слишком короткий. Увеличьте продолжительность цикла" }
        return UprResult(productive, totalRun, selectedCycleMinutes - totalRun)
    }

    fun toApv(
        pumpRateM3Day: Double,
        wellPotentialM3Day: Double,
        supplyAppearanceMinutes: Double,
        drainMinutes: Double,
        selectedCycleMinutes: Double
    ): ApvResult {
        validateCommon(pumpRateM3Day, wellPotentialM3Day, supplyAppearanceMinutes, selectedCycleMinutes)
        require(drainMinutes >= 0) { "Время слива не может быть отрицательным" }
        require(wellPotentialM3Day < pumpRateM3Day) {
            "Для режима АПВ потенциал должен быть меньше дебита УЭЦН"
        }

        // Режим для выбранного пользователем цикла.
        val productive = selectedCycleMinutes * wellPotentialM3Day / pumpRateM3Day
        val totalRun = productive + supplyAppearanceMinutes
        val stop = selectedCycleMinutes - totalRun

        // Минимальный цикл не зависит от выбранного цикла.
        // Требование: T - (T * P / Q + tп) >= tс
        // Отсюда: T >= (tп + tс) / (1 - P / Q)
        val requiredBeforeRounding =
            (supplyAppearanceMinutes + drainMinutes) /
                (1.0 - wellPotentialM3Day / pumpRateM3Day)

        // По принятому правилу минимальный цикл кратен 60 минутам.
        val minimumCycle = ceil(requiredBeforeRounding / 60.0).toInt() * 60

        return ApvResult(
            productiveRunMinutes = productive,
            totalRunMinutes = totalRun,
            requiredCycleBeforeRoundingMinutes = requiredBeforeRounding,
            minimumRequiredCycleMinutes = minimumCycle,
            stopMinutes = stop,
            cycleIsSuitable = selectedCycleMinutes >= minimumCycle && stop >= drainMinutes
        )
    }

    private fun validateCommon(rate: Double, potential: Double, supply: Double, cycle: Double) {
        require(rate > 0) { "Дебит УЭЦН должен быть больше нуля" }
        require(potential >= 0) { "Потенциал не может быть отрицательным" }
        require(supply >= 0) { "Время появления подачи не может быть отрицательным" }
        require(cycle > 0) { "Цикл должен быть больше нуля" }
    }
}
