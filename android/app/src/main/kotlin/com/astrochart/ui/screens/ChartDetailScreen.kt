package com.astrochart.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.astrochart.core.models.Aspect
import com.astrochart.core.models.NatalChart
import com.astrochart.core.models.PlanetaryPosition
import com.astrochart.ui.viewmodel.ChartViewModel

@Composable
fun ChartDetailScreen(
    chart: NatalChart?,
    viewModel: ChartViewModel,
    modifier: Modifier = Modifier
) {
    if (chart == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text("No chart data available")
        }
        return
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Positions", "Aspects", "Balance")

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(tab) }
                )
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> {
                    item {
                        Text(
                            "Ascendant",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    item {
                        PositionCard(chart.ascendant)
                    }
                    item {
                        Text(
                            "Planetary Positions",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    items(chart.planets) { planet ->
                        PositionCard(planet)
                    }
                }

                1 -> {
                    item {
                        Text(
                            "Aspects",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    items(chart.aspects) { aspect ->
                        AspectCard(aspect)
                    }
                }

                2 -> {
                    item {
                        Text(
                            "Element Distribution",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    items(chart.balance.elements.toList()) { (element, count) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(element, modifier = Modifier.weight(1f))
                            Text("$count planets")
                        }
                    }
                    item {
                        Text(
                            "Modality Distribution",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    items(chart.balance.modalities.toList()) { (modality, count) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(modality, modifier = Modifier.weight(1f))
                            Text("$count planets")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PositionCard(planet: PlanetaryPosition) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = planet.name,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = planet.label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "House ${planet.house}",
                style = MaterialTheme.typography.bodySmall
            )
            if (planet.retrograde) {
                Text(
                    text = "℞ Retrograde",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AspectCard(aspect: Aspect) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${aspect.bodyA} ${aspect.type} ${aspect.bodyB}",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "Orb: ${String.format("%.2f°", aspect.orb)}",
                style = MaterialTheme.typography.bodySmall
            )
            if (aspect.interpretation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = aspect.interpretation,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
