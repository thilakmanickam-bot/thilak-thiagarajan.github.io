package com.astrochart.core.interpret

data class PlanetDetail(val role: String, val glyph: String)

/** Per-planet role phrase + glyph, reused by the reading engine and the chart wheel. */
object PlanetInfo {

    /** Canonical display order. */
    val order: List<String> = listOf(
        "Sun", "Moon", "Mercury", "Venus", "Mars",
        "Jupiter", "Saturn", "Rahu", "Ketu"
    )

    val details: Map<String, PlanetDetail> = mapOf(
        "Sun" to PlanetDetail("core identity and vitality", "☉"),
        "Moon" to PlanetDetail("emotional nature and instincts", "☽"),
        "Mercury" to PlanetDetail("mind and communication", "☿"),
        "Venus" to PlanetDetail("love, values, and sense of beauty", "♀"),
        "Mars" to PlanetDetail("drive, courage, and desire", "♂"),
        "Jupiter" to PlanetDetail("growth, luck, and optimism", "♃"),
        "Saturn" to PlanetDetail("discipline, structure, and responsibility", "♄"),
        "Uranus" to PlanetDetail("individuality and need for change", "♅"),
        "Neptune" to PlanetDetail("imagination, dreams, and spirituality", "♆"),
        "Pluto" to PlanetDetail("power and capacity for transformation", "♇"),
        // The lunar nodes: shadow points rather than bodies, which is why the
        // Western set gives them the node glyphs and not planetary ones. The
        // outer three stay in the map for any saved chart that still names
        // them, but they are no longer in `order` and are no longer computed.
        "Rahu" to PlanetDetail("ambition, and what pulls you forward", "\u260A"),
        "Ketu" to PlanetDetail("detachment, and what you already carry", "\u260B")
    )

    fun of(name: String): PlanetDetail? = details[name]
    fun glyph(name: String): String = details[name]?.glyph ?: "•"
}
