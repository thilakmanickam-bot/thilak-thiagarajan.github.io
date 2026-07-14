package com.astrochart.data

import com.astrochart.core.utils.ChartCalculator
import com.astrochart.core.utils.ZodiacUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SampleDataTest {

    @Test
    fun sampleBirthData_hasExpectedValues() {
        val birthData = SampleData.sampleBirthData()

        assertEquals(1938, birthData.dateTime.year)
        assertEquals(7, birthData.dateTime.monthValue)
        assertEquals(20, birthData.dateTime.dayOfMonth)
        assertEquals("America/Los_Angeles", birthData.timeZone.id)
    }

    @Test
    fun sampleBirthData_computesValidChart() {
        val chart = ChartCalculator.calculateNatalChart(SampleData.sampleBirthData())

        assertEquals(10, chart.planets.size)
        assertTrue(chart.ascendant.sign in ZodiacUtils.getAllSigns())
        for (planet in chart.planets) {
            assertTrue(planet.lon >= 0.0 && planet.lon < 360.0)
        }
    }
}
