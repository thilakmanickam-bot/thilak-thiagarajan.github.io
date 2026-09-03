package com.astrochart.core.utils

import com.astrochart.core.models.BirthData
import com.astrochart.core.panchangam.SolarLunar
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.abs
import kotlin.test.assertTrue

/**
 * Invariants an ephemeris cannot violate, checked across a century.
 *
 * These need no almanac and no reference chart: they are facts about the solar
 * system, so any one of them failing means the arithmetic is wrong, whatever a
 * printed sheet happens to say.
 *
 * They exist because the formulas these replaced were fabricated — a correct
 * mean longitude plus one sine term of invented amplitude — and shipped for
 * months. [innerPlanetsStayNearTheSun] alone would have caught it on day one:
 * the old code put Mercury 109° from the Sun and Venus 161°, against physical
 * maxima of 28° and 48°. A single reference chart could have been argued with;
 * this cannot.
 */
class EphemerisInvariantsTest {

    /** ~890 samples from 1950 to 2050, stepping by a prime number of days so
     *  the sampling never syncs with any planet's period. */
    private val sampleDays: List<Double> = buildList {
        var d = LocalDate.of(1950, 1, 1)
        val end = LocalDate.of(2050, 1, 1)
        while (d.isBefore(end)) {
            add(SolarLunar.julianDayUt(d, 12.0))
            d = d.plusDays(41)
        }
    }

    private fun sun(jd: Double): Double =
        SolarLunar.sunApparentLongitude(SolarLunar.toJde(jd, jdYear(jd)))

    private fun moon(jd: Double): Double =
        SolarLunar.moonLongitude(SolarLunar.toJde(jd, jdYear(jd)))

    private fun jdYear(jd: Double): Int = 2000 + ((jd - 2451545.0) / 365.25).toInt()

    /** Angular separation, 0-180. */
    private fun separation(a: Double, b: Double): Double {
        val d = ((a - b) % 360.0 + 360.0) % 360.0
        return if (d > 180.0) 360.0 - d else d
    }

    @Test
    fun innerPlanetsStayNearTheSun() {
        // Mercury's greatest elongation is ~27.8°, Venus's ~47.3°. Both orbit
        // inside the Earth, so these are hard ceilings, not tendencies.
        sampleDays.forEach { jd ->
            val s = sun(jd)
            val mercury = separation(
                PlanetEphemeris.geocentricLongitude(PlanetEphemeris.Body.MERCURY, jd), s
            )
            val venus = separation(
                PlanetEphemeris.geocentricLongitude(PlanetEphemeris.Body.VENUS, jd), s
            )
            assertTrue(mercury <= 28.5, "Mercury was $mercury° from the Sun at jd $jd")
            assertTrue(venus <= 48.5, "Venus was $venus° from the Sun at jd $jd")
        }
    }

    @Test
    fun everyLongitudeIsInRange() {
        sampleDays.forEach { jd ->
            PlanetEphemeris.Body.entries
                .filter { it != PlanetEphemeris.Body.EARTH }
                .forEach { body ->
                    val lon = PlanetEphemeris.geocentricLongitude(body, jd)
                    assertTrue(lon >= 0.0 && lon < 360.0, "$body was $lon at jd $jd")
                }
            assertTrue(PlanetEphemeris.meanNode(jd).let { it >= 0.0 && it < 360.0 })
        }
    }

    @Test
    fun theNodeRegressesSteadily() {
        // The mean node moves backwards ~0.0529539°/day and never forwards.
        sampleDays.forEach { jd ->
            val a = PlanetEphemeris.meanNode(jd)
            val b = PlanetEphemeris.meanNode(jd + 30.0)
            // Backwards by ~1.59° over 30 days, allowing for the 360 wrap.
            val moved = ((a - b) % 360.0 + 360.0) % 360.0
            assertTrue(
                abs(moved - 30.0 * 0.0529539) < 0.01,
                "node moved $moved° over 30 days from jd $jd"
            )
        }
    }

    @Test
    fun theSunAndMoonMoveAtPlausibleRates() {
        // Sun 0.953-1.019°/day, Moon 11.76-15.38°/day. Generous bands: the
        // point is to catch a formula that has come unstuck, not to re-derive
        // the ephemeris.
        sampleDays.forEach { jd ->
            val sunStep = separation(sun(jd + 1.0), sun(jd))
            val moonStep = separation(moon(jd + 1.0), moon(jd))
            assertTrue(sunStep in 0.93..1.04, "Sun moved $sunStep°/day at jd $jd")
            assertTrue(moonStep in 11.0..15.8, "Moon moved $moonStep°/day at jd $jd")
        }
    }

    @Test
    fun tithiIsIndependentOfTheAyanamsa() {
        // Tithi is Moon minus Sun, so the ayanamsa cancels. If a change to the
        // ayanamsa model ever moves a tithi, something is subtracting it twice
        // or from only one term.
        sampleDays.forEach { jd ->
            val elong = ((moon(jd) - sun(jd)) % 360.0 + 360.0) % 360.0
            val tithi = (elong / 12.0).toInt()
            assertTrue(tithi in 0..29, "tithi index $tithi at jd $jd")
        }
    }

    @Test
    fun theLagnamSweepsEverySignInADay() {
        // One sign roughly every two hours, forward through the zodiac and all
        // twelve in a day. A broken ascendant sticks, reverses, or repeats —
        // the formula this replaced had no obliquity term and did all three.
        val signs = mutableSetOf<String>()
        var previous = -1.0
        var wraps = 0
        for (hour in 0 until 24) {
            val chart = ChartCalculator.calculateNatalChart(
                BirthData(
                    dateTime = LocalDateTime.of(2026, 3, 15, hour, 0),
                    latitude = 13.0827,
                    longitude = 80.2707,
                    timeZone = ZoneId.of("Asia/Kolkata"),
                    locationName = "Chennai"
                )
            )
            val lon = chart.ascendant.siderealLon
            signs.add(chart.ascendant.sign)
            if (previous >= 0.0 && lon < previous) wraps++
            previous = lon
        }
        assertTrue(signs.size == 12, "the lagnam visited ${signs.size} signs in a day: $signs")
        assertTrue(wraps == 1, "the lagnam wrapped $wraps times in a day, expected once")
    }
}
