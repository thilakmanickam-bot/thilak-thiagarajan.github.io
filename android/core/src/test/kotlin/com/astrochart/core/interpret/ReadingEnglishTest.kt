package com.astrochart.core.interpret

import com.astrochart.core.i18n.Language
import com.astrochart.core.i18n.Translations
import com.astrochart.core.models.BirthData
import com.astrochart.core.models.NatalChart
import com.astrochart.core.utils.ChartCalculator
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Native-English-reader checks on the generated reading. Deliberately robust,
 * not brittle: it asserts on structure, invariants, and script — never on exact
 * prose — so wording can be refined without breaking the tests.
 */
class ReadingEnglishTest {

    private fun chart(
        dt: LocalDateTime,
        lat: Double,
        lon: Double,
        zone: ZoneId,
        place: String
    ): NatalChart = ChartCalculator.calculateNatalChart(
        BirthData(
            dateTime = dt,
            latitude = lat,
            longitude = lon,
            timeZone = zone,
            locationName = place
        )
    )

    private fun sampleCharts(): List<NatalChart> = listOf(
        chart(
            LocalDateTime.of(1990, 7, 20, 14, 30),
            34.0522, -118.2437,
            ZoneId.of("America/Los_Angeles"),
            "Los Angeles"
        ),
        chart(
            LocalDateTime.of(1985, 12, 3, 6, 15),
            13.0827, 80.2707,
            ZoneId.of("Asia/Kolkata"),
            "Chennai"
        ),
        chart(
            LocalDateTime.of(2001, 3, 14, 22, 5),
            51.5074, -0.1278,
            ZoneId.of("Europe/London"),
            "London"
        )
    )

    private val planetNames = PlanetInfo.order

    @Test
    fun reading_isStructurallySound_forDiverseCharts() {
        for (chart in sampleCharts()) {
            val sections = ChartReading.build(chart, "Alex", Language.EN)

            // Exactly five sections, all titled, no blank paragraphs.
            assertEquals(5, sections.size, "expected 5 sections")
            for (section in sections) {
                assertTrue(section.title.isNotBlank(), "blank section title")
                assertTrue(
                    section.paragraphs.isNotEmpty(),
                    "${section.title} should have paragraphs"
                )
                for (p in section.paragraphs) {
                    assertTrue(p.isNotBlank(), "${section.title} has a blank paragraph")
                }
            }

            // Overview names the person.
            val overview = sections[0].paragraphs.joinToString(" ")
            assertTrue(overview.contains("Alex"), "Overview should mention Alex")

            // The placements section: one paragraph per planet, each planet named once.
            val placements = sections.first { it.paragraphs.size == chart.planets.size }
            assertEquals(
                chart.planets.size,
                placements.paragraphs.size,
                "placements should have one paragraph per planet"
            )
            val placementsText = placements.paragraphs.joinToString("\n")
            for (planet in chart.planets) {
                val occurrences = Regex("\\b${Regex.escape(planet.name)}\\b")
                    .findAll(placementsText).count()
                assertEquals(
                    1, occurrences,
                    "${planet.name} should appear exactly once across placements"
                )
            }

            // No unresolved template artifacts anywhere in the reading.
            val allText = sections.joinToString("\n") {
                it.title + "\n" + it.paragraphs.joinToString("\n")
            }
            assertFalse(allText.contains('$'), "reading contains a literal '$' artifact")
            assertFalse(allText.contains("\${"), "reading contains an unresolved \${ template")
            assertFalse(allText.contains("null"), "reading contains the substring 'null'")

            // English is Latin script: no CJK (U+4E00–U+9FFF), no Tamil (U+0B80–U+0BFF).
            for (ch in allText) {
                assertFalse(
                    ch in '一'..'鿿',
                    "reading contains CJK character: $ch"
                )
                assertFalse(
                    ch in '஀'..'௿',
                    "reading contains Tamil character: $ch"
                )
            }

            // Every "Key aspects" paragraph cites an orb with a degree symbol.
            val aspects = sections.first { it.title == "Key aspects" }
            for (p in aspects.paragraphs) {
                assertTrue(p.contains("orb"), "aspect paragraph missing 'orb': $p")
                assertTrue(p.contains('°'), "aspect paragraph missing '°': $p")
            }
        }
    }

    @Test
    fun translations_english_signAndPlanetVocabulary() {
        val signs = listOf(
            "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
            "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
        )
        for (sign in signs) {
            assertEquals(
                sign,
                Translations.signName(sign, Language.EN),
                "EN sign name should be the canonical English"
            )
        }
        for (planet in planetNames) {
            assertTrue(
                Translations.planetName(planet, Language.EN).isNotBlank(),
                "EN planet name blank for $planet"
            )
        }
    }
}
