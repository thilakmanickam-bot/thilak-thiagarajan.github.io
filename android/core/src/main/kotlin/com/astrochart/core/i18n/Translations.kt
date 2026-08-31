package com.astrochart.core.i18n

import com.astrochart.core.interpret.HouseInfo
import com.astrochart.core.interpret.PlanetInfo
import com.astrochart.core.interpret.SignInfo
import com.astrochart.core.models.ChartStyle

/**
 * Single source of localized astrological vocabulary and reading text for the
 * three supported [Language]s. English delegates to the canonical objects
 * ([SignInfo], [PlanetInfo], [HouseInfo]) so there is no duplicated English;
 * Tamil and Chinese come from the maps below. Charts are always stored with
 * canonical English data values (e.g. sign = "Aries"); translation happens at
 * render time, so switching language never requires recomputation.
 */
object Translations {

    // ----- Zodiac sign names -------------------------------------------------

    private val signNameTa = mapOf(
        "Aries" to "மேஷம்", "Taurus" to "ரிஷபம்", "Gemini" to "மிதுனம்",
        "Cancer" to "கடகம்", "Leo" to "சிம்மம்", "Virgo" to "கன்னி",
        "Libra" to "துலாம்", "Scorpio" to "விருச்சிகம்", "Sagittarius" to "தனுசு",
        "Capricorn" to "மகரம்", "Aquarius" to "கும்பம்", "Pisces" to "மீனம்"
    )
    private val signNameZh = mapOf(
        "Aries" to "白羊座", "Taurus" to "金牛座", "Gemini" to "双子座",
        "Cancer" to "巨蟹座", "Leo" to "狮子座", "Virgo" to "处女座",
        "Libra" to "天秤座", "Scorpio" to "天蝎座", "Sagittarius" to "射手座",
        "Capricorn" to "摩羯座", "Aquarius" to "水瓶座", "Pisces" to "双鱼座"
    )
    // Rasi names are a small, well-established vocabulary, so they are provided
    // in every language even where the rest of that language's pack still falls
    // back to English.
    private val signNameHi = mapOf(
        "Aries" to "मेष", "Taurus" to "वृषभ", "Gemini" to "मिथुन",
        "Cancer" to "कर्क", "Leo" to "सिंह", "Virgo" to "कन्या",
        "Libra" to "तुला", "Scorpio" to "वृश्चिक", "Sagittarius" to "धनु",
        "Capricorn" to "मकर", "Aquarius" to "कुम्भ", "Pisces" to "मीन"
    )
    private val signNameTe = mapOf(
        "Aries" to "మేషం", "Taurus" to "వృషభం", "Gemini" to "మిథునం",
        "Cancer" to "కర్కాటకం", "Leo" to "సింహం", "Virgo" to "కన్య",
        "Libra" to "తుల", "Scorpio" to "వృశ్చికం", "Sagittarius" to "ధనుస్సు",
        "Capricorn" to "మకరం", "Aquarius" to "కుంభం", "Pisces" to "మీనం"
    )
    private val signNameKn = mapOf(
        "Aries" to "ಮೇಷ", "Taurus" to "ವೃಷಭ", "Gemini" to "ಮಿಥುನ",
        "Cancer" to "ಕರ್ಕಾಟಕ", "Leo" to "ಸಿಂಹ", "Virgo" to "ಕನ್ಯಾ",
        "Libra" to "ತುಲಾ", "Scorpio" to "ವೃಶ್ಚಿಕ", "Sagittarius" to "ಧನಸ್ಸು",
        "Capricorn" to "ಮಕರ", "Aquarius" to "ಕುಂಭ", "Pisces" to "ಮೀನ"
    )
    private val signNameMl = mapOf(
        "Aries" to "മേടം", "Taurus" to "ഇടവം", "Gemini" to "മിഥുനം",
        "Cancer" to "കർക്കടകം", "Leo" to "ചിങ്ങം", "Virgo" to "കന്നി",
        "Libra" to "തുലാം", "Scorpio" to "വൃശ്ചികം", "Sagittarius" to "ധനു",
        "Capricorn" to "മകരം", "Aquarius" to "കുംഭം", "Pisces" to "മീനം"
    )
    private val signNameMr = mapOf(
        "Aries" to "मेष", "Taurus" to "वृषभ", "Gemini" to "मिथुन",
        "Cancer" to "कर्क", "Leo" to "सिंह", "Virgo" to "कन्या",
        "Libra" to "तूळ", "Scorpio" to "वृश्चिक", "Sagittarius" to "धनू",
        "Capricorn" to "मकर", "Aquarius" to "कुंभ", "Pisces" to "मीन"
    )

