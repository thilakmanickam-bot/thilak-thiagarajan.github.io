package com.astrochart.core.panchangam

import com.astrochart.core.i18n.Language

/**
 * A panchangam term with an English (romanized) form, a Tamil-script form and an
 * optional Hindi (Devanagari) form. [hi] defaults to the English romanization for
 * terms not yet given a Devanagari name.
 */
data class PName(
    val en: String,
    val ta: String,
    val hi: String = en,
    val te: String = en,
    val kn: String = en,
    val ml: String = en,
    val mr: String = en
) {
    fun get(lang: Language): String = when (lang) {
        Language.TA -> ta
        Language.HI -> hi
        Language.TE -> te
        Language.KN -> kn
        Language.ML -> ml
        Language.MR -> mr
        else -> en
    }
}

/**
 * Localized names for the panchangam elements. English forms are the common
 * romanized Sanskrit/Tamil terms; Tamil forms are in Tamil script. Other
 * languages fall back to the romanized English (these are proper nouns).
 */
object PanchangamNames {

    /** Index 0 = Sunday … 6 = Saturday. */
    val weekdays = listOf(
        PName("Sunday", "ஞாயிறு", "रविवार", "ఆదివారం", "ಭಾನುವಾರ", "ഞായർ", "रविवार"),
        PName("Monday", "திங்கள்", "सोमवार", "సోమవారం", "ಸೋಮವಾರ", "തിങ്കൾ", "सोमवार"),
        PName("Tuesday", "செவ்வாய்", "मंगलवार", "మంగళవారం", "ಮಂಗಳವಾರ", "ചൊവ്വ", "मंगळवार"),
        PName("Wednesday", "புதன்", "बुधवार", "బుధవారం", "ಬುಧವಾರ", "ബുധൻ", "बुधवार"),
        PName("Thursday", "வியாழன்", "गुरुवार", "గురువారం", "ಗುರುವಾರ", "വ്യാഴം", "गुरुवार"),
        PName("Friday", "வெள்ளி", "शुक्रवार", "శుక్రవారం", "ಶುಕ್ರವಾರ", "വെള്ളി", "शुक्रवार"),
        PName("Saturday", "சனி", "शनिवार", "శనివారం", "ಶನಿವಾರ", "ശനി", "शनिवार")
    )

    /** Tamil solar months, index 0 = Chithirai (sidereal Aries) … 11 = Panguni. */
    val tamilMonths = listOf(
        PName("Chithirai", "சித்திரை", "चित्तिरै", "చిత్తిరై", "ಚಿತ್ತಿರೈ", "ചിത്തിര", "चित्तिरै"),
        PName("Vaikasi", "வைகாசி", "वैकासि", "వైకాసి", "ವೈಕಾಸಿ", "വൈകാസി", "वैकासी"),
        PName("Aani", "ஆனி", "आनि", "ఆని", "ಆನಿ", "ആനി", "आनी"),
        PName("Aadi", "ஆடி", "आडि", "ఆడి", "ಆಡಿ", "ആടി", "आडी"),
        PName("Aavani", "ஆவணி", "आवणि", "ఆవణి", "ಆವಣಿ", "ആവണി", "आवणी"),
        PName("Purattasi", "புரட்டாசி", "पुरट्टासि", "పురట్టాసి", "ಪುರಟ್ಟಾಸಿ", "പുരട്ടാസി", "पुरट्टासी"),
        PName("Aippasi", "ஐப்பசி", "ऐप्पसि", "ఐప్పసి", "ಐಪ್ಪಸಿ", "ഐപ്പസി", "ऐप्पसी"),
        PName("Karthigai", "கார்த்திகை", "कार्त्तिगै", "కార్తిగై", "ಕಾರ್ತಿಗೈ", "കാർത്തിഗൈ", "कार्तिगै"),
        PName("Margazhi", "மார்கழி", "मार्गळि", "మార్గళి", "ಮಾರ್ಗಳಿ", "മാർഗളി", "मार्गळी"),
        PName("Thai", "தை", "तै", "తై", "ತೈ", "തൈ", "तै"),
        PName("Maasi", "மாசி", "मासि", "మాసి", "ಮಾಸಿ", "മാസി", "माशी"),
        PName("Panguni", "பங்குனி", "पंगुनि", "పంగుని", "ಪಂಗುನಿ", "പങ്കുനി", "पंगुनी")
    )

