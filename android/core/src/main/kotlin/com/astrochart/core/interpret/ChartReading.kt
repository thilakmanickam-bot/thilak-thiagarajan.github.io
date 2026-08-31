package com.astrochart.core.interpret

import com.astrochart.core.i18n.ContentLang
import com.astrochart.core.i18n.Language
import com.astrochart.core.i18n.Translations
import com.astrochart.core.models.NatalChart
import com.astrochart.core.models.PlanetaryPosition
import com.astrochart.core.utils.AspectInterpretationProvider
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ReadingSection(val title: String, val paragraphs: List<String>)

/**
 * Builds a personalized reading from a computed [NatalChart] in the requested
 * [Language]. Vocabulary comes from [Translations]; the sentence templates are
 * language-specific so the prose reads naturally rather than as a word-swap.
 * Pure logic — unit-testable off-device — and regenerated at render time so the
 * language toggle re-renders instantly with no recomputation.
 */
object ChartReading {

    private val ELEMENTS = listOf("Fire", "Earth", "Air", "Water")

    private fun dateFmt(lang: Language): DateTimeFormatter = when (lang.content) {
        ContentLang.EN -> DateTimeFormatter.ofPattern("d MMM yyyy 'at' HH:mm", lang.locale)
        ContentLang.TA -> DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", lang.locale)
        ContentLang.ZH -> DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm", lang.locale)
    }

    fun build(chart: NatalChart, name: String, lang: Language = Language.EN): List<ReadingSection> {
        val who = name.ifBlank { defaultWho(lang) }
        val sun = chart.planets.firstOrNull { it.name == "Sun" }
        val moon = chart.planets.firstOrNull { it.name == "Moon" }
        val asc = chart.ascendant
        val sections = mutableListOf<ReadingSection>()

        // 1. Overview
        val date = chart.birthData.dateTime.format(dateFmt(lang))
        val place = chart.birthData.locationName
        val birth = birthPhrase(date, place, lang)
        sections += ReadingSection(
            title(lang, 0),
            listOf(bornLine(who, birth, lang), bigThree(who, sun?.sign, moon?.sign, asc.sign, lang))
        )

        // 2. Core self (the "big three")
        val core = mutableListOf<String>()
        sun?.let { core += placementSentence(it, lang) }
        moon?.let { core += placementSentence(it, lang) }
        core += risingSentence(asc.sign, lang)
        sections += ReadingSection(title(lang, 1), core)

        // 3. All placements, in canonical planet order
        val placements = PlanetInfo.order
            .mapNotNull { pn -> chart.planets.firstOrNull { it.name == pn } }
            .map { placementSentence(it, lang) }
        sections += ReadingSection(title(lang, 2), placements)

        // 4. Key aspects (tightest orbs first)
        val topAspects = chart.aspects.sortedBy { it.orb }.take(5)
        val aspectParas = if (topAspects.isEmpty()) {
            listOf(noAspects(lang))
        } else {
            topAspects.map { a ->
                val orb = String.format(Locale.US, "%.1f", a.orb)
                val interp = AspectInterpretationProvider.getInterpretation(a.bodyA, a.bodyB, a.type, lang)
                aspectLine(
                    Translations.planetName(a.bodyA, lang),
                    Translations.aspectType(a.type, lang),
                    Translations.planetName(a.bodyB, lang),
                    orb, interp, lang, a.type
                )
            }
        }
        sections += ReadingSection(title(lang, 3), aspectParas)

        // 5. Elemental & modality balance
        sections += ReadingSection(title(lang, 4), balanceParagraphs(chart, lang))

        return sections
    }

    // ----- Section titles ----------------------------------------------------

    private fun title(lang: Language, index: Int): String = when (lang.content) {
        ContentLang.EN -> listOf(
            "Overview", "Core self", "The placements", "Key aspects",
            "Elemental & modality balance"
        )[index]
        ContentLang.TA -> listOf(
            "மேற்பார்வை", "மைய சுயம்", "கிரக நிலைகள்", "முக்கிய கோணங்கள்",
            "பூத & குண சமநிலை"
        )[index]
        ContentLang.ZH -> listOf(
            "概览", "核心自我", "行星落点", "主要相位", "元素与模式平衡"
        )[index]
    }

