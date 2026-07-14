package com.astrochart.core.models

import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals

class BirthDataTest {

    @Test
    fun testBirthDataCreation() {
        val dateTime = LocalDateTime.of(1990, 1, 1, 12, 0)
        val birthData = BirthData(
            dateTime = dateTime,
            latitude = 40.7128,
            longitude = -74.0060,
            timeZone = ZoneId.of("America/New_York"),
            locationName = "New York"
        )

        assertEquals(dateTime, birthData.dateTime)
        assertEquals(40.7128, birthData.latitude)
        assertEquals(-74.0060, birthData.longitude)
        assertEquals("America/New_York", birthData.timeZone.id)
        assertEquals("New York", birthData.locationName)
    }

    @Test
    fun testToUTC_UTC() {
        val dateTime = LocalDateTime.of(2000, 1, 1, 12, 0)
        val birthData = BirthData(
            dateTime = dateTime,
            latitude = 0.0,
            longitude = 0.0,
            timeZone = ZoneId.of("UTC"),
            locationName = "Somewhere"
        )

        val utcTime = birthData.toUTC()

        assertEquals(dateTime, utcTime)
    }
}
