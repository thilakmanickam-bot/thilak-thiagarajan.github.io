package com.astrochart.core.interpret

import com.astrochart.core.models.BirthData
import com.astrochart.core.utils.ChartCalculator
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertTrue

class ChartReadingTest {

    private fun sampleChart() = ChartCalculator.calculateNatalChart(
        BirthData(
            dateTime = LocalDateTime.of(1990, 7, 20, 14, 30),
            latitude = 34.0522,
            longitude = -118.2437,
            timeZone = ZoneId.of("America/Los_Angeles"),
            locationName = "Los Angeles"
        )
    )

    @Test
    fun build_producesAllSections_nonEmpty() {
        val sections = ChartReading.build(sampleChart(), "Alex")

        val titles = sections.map { it.title }
        assertTrue(titles.contains("Overview"))
        assertTrue(titles.contains("Core self"))
        assertTrue(titles.contains("The placements"))
        assertTrue(titles.contains("Key aspects"))
        assertTrue(titles.contains("Elemental & modality balance"))

        for (section in sections) {
            assertTrue(section.paragraphs.isNotEmpty(), "${section.title} should have paragraphs")
            for (p in section.paragraphs) {
                assertTrue(p.isNotBlank(), "${section.title} has a blank paragraph")
            }
        }
    }

    @Test
    fun build_includesNameAndEveryPlanetOnce() {
        val chart = sampleChart()
        val sections = ChartReading.build(chart, "Alex")

        val overview = sections.first { it.title == "Overview" }.paragraphs.joinToString(" ")
        assertTrue(overview.contains("Alex"))

        val placements = sections.first { it.title == "The placements" }
        // One paragraph per planet in the chart.
        assertTrue(placements.paragraphs.size == chart.planets.size)
        for (planet in chart.planets) {
            val count = placements.paragraphs.count { it.contains("${planet.name} in ") }
            assertTrue(count == 1, "${planet.name} should appear exactly once in placements")
        }
    }
}
