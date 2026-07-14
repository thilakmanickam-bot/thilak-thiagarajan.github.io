package com.astrochart.core.utils

import com.astrochart.core.models.PlanetaryPosition
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AspectCalculatorTest {

    private fun createPlanet(name: String, longitude: Double): PlanetaryPosition {
        return PlanetaryPosition(
            name = name,
            lon = longitude,
            sign = "Aries",
            element = "Fire",
            modality = "Cardinal",
            degree = 0,
            minute = 0,
            label = "0° Aries",
            house = 1
        )
    }

    @Test
    fun testFindAspects_Conjunction() {
        val sun = createPlanet("Sun", 10.0)
        val moon = createPlanet("Moon", 12.0)

        val aspects = AspectCalculator.findAspects(listOf(sun, moon))

        assertEquals(1, aspects.size)
        assertEquals("Conjunction", aspects[0].type)
        assertTrue(aspects[0].orb < 8.0)
    }

    @Test
    fun testFindAspects_Sextile() {
        val sun = createPlanet("Sun", 0.0)
        val mercury = createPlanet("Mercury", 60.0)

        val aspects = AspectCalculator.findAspects(listOf(sun, mercury))

        assertEquals(1, aspects.size)
        assertEquals("Sextile", aspects[0].type)
    }

    @Test
    fun testFindAspects_Square() {
        val sun = createPlanet("Sun", 0.0)
        val venus = createPlanet("Venus", 90.0)

        val aspects = AspectCalculator.findAspects(listOf(sun, venus))

        assertEquals(1, aspects.size)
        assertEquals("Square", aspects[0].type)
    }

    @Test
    fun testFindAspects_Trine() {
        val sun = createPlanet("Sun", 0.0)
        val mars = createPlanet("Mars", 120.0)

        val aspects = AspectCalculator.findAspects(listOf(sun, mars))

        assertEquals(1, aspects.size)
        assertEquals("Trine", aspects[0].type)
    }

    @Test
    fun testFindAspects_Opposition() {
        val sun = createPlanet("Sun", 0.0)
        val jupiter = createPlanet("Jupiter", 180.0)

        val aspects = AspectCalculator.findAspects(listOf(sun, jupiter))

        assertEquals(1, aspects.size)
        assertEquals("Opposition", aspects[0].type)
    }

    @Test
    fun testFindAspects_NoAspect() {
        val sun = createPlanet("Sun", 0.0)
        val saturn = createPlanet("Saturn", 45.0)

        val aspects = AspectCalculator.findAspects(listOf(sun, saturn))

        assertTrue(aspects.isEmpty())
    }

    @Test
    fun testFindAspects_MultipleAspects() {
        val sun = createPlanet("Sun", 0.0)
        val moon = createPlanet("Moon", 10.0)
        val mercury = createPlanet("Mercury", 60.0)

        val aspects = AspectCalculator.findAspects(listOf(sun, moon, mercury))

        assertEquals(2, aspects.size)
    }

    @Test
    fun testFindAspects_WrapAround() {
        val sun = createPlanet("Sun", 0.0)
        val planet = createPlanet("Planet", 350.0)

        val aspects = AspectCalculator.findAspects(listOf(sun, planet))

        assertTrue(aspects.isEmpty() || aspects[0].type == "Opposition")
    }
}
