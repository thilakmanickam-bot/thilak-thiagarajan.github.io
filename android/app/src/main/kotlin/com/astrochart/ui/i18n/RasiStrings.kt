package com.astrochart.ui.i18n

import com.astrochart.core.i18n.ContentLang
import com.astrochart.core.i18n.Language

/** Localized labels for the Rasi Palan hub and its detail screens (EN / TA / ZH). */
data class RasiStrings(
    val title: String,
    val entry: String,
    val today: String,
    val weekly: String,
    val monthly: String,
    val yearly: String,
    val aboutSigns: String,
    val aboutNakshatras: String,
    val chooseSign: String,
    val rulingPlanet: String,
    val friendlySigns: String,
    val luckyColor: String,
    val luckyDay: String,
    val luckyNumber: String,
    val deity: String,
    val gemstone: String,
    val character: String,
    val nakshatraLord: String
) {
    companion object {
        fun forLanguage(lang: Language): RasiStrings = when (lang.content) {
            ContentLang.EN -> EN
            ContentLang.TA -> TA
            ContentLang.ZH -> ZH
        }

        private val EN = RasiStrings(
            title = "Rasi Palan",
            entry = "Rasi Palan (Horoscope)",
            today = "Today",
            weekly = "Weekly",
            monthly = "Monthly",
            yearly = "Yearly",
            aboutSigns = "About the rasis",
            aboutNakshatras = "About nakshatras",
            chooseSign = "Choose your rasi",
            rulingPlanet = "Ruling planet",
            friendlySigns = "Friendly rasis",
            luckyColor = "Lucky colour",
            luckyDay = "Lucky day",
            luckyNumber = "Lucky number",
            deity = "Deity",
            gemstone = "Gemstone",
            character = "Character",
            nakshatraLord = "Lord"
        )

        private val TA = RasiStrings(
            title = "ராசி பலன்",
            entry = "ராசி பலன்",
            today = "இன்று",
            weekly = "வார",
            monthly = "மாத",
            yearly = "ஆண்டு",
            aboutSigns = "ராசி பற்றிய தகவல்கள்",
            aboutNakshatras = "நட்சத்திரம் பற்றிய தகவல்கள்",
            chooseSign = "உங்கள் ராசியைத் தேர்ந்தெடுக்கவும்",
            rulingPlanet = "அதிபதி",
            friendlySigns = "நட்பு ராசிகள்",
            luckyColor = "அதிர்ஷ்ட நிறம்",
            luckyDay = "அதிர்ஷ்ட நாள்",
            luckyNumber = "அதிர்ஷ்ட எண்",
            deity = "தெய்வம்",
            gemstone = "கல்",
            character = "குணம்",
            nakshatraLord = "அதிபதி"
        )

        private val ZH = RasiStrings(
            title = "星座运势",
            entry = "星座运势",
            today = "今日",
            weekly = "每周",
            monthly = "每月",
            yearly = "每年",
            aboutSigns = "星座介绍",
            aboutNakshatras = "星宿介绍",
            chooseSign = "选择你的星座",
            rulingPlanet = "主宰行星",
            friendlySigns = "相合星座",
            luckyColor = "幸运色",
            luckyDay = "幸运日",
            luckyNumber = "幸运数字",
            deity = "守护神",
            gemstone = "宝石",
            character = "性格",
            nakshatraLord = "主星"
        )
    }
}
