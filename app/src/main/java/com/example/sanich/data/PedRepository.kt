package com.example.sanich.data


class PedRepository(
    private val dao: PedDao
) {


    suspend fun getDiameters(): List<Int> {

        return dao.getDiameters()

    }


    suspend fun getPowers(
        diameter: Int
    ): List<Int> {

        return dao.getPowers(diameter)

    }


    suspend fun getCasings(
        diameter: Int,
        power: Int
    ): List<Int> {

        return dao.getCasings(
            diameter,
            power
        )

    }


    suspend fun getWalls(
        diameter: Int,
        power: Int,
        casing: Int
    ): List<Double> {

        return dao.getWalls(
            diameter,
            power,
            casing
        )

    }


    suspend fun getResultPeds(
        diameter: Int,
        power: Int,
        casing: Int,
        wall: Double
    ): List<PedEntity> {

        return dao.getResultPeds(
            diameter,
            power,
            casing,
            wall
        )

    }


    suspend fun getPedCount(): Int {

        return dao.getCount()

    }

}