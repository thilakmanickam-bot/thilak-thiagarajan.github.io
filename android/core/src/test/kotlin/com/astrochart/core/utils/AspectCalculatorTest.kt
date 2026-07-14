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
    }

    @Test
    fun testFindAspects_NoAspect() {
        val sun = createPlanet("Sun", 0.0)
        val saturn = createPlanet("Saturn", 45.0)

        val aspects = AspectCalculator.findAspects(listOf(sun, saturn))

        assertTrue(aspects.isEmpty())
    }
}
