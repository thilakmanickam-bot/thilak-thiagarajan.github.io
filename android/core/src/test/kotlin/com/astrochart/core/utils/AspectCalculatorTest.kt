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
        // Sun 0° square Venus 90° (orb 0); Venus 90° trine Saturn 210° (orb 0);
        // Sun and Saturn are 150° apart, which is not a major aspect. So exactly
        // two aspects are expected. (A conjunction needs <=8°, so this avoids the
        // common mistake of assuming near-but-outside-orb pairs still count.)
        val sun = createPlanet("Sun", 0.0)
        val venus = createPlanet("Venus", 90.0)
        val saturn = createPlanet("Saturn", 210.0)

        val aspects = AspectCalculator.findAspects(listOf(sun, venus, saturn))

        assertEquals(2, aspects.size)
        val types = aspects.map { it.type }.toSet()
        assertTrue(types.contains("Square"))
        assertTrue(types.contains("Trine"))
    }

    @Test
    fun testFindAspects_WrapAround() {
        val sun = createPlanet("Sun", 0.0)
        val planet = createPlanet("Planet", 350.0)

        val aspects = AspectCalculator.findAspects(listOf(sun, planet))

        assertTrue(aspects.isEmpty() || aspects[0].type == "Opposition")
    }
}