    fun signName(sign: String, lang: Language): String = when (lang) {
        Language.EN -> sign
        Language.TA -> signNameTa[sign] ?: sign
        Language.ZH -> signNameZh[sign] ?: sign
        Language.HI -> signNameHi[sign] ?: sign
        Language.TE -> signNameTe[sign] ?: sign
        Language.KN -> signNameKn[sign] ?: sign
        Language.ML -> signNameMl[sign] ?: sign
        Language.MR -> signNameMr[sign] ?: sign
    }

    // ----- Sign keyword phrases ---------------------------------------------

    private val signKeywordsTa = mapOf(
        "Aries" to "துணிச்சலான, நேரடியான, முன்னோடியான",
        "Taurus" to "நிலையான, சுகபோகமான, உறுதியான",
        "Gemini" to "ஆர்வமுள்ள, பல்துறை, பேச்சுத்திறமிக்க",
        "Cancer" to "பராமரிக்கும், பாதுகாக்கும், உள்ளுணர்வுள்ள",
        "Leo" to "அன்பான, வெளிப்படையான, பெருமிதமான",
        "Virgo" to "நுணுக்கமான, நடைமுறையான, பகுப்பாய்வான",
        "Libra" to "சமநிலையான, உறவுசார்ந்த, சமரசமான",
        "Scorpio" to "தீவிரமான, தனிப்பட்ட, மாற்றம் தரும்",
        "Sagittarius" to "சாகசமான, நேர்மையான, விரிவான",
        "Capricorn" to "ஒழுங்கான, லட்சியமான, பொறுமையான",
        "Aquarius" to "சுதந்திரமான, கண்டுபிடிப்பான, மனிதநேயமான",
        "Pisces" to "கற்பனையான, இரக்கமுள்ள, கனவுலகமான"
    )
    private val signKeywordsZh = mapOf(
        "Aries" to "大胆、直接、开拓",
        "Taurus" to "稳健、感性、务实",
        "Gemini" to "好奇、多变、健谈",
        "Cancer" to "体贴、守护、直觉",
        "Leo" to "热情、外向、自豪",
        "Virgo" to "精确、实际、善于分析",
        "Libra" to "平衡、重关系、圆融",
        "Scorpio" to "强烈、内敛、善于蜕变",
        "Sagittarius" to "爱冒险、坦率、豁达",
        "Capricorn" to "自律、有抱负、有耐心",
        "Aquarius" to "独立、富创意、有人文关怀",
        "Pisces" to "富想象力、有同情心、爱幻想"
    )

    fun signKeywords(sign: String, lang: Language): String = when (lang.content) {
        ContentLang.EN -> SignInfo.of(sign).keywords
        ContentLang.TA -> signKeywordsTa[sign] ?: SignInfo.of(sign).keywords
        ContentLang.ZH -> signKeywordsZh[sign] ?: SignInfo.of(sign).keywords
    }

    // ----- Elements & modalities --------------------------------------------

    private val elementTa = mapOf("Fire" to "நெருப்பு", "Earth" to "மண்", "Air" to "காற்று", "Water" to "நீர்")
    private val elementZh = mapOf("Fire" to "火", "Earth" to "土", "Air" to "风", "Water" to "水")
    private val elementHi = mapOf("Fire" to "अग्नि", "Earth" to "पृथ्वी", "Air" to "वायु", "Water" to "जल")
    private val elementTe = mapOf("Fire" to "అగ్ని", "Earth" to "పృథ్వి", "Air" to "వాయువు", "Water" to "జలం")
    private val elementKn = mapOf("Fire" to "ಅಗ್ನಿ", "Earth" to "ಪೃಥ್ವಿ", "Air" to "ವಾಯು", "Water" to "ಜಲ")
    private val elementMl = mapOf("Fire" to "അഗ്നി", "Earth" to "ഭൂമി", "Air" to "വായു", "Water" to "ജലം")
    private val elementMr = mapOf("Fire" to "अग्नी", "Earth" to "पृथ्वी", "Air" to "वायू", "Water" to "जल")
    private val modalityTa = mapOf("Cardinal" to "சரம்", "Fixed" to "ஸ்திரம்", "Mutable" to "உபயம்")
    private val modalityZh = mapOf("Cardinal" to "基本", "Fixed" to "固定", "Mutable" to "变动")
    private val modalityHi = mapOf("Cardinal" to "चर", "Fixed" to "स्थिर", "Mutable" to "द्विस्वभाव")
    private val modalityTe = mapOf("Cardinal" to "చర", "Fixed" to "స్థిర", "Mutable" to "ద్విస్వభావ")
    private val modalityKn = mapOf("Cardinal" to "ಚರ", "Fixed" to "ಸ್ಥಿರ", "Mutable" to "ದ್ವಿಸ್ವಭಾವ")
    private val modalityMl = mapOf("Cardinal" to "ചരം", "Fixed" to "സ്ഥിരം", "Mutable" to "ഇരുസ്വഭാവം")
    private val modalityMr = mapOf("Cardinal" to "चर", "Fixed" to "स्थिर", "Mutable" to "द्विस्वभाव")

