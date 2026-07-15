package com.astrochart.data.util

import com.astrochart.core.models.BirthData
import com.astrochart.core.utils.ChartCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class ChartJsonTest {

    private fun sampleChart() = ChartCalculator.calculateNatalChart(
        BirthData(
            dateTime = LocalDateTime.of(1990, 7, 20, 14, 30),
            latitude = 34.0522,
            longitude = -118.2437,
            timeZone = ZoneId.of("America/Los_Angeles"),
            locationName = "Los Angeles"
        )
    )

    @Test
    fun roundTrip_preservesEntireChart() {
        val original = sampleChart()

        val json = ChartJson.toJson(original)
        val restored = ChartJson.fromJson(json)

        assertNotNull(restored)
        // Full structural equality (data classes) is the strongest check.
        assertEquals(original, restored)
    }

    @Test
    fun roundTrip_preservesBirthDataAndPlacements() {
        val original = sampleChart()

        val restored = ChartJson.fromJson(ChartJson.toJson(original))!!

        assertEquals(original.birthData.dateTime, restored.birthData.dateTime)
        assertEquals(original.birthData.timeZone, restored.birthData.timeZone)
        assertEquals(original.birthData.latitude, restored.birthData.latitude, 0.0)
        assertEquals(original.planets.size, restored.planets.size)
        assertEquals(original.ascendant.sign, restored.ascendant.sign)
        assertEquals(original.aspects.size, restored.aspects.size)
        assertEquals(original.balance.elements, restored.balance.elements)
    }

    @Test
    fun fromJson_returnsNullForBlankOrGarbage() {
        assertNull(ChartJson.fromJson(""))
        assertNull(ChartJson.fromJson("   "))
        assertNull(ChartJson.fromJson("not json at all {"))
    }
}
