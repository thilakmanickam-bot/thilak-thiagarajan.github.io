package com.astrochart.core.utils

import com.astrochart.core.models.BirthData
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * The app's chart against the Swiss Ephemeris, over 125 cases.
 *
 * Halo computes everything on device, with no network and no data files, which
 * is right for a panchangam app but leaves nothing outside the app checking the
 * arithmetic — and the formulas this replaced were wrong by up to 135° and
 * shipped for months. So Swiss is used as an oracle: it never ships, it runs in
 * CI, and [EphemerisGolden] is generated from it by
 * `tools/ephemeris_oracle.py`. CI also re-runs the generator in `--check` mode,
 * so the golden file cannot be edited to make this test pass.
 *
 * **Tolerances are measured, not chosen.** Over 891 dates from 1950 to 2050 the
 * worst disagreement with Swiss is:
 *
 *     Rahu   0.000°      Sun    0.015°      Moon    0.019°   ascendant 0.006°
 *     Saturn 0.819°      Mars   0.721°      Jupiter 0.712°
 *     Venus  0.710°      Mercury 0.708°
 *
 * Each body gets its own bound just above its own measured worst case. A single
 * loose bound would let the Sun, Moon, node or ascendant drift by a degree
 * unnoticed, when they are in fact three orders of magnitude better than that —
 * and those four are what the rasi, nakshatram, porutham and dasha all rest on.
 *
 * A quarter of a pada (0.83°) is the outer planetary bound, so the **rasi is
 * never in doubt**; only a pada landing within 0.8° of its boundary could fall
 * on the wrong side. See docs/RECKONING.md.
 */
class EphemerisGoldenTest {

    /** Measured worst case + a little headroom, per body. */
    private val tolerance = mapOf(
        "Sun" to 0.05,
        "Moon" to 0.05,
        "Rahu" to 0.01,
        "Mercury" to 0.9,
        "Venus" to 0.9,
        "Mars" to 0.9,
        "Jupiter" to 0.9,
        "Saturn" to 0.9
    )

    private val ascendantTolerance = 0.05

    /** Shortest angular distance between two longitudes, 0-180. */
    private fun separation(a: Double, b: Double): Double {
        val d = ((a - b) % 360.0 + 360.0) % 360.0
        return if (d > 180.0) 360.0 - d else d
    }

    private fun chartFor(c: EphemerisGolden.Case) =
        ChartCalculator.calculateNatalChart(
            BirthData(
                // The golden is UT, so build the chart in UTC and let the app's
                // own toUTC() be the identity — this tests the ephemeris, not
                // the timezone plumbing.
                dateTime = LocalDateTime.of(
                    c.year, c.month, c.day,
                    c.hourUt.toInt(),
                    ((c.hourUt - c.hourUt.toInt()) * 60).roundToInt()
                ),
                latitude = c.latitude,
                longitude = c.longitude,
                timeZone = ZoneId.of("UTC"),
                locationName = c.label
            )
        )

    @Test
    fun everyBodyMatchesTheSwissEphemeris() {
        val failures = mutableListOf<String>()
        var worst = 0.0
        var worstAt = ""

        EphemerisGolden.cases.forEach { case ->
            val chart = chartFor(case)
            EphemerisGolden.bodies.forEachIndexed { i, name ->
                val expected = case.longitudes[i]
                val actual = chart.planets.first { it.name == name }.siderealLon
                val error = separation(actual, expected)
                if (error > worst) {
                    worst = error
                    worstAt = "$name on ${case.label}"
                }
                val bound = tolerance.getValue(name)
                if (error > bound) {
                    failures += "%s on %s: app %.4f, Swiss %.4f, off by %.4f (bound %.2f)"
                        .format(name, case.label, actual, expected, error, bound)
                }
            }
        }

        if (failures.isNotEmpty()) {
            fail(
                "${failures.size} of ${EphemerisGolden.cases.size * EphemerisGolden.bodies.size} " +
                    "readings disagree with the Swiss Ephemeris:\n" +
                    failures.take(20).joinToString("\n") +
                    if (failures.size > 20) "\n… and ${failures.size - 20} more" else ""
            )
        }
        // Not an assertion — a note in the log, so a change that quietly halves
        // the accuracy is visible even while still inside tolerance.
        println("worst disagreement with Swiss: %.4f° (%s)".format(worst, worstAt))
    }

    @Test
    fun theAscendantMatchesTheSwissEphemeris() {
        // The ascendant was the worst error in the old engine — seven signs —
        // and it is the one value that depends on latitude, so the cases span
        // 51°N to 34°S deliberately.
        val failures = EphemerisGolden.cases.mapNotNull { case ->
            val actual = chartFor(case).ascendant.siderealLon
            val error = separation(actual, case.ascendant)
            if (error > ascendantTolerance) {
                "%s: app %.4f, Swiss %.4f, off by %.4f".format(
                    case.label, actual, case.ascendant, error
                )
            } else null
        }
        assertTrue(
            failures.isEmpty(),
            "${failures.size} ascendants disagree with Swiss:\n" +
                failures.take(20).joinToString("\n")
        )
    }

    @Test
    fun theGoldenSetIsWideEnoughToBeWorthTrusting() {
        // A guard on the fixture itself: if someone trims the sample set down
        // to a handful of convenient dates, the suite above still passes but
        // stops meaning anything.
        assertTrue(EphemerisGolden.cases.size >= 100, "only ${EphemerisGolden.cases.size} cases")
        assertTrue(EphemerisGolden.bodies.size == 8, "expected 8 bodies")

        val years = EphemerisGolden.cases.map { it.year }
        assertTrue(years.min() <= 1955 && years.max() >= 2045, "years span ${years.min()}-${years.max()}")

        val lats = EphemerisGolden.cases.map { it.latitude }
        assertTrue(lats.max() >= 50.0, "no high-latitude case; max ${lats.max()}")
        assertTrue(lats.min() <= -30.0, "no southern-hemisphere case; min ${lats.min()}")
    }

    @Test
    fun ketuStaysOppositeRahuThroughout() {
        // Swiss is asked only for Rahu; Ketu is defined as the opposite point,
        // so this relation is the assertion rather than a second number.
        EphemerisGolden.cases.forEach { case ->
            val chart = chartFor(case)
            val rahu = chart.planets.first { it.name == "Rahu" }.siderealLon
            val ketu = chart.planets.first { it.name == "Ketu" }.siderealLon
            assertTrue(
                abs(separation(rahu, ketu) - 180.0) < 1e-6,
                "${case.label}: Rahu $rahu and Ketu $ketu are ${separation(rahu, ketu)}° apart"
            )
        }
    }
}
