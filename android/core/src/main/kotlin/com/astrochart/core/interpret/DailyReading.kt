package com.astrochart.core.interpret

import com.astrochart.core.i18n.Language
import com.astrochart.core.utils.ZodiacUtils
import java.time.LocalDate
import kotlin.random.Random

/** A colour with a localized name and an ARGB value for a swatch. */
data class DailyColor(
    val nameEn: String,
    val nameTa: String,
    val nameZh: String,
    val hex: Long
) {
    fun name(lang: Language): String = when (lang) {
        Language.EN -> nameEn
        Language.TA -> nameTa
        Language.ZH -> nameZh
    }
}

/** A full day's reading. [summary] is the ≤200-char optimistic headline. */
data class DailyReadingData(
    val date: LocalDate,
    val summary: String,
    val goodToDo: String,
    val avoid: String,
    val focus: String,
    val luckyColorName: String,
    val luckyColorHex: Long,
    val avoidColorName: String,
    val avoidColorHex: Long
)

/**
 * Generates an optimistic daily reading that is **deterministic per calendar day**
 * (and lightly varied by the chart's Sun sign): the same date + language + sign
 * always yields the same reading, so it is stable across refreshes and can be
 * regenerated for a notification. Pure logic — unit-testable off-device. All text
 * is stored as plain literals (no string interpolation), so the CJK/Tamil
 * identifier hazard that affects [ChartReading] cannot occur here.
 */
object DailyReading {

    private val COLORS = listOf(
        DailyColor("Gold", "தங்கம்", "金色", 0xFFD9A94E),
        DailyColor("Blue", "நீலம்", "蓝色", 0xFF4C7BD9),
        DailyColor("Green", "பச்சை", "绿色", 0xFF4CAF7B),
        DailyColor("Red", "சிவப்பு", "红色", 0xFFE05555),
        DailyColor("White", "வெள்ளை", "白色", 0xFFF5F3EE),
        DailyColor("Purple", "ஊதா", "紫色", 0xFF8A6FD1),
        DailyColor("Orange", "ஆரஞ்சு", "橙色", 0xFFE8934E),
        DailyColor("Silver", "வெள்ளி", "银色", 0xFFC0C4CC),
        DailyColor("Teal", "நீலப்பச்சை", "青色", 0xFF3FB6A8),
        DailyColor("Pink", "இளஞ்சிவப்பு", "粉色", 0xFFE58FB0)
    )

    fun build(date: LocalDate, lang: Language, sign: String? = null): DailyReadingData {
        val signIndex = sign?.let { ZodiacUtils.getAllSigns().indexOf(it) }?.takeIf { it >= 0 } ?: 0
        val seed = date.toEpochDay() * 31L + signIndex
        val rnd = Random(seed)

        val summary = summaries(lang).random(rnd)
        val good = goodToDo(lang).random(rnd)
        val avoid = avoidList(lang).random(rnd)
        val focus = focusList(lang).random(rnd)

        val lucky = COLORS.random(rnd)
        var bad = COLORS.random(rnd)
        while (bad == lucky) bad = COLORS.random(rnd)

        return DailyReadingData(
            date = date,
            summary = summary,
            goodToDo = good,
            avoid = avoid,
            focus = focus,
            luckyColorName = lucky.name(lang),
            luckyColorHex = lucky.hex,
            avoidColorName = bad.name(lang),
            avoidColorHex = bad.hex
        )
    }

