package com.astrochart.core.interpret

import com.astrochart.core.models.BirthData
import com.astrochart.core.utils.ChartCalculator
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompatibilityTest {

    private fun chart(y: Int, mo: Int, d: Int, h: Int, mi: Int) = ChartCalculator.calculateNatalChart(
        BirthData(
            dateTime = LocalDateTime.of(y, mo, d, h, mi),
            latitude = 13.0827,
            longitude = 80.2707,
            timeZone = ZoneId.of("Asia/Kolkata"),
            locationName = "Chennai"
        )
    )

    @Test
    fun elementHarmony_isSymmetricAndBanded() {
        assertEquals(85, Compatibility.elementHarmony("Fire", "Fire"))
        assertEquals(90, Compatibility.elementHarmony("Fire", "Air"))
        assertEquals(90, Compatibility.elementHarmony("Air", "Fire"))
        assertEquals(90, Compatibility.elementHarmony("Water", "Earth"))
        assertEquals(45, Compatibility.elementHarmony("Fire", "Water"))
        assertEquals(55, Compatibility.elementHarmony("Fire", "Earth"))
    }

    @Test
    fun ganaHarmony_ordersDevaRakshasaLowest() {
        assertEquals(85, Compatibility.ganaHarmony(0, 0)) // Deva–Deva
        assertEquals(40, Compatibility.ganaHarmony(0, 2)) // Deva–Rakshasa
        assertEquals(40, Compatibility.ganaHarmony(2, 0)) // symmetric
        assertTrue(Compatibility.ganaHarmony(0, 0) > Compatibility.ganaHarmony(0, 1))
        assertTrue(Compatibility.ganaHarmony(0, 1) > Compatibility.ganaHarmony(1, 2))
        assertTrue(Compatibility.ganaHarmony(1, 2) > Compatibility.ganaHarmony(0, 2))
    }

    @Test
    fun birthNakshatra_isInRange_andMatchesPanchangam() {
        val c = chart(1990, 7, 20, 14, 30)
        val nak = Compatibility.birthNakshatra(c)
        assertTrue(nak in 0..26, "nakshatra $nak out of range")
    }

    @Test
    fun compute_producesBoundedSymmetricResult() {
        val a = chart(1990, 7, 20, 14, 30)
        val b = chart(1992, 3, 5, 9, 15)

        val ab = Compatibility.compute("A", a, "B", b)
        val ba = Compatibility.compute("B", b, "A", a)

        assertTrue(ab.overall in 0..100, "overall ${ab.overall}")
        assertEquals(ab.overall, ba.overall, "score should be symmetric")
        assertEquals(4, ab.components.size)
        assertEquals(setOf("sun", "moon", "ascendant", "gana"), ab.components.map { it.key }.toSet())
        ab.components.forEach { assertTrue(it.score in 0..100, "${it.key}=${it.score}") }
        assertTrue(ab.nakshatraA in 0..26 && ab.nakshatraB in 0..26)
        assertTrue(ab.ganaA in 0..2 && ab.ganaB in 0..2)
    }
}
