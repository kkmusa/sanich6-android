package com.example.sanich.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class HeadAtFrequencyCalculatorTest {
    @Test fun quadraticHead() {
        assertEquals(2449.44, HeadAtFrequencyCalculator.calculate(2100.0, 54.0), 1e-9)
        assertEquals(2100.0, HeadAtFrequencyCalculator.calculate(2100.0, 50.0), 1e-9)
        assertEquals(1344.0, HeadAtFrequencyCalculator.calculate(2100.0, 40.0), 1e-9)
        assertEquals(4725.0, HeadAtFrequencyCalculator.calculate(2100.0, 75.0), 1e-9)
        assertEquals(0.0, HeadAtFrequencyCalculator.calculate(2100.0, 0.0), 0.0)
    }

    @Test fun invalidInputsAndOverflow() {
        listOf(0.0 to 50.0, -1.0 to 50.0, 2100.0 to -1.0,
            Double.NaN to 50.0, 2100.0 to Double.POSITIVE_INFINITY,
            1e308 to 1e308).forEach { (head, frequency) ->
            assertThrows(IllegalArgumentException::class.java) {
                HeadAtFrequencyCalculator.calculate(head, frequency)
            }
        }
    }
}
