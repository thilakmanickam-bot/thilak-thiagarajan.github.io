package com.astrochart.ui.i18n

import com.astrochart.core.i18n.Language

/**
 * Localized labels for the panchangam and calendar screens. Kept separate from
 * [UiStrings] so the large panchangam vocabulary doesn't bloat the core string
 * table. English, Tamil, and Chinese are all provided.
 */
data class PanchangamStrings(
    val title: String,
    val calendarTitle: String,
    val calendarLabel: String,
    val monthCalendar: String,
    val today: String,
    val location: String,
    val sunrise: String,
    val sunset: String,
    val auspicious: String,
    val inauspicious: String,
    val abhijit: String,
    val brahmaMuhurta: String,
    val rahu: String,
    val yamagandam: String,
    val gulikai: String,
    val tithi: String,
    val nakshatra: String,
    val yoga: String,
    val karana: String,
    val paksha: String,
    val rasiPalan: String,
    val until: String,
    val then: String,
    val nextDay: String,
    val noSunToday: String,
    val swipeHint: String,
    val vrathaTitle: String,
    val vrathaNames: Map<String, String>
) {
    fun vratha(key: String): String = vrathaNames[key] ?: key

    companion object {
        fun forLanguage(lang: Language): PanchangamStrings = when (lang) {
            Language.EN -> EN
            Language.TA -> TA
            Language.ZH -> ZH
        }

        private val EN = PanchangamStrings(
            title = "Panchangam",
            calendarTitle = "Calendar",
            calendarLabel = "Calendar",
            monthCalendar = "Month calendar",
            today = "Today",
            location = "Location",
            sunrise = "Sunrise",
            sunset = "Sunset",
            auspicious = "Auspicious times",
            inauspicious = "Inauspicious times",
            abhijit = "Abhijit muhurta",
            brahmaMuhurta = "Brahma muhurta",
            rahu = "Rahu kalam",
            yamagandam = "Yamagandam",
            gulikai = "Gulikai",
            tithi = "Tithi",
            nakshatra = "Nakshatram",
            yoga = "Yogam",
            karana = "Karanam",
            paksha = "Paksha",
            rasiPalan = "Daily rasi palan",
            until = "until",
            then = "then",
            nextDay = "(next day)",
            noSunToday = "No sunrise/sunset at this location today.",
            swipeHint = "Swipe up for today's panchangam",
            vrathaTitle = "Vratham & special days",
            vrathaNames = mapOf(
                "amavasai" to "Amavasai (new moon)", "pournami" to "Pournami (full moon)",
                "ekadasi" to "Ekadasi", "sashti" to "Sashti", "chaturthi" to "Chaturthi",
                "sankatahara" to "Sankatahara Chaturthi", "pradosham" to "Pradosham",
                "sivarathiri" to "Sivarathiri", "krithigai" to "Krithigai", "thiruvonam" to "Thiruvonam"
            )
        )

        private val TA = PanchangamStrings(
            title = "பஞ்சாங்கம்",
            calendarTitle = "காலண்டர்",
            calendarLabel = "காலண்டர்",
            monthCalendar = "மாத காலண்டர்",
            today = "இன்று",
            location = "இடம்",
            sunrise = "சூரிய உதயம்",
            sunset = "சூரிய அஸ்தமனம்",
            auspicious = "நல்ல நேரம்",
            inauspicious = "தீய நேரம்",
            abhijit = "அபிஜித் முகூர்த்தம்",
            brahmaMuhurta = "பிரம்ம முகூர்த்தம்",
            rahu = "ராகு காலம்",
            yamagandam = "எமகண்டம்",
            gulikai = "குளிகை",
            tithi = "திதி",
            nakshatra = "நட்சத்திரம்",
            yoga = "யோகம்",
            karana = "கரணம்",
            paksha = "பக்ஷம்",
            rasiPalan = "இன்றைய ராசிபலன்",
            until = "வரை",
            then = "பின்பு",
            nextDay = "(மறுநாள்)",
            noSunToday = "இந்த இடத்தில் இன்று சூரிய உதயம்/அஸ்தமனம் இல்லை.",
            swipeHint = "இன்றைய பஞ்சாங்கத்திற்கு மேலே தள்ளுங்கள்",
            vrathaTitle = "விரத & விசேஷ தினங்கள்",
            vrathaNames = mapOf(
                "amavasai" to "அமாவாசை", "pournami" to "பௌர்ணமி",
                "ekadasi" to "ஏகாதசி", "sashti" to "சஷ்டி", "chaturthi" to "சதுர்த்தி",
                "sankatahara" to "சங்கடஹர சதுர்த்தி", "pradosham" to "பிரதோஷம்",
                "sivarathiri" to "சிவராத்திரி", "krithigai" to "கிருத்திகை", "thiruvonam" to "திருவோணம்"
            )
        )

        private val ZH = PanchangamStrings(
            title = "黄历",
            calendarTitle = "日历",
            calendarLabel = "日历",
            monthCalendar = "月历",
            today = "今天",
            location = "地点",
            sunrise = "日出",
            sunset = "日落",
            auspicious = "吉时",
            inauspicious = "凶时",
            abhijit = "阿毗吉特吉时",
            brahmaMuhurta = "梵天时",
            rahu = "罗睺时",
            yamagandam = "阎摩时",
            gulikai = "古力迦时",
            tithi = "太阴日",
            nakshatra = "星宿",
            yoga = "瑜伽",
            karana = "半日",
            paksha = "月相",
            rasiPalan = "每日星座运势",
            until = "至",
            then = "之后",
            nextDay = "（次日）",
            noSunToday = "该地点今日无日出/日落。",
            swipeHint = "上滑查看今日黄历",
            vrathaTitle = "斋戒与特殊日",
            vrathaNames = mapOf(
                "amavasai" to "新月 (Amavasai)", "pournami" to "满月 (Pournami)",
                "ekadasi" to "Ekadasi", "sashti" to "Sashti", "chaturthi" to "Chaturthi",
                "sankatahara" to "Sankatahara Chaturthi", "pradosham" to "Pradosham",
                "sivarathiri" to "Sivarathiri", "krithigai" to "Krithigai", "thiruvonam" to "Thiruvonam"
            )
        )
    }
}
