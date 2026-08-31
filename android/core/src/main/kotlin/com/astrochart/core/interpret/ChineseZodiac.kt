package com.astrochart.core.interpret

import com.astrochart.core.i18n.Language

/**
 * Chinese zodiac (Shengxiao) — the twelve-year animal cycle derived from the
 * birth year. Uses the Gregorian year (does not account for the Chinese New Year
 * boundary in Jan/Feb), matching a simple born-year lookup.
 */
object ChineseZodiac {

    /** Canonical animals in cycle order; index 0 = Rat corresponds to (year-4) mod 12. */
    private val animalsEn = listOf(
        "Rat", "Ox", "Tiger", "Rabbit", "Dragon", "Snake",
        "Horse", "Goat", "Monkey", "Rooster", "Dog", "Pig"
    )
    private val animalsTa = listOf(
        "எலி", "எருது", "புலி", "முயல்", "நாகம்", "பாம்பு",
        "குதிரை", "ஆடு", "குரங்கு", "சேவல்", "நாய்", "பன்றி"
    )
    private val animalsZh = listOf(
        "鼠", "牛", "虎", "兔", "龙", "蛇",
        "马", "羊", "猴", "鸡", "狗", "猪"
    )
    private val animalsHi = listOf(
        "चूहा", "बैल", "बाघ", "खरगोश", "ड्रैगन", "सांप",
        "घोड़ा", "बकरी", "बंदर", "मुर्गा", "कुत्ता", "सूअर"
    )
    private val animalsTe = listOf(
        "ఎలుక", "ఎద్దు", "పులి", "కుందేలు", "డ్రాగన్", "పాము",
        "గుర్రం", "మేక", "కోతి", "కోడి", "కుక్క", "పంది"
    )
    private val animalsKn = listOf(
        "ಇಲಿ", "ಎತ್ತು", "ಹುಲಿ", "ಮೊಲ", "ಡ್ರ್ಯಾಗನ್", "ಹಾವು",
        "ಕುದುರೆ", "ಆಡು", "ಕೋತಿ", "ಹುಂಜ", "ನಾಯಿ", "ಹಂದಿ"
    )
    private val animalsMl = listOf(
        "എലി", "കാള", "കടുവ", "മുയൽ", "ഡ്രാഗൺ", "പാമ്പ്",
        "കുതിര", "ആട്", "കുരങ്ങ്", "പൂവൻകോഴി", "നായ", "പന്നി"
    )
    private val animalsMr = listOf(
        "उंदीर", "बैल", "वाघ", "ससा", "ड्रॅगन", "साप",
        "घोडा", "बकरी", "माकड", "कोंबडा", "कुत्रा", "डुक्कर"
    )
    private val emoji = listOf(
        "🐀", "🐂", "🐅", "🐇", "🐉", "🐍",
        "🐎", "🐐", "🐒", "🐓", "🐕", "🐖"
    )

    private fun index(year: Int): Int = Math.floorMod(year - 4, 12)

    /** English animal name for the given birth year. */
    fun of(year: Int): String = animalsEn[index(year)]

    /** Localized animal name for the given birth year. */
    fun name(year: Int, lang: Language): String {
        val i = index(year)
        return when (lang) {
            Language.TA -> animalsTa[i]
            Language.ZH -> animalsZh[i]
            Language.HI -> animalsHi[i]
            Language.TE -> animalsTe[i]
            Language.KN -> animalsKn[i]
            Language.ML -> animalsMl[i]
            Language.MR -> animalsMr[i]
            else -> animalsEn[i]
        }
    }

    /** Emoji for the given birth year's animal. */
    fun emoji(year: Int): String = emoji[index(year)]
}
