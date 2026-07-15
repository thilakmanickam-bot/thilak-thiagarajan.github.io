package com.astrochart.ui.i18n

import com.astrochart.core.i18n.Language
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Verifies the Chinese ([Language.ZH]) UI string bundle is actually localized
 * (CJK present, differs from English) and that the parameterized helpers produce
 * no unresolved Kotlin string-template artifacts.
 */
class UiStringsChineseTest {

    private val zh = UiStrings.forLanguage(Language.ZH)
    private val en = UiStrings.forLanguage(Language.EN)

    private fun countCjk(text: String): Int =
        text.count { it.code in 0x4E00..0x9FFF }

    @Test
    fun representativeFields_areLocalizedChinese() {
        val zhFields = listOf(
            "navCalculate" to zh.navCalculate,
            "tabWheel" to zh.tabWheel,
            "tabReading" to zh.tabReading,
            "homeSubtitle" to zh.homeSubtitle,
            "labelAge" to zh.labelAge,
            "gender" to zh.gender,
            "genderFemale" to zh.genderFemale
        )
        val enFields = mapOf(
            "navCalculate" to en.navCalculate,
            "tabWheel" to en.tabWheel,
            "tabReading" to en.tabReading,
            "homeSubtitle" to en.homeSubtitle,
            "labelAge" to en.labelAge,
            "gender" to en.gender,
            "genderFemale" to en.genderFemale
        )

        for ((name, value) in zhFields) {
            assertTrue(value.isNotBlank(), "$name is blank")
            assertTrue(countCjk(value) > 0, "$name has no CJK characters: $value")
            assertNotEquals(enFields[name], value, "$name matches the English string")
        }
    }

    @Test
    fun parameterizedHelpers_haveNoTemplateArtifacts() {
        val samples = listOf(
            zh.ageValue(35),
            zh.houseLabel(7),
            zh.bodyCount(3),
            zh.deleteMessage("小明")
        )
        for (s in samples) {
            assertTrue(s.isNotBlank(), "parameterized string is blank")
            assertFalse(s.contains("$"), "unresolved '\$' template artifact in: $s")
            assertFalse(s.contains("\${"), "unresolved '\${' template artifact in: $s")
        }

        // The delete confirmation must interpolate the chart name.
        assertTrue(
            zh.deleteMessage("小明").contains("小明"),
            "deleteMessage should contain the chart name"
        )
    }
}
