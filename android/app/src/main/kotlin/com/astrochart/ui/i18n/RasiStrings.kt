package com.astrochart.ui.i18n

import com.astrochart.core.i18n.Language

/** Localized labels for the Rasi Palan hub and its detail screens. */
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
        fun forLanguage(lang: Language): RasiStrings = when (lang) {
            Language.TA -> TA
            Language.ZH -> ZH
            Language.HI -> HI
            Language.TE -> TE
            Language.KN -> KN
            Language.ML -> ML
            Language.MR -> MR
            else -> EN
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

        private val HI = RasiStrings(
            title = "राशिफल",
            entry = "राशिफल",
            today = "आज",
            weekly = "साप्ताहिक",
            monthly = "मासिक",
            yearly = "वार्षिक",
            aboutSigns = "राशियों के बारे में",
            aboutNakshatras = "नक्षत्रों के बारे में",
            chooseSign = "अपनी राशि चुनें",
            rulingPlanet = "स्वामी ग्रह",
            friendlySigns = "मित्र राशियाँ",
            luckyColor = "शुभ रंग",
            luckyDay = "शुभ दिन",
            luckyNumber = "शुभ अंक",
            deity = "देवता",
            gemstone = "रत्न",
            character = "स्वभाव",
            nakshatraLord = "स्वामी"
        )

        private val TE = RasiStrings(
            title = "రాశిఫలం",
            entry = "రాశిఫలం",
            today = "నేడు",
            weekly = "వారం",
            monthly = "నెల",
            yearly = "సంవత్సరం",
            aboutSigns = "రాశుల గురించి",
            aboutNakshatras = "నక్షత్రాల గురించి",
            chooseSign = "మీ రాశిని ఎంచుకోండి",
            rulingPlanet = "అధిపతి గ్రహం",
            friendlySigns = "మిత్ర రాశులు",
            luckyColor = "అదృష్ట రంగు",
            luckyDay = "అదృష్ట రోజు",
            luckyNumber = "అదృష్ట సంఖ్య",
            deity = "దేవత",
            gemstone = "రత్నం",
            character = "స్వభావం",
            nakshatraLord = "అధిపతి"
        )

        private val KN = RasiStrings(
            title = "ರಾಶಿಫಲ",
            entry = "ರಾಶಿಫಲ",
            today = "ಇಂದು",
            weekly = "ವಾರ",
            monthly = "ಮಾಸ",
            yearly = "ವರ್ಷ",
            aboutSigns = "ರಾಶಿಗಳ ಬಗ್ಗೆ",
            aboutNakshatras = "ನಕ್ಷತ್ರಗಳ ಬಗ್ಗೆ",
            chooseSign = "ನಿಮ್ಮ ರಾಶಿ ಆಯ್ಕೆಮಾಡಿ",
            rulingPlanet = "ಅಧಿಪತಿ ಗ್ರಹ",
            friendlySigns = "ಮಿತ್ರ ರಾಶಿಗಳು",
            luckyColor = "ಅದೃಷ್ಟ ಬಣ್ಣ",
            luckyDay = "ಅದೃಷ್ಟ ದಿನ",
            luckyNumber = "ಅದೃಷ್ಟ ಸಂಖ್ಯೆ",
            deity = "ದೇವತೆ",
            gemstone = "ರತ್ನ",
            character = "ಸ್ವಭಾವ",
            nakshatraLord = "ಅಧಿಪತಿ"
        )

        private val ML = RasiStrings(
            title = "രാശിഫലം",
            entry = "രാശിഫലം",
            today = "ഇന്ന്",
            weekly = "ആഴ്ച",
            monthly = "മാസം",
            yearly = "വർഷം",
            aboutSigns = "രാശികളെക്കുറിച്ച്",
            aboutNakshatras = "നക്ഷത്രങ്ങളെക്കുറിച്ച്",
            chooseSign = "നിങ്ങളുടെ രാശി തിരഞ്ഞെടുക്കുക",
            rulingPlanet = "അധിപതി ഗ്രഹം",
            friendlySigns = "മിത്ര രാശികൾ",
            luckyColor = "ഭാഗ്യ നിറം",
            luckyDay = "ഭാഗ്യ ദിവസം",
            luckyNumber = "ഭാഗ്യ സംഖ്യ",
            deity = "ദേവത",
            gemstone = "രത്നം",
            character = "സ്വഭാവം",
            nakshatraLord = "അധിപതി"
        )

        private val MR = RasiStrings(
            title = "राशीफल",
            entry = "राशीफल",
            today = "आज",
            weekly = "साप्ताहिक",
            monthly = "मासिक",
            yearly = "वार्षिक",
            aboutSigns = "राशींबद्दल",
            aboutNakshatras = "नक्षत्रांबद्दल",
            chooseSign = "तुमची राशी निवडा",
            rulingPlanet = "स्वामी ग्रह",
            friendlySigns = "मित्र राशी",
            luckyColor = "शुभ रंग",
            luckyDay = "शुभ दिवस",
            luckyNumber = "शुभ अंक",
            deity = "देवता",
            gemstone = "रत्न",
            character = "स्वभाव",
            nakshatraLord = "स्वामी"
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
