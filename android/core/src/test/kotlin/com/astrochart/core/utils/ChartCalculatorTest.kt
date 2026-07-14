package com.astrochart.core.utils

import com.astrochart.core.models.BirthData
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ChartCalculatorTest {

    @Test
    fun testCalculateNatalChart() {
        val birthData = BirthData(
            dateTime = LocalDateTime.of(2000, 1, 1, 12, 0),
            latitude = 40.7128,
            longitude = -74.0060,
            timeZone = ZoneId.of("America/New_York"),
            locationName = "New York"
        )

        val chart = ChartCalculator.calculateNatalChart(birthData)

        assertNotNull(chart)
        assertEquals(birthData, chart.birthData)
        assertNotNull(chart.ascendant)
        assertNotNull(chart.midheaven)
        assertTrue(chart.planets.size > 0)
        assertTrue(chart.aspects.size >= 0)
    }

    @Test
    fun testCalculateNatalChart_HasAllPlanets() {
        val birthData = BirthData(
            dateTime = LocalDateTime.of(1990, 7, 20, 14, 30),
            latitude = 34.0522,
            longitude = -118.2437,
            timeZone = ZoneId.of("America/Los_Angeles"),
            locationName = "Los Angeles"
        )

        val chart = ChartCalculator.calculateNatalChart(birthData)

        assertEquals(10, chart.planets.size)
        val planetNames = chart.planets.map { it.name }.toSet()
        assertTrue(planetNames.contains("Sun"))
        assertTrue(planetNames.contains("Moon"))
        assertTrue(planetNames.contains("Mercury"))
        assertTrue(planetNames.contains("Venus"))
        assertTrue(planetNames.contains("Mars"))
        assertTrue(planetNames.contains("Jupiter"))
        assertTrue(planetNames.contains("Saturn"))
        assertTrue(planetNames.contains("Uranus"))
        assertTrue(planetNames.contains("Neptune"))
        assertTrue(planetNames.contains("Pluto"))
    }

    @Test
    fun testCalculateNatalChart_PlanetHaveSigns() {
        val birthData = BirthData(
            dateTime = LocalDateTime.of(2000, 1, 1, 12, 0),
            latitude = 0.0,
            longitude = 0.0,
            timeZone = ZoneId.of("UTC"),
            locationName = "Equator"
        )

        val chart = ChartCalculator.calculateNatalChart(birthData)

        for (planet in chart.planets) {
            assertTrue(planet.sign in ZodiacUtils.getAllSigns())
            assertTrue(planet.degree in 0..29)
            assertTrue(planet.minute in 0..59)
            assertTrue(planet.lon in 0.0..360.0)
        }
    }

    @Test
    fun testCalculateNatalChart_AscendantHasSign() {
        val birthData = BirthData(
            dateTime = LocalDateTime.of(2000, 1, 1, 12, 0),
            latitude = 0.0,
            longitude = 0.0,
            timeZone = ZoneId.of("UTC"),
            locationName = "Equator"
        )

        val chart = ChartCalculator.calculateNatalChart(birthData)

        assertTrue(chart.ascendant.sign in ZodiacUtils.getAllSigns())
        assertEquals("Ascendant", chart.ascendant.name)
        assertEquals(1, chart.ascendant.house)
    }

    @Test
    fun testCalculateNatalChart_MidheavenHasSign() {
        val birthData = BirthData(
            dateTime = LocalDateTime.of(2000, 1, 1, 12, 0),
            latitude = 0.0,
            longitude = 0.0,
            timeZone = ZoneId.of("UTC"),
            locationName = "Equator"
        )

        val chart = ChartCalculator.calculateNatalChart(birthData)

        assertTrue(chart.midheaven.sign in ZodiacUtils.getAllSigns())
        assertEquals("Midheaven", chart.midheaven.name)
        assertEquals(10, chart.midheaven.house)
    }

    @Test
    fun testCalculateNatalChart_BalanceNotEmpty() {
        val birthData = BirthData(
            dateTime = LocalDateTime.of(2000, 1, 1, 12, 0),
            latitude = 0.0,
            longitude = 0.0,
            timeZone = ZoneId.of("UTC"),
            locationName = "Equator"
        )

        val chart = ChartCalculator.calculateNatalChart(birthData)

        assertTrue(chart.balance.elements.isNotEmpty())
        assertTrue(chart.balance.modalities.isNotEmpty())
        assertTrue(chart.balance.elements.values.sum() > 0)
        assertTrue(chart.balance.modalities.values.sum() > 0)
    }
}
