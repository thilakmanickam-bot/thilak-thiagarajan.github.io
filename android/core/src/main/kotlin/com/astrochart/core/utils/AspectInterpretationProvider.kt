package com.astrochart.core.utils

import com.astrochart.core.i18n.ContentLang
import com.astrochart.core.i18n.Language
import com.astrochart.core.i18n.Translations
import com.astrochart.core.models.AspectInterpretation

object AspectInterpretationProvider {

    fun getInterpretation(bodyA: String, bodyB: String, aspectType: String): String {
        val key = "$bodyA-$bodyB-$aspectType"
        return interpretations[key] ?: "This aspect brings combined energies of $bodyA and $bodyB."
    }

    /**
     * Localized aspect interpretation. English returns the bespoke text above;
     * Tamil and Chinese compose a natural sentence from the localized planet
     * names and the aspect's nature (blend / harmonious / challenging), so the
     * reading is fully translated without maintaining hundreds of hand-written
     * lines per language.
     */
    fun getInterpretation(bodyA: String, bodyB: String, aspectType: String, lang: Language): String {
        if (lang == Language.EN) return getInterpretation(bodyA, bodyB, aspectType)
        val a = Translations.planetName(bodyA, lang)
        val b = Translations.planetName(bodyB, lang)
        val nature = when (aspectType) {
            "Conjunction" -> 0   // blend
            "Trine", "Sextile" -> 1 // harmonious
            else -> 2            // challenging
        }
        return when (lang.content) {
            ContentLang.TA -> when (nature) {
                0 -> "$a – $b: இந்த இணைப்பு அவற்றின் ஆற்றல்களை ஒன்றிணைக்கிறது."
                1 -> "$a – $b: இவை இணக்கமாகப் பாய்கின்றன, இயல்பான திறமையையும் எளிமையையும் தருகின்றன."
                else -> "$a – $b: இவற்றுக்கிடையே பதற்றம் உள்ளது, வளர்ச்சிக்கான உந்துதலைத் தருகிறது."
            }
            else -> when (nature) {
                0 -> "${a}与${b}：能量融合，共同运作。"
                1 -> "${a}与${b}：和谐流动，带来天赋与顺畅。"
                else -> "${a}与${b}：彼此张力，激发成长。"
            }
        }
    }

