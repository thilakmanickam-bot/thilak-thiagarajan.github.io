package com.astrochart.core.panchangam

import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/** A recurring observance and the dates it falls on within a month. */
data class VrathaDay(val key: String, val dates: List<LocalDate>)

/** Moon phase marker for a calendar cell. */
enum class MoonMark { NONE, NEW, FULL }

/**
 * Month-level panchangam scans: the vratham (observance) days and the moon-phase
 * markers for each day. Each observance is placed on the day when its defining
 * tithi (or nakshatra) prevails at sunrise — the common almanac convention.
 * Derived entirely from [Panchangam.tithiNakshatraAtSunrise]; pure and testable.
 */
object MonthPanchangam {

    // tithi index (0-based): Shukla 0..14 (14 = Purnima), Krishna 15..29 (29 = Amavasya).
    private const val PURNIMA = 14
    private const val AMAVASYA = 29
    private const val SHUKLA_CHATURTHI = 3
    private const val KRISHNA_CHATURTHI = 18
    private const val SHUKLA_SASHTI = 5
    private const val KRISHNA_SASHTI = 20
    private const val SHUKLA_EKADASHI = 10
    private const val KRISHNA_EKADASHI = 25
    private const val SHUKLA_TRAYODASHI = 12
    private const val KRISHNA_TRAYODASHI = 27
    private const val KRISHNA_CHATURDASHI = 28

    // nakshatra index (0-based).
    private const val KRITTIKA = 2
    private const val SHRAVANA = 21

    /** Observance keys, in a sensible display order. Localized by the UI. */
    val KEYS = listOf(
        "amavasai", "pournami", "ekadasi", "sashti", "chaturthi",
        "sankatahara", "pradosham", "sivarathiri", "krithigai", "thiruvonam"
    )

    /**
     * All vratham days in [month], grouped by observance. Each is placed by its
     * traditional reference time: most fall on the day their tithi/nakshatra is
     * present at **sunrise**, but the evening rites — Pournami, Pradosham, and
     * Sankatahara Chaturthi — are placed by the tithi present at **sunset**
     * (matching published Tamil almanacs). Empty groups are dropped.
     */
    fun vrathaDays(month: YearMonth, latDeg: Double, lonEastDeg: Double, zone: ZoneId): List<VrathaDay> {
        val byKey = linkedMapOf<String, MutableList<LocalDate>>()
        KEYS.forEach { byKey[it] = mutableListOf() }

        for (day in 1..month.lengthOfMonth()) {
            val date = month.atDay(day)
            val sun = SunTimes.compute(date, latDeg, lonEastDeg, zone)
            val (tRise, nRise) = Panchangam.tithiNakshatraAtJd(sun.sunriseJdUt ?: sun.solarNoonJdUt, date.year)
            val (tSet, _) = Panchangam.tithiNakshatraAtJd(sun.sunsetJdUt ?: sun.solarNoonJdUt, date.year)

            when (tRise) {
                AMAVASYA -> byKey["amavasai"]!!.add(date)
                SHUKLA_EKADASHI, KRISHNA_EKADASHI -> byKey["ekadasi"]!!.add(date)
                SHUKLA_SASHTI, KRISHNA_SASHTI -> byKey["sashti"]!!.add(date)
                SHUKLA_CHATURTHI -> byKey["chaturthi"]!!.add(date)
                KRISHNA_CHATURDASHI -> byKey["sivarathiri"]!!.add(date)
            }
            when (tSet) {
                PURNIMA -> byKey["pournami"]!!.add(date)
                KRISHNA_CHATURTHI -> byKey["sankatahara"]!!.add(date)
                SHUKLA_TRAYODASHI, KRISHNA_TRAYODASHI -> byKey["pradosham"]!!.add(date)
            }
            when (nRise) {
                KRITTIKA -> byKey["krithigai"]!!.add(date)
                SHRAVANA -> byKey["thiruvonam"]!!.add(date)
            }
        }
        return KEYS.mapNotNull { key ->
            byKey[key]?.takeIf { it.isNotEmpty() }?.let { VrathaDay(key, it) }
        }
    }

    /** Moon-phase marker for a single day: new at sunrise, full at sunset. */
    fun moonMark(date: LocalDate, latDeg: Double, lonEastDeg: Double, zone: ZoneId): MoonMark {
        val sun = SunTimes.compute(date, latDeg, lonEastDeg, zone)
        val (tRise, _) = Panchangam.tithiNakshatraAtJd(sun.sunriseJdUt ?: sun.solarNoonJdUt, date.year)
        val (tSet, _) = Panchangam.tithiNakshatraAtJd(sun.sunsetJdUt ?: sun.solarNoonJdUt, date.year)
        return when {
            tRise == AMAVASYA -> MoonMark.NEW
            tSet == PURNIMA -> MoonMark.FULL
            else -> MoonMark.NONE
        }
    }

    /** Marks for every day of the month, keyed by day-of-month (1-based). */
    fun moonMarks(month: YearMonth, latDeg: Double, lonEastDeg: Double, zone: ZoneId): Map<Int, MoonMark> {
        val result = HashMap<Int, MoonMark>()
        for (day in 1..month.lengthOfMonth()) {
            val mark = moonMark(month.atDay(day), latDeg, lonEastDeg, zone)
            if (mark != MoonMark.NONE) result[day] = mark
        }
        return result
    }
}
