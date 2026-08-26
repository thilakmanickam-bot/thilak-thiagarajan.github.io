package com.astrochart.core.interpret

import com.astrochart.core.i18n.Language
import java.time.LocalDate
import java.time.temporal.IsoFields
import kotlin.random.Random

/** Horizon of a rasi-palan reading. */
enum class RasiPeriod { DAY, WEEK, MONTH, YEAR }

/**
 * Generates an optimistic rasi-palan reading for a moon-sign (rasi) over a
 * chosen [RasiPeriod]. Deterministic per (period-bucket + sign + language): the
 * same week/month/year and sign always yield the same three short paragraphs
 * (overall, work & money, love & family), so readings are stable across
 * refreshes. Purely for daily flavour — not a predictive claim — matching the
 * tone of [DailyReading]. All text is stored as plain literals.
 */
object RasiPalanText {

    /** A stable seed bucket for the period containing [date]. */
    private fun bucket(period: RasiPeriod, date: LocalDate): Long = when (period) {
        RasiPeriod.DAY -> date.toEpochDay()
        RasiPeriod.WEEK -> date.get(IsoFields.WEEK_BASED_YEAR) * 100L + date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        RasiPeriod.MONTH -> date.year * 100L + date.monthValue
        RasiPeriod.YEAR -> date.year.toLong()
    }

    /** Three short paragraphs: overall, work & money, love & family. */
    fun horoscope(signIndex: Int, period: RasiPeriod, date: LocalDate, lang: Language): List<String> {
        val seed = bucket(period, date) * 13L + signIndex + period.ordinal * 101L
        val rnd = Random(seed)
        return listOf(
            overall(lang, period).random(rnd),
            work(lang).random(rnd),
            love(lang).random(rnd)
        )
    }

    private fun periodWord(lang: Language, period: RasiPeriod): String = when (lang) {
        Language.EN -> when (period) {
            RasiPeriod.DAY -> "Today"; RasiPeriod.WEEK -> "This week"
            RasiPeriod.MONTH -> "This month"; RasiPeriod.YEAR -> "This year"
        }
        Language.TA -> when (period) {
            RasiPeriod.DAY -> "இன்று"; RasiPeriod.WEEK -> "இந்த வாரம்"
            RasiPeriod.MONTH -> "இந்த மாதம்"; RasiPeriod.YEAR -> "இந்த ஆண்டு"
        }
        Language.ZH -> when (period) {
            RasiPeriod.DAY -> "今天"; RasiPeriod.WEEK -> "本周"
            RasiPeriod.MONTH -> "本月"; RasiPeriod.YEAR -> "今年"
        }
    }

    private fun overall(lang: Language, period: RasiPeriod): List<String> {
        val p = periodWord(lang, period)
        return when (lang) {
            Language.EN -> listOf(
                "$p brings a steady, encouraging flow of energy. Trust your instincts and take initiative where it counts.",
                "$p favours confidence and clear thinking. Small, consistent steps carry you further than you expect.",
                "$p opens doors through warmth and patience. Stay optimistic and lean into new opportunities.",
                "$p rewards courage and focus. A lingering matter finds a graceful resolution.",
                "$p is bright and full of promise. Your efforts gather momentum and good news travels your way."
            )
            Language.TA -> listOf(
                "$p நிலையான, ஊக்கமளிக்கும் ஆற்றல் ஓட்டத்தைத் தருகிறது. உங்கள் உள்ளுணர்வை நம்பி முன்முயற்சி எடுங்கள்.",
                "$p தன்னம்பிக்கையையும் தெளிவான சிந்தனையையும் ஆதரிக்கிறது. சிறிய, தொடர்ச்சியான அடிகள் உங்களை முன்னேற்றும்.",
                "$p அன்பாலும் பொறுமையாலும் வாய்ப்புகளைத் திறக்கிறது. நம்பிக்கையுடன் புதிய வாய்ப்புகளை ஏற்றுக்கொள்ளுங்கள்.",
                "$p தைரியத்தையும் கவனத்தையும் பரிசளிக்கிறது. நீண்ட நாள் நிலுவையிலிருந்த விஷயம் நல்லபடியாக முடியும்.",
                "$p பிரகாசமாகவும் நம்பிக்கையுடனும் உள்ளது. உங்கள் முயற்சிகள் வேகம் பெறும், நற்செய்தி வந்து சேரும்."
            )
            Language.ZH -> listOf(
                "$p能量平稳而振奋。相信直觉，在关键处主动出击。",
                "$p有利于自信与清晰思考。稳健的小步会带你走得更远。",
                "$p以温暖与耐心打开机会之门。保持乐观，拥抱新的机遇。",
                "$p奖励勇气与专注。一件悬而未决的事将圆满解决。",
                "$p明亮而充满希望。你的努力积聚动力，好消息正在到来。"
            )
        }
    }

