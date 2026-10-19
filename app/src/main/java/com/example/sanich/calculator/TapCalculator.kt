package com.example.sanich.calculator

import kotlin.math.sqrt

enum class ControlStationMode { WITHOUT_VFD, WITH_VFD }
enum class CableMaterial(val resistivity: Double) { COPPER(0.017), ALUMINUM(0.026) }
enum class MotorType(val powerFactor: Double) { ASYNCHRONOUS(0.85), PERMANENT_MAGNET(0.95) }

data class TapResult(
    val temperatureCoefficient: Double,
    val voltageLoss: Double,
    val requiredVoltage: Double
)

object TapCalculator {
    fun calculate(
        mode: ControlStationMode,
        transformerNominalInputVoltage: Double,
        actualSupplyVoltage: Double,
        motorNominalVoltage: Double,
        motorNominalCurrent: Double,
        cableLengthMeters: Double,
        cableSectionMm2: Double,
        formationTemperatureC: Double,
        cableMaterial: CableMaterial,
        motorType: MotorType,
        vfdMaxOutputVoltage: Double? = null,
        maxFrequencyHz: Double? = null,
        motorNominalFrequencyHz: Double? = null,
        hasOutputFilter: Boolean = false
    ): TapResult {
        require(transformerNominalInputVoltage > 0) { "Номинальное входное напряжение ТМПН должно быть больше нуля" }
        require(actualSupplyVoltage > 0) { "Фактическое напряжение сети должно быть больше нуля" }
        require(motorNominalVoltage > 0) { "Номинальное напряжение ПЭД должно быть больше нуля" }
        require(motorNominalCurrent > 0) { "Номинальный ток ПЭД должен быть больше нуля" }
        require(cableLengthMeters > 0) { "Длина кабеля должна быть больше нуля" }
        require(cableSectionMm2 > 0) { "Сечение кабеля должно быть больше нуля" }

        val kt = 1.0 + 0.0043 * (formationTemperatureC - 20.0)
        require(kt > 0) { "Проверьте температуру пласта" }

        val voltageLoss = sqrt(3.0) * motorNominalCurrent * motorType.powerFactor *
            (cableLengthMeters / cableSectionMm2) * cableMaterial.resistivity * kt

        val base = when (mode) {
            ControlStationMode.WITHOUT_VFD ->
                transformerNominalInputVoltage / actualSupplyVoltage * motorNominalVoltage

            ControlStationMode.WITH_VFD -> {
                val maxOutput = requireNotNull(vfdMaxOutputVoltage) { "Укажите максимальное выходное напряжение ЧРП" }
                val maxFrequency = requireNotNull(maxFrequencyHz) { "Укажите максимальную частоту ЧРП" }
                val nominalFrequency = requireNotNull(motorNominalFrequencyHz) { "Укажите номинальную частоту ПЭД" }
                require(maxOutput > 0 && maxFrequency > 0 && nominalFrequency > 0) { "Параметры ЧРП должны быть больше нуля" }
                transformerNominalInputVoltage / maxOutput *
                    (maxFrequency / nominalFrequency) * motorNominalVoltage * (if (hasOutputFilter) 1.08 else 1.0)
            }
        }

        return TapResult(kt, voltageLoss, base + voltageLoss)
    }
}