    private fun defaultWho(lang: Language): String = when (lang.content) {
        ContentLang.EN -> "This person"
        ContentLang.TA -> "இந்த நபர்"
        ContentLang.ZH -> "此人"
    }

    // ----- Overview ----------------------------------------------------------

    private fun birthPhrase(date: String, place: String, lang: Language): String {
        if (place.isBlank()) return when (lang.content) {
            ContentLang.TA -> "$date அன்று"
            else -> date
        }
        return when (lang.content) {
            ContentLang.EN -> "$date in $place"
            ContentLang.TA -> "$place-ல் $date அன்று"
            ContentLang.ZH -> "$place，$date"
        }
    }

    private fun bornLine(who: String, birth: String, lang: Language): String = when (lang.content) {
        ContentLang.EN -> "$who was born on $birth."
        ContentLang.TA -> "$who $birth பிறந்தார்."
        ContentLang.ZH -> "${who}出生于${birth}。"
    }

    private fun bigThree(who: String, sunSign: String?, moonSign: String?, ascSign: String, lang: Language): String {
        val sun = sunSign ?: return risingSentence(ascSign, lang)
        val moon = moonSign ?: sun
        val sunN = Translations.signName(sun, lang)
        val moonN = Translations.signName(moon, lang)
        val ascN = Translations.signName(ascSign, lang)
        val sunKw = Translations.signKeywords(sun, lang)
        val moonKw = Translations.signKeywords(moon, lang)
        val ascKw = Translations.signKeywords(ascSign, lang)
        return when (lang.content) {
            ContentLang.EN -> "With the Sun in $sunN, the Moon in $moonN, and $ascN rising, " +
                "$who blends a $sunKw core, $moonKw emotions, and a $ascKw first impression."
            ContentLang.TA -> "$sunN ராசியில் சூரியன், $moonN ராசியில் சந்திரன், $ascN லக்னம் — $who ஒரு $sunKw சுயத்தையும், " +
                "$moonKw உணர்வுகளையும், $ascKw முதல் தோற்றத்தையும் இணைக்கிறார்."
            ContentLang.ZH -> "太阳在${sunN}、月亮在${moonN}、${ascN}上升，${who}融合了${sunKw}的自我，" +
                "${moonKw}的情感，以及${ascKw}的第一印象。"
        }
    }

    // ----- Core self ---------------------------------------------------------

    private fun risingSentence(ascSign: String, lang: Language): String {
        val asc = Translations.signName(ascSign, lang)
        val kw = Translations.signKeywords(ascSign, lang)
        return when (lang.content) {
            ContentLang.EN -> "With $asc rising, you meet the world in a $kw way — it shapes your " +
                "instincts, style, and the first impression you make."
            ContentLang.TA -> "$asc லக்னத்துடன், நீங்கள் உலகை $kw விதத்தில் சந்திக்கிறீர்கள் — இது உங்கள் " +
                "உள்ளுணர்வையும், பாணியையும், முதல் தோற்றத்தையும் வடிவமைக்கிறது."
            ContentLang.ZH -> "${asc}上升，你以${kw}的方式面对世界——它塑造了你的直觉、风格与第一印象。"
        }
    }

    // ----- Placements --------------------------------------------------------

