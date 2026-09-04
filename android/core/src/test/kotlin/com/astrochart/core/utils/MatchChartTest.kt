package com.astrochart.core.utils

import com.astrochart.core.models.BirthData
import com.astrochart.core.panchangam.Panchangam
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * What marriage matching draws, pinned where a reader actually looks.
 *
 * The complaint this answers was a Marriage Match Making screen whose input
 * card read "Rasi: Virgo, Nakshatram: Hasta" while the koshtam underneath it
 * put the Moon in Libra, the lagnam in Thulam, and Uranus, Neptune and Pluto in
 * the cells. Those two readings come from **two independent computations of the
 * same Moon** — [Panchangam.moonRasiAndNakshatra] for the card,
 * [ChartCalculator] for the chart — printed one above the other, so a
 * disagreement between them is visible on sight.
 *
 * [SolachiChartTest] proves the engine against the reference jathagam. This
 * proves the two paths that meet on the matchmaking screen agree, and that what
 * the koshtam contains is a rasi koshtam.
 */
class MatchChartTest {

    private data class Case(
        val label: String,
        val at: LocalDateTime,
        val lat: Double,
        val lon: Double,
        val zone: String
    )

    /**
     * Instants chosen with the Swiss Ephemeris so the sidereal Moon sits at
     * least 1.5° inside its rasi and its nakshatra. The two paths here agree far
     * more closely than that — they share [com.astrochart.core.panchangam.SolarLunar]
     * and the same ayanamsa — but a fixture parked on a boundary would turn any
     * future hundredth-of-a-degree change into a red test that means nothing.
     */
    private val cases = listOf(
        Case("Kāraikkudi", LocalDateTime.of(1989, 12, 21, 13, 2), 10.0730, 78.7833, "Asia/Kolkata"),
        Case("London", LocalDateTime.of(1990, 7, 20, 14, 30), 51.5074, -0.1278, "Europe/London"),
        Case("Sydney", LocalDateTime.of(2001, 11, 2, 23, 45), -33.8688, 151.2093, "Australia/Sydney"),
        Case("Delhi", LocalDateTime.of(2010, 6, 1, 4, 5), 28.6139, 77.2090, "Asia/Kolkata"),
        Case("Tokyo", LocalDateTime.of(1966, 2, 14, 18, 20), 35.6762, 139.6503, "Asia/Tokyo")
    )

    private fun Case.chart() = ChartCalculator.calculateNatalChart(
        BirthData(
            dateTime = at,
            latitude = lat,
            longitude = lon,
            timeZone = ZoneId.of(zone),
            locationName = label
        )
    )

    private val signs = listOf(
        "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
        "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    )

    @Test
    fun theCardAndTheKoshtamNameTheSameMoon() {
        cases.forEach { c ->
            val (rasi, nak) = Panchangam.moonRasiAndNakshatra(c.at, ZoneId.of(c.zone))
            val moon = c.chart().planets.first { it.name == "Moon" }

            assertEquals(
                signs[rasi], moon.sign,
                "${c.label}: the card says ${signs[rasi]}, the chart draws the Moon in ${moon.sign}"
            )
            assertEquals(
                nak, (moon.siderealLon / (800.0 / 60.0)).toInt(),
                "${c.label}: the card and the chart disagree on the Moon's nakshatra"
            )
        }
    }

    /**
     * The reference jathagam, read the way the matchmaking screen renders it —
     * through [SouthIndianChart.cells] rather than off the position list, since
     * that is the step between the engine and what is on screen.
     */
    @Test
    fun theReferenceKoshtamIsTheOneOnTheSheet() {
        val cells = SouthIndianChart.cells(cases[0].chart(), includeAscendant = true)

        fun cellOf(body: String) = cells.first { body in it.bodies }.sign

        assertEquals("Pisces", cellOf("Ascendant"), "lagnam is Meenam on the sheet")
        assertEquals("Virgo", cellOf("Moon"), "rasi is Kanni on the sheet")
    }

    @Test
    fun theKoshtamHoldsTheNineGrahasAndNothingElse() {
        // Uranus, Neptune and Pluto were computed from fabricated elements and
        // were dropped with them; they have no place in a rasi koshtam either
        // way. Their appearance is the most visible symptom of a chart built by
        // the old engine, so it is worth failing on directly.
        cases.forEach { c ->
            val bodies = SouthIndianChart.cells(c.chart(), includeAscendant = true)
                .flatMap { it.bodies }
            listOf("Uranus", "Neptune", "Pluto").forEach {
                assertTrue(it !in bodies, "${c.label}: $it is in the koshtam")
            }
            assertEquals(
                listOf(
                    "Sun", "Moon", "Mercury", "Venus", "Mars",
                    "Jupiter", "Saturn", "Rahu", "Ketu", "Ascendant"
                ).sorted(),
                bodies.sorted(),
                "${c.label}: the koshtam is not the nine grahas plus the lagnam"
            )
        }
    }
}
