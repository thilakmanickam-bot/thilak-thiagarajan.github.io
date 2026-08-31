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

    private val signKeywordsHi = mapOf(
        "Aries" to "साहसी, सीधा, अग्रणी", "Taurus" to "स्थिर, सुखप्रिय, व्यावहारिक",
        "Gemini" to "जिज्ञासु, बहुमुखी, बातूनी", "Cancer" to "पोषक, रक्षक, सहजज्ञानी",
        "Leo" to "गर्मजोश, अभिव्यक्तिशील, गर्वित", "Virgo" to "सटीक, व्यावहारिक, विश्लेषक",
        "Libra" to "संतुलित, संबंधप्रिय, कूटनीतिक", "Scorpio" to "गहन, निजी, रूपांतरकारी",
        "Sagittarius" to "साहसिक, ईमानदार, विस्तृत", "Capricorn" to "अनुशासित, महत्वाकांक्षी, धैर्यवान",
        "Aquarius" to "स्वतंत्र, आविष्कारशील, मानवतावादी", "Pisces" to "कल्पनाशील, करुणामय, स्वप्निल"
    )
    private val signKeywordsTe = mapOf(
        "Aries" to "సాహసం, నేరుగా, మార్గదర్శి", "Taurus" to "స్థిరం, సుఖప్రియం, ఆచరణాత్మకం",
        "Gemini" to "జిజ్ఞాస, బహుముఖం, మాటకారి", "Cancer" to "పోషణ, రక్షణ, అంతర్జ్ఞానం",
        "Leo" to "ఆప్యాయం, వ్యక్తీకరణ, గర్వం", "Virgo" to "ఖచ్చితం, ఆచరణాత్మకం, విశ్లేషణ",
        "Libra" to "సమతుల్యం, సంబంధప్రియం, దౌత్యం", "Scorpio" to "తీవ్రం, గోప్యం, పరివర్తన",
        "Sagittarius" to "సాహసికం, నిజాయితీ, విశాలం", "Capricorn" to "క్రమశిక్షణ, ఆశయం, ఓర్పు",
        "Aquarius" to "స్వతంత్రం, ఆవిష్కరణ, మానవత", "Pisces" to "ఊహాత్మకం, కరుణ, స్వప్నిల"
    )
    private val signKeywordsKn = mapOf(
        "Aries" to "ಧೈರ್ಯ, ನೇರ, ಮಾರ್ಗದರ್ಶಿ", "Taurus" to "ಸ್ಥಿರ, ಸುಖಪ್ರಿಯ, ಪ್ರಾಯೋಗಿಕ",
        "Gemini" to "ಕುತೂಹಲ, ಬಹುಮುಖ, ಮಾತುಗಾರ", "Cancer" to "ಪೋಷಣೆ, ರಕ್ಷಣೆ, ಅಂತರ್ಬೋಧೆ",
        "Leo" to "ಆತ್ಮೀಯ, ಅಭಿವ್ಯಕ್ತಿ, ಹೆಮ್ಮೆ", "Virgo" to "ನಿಖರ, ಪ್ರಾಯೋಗಿಕ, ವಿಶ್ಲೇಷಣೆ",
        "Libra" to "ಸಮತೋಲನ, ಸಂಬಂಧಪ್ರಿಯ, ರಾಜತಂತ್ರ", "Scorpio" to "ತೀವ್ರ, ಖಾಸಗಿ, ಪರಿವರ್ತನೆ",
        "Sagittarius" to "ಸಾಹಸ, ಪ್ರಾಮಾಣಿಕ, ವಿಶಾಲ", "Capricorn" to "ಶಿಸ್ತು, ಮಹತ್ವಾಕಾಂಕ್ಷೆ, ತಾಳ್ಮೆ",
        "Aquarius" to "ಸ್ವತಂತ್ರ, ಆವಿಷ್ಕಾರ, ಮಾನವೀಯ", "Pisces" to "ಕಲ್ಪನಾಶೀಲ, ಕರುಣಾಮಯ, ಸ್ವಪ್ನಶೀಲ"
    )
    private val signKeywordsMl = mapOf(
        "Aries" to "ധൈര്യം, നേരിട്ട്, മുൻനിര", "Taurus" to "സ്ഥിരം, സുഖപ്രിയം, പ്രായോഗികം",
        "Gemini" to "ജിജ്ഞാസ, ബഹുമുഖം, സംസാരപ്രിയം", "Cancer" to "പരിപാലനം, സംരക്ഷണം, അന്തർജ്ഞാനം",
        "Leo" to "ഊഷ്മളം, ആവിഷ്കാരം, അഭിമാനം", "Virgo" to "കൃത്യം, പ്രായോഗികം, വിശകലനം",
        "Libra" to "സന്തുലനം, ബന്ധപ്രിയം, നയതന്ത്രം", "Scorpio" to "തീവ്രം, സ്വകാര്യം, പരിവർത്തനം",
        "Sagittarius" to "സാഹസികം, സത്യസന്ധം, വിശാലം", "Capricorn" to "അച്ചടക്കം, മോഹം, ക്ഷമ",
        "Aquarius" to "സ്വതന്ത്രം, കണ്ടുപിടിത്തം, മാനവികം", "Pisces" to "ഭാവനാത്മകം, കാരുണ്യം, സ്വപ്നാത്മകം"
    )
    private val signKeywordsMr = mapOf(
        "Aries" to "धाडसी, थेट, अग्रेसर", "Taurus" to "स्थिर, सुखप्रिय, व्यावहारिक",
        "Gemini" to "जिज्ञासू, बहुमुखी, बोलका", "Cancer" to "पालनपोषण, रक्षक, अंतर्ज्ञानी",
        "Leo" to "उबदार, अभिव्यक्त, अभिमानी", "Virgo" to "अचूक, व्यावहारिक, विश्लेषक",
        "Libra" to "संतुलित, नातेप्रिय, कूटनीतिक", "Scorpio" to "तीव्र, खाजगी, परिवर्तनशील",
        "Sagittarius" to "साहसी, प्रामाणिक, विस्तृत", "Capricorn" to "शिस्तबद्ध, महत्त्वाकांक्षी, संयमी",
        "Aquarius" to "स्वतंत्र, नवोन्मेषी, मानवतावादी", "Pisces" to "कल्पक, करुणामय, स्वप्नाळू"
    )

    fun signKeywords(sign: String, lang: Language): String = when (lang) {
        Language.TA -> signKeywordsTa[sign] ?: SignInfo.of(sign).keywords
        Language.ZH -> signKeywordsZh[sign] ?: SignInfo.of(sign).keywords
        Language.HI -> signKeywordsHi[sign] ?: SignInfo.of(sign).keywords
        Language.TE -> signKeywordsTe[sign] ?: SignInfo.of(sign).keywords
        Language.KN -> signKeywordsKn[sign] ?: SignInfo.of(sign).keywords
        Language.ML -> signKeywordsMl[sign] ?: SignInfo.of(sign).keywords
        Language.MR -> signKeywordsMr[sign] ?: SignInfo.of(sign).keywords
        else -> SignInfo.of(sign).keywords
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

    private val planetRoleHi = mapOf(
        "Sun" to "मूल पहचान और जीवनशक्ति", "Moon" to "भावनात्मक स्वभाव और अंतर्ज्ञान",
        "Mercury" to "मन और संवाद", "Venus" to "प्रेम, मूल्य, सौंदर्यबोध",
        "Mars" to "प्रेरणा, साहस, इच्छा", "Jupiter" to "वृद्धि, भाग्य, आशावाद",
        "Saturn" to "अनुशासन, संरचना, जिम्मेदारी", "Uranus" to "विशिष्टता और परिवर्तन की चाह",
        "Neptune" to "कल्पना, स्वप्न, आध्यात्म", "Pluto" to "शक्ति और रूपांतरण की क्षमता"
    )
    private val planetRoleTe = mapOf(
        "Sun" to "మూల గుర్తింపు, జీవశక్తి", "Moon" to "భావోద్వేగ స్వభావం, అంతర్జ్ఞానం",
        "Mercury" to "మనసు, సంభాషణ", "Venus" to "ప్రేమ, విలువలు, సౌందర్యం",
        "Mars" to "ప్రేరణ, ధైర్యం, కోరిక", "Jupiter" to "వృద్ధి, అదృష్టం, ఆశావాదం",
        "Saturn" to "క్రమశిక్షణ, నిర్మాణం, బాధ్యత", "Uranus" to "విశిష్టత, మార్పు అవసరం",
        "Neptune" to "ఊహ, కలలు, ఆధ్యాత్మికత", "Pluto" to "శక్తి, పరివర్తన సామర్థ్యం"
    )
    private val planetRoleKn = mapOf(
        "Sun" to "ಮೂಲ ಗುರುತು, ಜೀವಶಕ್ತಿ", "Moon" to "ಭಾವನಾತ್ಮಕ ಸ್ವಭಾವ, ಅಂತರ್ಬೋಧೆ",
        "Mercury" to "ಮನಸ್ಸು, ಸಂವಹನ", "Venus" to "ಪ್ರೀತಿ, ಮೌಲ್ಯ, ಸೌಂದರ್ಯ",
        "Mars" to "ಪ್ರೇರಣೆ, ಧೈರ್ಯ, ಬಯಕೆ", "Jupiter" to "ಬೆಳವಣಿಗೆ, ಅದೃಷ್ಟ, ಆಶಾವಾದ",
        "Saturn" to "ಶಿಸ್ತು, ರಚನೆ, ಜವಾಬ್ದಾರಿ", "Uranus" to "ವಿಶಿಷ್ಟತೆ, ಬದಲಾವಣೆಯ ಅಗತ್ಯ",
        "Neptune" to "ಕಲ್ಪನೆ, ಕನಸು, ಆಧ್ಯಾತ್ಮ", "Pluto" to "ಶಕ್ತಿ, ಪರಿವರ್ತನೆ ಸಾಮರ್ಥ್ಯ"
    )
    private val planetRoleMl = mapOf(
        "Sun" to "അടിസ്ഥാന വ്യക്തിത്വം, ജീവശക്തി", "Moon" to "വൈകാരിക സ്വഭാവം, അന്തർജ്ഞാനം",
        "Mercury" to "മനസ്സ്, ആശയവിനിമയം", "Venus" to "സ്നേഹം, മൂല്യങ്ങൾ, സൗന്ദര്യം",
        "Mars" to "പ്രേരണ, ധൈര്യം, ആഗ്രഹം", "Jupiter" to "വളർച്ച, ഭാഗ്യം, ശുഭാപ്തി",
        "Saturn" to "അച്ചടക്കം, ഘടന, ഉത്തരവാദിത്വം", "Uranus" to "വ്യക്തിത്വം, മാറ്റത്തിന്റെ ആവശ്യം",
        "Neptune" to "ഭാവന, സ്വപ്നം, ആത്മീയത", "Pluto" to "ശക്തി, പരിവർത്തന ശേഷി"
    )
    private val planetRoleMr = mapOf(
        "Sun" to "मूळ ओळख, जीवनशक्ती", "Moon" to "भावनिक स्वभाव, अंतर्ज्ञान",
        "Mercury" to "मन, संवाद", "Venus" to "प्रेम, मूल्ये, सौंदर्यदृष्टी",
        "Mars" to "प्रेरणा, धैर्य, इच्छा", "Jupiter" to "वाढ, भाग्य, आशावाद",
        "Saturn" to "शिस्त, रचना, जबाबदारी", "Uranus" to "वेगळेपण, बदलाची गरज",
        "Neptune" to "कल्पना, स्वप्ने, अध्यात्म", "Pluto" to "शक्ती, परिवर्तनक्षमता"
    )

    fun planetRole(p: String, lang: Language): String = when (lang) {
        Language.TA -> planetRoleTa[p] ?: (PlanetInfo.of(p)?.role ?: "")
        Language.ZH -> planetRoleZh[p] ?: (PlanetInfo.of(p)?.role ?: "")
        Language.HI -> planetRoleHi[p] ?: (PlanetInfo.of(p)?.role ?: "")
        Language.TE -> planetRoleTe[p] ?: (PlanetInfo.of(p)?.role ?: "")
        Language.KN -> planetRoleKn[p] ?: (PlanetInfo.of(p)?.role ?: "")
        Language.ML -> planetRoleMl[p] ?: (PlanetInfo.of(p)?.role ?: "")
        Language.MR -> planetRoleMr[p] ?: (PlanetInfo.of(p)?.role ?: "")
        else -> PlanetInfo.of(p)?.role ?: "energy"
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

    private val houseAreaHi = mapOf(
        1 to "स्वयं, पहचान, रूप", 2 to "धन, संसाधन, मूल्य", 3 to "संवाद, सीखना, भाई-बहन",
        4 to "घर, परिवार, जड़ें", 5 to "रचनात्मकता, प्रेम, खेल", 6 to "काम, दिनचर्या, स्वास्थ्य",
        7 to "साझेदारी और घनिष्ठ संबंध", 8 to "घनिष्ठता, साझा संसाधन, रूपांतरण",
        9 to "विश्वास, उच्च शिक्षा, यात्रा", 10 to "करियर, प्रतिष्ठा, सार्वजनिक जीवन",
        11 to "मित्र, समुदाय, आशाएँ", 12 to "आंतरिक जगत, विश्राम, अवचेतन"
    )
    private val houseAreaTe = mapOf(
        1 to "స్వీయం, గుర్తింపు, రూపం", 2 to "ధనం, వనరులు, విలువలు", 3 to "సంభాషణ, నేర్చుకోవడం, తోబుట్టువులు",
        4 to "ఇల్లు, కుటుంబం, మూలాలు", 5 to "సృజనాత్మకత, ప్రేమ, ఆట", 6 to "పని, దినచర్య, ఆరోగ్యం",
        7 to "భాగస్వామ్యం, సన్నిహిత బంధాలు", 8 to "సాన్నిహిత్యం, భాగస్వామ్య వనరులు, పరివర్తన",
        9 to "నమ్మకాలు, ఉన్నత విద్య, ప్రయాణం", 10 to "వృత్తి, ప్రతిష్ఠ, ప్రజా జీవితం",
        11 to "స్నేహితులు, సమాజం, ఆశలు", 12 to "అంతరంగం, విశ్రాంతి, ఉపచేతన"
    )
    private val houseAreaKn = mapOf(
        1 to "ಸ್ವಯಂ, ಗುರುತು, ರೂಪ", 2 to "ಹಣ, ಸಂಪನ್ಮೂಲ, ಮೌಲ್ಯ", 3 to "ಸಂವಹನ, ಕಲಿಕೆ, ಒಡಹುಟ್ಟಿದವರು",
        4 to "ಮನೆ, ಕುಟುಂಬ, ಬೇರುಗಳು", 5 to "ಸೃಜನಶೀಲತೆ, ಪ್ರೀತಿ, ಆಟ", 6 to "ಕೆಲಸ, ದಿನಚರಿ, ಆರೋಗ್ಯ",
        7 to "ಪಾಲುದಾರಿಕೆ, ಆಪ್ತ ಸಂಬಂಧಗಳು", 8 to "ಆತ್ಮೀಯತೆ, ಹಂಚಿಕೆ ಸಂಪನ್ಮೂಲ, ಪರಿವರ್ತನೆ",
        9 to "ನಂಬಿಕೆ, ಉನ್ನತ ಶಿಕ್ಷಣ, ಪ್ರಯಾಣ", 10 to "ವೃತ್ತಿ, ಪ್ರತಿಷ್ಠೆ, ಸಾರ್ವಜನಿಕ ಜೀವನ",
        11 to "ಸ್ನೇಹಿತರು, ಸಮುದಾಯ, ಆಶಯಗಳು", 12 to "ಅಂತರಂಗ, ವಿಶ್ರಾಂತಿ, ಸುಪ್ತಪ್ರಜ್ಞೆ"
    )
    private val houseAreaMl = mapOf(
        1 to "സ്വയം, വ്യക്തിത്വം, രൂപം", 2 to "പണം, വിഭവങ്ങൾ, മൂല്യങ്ങൾ", 3 to "ആശയവിനിമയം, പഠനം, സഹോദരങ്ങൾ",
        4 to "വീട്, കുടുംബം, വേരുകൾ", 5 to "സർഗാത്മകത, സ്നേഹം, കളി", 6 to "ജോലി, ദിനചര്യ, ആരോഗ്യം",
        7 to "പങ്കാളിത്തം, അടുത്ത ബന്ധങ്ങൾ", 8 to "അടുപ്പം, പങ്കിട്ട വിഭവങ്ങൾ, പരിവർത്തനം",
        9 to "വിശ്വാസങ്ങൾ, ഉന്നത വിദ്യാഭ്യാസം, യാത്ര", 10 to "ജീവിതവൃത്തി, പ്രശസ്തി, പൊതുജീവിതം",
        11 to "സുഹൃത്തുക്കൾ, സമൂഹം, പ്രതീക്ഷകൾ", 12 to "ആന്തരികലോകം, വിശ്രമം, ഉപബോധമനസ്സ്"
    )
    private val houseAreaMr = mapOf(
        1 to "स्वतः, ओळख, रूप", 2 to "पैसा, संसाधने, मूल्ये", 3 to "संवाद, शिक्षण, भावंडे",
        4 to "घर, कुटुंब, मुळे", 5 to "सर्जनशीलता, प्रेम, खेळ", 6 to "काम, दिनचर्या, आरोग्य",
        7 to "भागीदारी आणि जवळचे नाते", 8 to "जवळीक, सामायिक संसाधने, परिवर्तन",
        9 to "श्रद्धा, उच्च शिक्षण, प्रवास", 10 to "करिअर, प्रतिष्ठा, सार्वजनिक जीवन",
        11 to "मित्र, समुदाय, आशा", 12 to "अंतर्विश्व, विश्रांती, अवचेतन"
    )

    fun houseArea(house: Int, lang: Language): String = when (lang) {
        Language.TA -> houseAreaTa[house] ?: HouseInfo.of(house)
        Language.ZH -> houseAreaZh[house] ?: HouseInfo.of(house)
        Language.HI -> houseAreaHi[house] ?: HouseInfo.of(house)
        Language.TE -> houseAreaTe[house] ?: HouseInfo.of(house)
        Language.KN -> houseAreaKn[house] ?: HouseInfo.of(house)
        Language.ML -> houseAreaMl[house] ?: HouseInfo.of(house)
        Language.MR -> houseAreaMr[house] ?: HouseInfo.of(house)
        else -> HouseInfo.of(house)
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
    private val bodyAbbrHi = mapOf(
        "Sun" to "सू", "Moon" to "चं", "Mercury" to "बु", "Venus" to "शु",
        "Mars" to "मं", "Jupiter" to "गु", "Saturn" to "श", "Uranus" to "यु",
        "Neptune" to "ने", "Pluto" to "प्लू", "Ascendant" to "लग्न"
    )
    private val bodyAbbrTe = mapOf(
        "Sun" to "సూ", "Moon" to "చం", "Mercury" to "బు", "Venus" to "శు",
        "Mars" to "కు", "Jupiter" to "గు", "Saturn" to "శ", "Uranus" to "యు",
        "Neptune" to "నె", "Pluto" to "ప్లూ", "Ascendant" to "లగ్న"
    )
    private val bodyAbbrKn = mapOf(
        "Sun" to "ಸೂ", "Moon" to "ಚಂ", "Mercury" to "ಬು", "Venus" to "ಶು",
        "Mars" to "ಮಂ", "Jupiter" to "ಗು", "Saturn" to "ಶ", "Uranus" to "ಯು",
        "Neptune" to "ನೆ", "Pluto" to "ಪ್ಲೂ", "Ascendant" to "ಲಗ್ನ"
    )
    private val bodyAbbrMl = mapOf(
        "Sun" to "സൂ", "Moon" to "ച", "Mercury" to "ബു", "Venus" to "ശു",
        "Mars" to "ചൊ", "Jupiter" to "വ്യാ", "Saturn" to "ശ", "Uranus" to "യു",
        "Neptune" to "നെ", "Pluto" to "പ്ലൂ", "Ascendant" to "ലഗ്നം"
    )
    private val bodyAbbrMr = mapOf(
        "Sun" to "सू", "Moon" to "चं", "Mercury" to "बु", "Venus" to "शु",
        "Mars" to "मं", "Jupiter" to "गु", "Saturn" to "श", "Uranus" to "यु",
        "Neptune" to "ने", "Pluto" to "प्लू", "Ascendant" to "लग्न"
    )

    /** Compact label for a body (planet or "Ascendant"), used in the square chart. */
    fun bodyAbbr(body: String, lang: Language): String = when (lang) {
        Language.TA -> bodyAbbrTa[body] ?: body
        Language.ZH -> bodyAbbrZh[body] ?: body
        Language.HI -> bodyAbbrHi[body] ?: body
        Language.TE -> bodyAbbrTe[body] ?: body
        Language.KN -> bodyAbbrKn[body] ?: body
        Language.ML -> bodyAbbrMl[body] ?: body
        Language.MR -> bodyAbbrMr[body] ?: body
        else -> bodyAbbrEn[body] ?: body
    }

    // ----- Chart-style display names ----------------------------------------

    fun chartStyleName(style: ChartStyle, lang: Language): String = when (style) {
        ChartStyle.WESTERN_WHEEL -> when (lang) {
            Language.TA -> "மேற்கத்திய சக்கரம்"
            Language.ZH -> "西方星盘"
            Language.HI -> "पश्चिमी चक्र"
            Language.TE -> "పాశ్చాత్య చక్రం"
            Language.KN -> "ಪಾಶ್ಚಾತ್ಯ ಚಕ್ರ"
            Language.ML -> "പാശ്ചാത്യ ചക്രം"
            Language.MR -> "पाश्चात्य चक्र"
            else -> "Western wheel"
        }
        ChartStyle.SOUTH_INDIAN -> when (lang) {
            Language.TA -> "தென்னிந்தியம் (தமிழ்)"
            Language.ZH -> "南印度（泰米尔）"
            Language.HI -> "दक्षिण भारतीय (तमिल)"
            Language.TE -> "దక్షిణ భారత (తమిళ)"
            Language.KN -> "ದಕ್ಷಿಣ ಭಾರತೀಯ (ತಮಿಳು)"
            Language.ML -> "ദക്ഷിണേന്ത്യൻ (തമിഴ്)"
            Language.MR -> "दक्षिण भारतीय (तमिळ)"
            else -> "South Indian (Tamil)"
        }
    }
}
