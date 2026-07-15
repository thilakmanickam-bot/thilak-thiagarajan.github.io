package com.astrochart.core.i18n

import com.astrochart.core.interpret.HouseInfo
import com.astrochart.core.interpret.PlanetInfo
import com.astrochart.core.interpret.SignInfo

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

    fun signName(sign: String, lang: Language): String = when (lang) {
        Language.EN -> sign
        Language.TA -> signNameTa[sign] ?: sign
        Language.ZH -> signNameZh[sign] ?: sign
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
        "Cancer" to "体贴、护己、直觉",
        "Leo" to "热情、外向、自豪",
        "Virgo" to "精确、实际、善于分析",
        "Libra" to "平衡、重关系、圆融",
        "Scorpio" to "强烈、内敛、善于蜕变",
        "Sagittarius" to "爱冒险、坦率、豁达",
        "Capricorn" to "自律、有抱负、有耐心",
        "Aquarius" to "独立、富创意、有人文关怀",
        "Pisces" to "富想象力、有同情心、爱幻想"
    )

    fun signKeywords(sign: String, lang: Language): String = when (lang) {
        Language.EN -> SignInfo.of(sign).keywords
        Language.TA -> signKeywordsTa[sign] ?: SignInfo.of(sign).keywords
        Language.ZH -> signKeywordsZh[sign] ?: SignInfo.of(sign).keywords
    }

    // ----- Elements & modalities --------------------------------------------

    private val elementTa = mapOf("Fire" to "நெருப்பு", "Earth" to "மண்", "Air" to "காற்று", "Water" to "நீர்")
    private val elementZh = mapOf("Fire" to "火", "Earth" to "土", "Air" to "风", "Water" to "水")
    private val modalityTa = mapOf("Cardinal" to "சரம்", "Fixed" to "ஸ்திரம்", "Mutable" to "உபயம்")
    private val modalityZh = mapOf("Cardinal" to "基本", "Fixed" to "固定", "Mutable" to "变动")

    fun element(e: String, lang: Language): String = when (lang) {
        Language.EN -> e
        Language.TA -> elementTa[e] ?: e
        Language.ZH -> elementZh[e] ?: e
    }

    fun modality(m: String, lang: Language): String = when (lang) {
        Language.EN -> m
        Language.TA -> modalityTa[m] ?: m
        Language.ZH -> modalityZh[m] ?: m
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
        Language.EN -> p
        Language.TA -> planetNameTa[p] ?: p
        Language.ZH -> planetNameZh[p] ?: p
    }

    fun planetRole(p: String, lang: Language): String = when (lang) {
        Language.EN -> PlanetInfo.of(p)?.role ?: "energy"
        Language.TA -> planetRoleTa[p] ?: (PlanetInfo.of(p)?.role ?: "")
        Language.ZH -> planetRoleZh[p] ?: (PlanetInfo.of(p)?.role ?: "")
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

    fun houseArea(house: Int, lang: Language): String = when (lang) {
        Language.EN -> HouseInfo.of(house)
        Language.TA -> houseAreaTa[house] ?: HouseInfo.of(house)
        Language.ZH -> houseAreaZh[house] ?: HouseInfo.of(house)
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

    fun aspectType(type: String, lang: Language): String = when (lang) {
        Language.EN -> type
        Language.TA -> aspectTypeTa[type] ?: type
        Language.ZH -> aspectTypeZh[type] ?: type
    }
}