    private val interpretations = mapOf(
        // Sun aspects
        "Sun-Moon-Conjunction" to "Natural integration of conscious will and emotional nature. Core sense of identity is aligned with feelings.",
        "Sun-Moon-Opposition" to "Tension between inner needs and outer expression. Often brings emotional depth and self-awareness.",
        "Sun-Moon-Square" to "Internal conflict between emotions and conscious will. Drives personal growth through resolution.",
        "Sun-Moon-Trine" to "Harmonious flow between emotions and will. Easy self-expression and emotional balance.",
        "Sun-Moon-Sextile" to "Cooperative energies between reason and emotion. Flexibility in responding to life situations.",

        "Sun-Mercury-Conjunction" to "Sharp mind and clear self-expression. Strong connection between thoughts and identity.",
        "Sun-Mercury-Opposition" to "Tendency to be opinionated. Dialogue and diverse perspectives enrich understanding.",
        "Sun-Mercury-Square" to "Mental restlessness. Powerful drive to communicate and resolve contradictions.",
        "Sun-Mercury-Trine" to "Clear thinking and excellent communication. Natural ability to learn and teach.",
        "Sun-Mercury-Sextile" to "Flexible mind and diplomatic communication. Good at adaptation and problem-solving.",

        "Sun-Venus-Conjunction" to "Charm, creativity, and love of beauty. Strong personal magnetism and aesthetic sense.",
        "Sun-Venus-Opposition" to "Magnetic attraction to others. Relationship awareness and desire for balance in partnerships.",
        "Sun-Venus-Square" to "Creative tension in relationships. Drives personal growth through love and artistic expression.",
        "Sun-Venus-Trine" to "Natural charm and grace. Ability to attract what you need and create beauty.",
        "Sun-Venus-Sextile" to "Sociable and artistic. Natural diplomacy and ability to find pleasure in life.",

        "Sun-Mars-Conjunction" to "Intense drive and ambition. Strong will and assertive self-expression.",
        "Sun-Mars-Opposition" to "External conflict reveals internal power. Drives achievement through overcoming resistance.",
        "Sun-Mars-Square" to "Passionate determination. Can be aggressive but also powerfully motivated.",
        "Sun-Mars-Trine" to "Natural courage and energy. Ability to pursue goals with confidence.",
        "Sun-Mars-Sextile" to "Enterprising and confident. Good at initiating projects and taking action.",

        "Sun-Jupiter-Conjunction" to "Optimism, generosity, and natural luck. Expansive view of life and belief in possibilities.",
        "Sun-Jupiter-Opposition" to "Tendency toward excess. Need for wisdom in expansion and growth.",
        "Sun-Jupiter-Square" to "Overconfidence or expansion beyond resources. Drives learning through experience.",
        "Sun-Jupiter-Trine" to "Natural good fortune and optimism. Ability to inspire others and grow.",
        "Sun-Jupiter-Sextile" to "Fortunate and generous. Good judgment in taking opportunities.",

        "Sun-Saturn-Conjunction" to "Responsible and disciplined. Maturity and respect for structure and limits.",
        "Sun-Saturn-Opposition" to "Life lesson in responsibility. Can feel restricted but builds character.",
        "Sun-Saturn-Square" to "Difficult early experiences shape strong character. Perseverance leads to success.",
        "Sun-Saturn-Trine" to "Natural discipline and wisdom. Respect for tradition and proven methods.",
        "Sun-Saturn-Sextile" to "Practical ambition. Good at strategic planning and achieving long-term goals.",

        // Moon aspects
        "Moon-Mercury-Conjunction" to "Emotional intelligence and intuitive communication. Feelings flow easily into words.",
        "Moon-Mercury-Opposition" to "Emotional subjectivity in thinking. Learning to balance heart and mind.",
        "Moon-Mercury-Square" to "Emotionally driven thoughts. Intuitive but sometimes scattered.",
        "Moon-Mercury-Trine" to "Natural empathy and good memory. Ability to communicate feelings well.",
        "Moon-Mercury-Sextile" to "Tactful communication and emotional awareness. Good at listening.",

        "Moon-Venus-Conjunction" to "Emotional warmth and affection. Natural capacity for nurturing and loving.",
        "Moon-Venus-Opposition" to "Relationship emotions are complex. Deep need for emotional and romantic connection.",
        "Moon-Venus-Square" to "Emotional insecurity in relationships. Drives the search for deeper love.",
        "Moon-Venus-Trine" to "Harmony in emotions and relationships. Natural ability to give and receive love.",
        "Moon-Venus-Sextile" to "Caring and emotionally available. Easy-going in relationships.",

        "Moon-Mars-Conjunction" to "Intense emotions and passionate nature. Strong drive to act on feelings.",
        "Moon-Mars-Opposition" to "Emotional intensity creates external challenges. Builds strength through conflict.",
        "Moon-Mars-Square" to "Emotional reactivity and impulsiveness. Passionate but sometimes volatile.",
        "Moon-Mars-Trine" to "Courageous emotions and protective instincts. Natural warrior spirit.",
        "Moon-Mars-Sextile" to "Emotionally brave and decisive. Good at protecting what matters.",

        "Moon-Jupiter-Conjunction" to "Emotional optimism and generosity. Natural nurturing and benevolence.",
        "Moon-Jupiter-Opposition" to "Emotional expansion. Need to balance giving with self-care.",
        "Moon-Jupiter-Square" to "Emotional exaggeration or overindulgence. Learns wisdom through experience.",
        "Moon-Jupiter-Trine" to "Harmonious emotions and natural luck. Optimistic and generous spirit.",
        "Moon-Jupiter-Sextile" to "Cheerful and emotionally open. Ability to uplift others.",

        "Moon-Saturn-Conjunction" to "Emotional discipline and depth. Serious and responsible feelings.",
        "Moon-Saturn-Opposition" to "Emotional restriction or grief becomes teacher. Builds emotional strength.",
        "Moon-Saturn-Square" to "Emotional isolation or sadness. Develops emotional maturity and resilience.",
        "Moon-Saturn-Trine" to "Emotional wisdom and stability. Trustworthy and dependable.",
        "Moon-Saturn-Sextile" to "Emotionally reserved but responsible. Good at supporting others.",

        // Venus aspects
        "Venus-Mars-Conjunction" to "Magnetic attraction and passionate romance. Strong desire nature.",
        "Venus-Mars-Opposition" to "Love and desire create attraction and conflict. Passionate relationships.",
        "Venus-Mars-Square" to "Passionate but frustrated love desires. Drives pursuit of authentic intimacy.",
        "Venus-Mars-Trine" to "Harmonious blend of love and desire. Attractive and passionate.",
        "Venus-Mars-Sextile" to "Attractive and confident in romantic pursuits. Good at flirtation.",

        "Venus-Jupiter-Conjunction" to "Lucky in love and generosity. Natural charm and abundance mindset.",
        "Venus-Jupiter-Opposition" to "Indulgence in pleasure. Generosity can exceed resources.",
        "Venus-Jupiter-Square" to "Over-indulgence in sensual pleasures. Learns through excess.",
        "Venus-Jupiter-Trine" to "Graceful and fortunate. Natural talent for attracting abundance.",
        "Venus-Jupiter-Sextile" to "Generous and sociable. Natural ability to enjoy life.",

        "Venus-Saturn-Conjunction" to "Serious approach to love and values. Faithful and committed.",
        "Venus-Saturn-Opposition" to "Love involves sacrifice or duty. Builds deep commitment.",
        "Venus-Saturn-Square" to "Difficult early relationships shape values. Learns enduring love.",
        "Venus-Saturn-Trine" to "Loyal and steadfast in love. Values lasting relationships.",
        "Venus-Saturn-Sextile" to "Responsible and faithful. Good at maintaining relationships.",

        // Mercury aspects
        "Mercury-Mars-Conjunction" to "Sharp, direct communication. Argumentative but intellectually honest.",
        "Mercury-Mars-Opposition" to "Communication conflicts reveal different viewpoints. Learning through debate.",
        "Mercury-Mars-Square" to "Aggressive thinking and impulsive speech. Powerful mental energy.",
        "Mercury-Mars-Trine" to "Clear thinking and convincing speech. Natural debater.",
        "Mercury-Mars-Sextile" to "Witty and direct communication. Good at solving problems with words.",

        "Mercury-Jupiter-Conjunction" to "Optimistic thinking and broad perspective. Natural teacher and philosopher.",
        "Mercury-Jupiter-Opposition" to "Opinionated thinking. Learning to hear other perspectives.",
        "Mercury-Jupiter-Square" to "Overconfident thinking or exaggeration. Learning through mistakes.",
        "Mercury-Jupiter-Trine" to "Expansive thinking and excellent communication. Natural wisdom.",
        "Mercury-Jupiter-Sextile" to "Positive perspective and good luck with communication.",

        "Mercury-Saturn-Conjunction" to "Serious, deliberate thinking. Clear, logical communication.",
        "Mercury-Saturn-Opposition" to "Restricted thinking or learning difficulties. Develops deep focus.",
        "Mercury-Saturn-Square" to "Challenging learning process. Gains mastery through persistence.",
        "Mercury-Saturn-Trine" to "Disciplined thinking and clear structure. Natural organizer.",
        "Mercury-Saturn-Sextile" to "Practical thinking and good planning. Efficient communicator.",

        // Mars aspects
        "Mars-Jupiter-Conjunction" to "Expansive energy and adventurous spirit. Natural ambition and confidence.",
        "Mars-Jupiter-Opposition" to "Energy outward creates external resistance. Learns through challenge.",
        "Mars-Jupiter-Square" to "Excessive energy or recklessness. Learns wisdom from consequences.",
        "Mars-Jupiter-Trine" to "Natural courage and good fortune. Attractive confidence.",
        "Mars-Jupiter-Sextile" to "Enterprising and fortunate. Good at taking opportunities.",

        "Mars-Saturn-Conjunction" to "Disciplined energy and focused ambition. Respect for limits.",
        "Mars-Saturn-Opposition" to "Energy blocked by external circumstances. Builds character through struggle.",
        "Mars-Saturn-Square" to "Frustration and blocked energy. Eventual powerful achievement.",
        "Mars-Saturn-Trine" to "Disciplined warrior energy. Strategic and determined.",
        "Mars-Saturn-Sextile" to "Controlled ambition and responsible action. Reliable and strong.",

        // Jupiter aspects
        "Jupiter-Saturn-Conjunction" to "Balance between expansion and caution. Natural wisdom and maturity.",
        "Jupiter-Saturn-Opposition" to "Growth through balancing extremes. Life teaches through extremes.",
        "Jupiter-Saturn-Square" to "Tension between ambition and limitation. Eventual mastery.",
        "Jupiter-Saturn-Trine" to "Balance between optimism and caution. Wise judgment.",
        "Jupiter-Saturn-Sextile" to "Fortunate timing and good judgment. Balance in decision-making."
    )

    fun getAllInterpretations(): List<AspectInterpretation> {
        return interpretations.map { (key, text) ->
            val parts = key.split("-")
            AspectInterpretation(parts[0], parts[1], parts[2], text)
        }
    }
}
