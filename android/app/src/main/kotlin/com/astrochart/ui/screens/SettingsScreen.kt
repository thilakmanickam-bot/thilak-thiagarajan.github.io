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
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.astrochart.ui.theme.CardBorder
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary

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
    onPrimaryChange: (PrimaryProfile?) -> Unit,
    onNavigateToPremium: () -> Unit,
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
            Text(strings.settingsTheme, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
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
            Text(strings.settingsChartType, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(strings.settingsChartTypeDesc, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            Spacer(Modifier.height(8.dp))
            ChartStyle.entries.forEach { style ->
                ChoiceRow(
                    label = Translations.chartStyleName(style, lang),
                    selected = style == currentStyle,
                    onSelect = { onStyleChange(style) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Defaults: language + panchangam city.
        EyebrowLabel(text = strings.settingsDefaults)
        Spacer(Modifier.height(12.dp))
        CelestialCard {
            Text(strings.languageLabel, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(6.dp))
            Language.entries.forEach { l ->
                ChoiceRow(
                    label = l.displayName,
                    selected = l == currentLanguage,
                    onSelect = { onLanguageChange(l) }
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(strings.settingsCity, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
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
            onPrimaryChange = onPrimaryChange
        )

        Spacer(Modifier.height(24.dp))

        // Premium entry.
        EyebrowLabel(text = strings.settingsPremiumRow)
        Spacer(Modifier.height(12.dp))
        CelestialCard(modifier = Modifier.clickable(onClick = onNavigateToPremium)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = GoldDeep)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(strings.premiumEntry, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text(strings.premiumComingSoon, style = MaterialTheme.typography.bodySmall, color = GoldDeep)
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted)
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun PrimaryProfileCard(
    strings: UiStrings,
    lang: Language,
    primary: PrimaryProfile?,
    onPrimaryChange: (PrimaryProfile?) -> Unit
) {
    val signs = ZodiacUtils.getAllSigns()
    var name by remember(primary) { mutableStateOf(primary?.name ?: "") }
    var rasi by remember(primary) { mutableStateOf(primary?.rasi) }
    var nak by remember(primary) { mutableStateOf(primary?.nakshatra) }

    LaunchedEffect(name, rasi, nak) {
        val r = rasi
        val n = nak
        if (r != null && n != null) onPrimaryChange(PrimaryProfile(name.trim(), r, n))
    }

    CelestialCard {
        Text(strings.settingsPrimary, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Text(strings.settingsPrimaryDesc, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(strings.settingsPrimary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldDeep,
                unfocusedBorderColor = CardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = GoldDeep
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        LabeledDropdown(
            label = strings.settingsPrimaryRasi,
            options = signs.indices.toList(),
            selected = rasi,
            optionLabel = { Translations.signName(signs[it], lang) },
            onSelected = { rasi = it },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        LabeledDropdown(
            label = strings.settingsPrimaryNak,
            options = PanchangamNames.nakshatras.indices.toList(),
            selected = nak,
            optionLabel = { PanchangamNames.nakshatras[it].get(lang) },
            onSelected = { nak = it },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun themeLabel(theme: AppTheme, lang: Language): String = when (lang) {
    Language.EN -> theme.labelEn
    Language.TA -> theme.labelTa
    Language.ZH -> theme.labelZh
}

@Composable
private fun ChoiceRow(label: String, selected: Boolean, onSelect: () -> Unit) {
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
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) TextPrimary else TextMuted
        )
    }
}
