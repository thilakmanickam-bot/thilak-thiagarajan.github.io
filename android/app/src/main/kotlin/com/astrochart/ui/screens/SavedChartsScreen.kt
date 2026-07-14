package com.astrochart.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.astrochart.data.db.entities.SavedChartEntity
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

@Composable
fun SavedChartsScreen(
    charts: List<SavedChartEntity>,
    onChartSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Saved Charts",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (charts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No saved charts yet. Calculate a chart and save it to see it here.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(charts, key = { it.id }) { chart ->
                    SavedChartCard(chart = chart, onClick = { onChartSelected(chart.id) })
                }
            }
        }
    }
}

@Composable
private fun SavedChartCard(chart: SavedChartEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = chart.name.ifBlank { "Untitled chart" },
                style = MaterialTheme.typography.titleMedium
            )
            if (chart.locationName.isNotBlank()) {
                Text(
                    text = chart.locationName,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = chart.birthDateTime.format(dateFormatter),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
