package com.astrochart.core.models

/**
 * How a natal chart is drawn. The underlying [NatalChart] data is identical for
 * every style — only the rendering differs — so switching style never triggers
 * recomputation.
 *
 * [WESTERN_WHEEL] is the circular zodiac wheel; [SOUTH_INDIAN] is the fixed
 * 4×4 square grid used in Tamil / South-Indian astrology (rasi koshtam), where
 * each zodiac sign occupies a fixed cell and bodies are written into the cell
 * of the sign they fall in. [code] is the persisted key.
 */
enum class ChartStyle(val code: String) {
    WESTERN_WHEEL("western_wheel"),
    SOUTH_INDIAN("south_indian");

    companion object {
        val DEFAULT = WESTERN_WHEEL

        fun fromCode(code: String?): ChartStyle =
            entries.firstOrNull { it.code == code } ?: DEFAULT
    }
}
