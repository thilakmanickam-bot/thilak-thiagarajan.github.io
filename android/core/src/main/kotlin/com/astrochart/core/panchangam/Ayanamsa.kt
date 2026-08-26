package com.astrochart.core.panchangam

/**
 * Lahiri (Chitrapaksha) ayanamsa — the offset subtracted from a tropical
 * longitude to obtain the sidereal (nirayana) longitude used throughout Indian
 * astrology. A linear model anchored near J2000 (≈ 23.853°) with the mean
 * precession rate; accurate to about an arc-minute over the app's date range,
 * which shifts a nakshatra boundary by only a few minutes of time.
 *
 * Note: tithi, karana (Moon − Sun) and the paksha are ayanamsa-independent —
 * the offset cancels — so they carry the full accuracy of [SolarLunar].
 */
object Ayanamsa {
    fun lahiri(jde: Double): Double {
        val t = (jde - 2451545.0) / 36525.0 // Julian centuries from J2000
        return 23.8523 + 1.396 * t
    }

    /** Sidereal longitude for a tropical [longitude] at [jde]. */
    fun toSidereal(longitude: Double, jde: Double): Double =
        SolarLunar.norm360(longitude - lahiri(jde))
}
