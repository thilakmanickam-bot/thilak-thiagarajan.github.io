package com.astrochart.core.interpret

import com.astrochart.core.models.NatalChart
import com.astrochart.core.models.PlanetaryPosition
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ReadingSection(val title: String, val paragraphs: List<String>)

/**
 * Builds a personalized, plain-language reading from a computed [NatalChart] by
 * composing phrases from [PlanetInfo], [SignInfo], and [HouseInfo] plus the
 * chart's own aspect interpretations. Pure logic — unit-testable off-device.
 */
object ChartReading {

    private val dateFmt = DateTimeFormatter.ofPattern("d MMMM yyyy 'at' HH:mm")
    private val ELEMENTS = listOf("Fire", "Earth", "Air", "Water")

    fun build(chart: NatalChart, name: String): List<ReadingSection> {
        val who = name.ifBlank { "This person" }
        val sun = chart.planets.firstOrNull { it.name == "Sun" }
        val moon = chart.planets.firstOrNull { it.name == "Moon" }
        val asc = chart.ascendant
        val sections = mutableListOf<ReadingSection>()

        // 1. Overview
        val birth = buildString {
            append(chart.birthData.dateTime.format(dateFmt))
            if (chart.birthData.locationName.isNotBlank()) append(" in ${chart.birthData.locationName}")
        }
        val bigThree = buildString {
            append("With ")
            if (sun != null) append("the Sun in ${sun.sign}, ")
            if (moon != null) append("the Moon in ${moon.sign}, ")
            append("and ${asc.sign} rising, $who blends ")
            if (sun != null) append("a ${SignInfo.of(sun.sign).keywords} core")
            if (moon != null) append(", ${SignInfo.of(moon.sign).keywords} emotions,")
            append(" and a ${SignInfo.of(asc.sign).keywords} first impression.")
        }
        sections += ReadingSection(
            "Overview",
            listOf("$who was born on $birth.", bigThree)
        )

        // 2. Core self (the "big three")
        val core = mutableListOf<String>()
        sun?.let { core += placementSentence(it) }
        moon?.let { core += placementSentence(it) }
        core += "With ${asc.sign} rising, you meet the world in a ${SignInfo.of(asc.sign).keywords} way — " +
            "it shapes your instincts, style, and the first impression you make."
        sections += ReadingSection("Core self", core)

        // 3. All placements, in canonical planet order
        val placements = PlanetInfo.order
            .mapNotNull { pn -> chart.planets.firstOrNull { it.name == pn } }
            .map { placementSentence(it) }
        sections += ReadingSection("The placements", placements)

        // 4. Key aspects (tightest orbs first)
        val topAspects = chart.aspects.sortedBy { it.orb }.take(5)
        val aspectParas = if (topAspects.isEmpty()) {
            listOf("No major aspects fall within orb here — the planets each act fairly independently.")
        } else {
            topAspects.map { a ->
                val orb = String.format(Locale.US, "%.1f", a.orb)
                "${a.bodyA} ${a.type.lowercase()} ${a.bodyB} (orb $orb°): ${a.interpretation}"
            }
        }
        sections += ReadingSection("Key aspects", aspectParas)

        // 5. Elemental & modality balance
        sections += ReadingSection("Elemental & modality balance", balanceParagraphs(chart))

        return sections
    }

    private fun placementSentence(p: PlanetaryPosition): String {
        val role = PlanetInfo.of(p.name)?.role ?: "energy"
        val keywords = SignInfo.of(p.sign).keywords
        return "${PlanetInfo.glyph(p.name)} ${p.name} in ${p.sign} (House ${p.house}) — " +
            "your $role is expressed in a $keywords way, focused on ${HouseInfo.of(p.house)}."
    }

    private fun balanceParagraphs(chart: NatalChart): List<String> {
        val el = chart.balance.elements
        val mo = chart.balance.modalities
        val paras = mutableListOf<String>()

        el.maxByOrNull { it.value }?.key?.let { paras += dominantElementText(it) }

        val missing = ELEMENTS.filter { (el[it] ?: 0) == 0 }
        if (missing.isNotEmpty()) {
            paras += "There is little or no ${missing.joinToString(", ")} energy — " +
                lackingElementText(missing.first())
        }

        mo.maxByOrNull { it.value }?.key?.let { paras += modalityText(it) }
        return paras
    }

    private fun dominantElementText(e: String): String = when (e) {
        "Fire" -> "Fire is emphasised: energetic, enthusiastic, and action-oriented, you lead with warmth and spontaneity."
        "Earth" -> "Earth is emphasised: grounded, practical, and reliable, you build steadily and value what is tangible."
        "Air" -> "Air is emphasised: intellectual, curious, and social, you process life through ideas and connection."
        else -> "Water is emphasised: emotional, intuitive, and sensitive, you feel your way through life and attune to others."
    }

    private fun lackingElementText(e: String): String = when (e) {
        "Fire" -> "cultivating initiative and self-assertion can bring balance."
        "Earth" -> "grounding routines and follow-through are worth developing."
        "Air" -> "stepping back for objectivity and dialogue can help."
        else -> "making room for feelings and empathy can bring balance."
    }

    private fun modalityText(m: String): String = when (m) {
        "Cardinal" -> "Cardinal energy dominates — you are an initiator, happiest starting things and setting direction."
        "Fixed" -> "Fixed energy dominates — you are steady and determined, with real staying power once committed."
        else -> "Mutable energy dominates — you are adaptable and flexible, at ease with change and variety."
    }
}
