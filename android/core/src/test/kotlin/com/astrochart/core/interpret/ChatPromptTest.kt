package com.astrochart.core.interpret

import com.astrochart.core.models.ChartStyle
import com.astrochart.core.i18n.Language
import com.astrochart.core.models.BirthData
import com.astrochart.core.utils.ChartCalculator
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChatPromptTest {

    private fun sampleChart() = ChartCalculator.calculateNatalChart(
        BirthData(
            dateTime = LocalDateTime.of(1990, 7, 20, 14, 30),
            latitude = 34.0522,
            longitude = -118.2437,
            timeZone = ZoneId.of("America/Los_Angeles"),
            locationName = "Los Angeles"
        )
    )

    @Test
    fun chartContext_reflectsRealChart_localized() {
        val chart = sampleChart()
        val ctx = ChatPrompt.chartContext(chart, "Alex", ChartStyle.SOUTH_INDIAN, Language.EN)
        assertTrue(ctx.contains("Alex"), "context should name the person")
        // Every planet in the chart is represented in the context snapshot.
        for (planet in chart.planets) {
            assertTrue(ctx.contains("${planet.name} in "), "${planet.name} missing from context")
        }

        // Chinese context localizes planet names (Sun -> 太阳).
        val zh = ChatPrompt.chartContext(chart, "Alex", ChartStyle.SOUTH_INDIAN, Language.ZH)
        assertTrue(zh.contains("太阳"), "Chinese context should localize the Sun")
    }

    @Test
    fun systemPrompt_containsPersona_context_andLanguageDirective() {
        val chart = sampleChart()
        val ctx = ChatPrompt.chartContext(chart, "Alex", ChartStyle.SOUTH_INDIAN, Language.EN)
        val prompt = ChatPrompt.systemPrompt(Language.EN, ctx)

        // Persona guardrail present, chart context embedded (header + the actual
        // chart snapshot), English directive present.
        assertTrue(prompt.contains("You do not tell the future"), "persona boundary missing")
        assertTrue(prompt.contains("for reflection only"), "context header missing")
        assertTrue(prompt.contains("Alex"), "chart context not embedded")
        assertTrue(prompt.contains(ctx), "the chart snapshot is not embedded verbatim")
        assertTrue(prompt.contains("Always reply in English"), "English directive missing")

        // The Tamil and Chinese prompts carry their own reply directives.
        assertTrue(ChatPrompt.systemPrompt(Language.TA, ctx).contains("தமிழில்"))
        assertTrue(ChatPrompt.systemPrompt(Language.ZH, ctx).contains("用中文"))
    }

    @Test
    fun greetingAndSuggestions_nonBlank_forEveryLanguage() {
        for (lang in Language.entries) {
            val greeting = ChatPrompt.greeting(lang, "Alex")
            assertTrue(greeting.isNotBlank(), "$lang greeting is blank")
            // Interpolation must survive CJK/Tamil scripts: the name is present,
            // not swallowed into an identifier by an unbraced `$name`.
            assertTrue(greeting.contains("Alex"), "$lang greeting lost the name")

            val questions = ChatPrompt.suggestedQuestions(lang)
            assertEquals(4, questions.size, "$lang should have 4 starter questions")
            for (q in questions) {
                assertTrue(q.isNotBlank(), "$lang has a blank suggested question")
            }
        }
    }

    @Test
    fun localizedText_actuallyUsesTheTargetScript() {
        // Guards against a copy-paste of English into the TA/ZH branches: the
        // localized greeting and suggestions must contain their own script.
        val taGreeting = ChatPrompt.greeting(Language.TA, "Alex")
        assertTrue(taGreeting.any { it in '஀'..'௿' }, "TA greeting has no Tamil script")
        assertTrue(
            ChatPrompt.suggestedQuestions(Language.TA).all { q -> q.any { it in '஀'..'௿' } },
            "a TA suggestion has no Tamil script"
        )

        val zhGreeting = ChatPrompt.greeting(Language.ZH, "Alex")
        assertTrue(zhGreeting.any { it in '一'..'鿿' }, "ZH greeting has no Han script")
        assertTrue(
            ChatPrompt.suggestedQuestions(Language.ZH).all { q -> q.any { it in '一'..'鿿' } },
            "a ZH suggestion has no Han script"
        )
    }
}
