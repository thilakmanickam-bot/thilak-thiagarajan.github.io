package com.astrochart.core.panchangam

import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.floor

/**
 * The coordinates a traditional sankalpa recites to locate an intention in
 * time: the year (samvatsara), the Sun's half-year (ayana), the season (ritu),
 * the lunar month (masa), the fortnight (paksha) and the lunar day (tithi).
 *
 * Everything here is derived, never tabulated — there is no year table to go
 * stale. Two reckonings are supported because South India genuinely uses two,
 * and they disagree for several weeks every spring:
 *
 *  - [YearReckoning.SOLAR] turns the year at Mesha sankranti, the Sun's entry
 *    into sidereal Aries (Tamil Puthandu, ~14 April).
 *  - [YearReckoning.LUNAR] turns it at Chaitra shukla pratipada, the first day
 *    of the lunar month Chaitra (Ugadi / Gudi Padwa, ~19 March in 2026).
 *
 * In 2026 that gap runs 19 March – 13 April: a Telugu almanac reads Parabhava
 * while a Tamil one still reads Vishvavasu. Both are correct in their own
 * reckoning, so callers must pass the one their reader uses rather than let the
 * app pick — see the verification note on [SAMVATSARA_NAMES].
 */
object HinduYear {

    /**
     * The 60-year cycle, in plain transliteration (the user's choice over IAST
     * diacritics), index 0 = Prabhava = samvatsara #1.
     *
     * **Verified, not recalled.** The ordering and the epoch below are pinned by
     * four independent facts that must all hold simultaneously, and do:
     *
     *  1. Prabhava (#1) is the year beginning in 1987.
     *  2. Parabhava (#40) is the year beginning in 2026.
     *  3. Krodhi (#38) is 2024-25 and Vishvavasu (#39) is 2025-26.
     *  4. The traditional grouping — Prabhava…Vyaya to Brahma, Sarvajit…
     *     Parabhava to Vishnu, the rest to Shiva — puts Vyaya at #20,
     *     Sarvajit at #21 and Parabhava at #40, which this list does.
     *
     * Several names have equally attested spellings (Dhatri/Dhata,
     * Hevilambi/Hemalambi, Shobhakrit/Shobhana). The forms below are the ones
     * South Indian panchangams usually print. Spelling is cosmetic; the
     * *ordering* is what a wrong year name would come from, and it is fixed by
     * the four anchors above.
     */
    val SAMVATSARA_NAMES: List<String> = listOf(
        "Prabhava", "Vibhava", "Shukla", "Pramoduta", "Prajotpatti",
        "Angirasa", "Shrimukha", "Bhava", "Yuva", "Dhatri",
        "Ishvara", "Bahudhanya", "Pramathi", "Vikrama", "Vrisha",
        "Chitrabhanu", "Svabhanu", "Tarana", "Parthiva", "Vyaya",
        "Sarvajit", "Sarvadhari", "Virodhi", "Vikriti", "Khara",
        "Nandana", "Vijaya", "Jaya", "Manmatha", "Durmukhi",
        "Hevilambi", "Vilambi", "Vikari", "Sharvari", "Plava",
        "Shubhakrit", "Shobhakrit", "Krodhi", "Vishvavasu", "Parabhava",
        "Plavanga", "Kilaka", "Saumya", "Sadharana", "Virodhikrit",
        "Paridhavi", "Pramadi", "Ananda", "Rakshasa", "Nala",
        "Pingala", "Kalayukta", "Siddharthi", "Raudra", "Durmati",
        "Dundubhi", "Rudhirodgari", "Raktakshi", "Krodhana", "Akshaya"
    )

    /** Amanta lunar months, index 0 = Chaitra. */
    val LUNAR_MONTH_NAMES: List<String> = listOf(
        "Chaitra", "Vaishakha", "Jyeshtha", "Ashadha", "Shravana", "Bhadrapada",
        "Ashvina", "Kartika", "Margashirsha", "Pausha", "Magha", "Phalguna"
    )

    /** Which new-year boundary a reader's tradition uses. */
    enum class YearReckoning { SOLAR, LUNAR }

    /** The Sun's northward or southward half-year. */
    enum class Ayana { UTTARAYANA, DAKSHINAYANA }

    /** The six seasons, paired to the lunar months two at a time. */
    enum class Ritu { VASANTA, GRISHMA, VARSHA, SHARAD, HEMANTA, SHISHIRA }

    /** The waxing or waning fortnight. */
    enum class Paksha { SHUKLA, KRISHNA }

    /**
     * A lunar month. [isAdhika] marks an intercalary month — one containing no
     * solar ingress at all, which the calendar repeats to keep the lunar year
     * in step with the solar one.
     */
    data class LunarMonth(val index: Int, val isAdhika: Boolean) {
        val name: String get() = LUNAR_MONTH_NAMES[index]
    }

    /**
     * Samvatsara index is a pure function of the Gregorian year the samvatsara
     * *began* in. Shaka = CE − 78, and the cycle runs 12 ahead of the Shaka
     * year, so the two constants collapse to a single subtraction of 66.
     */
    private const val EPOCH_OFFSET = 66

