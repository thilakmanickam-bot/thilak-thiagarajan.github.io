package com.astrochart.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.astrochart.core.i18n.Translations
import com.astrochart.core.interpret.CompatibilityResult
import com.astrochart.core.panchangam.PanchangamNames
import com.astrochart.data.db.entities.SavedChartEntity
import com.astrochart.ui.components.CelestialCard
import com.astrochart.ui.components.EyebrowLabel
import com.astrochart.ui.components.LabeledDropdown
import com.astrochart.ui.components.SectionDivider
import com.astrochart.ui.i18n.CompatibilityStrings
import com.astrochart.ui.i18n.LocalLanguage
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.GoldLight
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary

@Composable
fun CompatibilityScreen(
    charts: List<SavedChartEntity>,
    result: CompatibilityResult?,
    onCompute: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalLanguage.current
    val cs = remember(lang) { CompatibilityStrings.forLanguage(lang) }

    var selectedA by remember { mutableStateOf<SavedChartEntity?>(null) }
    var selectedB by remember { mutableStateOf<SavedChartEntity?>(null) }

    LaunchedEffect(selectedA, selectedB) {
        val a = selectedA
        val b = selectedB
        if (a != null && b != null && a.id != b.id) onCompute(a.id, b.id)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        if (charts.size < 2) {
            Text(
                text = cs.notEnoughCharts,
                style = MaterialTheme.typography.bodyLarge,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp)
            )
            return@Column
        }

        Text(
            text = cs.choose,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
        Spacer(Modifier.height(12.dp))

        LabeledDropdown(
            label = cs.personA,
            options = charts,
            selected = selectedA,
            optionLabel = { it.name },
            onSelected = { selectedA = it },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        LabeledDropdown(
            label = cs.personB,
            options = charts,
            selected = selectedB,
            optionLabel = { it.name },
            onSelected = { selectedB = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        val a = selectedA
        val b = selectedB
        when {
            a == null || b == null -> Unit
            a.id == b.id -> HintText(cs.needTwo)
            result != null -> ResultCard(result, cs, lang)
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun HintText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = TextMuted,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ResultCard(
    result: CompatibilityResult,
    cs: CompatibilityStrings,
    lang: com.astrochart.core.i18n.Language
) {
    CelestialCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(result.nameA, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Icon(
                Icons.Filled.Favorite,
                contentDescription = null,
                tint = GoldDeep,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Text(result.nameB, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "${result.overall}%",
            style = MaterialTheme.typography.displaySmall,
            color = GoldLight,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            text = cs.band(result.overall),
            style = MaterialTheme.typography.titleMedium,
            color = GoldDeep,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(16.dp))

        result.components.forEach { comp ->
            ScoreRow(cs.component(comp.key), comp.score)
        }

        Spacer(Modifier.height(12.dp))
        SectionDivider()
        Spacer(Modifier.height(12.dp))

        DetailRow(cs.sun, Translations.signName(result.sunA, lang), Translations.signName(result.sunB, lang))
        DetailRow(cs.moon, Translations.signName(result.moonA, lang), Translations.signName(result.moonB, lang))
        DetailRow(
            cs.birthStar,
            PanchangamNames.nakshatras[result.nakshatraA].get(lang),
            PanchangamNames.nakshatras[result.nakshatraB].get(lang)
        )
        DetailRow(cs.gana, cs.ganaNames[result.ganaA], cs.ganaNames[result.ganaB])
    }
}

@Composable
private fun ScoreRow(label: String, score: Int) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Text(text = "$score%", style = MaterialTheme.typography.bodyMedium, color = GoldDeep)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier.fillMaxWidth(),
            color = GoldDeep,
            trackColor = TextMuted.copy(alpha = 0.25f)
        )
    }
}

@Composable
private fun DetailRow(label: String, valueA: String, valueB: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        Box(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = "$valueA · $valueB",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                textAlign = TextAlign.End
            )
        }
    }
}
