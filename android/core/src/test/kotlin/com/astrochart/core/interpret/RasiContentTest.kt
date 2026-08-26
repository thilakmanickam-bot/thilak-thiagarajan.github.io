package com.astrochart.core.interpret

import com.astrochart.core.i18n.Language
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RasiContentTest {

    @Test
    fun horoscope_isThreeCleanParagraphs_forEverySignPeriodLanguage() {
        val date = LocalDate.of(2026, 8, 26)
        for (lang in Language.entries) {
            for (period in RasiPeriod.entries) {
                for (sign in 0..11) {
                    val paras = RasiPalanText.horoscope(sign, period, date, lang)
                    assertEquals(3, paras.size, "$lang $period sign $sign")
                    paras.forEach { assertTrue(it.isNotBlank() && !it.contains("\${")) }
                }
            }
        }
    }

    @Test
    fun horoscope_isDeterministicPerBucket() {
        val d1 = LocalDate.of(2026, 8, 26)
        val d2 = LocalDate.of(2026, 8, 26)
        assertEquals(
            RasiPalanText.horoscope(3, RasiPeriod.MONTH, d1, Language.EN),
            RasiPalanText.horoscope(3, RasiPeriod.MONTH, d2, Language.EN)
        )
    }

    @Test
    fun rasiInfo_isCompleteAndInRange() {
        assertEquals(12, RasiInfo.all.size)
        RasiInfo.all.forEachIndexed { i, info ->
            assertEquals(i, info.signIndex)
            assertTrue(info.lord.isNotBlank())
            assertTrue(info.luckyDayIndex in 0..6)
            assertTrue(info.luckyNumber in 1..9)
            assertTrue(info.friendlySigns.isNotEmpty())
            info.friendlySigns.forEach { assertTrue(it in 0..11, "friendly $it out of range") }
            assertTrue(info.friendlySigns.none { it == i }, "sign $i lists itself as friendly")
            for (lang in Language.entries) {
                assertTrue(info.color.get(lang).isNotBlank())
                assertTrue(info.deity.get(lang).isNotBlank())
                assertTrue(info.gemstone.get(lang).isNotBlank())
                assertTrue(info.description.get(lang).isNotBlank())
            }
        }
    }
}
