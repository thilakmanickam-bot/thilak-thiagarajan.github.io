package com.astrochart.core.interpret

import com.astrochart.core.i18n.ContentLang
import com.astrochart.core.i18n.Language

/** A short localized triple (English / Tamil / Chinese). */
data class L3(val en: String, val ta: String, val zh: String) {
    fun get(lang: Language): String = when (lang.content) {
        ContentLang.EN -> en
        ContentLang.TA -> ta
        ContentLang.ZH -> zh
    }
}

/**
 * Traditional reference facts for a moon-sign (rasi): ruling planet, friendly
 * signs, and the customary lucky colour / day / number / deity / gemstone, plus
 * a short personality note. Values follow common Tamil-astrology conventions
 * (lord-based day and number; element-based friendly signs). Indices are
 * 0 = Aries … 11 = Pisces, matching [com.astrochart.core.utils.ZodiacUtils].
 */
data class RasiInfoData(
    val signIndex: Int,
    val lord: String,                 // planet English key (localize via Translations.planetName)
    val friendlySigns: List<Int>,     // sign indices
    val luckyDayIndex: Int,           // 0 = Sunday … 6 = Saturday
    val luckyNumber: Int,
    val color: L3,
    val deity: L3,
    val gemstone: L3,
    val description: L3
)

object RasiInfo {

    private val CORAL = L3("Red Coral", "பவளம்", "红珊瑚")
    private val DIAMOND = L3("Diamond", "வைரம்", "钻石")
    private val EMERALD = L3("Emerald", "மரகதம்", "祖母绿")
    private val PEARL = L3("Pearl", "முத்து", "珍珠")
    private val RUBY = L3("Ruby", "மாணிக்கம்", "红宝石")
    private val YSAPPHIRE = L3("Yellow Sapphire", "புஷ்பராகம்", "黄宝石")
    private val BSAPPHIRE = L3("Blue Sapphire", "நீலமணி", "蓝宝石")

    private val RED = L3("Red", "சிவப்பு", "红色")
    private val WHITE = L3("White", "வெள்ளை", "白色")
    private val GREEN = L3("Green", "பச்சை", "绿色")
    private val SILVER = L3("Silver", "வெள்ளி", "银色")
    private val GOLD = L3("Gold", "தங்கம்", "金色")
    private val BLUE = L3("Blue", "நீலம்", "蓝色")
    private val YELLOW = L3("Yellow", "மஞ்சள்", "黄色")

