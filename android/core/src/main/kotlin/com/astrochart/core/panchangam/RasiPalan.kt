package com.astrochart.core.panchangam

import com.astrochart.core.i18n.ContentLang
import com.astrochart.core.i18n.Language
import kotlin.random.Random

/**
 * A light, optimistic one-word daily outlook for each of the twelve moon-signs
 * (rasi) — the "raasi palan" strip. Deterministic per calendar day and sign, so
 * it is stable across refreshes, and purely for daily flavour (not a claim of
 * predictive accuracy), matching the tone of [com.astrochart.core.interpret.DailyReading].
 */
object RasiPalan {

    private data class Word(val en: String, val ta: String, val zh: String)

    private val WORDS = listOf(
        Word("Joy", "மகிழ்ச்சி", "喜悦"),
        Word("Success", "வெற்றி", "成功"),
        Word("Gains", "லாபம்", "获益"),
        Word("Good news", "நற்செய்தி", "喜讯"),
        Word("Support", "ஆதரவு", "扶持"),
        Word("Progress", "முன்னேற்றம்", "进展"),
        Word("Harmony", "நல்லிணக்கம்", "和谐"),
        Word("Confidence", "தன்னம்பிக்கை", "自信"),
        Word("Care needed", "கவனம்", "谨慎"),
        Word("Patience", "பொறுமை", "耐心"),
        Word("Rest", "ஓய்வு", "休息"),
        Word("New start", "புதுத்தொடக்கம்", "新起点")
    )

    /** Twelve signs in rasi order, index 0 = Mesha (Aries) … 11 = Meena (Pisces). */
    fun word(epochDay: Long, signIndex: Int, lang: Language): String {
        val rnd = Random(epochDay * 31L + signIndex)
        val w = WORDS[rnd.nextInt(WORDS.size)]
        return when (lang.content) {
            ContentLang.EN -> w.en
            ContentLang.TA -> w.ta
            ContentLang.ZH -> w.zh
        }
    }
}
