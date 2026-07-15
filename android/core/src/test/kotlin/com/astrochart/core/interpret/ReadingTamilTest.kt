package com.astrochart.core.interpret

import com.astrochart.core.i18n.Language
import com.astrochart.core.i18n.Translations
import com.astrochart.core.models.BirthData
import com.astrochart.core.utils.ChartCalculator
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Native-Tamil-reader / translation-QA coverage for [ChartReading] and the
 * Tamil vocabulary maps. Verifies that a full Tamil reading renders with real
 * Tamil script, no leaked string-template artifacts, and that every sign and
 * planet is actually localized (not silently falling back to English).
 */
class ReadingTamilTest {

    private val NAME = "அருண்"

    private val SIGNS = listOf(
        "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
        "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    )
    private val PLANETS = listOf(
        "Sun", "Moon", "Mercury", "Venus", "Mars",
        "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto"
    )

    private fun losAngeles() = ChartCalculator.calculateNatalChart(
        BirthData(
            dateTime = LocalDateTime.of(1990, 7, 20, 14, 30),
            latitude = 34.0522,
            longitude = -118.2437,
            timeZone = ZoneId.of("America/Los_Angeles"),
            locationName = "Los Angeles"
        )
    )

    private fun chennai() = ChartCalculator.calculateNatalChart(
        BirthData(
            dateTime = LocalDateTime.of(1985, 12, 3, 6, 15),
            latitude = 13.0827,
            longitude = 80.2707,
            timeZone = ZoneId.of("Asia/Kolkata"),
            locationName = "Chennai"
        )
    )

    /** Number of characters in the Tamil Unicode block U+0B80..U+0BFF. */
    private fun tamilCharCount(text: String): Int =
        text.count { it.code in 0x0B80..0x0BFF }

    @Test
    fun tamilReading_isWellFormed_forDiverseCharts() {
        for (chart in listOf(losAngeles(), chennai())) {
            val sections = ChartReading.build(chart, NAME, Language.TA)

            // Structure: exactly five sections, all titles & paragraphs non-blank.
            assertEquals(5, sections.size, "Tamil reading should have 5 sections")
            for (section in sections) {
                assertTrue(section.title.isNotBlank(), "Blank section title")
                assertTrue(section.paragraphs.isNotEmpty(), "${section.title} has no paragraphs")
                for (p in section.paragraphs) {
                    assertTrue(p.isNotBlank(), "${section.title} has a blank paragraph")
                }
            }

            // Overview (section 0) carries the person's name.
            val overview = sections[0].paragraphs.joinToString(" ")
            assertTrue(overview.contains(NAME), "Overview should contain the name $NAME")

            // Placements (section 2) has exactly one paragraph per planet.
            val placements = sections[2]
            assertEquals(
                chart.planets.size, placements.paragraphs.size,
                "Placements should have one paragraph per planet"
            )

            // No unresolved string-template artifacts anywhere in the reading.
            val allParagraphs = sections.flatMap { it.paragraphs }
            for (p in allParagraphs) {
                assertFalse(p.contains("$"), "Unresolved dollar sign in paragraph: $p")
                assertFalse(p.contains("\${"), "Unresolved template expression in paragraph: $p")
                assertFalse(p.contains("null"), "Literal 'null' in paragraph: $p")
            }

            // Real Tamil script is present (not just English fallback).
            val joined = allParagraphs.joinToString(" ")
            assertTrue(
                tamilCharCount(joined) >= 20,
                "Reading should contain plenty of Tamil-block characters"
            )
        }
    }

    @Test
    fun allSigns_areLocalizedToTamil() {
        for (sign in SIGNS) {
            val ta = Translations.signName(sign, Language.TA)
            assertTrue(ta.isNotBlank(), "Tamil sign name blank for $sign")
            assertNotEquals(sign, ta, "Sign $sign was not translated to Tamil")
            assertTrue(
                tamilCharCount(ta) > 0,
                "Tamil sign name for $sign has no Tamil-block characters: $ta"
            )
        }
    }

    @Test
    fun allPlanets_areLocalizedToTamil() {
        for (planet in PLANETS) {
            val ta = Translations.planetName(planet, Language.TA)
            assertTrue(ta.isNotBlank(), "Tamil planet name blank for $planet")
            assertNotEquals(planet, ta, "Planet $planet was not translated to Tamil")
            assertTrue(
                tamilCharCount(ta) > 0,
                "Tamil planet name for $planet has no Tamil-block characters: $ta"
            )
        }
    }

    @Test
    fun chineseZodiac_isLocalizedToTamil() {
        val ta = ChineseZodiac.name(2000, Language.TA)
        val en = ChineseZodiac.name(2000, Language.EN)
        assertTrue(ta.isNotBlank(), "Tamil zodiac animal blank")
        assertNotEquals(en, ta, "Zodiac animal for 2000 not translated to Tamil")
        assertTrue(
            tamilCharCount(ta) > 0,
            "Tamil zodiac animal has no Tamil-block characters: $ta"
        )
    }
}
