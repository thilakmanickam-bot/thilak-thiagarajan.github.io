package com.astrochart.core.panchangam

import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The year names are the one thing in this app a reader can catch us out on:
 * they open a printed almanac, and either it says what we say or it doesn't.
 * So every assertion here is pinned to a published date rather than to the
 * implementation, and the two reckonings are checked where they disagree.
 *
 * Published Ugadi dates (Chaitra shukla pratipada, India): 2020-03-25,
 * 2021-04-13, 2022-04-02, 2023-03-22, 2024-04-09, 2025-03-30, 2026-03-19,
 * 2027-04-07.
 */
class HinduYearTest {

    // Chennai — the almanac these dates come from is published for India.
    private val lat = 13.0827
    private val lon = 80.2707
    private val zone: ZoneId = ZoneId.of("Asia/Kolkata")

    // --- The 60-year cycle -------------------------------------------------

    @Test
    fun theCycleHasSixtyDistinctNames() {
        assertEquals(60, HinduYear.SAMVATSARA_NAMES.size)
        assertEquals(60, HinduYear.SAMVATSARA_NAMES.toSet().size)
    }

    @Test
    fun knownYearsGetTheirPublishedNames() {
        // Anchored at both ends of a 60-year span, so an off-by-one anywhere
        // in the list or the epoch breaks at least one of these.
        val expected = mapOf(
            1987 to "Prabhava",
            2018 to "Vilambi",
            2019 to "Vikari",
            2020 to "Sharvari",
            2021 to "Plava",
            2022 to "Shubhakrit",
            2023 to "Shobhakrit",
            2024 to "Krodhi",
            2025 to "Vishvavasu",
            2026 to "Parabhava",
            2046 to "Akshaya",
            2047 to "Prabhava"
        )
        expected.forEach { (startYear, name) ->
            assertEquals(name, HinduYear.samvatsaraName(startYear), "year $startYear")
        }
    }

    @Test
    fun theTraditionalGroupingFallsWhereItShould() {
        // Prabhava…Vyaya to Brahma, Sarvajit…Parabhava to Vishnu, the rest to
        // Shiva — an independent constraint on positions 1, 20, 21 and 40.
        assertEquals("Prabhava", HinduYear.SAMVATSARA_NAMES[0])
        assertEquals("Vyaya", HinduYear.SAMVATSARA_NAMES[19])
        assertEquals("Sarvajit", HinduYear.SAMVATSARA_NAMES[20])
        assertEquals("Parabhava", HinduYear.SAMVATSARA_NAMES[39])
    }

    @Test
    fun theCycleRepeatsEverySixtyYearsAndNeverSooner() {
        for (y in 1900..2100) {
            assertEquals(HinduYear.samvatsaraName(y), HinduYear.samvatsaraName(y + 60), "y=$y")
        }
        // 60 distinct names over any 60 consecutive years.
        assertEquals(60, (2000..2059).map { HinduYear.samvatsaraName(it) }.toSet().size)
    }

    @Test
    fun theIndexIsAlwaysInRange() {
        for (y in 1800..2200) {
            val i = HinduYear.samvatsaraIndex(y)
            assertTrue(i in 1..60, "year $y gave index $i")
        }
    }

    // --- Year boundaries ---------------------------------------------------

    @Test
    fun ugadiMatchesThePublishedDates() {
        val published = mapOf(
            2020 to LocalDate.of(2020, 3, 25),
            2021 to LocalDate.of(2021, 4, 13),
            2022 to LocalDate.of(2022, 4, 2),
            2023 to LocalDate.of(2023, 3, 22),
            2024 to LocalDate.of(2024, 4, 9),
            2025 to LocalDate.of(2025, 3, 30),
            2026 to LocalDate.of(2026, 3, 19),
            2027 to LocalDate.of(2027, 4, 7)
        )
        published.forEach { (year, date) ->
            assertEquals(date, HinduYear.ugadi(year, lat, lon, zone), "Ugadi $year")
        }
    }

