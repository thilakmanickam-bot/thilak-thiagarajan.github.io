package com.astrochart.core.utils

import com.astrochart.core.models.BirthData
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ChartCalculatorTest {

    @Test
    fun testCalculateNatalChart_BasicCalculation() {
        val birthData = BirthData(
            dateTime = LocalDateTime.of(2000, 1, 1, 12, 0),
            latitude = 0.0,
            longitude = 0.0,
            timeZone = ZoneId.of("UTC"),
            locationName = "Test"
        )

        val chart = ChartCalculator.calculateNatalChart(birthData)

        assertNotNull(chart)
        assertNotNull(chart.ascendant)
        assertNotNull(chart.midheaven)
        assertTrue(chart.planets.size == 10)
        assertTrue(chart.balance.elements.isNotEmpty())
    }
}
