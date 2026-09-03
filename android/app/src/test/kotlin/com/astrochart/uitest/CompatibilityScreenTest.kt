package com.astrochart.uitest

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.astrochart.core.i18n.Language
import com.astrochart.core.panchangam.PanchangamNames
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
        composeTestRule.onNodeWithText(strings.pob).assertDoesNotExist()
    }

    @Test
    fun expandingOnePersonLeavesTheOtherCollapsed() {
        setContent()

        composeTestRule.onAllNodesWithText(ps.birthDetails)[0]
            .performScrollTo()
            .performClick()

        // One set of fields, not two: each card holds its own state.
        composeTestRule.onAllNodesWithText(ps.birthDetailsHint).assertCountEquals(1)
        composeTestRule.onAllNodesWithText(strings.pob).assertCountEquals(1)
        // The birthplace field's own label, not its placeholder: Material 3
        // renders a placeholder only while the field is focused and empty, so
        // asserting on strings.searchCityHint passes vacuously either way.
        composeTestRule.onAllNodesWithText(strings.location).assertCountEquals(1)
    }

    @Test
    fun rasiAndNakshatramStayHandPickedWhileNoBirthDetailsAreGiven() {
        setContent()

        // The derived read-out replaces the dropdowns, so its absence is what
        // says the manual route — the only route to a score without birth
        // details — is still the one in force.
        composeTestRule.onNodeWithText(ps.derivedFromBirth).assertDoesNotExist()

        // At least one "Rasi" per person. Not an exact count: the caption above
        // the dropdown and the dropdown's own label are the same word, and how
        // many of those end up as separate nodes is a styling detail.
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

    /**
     * The whole manual path, end to end. The six loose `var`s this screen used
     * to hold were folded into one state holder per person; nothing about that
     * refactor looks wrong from the outside if the two people's state got
     * crossed or a field stopped reaching the scoring call, so this drives four
     * dropdowns and checks a score actually comes back.
     *
     * Nodes are found by "has this label AND is clickable", which picks the
     * dropdown itself over the caption above it that repeats the same word.
     */
    @Test
    fun aMatchCanStillBeScoredFromHandPickedValuesAlone() {
        setContent()

        pickFromDropdown(label = ps.rasi, option = "Aries")
        pickFromDropdown(label = ps.nakshatram, option = nakshatra(0))
        pickFromDropdown(label = ps.rasi, option = "Taurus", index = 1)
        pickFromDropdown(label = ps.nakshatram, option = nakshatra(1), index = 1)

        composeTestRule.onNodeWithText(ps.calculate, ignoreCase = true)
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText(ps.totalScore).performScrollTo().assertIsDisplayed()
        // No birth details were given, so nothing claims to have been derived.
        composeTestRule.onNodeWithText(ps.derivedFromBirth).assertDoesNotExist()
    }

    private fun nakshatra(index: Int) =
        PanchangamNames.nakshatras[index].get(Language.EN)

    private fun pickFromDropdown(label: String, option: String, index: Int = 0) {
        composeTestRule.onAllNodes(hasText(label) and hasClickAction())[index]
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithText(option).performClick()
    }

    @Test
    fun calculateIsBlockedUntilBothPeopleAreComplete() {
        setContent()

        composeTestRule.onNodeWithText(ps.calculate, ignoreCase = true)
            .performScrollTo()
            .assertIsNotEnabled()
        composeTestRule.onNodeWithText(ps.fillAll).performScrollTo().assertIsDisplayed()
    }
}
