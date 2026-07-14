package com.astrochart.core.models

/**
 * Complete natal chart data.
 */
data class NatalChart(
    val birthData: BirthData,
    val ascendant: PlanetaryPosition,
    val midheaven: PlanetaryPosition,
    val planets: List<PlanetaryPosition>,
    val houses: List<PlanetaryPosition>,
    val aspects: List<Aspect>,
    val balance: Balance
)

/**
 * Aspect between two planets/points.
 */
data class Aspect(
    val bodyA: String,
    val bodyB: String,
    val type: String,           // "Conjunction", "Sextile", "Square", "Trine", "Opposition"
    val orb: Double,            // Orb in degrees
    val interpretation: String = ""
)

/**
 * Element and modality distribution.
 */
data class Balance(
    val elements: Map<String, Int>,      // "Fire" -> 2, "Earth" -> 4, etc.
    val modalities: Map<String, Int>     // "Cardinal" -> 4, "Fixed" -> 3, etc.
)
