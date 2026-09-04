package com.astrochart.uitest

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.astrochart.core.i18n.Language
import com.astrochart.core.models.ChartStyle
import com.astrochart.ui.screens.OnboardingChartStyleStep
import com.astrochart.ui.screens.OnboardingLanguageStep
import com.astrochart.ui.theme.AstroChartTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The two onboarding steps that write the user's settings. Both are stateless
 * (the wizard hoists selection and step advancement into callbacks), so a
 * regression here is silent: the row still highlights, but the wrong value —
 * or none — reaches [com.astrochart.ui.i18n.LanguageStore] /
 * [com.astrochart.ui.i18n.ChartStyleStore], and the user lands in a language
 * or chart style they never chose.
 *
 * The wizard as a whole opens on the sign-in step, which needs a live Firebase
 * app, so these compose the individual steps exactly as the wizard does.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class OnboardingStepsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ---- language step ----

    @Test
    fun languageStep_reportsTheChosenLanguage() {
        var selected: Language? = null
        composeTestRule.setContent {
            AstroChartTheme {
                OnboardingLanguageStep(
                    selected = Language.EN,
                    onSelect = { selected = it },
                    onNext = {}
                )
            }
        }

        composeTestRule.onNodeWithText(Language.TA.displayName).performScrollTo().performClick()

        assertEquals(Language.TA, selected)
    }

    @Test
    fun languageStep_offersEveryLanguage() {
        composeTestRule.setContent {
            AstroChartTheme {
                OnboardingLanguageStep(selected = Language.EN, onSelect = {}, onNext = {})
            }
        }

        // A language silently missing from onboarding is only ever noticed by a
        // speaker of it, so assert the list is complete rather than spot-checking.
        Language.entries.forEach { language ->
            composeTestRule.onNodeWithText(language.displayName).performScrollTo()
        }
    }

    @Test
    fun languageStep_continueDoesNotSelectAnything() {
        var selected: Language? = null
        var advanced = false
        composeTestRule.setContent {
            AstroChartTheme {
                OnboardingLanguageStep(
                    selected = Language.EN,
                    onSelect = { selected = it },
                    onNext = { advanced = true }
                )
            }
        }

        // GoldButton renders text.uppercase(), so the semantics say "CONTINUE".
        composeTestRule.onNodeWithText("Continue", ignoreCase = true)
            .performScrollTo()
            .performClick()

        assertTrue(advanced)
        assertNull(selected)
    }

    // ---- chart style step ----

    @Test
    fun chartStyleStep_reportsSouthIndian() {
        var selected: ChartStyle? = null
        composeTestRule.setContent {
            AstroChartTheme {
                OnboardingChartStyleStep(
                    selected = ChartStyle.WESTERN_WHEEL,
                    onSelect = { selected = it },
                    onNext = {}
                )
            }
        }

        composeTestRule.onNodeWithText("South Indian").performScrollTo().performClick()

        assertEquals(ChartStyle.SOUTH_INDIAN, selected)
    }

    @Test
    fun chartStyleStep_reportsWesternWheel() {
        var selected: ChartStyle? = null
        composeTestRule.setContent {
            AstroChartTheme {
                OnboardingChartStyleStep(
                    selected = ChartStyle.SOUTH_INDIAN,
                    onSelect = { selected = it },
                    onNext = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Western (Wheel)").performScrollTo().performClick()

        assertEquals(ChartStyle.WESTERN_WHEEL, selected)
    }

    @Test
    fun chartStyleStep_northIndianIsInertNotSelectable() {
        var selected: ChartStyle? = null
        composeTestRule.setContent {
            AstroChartTheme {
                OnboardingChartStyleStep(
                    selected = ChartStyle.WESTERN_WHEEL,
                    onSelect = { selected = it },
                    onNext = {}
                )
            }
        }

        // North Indian is shown as "Coming soon" and is deliberately not a
        // ChoiceRow. If someone later wires it up before the layout exists,
        // this catches it rather than the user getting a blank chart.
        composeTestRule.onNodeWithText("North Indian — Coming soon")
            .performScrollTo()
            .performClick()

        assertNull(selected)
    }
}
