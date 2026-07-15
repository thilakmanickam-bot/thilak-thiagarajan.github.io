package com.astrochart.ui.i18n

import com.astrochart.core.i18n.Language
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Translation-QA coverage for the Tamil [UiStrings]. Confirms representative
 * literals are real Tamil (differ from English, contain Tamil-block script) and
 * that the parameterized helpers leak no string-template artifacts.
 */
class UiStringsTamilTest {

    private val ta = UiStrings.forLanguage(Language.TA)
    private val en = UiStrings.forLanguage(Language.EN)

    /** Number of characters in the Tamil Unicode block U+0B80..U+0BFF. */
    private fun tamilCharCount(text: String): Int =
        text.count { it.code in 0x0B80..0x0BFF }

    @Test
    fun representativeFields_areLocalizedToTamil() {
        val cases = listOf(
            "navCalculate" to Pair(ta.navCalculate, en.navCalculate),
            "tabWheel" to Pair(ta.tabWheel, en.tabWheel),
            "tabReading" to Pair(ta.tabReading, en.tabReading),
            "homeSubtitle" to Pair(ta.homeSubtitle, en.homeSubtitle),
            "labelAge" to Pair(ta.labelAge, en.labelAge),
            "gender" to Pair(ta.gender, en.gender),
            "genderFemale" to Pair(ta.genderFemale, en.genderFemale)
        )
        for ((field, values) in cases) {
            val (taValue, enValue) = values
            assertTrue(taValue.isNotBlank(), "$field Tamil value is blank")
            assertNotEquals(enValue, taValue, "$field was not translated to Tamil")
            assertTrue(
                tamilCharCount(taValue) > 0,
                "$field Tamil value has no Tamil-block characters: $taValue"
            )
        }
    }

    @Test
    fun parameterizedHelpers_haveNoTemplateArtifacts() {
        val samples = listOf(
            "ageValue" to ta.ageValue(35),
            "houseLabel" to ta.houseLabel(7),
            "bodyCount" to ta.bodyCount(3),
            "deleteMessage" to ta.deleteMessage("அருண்")
        )
        for ((field, value) in samples) {
            assertTrue(value.isNotBlank(), "$field produced a blank value")
            assertFalse(value.contains("$"), "$field leaked a dollar sign: $value")
            assertFalse(value.contains("\${"), "$field leaked a template expression: $value")
        }
        assertTrue(
            ta.deleteMessage("அருண்").contains("அருண்"),
            "deleteMessage should embed the provided name"
        )
    }
}
