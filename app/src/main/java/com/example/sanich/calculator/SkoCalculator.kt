package com.example.sanich.calculator

enum class SkoVariant {
    DIRECT_WORKING,
    DIRECT_NO_FLOW,
    REVERSE_WORKING,
    REVERSE_PUMP_OFF
}

data class SkoResult(
    val treatmentVolumeM3: Double,
    val baseVolumeM3: Double,
    val additionalVolumeM3: Double
)

object SkoCalculator {
    fun calculate(
        variant: SkoVariant,
        pumpDepthMeters: Double,
        tubingLitersPerMeter: Double? = null,
        casingInnerDiameterMeters: Double? = null,
        tubingOuterDiameterMeters: Double? = null
    ): SkoResult {
        require(pumpDepthMeters > 0.0) { "Глубина должна быть больше нуля" }

        return when (variant) {
            SkoVariant.DIRECT_WORKING -> {
                val tubing = requireNotNull(tubingLitersPerMeter) { "Выберите НКТ" }
                val base = tubing * pumpDepthMeters / 1000.0
                SkoResult(base, base, 0.0)
            }
            SkoVariant.DIRECT_NO_FLOW -> {
                val tubing = requireNotNull(tubingLitersPerMeter) { "Выберите НКТ" }
                val base = tubing * pumpDepthMeters / 1000.0
                SkoResult(base + 8.0, base, 8.0)
            }
            SkoVariant.REVERSE_WORKING -> {
                val casing = requireNotNull(casingInnerDiameterMeters) { "Введите внутренний диаметр ЭК" }
                val tubing = requireNotNull(tubingOuterDiameterMeters) { "Введите наружный диаметр НКТ" }
                require(casing > tubing && tubing > 0.0) { "Диаметр ЭК должен быть больше диаметра НКТ" }
                val base = 0.79 * pumpDepthMeters * (casing * casing - tubing * tubing)
                SkoResult(base + 0.5, base, 0.5)
            }
            SkoVariant.REVERSE_PUMP_OFF -> {
                val casing = requireNotNull(casingInnerDiameterMeters) { "Введите внутренний диаметр ЭК" }
                require(casing > 0.0) { "Диаметр ЭК должен быть больше нуля" }
                val base = 0.79 * casing * casing * pumpDepthMeters
                SkoResult(base, base, 0.0)
            }
        }
    }
}
