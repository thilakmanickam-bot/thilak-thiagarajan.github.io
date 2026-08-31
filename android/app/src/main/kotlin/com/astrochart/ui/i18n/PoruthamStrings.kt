package com.astrochart.ui.i18n

import com.astrochart.core.i18n.Language
import com.astrochart.core.interpret.Koota

/**
 * Localized text for the South-Indian marriage-matching (Porutham) screen.
 * Koota names and their present/absent verdicts are keyed by [Koota.key] so the
 * core stays language-neutral. Currently EN / TA / ZH; other languages fall back
 * to English until reviewed translations are added.
 */
data class PoruthamStrings(
    val title: String,
    val entry: String,
    val subtitle: String,
    val boyDetails: String,
    val girlDetails: String,
    val name: String,
    val enterBoyName: String,
    val enterGirlName: String,
    val rasi: String,
    val nakshatram: String,
    val calculate: String,
    val boyName: String,
    val girlName: String,
    val totalScore: String,
    val kuta: String,
    val gained: String,
    val max: String,
    val present: String,
    val absent: String,
    val expertConnect: String,
    val fillAll: String,
    val summaryGood: String,
    val summaryFair: String,
    val summaryPoor: String,
    val summaryCritical: String,
    private val kootaNames: Map<String, String>,
    private val presentDesc: Map<String, String>,
    private val absentDesc: Map<String, String>
) {
    fun kootaName(koota: Koota): String = kootaNames[koota.key] ?: koota.key
    fun description(koota: Koota, present: Boolean): String =
        (if (present) presentDesc else absentDesc)[koota.key] ?: ""

    /** One-line verdict for the whole match. */
    fun summary(total: Int, hasCriticalDosha: Boolean): String = when {
        hasCriticalDosha -> summaryCritical
        total >= 28 -> summaryGood
        total >= 18 -> summaryFair
        else -> summaryPoor
    }

    companion object {
        fun forLanguage(lang: Language): PoruthamStrings = when (lang) {
            Language.TA -> TA
            Language.ZH -> ZH
            Language.HI -> HI
            else -> EN
        }

        private val EN = PoruthamStrings(
            title = "Marriage Match Making",
            entry = "Marriage Match Making",
            subtitle = "Check astrological compatibility for a perfect match",
            boyDetails = "Boy Details",
            girlDetails = "Girl Details",
            name = "Name",
            enterBoyName = "Enter boy's name",
            enterGirlName = "Enter girl's name",
            rasi = "Rasi",
            nakshatram = "Nakshatram",
            calculate = "Calculate Match",
            boyName = "Boy Name",
            girlName = "Girl Name",
            totalScore = "Total Compatibility Score",
            kuta = "Kuta",
            gained = "Gained",
            max = "Max",
            present = "Present",
            absent = "Absent",
            expertConnect = "Expert Connect",
            fillAll = "Choose the rasi and nakshatram for both people.",
            summaryGood = "Key astrological factors are well matched. A harmonious and prosperous union is indicated.",
            summaryFair = "Key astrological factors analyzed for compatibility and longevity. A reasonable match with a few areas to consider.",
            summaryPoor = "Several factors are missing. Consider consulting an astrologer before proceeding.",
            summaryCritical = "Key astrological factors analyzed for compatibility and longevity. Several critical factors are missing. Health and longevity concerns noted.",
            kootaNames = mapOf(
                "dina" to "Dina", "gana" to "Gana", "mahendra" to "Mahendra",
                "streeDeergha" to "StreeDeergha", "yoni" to "Yoni", "rasi" to "Rasi",
                "rasiAdhipathi" to "RasiAdhipathi", "vasya" to "Vasya", "rajju" to "Rajju",
                "vedha" to "Vedha", "varna" to "Varna", "nadi" to "Nadi"
            ),
            presentDesc = mapOf(
                "dina" to "Dina is present, supporting health and prosperity for the couple.",
                "gana" to "Gana is present, indicating good temperament and character compatibility.",
                "mahendra" to "Mahendra is present, favouring progeny and well-being.",
                "streeDeergha" to "StreeDeergha is present, supporting the longevity of the girl.",
                "yoni" to "Yoni is present, indicating good physical compatibility.",
                "rasi" to "Rasi is present, signifying love and affection between the couple.",
                "rasiAdhipathi" to "RasiAdhipathi is present, indicating psychological compatibility.",
                "vasya" to "Vasya is present, showing mutual attraction between partners.",
                "rajju" to "Rajju is present, supporting the longevity of the marriage.",
                "vedha" to "Vedha is present, indicating fewer hardships and impediments in marital life.",
                "varna" to "Varna is present, suggesting harmony in temperament and reduced ego clashes.",
                "nadi" to "Nadi is present, supporting good health and healthy progeny."
            ),
            absentDesc = mapOf(
                "dina" to "Dina is absent, which may risk adverse effects on health and prosperity for the couple.",
                "gana" to "Gana is absent, which may lead to differences in temperament.",
                "mahendra" to "Mahendra is absent, which can cause delays in childbirth.",
                "streeDeergha" to "StreeDeergha is absent, potentially affecting the longevity of the girl.",
                "yoni" to "Yoni is absent, which may affect physical compatibility.",
                "rasi" to "Rasi is absent, which may affect love and affection between the couple.",
                "rasiAdhipathi" to "RasiAdhipathi is absent, which may affect psychological compatibility.",
                "vasya" to "Vasya is absent, which may reduce mutual attraction between partners.",
                "rajju" to "Rajju is absent, which is critical and may threaten the longevity of the marriage. Marriage is strongly not recommended.",
                "vedha" to "Vedha is absent, which may bring hardships and impediments in marital life.",
                "varna" to "Varna is absent, which may lead to differences in temperament and ego clashes.",
                "nadi" to "Nadi is absent, which may cause health issues and hereditary problems. Marriage is not recommended."
            )
        )

        private val HI = PoruthamStrings(
            title = "विवाह मिलान",
            entry = "विवाह मिलान",
            subtitle = "उत्तम जोड़ी के लिए ज्योतिषीय अनुकूलता जाँचें",
            boyDetails = "वर का विवरण",
            girlDetails = "वधू का विवरण",
            name = "नाम",
            enterBoyName = "वर का नाम भरें",
            enterGirlName = "वधू का नाम भरें",
            rasi = "राशि",
            nakshatram = "नक्षत्र",
            calculate = "मिलान करें",
            boyName = "वर का नाम",
            girlName = "वधू का नाम",
            totalScore = "कुल अनुकूलता अंक",
            kuta = "कूट",
            gained = "प्राप्त",
            max = "अधिकतम",
            present = "उपस्थित",
            absent = "अनुपस्थित",
            expertConnect = "विशेषज्ञ से जुड़ें",
            fillAll = "दोनों के लिए राशि और नक्षत्र चुनें।",
            summaryGood = "मुख्य ज्योतिषीय कारक भली-भाँति मिलते हैं। सुखद और समृद्ध दांपत्य का संकेत है।",
            summaryFair = "अनुकूलता और दीर्घायु का विश्लेषण किया गया। कुछ बिंदुओं पर विचार के साथ यह एक ठीक-ठाक मिलान है।",
            summaryPoor = "कई कारक अनुपस्थित हैं। आगे बढ़ने से पहले किसी ज्योतिषी से परामर्श करें।",
            summaryCritical = "अनुकूलता और दीर्घायु का विश्लेषण किया गया। कई महत्वपूर्ण कारक अनुपस्थित हैं। स्वास्थ्य और दीर्घायु संबंधी चिंताएँ हैं।",
            kootaNames = mapOf(
                "dina" to "दिन", "gana" to "गण", "mahendra" to "महेंद्र",
                "streeDeergha" to "स्त्री दीर्घ", "yoni" to "योनि", "rasi" to "राशि",
                "rasiAdhipathi" to "राशि अधिपति", "vasya" to "वश्य", "rajju" to "रज्जु",
                "vedha" to "वेध", "varna" to "वर्ण", "nadi" to "नाड़ी"
            ),
            presentDesc = mapOf(
                "dina" to "दिन कूट उपस्थित है; दंपति के स्वास्थ्य और समृद्धि के लिए शुभ।",
                "gana" to "गण कूट उपस्थित है; अच्छा स्वभाव और चारित्रिक अनुकूलता।",
                "mahendra" to "महेंद्र कूट उपस्थित है; संतान और कल्याण के लिए अनुकूल।",
                "streeDeergha" to "स्त्री दीर्घ उपस्थित है; वधू की दीर्घायु के लिए सहायक।",
                "yoni" to "योनि कूट उपस्थित है; अच्छी शारीरिक अनुकूलता का संकेत।",
                "rasi" to "राशि कूट उपस्थित है; दंपति में प्रेम और स्नेह का सूचक।",
                "rasiAdhipathi" to "राशि अधिपति उपस्थित है; मानसिक अनुकूलता का संकेत।",
                "vasya" to "वश्य कूट उपस्थित है; परस्पर आकर्षण का सूचक।",
                "rajju" to "रज्जु कूट उपस्थित है; विवाह की दीर्घता के लिए सहायक।",
                "vedha" to "वेध कूट उपस्थित है; दांपत्य जीवन में कम बाधाओं का संकेत।",
                "varna" to "वर्ण कूट उपस्थित है; स्वभाव में सामंजस्य और कम अहं-टकराव।",
                "nadi" to "नाड़ी कूट उपस्थित है; अच्छे स्वास्थ्य और स्वस्थ संतान के लिए शुभ।"
            ),
            absentDesc = mapOf(
                "dina" to "दिन कूट अनुपस्थित है; स्वास्थ्य और समृद्धि पर प्रतिकूल प्रभाव संभव।",
                "gana" to "गण कूट अनुपस्थित है; स्वभाव में मतभेद हो सकते हैं।",
                "mahendra" to "महेंद्र कूट अनुपस्थित है; संतान में विलंब हो सकता है।",
                "streeDeergha" to "स्त्री दीर्घ अनुपस्थित है; वधू की आयु पर प्रभाव संभव।",
                "yoni" to "योनि कूट अनुपस्थित है; शारीरिक अनुकूलता पर प्रभाव पड़ सकता है।",
                "rasi" to "राशि कूट अनुपस्थित है; प्रेम और स्नेह पर प्रभाव पड़ सकता है।",
                "rasiAdhipathi" to "राशि अधिपति अनुपस्थित है; मानसिक अनुकूलता पर प्रभाव संभव।",
                "vasya" to "वश्य कूट अनुपस्थित है; परस्पर आकर्षण घट सकता है।",
                "rajju" to "रज्जु कूट अनुपस्थित है; यह अत्यंत महत्वपूर्ण है और विवाह की दीर्घता के लिए संकटपूर्ण हो सकता है। विवाह की अनुशंसा नहीं की जाती।",
                "vedha" to "वेध कूट अनुपस्थित है; दांपत्य जीवन में बाधाएँ आ सकती हैं।",
                "varna" to "वर्ण कूट अनुपस्थित है; स्वभाव में मतभेद और अहं-टकराव हो सकते हैं।",
                "nadi" to "नाड़ी कूट अनुपस्थित है; स्वास्थ्य और आनुवंशिक समस्याएँ हो सकती हैं। विवाह की अनुशंसा नहीं की जाती।"
            )
        )

        private val TA = PoruthamStrings(
            title = "திருமணப் பொருத்தம்",
            entry = "திருமணப் பொருத்தம்",
            subtitle = "சிறந்த பொருத்தத்திற்கு ஜோதிட பொருத்தத்தைச் சரிபார்க்கவும்",
            boyDetails = "மணமகன் விவரங்கள்",
            girlDetails = "மணமகள் விவரங்கள்",
            name = "பெயர்",
            enterBoyName = "மணமகன் பெயரை உள்ளிடவும்",
            enterGirlName = "மணமகள் பெயரை உள்ளிடவும்",
            rasi = "ராசி",
            nakshatram = "நட்சத்திரம்",
            calculate = "பொருத்தம் பார்க்க",
            boyName = "மணமகன் பெயர்",
            girlName = "மணமகள் பெயர்",
            totalScore = "மொத்த பொருத்த மதிப்பெண்",
            kuta = "பொருத்தம்",
            gained = "பெற்றது",
            max = "அதிகபட்சம்",
            present = "உண்டு",
            absent = "இல்லை",
            expertConnect = "நிபுணரை அணுகவும்",
            fillAll = "இருவருக்கும் ராசி மற்றும் நட்சத்திரத்தைத் தேர்ந்தெடுக்கவும்.",
            summaryGood = "முக்கிய ஜோதிட அம்சங்கள் நன்கு பொருந்துகின்றன. இணக்கமான, செழிப்பான வாழ்க்கை அமையும்.",
            summaryFair = "பொருத்தமும் நீடித்த வாழ்வும் பகுப்பாய்வு செய்யப்பட்டது. சில அம்சங்களைக் கவனிக்க வேண்டிய ஓரளவு பொருத்தம்.",
            summaryPoor = "பல அம்சங்கள் இல்லை. முன்செல்வதற்கு முன் ஜோதிடரை அணுகவும்.",
            summaryCritical = "பொருத்தமும் நீடித்த வாழ்வும் பகுப்பாய்வு செய்யப்பட்டது. முக்கியமான பல அம்சங்கள் இல்லை. ஆரோக்கியம் மற்றும் ஆயுள் குறித்த கவலைகள் உள்ளன.",
            kootaNames = mapOf(
                "dina" to "தினம்", "gana" to "கணம்", "mahendra" to "மகேந்திரம்",
                "streeDeergha" to "ஸ்திரீ தீர்க்கம்", "yoni" to "யோனி", "rasi" to "ராசி",
                "rasiAdhipathi" to "ராசி அதிபதி", "vasya" to "வசியம்", "rajju" to "ரஜ்ஜு",
                "vedha" to "வேதை", "varna" to "வர்ணம்", "nadi" to "நாடி"
            ),
            presentDesc = mapOf(
                "dina" to "தினப் பொருத்தம் உண்டு; தம்பதியரின் ஆரோக்கியத்திற்கும் செழிப்பிற்கும் ஆதரவு.",
                "gana" to "கணப் பொருத்தம் உண்டு; நல்ல குணமும் மனப்பொருத்தமும் அமையும்.",
                "mahendra" to "மகேந்திரப் பொருத்தம் உண்டு; சந்ததிக்கும் நலனுக்கும் சாதகம்.",
                "streeDeergha" to "ஸ்திரீ தீர்க்கம் உண்டு; மணமகளின் நீடித்த வாழ்வுக்கு ஆதரவு.",
                "yoni" to "யோனிப் பொருத்தம் உண்டு; உடல் ரீதியான பொருத்தம் நன்று.",
                "rasi" to "ராசிப் பொருத்தம் உண்டு; தம்பதியரிடையே அன்பும் பாசமும் மிகும்.",
                "rasiAdhipathi" to "ராசி அதிபதிப் பொருத்தம் உண்டு; மனநிலைப் பொருத்தம் நன்று.",
                "vasya" to "வசியப் பொருத்தம் உண்டு; பரஸ்பர ஈர்ப்பு நன்று.",
                "rajju" to "ரஜ்ஜுப் பொருத்தம் உண்டு; திருமண வாழ்வின் நீடிப்பிற்கு ஆதரவு.",
                "vedha" to "வேதைப் பொருத்தம் உண்டு; வாழ்வில் இடையூறுகள் குறையும்.",
                "varna" to "வர்ணப் பொருத்தம் உண்டு; மனப்பொருத்தமும் அமைதியும் அமையும்.",
                "nadi" to "நாடிப் பொருத்தம் உண்டு; நல்ல ஆரோக்கியமும் ஆரோக்கியமான சந்ததியும்."
            ),
            absentDesc = mapOf(
                "dina" to "தினப் பொருத்தம் இல்லை; ஆரோக்கியம் மற்றும் செழிப்பில் பாதிப்பு ஏற்படலாம்.",
                "gana" to "கணப் பொருத்தம் இல்லை; குண வேறுபாடுகள் ஏற்படலாம்.",
                "mahendra" to "மகேந்திரப் பொருத்தம் இல்லை; குழந்தைப்பேற்றில் தாமதம் ஏற்படலாம்.",
                "streeDeergha" to "ஸ்திரீ தீர்க்கம் இல்லை; மணமகளின் ஆயுளைப் பாதிக்கலாம்.",
                "yoni" to "யோனிப் பொருத்தம் இல்லை; உடல் ரீதியான பொருத்தத்தைப் பாதிக்கலாம்.",
                "rasi" to "ராசிப் பொருத்தம் இல்லை; அன்பையும் பாசத்தையும் பாதிக்கலாம்.",
                "rasiAdhipathi" to "ராசி அதிபதிப் பொருத்தம் இல்லை; மனநிலைப் பொருத்தத்தைப் பாதிக்கலாம்.",
                "vasya" to "வசியப் பொருத்தம் இல்லை; பரஸ்பர ஈர்ப்பு குறையலாம்.",
                "rajju" to "ரஜ்ஜுப் பொருத்தம் இல்லை; இது மிக முக்கியமானது, திருமண வாழ்வின் நீடிப்பைப் பாதிக்கலாம். திருமணம் பரிந்துரைக்கப்படவில்லை.",
                "vedha" to "வேதைப் பொருத்தம் இல்லை; வாழ்வில் இடையூறுகள் ஏற்படலாம்.",
                "varna" to "வர்ணப் பொருத்தம் இல்லை; குண வேறுபாடுகள் ஏற்படலாம்.",
                "nadi" to "நாடிப் பொருத்தம் இல்லை; ஆரோக்கியம் மற்றும் மரபுவழிப் பிரச்சினைகள் ஏற்படலாம். திருமணம் பரிந்துரைக்கப்படவில்லை."
            )
        )

        private val ZH = PoruthamStrings(
            title = "婚配合婚",
            entry = "婚配合婚",
            subtitle = "查看占星契合度，寻找理想良缘",
            boyDetails = "男方信息",
            girlDetails = "女方信息",
            name = "姓名",
            enterBoyName = "输入男方姓名",
            enterGirlName = "输入女方姓名",
            rasi = "月亮星座",
            nakshatram = "出生星宿",
            calculate = "计算契合度",
            boyName = "男方姓名",
            girlName = "女方姓名",
            totalScore = "综合契合分数",
            kuta = "相配项",
            gained = "得分",
            max = "满分",
            present = "具备",
            absent = "缺失",
            expertConnect = "咨询专家",
            fillAll = "请为双方选择月亮星座与出生星宿。",
            summaryGood = "主要占星要素契合良好，预示和谐美满、富足的结合。",
            summaryFair = "已分析契合度与长久性。整体尚可，但有几项需要留意。",
            summaryPoor = "多项要素缺失。建议在推进前咨询占星师。",
            summaryCritical = "已分析契合度与长久性。多项关键要素缺失，健康与长久性方面存在隐忧。",
            kootaNames = mapOf(
                "dina" to "日相配", "gana" to "族群相配", "mahendra" to "子嗣相配",
                "streeDeergha" to "女方长寿相配", "yoni" to "生殖相配", "rasi" to "星座相配",
                "rasiAdhipathi" to "星主相配", "vasya" to "吸引相配", "rajju" to "命绳相配",
                "vedha" to "阻碍相配", "varna" to "种姓相配", "nadi" to "脉相配"
            ),
            presentDesc = mapOf(
                "dina" to "日相配具备，有利于双方的健康与富足。",
                "gana" to "族群相配具备，预示良好的性情与品格契合。",
                "mahendra" to "子嗣相配具备，有利于子嗣与安康。",
                "streeDeergha" to "女方长寿相配具备，有助于女方的长寿。",
                "yoni" to "生殖相配具备，预示良好的身体契合。",
                "rasi" to "星座相配具备，象征夫妻间的爱意与情感。",
                "rasiAdhipathi" to "星主相配具备，预示心理层面的契合。",
                "vasya" to "吸引相配具备，显示彼此的相互吸引。",
                "rajju" to "命绳相配具备，有助于婚姻的长久。",
                "vedha" to "阻碍相配具备，预示婚姻生活中较少艰难与阻碍。",
                "varna" to "种姓相配具备，预示性情和谐、减少自我冲突。",
                "nadi" to "脉相配具备，有利于健康与健康的子嗣。"
            ),
            absentDesc = mapOf(
                "dina" to "日相配缺失，可能对双方的健康与富足产生不利影响。",
                "gana" to "族群相配缺失，可能导致性情上的差异。",
                "mahendra" to "子嗣相配缺失，可能导致生育延迟。",
                "streeDeergha" to "女方长寿相配缺失，可能影响女方的寿命。",
                "yoni" to "生殖相配缺失，可能影响身体契合。",
                "rasi" to "星座相配缺失，可能影响夫妻间的爱意与情感。",
                "rasiAdhipathi" to "星主相配缺失，可能影响心理契合。",
                "vasya" to "吸引相配缺失，可能减弱彼此的吸引。",
                "rajju" to "命绳相配缺失，此项至关重要，可能危及婚姻的长久，强烈不建议成婚。",
                "vedha" to "阻碍相配缺失，可能带来婚姻生活中的艰难与阻碍。",
                "varna" to "种姓相配缺失，可能导致性情差异与自我冲突。",
                "nadi" to "脉相配缺失，可能引发健康与遗传问题，不建议成婚。"
            )
        )
    }
}
