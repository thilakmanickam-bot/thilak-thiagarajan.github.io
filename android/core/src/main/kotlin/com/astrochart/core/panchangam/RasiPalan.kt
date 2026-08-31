package com.astrochart.core.panchangam

import com.astrochart.core.i18n.Language
import kotlin.random.Random

/**
 * A light, optimistic one-word daily outlook for each of the twelve moon-signs
 * (rasi) — the "raasi palan" strip. Deterministic per calendar day and sign, so
 * it is stable across refreshes, and purely for daily flavour (not a claim of
 * predictive accuracy), matching the tone of [com.astrochart.core.interpret.DailyReading].
 */
object RasiPalan {

    private data class Word(
        val en: String, val ta: String, val zh: String,
        val hi: String, val te: String, val kn: String, val ml: String, val mr: String
    )

    private val WORDS = listOf(
        Word("Joy", "மகிழ்ச்சி", "喜悦", "आनंद", "ఆనందం", "ಆನಂದ", "സന്തോഷം", "आनंद"),
        Word("Success", "வெற்றி", "成功", "सफलता", "విజయం", "ಯಶಸ್ಸು", "വിജയം", "यश"),
        Word("Gains", "லாபம்", "获益", "लाभ", "లాభం", "ಲಾಭ", "നേട്ടം", "लाभ"),
        Word("Good news", "நற்செய்தி", "喜讯", "शुभ समाचार", "శుభవార్త", "ಶುಭ ಸುದ್ದಿ", "ശുഭവാർത്ത", "शुभ वार्ता"),
        Word("Support", "ஆதரவு", "扶持", "सहयोग", "మద్దతు", "ಬೆಂಬಲ", "പിന്തുണ", "पाठिंबा"),
        Word("Progress", "முன்னேற்றம்", "进展", "प्रगति", "పురోగతి", "ಪ್ರಗತಿ", "പുരോഗതി", "प्रगती"),
        Word("Harmony", "நல்லிணக்கம்", "和谐", "सामंजस्य", "సామరస్యం", "ಸಾಮರಸ್ಯ", "ഇണക്കം", "सामंजस्य"),
        Word("Confidence", "தன்னம்பிக்கை", "自信", "आत्मविश्वास", "ఆత్మవిశ్వాసం", "ಆತ್ಮವಿಶ್ವಾಸ", "ആത്മവിശ്വാസം", "आत्मविश्वास"),
        Word("Care needed", "கவனம்", "谨慎", "सावधानी", "జాగ్రత్త", "ಎಚ್ಚರಿಕೆ", "ജാഗ്രത", "सावधानी"),
        Word("Patience", "பொறுமை", "耐心", "धैर्य", "ఓపిక", "ತಾಳ್ಮೆ", "ക്ഷമ", "संयम"),
        Word("Rest", "ஓய்வு", "休息", "विश्राम", "విశ్రాంతి", "ವಿಶ್ರಾಂತಿ", "വിശ്രമം", "विश्रांती"),
        Word("New start", "புதுத்தொடக்கம்", "新起点", "नई शुरुआत", "కొత్త ప్రారంభం", "ಹೊಸ ಆರಂಭ", "പുതിയ തുടക്കം", "नवी सुरुवात")
    )

    /** Twelve signs in rasi order, index 0 = Mesha (Aries) … 11 = Meena (Pisces). */
    fun word(epochDay: Long, signIndex: Int, lang: Language): String {
        val rnd = Random(epochDay * 31L + signIndex)
        val w = WORDS[rnd.nextInt(WORDS.size)]
        return when (lang) {
            Language.TA -> w.ta
            Language.ZH -> w.zh
            Language.HI -> w.hi
            Language.TE -> w.te
            Language.KN -> w.kn
            Language.ML -> w.ml
            Language.MR -> w.mr
            else -> w.en
        }
    }
}