    private fun work(lang: Language): List<String> = when (lang) {
        Language.EN -> listOf(
            "Work & money: a practical idea pays off; keep your commitments and finances stay comfortable.",
            "Work & money: recognition comes through steady effort. A small gain or opportunity appears.",
            "Work & money: teamwork smooths a task. Avoid rushing decisions and results follow.",
            "Work & money: focus sharpens and a delayed matter moves forward. Spend mindfully."
        )
        Language.TA -> listOf(
            "வேலை & பணம்: நடைமுறையான யோசனை பலன் தரும்; உறுதிமொழிகளைக் காத்தால் நிதி நிலை வசதியாக இருக்கும்.",
            "வேலை & பணம்: நிலையான முயற்சியால் அங்கீகாரம் கிடைக்கும். சிறிய லாபம் அல்லது வாய்ப்பு வரும்.",
            "வேலை & பணம்: குழு உழைப்பு வேலையை எளிதாக்கும். அவசர முடிவுகளைத் தவிர்த்தால் பலன் கிட்டும்.",
            "வேலை & பணம்: கவனம் கூர்மையாகும், தாமதமான விஷயம் முன்னேறும். சிக்கனமாகச் செலவழியுங்கள்."
        )
        Language.ZH -> listOf(
            "事业与财务：务实的点子会有回报；守住承诺，财务保持宽裕。",
            "事业与财务：稳定的努力赢得认可，会有小小的收获或机会。",
            "事业与财务：团队合作让任务顺畅。别急于决定，结果自会到来。",
            "事业与财务：专注力提升，拖延之事向前推进。理性消费。"
        )
    }

    private fun love(lang: Language): List<String> = when (lang) {
        Language.EN -> listOf(
            "Love & family: warmth flows easily at home; a kind word deepens a bond.",
            "Love & family: quality time brings closeness. Listen well and harmony grows.",
            "Love & family: a happy moment or reunion lifts your spirits. Share your joy.",
            "Love & family: understanding replaces friction. Support flows both ways."
        )
        Language.TA -> listOf(
            "காதல் & குடும்பம்: வீட்டில் அன்பு எளிதாகப் பாயும்; ஒரு இனிய வார்த்தை உறவை வலுப்படுத்தும்.",
            "காதல் & குடும்பம்: ஒன்றாகச் செலவழிக்கும் நேரம் நெருக்கத்தைத் தரும். நன்கு கேளுங்கள், இணக்கம் வளரும்.",
            "காதல் & குடும்பம்: மகிழ்ச்சியான தருணமோ சந்திப்போ மனதை உற்சாகப்படுத்தும். மகிழ்ச்சியைப் பகிருங்கள்.",
            "காதல் & குடும்பம்: புரிதல் மனக்கசப்பை நீக்கும். ஆதரவு இருபுறமும் பாயும்."
        )
        Language.ZH -> listOf(
            "爱情与家庭：家中温暖自在，一句体贴的话让感情更深。",
            "爱情与家庭：共度的时光拉近彼此。用心倾听，和谐渐长。",
            "爱情与家庭：一个愉快的时刻或重聚令你振奋。分享你的喜悦。",
            "爱情与家庭：理解取代摩擦，支持双向流动。"
        )
    }
}
