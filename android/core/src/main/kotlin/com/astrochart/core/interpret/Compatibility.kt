package com.astrochart.core.interpret

import com.astrochart.core.models.NatalChart
import com.astrochart.core.panchangam.Ayanamsa
import com.astrochart.core.panchangam.SolarLunar
import com.astrochart.core.utils.ZodiacUtils

/** One scored facet of a couple's match. [key] is a stable UI-localization key. */
data class CompatibilityComponent(val key: String, val score: Int)

/**
 * Result of comparing two profiles. Scores are 0–100. All names/indices are
 * language-neutral (English signs, 0-based nakshatra/gana) so the UI localizes.
 */
data class CompatibilityResult(
    val nameA: String,
    val nameB: String,
    val overall: Int,
    val components: List<CompatibilityComponent>,
    val sunA: String,
    val sunB: String,
    val moonA: String,
    val moonB: String,
    val nakshatraA: Int,
    val nakshatraB: Int,
    val ganaA: Int, // 0 = Deva, 1 = Manushya, 2 = Rakshasa
    val ganaB: Int
)

/**
 * A light couple-compatibility score blending Western luminary/element harmony
 * (Sun, Moon, Ascendant) with a Vedic Gana porutham from each person's birth
 * nakshatra (the Moon's sidereal star). Deterministic and for entertainment,
 * in the spirit of the app's other readings. Pure logic — unit-tested.
 */
object Compatibility {

    // Gana of each of the 27 nakshatras (0 = Ashwini): 0 Deva, 1 Manushya, 2 Rakshasa.
    private val GANA = intArrayOf(
        0, 1, 2, 1, 0, 1, 0, 0, 2, // Ashwini..Ashlesha
        2, 1, 1, 0, 2, 0, 2, 0, 2, // Magha..Jyeshtha
        2, 1, 1, 0, 2, 2, 1, 1, 0  // Mula..Revati
    )

    private const val NAK_SIZE = 360.0 / 27.0

    fun compute(nameA: String, chartA: NatalChart, nameB: String, chartB: NatalChart): CompatibilityResult {
        val sunA = sign(chartA, "Sun")
        val sunB = sign(chartB, "Sun")
        val moonA = sign(chartA, "Moon")
        val moonB = sign(chartB, "Moon")

        val sunScore = elementHarmony(ZodiacUtils.getElement(sunA), ZodiacUtils.getElement(sunB))
        val moonScore = elementHarmony(ZodiacUtils.getElement(moonA), ZodiacUtils.getElement(moonB))
        val ascScore = elementHarmony(
            ZodiacUtils.getElement(chartA.ascendant.sign),
            ZodiacUtils.getElement(chartB.ascendant.sign)
        )

        val nakA = birthNakshatra(chartA)
        val nakB = birthNakshatra(chartB)
        val ganaA = GANA[nakA]
        val ganaB = GANA[nakB]
        val ganaScore = ganaHarmony(ganaA, ganaB)

        // Weighted blend: Sun 30%, Moon 30%, Ascendant 15%, Gana 25%.
        val overall = (sunScore * 30 + moonScore * 30 + ascScore * 15 + ganaScore * 25) / 100

        return CompatibilityResult(
            nameA = nameA,
            nameB = nameB,
            overall = overall,
            components = listOf(
                CompatibilityComponent("sun", sunScore),
                CompatibilityComponent("moon", moonScore),
                CompatibilityComponent("ascendant", ascScore),
                CompatibilityComponent("gana", ganaScore)
            ),
            sunA = sunA, sunB = sunB,
            moonA = moonA, moonB = moonB,
            nakshatraA = nakA, nakshatraB = nakB,
            ganaA = ganaA, ganaB = ganaB
        )
    }

    private fun sign(chart: NatalChart, planet: String): String =
        chart.planets.firstOrNull { it.name == planet }?.sign ?: chart.ascendant.sign

    /** Element harmony 0–100 between two of Fire/Earth/Air/Water. */
    fun elementHarmony(a: String, b: String): Int {
        if (a == b) return 85
        val pair = setOf(a, b)
        return when (pair) {
            setOf("Fire", "Air"), setOf("Earth", "Water") -> 90
            setOf("Fire", "Earth"), setOf("Air", "Water") -> 55
            setOf("Fire", "Water"), setOf("Earth", "Air") -> 45
            else -> 60
        }
    }

    /** Gana porutham 0–100. 0 Deva, 1 Manushya, 2 Rakshasa. */
    fun ganaHarmony(a: Int, b: Int): Int {
        if (a == b) return if (a == 2) 70 else 85 // Rakshasa–Rakshasa a touch lower
        val pair = setOf(a, b)
        return when (pair) {
            setOf(0, 1) -> 75 // Deva–Manushya
            setOf(1, 2) -> 55 // Manushya–Rakshasa
            else -> 40        // Deva–Rakshasa
        }
    }

    /** Birth nakshatra (0–26) from the natal Moon's sidereal longitude. */
    fun birthNakshatra(chart: NatalChart): Int {
        val moon = chart.planets.firstOrNull { it.name == "Moon" } ?: return 0
        val utc = chart.birthData.toUTC()
        val hour = utc.hour + utc.minute / 60.0 + utc.second / 3600.0
        val jde = SolarLunar.toJde(SolarLunar.julianDayUt(utc.toLocalDate(), hour), utc.year)
        val sidereal = Ayanamsa.toSidereal(moon.lon, jde)
        return (sidereal / NAK_SIZE).toInt().coerceIn(0, 26)
    }
}