    @Test
    fun meshaSankrantiLandsOnTheTamilNewYear() {
        // Puthandu 2026 is published as 14 April; the date has sat on 13–15
        // April for the whole modern era, so assert the window as well as the
        // one date we have a citation for.
        assertEquals(LocalDate.of(2026, 4, 14), HinduYear.meshaSankranti(2026, lat, lon, zone))
        for (year in 2018..2030) {
            val d = HinduYear.meshaSankranti(year, lat, lon, zone)
            assertEquals(4, d.monthValue, "Mesha sankranti $year fell in month ${d.monthValue}")
            assertTrue(d.dayOfMonth in 13..15, "Mesha sankranti $year was $d")
        }
    }

    // --- Where the two reckonings disagree ---------------------------------

    @Test
    fun tamilAndTeluguDisagreeBetweenUgadiAndPuthandu() {
        // 26 March 2026 sits in the gap. A Tamil almanac prints Panguni of
        // Vishvavasu; a Telugu one has already turned to Parabhava. Getting
        // this backwards is the single most visible error this file can make.
        val inTheGap = LocalDate.of(2026, 3, 26)
        assertEquals(
            "Vishvavasu",
            HinduYear.samvatsara(inTheGap, lat, lon, zone, HinduYear.YearReckoning.SOLAR)
        )
        assertEquals(
            "Parabhava",
            HinduYear.samvatsara(inTheGap, lat, lon, zone, HinduYear.YearReckoning.LUNAR)
        )
    }

    @Test
    fun bothReckoningsAgreeOnceBothYearsHaveTurned() {
        val afterBoth = LocalDate.of(2026, 6, 1)
        HinduYear.YearReckoning.entries.forEach { r ->
            assertEquals("Parabhava", HinduYear.samvatsara(afterBoth, lat, lon, zone, r), "$r")
        }
        // And before either has turned, both still read the previous year.
        val beforeBoth = LocalDate.of(2026, 2, 1)
        HinduYear.YearReckoning.entries.forEach { r ->
            assertEquals("Vishvavasu", HinduYear.samvatsara(beforeBoth, lat, lon, zone, r), "$r")
        }
    }

    @Test
    fun theSolarYearTurnsOnPuthanduNotNewYearsDay() {
        // Margazhi runs across 31 December, so a naive "use date.year" would
        // roll the name over a week early. It must not.
        val lateDecember = LocalDate.of(2025, 12, 28)
        val earlyJanuary = LocalDate.of(2026, 1, 4)
        val solar = HinduYear.YearReckoning.SOLAR
        assertEquals(
            HinduYear.samvatsara(lateDecember, lat, lon, zone, solar),
            HinduYear.samvatsara(earlyJanuary, lat, lon, zone, solar)
        )
        assertEquals("Vishvavasu", HinduYear.samvatsara(earlyJanuary, lat, lon, zone, solar))
    }

    // --- Lunar month, season, ayana ---------------------------------------

    @Test
    fun ugadiIsTheFirstDayOfChaitra() {
        val ugadi = HinduYear.ugadi(2026, lat, lon, zone)
        assertEquals(0, HinduYear.lunarMonth(ugadi, lat, lon, zone).index)
        // …and the day before it is still Phalguna, the last month.
        assertEquals(11, HinduYear.lunarMonth(ugadi.minusDays(1), lat, lon, zone).index)
    }

    @Test
    fun theLunarMonthAdvancesOnceThroughTheYear() {
        // Sampling the 1st of each month from Ugadi must visit months in
        // ascending order without skipping — a wrong new-moon bracket shows up
        // here as a month that jumps or goes backwards.
        var previous = -1
        var seen = 0
        var d = HinduYear.ugadi(2026, lat, lon, zone)
        while (d.isBefore(LocalDate.of(2027, 3, 1))) {
            val m = HinduYear.lunarMonth(d, lat, lon, zone).index
            assertTrue(m in 0..11, "month index $m on $d")
            if (m != previous) {
                assertTrue(previous == -1 || m == previous + 1, "jumped $previous -> $m on $d")
                previous = m
                seen++
            }
            d = d.plusDays(7)
        }
        assertTrue(seen >= 11, "only saw $seen distinct lunar months in a year")
    }

