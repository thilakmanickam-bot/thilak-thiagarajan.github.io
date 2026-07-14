package com.astrochart.core.utils

import org.junit.Test
import kotlin.test.assertNotEmpty
import kotlin.test.assertTrue

class AspectInterpretationProviderTest {

    @Test
    fun testGetInterpretation_SunMoonConjunction() {
        val interpretation = AspectInterpretationProvider.getInterpretation("Sun", "Moon", "Conjunction")
        assertNotEmpty(interpretation)
        assertTrue(interpretation.contains("integration") || interpretation.contains("conscious"))
    }

    @Test
    fun testGetInterpretation_VenusMarsTrine() {
        val interpretation = AspectInterpretationProvider.getInterpretation("Venus", "Mars", "Trine")
        assertNotEmpty(interpretation)
    }

    @Test
    fun testGetInterpretation_DefaultMessage() {
        val interpretation = AspectInterpretationProvider.getInterpretation("Aries", "Taurus", "Conjunction")
        assertNotEmpty(interpretation)
        assertTrue(interpretation.contains("Aries") && interpretation.contains("Taurus"))
    }

    @Test
    fun testGetAllInterpretations() {
        val interpretations = AspectInterpretationProvider.getAllInterpretations()
        assertTrue(interpretations.isNotEmpty())
        assertTrue(interpretations.size > 50)
    }

    @Test
    fun testAllInterpretationsHaveText() {
        val interpretations = AspectInterpretationProvider.getAllInterpretations()
        for (interp in interpretations) {
            assertNotEmpty(interp.interpretation)
            assertNotEmpty(interp.bodyA)
            assertNotEmpty(interp.bodyB)
            assertNotEmpty(interp.aspectType)
        }
    }
}
