package com.example.sanich.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ForeignPumpCalculatorTest {
    @Test
    fun dn360At50Hz() {
        val result = ForeignPumpCalculator.calculate(360.0, 60.0, 50.0)
        assertEquals(300.0, result.barrelsPerDay, 0.0001)
        assertEquals(47.6961884784, result.cubicMetersPerDay, 0.0001)
    }

    @Test
    fun customTargetFrequency() {
        val result = ForeignPumpCalculator.calculate(360.0, 60.0, 55.0)
        assertEquals(330.0, result.barrelsPerDay, 0.0001)
    }

    @Test
    fun rejectsZeroFrequency() {
        assertThrows(IllegalArgumentException::class.java) {
            ForeignPumpCalculator.calculate(360.0, 0.0, 50.0)
        }
    }
}
