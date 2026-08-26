package com.astrochart.core.utils

import com.astrochart.core.i18n.Language
import com.astrochart.core.i18n.Translations
import com.astrochart.core.models.BirthData
import com.astrochart.core.models.ChartStyle
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SouthIndianChartTest {

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
    fun signPositions_areTheFixedTamilLayout() {
        // Every one of the twelve signs has a distinct, in-bounds perimeter cell.
        val signs = ZodiacUtils.getAllSigns()
        val coords = signs.map { assertNotNull(SouthIndianChart.cellOf(it), "no cell for $it") }
        assertEquals(12, coords.toSet().size, "each sign must occupy a unique cell")
        for ((r, c) in coords) {
            assertTrue(r in 0..3 && c in 0..3, "cell out of the 4x4 grid: $r,$c")
        }
        // A couple of anchor positions from the conventional arrangement.
        assertEquals(0 to 1, SouthIndianChart.cellOf("Aries"))
        assertEquals(0 to 0, SouthIndianChart.cellOf("Pisces"))
        assertEquals(3 to 3, SouthIndianChart.cellOf("Virgo"))
    }

    @Test
    fun perimeterNeverOverlapsCentre() {
        val center = SouthIndianChart.CENTER_CELLS.toSet()
        SouthIndianChart.emptyCells().forEach { cell ->
            assertFalse((cell.row to cell.col) in center, "${cell.sign} sits on the centre block")
        }
    }

    @Test
    fun unknownSign_hasNoCell() {
        assertNull(SouthIndianChart.cellOf("Ophiuchus"))
    }

    @Test
    fun cells_placeEveryPlanetOnceInItsSign() {
        val chart = sampleChart()
        val cells = SouthIndianChart.cells(chart, includeAscendant = true)
        assertEquals(12, cells.size)

        // Every planet appears exactly once, in the cell whose sign matches it.
        for (planet in chart.planets) {
            val hosting = cells.filter { it.bodies.contains(planet.name) }
            assertEquals(1, hosting.size, "${planet.name} should be in exactly one cell")
            assertEquals(planet.sign, hosting.first().sign, "${planet.name} placed in wrong sign")
        }

        // Ascendant is present, and leads its cell (lagnam first).
        val ascCell = cells.first { it.sign == chart.ascendant.sign }
        assertEquals("Ascendant", ascCell.bodies.first())

        // Total bodies = planets + one ascendant marker.
        val total = cells.sumOf { it.bodies.size }
        assertEquals(chart.planets.size + 1, total)
    }

    @Test
    fun cells_withoutAscendant_omitTheLagnamMarker() {
        val chart = sampleChart()
        val cells = SouthIndianChart.cells(chart, includeAscendant = false)
        assertFalse(cells.any { it.bodies.contains("Ascendant") })
        assertEquals(chart.planets.size, cells.sumOf { it.bodies.size })
    }

    @Test
    fun bodyAbbr_isShortAndPresentForEveryBodyInEveryLanguage() {
        val bodies = listOf(
            "Sun", "Moon", "Mercury", "Venus", "Mars", "Jupiter",
            "Saturn", "Uranus", "Neptune", "Pluto", "Ascendant"
        )
        for (lang in Language.entries) {
            for (body in bodies) {
                val abbr = Translations.bodyAbbr(body, lang)
                assertTrue(abbr.isNotBlank(), "$lang abbr for $body is blank")
                assertTrue(abbr.length <= 4, "$lang abbr for $body too long: $abbr")
            }
        }
    }

    @Test
    fun chartStyleName_localizedForEveryStyleAndLanguage() {
        for (style in ChartStyle.entries) {
            for (lang in Language.entries) {
                assertTrue(
                    Translations.chartStyleName(style, lang).isNotBlank(),
                    "$lang name for $style is blank"
                )
            }
        }
    }

    @Test
    fun chartStyle_roundTripsThroughCode() {
        for (style in ChartStyle.entries) {
            assertEquals(style, ChartStyle.fromCode(style.code))
        }
        assertEquals(ChartStyle.DEFAULT, ChartStyle.fromCode("nonsense"))
        assertEquals(ChartStyle.DEFAULT, ChartStyle.fromCode(null))
    }
}