    private fun placementSentence(p: PlanetaryPosition, lang: Language): String {
        val glyph = PlanetInfo.glyph(p.name)
        val planet = Translations.planetName(p.name, lang)
        val sign = Translations.signName(p.sign, lang)
        val role = Translations.planetRole(p.name, lang)
        val kw = Translations.signKeywords(p.sign, lang)
        val area = Translations.houseArea(p.house, lang)
        return when (lang.content) {
            ContentLang.EN -> "$glyph $planet in $sign (House ${p.house}) — you express your $role " +
                "in a $kw way, focusing on $area."
            ContentLang.TA -> "$glyph $planet — $sign ராசியில் (${p.house}ஆம் வீடு): உங்கள் $role $kw " +
                "விதத்தில் வெளிப்படுகிறது, $area தொடர்பாக."
            ContentLang.ZH -> "$glyph ${planet}在${sign}（第${p.house}宫）——你的${role}以${kw}的方式展现，聚焦于${area}。"
        }
    }

    // ----- Key aspects -------------------------------------------------------

    private fun noAspects(lang: Language): String = when (lang.content) {
        ContentLang.EN -> "No major aspects fall within orb here — the planets each act fairly independently."
        ContentLang.TA -> "இங்கு இடைவெளிக்குள் பெரிய கோணங்கள் எதுவும் இல்லை — கிரகங்கள் ஒவ்வொன்றும் தனித்தனியே செயல்படுகின்றன."
        ContentLang.ZH -> "此处没有落在容许度内的主要相位——各行星相对独立地运作。"
    }

    private fun aspectLine(
        a: String, type: String, b: String, orb: String, interp: String,
        lang: Language, rawType: String
    ): String = when (lang.content) {
        ContentLang.EN -> "$a ${rawType.lowercase()} $b (orb $orb°): $interp"
        ContentLang.TA -> "$a–$b $type ($orb° இடைவெளி): $interp"
        ContentLang.ZH -> "$a$type$b（容许度 $orb°）：$interp"
    }

    // ----- Balance -----------------------------------------------------------

    private fun balanceParagraphs(chart: NatalChart, lang: Language): List<String> {
        val el = chart.balance.elements
        val mo = chart.balance.modalities
        val paras = mutableListOf<String>()

        el.maxByOrNull { it.value }?.key?.let { paras += dominantElementText(it, lang) }

        val missing = ELEMENTS.filter { (el[it] ?: 0) == 0 }
        if (missing.isNotEmpty()) {
            val sep = when (lang.content) {
                ContentLang.ZH -> "、"
                ContentLang.EN -> " or "
                ContentLang.TA -> ", "
            }
            val names = missing.joinToString(sep) { Translations.element(it, lang) }
            paras += lackingElementText(names, missing.first(), lang)
        }

        mo.maxByOrNull { it.value }?.key?.let { paras += modalityText(it, lang) }
        return paras
    }

    private fun dominantElementText(e: String, lang: Language): String = when (lang.content) {
        ContentLang.EN -> when (e) {
            "Fire" -> "Fire is emphasised: energetic, enthusiastic, and action-oriented, you lead with warmth and spontaneity."
            "Earth" -> "Earth is emphasised: grounded, practical, and reliable, you build steadily and value what is tangible."
            "Air" -> "Air is emphasised: intellectual, curious, and social, you process life through ideas and connection."
            else -> "Water is emphasised: emotional, intuitive, and sensitive, you feel your way through life and attune to others."
        }
        ContentLang.TA -> when (e) {
            "Fire" -> "நெருப்பு மேலோங்குகிறது: சுறுசுறுப்பான, உற்சாகமான, செயல்முனைப்பான நீங்கள் அரவணைப்புடனும் தன்னிச்சையாகவும் வழிநடத்துகிறீர்கள்."
            "Earth" -> "மண் மேலோங்குகிறது: உறுதியான, நடைமுறையான, நம்பகமான நீங்கள் நிதானமாகக் கட்டமைத்து உறுதியானவற்றை மதிக்கிறீர்கள்."
            "Air" -> "காற்று மேலோங்குகிறது: அறிவார்ந்த, ஆர்வமுள்ள, சமூகப்பண்புள்ள நீங்கள் கருத்துகள் மற்றும் தொடர்பின் மூலம் வாழ்க்கையை அணுகுகிறீர்கள்."
            else -> "நீர் மேலோங்குகிறது: உணர்ச்சிமிக்க, உள்ளுணர்வுள்ள, உணர்திறன் கொண்ட நீங்கள் உணர்வின் வழியே வாழ்ந்து பிறருடன் ஒத்திசைகிறீர்கள்."
        }
        ContentLang.ZH -> when (e) {
            "Fire" -> "火元素突出：精力充沛、热情、行动导向，你以温暖与自发引领。"
            "Earth" -> "土元素突出：踏实、务实、可靠，你稳步建设并重视实在之物。"
            "Air" -> "风元素突出：理智、好奇、善交际，你透过思想与联结理解人生。"
            else -> "水元素突出：情感丰富、直觉敏锐、细腻，你凭感受生活并与他人共情。"
        }
    }

