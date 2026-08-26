package com.astrochart.core.panchangam

import java.time.LocalDate
import kotlin.math.floor
import kotlin.math.sin

/**
 * Apparent geocentric ecliptic longitudes of the Sun and Moon, from Jean
 * Meeus' *Astronomical Algorithms*. The Sun follows chapter 25 and the Moon a
 * truncated chapter-47 series (the largest ~34 periodic terms plus the additive
 * terms). Verified against Meeus' worked examples: Sun for 1992-10-13.0 TD =
 * 199.90894° (book: 199.90895°) and Moon for 1992-04-12.0 TD = 133.1659°
 * (book: 133.162655°, a ~12″ truncation residual). Both are well inside the
 * accuracy panchangam boundary times need.
 *
 * All angles are in degrees, longitudes tropical of date, normalized to
 * `[0, 360)`. Inputs are Julian Ephemeris Day (JDE, i.e. TT-based).
 */
object SolarLunar {

    fun norm360(x: Double): Double = ((x % 360.0) + 360.0) % 360.0

    /** JD (UT) for a civil date at the given fractional UT hour. */
    fun julianDayUt(date: LocalDate, hourUt: Double): Double {
        val y = date.year
        val m = date.monthValue
        val d = date.dayOfMonth
        val a = (14 - m) / 12
        val yy = y + 4800 - a
        val mm = m + 12 * a - 3
        val jdn = d + (153 * mm + 2) / 5 + 365 * yy + yy / 4 - yy / 100 + yy / 400 - 32045
        return jdn - 0.5 + hourUt / 24.0
    }

    /**
     * Approximate ΔT (TT − UT) in seconds. Espenak–Meeus polynomial centred on
     * 2000; good to a few seconds across the app's usable range and far finer
     * than the panchangam needs.
     */
    fun deltaTSeconds(year: Int): Double {
        val t = year - 2000.0
        return 62.92 + 0.32217 * t + 0.005589 * t * t
    }

    /** JDE (TT) from a JD (UT), applying ΔT for [year]. */
    fun toJde(jdUt: Double, year: Int): Double = jdUt + deltaTSeconds(year) / 86400.0

    /** Apparent longitude of the Sun (Meeus ch. 25), degrees in `[0, 360)`. */
    fun sunApparentLongitude(jde: Double): Double {
        val t = (jde - 2451545.0) / 36525.0
        val l0 = 280.46646 + 36000.76983 * t + 0.0003032 * t * t
        val m = 357.52911 + 35999.05029 * t - 0.0001537 * t * t
        val mr = Math.toRadians(m)
        val c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(mr) +
            (0.019993 - 0.000101 * t) * sin(2 * mr) +
            0.000289 * sin(3 * mr)
        val trueLon = l0 + c
        val omega = 125.04 - 1934.136 * t
        val apparent = trueLon - 0.00569 - 0.00478 * sin(Math.toRadians(omega))
        return norm360(apparent)
    }

    // Meeus table 47.A, largest terms: {D, M, M', F, coefficient (1e-6 deg)}.
    private val MOON_TERMS = arrayOf(
        intArrayOf(0, 0, 1, 0, 6288774), intArrayOf(2, 0, -1, 0, 1274027),
        intArrayOf(2, 0, 0, 0, 658314), intArrayOf(0, 0, 2, 0, 213618),
        intArrayOf(0, 1, 0, 0, -185116), intArrayOf(0, 0, 0, 2, -114332),
        intArrayOf(2, 0, -2, 0, 58793), intArrayOf(2, -1, -1, 0, 57066),
        intArrayOf(2, 0, 1, 0, 53322), intArrayOf(2, -1, 0, 0, 45758),
        intArrayOf(0, 1, -1, 0, -40923), intArrayOf(1, 0, 0, 0, -34720),
        intArrayOf(0, 1, 1, 0, -30383), intArrayOf(2, 0, 0, -2, 15327),
        intArrayOf(0, 0, 1, 2, -12528), intArrayOf(0, 0, 1, -2, 10980),
        intArrayOf(4, 0, -1, 0, 10675), intArrayOf(0, 0, 3, 0, 10034),
        intArrayOf(4, 0, -2, 0, 8548), intArrayOf(2, 1, -1, 0, -7888),
        intArrayOf(2, 1, 0, 0, -6766), intArrayOf(1, 0, -1, 0, -5163),
        intArrayOf(1, 1, 0, 0, 4987), intArrayOf(2, -1, 1, 0, 4036),
        intArrayOf(2, 0, 2, 0, 3994), intArrayOf(4, 0, 0, 0, 3861),
        intArrayOf(2, 0, -3, 0, 3665), intArrayOf(0, 1, -2, 0, -2689),
        intArrayOf(2, 0, -1, 2, -2602), intArrayOf(2, -1, -2, 0, 2390),
        intArrayOf(1, 0, 1, 0, -2348), intArrayOf(2, -2, 0, 0, 2236),
        intArrayOf(0, 2, -1, 0, -2120), intArrayOf(2, 2, -1, 0, -2069)
    )

    /** Apparent longitude of the Moon (Meeus ch. 47, truncated), degrees. */
    fun moonLongitude(jde: Double): Double {
        val t = (jde - 2451545.0) / 36525.0
        val lp = 218.3164477 + 481267.88123421 * t - 0.0015786 * t * t +
            t * t * t / 538841.0 - t * t * t * t / 65194000.0
        val d = 297.8501921 + 445267.1114034 * t - 0.0018819 * t * t +
            t * t * t / 545868.0 - t * t * t * t / 113065000.0
        val m = 357.5291092 + 35999.0502909 * t - 0.0001536 * t * t + t * t * t / 24490000.0
        val mp = 134.9633964 + 477198.8675055 * t + 0.0087414 * t * t +
            t * t * t / 69699.0 - t * t * t * t / 14712000.0
        val f = 93.2720950 + 483202.0175233 * t - 0.0036539 * t * t -
            t * t * t / 3526000.0 + t * t * t * t / 863310000.0
        val a1 = 119.75 + 131.849 * t
        val a2 = 53.09 + 479264.290 * t
        val e = 1 - 0.002516 * t - 0.0000074 * t * t

        var suml = 0.0
        for (term in MOON_TERMS) {
            val arg = term[0] * d + term[1] * m + term[2] * mp + term[3] * f
            val eFactor = when (kotlin.math.abs(term[1])) {
                1 -> e
                2 -> e * e
                else -> 1.0
            }
            suml += term[4] * eFactor * sin(Math.toRadians(arg))
        }
        suml += 3958.0 * sin(Math.toRadians(a1)) +
            1962.0 * sin(Math.toRadians(lp - f)) +
            318.0 * sin(Math.toRadians(a2))

        var lon = lp + suml / 1_000_000.0
        // Dominant nutation-in-longitude terms (keeps the apparent longitude
        // consistent with the Sun's; sub-arcsecond effect on boundary times).
        val omega = 125.04452 - 1934.136261 * t
        val ls = 280.4665 + 36000.7698 * t
        val lm = 218.3165 + 481267.8813 * t
        val dpsi = (-17.20 * sin(Math.toRadians(omega)) - 1.32 * sin(Math.toRadians(2 * ls)) -
            0.23 * sin(Math.toRadians(2 * lm)) + 0.21 * sin(Math.toRadians(2 * omega))) / 3600.0
        lon += dpsi
        return norm360(lon)
    }

    /** Sign index 0–11 (Aries..Pisces) of a tropical longitude. */
    fun signIndex(longitude: Double): Int = floor(norm360(longitude) / 30.0).toInt() % 12
}