    fun element(e: String, lang: Language): String = when (lang) {
        Language.TA -> elementTa[e] ?: e
        Language.ZH -> elementZh[e] ?: e
        Language.HI -> elementHi[e] ?: e
        Language.TE -> elementTe[e] ?: e
        Language.KN -> elementKn[e] ?: e
        Language.ML -> elementMl[e] ?: e
        Language.MR -> elementMr[e] ?: e
        else -> e
    }

    fun modality(m: String, lang: Language): String = when (lang) {
        Language.TA -> modalityTa[m] ?: m
        Language.ZH -> modalityZh[m] ?: m
        Language.HI -> modalityHi[m] ?: m
        Language.TE -> modalityTe[m] ?: m
        Language.KN -> modalityKn[m] ?: m
        Language.ML -> modalityMl[m] ?: m
        Language.MR -> modalityMr[m] ?: m
        else -> m
    }

    // ----- Planet names & roles ---------------------------------------------

    private val planetNameTa = mapOf(
        "Sun" to "சூரியன்", "Moon" to "சந்திரன்", "Mercury" to "புதன்", "Venus" to "சுக்கிரன்",
        "Mars" to "செவ்வாய்", "Jupiter" to "குரு", "Saturn" to "சனி", "Uranus" to "யுரேனஸ்",
        "Neptune" to "நெப்டியூன்", "Pluto" to "புளூட்டோ"
    )
    private val planetNameZh = mapOf(
        "Sun" to "太阳", "Moon" to "月亮", "Mercury" to "水星", "Venus" to "金星",
        "Mars" to "火星", "Jupiter" to "木星", "Saturn" to "土星", "Uranus" to "天王星",
        "Neptune" to "海王星", "Pluto" to "冥王星"
    )
    private val planetNameHi = mapOf(
        "Sun" to "सूर्य", "Moon" to "चंद्र", "Mercury" to "बुध", "Venus" to "शुक्र",
        "Mars" to "मंगल", "Jupiter" to "बृहस्पति", "Saturn" to "शनि", "Uranus" to "यूरेनस",
        "Neptune" to "नेप्च्यून", "Pluto" to "प्लूटो"
    )
    private val planetNameTe = mapOf(
        "Sun" to "సూర్యుడు", "Moon" to "చంద్రుడు", "Mercury" to "బుధుడు", "Venus" to "శుక్రుడు",
        "Mars" to "కుజుడు", "Jupiter" to "గురుడు", "Saturn" to "శని", "Uranus" to "యురేనస్",
        "Neptune" to "నెప్ట్యూన్", "Pluto" to "ప్లూటో"
    )
    private val planetNameKn = mapOf(
        "Sun" to "ಸೂರ್ಯ", "Moon" to "ಚಂದ್ರ", "Mercury" to "ಬುಧ", "Venus" to "ಶುಕ್ರ",
        "Mars" to "ಮಂಗಳ", "Jupiter" to "ಗುರು", "Saturn" to "ಶನಿ", "Uranus" to "ಯುರೇನಸ್",
        "Neptune" to "ನೆಪ್ಚೂನ್", "Pluto" to "ಪ್ಲೂಟೊ"
    )
    private val planetNameMl = mapOf(
        "Sun" to "സൂര്യൻ", "Moon" to "ചന്ദ്രൻ", "Mercury" to "ബുധൻ", "Venus" to "ശുക്രൻ",
        "Mars" to "ചൊവ്വ", "Jupiter" to "വ്യാഴം", "Saturn" to "ശനി", "Uranus" to "യുറാനസ്",
        "Neptune" to "നെപ്റ്റ്യൂൺ", "Pluto" to "പ്ലൂട്ടോ"
    )
    private val planetNameMr = mapOf(
        "Sun" to "सूर्य", "Moon" to "चंद्र", "Mercury" to "बुध", "Venus" to "शुक्र",
        "Mars" to "मंगळ", "Jupiter" to "गुरू", "Saturn" to "शनी", "Uranus" to "युरेनस",
        "Neptune" to "नेपच्यून", "Pluto" to "प्लुटो"
    )
    private val planetRoleTa = mapOf(
        "Sun" to "மைய அடையாளமும் உயிர்ச்சக்தியும்",
        "Moon" to "உணர்வியல்பும் உள்ளுணர்வும்",
        "Mercury" to "மனமும் தொடர்பாடலும்",
        "Venus" to "அன்பு, மதிப்புகள், அழகுணர்வு",
        "Mars" to "உந்துதல், தைரியம், ஆசை",
        "Jupiter" to "வளர்ச்சி, அதிர்ஷ்டம், நம்பிக்கை",
        "Saturn" to "ஒழுக்கம், அமைப்பு, பொறுப்பு",
        "Uranus" to "தனித்துவமும் மாற்றத் தேவையும்",
        "Neptune" to "கற்பனை, கனவுகள், ஆன்மிகம்",
        "Pluto" to "அதிகாரமும் மாற்றும் ஆற்றலும்"
    )
    private val planetRoleZh = mapOf(
        "Sun" to "核心自我与生命力",
        "Moon" to "情感本性与直觉",
        "Mercury" to "思维与沟通",
        "Venus" to "爱、价值观与审美",
        "Mars" to "动力、勇气与欲望",
        "Jupiter" to "成长、幸运与乐观",
        "Saturn" to "纪律、结构与责任",
        "Uranus" to "个性与求变",
        "Neptune" to "想象、梦想与灵性",
        "Pluto" to "力量与蜕变的能力"
    )

