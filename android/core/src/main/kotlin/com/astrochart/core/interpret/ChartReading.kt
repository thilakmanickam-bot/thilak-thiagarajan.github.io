package com.astrochart.core.interpret

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

    private fun dateFmt(lang: Language): DateTimeFormatter = when (lang) {
        Language.EN -> DateTimeFormatter.ofPattern("d MMM yyyy 'at' HH:mm", lang.locale)
        Language.ZH -> DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm", lang.locale)
        else -> DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", lang.locale)
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

    private fun title(lang: Language, index: Int): String = when (lang) {
        Language.TA -> listOf(
            "மேற்பார்வை", "மைய சுயம்", "கிரக நிலைகள்", "முக்கிய கோணங்கள்",
            "பூத & குண சமநிலை"
        )[index]
        Language.ZH -> listOf(
            "概览", "核心自我", "行星落点", "主要相位", "元素与模式平衡"
        )[index]
        Language.HI -> listOf(
            "अवलोकन", "मूल स्व", "ग्रह स्थितियाँ", "प्रमुख दृष्टियाँ", "तत्व व गुण संतुलन"
        )[index]
        Language.TE -> listOf(
            "అవలోకనం", "మూల స్వభావం", "గ్రహ స్థానాలు", "ముఖ్య దృష్టులు", "మూలకం & గుణ సమతుల్యం"
        )[index]
        Language.KN -> listOf(
            "ಅವಲೋಕನ", "ಮೂಲ ಸ್ವಭಾವ", "ಗ್ರಹ ಸ್ಥಾನಗಳು", "ಪ್ರಮುಖ ದೃಷ್ಟಿಗಳು", "ಮೂಲಧಾತು & ಗುಣ ಸಮತೋಲನ"
        )[index]
        Language.ML -> listOf(
            "അവലോകനം", "മൂല സ്വത്വം", "ഗ്രഹ സ്ഥാനങ്ങൾ", "പ്രധാന ദൃഷ്ടികൾ", "മൂലകം & ഗുണ സന്തുലനം"
        )[index]
        Language.MR -> listOf(
            "आढावा", "मूळ स्व", "ग्रहस्थिती", "प्रमुख दृष्टी", "तत्त्व व गुण संतुलन"
        )[index]
        else -> listOf(
            "Overview", "Core self", "The placements", "Key aspects",
            "Elemental & modality balance"
        )[index]
    }

    private fun defaultWho(lang: Language): String = when (lang) {
        Language.TA -> "இந்த நபர்"
        Language.ZH -> "此人"
        Language.HI -> "यह व्यक्ति"
        Language.TE -> "ఈ వ్యక్తి"
        Language.KN -> "ಈ ವ್ಯಕ್ತಿ"
        Language.ML -> "ഈ വ്യക്തി"
        Language.MR -> "ही व्यक्ती"
        else -> "This person"
    }

    // ----- Overview ----------------------------------------------------------

    private fun birthPhrase(date: String, place: String, lang: Language): String {
        if (place.isBlank()) return when (lang) {
            Language.TA -> "$date அன்று"
            else -> date
        }
        return when (lang) {
            Language.TA -> "$place-ல் $date அன்று"
            Language.ZH -> "$place，$date"
            Language.HI -> "$place में $date"
            Language.TE -> "$place లో $date"
            Language.KN -> "$place ನಲ್ಲಿ $date"
            Language.ML -> "$place-ൽ $date"
            Language.MR -> "$place मध्ये $date"
            else -> "$date in $place"
        }
    }

    private fun bornLine(who: String, birth: String, lang: Language): String = when (lang) {
        Language.TA -> "$who $birth பிறந்தார்."
        Language.ZH -> "${who}出生于${birth}。"
        Language.HI -> "${who} का जन्म ${birth} को हुआ।"
        Language.TE -> "${who} ${birth} జన్మించారు."
        Language.KN -> "${who} ${birth} ರಂದು ಜನಿಸಿದರು."
        Language.ML -> "${who} ${birth}-ന് ജനിച്ചു."
        Language.MR -> "${who} यांचा जन्म ${birth} रोजी झाला."
        else -> "$who was born on $birth."
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
        return when (lang) {
            Language.TA -> "$sunN ராசியில் சூரியன், $moonN ராசியில் சந்திரன், $ascN லக்னம் — $who ஒரு $sunKw சுயத்தையும், " +
                "$moonKw உணர்வுகளையும், $ascKw முதல் தோற்றத்தையும் இணைக்கிறார்."
            Language.ZH -> "太阳在${sunN}、月亮在${moonN}、${ascN}上升，${who}融合了${sunKw}的自我，" +
                "${moonKw}的情感，以及${ascKw}的第一印象。"
            Language.HI -> "सूर्य ${sunN} में, चंद्रमा ${moonN} में, और ${ascN} लग्न — ${who} एक ${sunKw} स्व, " +
                "${moonKw} भावनाएँ, और ${ascKw} पहली छाप को जोड़ते हैं।"
            Language.TE -> "సూర్యుడు ${sunN} లో, చంద్రుడు ${moonN} లో, ${ascN} లగ్నం — ${who} ${sunKw} స్వభావాన్ని, " +
                "${moonKw} భావాలను, ${ascKw} మొదటి ముద్రను మేళవిస్తారు."
            Language.KN -> "ಸೂರ್ಯ ${sunN} ನಲ್ಲಿ, ಚಂದ್ರ ${moonN} ನಲ್ಲಿ, ${ascN} ಲಗ್ನ — ${who} ${sunKw} ಸ್ವಭಾವ, " +
                "${moonKw} ಭಾವನೆಗಳು, ${ascKw} ಮೊದಲ ಪ್ರಭಾವವನ್ನು ಬೆಸೆಯುತ್ತಾರೆ."
            Language.ML -> "സൂര്യൻ ${sunN}-ൽ, ചന്ദ്രൻ ${moonN}-ൽ, ${ascN} ലഗ്നം — ${who} ${sunKw} സ്വത്വം, " +
                "${moonKw} വികാരങ്ങൾ, ${ascKw} ആദ്യ പ്രതീതി എന്നിവ സമ്മേളിപ്പിക്കുന്നു."
            Language.MR -> "सूर्य ${sunN} मध्ये, चंद्र ${moonN} मध्ये, आणि ${ascN} लग्न — ${who} ${sunKw} स्व, " +
                "${moonKw} भावना आणि ${ascKw} पहिली छाप एकत्र करतात."
            else -> "With the Sun in $sunN, the Moon in $moonN, and $ascN rising, " +
                "$who blends a $sunKw core, $moonKw emotions, and a $ascKw first impression."
        }
    }

    // ----- Core self ---------------------------------------------------------

    private fun risingSentence(ascSign: String, lang: Language): String {
        val asc = Translations.signName(ascSign, lang)
        val kw = Translations.signKeywords(ascSign, lang)
        return when (lang) {
            Language.TA -> "$asc லக்னத்துடன், நீங்கள் உலகை $kw விதத்தில் சந்திக்கிறீர்கள் — இது உங்கள் " +
                "உள்ளுணர்வையும், பாணியையும், முதல் தோற்றத்தையும் வடிவமைக்கிறது."
            Language.ZH -> "${asc}上升，你以${kw}的方式面对世界——它塑造了你的直觉、风格与第一印象。"
            Language.HI -> "${asc} लग्न के साथ, आप दुनिया से ${kw} ढंग से मिलते हैं — यह आपकी सहज-वृत्ति, " +
                "शैली और पहली छाप को आकार देता है।"
            Language.TE -> "${asc} లగ్నంతో, మీరు ప్రపంచాన్ని ${kw} విధంగా ఎదుర్కొంటారు — ఇది మీ సహజ ప్రవృత్తి, " +
                "శైలి, మొదటి ముద్రను తీర్చిదిద్దుతుంది."
            Language.KN -> "${asc} ಲಗ್ನದೊಂದಿಗೆ, ನೀವು ಜಗತ್ತನ್ನು ${kw} ರೀತಿಯಲ್ಲಿ ಎದುರುಗೊಳ್ಳುತ್ತೀರಿ — ಇದು ನಿಮ್ಮ ಸಹಜ ಪ್ರವೃತ್ತಿ, " +
                "ಶೈಲಿ ಮತ್ತು ಮೊದಲ ಪ್ರಭಾವವನ್ನು ರೂಪಿಸುತ್ತದೆ."
            Language.ML -> "${asc} ലഗ്നത്തോടെ, നിങ്ങൾ ലോകത്തെ ${kw} രീതിയിൽ അഭിമുഖീകരിക്കുന്നു — ഇത് നിങ്ങളുടെ സഹജവാസന, " +
                "ശൈലി, ആദ്യ പ്രതീതി എന്നിവ രൂപപ്പെടുത്തുന്നു."
            Language.MR -> "${asc} लग्नासह, तुम्ही जगाला ${kw} पद्धतीने सामोरे जाता — ते तुमची अंतःप्रेरणा, " +
                "शैली आणि पहिली छाप घडवते."
            else -> "With $asc rising, you meet the world in a $kw way — it shapes your " +
                "instincts, style, and the first impression you make."
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
        return when (lang) {
            Language.TA -> "$glyph $planet — $sign ராசியில் (${p.house}ஆம் வீடு): உங்கள் $role $kw " +
                "விதத்தில் வெளிப்படுகிறது, $area தொடர்பாக."
            Language.ZH -> "$glyph ${planet}在${sign}（第${p.house}宫）——你的${role}以${kw}的方式展现，聚焦于${area}。"
            Language.HI -> "$glyph ${planet} ${sign} में (भाव ${p.house}) — आप अपने ${role} को ${kw} ढंग से " +
                "व्यक्त करते हैं, ${area} पर केंद्रित।"
            Language.TE -> "$glyph ${planet} ${sign} లో (భావం ${p.house}) — మీరు మీ ${role} ను ${kw} విధంగా " +
                "వ్యక్తం చేస్తారు, ${area} పై దృష్టితో."
            Language.KN -> "$glyph ${planet} ${sign} ನಲ್ಲಿ (ಭಾವ ${p.house}) — ನೀವು ನಿಮ್ಮ ${role} ಅನ್ನು ${kw} ರೀತಿಯಲ್ಲಿ " +
                "ವ್ಯಕ್ತಪಡಿಸುತ್ತೀರಿ, ${area} ಮೇಲೆ ಕೇಂದ್ರೀಕರಿಸಿ."
            Language.ML -> "$glyph ${planet} ${sign}-ൽ (ഭാവം ${p.house}) — നിങ്ങൾ നിങ്ങളുടെ ${role} ${kw} രീതിയിൽ " +
                "പ്രകടിപ്പിക്കുന്നു, ${area}-ൽ ശ്രദ്ധ കേന്ദ്രീകരിച്ച്."
            Language.MR -> "$glyph ${planet} ${sign} मध्ये (भाव ${p.house}) — तुम्ही तुमचा ${role} ${kw} पद्धतीने " +
                "व्यक्त करता, ${area} वर लक्ष केंद्रित करून."
            else -> "$glyph $planet in $sign (House ${p.house}) — you express your $role " +
                "in a $kw way, focusing on $area."
        }
    }

    // ----- Key aspects -------------------------------------------------------

    private fun noAspects(lang: Language): String = when (lang) {
        Language.TA -> "இங்கு இடைவெளிக்குள் பெரிய கோணங்கள் எதுவும் இல்லை — கிரகங்கள் ஒவ்வொன்றும் தனித்தனியே செயல்படுகின்றன."
        Language.ZH -> "此处没有落在容许度内的主要相位——各行星相对独立地运作。"
        Language.HI -> "यहाँ कोई प्रमुख दृष्टि कक्षा के भीतर नहीं आती — प्रत्येक ग्रह काफ़ी हद तक स्वतंत्र रूप से कार्य करता है।"
        Language.TE -> "ఇక్కడ ఏ ముఖ్య దృష్టీ కక్ష్యలోకి రావడం లేదు — ప్రతి గ్రహం చాలావరకు స్వతంత్రంగా పనిచేస్తుంది."
        Language.KN -> "ಇಲ್ಲಿ ಯಾವುದೇ ಪ್ರಮುಖ ದೃಷ್ಟಿ ಕಕ್ಷೆಯೊಳಗೆ ಬರುವುದಿಲ್ಲ — ಪ್ರತಿ ಗ್ರಹವೂ ಬಹುಪಾಲು ಸ್ವತಂತ್ರವಾಗಿ ವರ್ತಿಸುತ್ತದೆ."
        Language.ML -> "ഇവിടെ പ്രധാന ദൃഷ്ടികളൊന്നും പരിധിക്കുള്ളിൽ വരുന്നില്ല — ഓരോ ഗ്രഹവും ഏറെക്കുറെ സ്വതന്ത്രമായി പ്രവർത്തിക്കുന്നു."
        Language.MR -> "इथे कोणतीही प्रमुख दृष्टी कक्षेत येत नाही — प्रत्येक ग्रह बऱ्यापैकी स्वतंत्रपणे कार्य करतो."
        else -> "No major aspects fall within orb here — the planets each act fairly independently."
    }

    private fun aspectLine(
        a: String, type: String, b: String, orb: String, interp: String,
        lang: Language, rawType: String
    ): String = when (lang) {
        Language.TA -> "$a–$b $type ($orb° இடைவெளி): $interp"
        Language.ZH -> "$a$type$b（容许度 $orb°）：$interp"
        Language.HI -> "$a–$b $type ($orb° कक्षा): $interp"
        Language.TE -> "$a–$b $type ($orb° కక్ష్య): $interp"
        Language.KN -> "$a–$b $type ($orb° ಕಕ್ಷೆ): $interp"
        Language.ML -> "$a–$b $type ($orb° പരിധി): $interp"
        Language.MR -> "$a–$b $type ($orb° कक्षा): $interp"
        else -> "$a ${rawType.lowercase()} $b (orb $orb°): $interp"
    }

    // ----- Balance -----------------------------------------------------------

    private fun balanceParagraphs(chart: NatalChart, lang: Language): List<String> {
        val el = chart.balance.elements
        val mo = chart.balance.modalities
        val paras = mutableListOf<String>()

        el.maxByOrNull { it.value }?.key?.let { paras += dominantElementText(it, lang) }

        val missing = ELEMENTS.filter { (el[it] ?: 0) == 0 }
        if (missing.isNotEmpty()) {
            val sep = when (lang) {
                Language.ZH -> "、"
                Language.EN -> " or "
                else -> ", "
            }
            val names = missing.joinToString(sep) { Translations.element(it, lang) }
            paras += lackingElementText(names, missing.first(), lang)
        }

        mo.maxByOrNull { it.value }?.key?.let { paras += modalityText(it, lang) }
        return paras
    }

    private fun dominantElementText(e: String, lang: Language): String = when (lang) {
        Language.TA -> when (e) {
            "Fire" -> "நெருப்பு மேலோங்குகிறது: சுறுசுறுப்பான, உற்சாகமான, செயல்முனைப்பான நீங்கள் அரவணைப்புடனும் தன்னிச்சையாகவும் வழிநடத்துகிறீர்கள்."
            "Earth" -> "மண் மேலோங்குகிறது: உறுதியான, நடைமுறையான, நம்பகமான நீங்கள் நிதானமாகக் கட்டமைத்து உறுதியானவற்றை மதிக்கிறீர்கள்."
            "Air" -> "காற்று மேலோங்குகிறது: அறிவார்ந்த, ஆர்வமுள்ள, சமூகப்பண்புள்ள நீங்கள் கருத்துகள் மற்றும் தொடர்பின் மூலம் வாழ்க்கையை அணுகுகிறீர்கள்."
            else -> "நீர் மேலோங்குகிறது: உணர்ச்சிமிக்க, உள்ளுணர்வுள்ள, உணர்திறன் கொண்ட நீங்கள் உணர்வின் வழியே வாழ்ந்து பிறருடன் ஒத்திசைகிறீர்கள்."
        }
        Language.ZH -> when (e) {
            "Fire" -> "火元素突出：精力充沛、热情、行动导向，你以温暖与自发引领。"
            "Earth" -> "土元素突出：踏实、务实、可靠，你稳步建设并重视实在之物。"
            "Air" -> "风元素突出：理智、好奇、善交际，你透过思想与联结理解人生。"
            else -> "水元素突出：情感丰富、直觉敏锐、细腻，你凭感受生活并与他人共情。"
        }
        Language.HI -> when (e) {
            "Fire" -> "अग्नि प्रबल है: ऊर्जावान, उत्साही और क्रियाशील, आप गर्मजोशी और सहजता से नेतृत्व करते हैं।"
            "Earth" -> "पृथ्वी प्रबल है: व्यावहारिक, स्थिर और भरोसेमंद, आप धीरे-धीरे निर्माण करते और मूर्त चीज़ों को महत्व देते हैं।"
            "Air" -> "वायु प्रबल है: बौद्धिक, जिज्ञासु और सामाजिक, आप विचारों और संबंधों के माध्यम से जीवन को समझते हैं।"
            else -> "जल प्रबल है: भावुक, अंतर्ज्ञानी और संवेदनशील, आप भावनाओं के सहारे जीते और दूसरों से जुड़ते हैं।"
        }
        Language.TE -> when (e) {
            "Fire" -> "అగ్ని ప్రబలంగా ఉంది: శక్తివంతమైన, ఉత్సాహభరిత, చురుకైన మీరు ఆప్యాయత, సహజత్వంతో నడిపిస్తారు."
            "Earth" -> "పృథ్వి ప్రబలంగా ఉంది: ఆచరణాత్మక, స్థిర, నమ్మదగిన మీరు నిదానంగా నిర్మిస్తూ స్పష్టమైన వాటిని విలువైనవిగా భావిస్తారు."
            "Air" -> "వాయువు ప్రబలంగా ఉంది: మేధావంతమైన, కుతూహలంగల, సామాజిక మీరు ఆలోచనలు, అనుబంధాల ద్వారా జీవితాన్ని అర్థం చేసుకుంటారు."
            else -> "జలం ప్రబలంగా ఉంది: భావోద్వేగ, అంతర్ దృష్టిగల, సున్నితమైన మీరు భావాల ద్వారా జీవిస్తూ ఇతరులతో మమేకమవుతారు."
        }
        Language.KN -> when (e) {
            "Fire" -> "ಅಗ್ನಿ ಪ್ರಬಲವಾಗಿದೆ: ಚೈತನ್ಯಶೀಲ, ಉತ್ಸಾಹಿ ಮತ್ತು ಕ್ರಿಯಾಶೀಲ ನೀವು ಬೆಚ್ಚನೆಯ ಮತ್ತು ಸಹಜತೆಯಿಂದ ಮುನ್ನಡೆಸುತ್ತೀರಿ."
            "Earth" -> "ಪೃಥ್ವಿ ಪ್ರಬಲವಾಗಿದೆ: ಪ್ರಾಯೋಗಿಕ, ಸ್ಥಿರ ಮತ್ತು ನಂಬಬಹುದಾದ ನೀವು ನಿಧಾನವಾಗಿ ಕಟ್ಟುತ್ತಾ ಸ್ಪಷ್ಟವಾದವುಗಳನ್ನು ಮೌಲ್ಯೀಕರಿಸುತ್ತೀರಿ."
            "Air" -> "ವಾಯು ಪ್ರಬಲವಾಗಿದೆ: ಬೌದ್ಧಿಕ, ಕುತೂಹಲಿ ಮತ್ತು ಸಾಮಾಜಿಕ ನೀವು ವಿಚಾರ ಮತ್ತು ಸಂಬಂಧಗಳ ಮೂಲಕ ಜೀವನವನ್ನು ಅರ್ಥಮಾಡಿಕೊಳ್ಳುತ್ತೀರಿ."
            else -> "ಜಲ ಪ್ರಬಲವಾಗಿದೆ: ಭಾವನಾತ್ಮಕ, ಅಂತಃಪ್ರಜ್ಞೆಯ ಮತ್ತು ಸೂಕ್ಷ್ಮ ನೀವು ಭಾವನೆಗಳ ಮೂಲಕ ಬದುಕುತ್ತಾ ಇತರರೊಂದಿಗೆ ಹೊಂದಿಕೊಳ್ಳುತ್ತೀರಿ."
        }
        Language.ML -> when (e) {
            "Fire" -> "അഗ്നി പ്രബലമാണ്: ഊർജസ്വലരും ഉത്സാഹികളും കർമനിരതരുമായ നിങ്ങൾ ഊഷ്മളതയോടും സ്വാഭാവികതയോടും നയിക്കുന്നു."
            "Earth" -> "ഭൂമി പ്രബലമാണ്: പ്രായോഗികരും സ്ഥിരരും വിശ്വാസയോഗ്യരുമായ നിങ്ങൾ പതിയെ പടുത്തുയർത്തുകയും വ്യക്തമായവയ്ക്ക് വില കൽപ്പിക്കുകയും ചെയ്യുന്നു."
            "Air" -> "വായു പ്രബലമാണ്: ബൗദ്ധികരും ജിജ്ഞാസുക്കളും സാമൂഹികരുമായ നിങ്ങൾ ആശയങ്ങളിലൂടെയും ബന്ധങ്ങളിലൂടെയും ജീവിതത്തെ ഗ്രഹിക്കുന്നു."
            else -> "ജലം പ്രബലമാണ്: വികാരജീവികളും അന്തർജ്ഞാനികളും സൂക്ഷ്മരുമായ നിങ്ങൾ വികാരങ്ങളിലൂടെ ജീവിക്കുകയും മറ്റുള്ളവരോട് ഇണങ്ങുകയും ചെയ്യുന്നു."
        }
        Language.MR -> when (e) {
            "Fire" -> "अग्नी प्रबळ आहे: ऊर्जावान, उत्साही व कृतिशील तुम्ही उबदारपणे व सहजतेने नेतृत्व करता."
            "Earth" -> "पृथ्वी प्रबळ आहे: व्यावहारिक, स्थिर व विश्वासार्ह तुम्ही सावकाश उभारणी करता व मूर्त गोष्टींना महत्त्व देता."
            "Air" -> "वायू प्रबळ आहे: बौद्धिक, जिज्ञासू व सामाजिक तुम्ही कल्पना व नातेसंबंधांतून जीवन समजून घेता."
            else -> "जल प्रबळ आहे: भावनाशील, अंतर्ज्ञानी व संवेदनशील तुम्ही भावनांच्या आधारे जगता व इतरांशी जुळवून घेता."
        }
        else -> when (e) {
            "Fire" -> "Fire is emphasised: energetic, enthusiastic, and action-oriented, you lead with warmth and spontaneity."
            "Earth" -> "Earth is emphasised: grounded, practical, and reliable, you build steadily and value what is tangible."
            "Air" -> "Air is emphasised: intellectual, curious, and social, you process life through ideas and connection."
            else -> "Water is emphasised: emotional, intuitive, and sensitive, you feel your way through life and attune to others."
        }
    }

    private fun lackingElementText(names: String, first: String, lang: Language): String = when (lang) {
        Language.TA -> "$names ஆற்றல் மிகக் குறைவு அல்லது இல்லை — " + when (first) {
            "Fire" -> "முன்முயற்சியையும் தன்னம்பிக்கையையும் வளர்ப்பது சமநிலையைத் தரும்."
            "Earth" -> "நிலையான வழக்கங்களையும் நிறைவேற்றும் திறனையும் வளர்ப்பது நல்லது."
            "Air" -> "பொருள்நிலைப் பார்வைக்கும் உரையாடலுக்கும் சற்று விலகி நிற்பது உதவும்."
            else -> "உணர்வுகளுக்கும் இரக்கத்துக்கும் இடம் கொடுப்பது சமநிலையைத் தரும்."
        }
        Language.ZH -> "${names}元素的能量很少或缺失——" + when (first) {
            "Fire" -> "培养主动性与自我主张能带来平衡。"
            "Earth" -> "值得培养踏实的日常与执行力。"
            "Air" -> "适时抽离以获得客观视角与对话会有帮助。"
            else -> "为情感与同理心留出空间能带来平衡。"
        }
        Language.HI -> "${names} ऊर्जा बहुत कम या नहीं है — " + when (first) {
            "Fire" -> "पहल और आत्म-अभिव्यक्ति विकसित करना संतुलन ला सकता है।"
            "Earth" -> "स्थिर दिनचर्या और पूर्णता तक ले जाना विकसित करने योग्य है।"
            "Air" -> "वस्तुनिष्ठता और संवाद के लिए थोड़ा पीछे हटना मदद कर सकता है।"
            else -> "भावनाओं और सहानुभूति के लिए जगह बनाना संतुलन ला सकता है।"
        }
        Language.TE -> "${names} శక్తి చాలా తక్కువ లేదా లేదు — " + when (first) {
            "Fire" -> "చొరవను, ఆత్మ ప్రకటనను పెంపొందించడం సమతుల్యతను తెస్తుంది."
            "Earth" -> "స్థిర దినచర్యలు, పూర్తి చేసే గుణం అభివృద్ధి చేయదగినవి."
            "Air" -> "వస్తునిష్ఠత, సంభాషణ కోసం కొంత వెనక్కి తగ్గడం సహాయపడుతుంది."
            else -> "భావాలకు, సానుభూతికి చోటు ఇవ్వడం సమతుల్యతను తెస్తుంది."
        }
        Language.KN -> "${names} ಶಕ್ತಿ ಬಹಳ ಕಡಿಮೆ ಅಥವಾ ಇಲ್ಲ — " + when (first) {
            "Fire" -> "ಉಪಕ್ರಮ ಮತ್ತು ಆತ್ಮಾಭಿವ್ಯಕ್ತಿಯನ್ನು ಬೆಳೆಸುವುದು ಸಮತೋಲನ ತರುತ್ತದೆ."
            "Earth" -> "ಸ್ಥಿರ ದಿನಚರಿ ಮತ್ತು ಪೂರ್ಣಗೊಳಿಸುವಿಕೆ ಬೆಳೆಸಲು ಯೋಗ್ಯ."
            "Air" -> "ವಸ್ತುನಿಷ್ಠತೆ ಮತ್ತು ಸಂವಾದಕ್ಕಾಗಿ ಸ್ವಲ್ಪ ಹಿಂದೆ ಸರಿಯುವುದು ಸಹಾಯಕ."
            else -> "ಭಾವನೆಗಳಿಗೆ ಮತ್ತು ಸಹಾನುಭೂತಿಗೆ ಜಾಗ ನೀಡುವುದು ಸಮತೋಲನ ತರುತ್ತದೆ."
        }
        Language.ML -> "${names} ഊർജം വളരെ കുറവോ ഇല്ലയോ ആണ് — " + when (first) {
            "Fire" -> "മുൻകൈയും ആത്മപ്രകാശനവും വളർത്തുന്നത് സന്തുലനം നൽകും."
            "Earth" -> "സ്ഥിരമായ ദിനചര്യയും പൂർത്തീകരണവും വളർത്തിയെടുക്കാൻ യോഗ്യം."
            "Air" -> "വസ്തുനിഷ്ഠതയ്ക്കും സംവാദത്തിനും അൽപം പിന്നോട്ട് നിൽക്കുന്നത് സഹായിക്കും."
            else -> "വികാരങ്ങൾക്കും സഹാനുഭൂതിക്കും ഇടം നൽകുന്നത് സന്തുലനം നൽകും."
        }
        Language.MR -> "${names} ऊर्जा फारच कमी किंवा नाही — " + when (first) {
            "Fire" -> "पुढाकार व आत्म-अभिव्यक्ती जोपासणे संतुलन आणू शकते."
            "Earth" -> "स्थिर दिनक्रम व पूर्णत्वाकडे नेणे जोपासण्याजोगे आहे."
            "Air" -> "वस्तुनिष्ठता व संवादासाठी थोडे मागे होणे मदत करते."
            else -> "भावना व सहानुभूतीला जागा देणे संतुलन आणू शकते."
        }
        else -> "There is little or no $names energy — " + when (first) {
            "Fire" -> "cultivating initiative and self-assertion can bring balance."
            "Earth" -> "grounding routines and follow-through are worth developing."
            "Air" -> "stepping back for objectivity and dialogue can help."
            else -> "making room for feelings and empathy can bring balance."
        }
    }

    private fun modalityText(m: String, lang: Language): String = when (lang) {
        Language.TA -> when (m) {
            "Cardinal" -> "சர ஆற்றல் மேலோங்குகிறது — நீங்கள் ஒரு தொடக்கக்காரர், புதிதாகத் தொடங்குவதிலும் திசை அமைப்பதிலும் மகிழ்ச்சி காண்கிறீர்கள்."
            "Fixed" -> "ஸ்திர ஆற்றல் மேலோங்குகிறது — நீங்கள் உறுதியானவர், தீர்மானமானவர், ஒருமுறை உறுதிபூண்டால் நிலைத்திருக்கும் சக்தி கொண்டவர்."
            else -> "உபய ஆற்றல் மேலோங்குகிறது — நீங்கள் தகவமைப்புத் திறன் கொண்டவர், நெகிழ்வானவர், மாற்றத்திலும் பன்முகத்தன்மையிலும் எளிதாக இருப்பவர்."
        }
        Language.ZH -> when (m) {
            "Cardinal" -> "基本模式主导——你是发起者，最乐于开创与定方向。"
            "Fixed" -> "固定模式主导——你稳定而坚定，一旦投入便有持久的耐力。"
            else -> "变动模式主导——你适应力强、灵活，乐于面对变化与多样。"
        }
        Language.HI -> when (m) {
            "Cardinal" -> "चर ऊर्जा प्रबल है — आप एक प्रवर्तक हैं, चीज़ें शुरू करने और दिशा तय करने में सबसे प्रसन्न।"
            "Fixed" -> "स्थिर ऊर्जा प्रबल है — आप दृढ़ और निश्चयी हैं, प्रतिबद्ध होने पर वास्तविक टिकाव-शक्ति के साथ।"
            else -> "द्विस्वभाव ऊर्जा प्रबल है — आप अनुकूलनशील और लचीले हैं, परिवर्तन और विविधता में सहज।"
        }
        Language.TE -> when (m) {
            "Cardinal" -> "చర శక్తి ప్రబలంగా ఉంది — మీరు ప్రారంభకర్త, పనులు మొదలుపెట్టడంలో, దిశ నిర్దేశించడంలో ఆనందిస్తారు."
            "Fixed" -> "స్థిర శక్తి ప్రబలంగా ఉంది — మీరు దృఢమైన, నిశ్చయాత్మక వ్యక్తి, కట్టుబడ్డాక నిలకడ శక్తి కలిగి."
            else -> "ద్విస్వభావ శక్తి ప్రబలంగా ఉంది — మీరు అనుకూలత, వశ్యత కలిగి, మార్పు, వైవిధ్యంలో సౌకర్యంగా ఉంటారు."
        }
        Language.KN -> when (m) {
            "Cardinal" -> "ಚರ ಶಕ್ತಿ ಪ್ರಬಲವಾಗಿದೆ — ನೀವು ಪ್ರಾರಂಭಕರ್ತ, ಕೆಲಸಗಳನ್ನು ಆರಂಭಿಸುವಲ್ಲಿ ಮತ್ತು ದಿಕ್ಕು ನಿರ್ಧರಿಸುವಲ್ಲಿ ಸಂತೋಷಿಸುತ್ತೀರಿ."
            "Fixed" -> "ಸ್ಥಿರ ಶಕ್ತಿ ಪ್ರಬಲವಾಗಿದೆ — ನೀವು ದೃಢ ಮತ್ತು ನಿಶ್ಚಯದವರು, ಬದ್ಧರಾದ ಮೇಲೆ ನಿಜವಾದ ಸಹಿಷ್ಣುತೆಯೊಂದಿಗೆ."
            else -> "ದ್ವಿಸ್ವಭಾವ ಶಕ್ತಿ ಪ್ರಬಲವಾಗಿದೆ — ನೀವು ಹೊಂದಿಕೊಳ್ಳುವ ಮತ್ತು ಸ್ಥಿತಿಸ್ಥಾಪಕ, ಬದಲಾವಣೆ ಮತ್ತು ವೈವಿಧ್ಯದಲ್ಲಿ ಸಹಜ."
        }
        Language.ML -> when (m) {
            "Cardinal" -> "ചര ഊർജം പ്രബലമാണ് — നിങ്ങൾ ഒരു ആരംഭകൻ, കാര്യങ്ങൾ തുടങ്ങുന്നതിലും ദിശ നിർണയിക്കുന്നതിലും ഏറ്റവും സന്തുഷ്ടർ."
            "Fixed" -> "സ്ഥിര ഊർജം പ്രബലമാണ് — നിങ്ങൾ ഉറച്ചവരും നിശ്ചയദാർഢ്യമുള്ളവരും, പ്രതിജ്ഞാബദ്ധരായാൽ യഥാർഥ സഹനശക്തിയോടെ."
            else -> "ദ്വിസ്വഭാവ ഊർജം പ്രബലമാണ് — നിങ്ങൾ അനുകൂലനശേഷിയും വഴക്കവുമുള്ളവർ, മാറ്റത്തിലും വൈവിധ്യത്തിലും സ്വാഭാവികർ."
        }
        Language.MR -> when (m) {
            "Cardinal" -> "चर ऊर्जा प्रबळ आहे — तुम्ही आरंभकर्ता आहात, गोष्टी सुरू करण्यात व दिशा ठरवण्यात सर्वाधिक आनंदी."
            "Fixed" -> "स्थिर ऊर्जा प्रबळ आहे — तुम्ही ठाम व निश्चयी आहात, वचनबद्ध झाल्यावर खऱ्या टिकावशक्तीसह."
            else -> "द्विस्वभाव ऊर्जा प्रबळ आहे — तुम्ही अनुकूल व लवचिक आहात, बदल व विविधतेत सहज."
        }
        else -> when (m) {
            "Cardinal" -> "Cardinal energy dominates — you are an initiator, happiest starting things and setting direction."
            "Fixed" -> "Fixed energy dominates — you are steady and determined, with real staying power once committed."
            else -> "Mutable energy dominates — you are adaptable and flexible, at ease with change and variety."
        }
    }
}
