package com.example.sanich.data

object ReferenceData {
    val pedDiametersMm = listOf(92.0, 103.0, 117.0, 130.0)

    /*
     * Полный набор ЭК и толщин стенки собран по всей таблице
     * PedCoolingDatabase.json, без привязки к конкретному ПЭД/мощности.
     * В исходной таблице колонна 140 мм соответствует используемому
     * в геометрическом расчёте наружному диаметру 139,7 мм.
     */
    val casingWallThicknessesByOuterDiameterMm: Map<Double, List<Double>> = linkedMapOf(
        102.0 to listOf(6.5),
        114.0 to listOf(5.2, 5.7, 6.4, 7.4, 8.6, 10.2),
        139.7 to listOf(6.2, 7.0, 7.7, 9.2, 10.5),
        146.0 to listOf(6.5, 7.0, 7.7, 8.5, 9.6, 10.7),
        168.0 to listOf(7.3, 8.0, 8.9, 10.6, 12.1),
        178.0 to listOf(5.9, 6.9, 8.1, 9.2, 10.4, 11.5, 12.7, 13.7, 15.0),
        194.0 to listOf(7.6, 8.3, 9.3, 10.9, 12.7, 16.1)
    )

    val casingOuterDiametersMm = casingWallThicknessesByOuterDiameterMm.keys.toList()

    // Для совместимости с местами, где нужен общий список всех толщин.
    val casingWallThicknessesMm = casingWallThicknessesByOuterDiameterMm.values
        .flatten()
        .distinct()
        .sorted()

    fun wallThicknessesForCasing(casingOuterDiameterMm: Double): List<Double> =
        casingWallThicknessesByOuterDiameterMm[casingOuterDiameterMm].orEmpty()

    val tubingDiametersMm = listOf("60", "73", "89")
}
