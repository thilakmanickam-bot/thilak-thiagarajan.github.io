package com.astrochart.core.utils

import com.astrochart.core.models.*

object ChartCalculator {

    fun calculateNatalChart(birthData: BirthData): NatalChart {
        val (planets, angles) = EphemerisCalculator.calculatePlanetaryPositions(birthData)
        val (ascendantLon, midheavenLon) = angles

        // The lagnam is the ascendant read in the sidereal zodiac; the tropical
        // angle is kept alongside it so the Western wheel still draws where a
        // Western wheel should.
        val ascendant = angle("Ascendant", ascendantLon, house = 1, birthData = birthData)
        val midheaven = angle("Midheaven", midheavenLon, house = 10, birthData = birthData)

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

    /**
     * An angle (ascendant or midheaven) as a [PlanetaryPosition], carrying both
     * zodiacs exactly as [EphemerisCalculator] does for the planets — the two
     * must agree, or the lagnam in the koshtam would sit in a different rasi
     * from every planet around it.
     */
    private fun angle(
        name: String,
        tropicalLon: Double,
        house: Int,
        birthData: BirthData
    ): PlanetaryPosition {
        val siderealLon = EphemerisCalculator.toSidereal(birthData, tropicalLon)
        val sign = ZodiacUtils.getSignFromLongitude(siderealLon)
        val (degree, minute) = ZodiacUtils.formatPosition(siderealLon)
        val tropicalSign = ZodiacUtils.getSignFromLongitude(tropicalLon)
        val (tropicalDegree, tropicalMinute) = ZodiacUtils.formatPosition(tropicalLon)
        return PlanetaryPosition(
            name = name,
            lon = tropicalLon,
            siderealLon = siderealLon,
            sign = sign,
            element = ZodiacUtils.getElement(sign),
            modality = ZodiacUtils.getModality(sign),
            degree = degree,
            minute = minute,
            label = "$degree°$minute' $sign",
            tropicalSign = tropicalSign,
            tropicalLabel = "$tropicalDegree°$tropicalMinute' $tropicalSign",
            house = house
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
