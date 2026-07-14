package com.astrochart.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.astrochart.core.models.Aspect
import com.astrochart.core.models.NatalChart
import com.astrochart.core.models.PlanetaryPosition
import com.astrochart.ui.components.CelestialCard
import com.astrochart.ui.components.LocalBackgroundMotion
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

    val tabs = listOf("Placements", "Aspects", "Balance")
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

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            contentColor = GoldDeep
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
                0 -> PlacementsTab(chart)
                1 -> AspectsTab(chart)
                else -> BalanceTab(chart)
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
