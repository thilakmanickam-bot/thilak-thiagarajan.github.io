package com.astrochart.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.astrochart.core.i18n.Translations
import com.astrochart.core.models.NatalChart
import com.astrochart.core.utils.SouthIndianChart
import com.astrochart.ui.i18n.LocalLanguage
import com.astrochart.ui.i18n.LocalStrings
import com.astrochart.ui.theme.CardBorder
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.GoldLight
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary

/**
 * The South-Indian (Tamil) square chart — *rasi koshtam*. Twelve fixed sign
 * cells around a merged centre; each body is written into the cell of the sign
 * it falls in. Sign names and body labels are localized at render time, so the
 * same [chart] renders correctly in every language. Placement is delegated to
 * the pure [SouthIndianChart] logic.
 */
@Composable
fun SouthIndianChartView(
    chart: NatalChart,
    chartName: String,
    modifier: Modifier = Modifier
) {
    val lang = LocalLanguage.current
    val strings = LocalStrings.current
    val cells = remember(chart) { SouthIndianChart.cells(chart, includeAscendant = true) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        val cell = maxWidth / 4
        val border = CardBorder

        // Twelve perimeter sign cells.
        cells.forEach { c ->
            Box(
                modifier = Modifier
                    .offset(x = cell * c.col, y = cell * c.row)
                    .size(cell)
                    .border(1.dp, border)
                    .padding(3.dp)
            ) {
                Column {
                    Text(
                        text = Translations.signName(c.sign, lang),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        maxLines = 1
                    )
                    if (c.bodies.isNotEmpty()) {
                        Text(
                            text = c.bodies.joinToString("  ") { Translations.bodyAbbr(it, lang) },
                            style = MaterialTheme.typography.bodySmall,
                            color = GoldLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Merged 2×2 centre with the chart's identity + lagnam (ascendant) sign.
        Box(
            modifier = Modifier
                .offset(
                    x = cell * SouthIndianChart.CENTER_COL,
                    y = cell * SouthIndianChart.CENTER_ROW
                )
                .size(cell * SouthIndianChart.CENTER_SPAN)
                .border(1.dp, GoldDeep.copy(alpha = 0.5f))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (chartName.isNotBlank()) {
                    Text(
                        text = chartName,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
                Text(
                    text = strings.labelRising,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Text(
                    text = Translations.signName(chart.ascendant.sign, lang),
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldDeep,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
