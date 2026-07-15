package com.astrochart.ui.i18n

import com.astrochart.core.i18n.Language
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Native-English-reader checks on the UI string table for [Language.EN]: a
 * sampling of static fields and every parameterized helper must be non-blank
 * and free of unresolved template artifacts.
 */
class UiStringsEnglishTest {

    private val en = UiStrings.forLanguage(Language.EN)

    private fun assertClean(value: String, label: String) {
        assertTrue(value.isNotBlank(), "$label is blank")
        assertFalse(value.contains("$"), "$label contains a literal '\$' artifact: $value")
        assertFalse(value.contains("\${"), "$label contains an unresolved '\${' template: $value")
    }

    @Test
    fun staticFields_areNonBlank() {
        val samples = mapOf(
            "navCalculate" to en.navCalculate,
            "tabWheel" to en.tabWheel,
            "tabReading" to en.tabReading,
            "homeSubtitle" to en.homeSubtitle,
            "labelAge" to en.labelAge,
            "labelGender" to en.labelGender,
            "labelChineseZodiac" to en.labelChineseZodiac
        )
        for ((label, value) in samples) {
            assertTrue(value.isNotBlank(), "$label should be non-blank")
        }
    }

    @Test
    fun parameterizedHelpers_areCleanAndInterpolated() {
        assertClean(en.ageValue(35), "ageValue(35)")
        assertClean(en.houseLabel(7), "houseLabel(7)")
        assertClean(en.bodyCount(1), "bodyCount(1)")
        assertClean(en.bodyCount(3), "bodyCount(3)")
        assertClean(en.deleteMessage("Alex"), "deleteMessage(Alex)")

        assertTrue(
            en.deleteMessage("Alex").contains("Alex"),
            "deleteMessage should interpolate the name"
        )
    }
}