    private fun summaries(lang: Language): List<String> = when (lang) {
        Language.EN -> listOf(
            "A bright, open day — your energy flows easily and small efforts bring rewarding results. Stay warm and say yes to good opportunities.",
            "Momentum is on your side today. Trust your instincts, lead with kindness, and let your natural confidence shine.",
            "A gentle, lucky current runs through the day. Connections feel warm and your ideas land well — lean into optimism.",
            "Today rewards courage and clarity. Take the first step you have been putting off; the path opens as you move.",
            "Calm and creativity meet today. You will find graceful solutions and a little joy in the ordinary moments.",
            "A day for fresh starts. Your focus is sharp and your heart is light — plant a seed you will be glad you did.",
            "Good fortune favours your generosity today. Share warmth freely and watch it return to you multiplied.",
            "Steady progress and pleasant surprises await. Keep an open mind and let the day's easy rhythm carry you."
        )
        Language.TA -> listOf(
            "பிரகாசமான, திறந்த நாள் — உங்கள் ஆற்றல் எளிதாகப் பாய்கிறது, சிறு முயற்சிகள் நல்ல பலனைத் தரும். அன்பாக இருங்கள், நல்ல வாய்ப்புகளுக்கு ஆம் சொல்லுங்கள்.",
            "இன்று வேகம் உங்கள் பக்கம் உள்ளது. உங்கள் உள்ளுணர்வை நம்புங்கள், கருணையுடன் வழிநடத்துங்கள், உங்கள் தன்னம்பிக்கை ஒளிரட்டும்.",
            "நாள் முழுவதும் மென்மையான அதிர்ஷ்ட ஓட்டம். உறவுகள் அன்பாக இருக்கும், உங்கள் கருத்துகள் நன்கு பதியும் — நம்பிக்கையுடன் இருங்கள்.",
            "இன்று தைரியத்தையும் தெளிவையும் பரிசளிக்கிறது. தள்ளிப்போட்ட முதல் அடியை எடுங்கள்; நகர்ந்தால் பாதை திறக்கும்.",
            "இன்று அமைதியும் படைப்பாற்றலும் சந்திக்கின்றன. அழகான தீர்வுகளும் சிறு மகிழ்ச்சியும் கிடைக்கும்.",
            "புதிய தொடக்கங்களுக்கான நாள். உங்கள் கவனம் கூர்மையாகவும் மனம் இலகுவாகவும் உள்ளது — ஒரு விதையை விதையுங்கள்.",
            "இன்று உங்கள் தாராள மனது அதிர்ஷ்டத்தைத் தரும். அன்பைப் பகிருங்கள், அது பன்மடங்காகத் திரும்பும்.",
            "நிலையான முன்னேற்றமும் இனிய ஆச்சரியங்களும் காத்திருக்கின்றன. திறந்த மனதுடன் நாளின் இலகுவான தாளத்தை அனுபவியுங்கள்."
        )
        Language.ZH -> listOf(
            "明亮而开阔的一天——你的能量顺畅流动，小小的努力也能带来回报。保持温暖，对好机会说好。",
            "今天势头在你这边。相信直觉，以善意引领，让你天生的自信闪耀。",
            "整天都有一股温柔的幸运暖流。人际温暖，想法也能被认可——尽管乐观。",
            "今天奖励勇气与清晰。迈出你一直拖延的第一步；一动身，路就打开。",
            "今天平静与创意相遇。你会找到优雅的解决办法，并在平凡时刻收获小确幸。",
            "适合重新开始的一天。你的专注敏锐、心情轻盈——种下一颗你会庆幸的种子。",
            "今天你的慷慨会带来好运。慷慨地分享温暖，它会加倍回到你身边。",
            "稳步的进展与愉快的惊喜在等你。保持开放，随着这一天轻松的节奏前行。"
        )
    }

    private fun goodToDo(lang: Language): List<String> = when (lang) {
        Language.EN -> listOf(
            "Start something new", "Reach out to a friend", "Tidy your space",
            "Speak your ideas aloud", "Take a mindful walk", "Finish a lingering task",
            "Offer help freely", "Plan your week ahead"
        )
        Language.TA -> listOf(
            "புதியதைத் தொடங்குங்கள்", "நண்பரைத் தொடர்பு கொள்ளுங்கள்", "இடத்தை ஒழுங்குபடுத்துங்கள்",
            "உங்கள் கருத்துகளைப் பகிருங்கள்", "அமைதியாக நடந்து வாருங்கள்", "நிலுவையிலுள்ள வேலையை முடியுங்கள்",
            "மனமுவந்து உதவுங்கள்", "வாரத்தைத் திட்டமிடுங்கள்"
        )
        Language.ZH -> listOf(
            "开始新事物", "联系一位朋友", "整理你的空间",
            "说出你的想法", "正念散步", "完成拖延的任务",
            "主动帮助他人", "规划你的一周"
        )
    }

    private fun avoidList(lang: Language): List<String> = when (lang) {
        Language.EN -> listOf(
            "Rushing decisions", "Overthinking small things", "Skipping rest",
            "Harsh self-criticism", "Endless scrolling", "Saying yes when you mean no",
            "Ignoring your body's signals", "Comparing yourself to others"
        )
        Language.TA -> listOf(
            "அவசர முடிவுகள்", "சிறிய விஷயங்களில் அதிகம் யோசிப்பது", "ஓய்வைத் தவிர்ப்பது",
            "கடுமையான சுயவிமர்சனம்", "முடிவற்ற திரை உலா", "மனதில் இல்லாமல் ஆம் சொல்வது",
            "உடலின் அறிகுறிகளைப் புறக்கணிப்பது", "பிறருடன் ஒப்பிடுவது"
        )
        Language.ZH -> listOf(
            "仓促做决定", "为小事过度思虑", "忽略休息",
            "严苛的自我批评", "无休止刷手机", "口是心非地答应",
            "忽视身体的信号", "与他人比较"
        )
    }

    private fun focusList(lang: Language): List<String> = when (lang) {
        Language.EN -> listOf(
            "Connection & communication", "Creativity & self-expression", "Grounding & steady progress",
            "Courage & fresh starts", "Rest & inner balance", "Generosity & warmth",
            "Clarity & focus", "Joy & play"
        )
        Language.TA -> listOf(
            "தொடர்பும் உரையாடலும்", "படைப்பாற்றலும் சுய வெளிப்பாடும்", "நிலைத்த முன்னேற்றம்",
            "தைரியமும் புதிய தொடக்கமும்", "ஓய்வும் உள் சமநிலையும்", "தாராளமும் அன்பும்",
            "தெளிவும் கவனமும்", "மகிழ்ச்சியும் விளையாட்டும்"
        )
        Language.ZH -> listOf(
            "联结与沟通", "创造与自我表达", "踏实与稳步前进",
            "勇气与新的开始", "休息与内在平衡", "慷慨与温暖",
            "清晰与专注", "欢乐与玩耍"
        )
    }
}
