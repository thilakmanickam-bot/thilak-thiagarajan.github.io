package com.astrochart.ui.i18n

import com.astrochart.core.i18n.Language
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Where "until" sits relative to the time is grammar, and it differs by
 * language: English and Chinese put it first, the Indic languages here are
 * postpositional and put it last. Rendering one order for all of them read as
 * "02:56 AM until" in English, which is why this is pinned per language rather
 * than left to the screen.
 */
class PanchangamUntilOrderTest {

    private fun strings(lang: Language) = PanchangamStrings.forLanguage(lang)

    @Test
    fun englishReadsUntilBeforeTheTime() {
        assertEquals("until 02:56 AM", strings(Language.EN).untilTime("02:56 AM"))
    }

    @Test
    fun tamilKeepsVaraiAfterTheTime() {
        assertEquals("02:56 AM வரை", strings(Language.TA).untilTime("02:56 AM"))
    }

    @Test
    fun chineseReadsBeforeTheTime() {
        assertEquals("至 02:56", strings(Language.ZH).untilTime("02:56"))
    }

    @Test
    fun theWholeTimePhraseStaysTogetherIncludingNextDay() {
        // The word governs the whole phrase, so it must not end up between the
        // time and its "(next day)" qualifier in either order.
        val en = strings(Language.EN)
        val ta = strings(Language.TA)

        assertEquals("until 12:59 AM (next day)", en.untilTime("12:59 AM ${en.nextDay}"))
        assertEquals("12:59 AM (மறுநாள்) வரை", ta.untilTime("12:59 AM ${ta.nextDay}"))
    }

    @Test
    fun everyLanguageProducesTheTimeAndTheWordExactlyOnce() {
        Language.entries.forEach { lang ->
            val ps = strings(lang)
            val rendered = ps.untilTime("07:01 PM")

            assertTrue("$lang lost the time: $rendered", rendered.contains("07:01 PM"))
            assertTrue("$lang lost the word: $rendered", rendered.contains(ps.until))
            assertEquals(
                "$lang should read '${ps.until}' on one side of the time only",
                if (ps.untilPrecedesTime) "${ps.until} 07:01 PM" else "07:01 PM ${ps.until}",
                rendered
            )
        }
    }

    @Test
    fun onlyEnglishAndChineseArePrepositional() {
        // A guard on the data, not the function: adding a translation without
        // deciding this leaves it postpositional, which is right for the Indic
        // languages and wrong for a European one.
        val prepositional = Language.entries.filter { strings(it).untilPrecedesTime }

        assertEquals(setOf(Language.EN, Language.ZH), prepositional.toSet())
    }
}
