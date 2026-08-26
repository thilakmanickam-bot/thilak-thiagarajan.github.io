package com.astrochart.core.panchangam

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin

/**
 * Sunrise, sunset and solar noon for a date and location, via the standard
 * low-precision "sunrise equation" (accurate to ~1 minute at ordinary
 * latitudes — verified against published Chennai timings). Events are returned
 * both as UT Julian Days (for the panchangam's boundary maths) and, through the
 * helpers, as wall-clock [LocalTime] in the supplied [ZoneId], so DST is handled
 * by the JDK. [sunriseJdUt]/[sunsetJdUt] are null in polar day/night.
 */
data class SunEvents(
    val date: LocalDate,
    val zone: ZoneId,
    val sunriseJdUt: Double?,
    val sunsetJdUt: Double?,
    val solarNoonJdUt: Double
) {
    fun sunrise(): LocalTime? = sunriseJdUt?.let { localTime(it, zone) }
    fun sunset(): LocalTime? = sunsetJdUt?.let { localTime(it, zone) }
    fun solarNoon(): LocalTime = localTime(solarNoonJdUt, zone)

    /** Daylight length in fractional hours, or null when there is no rise/set. */
    fun daylightHours(): Double? {
        val r = sunriseJdUt ?: return null
        val s = sunsetJdUt ?: return null
        return (s - r) * 24.0
    }

    companion object {
        fun instant(jdUt: Double): Instant =
            Instant.ofEpochSecond(((jdUt - 2440587.5) * 86400.0).roundToLong())

        fun localTime(jdUt: Double, zone: ZoneId): LocalTime =
            ZonedDateTime.ofInstant(instant(jdUt), zone).toLocalTime()
    }
}

object SunTimes {

    private const val ZENITH_CORRECTION = -0.833 // refraction + solar semidiameter

    fun compute(date: LocalDate, latDeg: Double, lonEastDeg: Double, zone: ZoneId): SunEvents {
        val lw = -lonEastDeg // west longitude, positive west
        val jdApproxNoon = SolarLunar.julianDayUt(date, 12.0)
        val n = ((jdApproxNoon - 2451545.0 - 0.0009) - (lw / 360.0)).roundToLong().toDouble()
        val jStar = 2451545.0 + 0.0009 + (lw / 360.0) + n
        val m = SolarLunar.norm360(357.5291 + 0.98560028 * (jStar - 2451545.0))
        val mr = Math.toRadians(m)
        val c = 1.9148 * sin(mr) + 0.0200 * sin(2 * mr) + 0.0003 * sin(3 * mr)
        val lambda = SolarLunar.norm360(m + c + 180.0 + 102.9372)
        val jTransit = jStar + 0.0053 * sin(mr) - 0.0069 * sin(Math.toRadians(2 * lambda))

        val sinDec = sin(Math.toRadians(lambda)) * sin(Math.toRadians(23.44))
        val dec = asin(sinDec)
        val cosH = (sin(Math.toRadians(ZENITH_CORRECTION)) - sin(Math.toRadians(latDeg)) * sinDec) /
            (cos(Math.toRadians(latDeg)) * cos(dec))

        return if (abs(cosH) > 1.0) {
            SunEvents(date, zone, null, null, jTransit)
        } else {
            val h = Math.toDegrees(acos(cosH))
            SunEvents(
                date = date,
                zone = zone,
                sunriseJdUt = jTransit - h / 360.0,
                sunsetJdUt = jTransit + h / 360.0,
                solarNoonJdUt = jTransit
            )
        }
    }
}
