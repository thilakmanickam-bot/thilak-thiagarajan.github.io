package com.astrochart.uitest

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.astrochart.Features
import com.astrochart.core.i18n.Language
import com.astrochart.ui.i18n.PanchangamStrings
import com.astrochart.ui.i18n.PoruthamStrings
import com.astrochart.ui.i18n.RasiStrings
import com.astrochart.ui.i18n.UiStrings
import com.astrochart.ui.screens.HomeScreen
import com.astrochart.ui.theme.AstroChartTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The home screen is seven near-identical gold buttons wired to seven different
 * navigation lambdas. Nothing about a mis-wired one looks wrong — the button
 * renders perfectly and simply lands you on the wrong screen — so this asserts
 * that each label invokes *its own* callback and no other. Labels are read from
 * [UiStrings] rather than hardcoded, so a copy change updates the test with the
 * screen instead of breaking it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val strings = UiStrings.forLanguage(Language.EN)
    private val rasi = RasiStrings.forLanguage(Language.EN)
    private val porutham = PoruthamStrings.forLanguage(Language.EN)
    private val panchangam = PanchangamStrings.forLanguage(Language.EN)

    /** Every destination reached, in order, so a swap between two shows up. */
    private val visited = mutableListOf<String>()

    private fun setContent() {
        composeTestRule.setContent {
            AstroChartTheme {
                HomeScreen(
                    onNavigateToBirthInput = { visited += "birthInput" },
                    onNavigateToSavedCharts = { visited += "savedCharts" },
                    onNavigateToChat = { visited += "chat" },
                    onNavigateToPremium = { visited += "premium" },
                    onNavigateToPanchangam = { visited += "panchangam" },
                    onNavigateToCompatibility = { visited += "compatibility" },
                    onNavigateToRasi = { visited += "rasi" }
                )
            }
        }
    }

    /**
     * [ignoreCase] is required, not incidental: [com.astrochart.ui.components.GoldButton]
     * and `OutlineGoldButton` render `text.uppercase()`, so the semantics tree
     * holds "CALCULATE MY CHART" while [com.astrochart.ui.i18n.UiStrings] holds
     * "Calculate My Chart". Matching case-sensitively finds nothing. Asserting
     * on the uppercased form instead would just hard-code a styling decision
     * into every test.
     */
    private fun clickAndExpect(label: String, destination: String) {
        composeTestRule.onNodeWithText(label, ignoreCase = true).performScrollTo().performClick()

        assertEquals(listOf(destination), visited)
        visited.clear()
    }

    @Test
    fun calculateButton_routesToBirthInput() {
        setContent()

        clickAndExpect(strings.navCalculate, "birthInput")
    }

    @Test
    fun savedChartsButton_routesToSavedCharts() {
        setContent()

        clickAndExpect(strings.homeViewSaved, "savedCharts")
    }

    @Test
    fun rasiButton_routesToRasi() {
        setContent()

        clickAndExpect(rasi.entry, "rasi")
    }

    @Test
    fun compatibilityButton_routesToCompatibility() {
        setContent()

        clickAndExpect(porutham.entry, "compatibility")
    }

    @Test
    fun premiumButton_routesToPremium() {
        setContent()

        clickAndExpect(strings.premiumEntry, "premium")
    }

    @Test
    fun swipeUpHandle_opensPanchangam() {
        setContent()

        // The handle is also tappable, not only draggable — the tap path is what
        // most users hit, and it's the one a refactor is likeliest to drop.
        clickAndExpect(panchangam.swipeHint, "panchangam")
    }

    @Test
    fun chatEntry_followsTheFeatureFlag() {
        setContent()

        // ignoreCase for the same reason as clickAndExpect — and here it is what
        // makes the assertion mean anything. Without it this test passed while
        // CHAT_ENABLED was false, but for the wrong reason: the case-sensitive
        // match found nothing whether the button was rendered or not, so it
        // would have kept passing the day the flag was flipped on and the
        // gate silently stopped being tested at all.
        val chatEntries = composeTestRule.onAllNodesWithText(strings.chatEntry, ignoreCase = true)
        chatEntries.assertCountEquals(if (Features.CHAT_ENABLED) 1 else 0)
    }
}