    /** 27 nakshatras, index 0 = Ashwini. */
    val nakshatras = listOf(
        PName("Ashwini", "அசுவினி", "अश्विनी", "అశ్విని", "ಅಶ್ವಿನಿ", "അശ്വതി", "अश्विनी"),
        PName("Bharani", "பரணி", "भरणी", "భరణి", "ಭರಣಿ", "ഭരണി", "भरणी"),
        PName("Krittika", "கார்த்திகை", "कृत्तिका", "కృత్తిక", "ಕೃತ್ತಿಕಾ", "കാർത്തിക", "कृत्तिका"),
        PName("Rohini", "ரோகிணி", "रोहिणी", "రోహిణి", "ರೋಹಿಣಿ", "രോഹിണി", "रोहिणी"),
        PName("Mrigashira", "மிருகசீரிடம்", "मृगशिरा", "మృగశిర", "ಮೃಗಶಿರ", "മകയിരം", "मृगशीर्ष"),
        PName("Ardra", "திருவாதிரை", "आर्द्रा", "ఆర్ద్ర", "ಆರ್ದ್ರಾ", "തിരുവാതിര", "आर्द्रा"),
        PName("Punarvasu", "புனர்பூசம்", "पुनर्वसु", "పునర్వసు", "ಪುನರ್ವಸು", "പുണർതം", "पुनर्वसु"),
        PName("Pushya", "பூசம்", "पुष्य", "పుష్యమి", "ಪುಷ್ಯ", "പൂയം", "पुष्य"),
        PName("Ashlesha", "ஆயில்யம்", "आश्लेषा", "ఆశ్లేష", "ಆಶ್ಲೇಷ", "ആയില്യം", "आश्लेषा"),
        PName("Magha", "மகம்", "मघा", "మఖ", "ಮಘ", "മകം", "मघा"),
        PName("Purva Phalguni", "பூரம்", "पूर्वा फाल्गुनी", "పుబ్బ", "ಪೂರ್ವ ಫಲ್ಗುಣಿ", "പൂരം", "पूर्वा फाल्गुनी"),
        PName("Uttara Phalguni", "உத்திரம்", "उत्तरा फाल्गुनी", "ఉత్తర", "ಉತ್ತರ ಫಲ್ಗುಣಿ", "ഉത്രം", "उत्तरा फाल्गुनी"),
        PName("Hasta", "அஸ்தம்", "हस्त", "హస్త", "ಹಸ್ತ", "അത്തം", "हस्त"),
        PName("Chitra", "சித்திரை", "चित्रा", "చిత్త", "ಚಿತ್ರಾ", "ചിത്തിര", "चित्रा"),
        PName("Swati", "சுவாதி", "स्वाति", "స్వాతి", "ಸ್ವಾತಿ", "ചോതി", "स्वाती"),
        PName("Vishakha", "விசாகம்", "विशाखा", "విశాఖ", "ವಿಶಾಖ", "വിശാഖം", "विशाखा"),
        PName("Anuradha", "அனுஷம்", "अनुराधा", "అనూరాధ", "ಅನುರಾಧ", "അനിഴം", "अनुराधा"),
        PName("Jyeshtha", "கேட்டை", "ज्येष्ठा", "జ్యేష్ఠ", "ಜ್ಯೇಷ್ಠ", "കേട്ട", "ज्येष्ठा"),
        PName("Mula", "மூலம்", "मूल", "మూల", "ಮೂಲ", "മൂലം", "मूळ"),
        PName("Purva Ashadha", "பூராடம்", "पूर्वाषाढ़ा", "పూర్వాషాఢ", "ಪೂರ್ವಾಷಾಢ", "പൂരാടം", "पूर्वाषाढा"),
        PName("Uttara Ashadha", "உத்திராடம்", "उत्तराषाढ़ा", "ఉత్తరాషాఢ", "ಉತ್ತರಾಷಾಢ", "ഉത്രാടം", "उत्तराषाढा"),
        PName("Shravana", "திருவோணம்", "श्रवण", "శ్రవణం", "ಶ್ರವಣ", "തിരുവോണം", "श्रवण"),
        PName("Dhanishta", "அவிட்டம்", "धनिष्ठा", "ధనిష్ఠ", "ಧನಿಷ್ಠ", "അവിട്ടം", "धनिष्ठा"),
        PName("Shatabhisha", "சதயம்", "शतभिषा", "శతభిష", "ಶತಭಿಷ", "ചതയം", "शततारका"),
        PName("Purva Bhadrapada", "பூரட்டாதி", "पूर्व भाद्रपद", "పూర్వాభాద్ర", "ಪೂರ್ವಾಭಾದ್ರ", "പൂരുരുട്ടാതി", "पूर्वा भाद्रपदा"),
        PName("Uttara Bhadrapada", "உத்திரட்டாதி", "उत्तर भाद्रपद", "ఉత్తరాభాద్ర", "ಉತ್ತರಾಭಾದ್ರ", "ഉത്രട്ടാതി", "उत्तरा भाद्रपदा"),
        PName("Revati", "ரேவதி", "रेवती", "రేవతి", "ರೇವತಿ", "രേവതി", "रेवती")
    )

