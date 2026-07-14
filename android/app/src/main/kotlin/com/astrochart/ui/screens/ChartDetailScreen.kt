package com.astrochart.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.astrochart.core.interpret.ChartReading
import com.astrochart.core.models.Aspect
import com.astrochart.core.models.NatalChart
import com.astrochart.core.models.PlanetaryPosition
import com.astrochart.ui.components.CelestialCard
import com.astrochart.ui.components.EyebrowLabel
import com.astrochart.ui.components.LocalBackgroundMotion
import com.astrochart.ui.components.NatalWheel
import com.astrochart.ui.components.SectionDivider
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale

private val birthFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChartDetailScreen(
    chart: NatalChart?,
    chartName: String,
    modifier: Modifier = Modifier
) {
    if (chart == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GoldDeep)
        }
        return
    }

    val tabs = listOf("Wheel", "Placements", "Aspects", "Balance", "Reading")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    // Feed the pager position into the animated background as a parallax offset.
    val motion = LocalBackgroundMotion.current
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage + pagerState.currentPageOffsetFraction }
            .collect { motion.parallax = it - 1f }
    }
    DisposableEffect(Unit) { onDispose { motion.parallax = 0f } }

    Column(modifier = modifier.fillMaxSize()) {
        ChartHeader(chart = chart, chartName = chartName)

        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            contentColor = GoldDeep,
            edgePadding = 12.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    selectedContentColor = GoldDeep,
                    unselectedContentColor = TextMuted,
                    text = { Text(title, style = MaterialTheme.typography.titleSmall) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> WheelTab(chart)
                1 -> PlacementsTab(chart)
                2 -> AspectsTab(chart)
                3 -> BalanceTab(chart)
                else -> ReadingTab(chart, chartName)
            }
        }
    }
}

@Composable
private fun WheelTab(chart: NatalChart) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        CelestialCard(contentPadding = 16) {
            NatalWheel(chart = chart, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(14.dp))
            WheelLegend()
        }
    }
}

@Composable
private fun WheelLegend() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Ascendant is at the 9 o'clock position; the zodiac runs anticlockwise.",
            style = MaterialTheme.typography.bodySmall, color = TextMuted)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("— harmonious (trine/sextile)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8FB8C8))
            Text("— challenging (square/opp.)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun ReadingTab(chart: NatalChart, chartName: String) {
    val sections = remember(chart, chartName) { ChartReading.build(chart, chartName) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(sections) { section ->
            CelestialCard {
                EyebrowLabel(text = section.title)
                Spacer(modifier = Modifier.height(6.dp))
                section.paragraphs.forEachIndexed { i, para ->
                    if (i > 0) Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = para,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartHeader(chart: NatalChart, chartName: String) {
    val sun = chart.planets.firstOrNull { it.name == "Sun" }
    val moon = chart.planets.firstOrNull { it.name == "Moon" }

    CelestialCard(
        modifier = Modifier.padding(16.dp),
        contentPadding = 18
    ) {
        if (chartName.isNotBlank()) {
            Text(
                text = chartName,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(
            text = chart.birthData.dateTime.format(birthFormatter),
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
        if (chart.birthData.locationName.isNotBlank()) {
            Text(
                text = chart.birthData.locationName,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            sun?.let { KeyPlacement("Sun", it.sign) }
            moon?.let { KeyPlacement("Moon", it.sign) }
            KeyPlacement("Rising", chart.ascendant.sign)
        }
    }
}

@Composable
private fun KeyPlacement(label: String, sign: String) {
    Column {
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = sign,
            style = MaterialTheme.typography.titleMedium,
            color = GoldDeep,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PlacementsTab(chart: NatalChart) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { TabHeading("Angles") }
        item { PlacementRow(chart.ascendant) }
        item { PlacementRow(chart.midheaven) }
        item { TabHeading("Planets") }
        items(chart.planets) { planet -> PlacementRow(planet) }
    }
}

@Composable
private fun PlacementRow(position: PlanetaryPosition) {
    CelestialCard(contentPadding = 14) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = position.name,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(text = position.label, style = MaterialTheme.typography.bodyMedium, color = GoldDeep)
                Text(
                    text = "House ${position.house}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun AspectsTab(chart: NatalChart) {
    if (chart.aspects.isEmpty()) {
        EmptyTab("No major aspects within orb for this chart.")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(chart.aspects) { aspect -> AspectRow(aspect) }
    }
}

@Composable
private fun AspectRow(aspect: Aspect) {
    CelestialCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${aspect.bodyA} ${aspect.type} ${aspect.bodyB}",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            Text(
                text = "orb " + String.format(Locale.US, "%.1f°", aspect.orb),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
        if (aspect.interpretation.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = aspect.interpretation,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun BalanceTab(chart: NatalChart) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { TabHeading("Elements") }
        items(chart.balance.elements.entries.toList()) { (element, count) ->
            BalanceRow(element, count)
        }
        item { TabHeading("Modalities") }
        items(chart.balance.modalities.entries.toList()) { (modality, count) ->
            BalanceRow(modality, count)
        }
    }
}

@Composable
private fun BalanceRow(label: String, count: Int) {
    CelestialCard(contentPadding = 14) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
            Text(
                text = if (count == 1) "1 body" else "$count bodies",
                style = MaterialTheme.typography.bodyMedium,
                color = GoldDeep
            )
        }
    }
}

@Composable
private fun TabHeading(text: String) {
    Column {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = GoldDeep
        )
        Spacer(modifier = Modifier.height(4.dp))
        SectionDivider()
        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
private fun EmptyTab(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
    }
}