    private fun lackingElementText(names: String, first: String, lang: Language): String = when (lang.content) {
        ContentLang.EN -> "There is little or no $names energy — " + when (first) {
            "Fire" -> "cultivating initiative and self-assertion can bring balance."
            "Earth" -> "grounding routines and follow-through are worth developing."
            "Air" -> "stepping back for objectivity and dialogue can help."
            else -> "making room for feelings and empathy can bring balance."
        }
        ContentLang.TA -> "$names ஆற்றல் மிகக் குறைவு அல்லது இல்லை — " + when (first) {
            "Fire" -> "முன்முயற்சியையும் தன்னம்பிக்கையையும் வளர்ப்பது சமநிலையைத் தரும்."
            "Earth" -> "நிலையான வழக்கங்களையும் நிறைவேற்றும் திறனையும் வளர்ப்பது நல்லது."
            "Air" -> "பொருள்நிலைப் பார்வைக்கும் உரையாடலுக்கும் சற்று விலகி நிற்பது உதவும்."
            else -> "உணர்வுகளுக்கும் இரக்கத்துக்கும் இடம் கொடுப்பது சமநிலையைத் தரும்."
        }
        ContentLang.ZH -> "${names}元素的能量很少或缺失——" + when (first) {
            "Fire" -> "培养主动性与自我主张能带来平衡。"
            "Earth" -> "值得培养踏实的日常与执行力。"
            "Air" -> "适时抽离以获得客观视角与对话会有帮助。"
            else -> "为情感与同理心留出空间能带来平衡。"
        }
    }

    private fun modalityText(m: String, lang: Language): String = when (lang.content) {
        ContentLang.EN -> when (m) {
            "Cardinal" -> "Cardinal energy dominates — you are an initiator, happiest starting things and setting direction."
            "Fixed" -> "Fixed energy dominates — you are steady and determined, with real staying power once committed."
            else -> "Mutable energy dominates — you are adaptable and flexible, at ease with change and variety."
        }
        ContentLang.TA -> when (m) {
            "Cardinal" -> "சர ஆற்றல் மேலோங்குகிறது — நீங்கள் ஒரு தொடக்கக்காரர், புதிதாகத் தொடங்குவதிலும் திசை அமைப்பதிலும் மகிழ்ச்சி காண்கிறீர்கள்."
            "Fixed" -> "ஸ்திர ஆற்றல் மேலோங்குகிறது — நீங்கள் உறுதியானவர், தீர்மானமானவர், ஒருமுறை உறுதிபூண்டால் நிலைத்திருக்கும் சக்தி கொண்டவர்."
            else -> "உபய ஆற்றல் மேலோங்குகிறது — நீங்கள் தகவமைப்புத் திறன் கொண்டவர், நெகிழ்வானவர், மாற்றத்திலும் பன்முகத்தன்மையிலும் எளிதாக இருப்பவர்."
        }
        ContentLang.ZH -> when (m) {
            "Cardinal" -> "基本模式主导——你是发起者，最乐于开创与定方向。"
            "Fixed" -> "固定模式主导——你稳定而坚定，一旦投入便有持久的耐力。"
            else -> "变动模式主导——你适应力强、灵活，乐于面对变化与多样。"
        }
    }
}
