package com.astrochart.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.astrochart.core.models.Aspect
import com.astrochart.core.models.NatalChart
import com.astrochart.core.models.PlanetaryPosition
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale

private val birthFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")

@Composable
fun ChartDetailScreen(
    chart: NatalChart?,
    chartName: String,
    modifier: Modifier = Modifier
) {
    if (chart == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val tabs = listOf("Placements", "Aspects", "Balance")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        ChartHeader(chart = chart, chartName = chartName)

        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(title) }
                )
            }
        }

        // Swipe left/right moves between tabs; the TabRow stays in sync.
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (chartName.isNotBlank()) {
                Text(
                    text = chartName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = chart.birthData.dateTime.format(birthFormatter),
                style = MaterialTheme.typography.bodyMedium
            )
            if (chart.birthData.locationName.isNotBlank()) {
                Text(
                    text = chart.birthData.locationName,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                sun?.let { KeyPlacement("Sun", it.sign) }
                moon?.let { KeyPlacement("Moon", it.sign) }
                KeyPlacement("Rising", chart.ascendant.sign)
            }
        }
    }
}

@Composable
private fun KeyPlacement(label: String, sign: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = sign,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PlacementsTab(chart: NatalChart) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = position.name,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(text = position.label, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "House ${position.house}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(chart.aspects) { aspect -> AspectRow(aspect) }
    }
}

@Composable
private fun AspectRow(aspect: Aspect) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${aspect.bodyA} ${aspect.type} ${aspect.bodyB}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "orb " + String.format(Locale.US, "%.1f°", aspect.orb),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (aspect.interpretation.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = aspect.interpretation,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun BalanceTab(chart: NatalChart) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = if (count == 1) "1 body" else "$count bodies",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TabHeading(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun EmptyTab(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
    }
}
