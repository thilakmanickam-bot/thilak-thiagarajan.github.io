package com.astrochart.core.panchangam

import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals

/**
 * The Tamil year and month boundaries, pinned against a printed panchangam.
 *
 * These two dates look like they should follow one rule and do not, so the
 * evidence is worth restating where it will fail a build rather than only in
 * `docs/RECKONING.md`:
 *
 * | boundary  | sankranti (IST) | after sunrise | printed start |
 * |-----------|-----------------|---------------|---------------|
 * | Chithirai | 14 Apr 09:33    | 3h 35m        | **14 Apr**    |
 * | Aavani    | 17 Aug 07:59    | 2h 01m        | **18 Aug**    |
 *
 * Both transits are in the morning, after sunrise. The *later* one starts its
 * month the same day and the *earlier* one the next day, so the outcome is
 * anti-monotonic in "time after sunrise" — no cutoff of any kind produces both.
 * A uniform sunrise rule gives 15 Apr; a uniform sunset rule gives 17 Aug. Each
 * is wrong on one of the two.
 *
 * They are not the same kind of boundary. Puthandu is a **civil holiday fixed
 * to 14 April**, which the sunset rule reproduces in all of 2020–2030 (the
 * sunrise rule would move it to 15 April in five of those years); ordinary
 * months follow the drik sunrise rule.
 *
 * **So the two code paths differ on purpose, and aligning them breaks one of
 * these dates.** That is exactly the tidy-up this file exists to fail.
 */
class TamilBoundaryTest {

    private val chennaiLat = 13.0827
    private val chennaiLon = 80.2707
    private val zone: ZoneId = ZoneId.of("Asia/Kolkata")

    @Test
    fun puthanduIsTheFourteenthOfApril() {
        // Mesha sankranti is 14 Apr 09:33 IST — after sunrise (05:58) but well
        // before sunset, so the sunset rule keeps it on the 14th.
        assertEquals(
            LocalDate.of(2026, 4, 14),
            HinduYear.meshaSankranti(2026, chennaiLat, chennaiLon, zone),
            "Puthandu 2026 is 14 April in print; the sunrise rule would say 15 April"
        )
    }

    @Test
    fun puthanduStaysTheFourteenthAcrossYears() {
        // The sankranti drifts across roughly 13 Apr 20:00 to 14 Apr 15:30 over
        // this span, crossing sunrise repeatedly. Puthandu does not move, which
        // is the evidence that it is a fixed civil date rather than a drik one.
        (2020..2030).forEach { year ->
            assertEquals(
                LocalDate.of(year, 4, 14),
                HinduYear.meshaSankranti(year, chennaiLat, chennaiLon, zone),
                "Puthandu moved in $year"
            )
        }
    }

    @Test
    fun aavaniBeginsOnTheEighteenthOfAugust() {
        // Simha sankranti is 17 Aug 07:59 IST, after that morning's sunrise, so
        // the sunrise rule carries the month start to the 18th — which is what
        // the printed panchangam gives.
        val (month, day) = Panchangam.tamilDate(
            LocalDate.of(2026, 8, 18), chennaiLat, chennaiLon, zone
        )
        assertEquals(4, month, "18 Aug 2026 should be in Aavani (index 4)")
        assertEquals(1, day, "18 Aug 2026 is Aavani 1 in print")
    }

    @Test
    fun theSeventeenthOfAugustIsStillAadi() {
        // The other half of the same claim: if the boundary ever slid to the
        // sankranti's own day, this is the assertion that would catch it.
        //
        // The tightest assertion in this file. At sunrise on the 17th the
        // sidereal Sun is 119.919° — 0.081° short of Simha. The engine agrees
        // with the Swiss Ephemeris to 0.015° on the Sun and 0.005° on the
        // ayanamsa, so there is roughly 4× headroom; a change that widened the
        // Sun's error past ~0.08° would fail here first, which is the point.
        val (month, _) = Panchangam.tamilDate(
            LocalDate.of(2026, 8, 17), chennaiLat, chennaiLon, zone
        )
        assertEquals(3, month, "17 Aug 2026 is still Aadi (index 3), not Aavani")
    }

    @Test
    fun purattasiBeginsOnTheEighteenthOfSeptember() {
        // A second printed confirmation of the sunrise rule, and near enough a
        // replica of Aavani: Kanya sankranti is 17 Sep 07:53 IST against a
        // 05:59 sunrise, so the month starts on the 18th. The sunset rule would
        // say the 17th. Two independent months now pin this side, which one
        // alone did not.
        val (month, day) = Panchangam.tamilDate(
            LocalDate.of(2026, 9, 18), chennaiLat, chennaiLon, zone
        )
        assertEquals(5, month, "18 Sep 2026 should be in Purattasi (index 5)")
        assertEquals(1, day, "18 Sep 2026 is Purattasi 1 in print")
    }

    @Test
    fun theSeventeenthOfSeptemberIsStillAavani() {
        // Margin here is 0.077° — the tightest in the file, marginally ahead of
        // the 17 August case, for the same reason.
        val (month, _) = Panchangam.tamilDate(
            LocalDate.of(2026, 9, 17), chennaiLat, chennaiLon, zone
        )
        assertEquals(4, month, "17 Sep 2026 is still Aavani (index 4), not Purattasi")
    }
}