    /** 27 yogas, index 0 = Vishkambha. */
    val yogas = listOf(
        PName("Vishkambha", "விஷ்கம்பம்", "विष्कम्भ"), PName("Priti", "பிரீதி", "प्रीति"),
        PName("Ayushman", "ஆயுஷ்மான்", "आयुष्मान"), PName("Saubhagya", "சௌபாக்யம்", "सौभाग्य"),
        PName("Shobhana", "சோபனம்", "शोभन"), PName("Atiganda", "அதிகண்டம்", "अतिगण्ड"),
        PName("Sukarma", "சுகர்மா", "सुकर्मा"), PName("Dhriti", "திருதி", "धृति"),
        PName("Shula", "சூலம்", "शूल"), PName("Ganda", "கண்டம்", "गण्ड"),
        PName("Vriddhi", "விருத்தி", "वृद्धि"), PName("Dhruva", "துருவம்", "ध्रुव"),
        PName("Vyaghata", "வியாகாதம்", "व्याघात"), PName("Harshana", "ஹர்ஷணம்", "हर्षण"),
        PName("Vajra", "வஜ்ரம்", "वज्र"), PName("Siddhi", "சித்தி", "सिद्धि"),
        PName("Vyatipata", "வியதீபாதம்", "व्यतीपात"), PName("Variyana", "வரியான்", "वरीयान"),
        PName("Parigha", "பரிகம்", "परिघ"), PName("Shiva", "சிவம்", "शिव"),
        PName("Siddha", "சித்தம்", "सिद्ध"), PName("Sadhya", "சாத்தியம்", "साध्य"),
        PName("Shubha", "சுபம்", "शुभ"), PName("Shukla", "சுக்லம்", "शुक्ल"),
        PName("Brahma", "பிரம்மம்", "ब्रह्म"), PName("Indra", "ஐந்திரம்", "ऐन्द्र"),
        PName("Vaidhriti", "வைதிருதி", "वैधृति")
    )

