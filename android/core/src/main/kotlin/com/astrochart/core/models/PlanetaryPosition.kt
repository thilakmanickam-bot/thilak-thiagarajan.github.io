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
}
