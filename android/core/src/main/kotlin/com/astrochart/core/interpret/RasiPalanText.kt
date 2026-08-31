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
 * tone of [DailyReading]. Translated into every supported language.
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
        Language.TA -> when (period) {
            RasiPeriod.DAY -> "இன்று"; RasiPeriod.WEEK -> "இந்த வாரம்"
            RasiPeriod.MONTH -> "இந்த மாதம்"; RasiPeriod.YEAR -> "இந்த ஆண்டு"
        }
        Language.ZH -> when (period) {
            RasiPeriod.DAY -> "今天"; RasiPeriod.WEEK -> "本周"
            RasiPeriod.MONTH -> "本月"; RasiPeriod.YEAR -> "今年"
        }
        Language.HI -> when (period) {
            RasiPeriod.DAY -> "आज"; RasiPeriod.WEEK -> "इस सप्ताह"
            RasiPeriod.MONTH -> "इस माह"; RasiPeriod.YEAR -> "इस वर्ष"
        }
        Language.TE -> when (period) {
            RasiPeriod.DAY -> "నేడు"; RasiPeriod.WEEK -> "ఈ వారం"
            RasiPeriod.MONTH -> "ఈ నెల"; RasiPeriod.YEAR -> "ఈ సంవత్సరం"
        }
        Language.KN -> when (period) {
            RasiPeriod.DAY -> "ಇಂದು"; RasiPeriod.WEEK -> "ಈ ವಾರ"
            RasiPeriod.MONTH -> "ಈ ತಿಂಗಳು"; RasiPeriod.YEAR -> "ಈ ವರ್ಷ"
        }
        Language.ML -> when (period) {
            RasiPeriod.DAY -> "ഇന്ന്"; RasiPeriod.WEEK -> "ഈ ആഴ്ച"
            RasiPeriod.MONTH -> "ഈ മാസം"; RasiPeriod.YEAR -> "ഈ വർഷം"
        }
        Language.MR -> when (period) {
            RasiPeriod.DAY -> "आज"; RasiPeriod.WEEK -> "या आठवड्यात"
            RasiPeriod.MONTH -> "या महिन्यात"; RasiPeriod.YEAR -> "या वर्षी"
        }
        else -> when (period) {
            RasiPeriod.DAY -> "Today"; RasiPeriod.WEEK -> "This week"
            RasiPeriod.MONTH -> "This month"; RasiPeriod.YEAR -> "This year"
        }
    }

    private fun overall(lang: Language, period: RasiPeriod): List<String> {
        val p = periodWord(lang, period)
        return when (lang) {
            Language.TA -> listOf(
                "$p நிலையான, ஊக்கமளிக்கும் ஆற்றல் ஓட்டத்தைத் தருகிறது. உங்கள் உள்ளுணர்வை நம்பி முன்முயற்சி எடுங்கள்.",
                "$p தன்னம்பிக்கையையும் தெளிவான சிந்தனையையும் ஆதரிக்கிறது. சிறிய, தொடர்ச்சியான அடிகள் உங்களை முன்னேற்றும்.",
                "$p அன்பாலும் பொறுமையாலும் வாய்ப்புகளைத் திறக்கிறது. நம்பிக்கையுடன் புதிய வாய்ப்புகளை ஏற்றுக்கொள்ளுங்கள்.",
                "$p தைரியத்தையும் கவனத்தையும் பரிசளிக்கிறது. நீண்ட நாள் நிலுவையிலிருந்த விஷயம் நல்லபடியாக முடியும்.",
                "$p பிரகாசமாகவும் நம்பிக்கையுடனும் உள்ளது. உங்கள் முயற்சிகள் வேகம் பெறும், நற்செய்தி வந்து சேரும்."
            )
            Language.ZH -> listOf(
                "${p}能量平稳而振奋。相信直觉，在关键处主动出击。",
                "${p}有利于自信与清晰思考。稳健的小步会带你走得更远。",
                "${p}以温暖与耐心打开机会之门。保持乐观，拥抱新的机遇。",
                "${p}奖励勇气与专注。一件悬而未决的事将圆满解决。",
                "${p}明亮而充满希望。你的努力积聚动力，好消息正在到来。"
            )
            Language.HI -> listOf(
                "${p} ऊर्जा का स्थिर और उत्साहजनक प्रवाह रहता है। अपने अंतर्ज्ञान पर भरोसा करें और जहाँ ज़रूरी हो पहल करें।",
                "${p} आत्मविश्वास और स्पष्ट सोच का साथ रहता है। छोटे, निरंतर कदम आपको सोच से आगे ले जाते हैं।",
                "${p} गर्मजोशी और धैर्य से नए द्वार खुलते हैं। आशावादी रहें और नए अवसरों को अपनाएँ।",
                "${p} साहस और एकाग्रता का फल मिलता है। लंबे समय से लटका मामला सहजता से सुलझता है।",
                "${p} उज्ज्वल और आशाओं से भरा है। आपके प्रयास गति पकड़ते हैं और शुभ समाचार आता है।"
            )
            Language.TE -> listOf(
                "${p} శక్తి స్థిరంగా, ప్రోత్సాహకరంగా ప్రవహిస్తుంది. మీ అంతరాత్మను నమ్మి అవసరమైన చోట చొరవ చూపండి.",
                "${p} ఆత్మవిశ్వాసానికి, స్పష్టమైన ఆలోచనకు అనుకూలం. చిన్న, నిरంతర అడుగులు మిమ్మల్ని ఊహించని దూరం తీసుకెళ్తాయి.",
                "${p} ఆప్యాయత, ఓర్పుతో కొత్త ద్వారాలు తెరుస్తుంది. ఆశావాదంతో కొత్త అవకాశాలను స్వీకరించండి.",
                "${p} ధైర్యానికి, ఏకాగ్రతకు ఫలితం ఇస్తుంది. చాలాకాలంగా పెండింగ్‌లో ఉన్న విషయం చక్కగా పరిష్కారమవుతుంది.",
                "${p} ప్రకాశవంతంగా, ఆశలతో నిండి ఉంది. మీ ప్రయత్నాలు వేగం పుంజుకుంటాయి, శుభవార్త వస్తుంది."
            )
            Language.KN -> listOf(
                "${p} ಶಕ್ತಿ ಸ್ಥಿರವಾಗಿ, ಪ್ರೋತ್ಸಾಹದಾಯಕವಾಗಿ ಹರಿಯುತ್ತದೆ. ನಿಮ್ಮ ಅಂತರಂಗವನ್ನು ನಂಬಿ ಅಗತ್ಯವಿರುವಲ್ಲಿ ಮುಂದಾಗಿ.",
                "${p} ಆತ್ಮವಿಶ್ವಾಸ ಮತ್ತು ಸ್ಪಷ್ಟ ಚಿಂತನೆಗೆ ಪೂರಕ. ಸಣ್ಣ, ನಿರಂತರ ಹೆಜ್ಜೆಗಳು ನಿಮ್ಮನ್ನು ನಿರೀಕ್ಷೆಗಿಂತ ಮುಂದೆ ಕೊಂಡೊಯ್ಯುತ್ತವೆ.",
                "${p} ಪ್ರೀತಿ ಮತ್ತು ತಾಳ್ಮೆಯಿಂದ ಹೊಸ ಬಾಗಿಲುಗಳನ್ನು ತೆರೆಯುತ್ತದೆ. ಆಶಾವಾದಿಯಾಗಿರಿ, ಹೊಸ ಅವಕಾಶಗಳನ್ನು ಸ್ವೀಕರಿಸಿ.",
                "${p} ಧೈರ್ಯ ಮತ್ತು ಏಕಾಗ್ರತೆಗೆ ಫಲ ನೀಡುತ್ತದೆ. ಬಹುಕಾಲದಿಂದ ಬಾಕಿ ಇದ್ದ ವಿಷಯ ಸುಲಭವಾಗಿ ಇತ್ಯರ್ಥವಾಗುತ್ತದೆ.",
                "${p} ಪ್ರಕಾಶಮಾನವಾಗಿ, ಭರವಸೆಯಿಂದ ತುಂಬಿದೆ. ನಿಮ್ಮ ಪ್ರಯತ್ನಗಳು ವೇಗ ಪಡೆಯುತ್ತವೆ, ಶುಭ ಸುದ್ದಿ ಬರುತ್ತದೆ."
            )
            Language.ML -> listOf(
                "${p} ഊർജം സ്ഥിരമായി, പ്രോത്സാഹജനകമായി ഒഴുകുന്നു. നിങ്ങളുടെ ഉൾക്കാഴ്ചയെ വിശ്വസിച്ച് ആവശ്യമുള്ളിടത്ത് മുൻകൈയെടുക്കൂ.",
                "${p} ആത്മവിശ്വാസത്തിനും വ്യക്തമായ ചിന്തയ്ക്കും അനുകൂലം. ചെറിയ, തുടർച്ചയായ ചുവടുകൾ നിങ്ങളെ പ്രതീക്ഷിക്കുന്നതിലും അകലേക്ക് കൊണ്ടുപോകും.",
                "${p} സ്നേഹത്താലും ക്ഷമയാലും പുതിയ വാതിലുകൾ തുറക്കുന്നു. ശുഭാപ്തിവിശ്വാസത്തോടെ പുതിയ അവസരങ്ങൾ സ്വീകരിക്കൂ.",
                "${p} ധൈര്യത്തിനും ഏകാഗ്രതയ്ക്കും ഫലം നൽകുന്നു. ഏറെ നാളായി തീരാതെ കിടന്ന കാര്യം ഭംഗിയായി പരിഹരിക്കപ്പെടും.",
                "${p} തിളക്കമുള്ളതും പ്രതീക്ഷ നിറഞ്ഞതുമാണ്. നിങ്ങളുടെ ശ്രമങ്ങൾ വേഗം കൈവരിക്കും, ശുഭവാർത്ത എത്തും."
            )
            Language.MR -> listOf(
                "${p} ऊर्जेचा स्थिर आणि उत्साहवर्धक प्रवाह राहतो. आपल्या अंतर्मनावर विश्वास ठेवा आणि गरज तिथे पुढाकार घ्या.",
                "${p} आत्मविश्वास आणि स्पष्ट विचारांना साथ मिळते. लहान, सातत्यपूर्ण पावले तुम्हाला अपेक्षेपेक्षा पुढे नेतात.",
                "${p} प्रेम आणि संयमाने नवे दरवाजे उघडतात. आशावादी राहा आणि नव्या संधी स्वीकारा.",
                "${p} धैर्य आणि एकाग्रतेला फळ मिळते. बऱ्याच काळापासून रखडलेले काम सहजपणे मार्गी लागते.",
                "${p} तेजस्वी आणि आशेने भरलेला आहे. तुमचे प्रयत्न वेग घेतात आणि शुभ वार्ता येते."
            )
            else -> listOf(
                "$p brings a steady, encouraging flow of energy. Trust your instincts and take initiative where it counts.",
                "$p favours confidence and clear thinking. Small, consistent steps carry you further than you expect.",
                "$p opens doors through warmth and patience. Stay optimistic and lean into new opportunities.",
                "$p rewards courage and focus. A lingering matter finds a graceful resolution.",
                "$p is bright and full of promise. Your efforts gather momentum and good news travels your way."
            )
        }
    }

    private fun work(lang: Language): List<String> = when (lang) {
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
        Language.HI -> listOf(
            "काम & धन: व्यावहारिक विचार लाभ देता है; वादे निभाएँ तो आर्थिक स्थिति सहज रहती है।",
            "काम & धन: निरंतर प्रयास से पहचान मिलती है। छोटा लाभ या अवसर सामने आता है।",
            "काम & धन: टीम-वर्क काम को आसान बनाता है। जल्दबाज़ी से बचें, परिणाम मिलेंगे।",
            "काम & धन: एकाग्रता तेज़ होती है और रुका काम आगे बढ़ता है। सोच-समझकर खर्च करें।"
        )
        Language.TE -> listOf(
            "పని & డబ్బు: ఆచరణాత్మక ఆలోచన లాభం ఇస్తుంది; మాట నిలబెట్టుకుంటే ఆర్థిక స్థితి సౌకర్యంగా ఉంటుంది.",
            "పని & డబ్బు: నిరంతర కృషితో గుర్తింపు వస్తుంది. చిన్న లాభం లేదా అవకాశం కనిపిస్తుంది.",
            "పని & డబ్బు: జట్టుకృషి పనిని సులభం చేస్తుంది. తొందరపాటు నిర్ణయాలను తప్పించండి, ఫలితం వస్తుంది.",
            "పని & డబ్బు: ఏకాగ్రత పెరుగుతుంది, ఆగిన పని ముందుకు సాగుతుంది. ఆలోచించి ఖర్చు చేయండి."
        )
        Language.KN -> listOf(
            "ಕೆಲಸ & ಹಣ: ಪ್ರಾಯೋಗಿಕ ಆಲೋಚನೆ ಲಾಭ ನೀಡುತ್ತದೆ; ಮಾತು ಉಳಿಸಿಕೊಂಡರೆ ಆರ್ಥಿಕ ಸ್ಥಿತಿ ಸುಗಮವಾಗಿರುತ್ತದೆ.",
            "ಕೆಲಸ & ಹಣ: ನಿರಂತರ ಪ್ರಯತ್ನದಿಂದ ಗುರುತಿಸುವಿಕೆ ಸಿಗುತ್ತದೆ. ಸಣ್ಣ ಲಾಭ ಅಥವಾ ಅವಕಾಶ ಕಾಣಿಸುತ್ತದೆ.",
            "ಕೆಲಸ & ಹಣ: ತಂಡದ ಕೆಲಸ ಕಾರ್ಯವನ್ನು ಸುಲಭಗೊಳಿಸುತ್ತದೆ. ಆತುರದ ನಿರ್ಧಾರ ಬೇಡ, ಫಲಿತಾಂಶ ಬರುತ್ತದೆ.",
            "ಕೆಲಸ & ಹಣ: ಏಕಾಗ್ರತೆ ಹೆಚ್ಚುತ್ತದೆ, ನಿಂತ ಕೆಲಸ ಮುಂದೆ ಸಾಗುತ್ತದೆ. ವಿವೇಚನೆಯಿಂದ ಖರ್ಚು ಮಾಡಿ."
        )
        Language.ML -> listOf(
            "ജോലി & പണം: പ്രായോഗിക ആശയം ഫലം നൽകും; വാക്ക് പാലിച്ചാൽ സാമ്പത്തിക സ്ഥിതി സുഖകരമായിരിക്കും.",
            "ജോലി & പണം: തുടർച്ചയായ പരിശ്രമത്താൽ അംഗീകാരം ലഭിക്കും. ചെറിയ നേട്ടമോ അവസരമോ വരും.",
            "ജോലി & പണം: കൂട്ടായ്മ ജോലി എളുപ്പമാക്കും. തിടുക്കത്തിലുള്ള തീരുമാനം ഒഴിവാക്കൂ, ഫലം വരും.",
            "ജോലി & പണം: ശ്രദ്ധ മൂർച്ചയേറും, മുടങ്ങിയ കാര്യം മുന്നോട്ട് നീങ്ങും. കരുതലോടെ ചെലവാക്കൂ."
        )
        Language.MR -> listOf(
            "काम & पैसा: व्यावहारिक कल्पना फायदा देते; वचन पाळल्यास आर्थिक स्थिती सुखकर राहते.",
            "काम & पैसा: सातत्यपूर्ण प्रयत्नाने ओळख मिळते. लहान लाभ किंवा संधी समोर येते.",
            "काम & पैसा: सांघिक काम कार्य सोपे करते. घाईचे निर्णय टाळा, परिणाम मिळतील.",
            "काम & पैसा: एकाग्रता वाढते आणि रखडलेले काम पुढे सरकते. विचारपूर्वक खर्च करा."
        )
        else -> listOf(
            "Work & money: a practical idea pays off; keep your commitments and finances stay comfortable.",
            "Work & money: recognition comes through steady effort. A small gain or opportunity appears.",
            "Work & money: teamwork smooths a task. Avoid rushing decisions and results follow.",
            "Work & money: focus sharpens and a delayed matter moves forward. Spend mindfully."
        )
    }

    private fun love(lang: Language): List<String> = when (lang) {
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
        Language.HI -> listOf(
            "प्रेम & परिवार: घर में स्नेह सहज बहता है; एक मीठा शब्द रिश्ते को गहरा करता है।",
            "प्रेम & परिवार: साथ बिताया समय नज़दीकियाँ लाता है। ध्यान से सुनें, सामंजस्य बढ़ता है।",
            "प्रेम & परिवार: एक खुशी का पल या मिलन मन को उत्साहित करता है। अपनी खुशी बाँटें।",
            "प्रेम & परिवार: समझ मनमुटाव की जगह लेती है। सहारा दोनों ओर से बहता है।"
        )
        Language.TE -> listOf(
            "ప్రేమ & కుటుంబం: ఇంట్లో ఆప్యాయత సులభంగా ప్రవహిస్తుంది; ఒక మధురమైన మాట బంధాన్ని బలపరుస్తుంది.",
            "ప్రేమ & కుటుంబం: కలిసి గడిపిన సమయం సాన్నిహిత్యాన్ని తెస్తుంది. శ్రద్ధగా వినండి, సామరస్యం పెరుగుతుంది.",
            "ప్రేమ & కుటుంబం: ఒక ఆనంద క్షణం లేదా కలయిక మనసును ఉత్సాహపరుస్తుంది. మీ ఆనందాన్ని పంచుకోండి.",
            "ప్రేమ & కుటుంబం: అవగాహన మనస్పర్థలను తొలగిస్తుంది. మద్దతు రెండు వైపులా ప్రవహిస్తుంది."
        )
        Language.KN -> listOf(
            "ಪ್ರೀತಿ & ಕುಟುಂಬ: ಮನೆಯಲ್ಲಿ ಪ್ರೀತಿ ಸಲೀಸಾಗಿ ಹರಿಯುತ್ತದೆ; ಒಂದು ಸಿಹಿ ಮಾತು ಬಂಧವನ್ನು ಗಟ್ಟಿಗೊಳಿಸುತ್ತದೆ.",
            "ಪ್ರೀತಿ & ಕುಟುಂಬ: ಜೊತೆಗೆ ಕಳೆದ ಸಮಯ ಆತ್ಮೀಯತೆ ತರುತ್ತದೆ. ಗಮನವಿಟ್ಟು ಆಲಿಸಿ, ಸಾಮರಸ್ಯ ಬೆಳೆಯುತ್ತದೆ.",
            "ಪ್ರೀತಿ & ಕುಟುಂಬ: ಒಂದು ಸಂತಸದ ಕ್ಷಣ ಅಥವಾ ಭೇಟಿ ಮನಸ್ಸನ್ನು ಉಲ್ಲಾಸಗೊಳಿಸುತ್ತದೆ. ನಿಮ್ಮ ಸಂತೋಷವನ್ನು ಹಂಚಿಕೊಳ್ಳಿ.",
            "ಪ್ರೀತಿ & ಕುಟುಂಬ: ತಿಳಿವಳಿಕೆ ಭಿನ್ನಾಭಿಪ್ರಾಯವನ್ನು ನೀಗಿಸುತ್ತದೆ. ಬೆಂಬಲ ಎರಡೂ ಕಡೆ ಹರಿಯುತ್ತದೆ."
        )
        Language.ML -> listOf(
            "സ്നേഹം & കുടുംബം: വീട്ടിൽ സ്നേഹം സുഗമമായി ഒഴുകുന്നു; ഒരു നല്ല വാക്ക് ബന്ധം ദൃഢമാക്കുന്നു.",
            "സ്നേഹം & കുടുംബം: ഒരുമിച്ച് ചെലവഴിക്കുന്ന സമയം അടുപ്പം കൊണ്ടുവരുന്നു. ശ്രദ്ധയോടെ കേൾക്കൂ, ഇണക്കം വളരും.",
            "സ്നേഹം & കുടുംബം: ഒരു സന്തോഷ നിമിഷമോ കൂടിക്കാഴ്ചയോ മനസ്സിനെ ഉന്മേഷഭരിതമാക്കും. നിങ്ങളുടെ സന്തോഷം പങ്കിടൂ.",
            "സ്നേഹം & കുടുംബം: ധാരണ അഭിപ്രായവ്യത്യാസത്തിന് പകരമാകും. പിന്തുണ ഇരുവശത്തുനിന്നും ഒഴുകും."
        )
        Language.MR -> listOf(
            "प्रेम & कुटुंब: घरात स्नेह सहज वाहतो; एक गोड शब्द नाते अधिक घट्ट करतो.",
            "प्रेम & कुटुंब: एकत्र घालवलेला वेळ जवळीक आणतो. लक्षपूर्वक ऐका, सामंजस्य वाढते.",
            "प्रेम & कुटुंब: एखादा आनंदाचा क्षण किंवा भेट मन उल्हसित करते. आपला आनंद वाटा.",
            "प्रेम & कुटुंब: समजूत मतभेदाची जागा घेते. आधार दोन्ही बाजूंनी वाहतो."
        )
        else -> listOf(
            "Love & family: warmth flows easily at home; a kind word deepens a bond.",
            "Love & family: quality time brings closeness. Listen well and harmony grows.",
            "Love & family: a happy moment or reunion lifts your spirits. Share your joy.",
            "Love & family: understanding replaces friction. Support flows both ways."
        )
    }
}
