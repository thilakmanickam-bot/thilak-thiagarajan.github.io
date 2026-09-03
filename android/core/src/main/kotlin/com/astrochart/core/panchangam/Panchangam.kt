package com.astrochart.core.panchangam

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.floor

/** A start–end window on the clock (e.g. rahu kalam). */
data class PanchangamSegment(val start: LocalTime, val end: LocalTime)

/**
 * One panchangam element active at sunrise: its 0-based [index], the clock time
 * it ends, and whether that end falls on a later date.
 */
data class PanchangamElement(
    val index: Int,
    val endsAt: LocalTime,
    val endsNextDay: Boolean
)

/** A full day's panchangam. Names are resolved from the indices via [PanchangamNames]. */
data class DayPanchangam(
    val date: LocalDate,
    val weekdayIndex: Int,          // 0 = Sunday … 6 = Saturday
    val sunrise: LocalTime?,
    val sunset: LocalTime?,
    val tithi: PanchangamElement,   // index 0..29
    val nakshatra: PanchangamElement, // index 0..26
    val yoga: PanchangamElement,    // index 0..26
    val karanaHalf0: Int,           // 0..59, name via PanchangamNames.karanaName
    val karanaEndsAt: LocalTime,
    val tamilMonthIndex: Int,       // 0 = Chithirai … 11 = Panguni
    val tamilDay: Int,              // 1-based day of the Tamil month
    val rahuKalam: PanchangamSegment?,
    val yamagandam: PanchangamSegment?,
    val gulikai: PanchangamSegment?,
    val abhijit: PanchangamSegment?,
    val brahmaMuhurta: PanchangamSegment?
)

/**
 * Computes the almanac panchangam for a date and location. Elements are read at
 * sunrise (the panchangam day boundary) from the sidereal Sun/Moon, and their
 * end times are found by root-finding on the same ephemeris. Rahu/Yamagandam/
 * Gulikai use the standard weekday eighths of daylight. Verified against a
 * published Tamil almanac (Chennai, 2026-08-26).
 */
object Panchangam {

    private const val NAK_SIZE = 360.0 / 27.0

    // Part number (1..8 of daylight) of each inauspicious period, by weekday
    // (0 = Sunday). Standard South-Indian tables.
    private val RAHU_PART = intArrayOf(8, 2, 7, 5, 6, 4, 3)
    private val YAMA_PART = intArrayOf(5, 4, 3, 2, 1, 7, 6)
    private val GULIKAI_PART = intArrayOf(7, 6, 5, 4, 3, 2, 1)

    fun compute(date: LocalDate, latDeg: Double, lonEastDeg: Double, zone: ZoneId): DayPanchangam {
        val year = date.year
        val sun = SunTimes.compute(date, latDeg, lonEastDeg, zone)
        val sampleJd = sun.sunriseJdUt ?: sun.solarNoonJdUt

        val jde = SolarLunar.toJde(sampleJd, year)
        val sunL = SolarLunar.sunApparentLongitude(jde)
        val moonL = SolarLunar.moonLongitude(jde)
        val ayan = Ayanamsa.lahiri(jde)
        val elong = SolarLunar.norm360(moonL - sunL)
        val sidMoon = SolarLunar.norm360(moonL - ayan)
        val sidSun = SolarLunar.norm360(sunL - ayan)

        val tithi0 = floor(elong / 12.0).toInt().coerceIn(0, 29)
        val nak0 = floor(sidMoon / NAK_SIZE).toInt().coerceIn(0, 26)
        val yoga0 = floor(SolarLunar.norm360(sidMoon + sidSun) / NAK_SIZE).toInt().coerceIn(0, 26)
        val kar60 = floor(elong / 6.0).toInt().coerceIn(0, 59)

        val tithiEnd = crossTime(sampleJd, year, 12.0) { j ->
            SolarLunar.norm360(SolarLunar.moonLongitude(j) - SolarLunar.sunApparentLongitude(j))
        }
        val nakEnd = crossTime(sampleJd, year, NAK_SIZE) { j ->
            Ayanamsa.toSidereal(SolarLunar.moonLongitude(j), j)
        }
        val yogaEnd = crossTime(sampleJd, year, NAK_SIZE) { j ->
            SolarLunar.norm360(
                Ayanamsa.toSidereal(SolarLunar.moonLongitude(j), j) +
                    Ayanamsa.toSidereal(SolarLunar.sunApparentLongitude(j), j)
            )
        }
        val karanaEnd = crossTime(sampleJd, year, 6.0) { j ->
            SolarLunar.norm360(SolarLunar.moonLongitude(j) - SolarLunar.sunApparentLongitude(j))
        }

        val (tamilMonth, tamilDay) = tamilDate(date, latDeg, lonEastDeg, zone)

        return DayPanchangam(
            date = date,
            weekdayIndex = date.dayOfWeek.value % 7, // Mon=1..Sun=7 -> Sun=0..Sat=6
            sunrise = sun.sunrise(),
            sunset = sun.sunset(),
            tithi = element(tithi0, tithiEnd, date, zone),
            nakshatra = element(nak0, nakEnd, date, zone),
            yoga = element(yoga0, yogaEnd, date, zone),
            karanaHalf0 = kar60,
            karanaEndsAt = SunEvents.localTime(karanaEnd, zone),
            tamilMonthIndex = tamilMonth,
            tamilDay = tamilDay,
            rahuKalam = daylightPart(sun, RAHU_PART, date),
            yamagandam = daylightPart(sun, YAMA_PART, date),
            gulikai = daylightPart(sun, GULIKAI_PART, date),
            abhijit = abhijit(sun),
            brahmaMuhurta = brahmaMuhurta(sun, zone)
        )
    }