    @Test
    fun seasonsFollowTheLunarMonthsInPairs() {
        assertEquals(HinduYear.Ritu.VASANTA, HinduYear.ritu(0))   // Chaitra
        assertEquals(HinduYear.Ritu.VASANTA, HinduYear.ritu(1))   // Vaishakha
        assertEquals(HinduYear.Ritu.GRISHMA, HinduYear.ritu(2))
        assertEquals(HinduYear.Ritu.VARSHA, HinduYear.ritu(4))
        assertEquals(HinduYear.Ritu.SHARAD, HinduYear.ritu(6))
        assertEquals(HinduYear.Ritu.HEMANTA, HinduYear.ritu(8))
        assertEquals(HinduYear.Ritu.SHISHIRA, HinduYear.ritu(10))
        assertEquals(HinduYear.Ritu.SHISHIRA, HinduYear.ritu(11))  // Phalguna
    }

    @Test
    fun ayanaTurnsAtMakaraAndKarkaSankranti() {
        // Makara sankranti (~14 Jan) begins Uttarayana; Karka (~16 Jul) begins
        // Dakshinayana. Sample either side of both, well clear of the ingress.
        val u = HinduYear.Ayana.UTTARAYANA
        val d = HinduYear.Ayana.DAKSHINAYANA
        assertEquals(d, HinduYear.ayana(LocalDate.of(2026, 1, 5), lat, lon, zone))
        assertEquals(u, HinduYear.ayana(LocalDate.of(2026, 1, 25), lat, lon, zone))
        assertEquals(u, HinduYear.ayana(LocalDate.of(2026, 6, 20), lat, lon, zone))
        assertEquals(d, HinduYear.ayana(LocalDate.of(2026, 8, 1), lat, lon, zone))
        assertEquals(d, HinduYear.ayana(LocalDate.of(2026, 12, 20), lat, lon, zone))
    }

    // --- Fortnight and tithi ----------------------------------------------

    @Test
    fun pakshaSplitsTheMonthInHalf() {
        assertEquals(HinduYear.Paksha.SHUKLA, HinduYear.paksha(0))
        assertEquals(HinduYear.Paksha.SHUKLA, HinduYear.paksha(14))
        assertEquals(HinduYear.Paksha.KRISHNA, HinduYear.paksha(15))
        assertEquals(HinduYear.Paksha.KRISHNA, HinduYear.paksha(29))
    }

    @Test
    fun tithiNumbersRestartEachFortnight() {
        assertEquals(1, HinduYear.tithiInPaksha(0))    // Shukla pratipada
        assertEquals(15, HinduYear.tithiInPaksha(14))  // Pournami
        assertEquals(1, HinduYear.tithiInPaksha(15))   // Krishna pratipada
        assertEquals(15, HinduYear.tithiInPaksha(29))  // Amavasai
    }

    @Test
    fun ugadiCarriesShuklaPratipadaOrIsTheDayItBegins() {
        // The day is named for the first tithi of the bright half, but a tithi
        // is only *on average* a day long: it can open after one sunrise and
        // close before the next, touching no sunrise at all. 2026 is exactly
        // that — the new moon lands at 06:54 IST, 38 minutes after sunrise,
        // and pratipada is gone by the next one. The convention then gives the
        // name to the day it began on, which is why the published date is the
        // 19th and a naive "first sunrise in pratipada" answers the 20th.
        //
        // So the invariant is not "pratipada at sunrise" but: pratipada is
        // either running at Ugadi's sunrise, or it begins during Ugadi.
        for (year in 2020..2027) {
            val ugadi = HinduYear.ugadi(year, lat, lon, zone)
            val (atSunrise, _) = Panchangam.tithiNakshatraAtSunrise(ugadi, lat, lon, zone)
            val (nextSunrise, _) =
                Panchangam.tithiNakshatraAtSunrise(ugadi.plusDays(1), lat, lon, zone)
            // Running at sunrise, or begun since the previous sunrise: either
            // way the tithi at Ugadi's own sunrise is amavasya or pratipada,
            // never anything later.
            assertTrue(
                atSunrise == 0 || atSunrise == 29,
                "Ugadi $year ($ugadi) had tithi index $atSunrise at sunrise"
            )
            if (atSunrise == 29) {
                // The kshaya case: pratipada opened after this sunrise, so by
                // the next one the month is already under way.
                assertTrue(
                    nextSunrise <= 1,
                    "Ugadi $year ($ugadi) was amavasya at sunrise but the next " +
                        "sunrise was tithi $nextSunrise, too late to be Chaitra's start"
                )
            }
        }
    }
}
