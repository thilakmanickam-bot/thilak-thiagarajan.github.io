package com.astrochart.core.utils

import com.astrochart.core.models.Aspect
import com.astrochart.core.models.PlanetaryPosition
import kotlin.math.abs

object AspectCalculator {

    private val MAJOR_ASPECTS = listOf(
        AspectType("Conjunction", 0.0, 8.0),
        AspectType("Sextile", 60.0, 6.0),
        AspectType("Square", 90.0, 8.0),
        AspectType("Trine", 120.0, 8.0),
        AspectType("Opposition", 180.0, 8.0)
    )

    fun findAspects(planets: List<PlanetaryPosition>): List<Aspect> {
        val aspects = mutableListOf<Aspect>()

        for (i in planets.indices) {
            for (j in (i + 1) until planets.size) {
                val aspect = findAspect(planets[i], planets[j])
                if (aspect != null) {
                    aspects.add(aspect)
                }
            }
        }

        return aspects.sortedWith(compareBy({ it.orb }, { it.type }))
    }

    private fun findAspect(bodyA: PlanetaryPosition, bodyB: PlanetaryPosition): Aspect? {
        val diff = abs(bodyA.lon - bodyB.lon)
        val angle = if (diff > 180.0) 360.0 - diff else diff

        for (majorAspect in MAJOR_ASPECTS) {
            val orb = abs(angle - majorAspect.angle)
            if (orb <= majorAspect.maxOrb) {
                return Aspect(
                    bodyA = bodyA.name,
                    bodyB = bodyB.name,
                    type = majorAspect.name,
                    orb = orb
                )
            }
        }

        return null
    }

    private data class AspectType(
        val name: String,
        val angle: Double,
        val maxOrb: Double
    )
}