    fun planetName(p: String, lang: Language): String = when (lang) {
        Language.TA -> planetNameTa[p] ?: p
        Language.ZH -> planetNameZh[p] ?: p
        Language.HI -> planetNameHi[p] ?: p
        Language.TE -> planetNameTe[p] ?: p
        Language.KN -> planetNameKn[p] ?: p
        Language.ML -> planetNameMl[p] ?: p
        Language.MR -> planetNameMr[p] ?: p
        else -> p
    }

    fun planetRole(p: String, lang: Language): String = when (lang.content) {
        ContentLang.EN -> PlanetInfo.of(p)?.role ?: "energy"
        ContentLang.TA -> planetRoleTa[p] ?: (PlanetInfo.of(p)?.role ?: "")
        ContentLang.ZH -> planetRoleZh[p] ?: (PlanetInfo.of(p)?.role ?: "")
    }

    // ----- House areas -------------------------------------------------------

    private val houseAreaTa = mapOf(
        1 to "சுயம், அடையாளம், தோற்றம்",
        2 to "பணம், வளங்கள், மதிப்புகள்",
        3 to "தொடர்பாடல், கற்றல், உடன்பிறப்புகள்",
        4 to "வீடு, குடும்பம், வேர்கள்",
        5 to "படைப்பாற்றல், காதல், விளையாட்டு",
        6 to "வேலை, வழக்கங்கள், ஆரோக்கியம்",
        7 to "கூட்டாண்மையும் நெருங்கிய உறவுகளும்",
        8 to "நெருக்கம், பகிர்ந்த வளங்கள், மாற்றம்",
        9 to "நம்பிக்கைகள், உயர் கல்வி, பயணம்",
        10 to "தொழில், புகழ், பொது வாழ்க்கை",
        11 to "நட்பு, சமூகம், நம்பிக்கைகள்",
        12 to "உள் உலகம், ஓய்வு, ஆழ்மனம்"
    )
    private val houseAreaZh = mapOf(
        1 to "自我、身份与外在形象",
        2 to "金钱、资源与价值观",
        3 to "沟通、学习与手足",
        4 to "家庭、亲人与根基",
        5 to "创造力、爱情与玩乐",
        6 to "工作、日常与健康",
        7 to "伴侣与亲密关系",
        8 to "亲密、共享资源与蜕变",
        9 to "信念、高等学问与旅行",
        10 to "事业、声誉与公众生活",
        11 to "友谊、社群与愿望",
        12 to "内在世界、休息与潜意识"
    )

    fun houseArea(house: Int, lang: Language): String = when (lang.content) {
        ContentLang.EN -> HouseInfo.of(house)
        ContentLang.TA -> houseAreaTa[house] ?: HouseInfo.of(house)
        ContentLang.ZH -> houseAreaZh[house] ?: HouseInfo.of(house)
    }

    // ----- Aspect type names -------------------------------------------------

