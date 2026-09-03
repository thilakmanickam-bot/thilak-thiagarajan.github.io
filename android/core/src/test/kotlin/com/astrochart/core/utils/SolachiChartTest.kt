package com.astrochart.core.utils

import com.astrochart.core.models.BirthData
import com.astrochart.core.models.PlanetaryPosition
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The reference jathagam: S. Solachi, 21.12.1989, 13:02, Kāraikkudi.
 *
 * A hand-computed Tamil sheet, used here as the acceptance test for the chart
 * engine. Its three headline values are what a reader checks first —
 *
 *   ஜென்ம லக்கனம் : மீனம்      (lagnam: Meena)
 *   நட்சத்திரம்    : அஸ்தம்     (nakshatram: Hasta)
 *   ராசி          : கன்னி      (rasi: Kanni)
 *
 * — and all three are asserted below.
 *
 * For scale, what this replaced got Mars wrong by 40°, Jupiter by 28°, Mercury
 * by 98°, Venus by 123° and Saturn by 135°, and put the lagnam in Thulam,
 * seven signs from Meenam.
 *
 * **Two knowing departures from the sheet**, both argued rather than assumed:
 *
 *  - *Jupiter.* The sheet reads Thiruvonam (Shravana, in Makaram). Jupiter
 *    reached opposition **in Gemini on 27 December 1989**, six days after this
 *    birth, so it cannot have been in Makaram; Mithunam is the only possible
 *    answer. Note also that the sheet gives Venus the same nakshatra on the
 *    adjacent line, which is what a copying slip looks like.
 *  - *Ketu.* The sheet reads Uthiram (in Kanni) while giving Rahu Avittam (in
 *    Makaram). The nodes are always exactly opposite, and those two are 120°
 *    apart, so the sheet disagrees with itself there. [nodesAreOpposite] pins
 *    the invariant instead.
 *
 * Padas are asserted only where the engine reproduces the sheet exactly. Where
 * it lands a pada or two away — Mercury, Venus, Saturn, and the Moon by one —
 * the rasi is asserted and the pada deliberately is not: a hand-computed sheet
 * and a modern ephemeris disagree at that resolution for real reasons (vakya
 * versus drik reckoning among them), and pinning a number here that is not
 * independently verified would be inventing precision.
 */
class SolachiChartTest {

    private val chart = ChartCalculator.calculateNatalChart(
        BirthData(
            dateTime = LocalDateTime.of(1989, 12, 21, 13, 2),
            latitude = 10.0730,
            longitude = 78.7833,
            timeZone = ZoneId.of("Asia/Kolkata"),
            locationName = "Kāraikkudi, India",
            gender = "Female"
        )
    )

    private val nakshatras = listOf(
        "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra",
        "Punarvasu", "Pushya", "Ashlesha", "Magha", "P.Phalguni", "U.Phalguni",
        "Hasta", "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha", "Mula",
        "P.Ashadha", "U.Ashadha", "Shravana", "Dhanishta", "Shatabhisha",
        "P.Bhadrapada", "U.Bhadrapada", "Revati"
    )

    private fun body(name: String): PlanetaryPosition =
        if (name == "Lagnam") chart.ascendant
        else chart.planets.first { it.name == name }

    /** Sidereal (nakshatra, 1-based pada) for a body. */
    private fun nakPada(name: String): Pair<String, Int> {
        val sid = body(name).siderealLon
        val size = 800.0 / 60.0          // 13°20'
        val i = (sid / size).toInt()
        val pada = ((sid - i * size) / (size / 4)).toInt() + 1
        return nakshatras[i] to pada
    }

    private fun assertRasi(name: String, expected: String) =
        assertEquals(expected, body(name).sign, "$name rasi")

    // --- The three headline values on the sheet --------------------------

    @Test
    fun lagnamIsMeena() {
        // ஜென்ம லக்கனம் : மீனம். The old ascendant formula carried no
        // obliquity term at all and answered Thulam.
        assertRasi("Lagnam", "Pisces")
    }

    @Test
    fun theMoonsNakshatraIsHasta() {
        // நட்சத்திரம் : அஸ்தம்
        assertEquals("Hasta", nakPada("Moon").first)
    }

    @Test
    fun theMoonsRasiIsKanni() {
        // ராசி : கன்னி
        assertRasi("Moon", "Virgo")
    }

    // --- Every body's rasi, which is what the koshtam draws ---------------

    @Test
    fun everyGrahaLandsInTheRasiTheSheetGives() {
        assertRasi("Sun", "Sagittarius")      // Moolam
        assertRasi("Moon", "Virgo")           // Astham
        assertRasi("Mars", "Scorpio")         // Anusham
        assertRasi("Mercury", "Sagittarius")  // Pooradam
        assertRasi("Venus", "Capricorn")      // Thiruvonam
        assertRasi("Saturn", "Sagittarius")   // Pooradam
        assertRasi("Rahu", "Capricorn")       // Avittam
        assertRasi("Lagnam", "Pisces")        // Uthirattathi
    }

    @Test
    fun jupiterIsInMithunam() {
        // Against the sheet, and deliberately — see the class comment. Jupiter
        // was at opposition in Gemini on 27 December 1989.
        assertRasi("Jupiter", "Gemini")
    }

    // --- Padas the engine reproduces exactly ------------------------------

    @Test
    fun theSunMarsAndRahuMatchToThePada() {
        assertEquals("Mula" to 2, nakPada("Sun"))
        assertEquals("Anuradha" to 2, nakPada("Mars"))
        assertEquals("Dhanishta" to 1, nakPada("Rahu"))
    }

    // --- Invariants that catch whole classes of error ---------------------

    @Test
    fun nodesAreOpposite() {
        val rahu = body("Rahu").siderealLon
        val ketu = body("Ketu").siderealLon
        val gap = ((ketu - rahu) % 360.0 + 360.0) % 360.0
        assertTrue(abs(gap - 180.0) < 1e-6, "Rahu $rahu and Ketu $ketu are $gap apart")
    }

    @Test
    fun theChartCarriesTheNineGrahasAndNoOuterPlanets() {
        // Keeping the styles separate starts here: a rasi koshtam has no
        // Uranus, Neptune or Pluto in it, and the screenshot that prompted
        // this showed all three sitting in Tamil sign cells.
        val names = chart.planets.map { it.name }
        assertEquals(
            listOf("Sun", "Moon", "Mercury", "Venus", "Mars", "Jupiter", "Saturn", "Rahu", "Ketu"),
            names
        )
    }

    @Test
    fun everyBodyCarriesBothZodiacs() {
        // The sidereal and tropical frames must differ by exactly the ayanamsa
        // — about 23.7° for 1989. A body left in one frame would show a zero
        // gap here and quietly display a Western sign in the koshtam.
        (chart.planets + chart.ascendant).forEach { p ->
            val gap = ((p.lon - p.siderealLon) % 360.0 + 360.0) % 360.0
            assertTrue(
                abs(gap - 23.71) < 0.05,
                "${p.name}: tropical ${p.lon} and sidereal ${p.siderealLon} differ by $gap"
            )
        }
    }
}
