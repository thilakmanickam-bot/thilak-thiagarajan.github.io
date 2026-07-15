package com.astrochart.core.utils

import com.astrochart.core.models.*

object ChartCalculator {

    fun calculateNatalChart(birthData: BirthData): NatalChart {
        val (planets, angles) = EphemerisCalculator.calculatePlanetaryPositions(birthData)
        val (ascendantLon, midheavenLon) = angles

        val ascendantSign = ZodiacUtils.getSignFromLongitude(ascendantLon)
        val (ascDegree, ascMinute) = ZodiacUtils.formatPosition(ascendantLon)
        val ascendant = PlanetaryPosition(
            name = "Ascendant",
            lon = ascendantLon,
            sign = ascendantSign,
            element = ZodiacUtils.getElement(ascendantSign),
            modality = ZodiacUtils.getModality(ascendantSign),
            degree = ascDegree,
            minute = ascMinute,
            label = "$ascDegree°$ascMinute' $ascendantSign",
            house = 1
        )

        val midheavenSign = ZodiacUtils.getSignFromLongitude(midheavenLon)
        val (mcDegree, mcMinute) = ZodiacUtils.formatPosition(midheavenLon)
        val midheaven = PlanetaryPosition(
            name = "Midheaven",
            lon = midheavenLon,
            sign = midheavenSign,
            element = ZodiacUtils.getElement(midheavenSign),
            modality = ZodiacUtils.getModality(midheavenSign),
            degree = mcDegree,
            minute = mcMinute,
            label = "$mcDegree°$mcMinute' $midheavenSign",
            house = 10
        )

        val allPositions = planets + ascendant + midheaven
        val aspects = AspectCalculator.findAspects(planets)

        val aspectsWithInterpretations = aspects.map { aspect ->
            aspect.copy(
                interpretation = AspectInterpretationProvider.getInterpretation(
                    aspect.bodyA,
                    aspect.bodyB,
                    aspect.type
                )
            )
        }

        val balance = calculateBalance(planets)

        return NatalChart(
            birthData = birthData,
            ascendant = ascendant,
            midheaven = midheaven,
            planets = planets,
            houses = emptyList(),
            aspects = aspectsWithInterpretations,
            balance = balance
        )
    }

    private fun calculateBalance(planets: List<PlanetaryPosition>): Balance {
        val elements = mutableMapOf("Fire" to 0, "Earth" to 0, "Air" to 0, "Water" to 0)
        val modalities = mutableMapOf("Cardinal" to 0, "Fixed" to 0, "Mutable" to 0)

        for (planet in planets) {
            elements[planet.element] = (elements[planet.element] ?: 0) + 1
            modalities[planet.modality] = (modalities[planet.modality] ?: 0) + 1
        }

        return Balance(
            elements = elements.filterValues { it > 0 },
            modalities = modalities.filterValues { it > 0 }
        )
    }
}
