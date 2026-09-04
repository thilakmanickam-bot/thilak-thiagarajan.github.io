package com.astrochart.uitest

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.astrochart.core.i18n.Language
import com.astrochart.ui.components.AppBottomNav
import com.astrochart.ui.components.NavSection
import com.astrochart.ui.i18n.LocalLanguage
import com.astrochart.ui.i18n.LocalStrings
import com.astrochart.ui.i18n.PanchangamStrings
import com.astrochart.ui.i18n.RasiStrings
import com.astrochart.ui.i18n.SankalpaStrings
import com.astrochart.ui.i18n.UiStrings
import com.astrochart.ui.theme.AstroChartTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The bottom bar is how Calendar, Sankalpa and Settings are now reached at all —
 * they were removed from the top app bar when it landed — so a destination
 * missing here is a feature that has become unreachable, not a cosmetic slip.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AppBottomNavTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val lang = Language.EN
    private val strings = UiStrings.forLanguage(lang)
    private val panchangam = PanchangamStrings.forLanguage(lang)
    private val rasi = RasiStrings.forLanguage(lang)
    private val sankalpa = SankalpaStrings.forLanguage(lang)

    private fun setContent(route: String, onNavigate: (String) -> Unit = {}) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalStrings provides strings, LocalLanguage provides lang) {
                AstroChartTheme {
                    AppBottomNav(currentRoute = route, onNavigate = onNavigate)
                }
            }
        }
    }

    @Test
    fun everyDestinationIsPresent() {
        setContent(route = "home")
        listOf(
            strings.navHome, panchangam.calendarLabel, sankalpa.navLabel,
            rasi.title, strings.settingsLabel
        ).forEach { composeTestRule.onNodeWithText(it).assertExists() }
    }

    @Test
    fun tappingADestinationReportsItsRoute() {
        var navigated: String? = null
        setContent(route = "home") { navigated = it }

        composeTestRule.onNodeWithText(sankalpa.navLabel).performClick()
        assertEquals("sankalpa", navigated)
    }

    @Test
    fun tappingTheCurrentDestinationDoesNothing() {
        // Re-tapping the tab you are on must not navigate. With
        // launchSingleTop this would be harmless, but reporting it anyway
        // would push a duplicate entry on hosts that handle it differently —
        // and it is free to just not fire.
        var navigations = 0
        setContent(route = "home") { navigations++ }

        composeTestRule.onNodeWithText(strings.navHome).performClick()
        assertEquals(0, navigations)
    }

    @Test
    fun aSectionStaysSelectedAnywhereInsideIt() {
        // Opening a rasi's horoscope is still "in" Rasi Palan, so the bar must
        // not drop its selection to nothing on a nested route.
        assertEquals(NavSection.RASI, NavSection.forRoute("rasi_horoscope"))
        assertEquals(NavSection.RASI, NavSection.forRoute("nakshatra_list"))
        assertEquals(NavSection.CALENDAR, NavSection.forRoute("calendar"))
        assertEquals(NavSection.SETTINGS, NavSection.forRoute("premium"))
        assertEquals(NavSection.SANKALPA, NavSection.forRoute("sankalpa"))
    }

    @Test
    fun aScreenThatIsNotATabSelectsNothing() {
        // chart_detail and birth_input are pushed on top of a tab, not tabs
        // themselves. MainActivity keys the back arrow off exactly this, so a
        // stray match here would hide the only way back out of them.
        listOf("chart_detail", "birth_input", "saved_charts", "chat").forEach {
            assertNull(NavSection.forRoute(it), "$it should not be a tab")
        }
        assertNull(NavSection.forRoute(null))
    }

    @Test
    fun everySectionRouteIsOneItOwns() {
        // A section whose own route is not in its ownedRoutes would navigate
        // somewhere it then fails to highlight.
        NavSection.entries.forEach { section ->
            assertTrue(
                section.route in section.ownedRoutes,
                "${section.name} navigates to ${section.route}, which it does not own"
            )
        }
    }
}
