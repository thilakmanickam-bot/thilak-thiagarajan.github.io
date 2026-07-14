package com.astrochart.core.models

/**
 * Planetary position in the natal chart.
 */
data class PlanetaryPosition(
    val name: String,           // "Sun", "Moon", "Mercury", etc.
    val lon: Double,            // Ecliptic longitude 0-360
    val sign: String,           // "Aries", "Taurus", etc.
    val element: String,        // "Fire", "Earth", "Air", "Water"
    val modality: String,       // "Cardinal", "Fixed", "Mutable"
    val degree: Int,            // Degrees within sign (0-29)
    val minute: Int,            // Minutes within degree (0-59)
    val label: String,          // e.g. "27°23' Cancer"
    val house: Int,             // House number (1-12)
    val retrograde: Boolean = false
) {
    fun toReadableString(): String = "$label (House $house)${if (retrograde) " ℞" else ""}"
}
