package com.astrochart.core.utils

import com.astrochart.core.models.NatalChart

/**
 * Pure layout logic for the South-Indian (Tamil) square chart — the *rasi
 * koshtam*. The 4×4 grid has twelve fixed perimeter cells (one per zodiac sign)
 * and a merged 2×2 centre. Signs never move; each planet (and, optionally, the
 * ascendant/lagnam) is written into the cell of the sign it occupies.
 *
 * Coordinates are 0-indexed `(row, col)` from the top-left. The centre block
 * spans rows 1–2 and columns 1–2 and holds no sign. This is rendering-only
 * logic over the already-computed [NatalChart], so it is fully unit-testable
 * off-device and identical across languages (body names stay canonical English
 * until the UI localizes them).
 */
object SouthIndianChart {

    /** A single sign cell with the bodies that fall in that sign. */
    data class Cell(
        val row: Int,
        val col: Int,
        val sign: String,
        val bodies: List<String>
    )

    /**
     * The fixed sign → (row, col) placement, in zodiac order (Aries … Pisces).
     * Aries sits top-row second cell; the sequence then runs clockwise around
     * the perimeter, which is the conventional Tamil almanac arrangement.
     */
    private val SIGN_POSITIONS: List<Triple<String, Int, Int>> = listOf(
        Triple("Aries", 0, 1),
        Triple("Taurus", 0, 2),
        Triple("Gemini", 0, 3),
        Triple("Cancer", 1, 3),
        Triple("Leo", 2, 3),
        Triple("Virgo", 3, 3),
        Triple("Libra", 3, 2),
        Triple("Scorpio", 3, 1),
        Triple("Sagittarius", 3, 0),
        Triple("Capricorn", 2, 0),
        Triple("Aquarius", 1, 0),
        Triple("Pisces", 0, 0)
    )

    /** Grid coordinates (row, col) of the four cells that form the merged centre. */
    val CENTER_CELLS: List<Pair<Int, Int>> = listOf(1 to 1, 1 to 2, 2 to 1, 2 to 2)

    /** Top-left origin of the merged centre block and its span (in cells). */
    const val CENTER_ROW = 1
    const val CENTER_COL = 1
    const val CENTER_SPAN = 2

    /** Fixed (row, col) of a sign's cell, or `null` if the name is unknown. */
    fun cellOf(sign: String): Pair<Int, Int>? =
        SIGN_POSITIONS.firstOrNull { it.first == sign }?.let { it.second to it.third }

    /** The twelve sign cells in zodiac order, with no bodies. */
    fun emptyCells(): List<Cell> =
        SIGN_POSITIONS.map { (sign, row, col) -> Cell(row, col, sign, emptyList()) }

    /**
     * The twelve sign cells populated from [chart]. Each planet is placed in its
     * sign's cell; when [includeAscendant] is true the ascendant is added as an
     * `"Ascendant"` marker in its own sign's cell (listed first there, as the
     * lagnam conventionally leads). Bodies within a cell keep the chart's planet
     * order.
     */
    fun cells(chart: NatalChart, includeAscendant: Boolean = true): List<Cell> {
        val bySign: Map<String, MutableList<String>> =
            SIGN_POSITIONS.associate { it.first to mutableListOf<String>() }

        if (includeAscendant) {
            // Sidereal, unconditionally, and deliberately not gated on
            // ChartStyle: a rasi koshtam is a Vedic chart by definition, so
            // there is no tropical variant of it to switch to. Same for
            // porutham, Rasi Palan and the panchangam. Only the surfaces
            // that can be drawn either way take a style.
            bySign[chart.ascendant.sign]?.add("Ascendant")
        }
        for (planet in chart.planets) {
            bySign[planet.sign]?.add(planet.name)
        }

        return SIGN_POSITIONS.map { (sign, row, col) ->
            Cell(row, col, sign, bySign[sign]?.toList() ?: emptyList())
        }
    }
}
