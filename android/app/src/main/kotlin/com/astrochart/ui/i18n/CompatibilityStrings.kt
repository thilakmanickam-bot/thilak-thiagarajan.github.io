package com.astrochart.ui.i18n

import com.astrochart.core.i18n.Language

/** Localized labels for the couples-compatibility screen (EN / TA / ZH). */
data class CompatibilityStrings(
    val title: String,
    val entry: String,
    val choose: String,
    val personA: String,
    val personB: String,
    val overall: String,
    val sun: String,
    val moon: String,
    val ascendant: String,
    val gana: String,
    val birthStar: String,
    val needTwo: String,
    val notEnoughCharts: String,
    val excellent: String,
    val good: String,
    val fair: String,
    val challenging: String,
    val ganaNames: List<String> // [Deva, Manushya, Rakshasa]
) {
    /** Localized label for a component key from [com.astrochart.core.interpret.CompatibilityComponent]. */
    fun component(key: String): String = when (key) {
        "sun" -> sun
        "moon" -> moon
        "ascendant" -> ascendant
        "gana" -> gana
        else -> key
    }

    /** Verdict word for an overall/among score. */
    fun band(score: Int): String = when {
        score >= 80 -> excellent
        score >= 65 -> good
        score >= 50 -> fair
        else -> challenging
    }

    companion object {
        fun forLanguage(lang: Language): CompatibilityStrings = when (lang) {
            Language.EN -> EN
            Language.TA -> TA
            Language.ZH -> ZH
        }

        private val EN = CompatibilityStrings(
            title = "Compatibility",
            entry = "Couples Compatibility",
            choose = "Choose two saved profiles to see their match.",
            personA = "Profile 1",
            personB = "Profile 2",
            overall = "Overall match",
            sun = "Sun signs",
            moon = "Moon signs",
            ascendant = "Ascendants",
            gana = "Gana porutham",
            birthStar = "Birth star",
            needTwo = "Select two different profiles.",
            notEnoughCharts = "Save at least two charts first, then come back here.",
            excellent = "Excellent",
            good = "Good",
            fair = "Fair",
            challenging = "Challenging",
            ganaNames = listOf("Deva", "Manushya", "Rakshasa")
        )

        private val TA = CompatibilityStrings(
            title = "பொருத்தம்",
            entry = "தம்பதி பொருத்தம்",
            choose = "பொருத்தத்தைக் காண இரண்டு சேமித்த ஜாதகங்களைத் தேர்ந்தெடுக்கவும்.",
            personA = "ஜாதகம் 1",
            personB = "ஜாதகம் 2",
            overall = "ஒட்டுமொத்த பொருத்தம்",
            sun = "சூரிய ராசிகள்",
            moon = "சந்திர ராசிகள்",
            ascendant = "லக்னங்கள்",
            gana = "கண பொருத்தம்",
            birthStar = "பிறப்பு நட்சத்திரம்",
            needTwo = "வெவ்வேறு இரண்டு ஜாதகங்களைத் தேர்ந்தெடுக்கவும்.",
            notEnoughCharts = "முதலில் குறைந்தது இரண்டு ஜாதகங்களைச் சேமியுங்கள், பிறகு இங்கே வாருங்கள்.",
            excellent = "மிகச் சிறந்தது",
            good = "நல்லது",
            fair = "சராசரி",
            challenging = "சவாலானது",
            ganaNames = listOf("தேவ கணம்", "மனுஷ்ய கணம்", "ராட்சச கணம்")
        )

        private val ZH = CompatibilityStrings(
            title = "配对",
            entry = "情侣配对",
            choose = "选择两个已保存的档案查看配对结果。",
            personA = "档案 1",
            personB = "档案 2",
            overall = "综合契合度",
            sun = "太阳星座",
            moon = "月亮星座",
            ascendant = "上升星座",
            gana = "族群相配",
            birthStar = "出生星宿",
            needTwo = "请选择两个不同的档案。",
            notEnoughCharts = "请先保存至少两个星盘，然后再回到这里。",
            excellent = "极佳",
            good = "良好",
            fair = "一般",
            challenging = "有挑战",
            ganaNames = listOf("天神族", "人族", "罗刹族")
        )
    }
}
