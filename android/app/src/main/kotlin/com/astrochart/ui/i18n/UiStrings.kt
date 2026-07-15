package com.astrochart.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import com.astrochart.core.i18n.Language

/**
 * All user-facing UI literals for one [Language]. Static strings are `val`s;
 * the few parameterized ones are functions. Provided through [LocalStrings] so
 * every screen reads `LocalStrings.current`, and the whole UI re-renders when
 * the language changes — no Activity recreation.
 */
data class UiStrings(
    val lang: Language,
    // App bar / navigation
    val appName: String,
    val navCalculate: String,
    val navSavedChartsTitle: String,
    val chartTitle: String,
    val back: String,
    val languageLabel: String,
    // Home
    val homeEyebrow: String,
    val homeTitleLine1: String,
    val homeTitleLine2: String,
    val homeSubtitle: String,
    val homeViewSaved: String,
    // Birth input
    val biEyebrow: String,
    val biHeading: String,
    val name: String,
    val nameRequired: String,
    val gender: String,
    val genderFemale: String,
    val genderMale: String,
    val genderOther: String,
    val genderUnset: String,
    val dob: String,
    val tob: String,
    val pob: String,
    val day: String,
    val month: String,
    val year: String,
    val hour: String,
    val minute: String,
    val location: String,
    val selectCity: String,
    val timeZone: String,
    val dropdownDefault: String,
    // Chart detail
    val tabWheel: String,
    val tabPlacements: String,
    val tabAspects: String,
    val tabBalance: String,
    val tabReading: String,
    val labelSun: String,
    val labelMoon: String,
    val labelRising: String,
    val labelAge: String,
    val labelGender: String,
    val labelChineseZodiac: String,
    val angles: String,
    val planets: String,
    val orb: String,
    val noAspects: String,
    val elements: String,
    val modalities: String,
    val wheelLegendAxis: String,
    val wheelLegendSoft: String,
    val wheelLegendHard: String,
    // Saved charts
    val noSaved: String,
    val untitled: String,
    val rename: String,
    val delete: String,
    val deleteTitle: String,
    val cancel: String,
    val renameTitle: String,
    val save: String
) {
    fun ageValue(years: Int): String = when (lang) {
        Language.EN -> "$years yrs"
        Language.TA -> "$years வயது"
        Language.ZH -> "${years}岁"
    }

    fun houseLabel(house: Int): String = when (lang) {
        Language.EN -> "House $house"
        Language.TA -> "$house ஆம் வீடு"
        Language.ZH -> "第${house}宫"
    }

    fun bodyCount(count: Int): String = when (lang) {
        Language.EN -> if (count == 1) "1 body" else "$count bodies"
        Language.TA -> "$count கிரகம்"
        Language.ZH -> "${count}颗"
    }

    fun deleteMessage(name: String): String = when (lang) {
        Language.EN -> "\"$name\" will be permanently removed."
        Language.TA -> "\"$name\" நிரந்தரமாக நீக்கப்படும்."
        Language.ZH -> "将永久删除“$name”。"
    }

    companion object {
        fun forLanguage(lang: Language): UiStrings = when (lang) {
            Language.EN -> EN
            Language.TA -> TA
            Language.ZH -> ZH
        }

        private val EN = UiStrings(
            lang = Language.EN,
            appName = "AstroChart",
            navCalculate = "Calculate My Chart",
            navSavedChartsTitle = "Saved Charts",
            chartTitle = "Chart",
            back = "Back",
            languageLabel = "Language",
            homeEyebrow = "Your cosmic guide",
            homeTitleLine1 = "Explore your",
            homeTitleLine2 = "natal chart",
            homeSubtitle = "Enter your birth details and see your placements, aspects, and elemental balance — computed on your device.",
            homeViewSaved = "View Saved Charts",
            biEyebrow = "New chart",
            biHeading = "Enter the birth details",
            name = "Name",
            nameRequired = "Required — the chart is saved under this name",
            gender = "Gender",
            genderFemale = "Female",
            genderMale = "Male",
            genderOther = "Other",
            genderUnset = "Prefer not to say",
            dob = "Date of birth",
            tob = "Time of birth (24-hour)",
            pob = "Place of birth",
            day = "Day",
            month = "Month",
            year = "Year",
            hour = "Hour",
            minute = "Minute",
            location = "Location",
            selectCity = "Select a city",
            timeZone = "Time zone",
            dropdownDefault = "Select…",
            tabWheel = "Wheel",
            tabPlacements = "Placements",
            tabAspects = "Aspects",
            tabBalance = "Balance",
            tabReading = "Reading",
            labelSun = "Sun",
            labelMoon = "Moon",
            labelRising = "Rising",
            labelAge = "Age",
            labelGender = "Gender",
            labelChineseZodiac = "Chinese zodiac",
            angles = "Angles",
            planets = "Planets",
            orb = "orb",
            noAspects = "No major aspects within orb for this chart.",
            elements = "Elements",
            modalities = "Modalities",
            wheelLegendAxis = "Ascendant is at the 9 o'clock position; the zodiac runs anticlockwise.",
            wheelLegendSoft = "— harmonious (trine/sextile)",
            wheelLegendHard = "— challenging (square/opp.)",
            noSaved = "No saved charts yet. Calculate a chart and it will be saved here automatically.",
            untitled = "Untitled chart",
            rename = "Rename",
            delete = "Delete",
            deleteTitle = "Delete chart?",
            cancel = "Cancel",
            renameTitle = "Rename chart",
            save = "Save"
        )

        private val TA = UiStrings(
            lang = Language.TA,
            appName = "AstroChart",
            navCalculate = "எனது ஜாதகத்தைக் கணக்கிடு",
            navSavedChartsTitle = "சேமித்த ஜாதகங்கள்",
            chartTitle = "ஜாதகம்",
            back = "பின்செல்",
            languageLabel = "மொழி",
            homeEyebrow = "உங்கள் விண்வெளி வழிகாட்டி",
            homeTitleLine1 = "உங்கள்",
            homeTitleLine2 = "ஜாதகத்தை ஆராயுங்கள்",
            homeSubtitle = "உங்கள் பிறப்பு விவரங்களை உள்ளிட்டு உங்கள் கிரக நிலைகள், கோணங்கள், பஞ்சபூத சமநிலையை — உங்கள் சாதனத்திலேயே கணக்கிட்டு — பாருங்கள்.",
            homeViewSaved = "சேமித்த ஜாதகங்களைப் பார்",
            biEyebrow = "புதிய ஜாதகம்",
            biHeading = "பிறப்பு விவரங்களை உள்ளிடுங்கள்",
            name = "பெயர்",
            nameRequired = "தேவை — இந்தப் பெயரில் ஜாதகம் சேமிக்கப்படும்",
            gender = "பாலினம்",
            genderFemale = "பெண்",
            genderMale = "ஆண்",
            genderOther = "மற்றவை",
            genderUnset = "கூற விரும்பவில்லை",
            dob = "பிறந்த தேதி",
            tob = "பிறந்த நேரம் (24 மணி)",
            pob = "பிறந்த இடம்",
            day = "நாள்",
            month = "மாதம்",
            year = "ஆண்டு",
            hour = "மணி",
            minute = "நிமிடம்",
            location = "இடம்",
            selectCity = "ஒரு நகரத்தைத் தேர்ந்தெடு",
            timeZone = "நேர மண்டலம்",
            dropdownDefault = "தேர்ந்தெடு…",
            tabWheel = "சக்கரம்",
            tabPlacements = "நிலைகள்",
            tabAspects = "கோணங்கள்",
            tabBalance = "சமநிலை",
            tabReading = "விளக்கம்",
            labelSun = "சூரியன்",
            labelMoon = "சந்திரன்",
            labelRising = "லக்னம்",
            labelAge = "வயது",
            labelGender = "பாலினம்",
            labelChineseZodiac = "சீன ராசி",
            angles = "கோணப் புள்ளிகள்",
            planets = "கிரகங்கள்",
            orb = "இடைவெளி",
            noAspects = "இந்த ஜாதகத்திற்கு இடைவெளிக்குள் பெரிய கோணங்கள் இல்லை.",
            elements = "பஞ்சபூதங்கள்",
            modalities = "குணங்கள்",
            wheelLegendAxis = "லக்னம் 9 மணி நிலையில் உள்ளது; ராசிகள் இடஞ்சுழியாக இயங்குகின்றன.",
            wheelLegendSoft = "— இணக்கமான (திரிகோணம்/அறுகோணம்)",
            wheelLegendHard = "— சவாலான (சதுரம்/எதிர்நிலை)",
            noSaved = "இதுவரை சேமித்த ஜாதகங்கள் இல்லை. ஒரு ஜாதகத்தைக் கணக்கிட்டால் அது தானாக இங்கே சேமிக்கப்படும்.",
            untitled = "பெயரிடப்படாத ஜாதகம்",
            rename = "மறுபெயரிடு",
            delete = "நீக்கு",
            deleteTitle = "ஜாதகத்தை நீக்கவா?",
            cancel = "ரத்து",
            renameTitle = "ஜாதகத்தை மறுபெயரிடு",
            save = "சேமி"
        )

        private val ZH = UiStrings(
            lang = Language.ZH,
            appName = "AstroChart",
            navCalculate = "计算我的星盘",
            navSavedChartsTitle = "已保存星盘",
            chartTitle = "星盘",
            back = "返回",
            languageLabel = "语言",
            homeEyebrow = "你的星象向导",
            homeTitleLine1 = "探索你的",
            homeTitleLine2 = "本命星盘",
            homeSubtitle = "输入你的出生信息，查看行星落点、相位与元素平衡——全部在你的设备上计算。",
            homeViewSaved = "查看已保存星盘",
            biEyebrow = "新星盘",
            biHeading = "输入出生信息",
            name = "姓名",
            nameRequired = "必填——星盘将以此姓名保存",
            gender = "性别",
            genderFemale = "女",
            genderMale = "男",
            genderOther = "其他",
            genderUnset = "不愿透露",
            dob = "出生日期",
            tob = "出生时间（24小时制）",
            pob = "出生地点",
            day = "日",
            month = "月",
            year = "年",
            hour = "时",
            minute = "分",
            location = "地点",
            selectCity = "选择城市",
            timeZone = "时区",
            dropdownDefault = "选择…",
            tabWheel = "星盘图",
            tabPlacements = "落点",
            tabAspects = "相位",
            tabBalance = "平衡",
            tabReading = "解读",
            labelSun = "太阳",
            labelMoon = "月亮",
            labelRising = "上升",
            labelAge = "年龄",
            labelGender = "性别",
            labelChineseZodiac = "生肖",
            angles = "角点",
            planets = "行星",
            orb = "容许度",
            noAspects = "此星盘没有落在容许度内的主要相位。",
            elements = "元素",
            modalities = "模式",
            wheelLegendAxis = "上升位于9点方向；黄道逆时针排列。",
            wheelLegendSoft = "— 和谐（三分/六分）",
            wheelLegendHard = "— 挑战（刑/冲）",
            noSaved = "尚无已保存的星盘。计算一个星盘后会自动保存在此。",
            untitled = "未命名星盘",
            rename = "重命名",
            delete = "删除",
            deleteTitle = "删除星盘？",
            cancel = "取消",
            renameTitle = "重命名星盘",
            save = "保存"
        )
    }
}

/** Current UI strings; defaults to English until [MainActivity] provides the choice. */
val LocalStrings = staticCompositionLocalOf { UiStrings.forLanguage(Language.EN) }

/** Current language, for date/number formatting and content lookups in screens. */
val LocalLanguage = staticCompositionLocalOf { Language.EN }
