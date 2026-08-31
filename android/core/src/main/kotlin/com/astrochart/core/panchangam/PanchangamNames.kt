package com.astrochart.core.panchangam

import com.astrochart.core.i18n.Language

/**
 * A panchangam term with an English (romanized) form, a Tamil-script form and an
 * optional Hindi (Devanagari) form. [hi] defaults to the English romanization for
 * terms not yet given a Devanagari name.
 */
data class PName(val en: String, val ta: String, val hi: String = en) {
    fun get(lang: Language): String = when (lang) {
        Language.TA -> ta
        Language.HI -> hi
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
        PName("Sunday", "ஞாயிறு", "रविवार"), PName("Monday", "திங்கள்", "सोमवार"),
        PName("Tuesday", "செவ்வாய்", "मंगलवार"), PName("Wednesday", "புதன்", "बुधवार"),
        PName("Thursday", "வியாழன்", "गुरुवार"), PName("Friday", "வெள்ளி", "शुक्रवार"),
        PName("Saturday", "சனி", "शनिवार")
    )

    /** Tamil solar months, index 0 = Chithirai (sidereal Aries) … 11 = Panguni. */
    val tamilMonths = listOf(
        PName("Chithirai", "சித்திரை", "चित्तिरै"), PName("Vaikasi", "வைகாசி", "वैकासि"),
        PName("Aani", "ஆனி", "आनि"), PName("Aadi", "ஆடி", "आडि"),
        PName("Aavani", "ஆவணி", "आवणि"), PName("Purattasi", "புரட்டாசி", "पुरट्टासि"),
        PName("Aippasi", "ஐப்பசி", "ऐप्पसि"), PName("Karthigai", "கார்த்திகை", "कार्त्तिगै"),
        PName("Margazhi", "மார்கழி", "मार्गऴि"), PName("Thai", "தை", "तै"),
        PName("Maasi", "மாசி", "मासि"), PName("Panguni", "பங்குனி", "पंगुनि")
    )

    /** 27 nakshatras, index 0 = Ashwini. */
    val nakshatras = listOf(
        PName("Ashwini", "அசுவினி", "अश्विनी"), PName("Bharani", "பரணி", "भरणी"),
        PName("Krittika", "கார்த்திகை", "कृत्तिका"), PName("Rohini", "ரோகிணி", "रोहिणी"),
        PName("Mrigashira", "மிருகசீரிடம்", "मृगशिरा"), PName("Ardra", "திருவாதிரை", "आर्द्रा"),
        PName("Punarvasu", "புனர்பூசம்", "पुनर्वसु"), PName("Pushya", "பூசம்", "पुष्य"),
        PName("Ashlesha", "ஆயில்யம்", "आश्लेषा"), PName("Magha", "மகம்", "मघा"),
        PName("Purva Phalguni", "பூரம்", "पूर्वा फाल्गुनी"), PName("Uttara Phalguni", "உத்திரம்", "उत्तरा फाल्गुनी"),
        PName("Hasta", "அஸ்தம்", "हस्त"), PName("Chitra", "சித்திரை", "चित्रा"),
        PName("Swati", "சுவாதி", "स्वाति"), PName("Vishakha", "விசாகம்", "विशाखा"),
        PName("Anuradha", "அனுஷம்", "अनुराधा"), PName("Jyeshtha", "கேட்டை", "ज्येष्ठा"),
        PName("Mula", "மூலம்", "मूल"), PName("Purva Ashadha", "பூராடம்", "पूर्वाषाढ़ा"),
        PName("Uttara Ashadha", "உத்திராடம்", "उत्तराषाढ़ा"), PName("Shravana", "திருவோணம்", "श्रवण"),
        PName("Dhanishta", "அவிட்டம்", "धनिष्ठा"), PName("Shatabhisha", "சதயம்", "शतभिषा"),
        PName("Purva Bhadrapada", "பூரட்டாதி", "पूर्व भाद्रपद"), PName("Uttara Bhadrapada", "உத்திரட்டாதி", "उत्तर भाद्रपद"),
        PName("Revati", "ரேவதி", "रेवती")
    )