    /** The 1-based samvatsara number (1–60) for a year beginning in [startYear]. */
    fun samvatsaraIndex(startYear: Int): Int {
        val m = Math.floorMod(startYear - EPOCH_OFFSET, 60)
        return if (m == 0) 60 else m
    }

    /** The samvatsara name for a year beginning in [startYear]. */
    fun samvatsaraName(startYear: Int): String =
        SAMVATSARA_NAMES[samvatsaraIndex(startYear) - 1]

    /**
     * The Gregorian year in which the samvatsara covering [date] began, under
     * [reckoning]. This is the value to hand [samvatsaraName].
     */
    fun yearStartGregorian(
        date: LocalDate,
        latDeg: Double,
        lonEastDeg: Double,
        zone: ZoneId,
        reckoning: YearReckoning
    ): Int {
        val boundary = when (reckoning) {
            YearReckoning.SOLAR -> meshaSankranti(date.year, latDeg, lonEastDeg, zone)
            YearReckoning.LUNAR -> ugadi(date.year, latDeg, lonEastDeg, zone)
        }
        return if (date.isBefore(boundary)) date.year - 1 else date.year
    }

    /** The samvatsara name covering [date] under [reckoning]. */
    fun samvatsara(
        date: LocalDate,
        latDeg: Double,
        lonEastDeg: Double,
        zone: ZoneId,
        reckoning: YearReckoning
    ): String = samvatsaraName(yearStartGregorian(date, latDeg, lonEastDeg, zone, reckoning))

    /**
     * Tamil New Year (Puthandu) for [year].
     *
     * **Not** the first sunrise with the Sun in Mesha. Tamil Nadu reckons a
     * solar month from the sankranti itself: if the Sun's ingress happens
     * before sunset, that day is the first of the month; if after sunset, the
     * next day is. In 2026 the ingress falls at 09:03 IST — after sunrise, so
     * a sunrise test would answer 15 April, while every almanac prints the
     * 14th.
     *
     * (Other regions cut elsewhere — Kerala at aparahna, Bengal at midnight —
     * which is why this is a Tamil-specific function rather than a general
     * "when does the solar month begin".)
     */
    fun meshaSankranti(year: Int, latDeg: Double, lonEastDeg: Double, zone: ZoneId): LocalDate {
        val ingress = meshaIngressJd(year)
        val day = SunEvents.instant(ingress).atZone(zone).toLocalDate()
        val sun = SunTimes.compute(day, latDeg, lonEastDeg, zone)
        val sunset = sun.sunsetJdUt ?: return day
        return if (ingress <= sunset) day else day.plusDays(1)
    }

    /**
     * The instant the Sun's sidereal longitude crosses 0° — bisected on the
     * 359°→0° wrap, since that is the only place the value decreases.
     */
    private fun meshaIngressJd(year: Int): Double {
        // The ingress has sat in 13-15 April for centuries and drifts about a
        // day per 70 years, so bracket generously rather than tightly.
        var lo = SolarLunar.julianDayUt(LocalDate.of(year, 3, 25), 0.0)
        var hi = SolarLunar.julianDayUt(LocalDate.of(year, 4, 30), 0.0)
        repeat(40) {
            val mid = (lo + hi) / 2.0
            if (sidSunAtJd(mid, year) > 180.0) lo = mid else hi = mid
        }
        return (lo + hi) / 2.0
    }

    /**
     * Ugadi / Gudi Padwa for [year]: the first sunrise of the lunar month
     * Chaitra, which is the month begun by the new moon that falls while the
     * Sun is in sidereal Meena.
     *
     * Found by walking new moons back from a date guaranteed to be past it,
     * rather than by testing every day — computing a lunar month costs two new
     * moon searches, so a day-by-day scan over six weeks would be some
     * thousands of lunar-position evaluations for one label.
     */
    fun ugadi(year: Int, latDeg: Double, lonEastDeg: Double, zone: ZoneId): LocalDate {
        // 20 April is always after Chaitra began (latest Ugadi is ~15 April)
        // and always before Vaishakha's new moon could displace it by two.
        var jd = SolarLunar.julianDayUt(LocalDate.of(year, 4, 20), 12.0)
        repeat(4) {
            val newMoon = newMoonAtOrBefore(jd, year)
            if (sidSunSignAtJd(newMoon, year) == MEENA) {
                return pratipadaDay(newMoon, latDeg, lonEastDeg, zone)
            }
            jd = newMoon - 1.0
        }
        error("No Chaitra new moon found for $year")
    }