    private val aspectTypeTa = mapOf(
        "Conjunction" to "இணைப்பு", "Sextile" to "அறுகோணம்", "Square" to "சதுரம்",
        "Trine" to "திரிகோணம்", "Opposition" to "எதிர்நிலை"
    )
    private val aspectTypeZh = mapOf(
        "Conjunction" to "合相", "Sextile" to "六分相", "Square" to "刑相",
        "Trine" to "三分相", "Opposition" to "对分相"
    )
    private val aspectTypeHi = mapOf(
        "Conjunction" to "युति", "Sextile" to "षष्ठक", "Square" to "चतुष्क",
        "Trine" to "त्रिकोण", "Opposition" to "विरोध"
    )
    private val aspectTypeTe = mapOf(
        "Conjunction" to "సంయుక్తం", "Sextile" to "షష్ఠకం", "Square" to "చతురస్రం",
        "Trine" to "త్రికోణం", "Opposition" to "విరుద్ధం"
    )
    private val aspectTypeKn = mapOf(
        "Conjunction" to "ಸಂಯುಕ್ತ", "Sextile" to "ಷಷ್ಠಕ", "Square" to "ಚತುರಸ್ರ",
        "Trine" to "ತ್ರಿಕೋಣ", "Opposition" to "ವಿರುದ್ಧ"
    )
    private val aspectTypeMl = mapOf(
        "Conjunction" to "സംയോഗം", "Sextile" to "ഷഷ്ഠകം", "Square" to "ചതുരം",
        "Trine" to "ത്രികോണം", "Opposition" to "വിരുദ്ധം"
    )
    private val aspectTypeMr = mapOf(
        "Conjunction" to "युती", "Sextile" to "षष्ठक", "Square" to "चौकोन",
        "Trine" to "त्रिकोण", "Opposition" to "विरोध"
    )

    fun aspectType(type: String, lang: Language): String = when (lang) {
        Language.TA -> aspectTypeTa[type] ?: type
        Language.ZH -> aspectTypeZh[type] ?: type
        Language.HI -> aspectTypeHi[type] ?: type
        Language.TE -> aspectTypeTe[type] ?: type
        Language.KN -> aspectTypeKn[type] ?: type
        Language.ML -> aspectTypeMl[type] ?: type
        Language.MR -> aspectTypeMr[type] ?: type
        else -> type
    }

    // ----- Compact body abbreviations (for the South-Indian grid) ------------
    //
    // Short labels that fit the small cells of the square chart. Covers the ten
    // bodies the app computes plus the ascendant/lagnam. The Tamil forms follow
    // common almanac usage (சூரி, சந், புத, …).

    private val bodyAbbrEn = mapOf(
        "Sun" to "Su", "Moon" to "Mo", "Mercury" to "Me", "Venus" to "Ve",
        "Mars" to "Ma", "Jupiter" to "Ju", "Saturn" to "Sa", "Uranus" to "Ur",
        "Neptune" to "Ne", "Pluto" to "Pl", "Ascendant" to "Asc"
    )
    private val bodyAbbrTa = mapOf(
        "Sun" to "சூரி", "Moon" to "சந்", "Mercury" to "புத", "Venus" to "சுக்",
        "Mars" to "செவ்", "Jupiter" to "குரு", "Saturn" to "சனி", "Uranus" to "யுரே",
        "Neptune" to "நெப்", "Pluto" to "புளூ", "Ascendant" to "லக்"
    )
    private val bodyAbbrZh = mapOf(
        "Sun" to "日", "Moon" to "月", "Mercury" to "水", "Venus" to "金",
        "Mars" to "火", "Jupiter" to "木", "Saturn" to "土", "Uranus" to "天",
        "Neptune" to "海", "Pluto" to "冥", "Ascendant" to "命"
    )

    /** Compact label for a body (planet or "Ascendant"), used in the square chart. */
    fun bodyAbbr(body: String, lang: Language): String = when (lang.content) {
        ContentLang.EN -> bodyAbbrEn[body] ?: body
        ContentLang.TA -> bodyAbbrTa[body] ?: body
        ContentLang.ZH -> bodyAbbrZh[body] ?: body
    }

    // ----- Chart-style display names ----------------------------------------

    fun chartStyleName(style: ChartStyle, lang: Language): String = when (style) {
        ChartStyle.WESTERN_WHEEL -> when (lang.content) {
            ContentLang.EN -> "Western wheel"
            ContentLang.TA -> "மேற்கத்திய சக்கரம்"
            ContentLang.ZH -> "西方星盘"
        }
        ChartStyle.SOUTH_INDIAN -> when (lang.content) {
            ContentLang.EN -> "South Indian (Tamil)"
            ContentLang.TA -> "தென்னிந்தியம் (தமிழ்)"
            ContentLang.ZH -> "南印度（泰米尔）"
        }
    }
}
