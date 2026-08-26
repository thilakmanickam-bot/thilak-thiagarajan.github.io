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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.astrochart.core.i18n.Translations
import com.astrochart.core.models.ChartStyle
import com.astrochart.ui.components.CelestialCard
import com.astrochart.ui.components.EyebrowLabel
import com.astrochart.ui.i18n.LocalLanguage
import com.astrochart.ui.i18n.LocalStrings
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary

/**
 * Settings / preferences. Currently hosts the chart-type chooser and the entry
 * point to Halo Premium. The chosen [ChartStyle] is hoisted to [MainActivity]
 * so the change is applied live everywhere and persisted.
 */
@Composable
fun SettingsScreen(
    currentStyle: ChartStyle,
    onStyleChange: (ChartStyle) -> Unit,
    onNavigateToPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val lang = LocalLanguage.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        EyebrowLabel(text = strings.settingsPreferences)
        Spacer(modifier = Modifier.height(12.dp))

        CelestialCard {
            Text(
                text = strings.settingsChartType,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = strings.settingsChartTypeDesc,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            ChartStyle.entries.forEach { style ->
                ChartStyleRow(
                    label = Translations.chartStyleName(style, lang),
                    selected = style == currentStyle,
                    onSelect = { onStyleChange(style) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        EyebrowLabel(text = strings.settingsPremiumRow)
        Spacer(modifier = Modifier.height(12.dp))

        CelestialCard(
            modifier = Modifier.clickable(onClick = onNavigateToPremium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.WorkspacePremium,
                    contentDescription = null,
                    tint = GoldDeep
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.premiumEntry,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = strings.premiumComingSoon,
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldDeep
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = TextMuted
                )
            }
        }
    }
}

@Composable
private fun ChartStyleRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
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
            colors = RadioButtonDefaults.colors(
                selectedColor = GoldDeep,
                unselectedColor = TextMuted
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) TextPrimary else TextMuted
        )
    }
}
