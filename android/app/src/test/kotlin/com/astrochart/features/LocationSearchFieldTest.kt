package com.astrochart.features

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import com.astrochart.data.LocationOption
import com.astrochart.ui.components.SearchableLocationField
import com.astrochart.ui.theme.AstroChartTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Regression test for a real bug: [SearchableLocationField] used to render
 * its suggestion list via `ExposedDropdownMenu`, a separate popup window
 * that could cut the field's IME connection once a suggestion appeared —
 * users reported typing/backspacing simply stopping working. Kept to
 * single-character queries (below the field's 2-char search threshold) so
 * this stays a fast, deterministic test of text-editing mechanics without
 * depending on the debounced search coroutine completing.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LocationSearchFieldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(onSelected: (LocationOption) -> Unit = {}) {
        composeTestRule.setContent {
            AstroChartTheme {
                SearchableLocationField(
                    label = "Location",
                    placeholder = "Search city",
                    selected = null,
                    onSelected = onSelected,
                    noResultsText = "No matching place"
                )
            }
        }
    }

    @Test
    fun typedText_appearsInTheField() {
        setContent()

        composeTestRule.onNodeWithText("Location").performTextInput("Pu")

        composeTestRule.onNodeWithText("Pu", substring = true).assertIsDisplayed()
    }

    @Test
    fun backspace_actuallyRemovesTheLastCharacter() {
        setContent()

        composeTestRule.onNodeWithText("Location").performTextInput("Pu")
        composeTestRule.onNodeWithText("Pu", substring = true).performKeyInput { pressKey(Key.Backspace) }

        // "Pu" must be gone (shortened to "P") — if backspace silently did
        // nothing (the original bug), "Pu" would still be there.
        composeTestRule.onNodeWithText("Pu", substring = true).assertDoesNotExist()
    }

    @Test
    fun repeatedBackspace_clearsTheFieldEntirely() {
        setContent()

        composeTestRule.onNodeWithText("Location").performTextInput("Pu")
        composeTestRule.onNodeWithText("Pu", substring = true).performKeyInput {
            pressKey(Key.Backspace)
            pressKey(Key.Backspace)
        }

        // An empty OutlinedTextField shows its placeholder text instead.
        composeTestRule.onNodeWithText("Search city").assertIsDisplayed()
    }
}
