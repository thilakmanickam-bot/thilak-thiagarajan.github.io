package com.astrochart.uitest

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.astrochart.core.i18n.Language
import com.astrochart.data.LocationCatalog
import com.astrochart.ui.i18n.PanchangamStrings
import com.astrochart.ui.screens.CalendarScreen
import com.astrochart.ui.theme.AstroChartTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.YearMonth

/**
 * The reminder switches are a paid feature shown to everyone, so the two states
 * that matter are "unlocked and it toggles" and "locked and it cannot".
 *
 * A switch that looks live and silently does nothing would be worse than one
 * that is plainly inert, and a switch that a non-paying user could flip would
 * give away the feature — both are invisible from the code alone once the
 * enabled flag is threaded through several layers, which is what these pin.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class VrathamReminderToggleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val ps = PanchangamStrings.forLanguage(Language.EN)
    private val chennai = LocationCatalog.byDisplayName("Chennai, India")!!

    /** September 2026 — the month in the reported screenshots. */
    private val month = YearMonth.of(2026, 9)

    private val changes = mutableListOf<Pair<String, Boolean>>()

    private fun setContent(on: Set<String> = emptySet(), unlocked: Boolean) {
        composeTestRule.setContent {
            AstroChartTheme {
                CalendarScreen(
                    month = month,
                    onMonthChange = {},
                    location = chennai,
                    onDaySelected = {},
                    remindersOn = on,
                    remindersUnlocked = unlocked,
                    onReminderChange = { key, value -> changes += key to value }
                )
            }
        }
    }

    @Test
    fun everyObservanceRowCarriesItsOwnSwitch() {
        setContent(unlocked = true)

        // Named after the observance, so the switches are distinguishable to a
        // screen reader and to this test — a row of unlabelled toggles would
        // be neither.
        composeTestRule.onAllNodesWithContentDescription(ps.vratha("amavasai")).assertCountEquals(1)
        composeTestRule.onAllNodesWithContentDescription(ps.vratha("pournami")).assertCountEquals(1)
    }

    @Test
    fun aPremiumUserCanTurnAReminderOn() {
        setContent(unlocked = true)

        composeTestRule.onNodeWithContentDescription(ps.vratha("amavasai"))
            .performScrollTo()
            .performClick()

        assertEquals(listOf("amavasai" to true), changes)
    }

    @Test
    fun anAlreadyOnReminderTogglesBackOff() {
        setContent(on = setOf("pournami"), unlocked = true)

        composeTestRule.onNodeWithContentDescription(ps.vratha("pournami"))
            .performScrollTo()
            .assertIsOn()
            .performClick()

        assertEquals(listOf("pournami" to false), changes)
    }

    @Test
    fun onlyTheEnabledKeysReadAsOn() {
        setContent(on = setOf("ekadasi"), unlocked = true)

        composeTestRule.onNodeWithContentDescription(ps.vratha("ekadasi"))
            .performScrollTo().assertIsOn()
        composeTestRule.onNodeWithContentDescription(ps.vratha("amavasai"))
            .performScrollTo().assertIsOff()
    }

    @Test
    fun aBasicUserSeesTheSwitchesButCannotUseThem() {
        setContent(unlocked = false)

        composeTestRule.onNodeWithContentDescription(ps.vratha("amavasai"))
            .performScrollTo()
            .assertIsNotEnabled()
            .performClick()

        assertEquals("a locked switch must not report a change", emptyList<Pair<String, Boolean>>(), changes)
    }

    @Test
    fun aBasicUserIsToldWhyTheSwitchesAreInert() {
        setContent(unlocked = false)

        composeTestRule.onNodeWithText(ps.remindersPremium).performScrollTo()
    }

    @Test
    fun aPremiumUserIsNotNaggedAboutPremium() {
        setContent(unlocked = true)

        composeTestRule.onNodeWithText(ps.remindersPremium).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(ps.vratha("amavasai"))
            .performScrollTo()
            .assertIsEnabled()
    }
}