    private fun element(index: Int, endJdUt: Double, date: LocalDate, zone: ZoneId): PanchangamElement {
        val zdt = ZonedDateTime.ofInstant(SunEvents.instant(endJdUt), zone)
        return PanchangamElement(
            index = index,
            endsAt = zdt.toLocalTime(),
            endsNextDay = zdt.toLocalDate().isAfter(date)
        )
    }

    /**
     * The UT Julian Day at which the quantity produced by [raw] (a longitude in
     * degrees, of period 360) next reaches the multiple of [size] above its
     * value at [startJdUt]. Brackets in 1-hour steps (up to 60 h) then bisects.
     */
    private fun crossTime(startJdUt: Double, year: Int, size: Double, raw: (Double) -> Double): Double {
        fun near(jdUt: Double, target: Double): Double {
            var x = raw(SolarLunar.toJde(jdUt, year))
            while (x < target - 180.0) x += 360.0
            while (x > target + 180.0) x -= 360.0
            return x
        }
        val base = raw(SolarLunar.toJde(startJdUt, year))
        val target = (floor(base / size) + 1.0) * size

        var hi = startJdUt
        var bracketed = false
        var step = 0
        while (step < 60) {
            hi += 1.0 / 24.0
            if (near(hi, target) >= target) { bracketed = true; break }
            step++
        }
        if (!bracketed) return hi
        var lo = hi - 1.0 / 24.0
        repeat(40) {
            val mid = (lo + hi) / 2.0
            if (near(mid, target) >= target) hi = mid else lo = mid
        }
        return (lo + hi) / 2.0
    }

    private fun daylightPart(sun: SunEvents, table: IntArray, date: LocalDate): PanchangamSegment? {
        val rise = sun.sunriseJdUt ?: return null
        val set = sun.sunsetJdUt ?: return null
        val part = (set - rise) / 8.0
        val p = table[date.dayOfWeek.value % 7]
        val startJd = rise + (p - 1) * part
        val endJd = rise + p * part
        return PanchangamSegment(SunEvents.localTime(startJd, sun.zone), SunEvents.localTime(endJd, sun.zone))
    }

    /** Abhijit muhurta: the 8th of 15 equal day-parts, centred on solar noon. */
    private fun abhijit(sun: SunEvents): PanchangamSegment? {
        val rise = sun.sunriseJdUt ?: return null
        val set = sun.sunsetJdUt ?: return null
        val unit = (set - rise) / 15.0
        return PanchangamSegment(
            SunEvents.localTime(rise + 7 * unit, sun.zone),
            SunEvents.localTime(rise + 8 * unit, sun.zone)
        )
    }

    /** Brahma muhurta: 96 to 48 minutes before sunrise. */
    private fun brahmaMuhurta(sun: SunEvents, zone: ZoneId): PanchangamSegment? {
        val rise = sun.sunriseJdUt ?: return null
        val minute = 1.0 / 1440.0
        return PanchangamSegment(
            SunEvents.localTime(rise - 96 * minute, zone),
            SunEvents.localTime(rise - 48 * minute, zone)
        )
    }

