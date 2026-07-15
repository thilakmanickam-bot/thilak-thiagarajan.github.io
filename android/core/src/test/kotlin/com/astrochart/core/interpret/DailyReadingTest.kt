package com.astrochart.core.interpret

import com.astrochart.core.i18n.Language
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DailyReadingTest {

    private val date = LocalDate.of(2026, 7, 15)

    private fun tamilCount(s: String) = s.count { it.code in 0x0B80..0x0BFF }
    private fun cjkCount(s: String) = s.count { it.code in 0x4E00..0x9FFF }

    @Test
    fun build_isDeterministic_forSameDateLangSign() {
        val a = DailyReading.build(date, Language.EN, "Leo")
        val b = DailyReading.build(date, Language.EN, "Leo")
        assertEquals(a, b, "same date+lang+sign should yield identical reading")
    }

    @Test
    fun build_variesByDateAndSign() {
        val today = DailyReading.build(date, Language.EN, "Leo")
        val tomorrow = DailyReading.build(date.plusDays(1), Language.EN, "Leo")
        val otherSign = DailyReading.build(date, Language.EN, "Scorpio")
        // Not a hard guarantee for every pair, but across these fields at least one differs.
        assertTrue(
            today != tomorrow,
            "reading should change day to day"
        )
        assertTrue(
            today != otherSign,
            "reading should vary by sign"
        )
    }

    @Test
    fun build_summaryWithinLimit_andFieldsNonBlank_allLanguages() {
        for (lang in Language.entries) {
            val r = DailyReading.build(date, lang, "Cancer")
            assertTrue(r.summary.length <= 200, "$lang summary must be <= 200 chars, was ${r.summary.length}")
            assertTrue(r.summary.isNotBlank(), "$lang summary blank")
            assertTrue(r.goodToDo.isNotBlank(), "$lang goodToDo blank")
            assertTrue(r.avoid.isNotBlank(), "$lang avoid blank")
            assertTrue(r.focus.isNotBlank(), "$lang focus blank")
            assertTrue(r.luckyColorName.isNotBlank(), "$lang lucky colour blank")
            assertTrue(r.avoidColorName.isNotBlank(), "$lang avoid colour blank")
            // Lucky and avoid colours are always distinct.
            assertNotEquals(r.luckyColorHex, r.avoidColorHex, "$lang lucky and avoid colour must differ")
            assertNotEquals(r.luckyColorName, r.avoidColorName, "$lang colour names must differ")
            // No unresolved string-template artifacts.
            for (text in listOf(r.summary, r.goodToDo, r.avoid, r.focus)) {
                assertFalse(text.contains("$"), "$lang template artifact in: $text")
                assertFalse(text.contains("null"), "$lang literal null in: $text")
            }
        }
    }

    @Test
    fun build_usesCorrectScriptPerLanguage() {
        val ta = DailyReading.build(date, Language.TA, "Cancer")
        assertTrue(tamilCount(ta.summary) >= 10, "Tamil summary should contain Tamil script")
        assertTrue(tamilCount(ta.focus) > 0, "Tamil focus should contain Tamil script")

        val zh = DailyReading.build(date, Language.ZH, "Cancer")
        assertTrue(cjkCount(zh.summary) >= 5, "Chinese summary should contain CJK script")
        assertTrue(cjkCount(zh.focus) > 0, "Chinese focus should contain CJK script")
    }
}
