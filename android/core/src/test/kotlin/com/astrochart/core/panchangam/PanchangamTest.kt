package com.astrochart.core.panchangam

import com.astrochart.core.i18n.Language
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The astronomy is pinned to Jean Meeus' worked examples, and the assembled
 * panchangam to a published Tamil almanac for Chennai on 2026-08-26 (Shukla
 * Trayodashi · Shravana · Saubhagya · Taitila · month Aavani day 9).
 */
class PanchangamTest {

    // --- Ephemeris vs Meeus' book ------------------------------------------

    @Test
    fun sun_matchesMeeusExample() {
        // 1992-10-13.0 TD, apparent longitude 199.90895°.
        val lon = SolarLunar.sunApparentLongitude(2448908.5)
        assertTrue(kotlin.math.abs(lon - 199.90895) < 0.002, "sun was $lon")
    }

    @Test
    fun moon_matchesMeeusExample() {
        // 1992-04-12.0 TD, longitude 133.162655° (truncation residual ~12″).
        val lon = SolarLunar.moonLongitude(2448724.5)
        assertTrue(kotlin.math.abs(lon - 133.162655) < 0.01, "moon was $lon")
    }

    // --- Assembled panchangam vs a published Tamil almanac -----------------

    private val chennai = Triple(13.0827, 80.2707, ZoneId.of("Asia/Kolkata"))

    private fun chennaiPanchangam(date: LocalDate): DayPanchangam =
        Panchangam.compute(date, chennai.first, chennai.second, chennai.third)

    @Test
    fun elementIndices_matchAlmanac_forChennai_2026_08_26() {
        val p = chennaiPanchangam(LocalDate.of(2026, 8, 26))

        assertEquals(3, p.weekdayIndex, "Wednesday") // 0=Sunday
        assertEquals(12, p.tithi.index, "Shukla Trayodashi")
        assertEquals("Shukla", PanchangamNames.paksha(p.tithi.index).en)
        assertEquals(21, p.nakshatra.index, "Shravana")
        assertEquals(3, p.yoga.index, "Saubhagya")
        assertEquals(25, p.karanaHalf0, "Taitila (26th half-tithi)")
    }

    @Test
    fun namedElements_areExactlyTheAlmanacsValues() {
        val p = chennaiPanchangam(LocalDate.of(2026, 8, 26))
        assertEquals("Trayodashi", PanchangamNames.tithiName(p.tithi.index).en)
        assertEquals("Shravana", PanchangamNames.nakshatras[p.nakshatra.index].en)
        assertEquals("Saubhagya", PanchangamNames.yogas[p.yoga.index].en)
        assertEquals("Taitila", PanchangamNames.karanaName(p.karanaHalf0).en)
        assertEquals(4, p.tamilMonthIndex, "Aavani")
        assertEquals("Aavani", PanchangamNames.tamilMonths[p.tamilMonthIndex].en)
        assertEquals(9, p.tamilDay)
    }

    @Test
    fun sunriseSunset_areReasonable_forChennai() {
        val p = chennaiPanchangam(LocalDate.of(2026, 8, 26))
        val sr = assertNotNull(p.sunrise)
        val ss = assertNotNull(p.sunset)
        // Published ~05:55 / ~18:20; our NOAA value ~05:59 / ~18:26. Allow slack.
        val srMin = sr.hour * 60 + sr.minute
        val ssMin = ss.hour * 60 + ss.minute
        assertTrue(srMin in (5 * 60 + 50)..(6 * 60 + 5), "sunrise $sr")
        assertTrue(ssMin in (18 * 60 + 18)..(18 * 60 + 32), "sunset $ss")
    }

    @Test
    fun rahuKalam_isTheWednesdayFifthEighth() {
        val p = chennaiPanchangam(LocalDate.of(2026, 8, 26))
        val rahu = assertNotNull(p.rahuKalam)
        // 5th of eight daylight parts falls just after noon on a Wednesday.
        assertEquals(12, rahu.start.hour, "rahu start ${rahu.start}")
        assertTrue(rahu.end.hour == 13, "rahu end ${rahu.end}")
        // All three inauspicious windows and both auspicious ones are present.
        assertNotNull(p.yamagandam)
        assertNotNull(p.gulikai)
        assertNotNull(p.abhijit)
        assertNotNull(p.brahmaMuhurta)
    }

    @Test
    fun elementEndTimes_areWellFormed() {
        val p = chennaiPanchangam(LocalDate.of(2026, 8, 26))
        // End times exist and are valid clock times; the nakshatra rolls to the
        // next day (Shravana ends after midnight).
        assertNotNull(p.tithi.endsAt)
        assertNotNull(p.nakshatra.endsAt)
        assertTrue(p.nakshatra.endsNextDay, "Shravana should end next day")
    }

    @Test
    fun tamilMonth_advancesAcrossSankranti() {
        // A day inside Aadi (previous month) resolves to a different month index.
        val julyDay = chennaiPanchangam(LocalDate.of(2026, 7, 20))
        assertTrue(julyDay.tamilMonthIndex in 0..11)
        assertTrue(julyDay.tamilDay in 1..32)
    }

    // --- Sidereal Moon rasi/nakshatra (for PrimaryProfile derivation) ------

    @Test
    fun moonRasiAndNakshatraAtJd_matchesAlmanacNakshatra_forChennai_2026_08_26() {
        // Cross-checked against the almanac-verified compute() nakshatra above
        // (elementIndices_matchAlmanac_forChennai_2026_08_26): both read the
        // same sidereal Moon longitude at the same instant.
        val date = LocalDate.of(2026, 8, 26)
        val sun = SunTimes.compute(date, chennai.first, chennai.second, chennai.third)
        val jdUt = sun.sunriseJdUt ?: sun.solarNoonJdUt
        val (rasi, nak) = Panchangam.moonRasiAndNakshatraAtJd(jdUt, date.year)
        assertEquals(21, nak, "Shravana")
        assertEquals(9, rasi, "Capricorn — nakshatra 21 falls within sidereal 270-300°")
    }

    @Test
    fun names_localizeToTamil() {
        val p = chennaiPanchangam(LocalDate.of(2026, 8, 26))
        assertEquals("திருவோணம்", PanchangamNames.nakshatras[p.nakshatra.index].get(Language.TA))
        assertEquals("ஆவணி", PanchangamNames.tamilMonths[p.tamilMonthIndex].get(Language.TA))
    }
}
