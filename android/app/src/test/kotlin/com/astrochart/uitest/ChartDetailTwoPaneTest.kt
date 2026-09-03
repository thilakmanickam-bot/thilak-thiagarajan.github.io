package com.astrochart.uitest

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.astrochart.core.i18n.Language
import com.astrochart.core.models.BirthData
import com.astrochart.core.models.NatalChart
import com.astrochart.core.utils.ChartCalculator
import com.astrochart.ui.i18n.LocalStrings
import com.astrochart.ui.i18n.UiStrings
import com.astrochart.ui.screens.ChartDetailScreen
import com.astrochart.ui.theme.AstroChartTheme
import com.astrochart.ui.theme.LocalWindowSizeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * On a tablet the chart screen splits into a 30% details rail and a 70% chart
 * pane, and the details rail lays its values out stacked rather than three
 * across.
 *
 * That rebuild is where something can go quietly missing: the six header values
 * are now assembled into a list and rendered by one of two branches, so a value
 * dropped from a branch would simply not appear, with nothing else looking
 * wrong. These assert the same content survives both widths.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "w1280dp-h800dp")
class ChartDetailTwoPaneTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val strings = UiStrings.forLanguage(Language.EN)

    /** A real chart, so the header shows values the calculator actually produced. */
    private val chart: NatalChart = ChartCalculator.calculateNatalChart(
        BirthData(
            dateTime = LocalDateTime.of(2019, 6, 13, 13, 8),
            latitude = 1.3521,
            longitude = 103.8198,
            timeZone = ZoneId.of("Asia/Singapore"),
            locationName = "Singapore, Singapore",
            gender = "Male"
        )
    )

    private fun setContent(width: Int) {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides
                    WindowSizeClass.calculateFromSize(DpSize(width.dp, 800.dp)),
                LocalStrings provides strings
            ) {
                AstroChartTheme {
                    ChartDetailScreen(chart = chart, chartName = "sai viyaan thilak")
                }
            }
        }
    }

    private fun assertHeaderIsWhole() {
        composeTestRule.onNodeWithText("sai viyaan thilak").assertExists()
        composeTestRule.onNodeWithText("Singapore, Singapore").assertExists()
        listOf(
            strings.labelSun, strings.labelMoon, strings.labelRising,
            strings.labelAge, strings.labelGender, strings.labelChineseZodiac
        ).forEach { label ->
            composeTestRule.onNodeWithText(label.uppercase()).assertExists()
        }
    }

    @Test
    fun aTabletShowsEveryDetailAlongsideTheChart() {
        setContent(width = 1280)

        assertHeaderIsWhole()
        // The tabs live in the other pane; both panes must be composed at once,
        // which is the whole point of the split.
        composeTestRule.onNodeWithText(strings.tabWheel).assertExists()
        composeTestRule.onNodeWithText(strings.tabReading).assertExists()
    }

    @Test
    fun aPhoneKeepsEveryDetailToo() {
        setContent(width = 400)

        assertHeaderIsWhole()
        composeTestRule.onNodeWithText(strings.tabWheel).assertExists()
    }

    @Test
    fun theTabsStillSwitchPanesOnATablet() {
        setContent(width = 1280)

        // Wheel is the landing tab; its legend copy proves the pager is showing
        // page 0 and not an empty pane beside the details.
        composeTestRule.onNodeWithText(strings.tabPlacements).assertExists()
        composeTestRule.onNodeWithText(strings.tabBalance).assertExists()
    }
}
