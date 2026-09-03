package com.astrochart.uitest

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.astrochart.core.i18n.Language
import com.astrochart.ui.i18n.PoruthamStrings
import com.astrochart.ui.i18n.UiStrings
import com.astrochart.ui.screens.CompatibilityScreen
import com.astrochart.ui.theme.AstroChartTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Birth details are optional, and the whole point of that word is that the
 * screen still works with them untouched. These cover the states that decide
 * it: collapsed (rasi and nakshatram hand-picked, exactly as before) and
 * expanded, per person.
 *
 * The derivation itself is arithmetic and belongs to the core — it is pinned
 * against an almanac in `PanchangamTest` rather than driven through the UI,
 * which would mean loading the 235k-row birthplace dataset just to pick a city.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CompatibilityScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val ps = PoruthamStrings.forLanguage(Language.EN)
    private val strings = UiStrings.forLanguage(Language.EN)

    private fun setContent() {
        composeTestRule.setContent {
            AstroChartTheme {
                CompatibilityScreen(onNavigateToPremium = {})
            }
        }
    }

    @Test
    fun birthDetailsAreOfferedOncePerPersonAndStartCollapsed() {
        setContent()

        composeTestRule.onAllNodesWithText(ps.birthDetails).assertCountEquals(2)

        // None of the fields are on screen yet, so the screen is no longer than
        // it was before this section existed.
        composeTestRule.onNodeWithText(ps.birthDetailsHint).assertDoesNotExist()
        composeTestRule.onNodeWithText(strings.searchCityHint).assertDoesNotExist()
    }

    @Test
    fun expandingOnePersonLeavesTheOtherCollapsed() {
        setContent()

        composeTestRule.onAllNodesWithText(ps.birthDetails)[0]
            .performScrollTo()
            .performClick()

        // One set of fields, not two: each card holds its own state.
        composeTestRule.onAllNodesWithText(ps.birthDetailsHint).assertCountEquals(1)
        composeTestRule.onAllNodesWithText(strings.searchCityHint).assertCountEquals(1)
    }

    @Test
    fun rasiAndNakshatramStayHandPickedWhileNoBirthDetailsAreGiven() {
        setContent()

        // The derived read-out replaces the dropdowns, so its absence is what
        // says the manual route — the only route to a score without birth
        // details — is still the one in force.
        composeTestRule.onNodeWithText(ps.derivedFromBirth).assertDoesNotExist()

        // At least one "Rasi" per person. Not an exact count: the dropdown
        // renders the same word as its caption, its label and its placeholder,
        // and how many of those are in the tree is a styling detail.
        val rasiNodes = composeTestRule.onAllNodesWithText(ps.rasi).fetchSemanticsNodes()
        assertTrue("expected a rasi control per person, found ${rasiNodes.size}", rasiNodes.size >= 2)
    }

    @Test
    fun clearingIsNotOfferedBeforeAnythingHasBeenEntered() {
        setContent()

        composeTestRule.onAllNodesWithText(ps.birthDetails)[0]
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText(ps.clearBirthDetails).assertDoesNotExist()
    }

    @Test
    fun calculateIsBlockedUntilBothPeopleAreComplete() {
        setContent()

        composeTestRule.onNodeWithText(ps.calculate, ignoreCase = true)
            .performScrollTo()
            .assertIsNotEnabled()
        composeTestRule.onNodeWithText(ps.fillAll).assertIsDisplayed()
    }
}
