package com.astrochart.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.astrochart.Features
import com.astrochart.core.i18n.Language
import com.astrochart.core.i18n.Translations
import com.astrochart.core.models.ChartStyle
import com.astrochart.core.panchangam.PanchangamNames
import com.astrochart.core.utils.ZodiacUtils
import com.astrochart.data.LocationCatalog
import com.astrochart.data.LocationOption
import com.astrochart.ui.components.CelestialCard
import com.astrochart.ui.components.EyebrowLabel
import com.astrochart.ui.components.LabeledDropdown
import com.astrochart.ui.i18n.LocalLanguage
import com.astrochart.ui.i18n.LocalStrings
import com.astrochart.ui.i18n.PanchangamStrings
import com.astrochart.ui.i18n.PrimaryProfile
import com.astrochart.ui.i18n.UiStrings
import com.astrochart.ui.theme.AppTheme
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary
import com.astrochart.ui.theme.fontFamilyForLanguage

/**
 * Settings / preferences: appearance (theme), chart type, defaults (language,
 * city) and the entry to Halo Premium. Every change is hoisted to [MainActivity]
 * so it applies live and is persisted.
 */
@Composable
fun SettingsScreen(
    currentStyle: ChartStyle,
    onStyleChange: (ChartStyle) -> Unit,
    currentLanguage: Language,
    onLanguageChange: (Language) -> Unit,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    currentLocation: LocationOption,
    onLocationChange: (LocationOption) -> Unit,
    primary: PrimaryProfile?,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToAccount: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val lang = LocalLanguage.current
    val ps = remember(lang) { PanchangamStrings.forLanguage(lang) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Appearance / theme.
        EyebrowLabel(text = strings.settingsAppearance)
        Spacer(Modifier.height(12.dp))
        CelestialCard {
            Text(strings.settingsTheme, style = MaterialTheme.typography.titleMedium, color = GoldDeep)
            Text(strings.settingsThemeDesc, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            Spacer(Modifier.height(8.dp))
            AppTheme.entries.forEach { theme ->
                ChoiceRow(
                    label = themeLabel(theme, lang),
                    selected = theme == currentTheme,
                    onSelect = { onThemeChange(theme) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Chart type.
        EyebrowLabel(text = strings.settingsPreferences)
        Spacer(Modifier.height(12.dp))
        CelestialCard {
            Text(strings.settingsChartType, style = MaterialTheme.typography.titleMedium, color = GoldDeep)
            Text(strings.settingsChartTypeDesc, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            Spacer(Modifier.height(8.dp))
            ChartStyle.entries.forEach { style ->
                ChoiceRow(
                    label = Translations.chartStyleName(style, lang),
                    selected = style == currentStyle,
                    onSelect = { onStyleChange(style) }
                )
            }
            // North Indian isn't implemented yet (no ChartStyle value to select) —
            // shown disabled here so it reads as "coming soon," not missing.
            Row(
                modifier = Modifier.fillMaxWidth().alpha(0.45f).padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.settingsChartTypeNorthIndianSoon,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMuted,
                    modifier = Modifier.padding(start = 40.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Defaults: language + panchangam city.
        EyebrowLabel(text = strings.settingsDefaults)
        Spacer(Modifier.height(12.dp))
        CelestialCard {
            Text(strings.languageLabel, style = MaterialTheme.typography.titleMedium, color = GoldDeep)
            Spacer(Modifier.height(6.dp))
            Language.entries.forEach { l ->
                ChoiceRow(
                    label = l.displayName,
                    selected = l == currentLanguage,
                    onSelect = { onLanguageChange(l) },
                    fontFamily = fontFamilyForLanguage(l)
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(strings.settingsCity, style = MaterialTheme.typography.titleMedium, color = GoldDeep)
            Spacer(Modifier.height(6.dp))
            LabeledDropdown(
                label = ps.location,
                options = LocationCatalog.locations,
                selected = currentLocation,
                optionLabel = { it.displayName },
                onSelected = onLocationChange,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(24.dp))

        // Primary profile — seeds Rasi Palan and the daily notification.
        EyebrowLabel(text = strings.settingsPrimary)
        Spacer(Modifier.height(12.dp))
        PrimaryProfileCard(
            strings = strings,
            lang = lang,
            primary = primary,
            onNavigateToEditProfile = onNavigateToEditProfile
        )

        Spacer(Modifier.height(24.dp))

        // Account (login / cloud sync) — shown only when the feature is enabled.
        if (Features.AUTH_ENABLED) {
            EyebrowLabel(text = strings.settingsAccountRow)
            Spacer(Modifier.height(12.dp))
            CelestialCard(modifier = Modifier.clickable(onClick = onNavigateToAccount)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = GoldDeep)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(strings.settingsAccountRow, style = MaterialTheme.typography.titleMedium, color = GoldDeep)
                        Text(strings.settingsAccountDesc, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted)
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // Premium entry.
        EyebrowLabel(text = strings.settingsPremiumRow)
        Spacer(Modifier.height(12.dp))
        CelestialCard(modifier = Modifier.clickable(onClick = onNavigateToPremium)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = GoldDeep)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(strings.premiumEntry, style = MaterialTheme.typography.titleMedium, color = GoldDeep)
                    Text(strings.premiumComingSoon, style = MaterialTheme.typography.bodySmall, color = GoldDeep)
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted)
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            text = "Worldwide location search data © GeoNames.org, CC BY 4.0",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Fonts: Noto Sans family © Google, SIL Open Font License 1.1",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        Spacer(Modifier.height(20.dp))
    }
}

/**
 * Read-only summary of the primary profile: rasi/nakshatra are derived from
 * real birth data (see [com.astrochart.ui.screens.OnboardingProfileStep]), so
 * editing them by hand here would let the two drift out of sync — tapping
 * this card instead reopens the same full birth-detail step the onboarding
 * wizard uses, prefilled with what's already saved.
 */
@Composable
private fun PrimaryProfileCard(
    strings: UiStrings,
    lang: Language,
    primary: PrimaryProfile?,
    onNavigateToEditProfile: () -> Unit
) {
    val signs = ZodiacUtils.getAllSigns()
    CelestialCard(modifier = Modifier.clickable(onClick = onNavigateToEditProfile)) {
        Text(strings.settingsPrimary, style = MaterialTheme.typography.titleMedium, color = GoldDeep)
        Text(strings.settingsPrimaryDesc, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        Spacer(Modifier.height(10.dp))
        if (primary != null) {
            Text(primary.name, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
            Text(
                text = "${strings.settingsPrimaryRasi}: ${Translations.signName(signs[primary.rasi], lang)}" +
                    "  ·  ${strings.settingsPrimaryNak}: ${PanchangamNames.nakshatras[primary.nakshatra].get(lang)}",
                style = MaterialTheme.typography.bodyMedium,
                color = GoldDeep
            )
        } else {
            Text(strings.settingsPrimaryEmpty, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(strings.settingsPrimaryEdit, style = MaterialTheme.typography.labelLarge, color = GoldDeep)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = GoldDeep)
        }
    }
}

private fun themeLabel(theme: AppTheme, lang: Language): String = when (lang) {
    Language.TA -> theme.labelTa
    Language.ZH -> theme.labelZh
    Language.HI -> theme.labelHi
    Language.TE -> theme.labelTe
    Language.KN -> theme.labelKn
    Language.ML -> theme.labelMl
    Language.MR -> theme.labelMr
    else -> theme.labelEn
}

/**
 * Radio-button list row, reused by the onboarding wizard's own choice steps.
 * [fontFamily] overrides the row's font — used only by the language list, so
 * each language's own name renders in its own bundled script font regardless
 * of which language is currently active (see LocalizedFonts.kt); every other
 * caller leaves it null and inherits the ambient typography as before.
 */
@Composable
internal fun ChoiceRow(label: String, selected: Boolean, onSelect: () -> Unit, fontFamily: FontFamily? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = GoldDeep, unselectedColor = TextMuted)
        )
        Text(
            text = label,
            style = if (fontFamily != null) {
                MaterialTheme.typography.bodyLarge.copy(fontFamily = fontFamily)
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = if (selected) TextPrimary else TextMuted
        )
    }
}