    val all: List<RasiInfoData> = listOf(
        RasiInfoData(
            0, "Mars", listOf(10, 4, 8, 2), 2, 9, RED,
            L3("Murugan", "முருகன்", "Murugan"), CORAL,
            L3(
                "Bold, energetic pioneers who lead with courage and act decisively; direct and independent.",
                "தைரியமும் ஆற்றலும் கொண்ட முன்னோடிகள்; நேரடியாகவும் சுதந்திரமாகவும் தீர்க்கமாகச் செயல்படுவார்கள்.",
                "大胆、有活力的开拓者，勇敢果断、直接而独立。"
            )
        ),
        RasiInfoData(
            1, "Venus", listOf(5, 9, 3, 7), 5, 6, WHITE,
            L3("Lakshmi", "லட்சுமி", "Lakshmi"), DIAMOND,
            L3(
                "Steady, patient and dependable; they value comfort, beauty and lasting security.",
                "நிலையான, பொறுமையான, நம்பகமான குணம்; சுகம், அழகு, நிலையான பாதுகாப்பை மதிப்பர்.",
                "稳健、耐心、可靠，重视舒适、美感与长久的安稳。"
            )
        ),
        RasiInfoData(
            2, "Mercury", listOf(6, 10, 0, 4), 3, 5, GREEN,
            L3("Vishnu", "விஷ்ணு", "Vishnu"), EMERALD,
            L3(
                "Curious, quick-witted communicators who love learning, variety and lively conversation.",
                "ஆர்வமும் கூர்மையான புத்தியும் கொண்டவர்; கற்றல், பன்முகத்தன்மை, உரையாடலை விரும்புவார்கள்.",
                "好奇、机敏、善于沟通，热爱学习、多样与畅谈。"
            )
        ),
        RasiInfoData(
            3, "Moon", listOf(7, 11, 1, 5), 1, 2, SILVER,
            L3("Parvati", "பார்வதி", "Parvati"), PEARL,
            L3(
                "Caring, intuitive and protective; deeply attached to home, family and emotional bonds.",
                "பராமரிக்கும், உள்ளுணர்வுள்ள, பாதுகாக்கும் குணம்; வீடு, குடும்பம், உணர்வுப் பிணைப்பில் ஆழ்ந்தவர்.",
                "体贴、直觉、守护，深深依恋家庭与情感纽带。"
            )
        ),
        RasiInfoData(
            4, "Sun", listOf(0, 8, 6, 10), 0, 1, GOLD,
            L3("Shiva", "சிவன்", "Shiva"), RUBY,
            L3(
                "Warm, generous and proud; natural leaders who shine with confidence and heartfelt loyalty.",
                "அன்பான, தாராளமான, பெருமிதமான குணம்; தன்னம்பிக்கையுடன் ஒளிரும் இயற்கையான தலைவர்கள்.",
                "热情、慷慨、自豪，天生的领袖，自信而真诚忠诚。"
            )
        ),
        RasiInfoData(
            5, "Mercury", listOf(1, 9, 7, 11), 3, 5, GREEN,
            L3("Vishnu", "விஷ்ணு", "Vishnu"), EMERALD,
            L3(
                "Precise, practical and analytical; helpful perfectionists with an eye for detail and order.",
                "நுணுக்கமான, நடைமுறையான, பகுப்பாய்வான குணம்; நுட்பத்திலும் ஒழுங்கிலும் கவனமுள்ளவர்.",
                "精确、务实、善于分析，注重细节与条理的助人者。"
            )
        ),
        RasiInfoData(
            6, "Venus", listOf(2, 10, 4, 8), 5, 6, BLUE,
            L3("Lakshmi", "லட்சுமி", "Lakshmi"), DIAMOND,
            L3(
                "Balanced, gracious and fair; they seek harmony, partnership and beauty in all things.",
                "சமநிலையான, அழகான, நியாயமான குணம்; அனைத்திலும் இணக்கம், கூட்டாண்மை, அழகை நாடுவார்.",
                "平衡、优雅、公正，处处追求和谐、伙伴关系与美。"
            )
        ),
        RasiInfoData(
            7, "Mars", listOf(3, 11, 5, 9), 2, 9, RED,
            L3("Durga", "துர்கை", "Durga"), CORAL,
            L3(
                "Intense, passionate and determined; deeply loyal, with great power to transform and endure.",
                "தீவிரமான, ஆர்வமான, உறுதியான குணம்; ஆழமான விசுவாசமும் மாற்றும் ஆற்றலும் கொண்டவர்.",
                "强烈、热情、坚定，忠诚且极具蜕变与坚忍之力。"
            )
        ),
        RasiInfoData(
            8, "Jupiter", listOf(0, 4, 2, 6), 4, 3, YELLOW,
            L3("Dakshinamurthy", "தட்சிணாமூர்த்தி", "Dakshinamurthy"), YSAPPHIRE,
            L3(
                "Optimistic, honest and adventurous; philosophical seekers who love freedom and the big picture.",
                "நம்பிக்கையான, நேர்மையான, சாகச குணம்; சுதந்திரத்தையும் விரிந்த பார்வையையும் விரும்பும் தேடுபவர்.",
                "乐观、坦率、爱冒险，热爱自由与宏观的哲思者。"
            )
        ),
        RasiInfoData(
            9, "Saturn", listOf(1, 5, 3, 11), 6, 8, BLUE,
            L3("Ayyappan", "ஐயப்பன்", "Ayyappan"), BSAPPHIRE,
            L3(
                "Disciplined, ambitious and patient; steady climbers who build lasting success through hard work.",
                "ஒழுக்கமான, லட்சியமான, பொறுமையான குணம்; கடின உழைப்பால் நிலையான வெற்றியைக் கட்டமைப்பவர்.",
                "自律、有抱负、耐心，凭勤奋稳步攀登、成就长久。"
            )
        ),
        RasiInfoData(
            10, "Saturn", listOf(2, 6, 0, 8), 6, 8, BLUE,
            L3("Shiva", "சிவன்", "Shiva"), BSAPPHIRE,
            L3(
                "Independent, inventive and humane; original thinkers drawn to ideas, community and progress.",
                "சுதந்திரமான, கண்டுபிடிப்பான, மனிதநேயமான குணம்; கருத்துகள், சமூகம், முன்னேற்றத்தில் ஈடுபடும் சிந்தனையாளர்.",
                "独立、富创造、有人文关怀，倾心理念、社群与进步的原创思想者。"
            )
        ),
        RasiInfoData(
            11, "Jupiter", listOf(3, 7, 1, 9), 4, 3, YELLOW,
            L3("Guru", "குரு", "Guru"), YSAPPHIRE,
            L3(
                "Imaginative, compassionate and gentle; dreamers with deep empathy and a spiritual heart.",
                "கற்பனையான, இரக்கமுள்ள, மென்மையான குணம்; ஆழ்ந்த பரிவும் ஆன்மிக மனமும் கொண்ட கனவுகாரர்.",
                "富想象、有同情心、温柔，怀深切共情与灵性之心的梦想家。"
            )
        )
    )

    fun of(signIndex: Int): RasiInfoData = all[((signIndex % 12) + 12) % 12]
}
