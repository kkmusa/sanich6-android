package com.example.sanich.calculator

import kotlin.math.round

object FlowCalculator {


    fun hermetic(
        deltaH: Double,
        vkp: Double,
        time: Double
    ): Double {
        validateCommon(deltaH, vkp, time)
        return round(deltaH * vkp * 1440 / time * 100) / 100

    }


    fun drain(
        deltaH: Double,
        vkp: Double,
        vnkt: Double,
        hst: Double,
        time: Double
    ): Double {
        validateCommon(deltaH, vkp, time)
        require(vnkt.isFinite() && vnkt >= 0.0) { "Объём НКТ не может быть отрицательным" }
        require(hst.isFinite() && hst >= 0.0) { "Высота столба не может быть отрицательной" }
        return round(
            ((deltaH * vkp) - (vnkt * hst)) * 1440 / time * 100
        ) / 100

    }


    fun afterDrain(
        deltaH: Double,
        vkp: Double,
        vnkt: Double,
        time: Double
    ): Double {
        validateCommon(deltaH, vkp, time)
        require(vnkt.isFinite() && vnkt >= 0.0) { "Объём НКТ не может быть отрицательным" }
        return round(
            deltaH * (vkp + vnkt) * 1440 / time * 100
        ) / 100

    }


    // НОВЫЙ РАСЧЕТ ПО ДАТЧИКАМ
    fun sensorHermetic(
        depth: Double,
        sensorPressure1: Double,
        sensorPressure2: Double,
        annulusPressure: Double,
        density: Double,
        casing: String,
        tubing: String,
        vkp: Double,
        time: Double
    ): Double {


        require(depth.isFinite()) { "Глубина должна быть конечным числом" }
        require(density.isFinite() && density > 0.0) { "Плотность должна быть больше нуля" }
        require(sensorPressure1.isFinite() && sensorPressure2.isFinite() && annulusPressure.isFinite()) {
            "Давления должны быть конечными числами"
        }
        require(casing.isNotBlank() && tubing.isNotBlank()) { "Типоразмеры колонн должны быть выбраны" }

        // уровень по давлению приема

        val level1 =
            depth - ((sensorPressure1 - annulusPressure) * 10 / density)




        // уровень по второму датчику давления

        val level2 =
            depth - ((sensorPressure2 - annulusPressure) * 10 / density)


        // разница уровней

        val deltaH = kotlin.math.abs(level1 - level2)

        // обычный расчет герметичного ОК

        return hermetic(
            deltaH = deltaH,
            vkp = vkp,
            time = time
        )

    }


    private fun validateCommon(deltaH: Double, vkp: Double, time: Double) {
        require(deltaH.isFinite() && deltaH >= 0.0) { "Изменение уровня не может быть отрицательным" }
        require(vkp.isFinite() && vkp >= 0.0) { "Удельный объём не может быть отрицательным" }
        require(time.isFinite() && time > 0.0) { "Время должно быть больше нуля" }
    }
}
