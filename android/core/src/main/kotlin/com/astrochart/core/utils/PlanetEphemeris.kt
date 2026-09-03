package com.astrochart.core.utils

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Geocentric ecliptic longitudes for the classical planets, and the Moon's
 * mean node.
 *
 * This replaces a set of formulas that were a correct mean longitude bolted to
 * a *single* sine term with an invented amplitude — Mercury was given 3.25°
 * where its real equation of centre is nearly 23°. Against a printed jathagam
 * those produced errors of 28° to 135°: Venus in Rishabam where the almanac
 * had Makaram, Saturn in Mesham against Dhanusu.
 *
 * The method here is the standard one: take Keplerian elements with linear
 * rates, solve Kepler's equation properly rather than truncating its series,
 * place the planet in its own orbital plane, rotate to the ecliptic, and
 * subtract the Earth's heliocentric position to get a geocentric direction.
 *
 * Elements are the JPL/Standish "Keplerian Elements for Approximate Positions
 * of the Major Planets", the 1800–2050 set. Standish quotes a worst case of a
 * few arcminutes for the inner planets and under ~0.2° for Jupiter and Saturn
 * — comfortably inside a 3°20' nakshatra pada, and two orders of magnitude
 * better than what this replaces.
 *
 * Deliberately absent: Uranus, Neptune and Pluto. They have no place in a rasi
 * koshtam, and the Vedic surfaces ask for [VedicBody] instead.
 */
object PlanetEphemeris {

    /** The bodies a jathagam actually names, plus the Earth as a work term. */
    enum class Body { MERCURY, VENUS, EARTH, MARS, JUPITER, SATURN }

    /** a, e, I, L, longitude of perihelion, longitude of node — value + per-century rate. */
    private data class Elements(
        val a0: Double, val aT: Double,
        val e0: Double, val eT: Double,
        val i0: Double, val iT: Double,
        val l0: Double, val lT: Double,
        val p0: Double, val pT: Double,
        val n0: Double, val nT: Double
    )

    private val ELEMENTS = mapOf(
        Body.MERCURY to Elements(
            0.38709927, 0.00000037, 0.20563593, 0.00001906, 7.00497902, -0.00594749,
            252.25032350, 149472.67411175, 77.45779628, 0.16047689, 48.33076593, -0.12534081
        ),
        Body.VENUS to Elements(
            0.72333566, 0.00000390, 0.00677672, -0.00004107, 3.39467605, -0.00078890,
            181.97909950, 58517.81538729, 131.60246718, 0.00268329, 76.67984255, -0.27769418
        ),
        Body.EARTH to Elements(
            1.00000261, 0.00000562, 0.01671123, -0.00004392, -0.00001531, -0.01294668,
            100.46457166, 35999.37244981, 102.93768193, 0.32327364, 0.0, 0.0
        ),
        Body.MARS to Elements(
            1.52371034, 0.00001847, 0.09339410, 0.00007882, 1.84969142, -0.00813131,
            -4.55343205, 19140.30268499, -23.94362959, 0.44441088, 49.55953891, -0.29257343
        ),
        Body.JUPITER to Elements(
            5.20288700, -0.00011607, 0.04838624, -0.00013253, 1.30439695, -0.00183714,
            34.39644051, 3034.74612775, 14.72847983, 0.21252668, 100.47390909, 0.20469106
        ),
        Body.SATURN to Elements(
            9.53667594, -0.00125060, 0.05386179, -0.00050991, 2.48599187, 0.00193609,
            49.95424423, 1222.49362201, 92.59887831, -0.41897216, 113.66242448, -0.28867794
        )
    )

    /**
     * The apparent geocentric ecliptic longitude of [body] at [jdUt], in
     * degrees, **tropical** — subtract the ayanamsa for a rasi.
     */
    fun geocentricLongitude(body: Body, jdUt: Double): Double {
        require(body != Body.EARTH) { "The Earth has no geocentric longitude" }
        val t = (jdUt - 2451545.0) / 36525.0
        val (x, y, _) = heliocentric(body, t)
        val (ex, ey, _) = heliocentric(Body.EARTH, t)
        return norm360(Math.toDegrees(atan2(y - ey, x - ex)))
    }

    /**
     * The Moon's **mean** ascending node — Rahu; Ketu is 180° away. Meeus 47.7.
     *
     * Mean rather than true: Tamil almanacs tabulate the mean node, and it is
     * the mean node that matches the reference jathagam this is tested against.
     * The true node oscillates about it by up to ~1.6°, which is half a pada.
     */
    fun meanNode(jdUt: Double): Double {
        val t = (jdUt - 2451545.0) / 36525.0
        return norm360(
            125.0445479 - 1934.1362891 * t + 0.0020754 * t * t +
                t * t * t / 467441.0 - t * t * t * t / 60616000.0
        )
    }

    /** Heliocentric rectangular ecliptic coordinates, in AU. */
    private fun heliocentric(body: Body, t: Double): Triple<Double, Double, Double> {
        val el = ELEMENTS.getValue(body)
        val a = el.a0 + el.aT * t
        val e = el.e0 + el.eT * t
        val inc = Math.toRadians(el.i0 + el.iT * t)
        val meanLon = el.l0 + el.lT * t
        val periLon = el.p0 + el.pT * t
        val nodeLon = el.n0 + el.nT * t

        // Mean anomaly, folded to [-180, 180] so Newton starts near the root.
        var m = norm360(meanLon - periLon)
        if (m > 180.0) m -= 360.0
        val mRad = Math.toRadians(m)

        // Kepler's equation. Newton-Raphson converges in a handful of passes at
        // these eccentricities (Mercury's 0.206 is the worst); the cap is only
        // there so a pathological input cannot spin forever.
        var ecc = mRad + e * sin(mRad)
        repeat(64) {
            val d = (ecc - e * sin(ecc) - mRad) / (1 - e * cos(ecc))
            ecc -= d
            if (abs(d) < 1e-12) return@repeat
        }

        // Position in the orbital plane, then rotated by argument of perihelion,
        // inclination and node into ecliptic coordinates.
        val xv = a * (cos(ecc) - e)
        val yv = a * sqrt(1 - e * e) * sin(ecc)
        val w = Math.toRadians(periLon - nodeLon)
        val om = Math.toRadians(nodeLon)
        val cw = cos(w); val sw = sin(w)
        val co = cos(om); val so = sin(om)
        val ci = cos(inc); val si = sin(inc)
        return Triple(
            (cw * co - sw * so * ci) * xv + (-sw * co - cw * so * ci) * yv,
            (cw * so + sw * co * ci) * xv + (-sw * so + cw * co * ci) * yv,
            (sw * si) * xv + (cw * si) * yv
        )
    }

    private fun norm360(x: Double): Double = ((x % 360.0) + 360.0) % 360.0
}
