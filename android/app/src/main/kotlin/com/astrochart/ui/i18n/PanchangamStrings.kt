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
    /**
     * Whether [until] reads before the time or after it.
     *
     * Defaults to after, because six of the eight languages here are
     * postpositional — Tamil "02:56 AM வரை", Hindi "…तक" — and a new Indic
     * translation is the likelier addition. English and Chinese set it true.
     */
    val untilPrecedesTime: Boolean = false,
    val then: String,
    val nextDay: String,
    val noSunToday: String,
    val swipeHint: String,
    val vrathaTitle: String,
    /** Notification channel name — user-visible in Android's app settings. */
    val reminderChannelName: String,
    /** Notification title on the morning of an observance the user opted into. */
    val reminderTitle: String,
    /** Why the toggles are inert for a user without Premium. */
    val remindersPremium: String,
    val vrathaNames: Map<String, String>
) {
    fun vratha(key: String): String = vrathaNames[key] ?: key

    /**
     * "until 02:56 AM (next day)" in English, "02:56 AM (next day) வரை" in
     * Tamil. Where the word sits is grammar, not layout, so it is decided here
     * next to the translations rather than by the screen doing the drawing.
     *
     * [time] is the whole time phrase including any "(next day)" — the word
     * governs the phrase, so it must not land between the two.
     */
    fun untilTime(time: String): String =
        if (untilPrecedesTime) "$until $time" else "$time $until"

    companion object {
        fun forLanguage(lang: Language): PanchangamStrings = when (lang) {
            Language.TA -> TA
            Language.ZH -> ZH
            Language.HI -> HI
            Language.TE -> TE
            Language.KN -> KN
            Language.ML -> ML
            Language.MR -> MR
            else -> EN
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
            untilPrecedesTime = true,
            then = "then",
            nextDay = "(next day)",
            noSunToday = "No sunrise/sunset at this location today.",
            swipeHint = "Swipe up for today's panchangam",
            vrathaTitle = "Vratham & special days",
            reminderChannelName = "Vratham reminders",
            reminderTitle = "Today's observance",
            remindersPremium = "Reminders are part of Halo Premium.",
            vrathaNames = mapOf(
                "amavasai" to "Amavasai (new moon)", "pournami" to "Pournami (full moon)",
                "ekadasi" to "Ekadasi", "sashti" to "Sashti", "chaturthi" to "Chaturthi",
                "sankatahara" to "Sankatahara Chaturthi", "pradosham" to "Pradosham",
                "sivarathiri" to "Sivarathiri", "krithigai" to "Krithigai", "thiruvonam" to "Thiruvonam"
            )
        )

        private val HI = PanchangamStrings(
            title = "पंचांग",
            calendarTitle = "कैलेंडर",
            calendarLabel = "कैलेंडर",
            monthCalendar = "मासिक कैलेंडर",
            today = "आज",
            location = "स्थान",
            sunrise = "सूर्योदय",
            sunset = "सूर्यास्त",
            auspicious = "शुभ समय",
            inauspicious = "अशुभ समय",
            abhijit = "अभिजित मुहूर्त",
            brahmaMuhurta = "ब्रह्म मुहूर्त",
            rahu = "राहु काल",
            yamagandam = "यमगंड",
            gulikai = "गुलिक काल",
            tithi = "तिथि",
            nakshatra = "नक्षत्र",
            yoga = "योग",
            karana = "करण",
            paksha = "पक्ष",
            rasiPalan = "आज का राशिफल",
            until = "तक",
            then = "फिर",
            nextDay = "(अगले दिन)",
            noSunToday = "इस स्थान पर आज सूर्योदय/सूर्यास्त नहीं है।",
            swipeHint = "आज के पंचांग के लिए ऊपर स्वाइप करें",
            vrathaTitle = "व्रत और विशेष दिन",
            reminderChannelName = "व्रत अनुस्मारक",
            reminderTitle = "आज का व्रत",
            remindersPremium = "अनुस्मारक हेलो प्रीमियम का हिस्सा हैं।",
            vrathaNames = mapOf(
                "amavasai" to "अमावस्या", "pournami" to "पूर्णिमा",
                "ekadasi" to "एकादशी", "sashti" to "षष्ठी", "chaturthi" to "चतुर्थी",
                "sankatahara" to "संकष्टी चतुर्थी", "pradosham" to "प्रदोष",
                "sivarathiri" to "शिवरात्रि", "krithigai" to "कृत्तिका", "thiruvonam" to "तिरुवोणम"
            )
        )

        private val TE = PanchangamStrings(
            title = "పంచాంగం",
            calendarTitle = "క్యాలెండర్",
            calendarLabel = "క్యాలెండర్",
            monthCalendar = "నెలవారీ క్యాలెండర్",
            today = "నేడు",
            location = "స్థలం",
            sunrise = "సూర్యోదయం",
            sunset = "సూర్యాస్తమయం",
            auspicious = "శుభ సమయాలు",
            inauspicious = "అశుభ సమయాలు",
            abhijit = "అభిజిత్ ముహూర్తం",
            brahmaMuhurta = "బ్రహ్మ ముహూర్తం",
            rahu = "రాహు కాలం",
            yamagandam = "యమగండం",
            gulikai = "గుళిక కాలం",
            tithi = "తిథి",
            nakshatra = "నక్షత్రం",
            yoga = "యోగం",
            karana = "కరణం",
            paksha = "పక్షం",
            rasiPalan = "నేటి రాశిఫలం",
            until = "వరకు",
            then = "తరువాత",
            nextDay = "(మరుసటి రోజు)",
            noSunToday = "ఈ స్థలంలో నేడు సూర్యోదయం/సూర్యాస్తమయం లేదు.",
            swipeHint = "నేటి పంచాంగం కోసం పైకి స్వైప్ చేయండి",
            vrathaTitle = "వ్రతం & విశేష దినాలు",
            reminderChannelName = "వ్రత రిమైండర్లు",
            reminderTitle = "నేటి వ్రతం",
            remindersPremium = "రిమైండర్లు హాలో ప్రీమియంలో భాగం.",
            vrathaNames = mapOf(
                "amavasai" to "అమావాస్య", "pournami" to "పౌర్ణమి",
                "ekadasi" to "ఏకాదశి", "sashti" to "షష్ఠి", "chaturthi" to "చతుర్థి",
                "sankatahara" to "సంకటహర చతుర్థి", "pradosham" to "ప్రదోషం",
                "sivarathiri" to "శివరాత్రి", "krithigai" to "కృత్తిక", "thiruvonam" to "తిరువోణం"
            )
        )

        private val KN = PanchangamStrings(
            title = "ಪಂಚಾಂಗ",
            calendarTitle = "ಕ್ಯಾಲೆಂಡರ್",
            calendarLabel = "ಕ್ಯಾಲೆಂಡರ್",
            monthCalendar = "ಮಾಸಿಕ ಕ್ಯಾಲೆಂಡರ್",
            today = "ಇಂದು",
            location = "ಸ್ಥಳ",
            sunrise = "ಸೂರ್ಯೋದಯ",
            sunset = "ಸೂರ್ಯಾಸ್ತ",
            auspicious = "ಶುಭ ಸಮಯಗಳು",
            inauspicious = "ಅಶುಭ ಸಮಯಗಳು",
            abhijit = "ಅಭಿಜಿತ್ ಮುಹೂರ್ತ",
            brahmaMuhurta = "ಬ್ರಹ್ಮ ಮುಹೂರ್ತ",
            rahu = "ರಾಹು ಕಾಲ",
            yamagandam = "ಯಮಗಂಡ",
            gulikai = "ಗುಳಿಕ",
            tithi = "ತಿಥಿ",
            nakshatra = "ನಕ್ಷತ್ರ",
            yoga = "ಯೋಗ",
            karana = "ಕರಣ",
            paksha = "ಪಕ್ಷ",
            rasiPalan = "ಇಂದಿನ ರಾಶಿಫಲ",
            until = "ವರೆಗೆ",
            then = "ನಂತರ",
            nextDay = "(ಮರುದಿನ)",
            noSunToday = "ಈ ಸ್ಥಳದಲ್ಲಿ ಇಂದು ಸೂರ್ಯೋದಯ/ಸೂರ್ಯಾಸ್ತ ಇಲ್ಲ.",
            swipeHint = "ಇಂದಿನ ಪಂಚಾಂಗಕ್ಕಾಗಿ ಮೇಲಕ್ಕೆ ಸ್ವೈಪ್ ಮಾಡಿ",
            vrathaTitle = "ವ್ರತ & ವಿಶೇಷ ದಿನಗಳು",
            reminderChannelName = "ವ್ರತ ಜ್ಞಾಪನೆಗಳು",
            reminderTitle = "ಇಂದಿನ ವ್ರತ",
            remindersPremium = "ಜ್ಞಾಪನೆಗಳು ಹ್ಯಾಲೋ ಪ್ರೀಮಿಯಂನ ಭಾಗ.",
            vrathaNames = mapOf(
                "amavasai" to "ಅಮಾವಾಸ್ಯೆ", "pournami" to "ಪೂರ್ಣಿಮೆ",
                "ekadasi" to "ಏಕಾದಶಿ", "sashti" to "ಷಷ್ಠಿ", "chaturthi" to "ಚತುರ್ಥಿ",
                "sankatahara" to "ಸಂಕಷ್ಟಹರ ಚತುರ್ಥಿ", "pradosham" to "ಪ್ರದೋಷ",
                "sivarathiri" to "ಶಿವರಾತ್ರಿ", "krithigai" to "ಕೃತ್ತಿಕಾ", "thiruvonam" to "ತಿರುವೋಣಂ"
            )
        )

        private val ML = PanchangamStrings(
            title = "പഞ്ചാംഗം",
            calendarTitle = "കലണ്ടർ",
            calendarLabel = "കലണ്ടർ",
            monthCalendar = "മാസ കലണ്ടർ",
            today = "ഇന്ന്",
            location = "സ്ഥലം",
            sunrise = "സൂര്യോദയം",
            sunset = "സൂര്യാസ്തമയം",
            auspicious = "ശുഭ സമയങ്ങൾ",
            inauspicious = "അശുഭ സമയങ്ങൾ",
            abhijit = "അഭിജിത് മുഹൂർത്തം",
            brahmaMuhurta = "ബ്രഹ്മ മുഹൂർത്തം",
            rahu = "രാഹു കാലം",
            yamagandam = "യമഗണ്ഡം",
            gulikai = "ഗുളികൻ",
            tithi = "തിഥി",
            nakshatra = "നക്ഷത്രം",
            yoga = "യോഗം",
            karana = "കരണം",
            paksha = "പക്ഷം",
            rasiPalan = "ഇന്നത്തെ രാശിഫലം",
            until = "വരെ",
            then = "പിന്നെ",
            nextDay = "(പിറ്റേന്ന്)",
            noSunToday = "ഈ സ്ഥലത്ത് ഇന്ന് സൂര്യോദയം/സൂര്യാസ്തമയം ഇല്ല.",
            swipeHint = "ഇന്നത്തെ പഞ്ചാംഗത്തിന് മുകളിലേക്ക് സ്വൈപ്പ് ചെയ്യുക",
            vrathaTitle = "വ്രതം & വിശേഷ ദിനങ്ങൾ",
            reminderChannelName = "വ്രത ഓർമ്മപ്പെടുത്തലുകൾ",
            reminderTitle = "ഇന്നത്തെ വ്രതം",
            remindersPremium = "ഓർമ്മപ്പെടുത്തലുകൾ ഹാലോ പ്രീമിയത്തിന്റെ ഭാഗമാണ്.",
            vrathaNames = mapOf(
                "amavasai" to "അമാവാസി", "pournami" to "പൗർണമി",
                "ekadasi" to "ഏകാദശി", "sashti" to "ഷഷ്ഠി", "chaturthi" to "ചതുർത്ഥി",
                "sankatahara" to "സങ്കടഹര ചതുർത്ഥി", "pradosham" to "പ്രദോഷം",
                "sivarathiri" to "ശിവരാത്രി", "krithigai" to "കാർത്തിക", "thiruvonam" to "തിരുവോണം"
            )
        )

        private val MR = PanchangamStrings(
            title = "पंचांग",
            calendarTitle = "दिनदर्शिका",
            calendarLabel = "दिनदर्शिका",
            monthCalendar = "मासिक दिनदर्शिका",
            today = "आज",
            location = "स्थान",
            sunrise = "सूर्योदय",
            sunset = "सूर्यास्त",
            auspicious = "शुभ वेळा",
            inauspicious = "अशुभ वेळा",
            abhijit = "अभिजित मुहूर्त",
            brahmaMuhurta = "ब्रह्म मुहूर्त",
            rahu = "राहू काळ",
            yamagandam = "यमगंड",
            gulikai = "गुळिक काळ",
            tithi = "तिथी",
            nakshatra = "नक्षत्र",
            yoga = "योग",
            karana = "करण",
            paksha = "पक्ष",
            rasiPalan = "आजचे राशीफल",
            until = "पर्यंत",
            then = "नंतर",
            nextDay = "(दुसऱ्या दिवशी)",
            noSunToday = "या स्थानी आज सूर्योदय/सूर्यास्त नाही.",
            swipeHint = "आजच्या पंचांगासाठी वर स्वाइप करा",
            vrathaTitle = "व्रत आणि विशेष दिवस",
            reminderChannelName = "व्रत स्मरणपत्रे",
            reminderTitle = "आजचे व्रत",
            remindersPremium = "स्मरणपत्रे हॅलो प्रीमियमचा भाग आहेत.",
            vrathaNames = mapOf(
                "amavasai" to "अमावस्या", "pournami" to "पौर्णिमा",
                "ekadasi" to "एकादशी", "sashti" to "षष्ठी", "chaturthi" to "चतुर्थी",
                "sankatahara" to "संकष्टहर चतुर्थी", "pradosham" to "प्रदोष",
                "sivarathiri" to "शिवरात्री", "krithigai" to "कृत्तिका", "thiruvonam" to "तिरुवोणम"
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
            reminderChannelName = "விரத நினைவூட்டல்கள்",
            reminderTitle = "இன்றைய விரதம்",
            remindersPremium = "நினைவூட்டல்கள் ஹேலோ பிரீமியத்தின் ஒரு பகுதி.",
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
            untilPrecedesTime = true,
            then = "之后",
            nextDay = "（次日）",
            noSunToday = "该地点今日无日出/日落。",
            swipeHint = "上滑查看今日黄历",
            vrathaTitle = "斋戒与特殊日",
            reminderChannelName = "斋戒日提醒",
            reminderTitle = "今日斋戒",
            remindersPremium = "提醒功能属于 Halo 高级版。",
            vrathaNames = mapOf(
                "amavasai" to "新月 (Amavasai)", "pournami" to "满月 (Pournami)",
                "ekadasi" to "Ekadasi", "sashti" to "Sashti", "chaturthi" to "Chaturthi",
                "sankatahara" to "Sankatahara Chaturthi", "pradosham" to "Pradosham",
                "sivarathiri" to "Sivarathiri", "krithigai" to "Krithigai", "thiruvonam" to "Thiruvonam"
            )
        )
    }
}