    /**
     * Tamil solar month (0 = Chithirai) and 1-based day. The month is the Sun's
     * sidereal sign at sunrise; the day counts sunrises since the sign ingress
     * (the first sunrise in the sign is day 1).
     */
    fun tamilDate(date: LocalDate, latDeg: Double, lonEastDeg: Double, zone: ZoneId): Pair<Int, Int> {
        val todaySign = sidSunSignAtSunrise(date, latDeg, lonEastDeg, zone)
        var day = 1
        var probe = date
        var guard = 0
        while (guard < 40) {
            val prev = probe.minusDays(1)
            if (sidSunSignAtSunrise(prev, latDeg, lonEastDeg, zone) != todaySign) break
            probe = prev
            day++
            guard++
        }
        return todaySign to day
    }

    /** The (tithi index 0–29, nakshatra index 0–26) at a UT Julian Day. */
    fun tithiNakshatraAtJd(jdUt: Double, year: Int): Pair<Int, Int> {
        val jde = SolarLunar.toJde(jdUt, year)
        val sunL = SolarLunar.sunApparentLongitude(jde)
        val moonL = SolarLunar.moonLongitude(jde)
        val elong = SolarLunar.norm360(moonL - sunL)
        val sidMoon = Ayanamsa.toSidereal(moonL, jde)
        val tithi0 = floor(elong / 12.0).toInt().coerceIn(0, 29)
        val nak0 = floor(sidMoon / NAK_SIZE).toInt().coerceIn(0, 26)
        return tithi0 to nak0
    }

    /**
     * The (rasi index 0–11, nakshatra index 0–26) of the sidereal Moon at a UT
     * Julian Day — used to derive a birth chart's Vedic Moon sign/nakshatra
     * (e.g. for [com.astrochart.core.models.NatalChart.birthData]), since
     * [NatalChart]'s own tropical planet positions aren't sidereal-corrected.
     */
    fun moonRasiAndNakshatraAtJd(jdUt: Double, year: Int): Pair<Int, Int> {
        val jde = SolarLunar.toJde(jdUt, year)
        val moonL = SolarLunar.moonLongitude(jde)
        val sidMoon = Ayanamsa.toSidereal(moonL, jde)
        val rasi0 = floor(sidMoon / 30.0).toInt().coerceIn(0, 11)
        val nak0 = floor(sidMoon / NAK_SIZE).toInt().coerceIn(0, 26)
        return rasi0 to nak0
    }

    /**
     * The same (rasi, nakshatra) as [moonRasiAndNakshatraAtJd], taken from a
     * wall-clock birth date/time and the zone it was recorded in.
     *
     * Latitude and longitude are deliberately absent: the sidereal Moon's
     * longitude is a function of the instant alone, so the birthplace only
     * matters here insofar as it fixes the zone. That is what lets marriage
     * matching derive a rasi and nakshatram without computing a whole chart.
     */
    fun moonRasiAndNakshatra(local: LocalDateTime, zone: ZoneId): Pair<Int, Int> {
        val utc = local.atZone(zone).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()
        val jdUt = SolarLunar.julianDayUt(utc.toLocalDate(), utc.hour + utc.minute / 60.0)
        return moonRasiAndNakshatraAtJd(jdUt, utc.year)
    }

    /**
     * (tithi, nakshatra) prevailing at sunrise — a lightweight read used to scan
     * a month for vratham days, without the heavier work of [compute].
     */
    fun tithiNakshatraAtSunrise(date: LocalDate, latDeg: Double, lonEastDeg: Double, zone: ZoneId): Pair<Int, Int> {
        val sun = SunTimes.compute(date, latDeg, lonEastDeg, zone)
        return tithiNakshatraAtJd(sun.sunriseJdUt ?: sun.solarNoonJdUt, date.year)
    }

    private fun sidSunSignAtSunrise(date: LocalDate, latDeg: Double, lonEastDeg: Double, zone: ZoneId): Int {
        val sun = SunTimes.compute(date, latDeg, lonEastDeg, zone)
        val jd = sun.sunriseJdUt ?: sun.solarNoonJdUt
        val jde = SolarLunar.toJde(jd, date.year)
        val sid = Ayanamsa.toSidereal(SolarLunar.sunApparentLongitude(jde), jde)
        return (sid / 30.0).toInt() % 12
    }
}
