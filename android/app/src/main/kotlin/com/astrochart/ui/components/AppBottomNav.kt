package com.astrochart.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.astrochart.ui.i18n.LocalLanguage
import com.astrochart.ui.i18n.LocalStrings
import com.astrochart.ui.i18n.PanchangamStrings
import com.astrochart.ui.i18n.RasiStrings
import com.astrochart.ui.i18n.SankalpaStrings
import com.astrochart.ui.theme.CardBorder
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.TextMuted

/**
 * The five top-level places in the app. Each carries the route it navigates to
 * and the set of routes that should light it up — a section stays selected
 * while you are anywhere inside it, so opening a rasi's horoscope keeps Rasi
 * Palan lit rather than dropping the selection to nothing.
 */
enum class NavSection(val route: String, val ownedRoutes: Set<String>) {
    HOME("home", setOf("home")),
    CALENDAR("panchangam", setOf("panchangam", "calendar")),
    SANKALPA("sankalpa", setOf("sankalpa")),
    RASI(
        "rasi_hub",
        setOf("rasi_hub", "rasi_signs", "rasi_horoscope", "rasi_info", "nakshatra_list")
    ),
    SETTINGS("settings", setOf("settings", "edit_profile", "premium", "account"));

    companion object {
        /** The section owning [route], or null for a screen that belongs to none. */
        fun forRoute(route: String?): NavSection? =
            entries.firstOrNull { route != null && route in it.ownedRoutes }
    }
}

/**
 * The bottom navigation bar. Calendar and Settings used to be icon actions in
 * the top app bar; they live here now, alongside sankalpa, so the app's
 * top-level surfaces are all reachable in one tap from anywhere.
 *
 * This is the bottom-most element of the Scaffold's bottom bar, so it is the
 * one that consumes the navigation-bar inset — see [AdBanner], which is asked
 * to skip its own.
 */
@Composable
fun AppBottomNav(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val lang = LocalLanguage.current
    val panchangam = remember(lang) { PanchangamStrings.forLanguage(lang) }
    val rasi = remember(lang) { RasiStrings.forLanguage(lang) }
    val sankalpa = remember(lang) { SankalpaStrings.forLanguage(lang) }

    val selected = NavSection.forRoute(currentRoute)

    NavigationBar(
        modifier = modifier.navigationBarsPadding(),
        containerColor = Color.Transparent,
        contentColor = TextMuted,
        tonalElevation = 0.dp
    ) {
        Item(NavSection.HOME, Icons.Filled.Home, strings.navHome, selected, onNavigate)
        Item(
            NavSection.CALENDAR, Icons.Filled.CalendarMonth,
            panchangam.calendarLabel, selected, onNavigate
        )
        Item(
            NavSection.SANKALPA, Icons.Filled.SelfImprovement,
            sankalpa.navLabel, selected, onNavigate
        )
        Item(NavSection.RASI, Icons.Filled.Stars, rasi.title, selected, onNavigate)
        Item(
            NavSection.SETTINGS, Icons.Filled.Settings,
            strings.settingsLabel, selected, onNavigate
        )
    }
}

/**
 * One destination. The label is capped at a single line: several of the eight
 * languages render these words far longer than the English (Malayalam
 * "ക്രമീകരണങ്ങൾ" for Settings), and five items across a phone leaves each about
 * 70dp — without the cap the bar grows a second line for some languages and not
 * others, and the icons stop lining up.
 */
@Composable
private fun RowScope.Item(
    section: NavSection,
    icon: ImageVector,
    label: String,
    selected: NavSection?,
    onNavigate: (String) -> Unit
) {
    val isSelected = selected == section
    NavigationBarItem(
        selected = isSelected,
        onClick = { if (!isSelected) onNavigate(section.route) },
        icon = { Icon(imageVector = icon, contentDescription = label) },
        label = {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall
            )
        },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = GoldDeep,
            selectedTextColor = GoldDeep,
            indicatorColor = CardBorder,
            unselectedIconColor = TextMuted,
            unselectedTextColor = TextMuted
        )
    )
}
