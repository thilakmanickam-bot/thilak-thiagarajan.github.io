package com.astrochart.core.panchangam

import com.astrochart.core.i18n.Language

/** A panchangam term with an English (romanized) and a Tamil-script form. */
data class PName(val en: String, val ta: String) {
    fun get(lang: Language): String = if (lang == Language.TA) ta else en
}

/**
 * Localized names for the panchangam elements. English forms are the common
 * romanized Sanskrit/Tamil terms; Tamil forms are in Tamil script. Other
 * languages fall back to the romanized English (these are proper nouns).
 */
object PanchangamNames {

    /** Index 0 = Sunday … 6 = Saturday. */
    val weekdays = listOf(
        PName("Sunday", "ஞாயிறு"), PName("Monday", "திங்கள்"), PName("Tuesday", "செவ்வாய்"),
        PName("Wednesday", "புதன்"), PName("Thursday", "வியாழன்"), PName("Friday", "வெள்ளி"),
        PName("Saturday", "சனி")
    )

    /** Tamil solar months, index 0 = Chithirai (sidereal Aries) … 11 = Panguni. */
    val tamilMonths = listOf(
        PName("Chithirai", "சித்திரை"), PName("Vaikasi", "வைகாசி"), PName("Aani", "ஆனி"),
        PName("Aadi", "ஆடி"), PName("Aavani", "ஆவணி"), PName("Purattasi", "புரட்டாசி"),
        PName("Aippasi", "ஐப்பசி"), PName("Karthigai", "கார்த்திகை"), PName("Margazhi", "மார்கழி"),
        PName("Thai", "தை"), PName("Maasi", "மாசி"), PName("Panguni", "பங்குனி")
    )

    /** 27 nakshatras, index 0 = Ashwini. */
    val nakshatras = listOf(
        PName("Ashwini", "அசுவினி"), PName("Bharani", "பரணி"), PName("Krittika", "கார்த்திகை"),
        PName("Rohini", "ரோகிணி"), PName("Mrigashira", "மிருகசீரிடம்"), PName("Ardra", "திருவாதிரை"),
        PName("Punarvasu", "புனர்பூசம்"), PName("Pushya", "பூசம்"), PName("Ashlesha", "ஆயில்யம்"),
        PName("Magha", "மகம்"), PName("Purva Phalguni", "பூரம்"), PName("Uttara Phalguni", "உத்திரம்"),
        PName("Hasta", "அஸ்தம்"), PName("Chitra", "சித்திரை"), PName("Swati", "சுவாதி"),
        PName("Vishakha", "விசாகம்"), PName("Anuradha", "அனுஷம்"), PName("Jyeshtha", "கேட்டை"),
        PName("Mula", "மூலம்"), PName("Purva Ashadha", "பூராடம்"), PName("Uttara Ashadha", "உத்திராடம்"),
        PName("Shravana", "திருவோணம்"), PName("Dhanishta", "அவிட்டம்"), PName("Shatabhisha", "சதயம்"),
        PName("Purva Bhadrapada", "பூரட்டாதி"), PName("Uttara Bhadrapada", "உத்திரட்டாதி"),
        PName("Revati", "ரேவதி")
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
        PName("Prathama", "பிரதமை"), PName("Dwitiya", "துவிதியை"), PName("Tritiya", "திருதியை"),
        PName("Chaturthi", "சதுர்த்தி"), PName("Panchami", "பஞ்சமி"), PName("Shashti", "சஷ்டி"),
        PName("Saptami", "சப்தமி"), PName("Ashtami", "அஷ்டமி"), PName("Navami", "நவமி"),
        PName("Dashami", "தசமி"), PName("Ekadashi", "ஏகாதசி"), PName("Dwadashi", "துவாதசி"),
        PName("Trayodashi", "திரயோதசி"), PName("Chaturdashi", "சதுர்த்தசி")
    )
    private val purnima = PName("Purnima", "பௌர்ணமி")
    private val amavasya = PName("Amavasya", "அமாவாசை")

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
        if (tithi0 < 15) PName("Shukla", "வளர்பிறை") else PName("Krishna", "தேய்பிறை")

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
