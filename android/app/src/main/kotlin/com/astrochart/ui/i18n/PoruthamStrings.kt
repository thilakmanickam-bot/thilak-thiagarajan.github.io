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
    val groomDetails: String,
    val brideDetails: String,
    val name: String,
    val enterGroomName: String,
    val enterBrideName: String,
    val rasi: String,
    val nakshatram: String,
    val calculate: String,
    val groomName: String,
    val brideName: String,
    val totalScore: String,
    val kuta: String,
    val gained: String,
    val max: String,
    val present: String,
    val absent: String,
    val askUniverse: String,
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
            Language.TE -> TE
            Language.KN -> KN
            Language.ML -> ML
            Language.MR -> MR
            else -> EN
        }

        private val EN = PoruthamStrings(
            title = "Marriage Match Making",
            entry = "Marriage Match Making",
            subtitle = "Check astrological compatibility for a perfect match",
            groomDetails = "Groom's Details",
            brideDetails = "Bride's Details",
            name = "Name",
            enterGroomName = "Enter the groom's name",
            enterBrideName = "Enter the bride's name",
            rasi = "Rasi",
            nakshatram = "Nakshatram",
            calculate = "Calculate Match",
            groomName = "Groom",
            brideName = "Bride",
            totalScore = "Total Compatibility Score",
            kuta = "Kuta",
            gained = "Gained",
            max = "Max",
            present = "Present",
            absent = "Absent",
            askUniverse = "Ask the Universe",
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
            groomDetails = "वर का विवरण",
            brideDetails = "वधू का विवरण",
            name = "नाम",
            enterGroomName = "वर का नाम भरें",
            enterBrideName = "वधू का नाम भरें",
            rasi = "राशि",
            nakshatram = "नक्षत्र",
            calculate = "मिलान करें",
            groomName = "वर का नाम",
            brideName = "वधू का नाम",
            totalScore = "कुल अनुकूलता अंक",
            kuta = "कूट",
            gained = "प्राप्त",
            max = "अधिकतम",
            present = "उपस्थित",
            absent = "अनुपस्थित",
            askUniverse = "ब्रह्मांड से पूछें",
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

        private val TE = PoruthamStrings(
            title = "వివాహ పొంతన",
            entry = "వివాహ పొంతన",
            subtitle = "ఉత్తమ జోడీ కోసం జ్యోతిష అనుకూలతను తనిఖీ చేయండి",
            groomDetails = "వరుడి వివరాలు",
            brideDetails = "వధువు వివరాలు",
            name = "పేరు",
            enterGroomName = "వరుడి పేరు నమోదు చేయండి",
            enterBrideName = "వధువు పేరు నమోదు చేయండి",
            rasi = "రాశి",
            nakshatram = "నక్షత్రం",
            calculate = "పొంతన చూడండి",
            groomName = "వరుడి పేరు",
            brideName = "వధువు పేరు",
            totalScore = "మొత్తం అనుకూలత స్కోరు",
            kuta = "కూటం",
            gained = "పొందిన",
            max = "గరిష్ఠం",
            present = "ఉంది",
            absent = "లేదు",
            askUniverse = "విశ్వాన్ని అడగండి",
            fillAll = "ఇద్దరికీ రాశి మరియు నక్షత్రాన్ని ఎంచుకోండి.",
            summaryGood = "ప్రధాన జ్యోతిష అంశాలు బాగా సరిపోతాయి. సామరస్యంతో కూడిన సంపన్న దాంపత్యం సూచించబడింది.",
            summaryFair = "అనుకూలత మరియు దీర్ఘాయువు విశ్లేషించబడ్డాయి. కొన్ని అంశాలను పరిగణించవలసిన ఓ మోస్తరు పొంతన.",
            summaryPoor = "అనేక అంశాలు లేవు. ముందుకు సాగే ముందు జ్యోతిష్కుడిని సంప్రదించండి.",
            summaryCritical = "అనుకూలత మరియు దీర్ఘాయువు విశ్లేషించబడ్డాయి. ముఖ్యమైన అనేక అంశాలు లేవు. ఆరోగ్యం మరియు దీర్ఘాయువుపై ఆందోళనలు ఉన్నాయి.",
            kootaNames = mapOf(
                "dina" to "దిన", "gana" to "గణ", "mahendra" to "మహేంద్ర",
                "streeDeergha" to "స్త్రీ దీర్ఘ", "yoni" to "యోని", "rasi" to "రాశి",
                "rasiAdhipathi" to "రాశ్యధిపతి", "vasya" to "వశ్య", "rajju" to "రజ్జు",
                "vedha" to "వేధ", "varna" to "వర్ణ", "nadi" to "నాడి"
            ),
            presentDesc = mapOf(
                "dina" to "దిన కూటం ఉంది; దంపతుల ఆరోగ్యం మరియు సంపదకు అనుకూలం.",
                "gana" to "గణ కూటం ఉంది; మంచి స్వభావం మరియు గుణానుకూలత.",
                "mahendra" to "మహేంద్ర కూటం ఉంది; సంతానం మరియు క్షేమానికి అనుకూలం.",
                "streeDeergha" to "స్త్రీ దీర్ఘ ఉంది; వధువు దీర్ఘాయువుకు మద్దతు.",
                "yoni" to "యోని కూటం ఉంది; మంచి శారీరక అనుకూలత.",
                "rasi" to "రాశి కూటం ఉంది; దంపతుల మధ్య ప్రేమ మరియు అనురాగాన్ని సూచిస్తుంది.",
                "rasiAdhipathi" to "రాశ్యధిపతి ఉంది; మానసిక అనుకూలతను సూచిస్తుంది.",
                "vasya" to "వశ్య కూటం ఉంది; పరస్పర ఆకర్షణను చూపుతుంది.",
                "rajju" to "రజ్జు కూటం ఉంది; వివాహ దీర్ఘతకు మద్దతు.",
                "vedha" to "వేధ కూటం ఉంది; దాంపత్య జీవితంలో తక్కువ ఆటంకాలను సూచిస్తుంది.",
                "varna" to "వర్ణ కూటం ఉంది; స్వభావంలో సామరస్యం, తక్కువ అహం ఘర్షణలు.",
                "nadi" to "నాడి కూటం ఉంది; మంచి ఆరోగ్యం మరియు ఆరోగ్యవంతమైన సంతానానికి అనుకూలం."
            ),
            absentDesc = mapOf(
                "dina" to "దిన కూటం లేదు; ఆరోగ్యం మరియు సంపదపై ప్రతికూల ప్రభావం సాధ్యం.",
                "gana" to "గణ కూటం లేదు; స్వభావంలో తేడాలు రావచ్చు.",
                "mahendra" to "మహేంద్ర కూటం లేదు; సంతానంలో ఆలస్యం కావచ్చు.",
                "streeDeergha" to "స్త్రీ దీర్ఘ లేదు; వధువు ఆయుష్షును ప్రభావితం చేయవచ్చు.",
                "yoni" to "యోని కూటం లేదు; శారీరక అనుకూలతను ప్రభావితం చేయవచ్చు.",
                "rasi" to "రాశి కూటం లేదు; ప్రేమ మరియు అనురాగాన్ని ప్రభావితం చేయవచ్చు.",
                "rasiAdhipathi" to "రాశ్యధిపతి లేదు; మానసిక అనుకూలతను ప్రభావితం చేయవచ్చు.",
                "vasya" to "వశ్య కూటం లేదు; పరస్పర ఆకర్షణ తగ్గవచ్చు.",
                "rajju" to "రజ్జు కూటం లేదు; ఇది చాలా ముఖ్యమైనది, వివాహ దీర్ఘతకు ప్రమాదకరం. వివాహం సిఫార్సు చేయబడదు.",
                "vedha" to "వేధ కూటం లేదు; దాంపత్య జీవితంలో ఆటంకాలు రావచ్చు.",
                "varna" to "వర్ణ కూటం లేదు; స్వభావ తేడాలు మరియు అహం ఘర్షణలు కావచ్చు.",
                "nadi" to "నాడి కూటం లేదు; ఆరోగ్య మరియు వంశపారంపర్య సమస్యలు రావచ్చు. వివాహం సిఫార్సు చేయబడదు."
            )
        )

        private val KN = PoruthamStrings(
            title = "ವಿವಾಹ ಹೊಂದಾಣಿಕೆ",
            entry = "ವಿವಾಹ ಹೊಂದಾಣಿಕೆ",
            subtitle = "ಸೂಕ್ತ ಜೋಡಿಗಾಗಿ ಜ್ಯೋತಿಷ ಹೊಂದಾಣಿಕೆಯನ್ನು ಪರಿಶೀಲಿಸಿ",
            groomDetails = "ವರನ ವಿವರಗಳು",
            brideDetails = "ವಧುವಿನ ವಿವರಗಳು",
            name = "ಹೆಸರು",
            enterGroomName = "ವರನ ಹೆಸರು ನಮೂದಿಸಿ",
            enterBrideName = "ವಧುವಿನ ಹೆಸರು ನಮೂದಿಸಿ",
            rasi = "ರಾಶಿ",
            nakshatram = "ನಕ್ಷತ್ರ",
            calculate = "ಹೊಂದಾಣಿಕೆ ಲೆಕ್ಕಿಸಿ",
            groomName = "ವರನ ಹೆಸರು",
            brideName = "ವಧುವಿನ ಹೆಸರು",
            totalScore = "ಒಟ್ಟು ಹೊಂದಾಣಿಕೆ ಅಂಕ",
            kuta = "ಕೂಟ",
            gained = "ಗಳಿಸಿದ",
            max = "ಗರಿಷ್ಠ",
            present = "ಇದೆ",
            absent = "ಇಲ್ಲ",
            askUniverse = "ವಿಶ್ವವನ್ನು ಕೇಳಿ",
            fillAll = "ಇಬ್ಬರಿಗೂ ರಾಶಿ ಮತ್ತು ನಕ್ಷತ್ರ ಆಯ್ಕೆಮಾಡಿ.",
            summaryGood = "ಪ್ರಮುಖ ಜ್ಯೋತಿಷ ಅಂಶಗಳು ಚೆನ್ನಾಗಿ ಹೊಂದುತ್ತವೆ. ಸಾಮರಸ್ಯ ಮತ್ತು ಸಮೃದ್ಧ ದಾಂಪತ್ಯ ಸೂಚಿತವಾಗಿದೆ.",
            summaryFair = "ಹೊಂದಾಣಿಕೆ ಮತ್ತು ದೀರ್ಘಾಯುಷ್ಯ ವಿಶ್ಲೇಷಿಸಲಾಗಿದೆ. ಕೆಲವು ಅಂಶಗಳನ್ನು ಪರಿಗಣಿಸಬೇಕಾದ ಸಾಧಾರಣ ಹೊಂದಾಣಿಕೆ.",
            summaryPoor = "ಹಲವು ಅಂಶಗಳಿಲ್ಲ. ಮುಂದುವರಿಯುವ ಮೊದಲು ಜ್ಯೋತಿಷಿಯನ್ನು ಸಂಪರ್ಕಿಸಿ.",
            summaryCritical = "ಹೊಂದಾಣಿಕೆ ಮತ್ತು ದೀರ್ಘಾಯುಷ್ಯ ವಿಶ್ಲೇಷಿಸಲಾಗಿದೆ. ಪ್ರಮುಖ ಹಲವು ಅಂಶಗಳಿಲ್ಲ. ಆರೋಗ್ಯ ಮತ್ತು ದೀರ್ಘಾಯುಷ್ಯದ ಬಗ್ಗೆ ಕಳವಳಗಳಿವೆ.",
            kootaNames = mapOf(
                "dina" to "ದಿನ", "gana" to "ಗಣ", "mahendra" to "ಮಹೇಂದ್ರ",
                "streeDeergha" to "ಸ್ತ್ರೀ ದೀರ್ಘ", "yoni" to "ಯೋನಿ", "rasi" to "ರಾಶಿ",
                "rasiAdhipathi" to "ರಾಶ್ಯಧಿಪತಿ", "vasya" to "ವಶ್ಯ", "rajju" to "ರಜ್ಜು",
                "vedha" to "ವೇಧ", "varna" to "ವರ್ಣ", "nadi" to "ನಾಡಿ"
            ),
            presentDesc = mapOf(
                "dina" to "ದಿನ ಕೂಟ ಇದೆ; ದಂಪತಿಗಳ ಆರೋಗ್ಯ ಮತ್ತು ಸಮೃದ್ಧಿಗೆ ಪೂರಕ.",
                "gana" to "ಗಣ ಕೂಟ ಇದೆ; ಉತ್ತಮ ಸ್ವಭಾವ ಮತ್ತು ಗುಣ ಹೊಂದಾಣಿಕೆ.",
                "mahendra" to "ಮಹೇಂದ್ರ ಕೂಟ ಇದೆ; ಸಂತಾನ ಮತ್ತು ಕ್ಷೇಮಕ್ಕೆ ಪೂರಕ.",
                "streeDeergha" to "ಸ್ತ್ರೀ ದೀರ್ಘ ಇದೆ; ವಧುವಿನ ದೀರ್ಘಾಯುಷ್ಯಕ್ಕೆ ಬೆಂಬಲ.",
                "yoni" to "ಯೋನಿ ಕೂಟ ಇದೆ; ಉತ್ತಮ ಶಾರೀರಿಕ ಹೊಂದಾಣಿಕೆ.",
                "rasi" to "ರಾಶಿ ಕೂಟ ಇದೆ; ದಂಪತಿಗಳ ನಡುವೆ ಪ್ರೀತಿ ಮತ್ತು ಅನುರಾಗ ಸೂಚಿಸುತ್ತದೆ.",
                "rasiAdhipathi" to "ರಾಶ್ಯಧಿಪತಿ ಇದೆ; ಮಾನಸಿಕ ಹೊಂದಾಣಿಕೆ ಸೂಚಿಸುತ್ತದೆ.",
                "vasya" to "ವಶ್ಯ ಕೂಟ ಇದೆ; ಪರಸ್ಪರ ಆಕರ್ಷಣೆ ತೋರಿಸುತ್ತದೆ.",
                "rajju" to "ರಜ್ಜು ಕೂಟ ಇದೆ; ವಿವಾಹದ ದೀರ್ಘತೆಗೆ ಬೆಂಬಲ.",
                "vedha" to "ವೇಧ ಕೂಟ ಇದೆ; ದಾಂಪತ್ಯ ಜೀವನದಲ್ಲಿ ಕಡಿಮೆ ಅಡೆತಡೆ ಸೂಚಿಸುತ್ತದೆ.",
                "varna" to "ವರ್ಣ ಕೂಟ ಇದೆ; ಸ್ವಭಾವದಲ್ಲಿ ಸಾಮರಸ್ಯ, ಕಡಿಮೆ ಅಹಂ ಘರ್ಷಣೆ.",
                "nadi" to "ನಾಡಿ ಕೂಟ ಇದೆ; ಉತ್ತಮ ಆರೋಗ್ಯ ಮತ್ತು ಆರೋಗ್ಯಕರ ಸಂತಾನಕ್ಕೆ ಪೂರಕ."
            ),
            absentDesc = mapOf(
                "dina" to "ದಿನ ಕೂಟ ಇಲ್ಲ; ಆರೋಗ್ಯ ಮತ್ತು ಸಮೃದ್ಧಿಯ ಮೇಲೆ ಪ್ರತಿಕೂಲ ಪರಿಣಾಮ ಸಾಧ್ಯ.",
                "gana" to "ಗಣ ಕೂಟ ಇಲ್ಲ; ಸ್ವಭಾವದಲ್ಲಿ ವ್ಯತ್ಯಾಸ ಬರಬಹುದು.",
                "mahendra" to "ಮಹೇಂದ್ರ ಕೂಟ ಇಲ್ಲ; ಸಂತಾನದಲ್ಲಿ ವಿಳಂಬ ಆಗಬಹುದು.",
                "streeDeergha" to "ಸ್ತ್ರೀ ದೀರ್ಘ ಇಲ್ಲ; ವಧುವಿನ ಆಯುಷ್ಯದ ಮೇಲೆ ಪರಿಣಾಮ ಬೀರಬಹುದು.",
                "yoni" to "ಯೋನಿ ಕೂಟ ಇಲ್ಲ; ಶಾರೀರಿಕ ಹೊಂದಾಣಿಕೆಯ ಮೇಲೆ ಪರಿಣಾಮ ಬೀರಬಹುದು.",
                "rasi" to "ರಾಶಿ ಕೂಟ ಇಲ್ಲ; ಪ್ರೀತಿ ಮತ್ತು ಅನುರಾಗದ ಮೇಲೆ ಪರಿಣಾಮ ಬೀರಬಹುದು.",
                "rasiAdhipathi" to "ರಾಶ್ಯಧಿಪತಿ ಇಲ್ಲ; ಮಾನಸಿಕ ಹೊಂದಾಣಿಕೆಯ ಮೇಲೆ ಪರಿಣಾಮ ಬೀರಬಹುದು.",
                "vasya" to "ವಶ್ಯ ಕೂಟ ಇಲ್ಲ; ಪರಸ್ಪರ ಆಕರ್ಷಣೆ ಕಡಿಮೆಯಾಗಬಹುದು.",
                "rajju" to "ರಜ್ಜು ಕೂಟ ಇಲ್ಲ; ಇದು ಬಹಳ ಮುಖ್ಯ, ವಿವಾಹದ ದೀರ್ಘತೆಗೆ ಅಪಾಯಕಾರಿ. ವಿವಾಹ ಶಿಫಾರಸು ಮಾಡಲಾಗುವುದಿಲ್ಲ.",
                "vedha" to "ವೇಧ ಕೂಟ ಇಲ್ಲ; ದಾಂಪತ್ಯ ಜೀವನದಲ್ಲಿ ಅಡೆತಡೆ ಬರಬಹುದು.",
                "varna" to "ವರ್ಣ ಕೂಟ ಇಲ್ಲ; ಸ್ವಭಾವ ವ್ಯತ್ಯಾಸ ಮತ್ತು ಅಹಂ ಘರ್ಷಣೆ ಆಗಬಹುದು.",
                "nadi" to "ನಾಡಿ ಕೂಟ ಇಲ್ಲ; ಆರೋಗ್ಯ ಮತ್ತು ಆನುವಂಶಿಕ ಸಮಸ್ಯೆ ಬರಬಹುದು. ವಿವಾಹ ಶಿಫಾರಸು ಮಾಡಲಾಗುವುದಿಲ್ಲ."
            )
        )

        private val ML = PoruthamStrings(
            title = "വിവാഹ പൊരുത്തം",
            entry = "വിവാഹ പൊരുത്തം",
            subtitle = "ഉത്തമ ജോഡിക്കായി ജ്യോതിഷ പൊരുത്തം പരിശോധിക്കുക",
            groomDetails = "വരന്റെ വിവരങ്ങൾ",
            brideDetails = "വധുവിന്റെ വിവരങ്ങൾ",
            name = "പേര്",
            enterGroomName = "വരന്റെ പേര് നൽകുക",
            enterBrideName = "വധുവിന്റെ പേര് നൽകുക",
            rasi = "രാശി",
            nakshatram = "നക്ഷത്രം",
            calculate = "പൊരുത്തം കണക്കാക്കുക",
            groomName = "വരന്റെ പേര്",
            brideName = "വധുവിന്റെ പേര്",
            totalScore = "മൊത്തം പൊരുത്ത സ്കോർ",
            kuta = "പൊരുത്തം",
            gained = "നേടിയത്",
            max = "പരമാവധി",
            present = "ഉണ്ട്",
            absent = "ഇല്ല",
            askUniverse = "പ്രപഞ്ചത്തോട് ചോദിക്കുക",
            fillAll = "രണ്ടുപേർക്കും രാശിയും നക്ഷത്രവും തിരഞ്ഞെടുക്കുക.",
            summaryGood = "പ്രധാന ജ്യോതിഷ ഘടകങ്ങൾ നന്നായി യോജിക്കുന്നു. ഇണക്കമുള്ള, സമൃദ്ധമായ ദാമ്പത്യം സൂചിപ്പിക്കുന്നു.",
            summaryFair = "പൊരുത്തവും ദീർഘായുസ്സും വിശകലനം ചെയ്തു. ചില ഘടകങ്ങൾ പരിഗണിക്കേണ്ട ഒരു ശരാശരി പൊരുത്തം.",
            summaryPoor = "പല ഘടകങ്ങളും ഇല്ല. മുന്നോട്ട് പോകുന്നതിന് മുമ്പ് ഒരു ജ്യോതിഷിയെ സമീപിക്കുക.",
            summaryCritical = "പൊരുത്തവും ദീർഘായുസ്സും വിശകലനം ചെയ്തു. പ്രധാനപ്പെട്ട പല ഘടകങ്ങളും ഇല്ല. ആരോഗ്യവും ദീർഘായുസ്സും സംബന്ധിച്ച ആശങ്കകൾ ഉണ്ട്.",
            kootaNames = mapOf(
                "dina" to "ദിന", "gana" to "ഗണ", "mahendra" to "മഹേന്ദ്ര",
                "streeDeergha" to "സ്ത്രീ ദീർഘ", "yoni" to "യോനി", "rasi" to "രാശി",
                "rasiAdhipathi" to "രാശ്യധിപതി", "vasya" to "വശ്യ", "rajju" to "രജ്ജു",
                "vedha" to "വേധ", "varna" to "വർണ", "nadi" to "നാഡി"
            ),
            presentDesc = mapOf(
                "dina" to "ദിന കൂടം ഉണ്ട്; ദമ്പതികളുടെ ആരോഗ്യത്തിനും സമൃദ്ധിക്കും അനുകൂലം.",
                "gana" to "ഗണ കൂടം ഉണ്ട്; നല്ല സ്വഭാവവും ഗുണപൊരുത്തവും.",
                "mahendra" to "മഹേന്ദ്ര കൂടം ഉണ്ട്; സന്താനത്തിനും ക്ഷേമത്തിനും അനുകൂലം.",
                "streeDeergha" to "സ്ത്രീ ദീർഘ ഉണ്ട്; വധുവിന്റെ ദീർഘായുസ്സിന് പിന്തുണ.",
                "yoni" to "യോനി കൂടം ഉണ്ട്; നല്ല ശാരീരിക പൊരുത്തം.",
                "rasi" to "രാശി കൂടം ഉണ്ട്; ദമ്പതികൾക്കിടയിൽ സ്നേഹവും വാത്സല്യവും സൂചിപ്പിക്കുന്നു.",
                "rasiAdhipathi" to "രാശ്യധിപതി ഉണ്ട്; മാനസിക പൊരുത്തം സൂചിപ്പിക്കുന്നു.",
                "vasya" to "വശ്യ കൂടം ഉണ്ട്; പരസ്പര ആകർഷണം കാണിക്കുന്നു.",
                "rajju" to "രജ്ജു കൂടം ഉണ്ട്; വിവാഹത്തിന്റെ ദീർഘതയ്ക്ക് പിന്തുണ.",
                "vedha" to "വേധ കൂടം ഉണ്ട്; ദാമ്പത്യ ജീവിതത്തിൽ കുറഞ്ഞ തടസ്സങ്ങൾ സൂചിപ്പിക്കുന്നു.",
                "varna" to "വർണ കൂടം ഉണ്ട്; സ്വഭാവത്തിൽ ഇണക്കം, കുറഞ്ഞ അഹം സംഘർഷം.",
                "nadi" to "നാഡി കൂടം ഉണ്ട്; നല്ല ആരോഗ്യത്തിനും ആരോഗ്യമുള്ള സന്താനത്തിനും അനുകൂലം."
            ),
            absentDesc = mapOf(
                "dina" to "ദിന കൂടം ഇല്ല; ആരോഗ്യത്തിലും സമൃദ്ധിയിലും പ്രതികൂല സ്വാധീനം സാധ്യം.",
                "gana" to "ഗണ കൂടം ഇല്ല; സ്വഭാവത്തിൽ വ്യത്യാസങ്ങൾ വരാം.",
                "mahendra" to "മഹേന്ദ്ര കൂടം ഇല്ല; സന്താനത്തിൽ കാലതാമസം വരാം.",
                "streeDeergha" to "സ്ത്രീ ദീർഘ ഇല്ല; വധുവിന്റെ ആയുസ്സിനെ ബാധിക്കാം.",
                "yoni" to "യോനി കൂടം ഇല്ല; ശാരീരിക പൊരുത്തത്തെ ബാധിക്കാം.",
                "rasi" to "രാശി കൂടം ഇല്ല; സ്നേഹത്തെയും വാത്സല്യത്തെയും ബാധിക്കാം.",
                "rasiAdhipathi" to "രാശ്യധിപതി ഇല്ല; മാനസിക പൊരുത്തത്തെ ബാധിക്കാം.",
                "vasya" to "വശ്യ കൂടം ഇല്ല; പരസ്പര ആകർഷണം കുറയാം.",
                "rajju" to "രജ്ജു കൂടം ഇല്ല; ഇത് വളരെ പ്രധാനമാണ്, വിവാഹത്തിന്റെ ദീർഘതയ്ക്ക് അപകടകരം. വിവാഹം ശുപാർശ ചെയ്യുന്നില്ല.",
                "vedha" to "വേധ കൂടം ഇല്ല; ദാമ്പത്യ ജീവിതത്തിൽ തടസ്സങ്ങൾ വരാം.",
                "varna" to "വർണ കൂടം ഇല്ല; സ്വഭാവ വ്യത്യാസവും അഹം സംഘർഷവും വരാം.",
                "nadi" to "നാഡി കൂടം ഇല്ല; ആരോഗ്യ, പാരമ്പര്യ പ്രശ്നങ്ങൾ വരാം. വിവാഹം ശുപാർശ ചെയ്യുന്നില്ല."
            )
        )

        private val MR = PoruthamStrings(
            title = "विवाह जुळणी",
            entry = "विवाह जुळणी",
            subtitle = "उत्तम जोडीसाठी ज्योतिषीय अनुकूलता तपासा",
            groomDetails = "वराचे तपशील",
            brideDetails = "वधूचे तपशील",
            name = "नाव",
            enterGroomName = "वराचे नाव भरा",
            enterBrideName = "वधूचे नाव भरा",
            rasi = "राशी",
            nakshatram = "नक्षत्र",
            calculate = "जुळणी काढा",
            groomName = "वराचे नाव",
            brideName = "वधूचे नाव",
            totalScore = "एकूण अनुकूलता गुण",
            kuta = "कूट",
            gained = "मिळाले",
            max = "कमाल",
            present = "आहे",
            absent = "नाही",
            askUniverse = "विश्वाला विचारा",
            fillAll = "दोघांसाठी राशी आणि नक्षत्र निवडा.",
            summaryGood = "मुख्य ज्योतिषीय घटक चांगले जुळतात. सामंजस्यपूर्ण व समृद्ध वैवाहिक जीवनाचे संकेत आहेत.",
            summaryFair = "अनुकूलता व दीर्घायुष्याचे विश्लेषण केले. काही घटकांचा विचार करावा लागेल असे साधारण जुळणी.",
            summaryPoor = "अनेक घटक अनुपस्थित आहेत. पुढे जाण्यापूर्वी ज्योतिषाचा सल्ला घ्या.",
            summaryCritical = "अनुकूलता व दीर्घायुष्याचे विश्लेषण केले. अनेक महत्त्वाचे घटक अनुपस्थित आहेत. आरोग्य व दीर्घायुष्याबाबत चिंता आहेत.",
            kootaNames = mapOf(
                "dina" to "दिन", "gana" to "गण", "mahendra" to "महेंद्र",
                "streeDeergha" to "स्त्री दीर्घ", "yoni" to "योनी", "rasi" to "राशी",
                "rasiAdhipathi" to "राश्यधिपती", "vasya" to "वश्य", "rajju" to "रज्जू",
                "vedha" to "वेध", "varna" to "वर्ण", "nadi" to "नाडी"
            ),
            presentDesc = mapOf(
                "dina" to "दिन कूट आहे; जोडप्याच्या आरोग्य व समृद्धीस अनुकूल.",
                "gana" to "गण कूट आहे; चांगला स्वभाव व गुणानुकूलता.",
                "mahendra" to "महेंद्र कूट आहे; संतती व कल्याणास अनुकूल.",
                "streeDeergha" to "स्त्री दीर्घ आहे; वधूच्या दीर्घायुष्यास आधार.",
                "yoni" to "योनी कूट आहे; चांगली शारीरिक अनुकूलता.",
                "rasi" to "राशी कूट आहे; जोडप्यात प्रेम व स्नेह दर्शवते.",
                "rasiAdhipathi" to "राश्यधिपती आहे; मानसिक अनुकूलता दर्शवते.",
                "vasya" to "वश्य कूट आहे; परस्पर आकर्षण दर्शवते.",
                "rajju" to "रज्जू कूट आहे; विवाहाच्या दीर्घतेस आधार.",
                "vedha" to "वेध कूट आहे; वैवाहिक जीवनात कमी अडथळे दर्शवते.",
                "varna" to "वर्ण कूट आहे; स्वभावात सामंजस्य, कमी अहं संघर्ष.",
                "nadi" to "नाडी कूट आहे; चांगले आरोग्य व निरोगी संततीस अनुकूल."
            ),
            absentDesc = mapOf(
                "dina" to "दिन कूट नाही; आरोग्य व समृद्धीवर प्रतिकूल परिणाम शक्य.",
                "gana" to "गण कूट नाही; स्वभावात फरक येऊ शकतो.",
                "mahendra" to "महेंद्र कूट नाही; संततीत विलंब होऊ शकतो.",
                "streeDeergha" to "स्त्री दीर्घ नाही; वधूच्या आयुष्यावर परिणाम होऊ शकतो.",
                "yoni" to "योनी कूट नाही; शारीरिक अनुकूलतेवर परिणाम होऊ शकतो.",
                "rasi" to "राशी कूट नाही; प्रेम व स्नेहावर परिणाम होऊ शकतो.",
                "rasiAdhipathi" to "राश्यधिपती नाही; मानसिक अनुकूलतेवर परिणाम होऊ शकतो.",
                "vasya" to "वश्य कूट नाही; परस्पर आकर्षण कमी होऊ शकते.",
                "rajju" to "रज्जू कूट नाही; हे अत्यंत महत्त्वाचे असून विवाहाच्या दीर्घतेस धोकादायक. विवाहाची शिफारस केली जात नाही.",
                "vedha" to "वेध कूट नाही; वैवाहिक जीवनात अडथळे येऊ शकतात.",
                "varna" to "वर्ण कूट नाही; स्वभाव फरक व अहं संघर्ष होऊ शकतो.",
                "nadi" to "नाडी कूट नाही; आरोग्य व आनुवंशिक समस्या येऊ शकतात. विवाहाची शिफारस केली जात नाही."
            )
        )

        private val TA = PoruthamStrings(
            title = "திருமணப் பொருத்தம்",
            entry = "திருமணப் பொருத்தம்",
            subtitle = "சிறந்த பொருத்தத்திற்கு ஜோதிட பொருத்தத்தைச் சரிபார்க்கவும்",
            groomDetails = "மணமகன் விவரங்கள்",
            brideDetails = "மணமகள் விவரங்கள்",
            name = "பெயர்",
            enterGroomName = "மணமகன் பெயரை உள்ளிடவும்",
            enterBrideName = "மணமகள் பெயரை உள்ளிடவும்",
            rasi = "ராசி",
            nakshatram = "நட்சத்திரம்",
            calculate = "பொருத்தம் பார்க்க",
            groomName = "மணமகன் பெயர்",
            brideName = "மணமகள் பெயர்",
            totalScore = "மொத்த பொருத்த மதிப்பெண்",
            kuta = "பொருத்தம்",
            gained = "பெற்றது",
            max = "அதிகபட்சம்",
            present = "உண்டு",
            absent = "இல்லை",
            askUniverse = "பிரபஞ்சத்திடம் கேளுங்கள்",
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
            groomDetails = "男方信息",
            brideDetails = "女方信息",
            name = "姓名",
            enterGroomName = "输入男方姓名",
            enterBrideName = "输入女方姓名",
            rasi = "月亮星座",
            nakshatram = "出生星宿",
            calculate = "计算契合度",
            groomName = "男方姓名",
            brideName = "女方姓名",
            totalScore = "综合契合分数",
            kuta = "相配项",
            gained = "得分",
            max = "满分",
            present = "具备",
            absent = "缺失",
            askUniverse = "向宇宙提问",
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
