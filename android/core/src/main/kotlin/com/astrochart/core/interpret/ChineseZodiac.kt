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
            Language.EN -> animalsEn[i]
            Language.TA -> animalsTa[i]
            Language.ZH -> animalsZh[i]
        }
    }

    /** Emoji for the given birth year's animal. */
    fun emoji(year: Int): String = emoji[index(year)]
}
