package com.astrochart.core.models

/**
 * Planetary position in the natal chart, carried in both zodiacs.
 *
 * Halo is a Vedic app — the rasi koshtam, the panchangam, porutham and Rasi
 * Palan are all sidereal — so the plain fields ([sign], [degree], [label] and
 * the rest) are **sidereal**, computed with the Lahiri ayanamsa. Anything that
 * reads them without thinking about it gets the rasi, which is the right
 * default here.
 *
 * The tropical frame survives in [lon], [tropicalSign] and [tropicalLabel],
 * for the one place it belongs: the Western wheel, where a tropical chart is
 * exactly what the reader is asking for.
 *
 * The two-frame fields are defaulted, and sit last, so existing positional
 * construction keeps compiling and a caller holding only one frame reads
 * sensibly rather than getting zeroes.
 */
data class PlanetaryPosition(
    val name: String,           // "Sun", "Moon", "Mercury", etc.
    val lon: Double,            // TROPICAL ecliptic longitude 0-360
    val sign: String,           // Sidereal rasi — "Aries", "Taurus", etc.
    val element: String,        // "Fire", "Earth", "Air", "Water" (of [sign])
    val modality: String,       // "Cardinal", "Fixed", "Mutable" (of [sign])
    val degree: Int,            // Degrees within the sidereal sign (0-29)
    val minute: Int,            // Minutes within degree (0-59)
    val label: String,          // e.g. "27°23' Cancer", sidereal
    val house: Int,             // House number (1-12)
    val retrograde: Boolean = false,
    val siderealLon: Double = lon,     // Lahiri sidereal longitude 0-360
    val tropicalSign: String = sign,   // Western sign
    val tropicalLabel: String = label  // e.g. "21°45' Leo", tropical
) {
    fun toReadableString(): String = "$label (House $house)${if (retrograde) " ℞" else ""}"

    /**
     * The sign to *show*, given how the chart is being drawn.
     *
     * UI must go through this rather than reading [sign] or [tropicalSign]
     * directly. Reading a field without thinking about which zodiac it belongs
     * to is exactly how the Western wheel came to be drawn at tropical
     * longitudes while the header beside it named sidereal rasis.
     *
     * Only [ChartStyle.WESTERN_WHEEL] is tropical. Everything else is Vedic, so
     * a style added later — North Indian, say — is correct by default instead
     * of by someone remembering to extend a `when`.
     */
    fun signFor(style: ChartStyle): String =
        if (style == ChartStyle.WESTERN_WHEEL) tropicalSign else sign

    /** The degree-and-sign label to show, in the zodiac [style] implies. */
    fun labelFor(style: ChartStyle): String =
        if (style == ChartStyle.WESTERN_WHEEL) tropicalLabel else label

    /** The longitude to draw at, in the zodiac [style] implies. */
    fun longitudeFor(style: ChartStyle): Double =
        if (style == ChartStyle.WESTERN_WHEEL) lon else siderealLon
}
