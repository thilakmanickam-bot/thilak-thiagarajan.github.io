package com.astrochart.core.interpret

import com.astrochart.core.i18n.Language
import org.junit.Test
import kotlin.test.assertEquals

class ChineseZodiacTest {

    @Test
    fun of_returnsCorrectAnimalForKnownYears() {
        assertEquals("Rat", ChineseZodiac.of(2008))
        assertEquals("Rat", ChineseZodiac.of(2020))
        assertEquals("Dragon", ChineseZodiac.of(2000))
        assertEquals("Horse", ChineseZodiac.of(1990))
        assertEquals("Pig", ChineseZodiac.of(2019))
    }

    @Test
    fun of_isStableAcrossTheTwelveYearCycle() {
        assertEquals(ChineseZodiac.of(1990), ChineseZodiac.of(2002))
        assertEquals(ChineseZodiac.of(1990), ChineseZodiac.of(1978))
    }

    @Test
    fun name_isLocalized() {
        assertEquals("龙", ChineseZodiac.name(2000, Language.ZH))
        assertEquals("நாகம்", ChineseZodiac.name(2000, Language.TA))
        assertEquals("Dragon", ChineseZodiac.name(2000, Language.EN))
    }
}
