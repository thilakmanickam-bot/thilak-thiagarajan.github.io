package com.astrochart.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.astrochart.data.db.entities.SavedChartEntity
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")

@Composable
fun SavedChartsScreen(
    charts: List<SavedChartEntity>,
    onChartSelected: (Long) -> Unit,
    onRename: (Long, String) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (charts.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No saved charts yet. Calculate a chart and it will be saved here automatically.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(charts, key = { it.id }) { chart ->
            SavedChartCard(
                chart = chart,
                onOpen = { onChartSelected(chart.id) },
                onRename = { newName -> onRename(chart.id, newName) },
                onDelete = { onDelete(chart.id) }
            )
        }
    }
}

@Composable
private fun SavedChartCard(
    chart: SavedChartEntity,
    onOpen: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showRename by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpen)
                    .padding(16.dp)
            ) {
                Text(
                    text = chart.name.ifBlank { "Untitled chart" },
                    style = MaterialTheme.typography.titleMedium
                )
                if (chart.locationName.isNotBlank()) {
                    Text(text = chart.locationName, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = chart.birthDateTime.format(dateFormatter),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { showRename = true }) { Text("Rename") }
                TextButton(onClick = { showDelete = true }) { Text("Delete") }
            }
        }
    }

    if (showRename) {
        RenameDialog(
            initial = chart.name,
            onConfirm = {
                onRename(it)
                showRename = false
            },
            onDismiss = { showRename = false }
        )
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete chart?") },
            text = { Text("\"${chart.name.ifBlank { "Untitled chart" }}\" will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDelete = false
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun RenameDialog(
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename chart") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim()) },
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