    /** 27 yogas, index 0 = Vishkambha. */
    val yogas = listOf(
        PName("Vishkambha", "விஷ்கம்பம்"), PName("Priti", "பிரீதி"), PName("Ayushman", "ஆயுஷ்மான்"),
        PName("Saubhagya", "சௌபாக்யம்"), PName("Shobhana", "சோபனம்"), PName("Atiganda", "அதிகண்டம்"),
        PName("Sukarma", "சுகர்மா"), PName("Dhriti", "திருதி"), PName("Shula", "சூலம்"),
        PName("Ganda", "கண்டம்"), PName("Vriddhi", "விருத்தி"), PName("Dhruva", "துருவம்"),
        PName("Vyaghata", "வியாகாதம்"), PName("Harshana", "ஹர்ஷணம்"), PName("Vajra", "வஜ்ரம்"),
        PName("Siddhi", "சித்தி"), PName("Vyatipata", "வியதீபாதம்"), PName("Variyana", "வரியான்"),
        PName("Parigha", "பரிகம்"), PName("Shiva", "சிவம்"), PName("Siddha", "சித்தம்"),
        PName("Sadhya", "சாத்தியம்"), PName("Shubha", "சுபம்"), PName("Shukla", "சுக்லம்"),
        PName("Brahma", "பிரம்மம்"), PName("Indra", "ஐந்திரம்"), PName("Vaidhriti", "வைதிருதி")
    )

    /** Tithi names within a paksha, index 0 = Prathama … 13 = Chaturdashi, 14 = Purnima/Amavasya. */
    private val tithiBase = listOf(
        PName("Prathama", "பிரதமை", "प्रतिपदा"), PName("Dwitiya", "துவிதியை", "द्वितीया"),
        PName("Tritiya", "திருதியை", "तृतीया"), PName("Chaturthi", "சதுர்த்தி", "चतुर्थी"),
        PName("Panchami", "பஞ்சமி", "पंचमी"), PName("Shashti", "சஷ்டி", "षष्ठी"),
        PName("Saptami", "சப்தமி", "सप्तमी"), PName("Ashtami", "அஷ்டமி", "अष्टमी"),
        PName("Navami", "நவமி", "नवमी"), PName("Dashami", "தசமி", "दशमी"),
        PName("Ekadashi", "ஏகாதசி", "एकादशी"), PName("Dwadashi", "துவாதசி", "द्वादशी"),
        PName("Trayodashi", "திரயோதசி", "त्रयोदशी"), PName("Chaturdashi", "சதுர்த்தசி", "चतुर्दशी")
    )
    private val purnima = PName("Purnima", "பௌர்ணமி", "पूर्णिमा")
    private val amavasya = PName("Amavasya", "அமாவாசை", "अमावस्या")

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
        if (tithi0 < 15) PName("Shukla", "வளர்பிறை", "शुक्ल") else PName("Krishna", "தேய்பிறை", "कृष्ण")

    private val movableKaranas = listOf(
        PName("Bava", "பவ"), PName("Balava", "பாலவ"), PName("Kaulava", "கௌலவ"),
        PName("Taitila", "தைதுல"), PName("Gara", "கரசை"), PName("Vanija", "வணிசை"),
        PName("Vishti", "விஷ்டி")
    )
    private val fixedKaranas = mapOf(
        0 to PName("Kimstughna", "கிம்ஸ்துக்ன"),
        57 to PName("Shakuni", "சகுனி"),
        58 to PName("Chatushpada", "சதுஷ்பாத"),
        59 to PName("Naga", "நாக")
    )

    /** Karana name for a 0-based half-tithi index (0..59). */
    fun karanaName(half0: Int): PName {
        val h = ((half0 % 60) + 60) % 60
        fixedKaranas[h]?.let { return it }
        return movableKaranas[(h - 1) % 7]
    }
}
