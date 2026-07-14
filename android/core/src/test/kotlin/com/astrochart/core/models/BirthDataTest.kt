package com.astrochart.core.models

import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
    fun testToUTC_WesternHemisphere() {
        val dateTime = LocalDateTime.of(2000, 1, 1, 12, 0)
        val birthData = BirthData(
            dateTime = dateTime,
            latitude = 0.0,
            longitude = 0.0,
            timeZone = ZoneId.of("America/New_York"),
            locationName = "New York"
        )

        val utcTime = birthData.toUTC()

        assertEquals(2000, utcTime.year)
        assertEquals(1, utcTime.monthValue)
        assertEquals(1, utcTime.dayOfMonth)
        assertTrue(utcTime.hour > 12)
    }

    @Test
    fun testToUTC_Eastern() {
        val dateTime = LocalDateTime.of(2000, 1, 1, 12, 0)
        val birthData = BirthData(
            dateTime = dateTime,
            latitude = 0.0,
            longitude = 0.0,
            timeZone = ZoneId.of("Europe/London"),
            locationName = "London"
        )

        val utcTime = birthData.toUTC()

        assertEquals(2000, utcTime.year)
        assertEquals(1, utcTime.monthValue)
        assertEquals(1, utcTime.dayOfMonth)
        assertEquals(12, utcTime.hour)
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

    @Test
    fun testBirthDataWithoutLocationName() {
        val dateTime = LocalDateTime.of(1990, 1, 1, 12, 0)
        val birthData = BirthData(
            dateTime = dateTime,
            latitude = 0.0,
            longitude = 0.0,
            timeZone = ZoneId.of("UTC")
        )

        assertEquals("", birthData.locationName)
    }
}
