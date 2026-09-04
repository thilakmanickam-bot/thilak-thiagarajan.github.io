package com.astrochart.core.interpret

import com.astrochart.core.models.ChartStyle
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
 * Native-reader / QA coverage for the Chinese ([Language.ZH]) natal-chart reading.
 *
 * Focuses on things a naive word-swap or a Kotlin string-template mistake would
 * break: unresolved `$`/`${` template artifacts (Chinese chars are valid Kotlin
 * identifier chars, so `$var` immediately followed by a Chinese char silently
 * becomes part of the identifier), actual presence of CJK script, and that the
 * localization maps actually differ from English.
 */
class ReadingChineseTest {

    private fun losAngelesChart() = ChartCalculator.calculateNatalChart(
        BirthData(
            dateTime = LocalDateTime.of(1990, 7, 20, 14, 30),
            latitude = 34.0522,
            longitude = -118.2437,
            timeZone = ZoneId.of("America/Los_Angeles"),
            locationName = "Los Angeles"
        )
    )

    private fun beijingChart() = ChartCalculator.calculateNatalChart(
        BirthData(
            dateTime = LocalDateTime.of(2001, 3, 21, 23, 45),
            latitude = 39.9042,
            longitude = 116.4074,
            timeZone = ZoneId.of("Asia/Shanghai"),
            locationName = "Beijing"
        )
    )

    private fun londonChart() = ChartCalculator.calculateNatalChart(
        BirthData(
            dateTime = LocalDateTime.of(1985, 12, 5, 6, 15),
            latitude = 51.5074,
            longitude = -0.1278,
            timeZone = ZoneId.of("Europe/London"),
            locationName = "London"
        )
    )

    private val allSigns = listOf(
        "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
        "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    )
    private val allPlanets = listOf(
        "Sun", "Moon", "Mercury", "Venus", "Mars",
        "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto"
    )

    /** Count characters in the CJK Unified Ideographs block U+4E00..U+9FFF. */
    private fun countCjk(text: String): Int =
        text.count { it.code in 0x4E00..0x9FFF }

    @Test
    fun zhReading_isWellFormed_forDiverseCharts() {
        val charts = listOf(losAngelesChart(), beijingChart(), londonChart())

        for (chart in charts) {
            val sections = ChartReading.build(chart, "小明", ChartStyle.SOUTH_INDIAN, Language.ZH)

            // Structure: exactly five sections, all titles and paragraphs non-blank.
            assertEquals(5, sections.size, "expected 5 sections")
            for (section in sections) {
                assertTrue(section.title.isNotBlank(), "blank section title")
                assertTrue(section.paragraphs.isNotEmpty(), "${section.title} has no paragraphs")
                for (p in section.paragraphs) {
                    assertTrue(p.isNotBlank(), "${section.title} has a blank paragraph")
                }
            }

            // Overview contains the name.
            val overview = sections[0].paragraphs.joinToString(" ")
            assertTrue(overview.contains("小明"), "overview should contain the name 小明")

            // Placements section: exactly one paragraph per planet in the chart.
            val placements = sections.first { it.title == "行星落点" }
            assertEquals(
                chart.planets.size, placements.paragraphs.size,
                "placements should have one paragraph per planet"
            )

            // No unresolved Kotlin string-template artifacts anywhere in the reading.
            val allParagraphs = sections.flatMap { it.paragraphs }
            for (p in allParagraphs) {
                assertFalse(p.contains("$"), "unresolved '\$' template artifact in: $p")
                assertFalse(p.contains("\${"), "unresolved '\${' template artifact in: $p")
                assertFalse(p.contains("null"), "literal 'null' leaked into reading: $p")
            }

            // Chinese script is actually present.
            val joined = allParagraphs.joinToString(" ")
            assertTrue(
                countCjk(joined) >= 20,
                "reading should contain at least 20 CJK characters, found ${countCjk(joined)}"
            )
        }
    }

    @Test
    fun zhVocabulary_isLocalized_notEnglish() {
        // Every sign name is localized (differs from English) and non-blank.
        for (sign in allSigns) {
            val zh = Translations.signName(sign, Language.ZH)
            assertTrue(zh.isNotBlank(), "$sign has blank Chinese name")
            assertNotEquals(sign, zh, "$sign was not translated to Chinese")
            assertTrue(countCjk(zh) > 0, "$sign Chinese name has no CJK: $zh")
        }
        // Every planet name is localized and non-blank.
        for (planet in allPlanets) {
            val zh = Translations.planetName(planet, Language.ZH)
            assertTrue(zh.isNotBlank(), "$planet has blank Chinese name")
            assertNotEquals(planet, zh, "$planet was not translated to Chinese")
            assertTrue(countCjk(zh) > 0, "$planet Chinese name has no CJK: $zh")
        }
    }

    @Test
    fun chineseZodiac_animalNames_areCorrect() {
        assertEquals("龙", ChineseZodiac.name(2000, Language.ZH))
        assertEquals("马", ChineseZodiac.name(1990, Language.ZH))
        assertEquals("鼠", ChineseZodiac.name(2008, Language.ZH))
    }
}