    /** Tithi names within a paksha, index 0 = Prathama … 13 = Chaturdashi, 14 = Purnima/Amavasya. */
    private val tithiBase = listOf(
        PName("Prathama", "பிரதமை", "प्रतिपदा", "పాడ్యమి", "ಪ್ರಥಮ", "പ്രഥമ", "प्रतिपदा"),
        PName("Dwitiya", "துவிதியை", "द्वितीया", "విదియ", "ದ್ವಿತೀಯ", "ദ്വിതീയ", "द्वितीया"),
        PName("Tritiya", "திருதியை", "तृतीया", "తదియ", "ತೃತೀಯ", "തൃതീയ", "तृतीया"),
        PName("Chaturthi", "சதுர்த்தி", "चतुर्थी", "చవితి", "ಚತುರ್ಥಿ", "ചതുർത്ഥി", "चतुर्थी"),
        PName("Panchami", "பஞ்சமி", "पंचमी", "పంచమి", "ಪಂಚಮಿ", "പഞ്ചമി", "पंचमी"),
        PName("Shashti", "சஷ்டி", "षष्ठी", "షష్ఠి", "ಷಷ್ಠಿ", "ഷഷ്ഠി", "षष्ठी"),
        PName("Saptami", "சப்தமி", "सप्तमी", "సప్తమి", "ಸಪ್ತಮಿ", "സപ്തമി", "सप्तमी"),
        PName("Ashtami", "அஷ்டமி", "अष्टमी", "అష్టమి", "ಅಷ್ಟಮಿ", "അഷ്ടമി", "अष्टमी"),
        PName("Navami", "நவமி", "नवमी", "నవమి", "ನವಮಿ", "നവമി", "नवमी"),
        PName("Dashami", "தசமி", "दशमी", "దశమి", "ದಶಮಿ", "ദശമി", "दशमी"),
        PName("Ekadashi", "ஏகாதசி", "एकादशी", "ఏకాదశి", "ಏಕಾದಶಿ", "ഏകാദശി", "एकादशी"),
        PName("Dwadashi", "துவாதசி", "द्वादशी", "ద్వాదశి", "ದ್ವಾದಶಿ", "ദ്വാദശി", "द्वादशी"),
        PName("Trayodashi", "திரயோதசி", "त्रयोदशी", "త్రయోదశి", "ತ್ರಯೋದಶಿ", "ത്രയോദശി", "त्रयोदशी"),
        PName("Chaturdashi", "சதுர்த்தசி", "चतुर्दशी", "చతుర్దశి", "ಚತುರ್ದಶಿ", "ചതുർദശി", "चतुर्दशी")
    )
    private val purnima = PName("Purnima", "பௌர்ணமி", "पूर्णिमा", "పౌర్ణమి", "ಪೂರ್ಣಿಮೆ", "പൗർണമി", "पौर्णिमा")
    private val amavasya = PName("Amavasya", "அமாவாசை", "अमावस्या", "అమావాస్య", "ಅಮಾವಾಸ್ಯೆ", "അമാവാസി", "अमावस्या")

    /** Localized tithi name for a 0-based tithi index (0..29). */
    fun tithiName(tithi0: Int): PName {
        val inPaksha = tithi0 % 15 // 0..14
        return when {
            inPaksha < 14 -> tithiBase[inPaksha]
            tithi0 < 15 -> purnima // shukla 15 = full moon
            else -> amavasya       // krishna 15 = new moon
        }
    }

    /** Paksha (fortnight): 0 = Shukla (waxing), 1 = Krishna (waning). */
    fun paksha(tithi0: Int): PName =
        if (tithi0 < 15) PName("Shukla", "வளர்பிறை", "शुक्ल", "శుక్ల", "ಶುಕ್ಲ", "ശുക്ലം", "शुक्ल")
        else PName("Krishna", "தேய்பிறை", "कृष्ण", "కృష్ణ", "ಕೃಷ್ಣ", "കൃഷ്ണം", "कृष्ण")

    private val movableKaranas = listOf(
        PName("Bava", "பவ", "बव"), PName("Balava", "பாலவ", "बालव"), PName("Kaulava", "கௌலவ", "कौलव"),
        PName("Taitila", "தைதுல", "तैतिल"), PName("Gara", "கரசை", "गर"), PName("Vanija", "வணிசை", "वणिज"),
        PName("Vishti", "விஷ்டி", "विष्टि")
    )
    private val fixedKaranas = mapOf(
        0 to PName("Kimstughna", "கிம்ஸ்துக்ன", "किंस्तुघ्न"),
        57 to PName("Shakuni", "சகுனி", "शकुनि"),
        58 to PName("Chatushpada", "சதுஷ்பாத", "चतुष्पद"),
        59 to PName("Naga", "நாக", "नाग")
    )

    /** Karana name for a 0-based half-tithi index (0..59). */
    fun karanaName(half0: Int): PName {
        val h = ((half0 % 60) + 60) % 60
        fixedKaranas[h]?.let { return it }
        return movableKaranas[(h - 1) % 7]
    }
}
