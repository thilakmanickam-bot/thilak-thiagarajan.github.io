package com.astrochart.core.interpret

import com.astrochart.core.i18n.Language
import com.astrochart.core.models.BirthData
import com.astrochart.core.models.ChartStyle
import com.astrochart.core.utils.ChartCalculator
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * The two zodiacs must not appear on one screen.
 *
 * The complaint that started this was a Tamil koshtam with Uranus, Neptune and
 * Pluto in it. Fixing the ephemeris made the plain fields sidereal, which fixed
 * the koshtam but left the *Western wheel* mixed instead: it drew at tropical
 * longitudes while the header, Placements, Balance and the reading beside it
 * still named sidereal rasis.
 *
 * These assert the separation holds in both directions, with a chart chosen so
 * the two answers genuinely differ.
 */
class ChartStyleSeparationTest {

    /**
     * The reference jathagam. The **Moon** is the discriminator here: Virgo
     * sidereal, Libra tropical. The Sun is not — at 269.4° tropical it is still
     * Sagittarius in both zodiacs, which is exactly the sort of coincidence that
     * makes a separation test pass while proving nothing.
     */
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

    private val moon = chart.planets.first { it.name == "Moon" }

    @Test
    fun theSameChartNamesDifferentSignsUnderEachStyle() {
        // If this ever passes trivially the fixture has drifted to a chart where
        // both zodiacs agree, and the rest of the file stops proving anything.
        assertNotEquals(
            moon.signFor(ChartStyle.SOUTH_INDIAN),
            moon.signFor(ChartStyle.WESTERN_WHEEL),
            "fixture no longer straddles a sign boundary"
        )
        assertEquals("Virgo", moon.signFor(ChartStyle.SOUTH_INDIAN))
        assertEquals("Libra", moon.signFor(ChartStyle.WESTERN_WHEEL))
    }

    @Test
    fun everyBodyIsConsistentWithinAStyle() {
        // Within one style, sign, label and longitude must all come from the
        // same frame. A body mixing them is how "27°23' Cancer" ends up printed
        // over a position that is not in Cancer.
        (chart.planets + chart.ascendant).forEach { p ->
            ChartStyle.entries.forEach { style ->
                val sign = p.signFor(style)
                val label = p.labelFor(style)
                val lon = p.longitudeFor(style)
                assertTrue(label.endsWith(sign), "${p.name} $style: '$label' does not name $sign")
                assertEquals(
                    sign,
                    com.astrochart.core.utils.ZodiacUtils.getSignFromLongitude(lon),
                    "${p.name} $style: longitude $lon is not in $sign"
                )
            }
        }
    }

    @Test
    fun theTwoStylesDifferByExactlyTheAyanamsa() {
        (chart.planets + chart.ascendant).forEach { p ->
            val gap = ((p.longitudeFor(ChartStyle.WESTERN_WHEEL) -
                p.longitudeFor(ChartStyle.SOUTH_INDIAN)) % 360.0 + 360.0) % 360.0
            assertTrue(
                abs(gap - 23.71) < 0.05,
                "${p.name}: styles differ by $gap°, which is not the 1989 ayanamsa"
            )
        }
    }

    @Test
    fun theReadingFollowsTheStyleItIsGiven() {
        val vedic = ChartReading.build(chart, "Solachi", ChartStyle.SOUTH_INDIAN, Language.EN)
            .flatMap { it.paragraphs }.joinToString(" ")
        val western = ChartReading.build(chart, "Solachi", ChartStyle.WESTERN_WHEEL, Language.EN)
            .flatMap { it.paragraphs }.joinToString(" ")

        assertNotEquals(vedic, western, "the reading ignored the style it was given")
        // The Moon's sign is named in the overview and again in placements, so
        // each reading must carry its own and not the other's.
        assertTrue(vedic.contains("Virgo"), "Vedic reading never names the Moon's rasi")
        assertTrue(western.contains("Libra"), "Western reading never names the Moon's tropical sign")
    }

    @Test
    fun theKoshtamIsAlwaysSidereal() {
        // Deliberately not style-gated: a rasi koshtam is Vedic by definition
        // and has no tropical variant. This pins that decision so nobody
        // "finishes the job" by gating it and quietly breaks porutham, which
        // matches on the same sidereal rasis.
        val cells = com.astrochart.core.utils.SouthIndianChart.cells(chart, includeAscendant = true)
        val moonCell = cells.first { "Moon" in it.bodies }
        assertEquals(moon.sign, moonCell.sign)
        assertEquals("Virgo", moonCell.sign)
    }
}