    /**
     * The day that carries the shukla pratipada beginning at [newMoon].
     *
     * A lunar day is named for the tithi running at sunrise, so normally this
     * is the first sunrise inside pratipada. But a tithi is shorter than a day
     * only on average: pratipada can open after one sunrise and close before
     * the next, touching no sunrise at all. That is a *kshaya* tithi, and the
     * convention is that the day it begins on takes the name.
     *
     * 2026 is exactly that case — the new moon falls at 06:54 IST, 38 minutes
     * after sunrise, and pratipada is over before the next one — which is why
     * "first sunrise at or after the new moon" answered 20 March against a
     * published 19th.
     */
    private fun pratipadaDay(
        newMoon: Double,
        latDeg: Double,
        lonEastDeg: Double,
        zone: ZoneId
    ): LocalDate {
        val begins = SunEvents.instant(newMoon).atZone(zone).toLocalDate()
        for (offset in 0L..1L) {
            val d = begins.plusDays(offset)
            val (tithi, _) = Panchangam.tithiNakshatraAtSunrise(d, latDeg, lonEastDeg, zone)
            if (tithi == 0) return d
        }
        return begins
    }

    /**
     * Uttarayana runs from Makara sankranti to Karka sankranti — the Sun in
     * sidereal Makara, Kumbha, Meena, Mesha, Vrishabha or Mithuna.
     */
    fun ayana(date: LocalDate, latDeg: Double, lonEastDeg: Double, zone: ZoneId): Ayana {
        val sign = Panchangam.sidSunSignAtSunrise(date, latDeg, lonEastDeg, zone)
        return if (sign >= MAKARA || sign <= MITHUNA) Ayana.UTTARAYANA else Ayana.DAKSHINAYANA
    }

    /** The amanta lunar month prevailing at sunrise on [date]. */
    fun lunarMonth(
        date: LocalDate,
        latDeg: Double,
        lonEastDeg: Double,
        zone: ZoneId
    ): LunarMonth {
        val sun = SunTimes.compute(date, latDeg, lonEastDeg, zone)
        return lunarMonthAtJd(sun.sunriseJdUt ?: sun.solarNoonJdUt, date.year)
    }

    /**
     * The amanta lunar month containing [jdUt]. A month is named for the solar
     * sign the Sun stood in at the new moon that began it, shifted by one:
     * the month begun under Meena is Chaitra, under Mesha is Vaishakha, and so
     * on. When the following new moon finds the Sun still in the same sign, no
     * ingress happened inside the month and it is adhika.
     */
    fun lunarMonthAtJd(jdUt: Double, year: Int): LunarMonth {
        val start = newMoonAtOrBefore(jdUt, year)
        // A synodic month is 29.27–29.83 days, so +31 is always past the next
        // new moon and never past the one after it.
        val next = newMoonAtOrBefore(start + 31.0, year)
        val signAtStart = sidSunSignAtJd(start, year)
        return LunarMonth(
            index = (signAtStart + 1) % 12,
            isAdhika = signAtStart == sidSunSignAtJd(next, year)
        )
    }

    /** The season, which follows the lunar month two months at a time. */
    fun ritu(lunarMonthIndex: Int): Ritu = Ritu.entries[lunarMonthIndex / 2]

    /** Waxing for tithi 0–14, waning for 15–29. */
    fun paksha(tithiIndex: Int): Paksha =
        if (tithiIndex < 15) Paksha.SHUKLA else Paksha.KRISHNA

    /** The 1–15 tithi number within its own fortnight. */
    fun tithiInPaksha(tithiIndex: Int): Int = (tithiIndex % 15) + 1

    // ---- internals -------------------------------------------------------

    private const val MITHUNA = 2
    private const val MAKARA = 9
    private const val MEENA = 11

    /** Moon-minus-Sun longitude, 0 at new moon, growing to 360 over the month. */
    private fun elongation(jdUt: Double, year: Int): Double {
        val jde = SolarLunar.toJde(jdUt, year)
        return SolarLunar.norm360(
            SolarLunar.moonLongitude(jde) - SolarLunar.sunApparentLongitude(jde)
        )
    }

    /** The Sun's sidereal longitude, 0–360. */
    private fun sidSunAtJd(jdUt: Double, year: Int): Double {
        val jde = SolarLunar.toJde(jdUt, year)
        return Ayanamsa.toSidereal(SolarLunar.sunApparentLongitude(jde), jde)
    }

    private fun sidSunSignAtJd(jdUt: Double, year: Int): Int =
        floor(sidSunAtJd(jdUt, year) / 30.0).toInt() % 12

    /**
     * The instant of the last new moon at or before [jdUt].
     *
     * Steps back a day at a time until the elongation *drops* between the two
     * probes — that discontinuity is the 360→0 wrap, and so brackets the new
     * moon — then bisects. Twenty halvings of a one-day bracket land inside a
     * second, far finer than needed: all this feeds is which 30°-wide solar
     * sign the Sun was in.
     */
    private fun newMoonAtOrBefore(jdUt: Double, year: Int): Double {
        var hi = jdUt
        var lo = jdUt - 1.0
        var guard = 0
        while (guard < 32 && elongation(lo, year) <= elongation(hi, year)) {
            hi = lo
            lo -= 1.0
            guard++
        }
        repeat(20) {
            val mid = (lo + hi) / 2.0
            if (elongation(mid, year) > 180.0) lo = mid else hi = mid
        }
        return (lo + hi) / 2.0
    }

}
