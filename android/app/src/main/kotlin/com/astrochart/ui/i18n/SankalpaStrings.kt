package com.astrochart.ui.i18n

import com.astrochart.core.i18n.Language

/**
 * Localized labels for sankalpa — the practice of stating an intention, and the
 * traditional framing that locates it in time.
 *
 * Naming follows the user's decision to use **plain transliteration** rather
 * than IAST diacritics: "sankalpa", "ritu", "samvatsara" — never "saṅkalpa",
 * "ṛtu", "saṃvatsara". In the Indic scripts the terms are written natively,
 * because they are ordinary words in those languages rather than loanwords; the
 * transliteration question only ever applied to the Latin-script locales.
 */
data class SankalpaStrings(
    val title: String,
    val entry: String,
    val navLabel: String,
    /** Heading over the "where you are in time" block. */
    val framingHeading: String,
    val samvatsara: String,
    val ayana: String,
    val ritu: String,
    val masa: String,
    val paksha: String,
    val tithi: String,
    val vaara: String,
    val place: String,
    val empty: String,
    val emptyHint: String,
    val newSankalpa: String,
    val intentionLabel: String,
    val intentionHint: String,
    val save: String,
    val delete: String,
    /** "%d times" — how often this sankalpa has been renewed. Counted, never streaked. */
    val renewedCount: String,
    val renewToday: String,
    val renewedToday: String
) {
    companion object {
        fun forLanguage(lang: Language): SankalpaStrings = when (lang) {
            Language.TA -> TA
            Language.ZH -> ZH
            Language.HI -> HI
            Language.TE -> TE
            Language.KN -> KN
            Language.ML -> ML
            Language.MR -> MR
            else -> EN
        }

        private val EN = SankalpaStrings(
            title = "Sankalpa",
            entry = "Sankalpa (Intention)",
            navLabel = "Sankalpa",
            framingHeading = "Here and now",
            samvatsara = "Year",
            ayana = "Ayana",
            ritu = "Season",
            masa = "Month",
            paksha = "Fortnight",
            tithi = "Tithi",
            vaara = "Weekday",
            place = "Place",
            empty = "No sankalpa yet",
            emptyHint = "A sankalpa is an intention you state, in the present tense, " +
                "and return to. Keep it short and keep it yours.",
            newSankalpa = "New sankalpa",
            intentionLabel = "Your intention",
            intentionHint = "I am…",
            save = "Save",
            delete = "Delete",
            renewedCount = "Renewed %d times",
            renewToday = "Renew today",
            renewedToday = "Renewed today"
        )

        private val HI = SankalpaStrings(
            title = "संकल्प",
            entry = "संकल्प",
            navLabel = "संकल्प",
            framingHeading = "यहाँ और अभी",
            samvatsara = "संवत्सर",
            ayana = "अयन",
            ritu = "ऋतु",
            masa = "मास",
            paksha = "पक्ष",
            tithi = "तिथि",
            vaara = "वार",
            place = "स्थान",
            empty = "अभी कोई संकल्प नहीं",
            emptyHint = "संकल्प वह इच्छा है जिसे आप वर्तमान काल में कहते हैं और " +
                "बार-बार दोहराते हैं। इसे छोटा रखें और अपना रखें।",
            newSankalpa = "नया संकल्प",
            intentionLabel = "आपका संकल्प",
            intentionHint = "मैं…",
            save = "सहेजें",
            delete = "हटाएँ",
            renewedCount = "%d बार दोहराया",
            renewToday = "आज दोहराएँ",
            renewedToday = "आज दोहराया गया"
        )

        private val TE = SankalpaStrings(
            title = "సంకల్పం",
            entry = "సంకల్పం",
            navLabel = "సంకల్పం",
            framingHeading = "ఇక్కడ, ఇప్పుడు",
            samvatsara = "సంవత్సరం",
            ayana = "అయనం",
            ritu = "ఋతువు",
            masa = "మాసం",
            paksha = "పక్షం",
            tithi = "తిథి",
            vaara = "వారం",
            place = "స్థలం",
            empty = "ఇంకా సంకల్పం లేదు",
            emptyHint = "సంకల్పం అంటే మీరు వర్తమాన కాలంలో చెప్పి, మళ్ళీ మళ్ళీ " +
                "తిరిగి వచ్చే ఉద్దేశం. దానిని చిన్నగా, మీ స్వంతంగా ఉంచండి.",
            newSankalpa = "కొత్త సంకల్పం",
            intentionLabel = "మీ సంకల్పం",
            intentionHint = "నేను…",
            save = "భద్రపరచు",
            delete = "తొలగించు",
            renewedCount = "%d సార్లు పునరుద్ధరించారు",
            renewToday = "ఈరోజు పునరుద్ధరించు",
            renewedToday = "ఈరోజు పునరుద్ధరించారు"
        )

        private val KN = SankalpaStrings(
            title = "ಸಂಕಲ್ಪ",
            entry = "ಸಂಕಲ್ಪ",
            navLabel = "ಸಂಕಲ್ಪ",
            framingHeading = "ಇಲ್ಲಿ, ಈಗ",
            samvatsara = "ಸಂವತ್ಸರ",
            ayana = "ಅಯನ",
            ritu = "ಋತು",
            masa = "ಮಾಸ",
            paksha = "ಪಕ್ಷ",
            tithi = "ತಿಥಿ",
            vaara = "ವಾರ",
            place = "ಸ್ಥಳ",
            empty = "ಇನ್ನೂ ಸಂಕಲ್ಪವಿಲ್ಲ",
            emptyHint = "ಸಂಕಲ್ಪವೆಂದರೆ ನೀವು ವರ್ತಮಾನ ಕಾಲದಲ್ಲಿ ಹೇಳಿ, ಮತ್ತೆ ಮತ್ತೆ " +
                "ಮರಳುವ ಉದ್ದೇಶ. ಅದನ್ನು ಚಿಕ್ಕದಾಗಿ, ನಿಮ್ಮದಾಗಿ ಇರಿಸಿ.",
            newSankalpa = "ಹೊಸ ಸಂಕಲ್ಪ",
            intentionLabel = "ನಿಮ್ಮ ಸಂಕಲ್ಪ",
            intentionHint = "ನಾನು…",
            save = "ಉಳಿಸಿ",
            delete = "ಅಳಿಸಿ",
            renewedCount = "%d ಬಾರಿ ನವೀಕರಿಸಲಾಗಿದೆ",
            renewToday = "ಇಂದು ನವೀಕರಿಸಿ",
            renewedToday = "ಇಂದು ನವೀಕರಿಸಲಾಗಿದೆ"
        )

        private val ML = SankalpaStrings(
            title = "സങ്കൽപം",
            entry = "സങ്കൽപം",
            navLabel = "സങ്കൽപം",
            framingHeading = "ഇവിടെ, ഇപ്പോൾ",
            samvatsara = "സംവത്സരം",
            ayana = "അയനം",
            ritu = "ഋതു",
            masa = "മാസം",
            paksha = "പക്ഷം",
            tithi = "തിഥി",
            vaara = "വാരം",
            place = "സ്ഥലം",
            empty = "ഇതുവരെ സങ്കൽപമില്ല",
            emptyHint = "വർത്തമാനകാലത്തിൽ പറഞ്ഞ്, വീണ്ടും വീണ്ടും മടങ്ങിവരുന്ന " +
                "ഉദ്ദേശ്യമാണ് സങ്കൽപം. അത് ചെറുതായി, നിങ്ങളുടേതായി സൂക്ഷിക്കുക.",
            newSankalpa = "പുതിയ സങ്കൽപം",
            intentionLabel = "നിങ്ങളുടെ സങ്കൽപം",
            intentionHint = "ഞാൻ…",
            save = "സംരക്ഷിക്കുക",
            delete = "ഇല്ലാതാക്കുക",
            renewedCount = "%d തവണ പുതുക്കി",
            renewToday = "ഇന്ന് പുതുക്കുക",
            renewedToday = "ഇന്ന് പുതുക്കി"
        )

        private val MR = SankalpaStrings(
            title = "संकल्प",
            entry = "संकल्प",
            navLabel = "संकल्प",
            framingHeading = "इथे आणि आता",
            samvatsara = "संवत्सर",
            ayana = "अयन",
            ritu = "ऋतू",
            masa = "मास",
            paksha = "पक्ष",
            tithi = "तिथी",
            vaara = "वार",
            place = "स्थळ",
            empty = "अद्याप संकल्प नाही",
            emptyHint = "संकल्प म्हणजे वर्तमानकाळात सांगितलेला आणि पुन्हा पुन्हा " +
                "परत येणारा हेतू. तो छोटा ठेवा आणि आपलाच ठेवा.",
            newSankalpa = "नवीन संकल्प",
            intentionLabel = "तुमचा संकल्प",
            intentionHint = "मी…",
            save = "जतन करा",
            delete = "हटवा",
            renewedCount = "%d वेळा नूतनीकरण केले",
            renewToday = "आज नूतनीकरण करा",
            renewedToday = "आज नूतनीकरण केले"
        )

        private val TA = SankalpaStrings(
            title = "சங்கல்பம்",
            entry = "சங்கல்பம்",
            navLabel = "சங்கல்பம்",
            framingHeading = "இங்கே, இப்போது",
            samvatsara = "வருடம்",
            ayana = "அயனம்",
            ritu = "பருவம்",
            masa = "மாதம்",
            paksha = "பட்சம்",
            tithi = "திதி",
            vaara = "கிழமை",
            place = "இடம்",
            empty = "இதுவரை சங்கல்பம் இல்லை",
            emptyHint = "சங்கல்பம் என்பது நிகழ்காலத்தில் சொல்லி, திரும்பத் திரும்ப " +
                "வந்து சேரும் எண்ணம். அதைச் சுருக்கமாகவும் உங்களுடையதாகவும் வைத்திருங்கள்.",
            newSankalpa = "புதிய சங்கல்பம்",
            intentionLabel = "உங்கள் சங்கல்பம்",
            intentionHint = "நான்…",
            save = "சேமி",
            delete = "நீக்கு",
            renewedCount = "%d முறை புதுப்பிக்கப்பட்டது",
            renewToday = "இன்று புதுப்பி",
            renewedToday = "இன்று புதுப்பிக்கப்பட்டது"
        )

        private val ZH = SankalpaStrings(
            title = "发愿",
            entry = "发愿（心念）",
            navLabel = "发愿",
            framingHeading = "此时此地",
            samvatsara = "年名",
            ayana = "日行",
            ritu = "季节",
            masa = "月份",
            paksha = "半月",
            tithi = "太阴日",
            vaara = "星期",
            place = "地点",
            empty = "尚无心愿",
            emptyHint = "发愿是以现在式说出、并不断回到的一个心念。请写得简短，写得属于你自己。",
            newSankalpa = "新的心愿",
            intentionLabel = "你的心愿",
            intentionHint = "我…",
            save = "保存",
            delete = "删除",
            renewedCount = "已重申 %d 次",
            renewToday = "今日重申",
            renewedToday = "今日已重申"
        )
    }
}
