package com.astrochart.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.astrochart.core.i18n.Language
import com.astrochart.data.db.entities.SavedChartEntity
import com.astrochart.ui.components.CelestialCard
import com.astrochart.ui.i18n.LocalLanguage
import com.astrochart.ui.i18n.LocalStrings
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary
import java.time.format.DateTimeFormatter

private fun dateFormatter(lang: Language): DateTimeFormatter =
    DateTimeFormatter.ofPattern(
        if (lang == Language.ZH) "yyyy年M月d日 HH:mm" else "d MMM yyyy, HH:mm",
        lang.locale
    )

@Composable
fun SavedChartsScreen(
    charts: List<SavedChartEntity>,
    onChartSelected: (Long) -> Unit,
    onRename: (Long, String) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    if (charts.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = strings.noSaved,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
    val strings = LocalStrings.current
    val lang = LocalLanguage.current
    var showRename by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    CelestialCard(contentPadding = 0) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpen)
                    .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 8.dp)
            ) {
                Text(
                    text = chart.name.ifBlank { strings.untitled },
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                if (chart.locationName.isNotBlank()) {
                    Text(chart.locationName, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
                Text(
                    text = chart.birthDateTime.format(dateFormatter(lang)),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            IconButton(onClick = { showRename = true }) {
                Icon(Icons.Filled.Edit, contentDescription = strings.rename, tint = GoldDeep)
            }
            IconButton(onClick = { showDelete = true }) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = strings.delete, tint = GoldDeep)
            }
            Spacer(modifier = Modifier.width(4.dp))
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
            title = { Text(strings.deleteTitle) },
            text = { Text(strings.deleteMessage(chart.name.ifBlank { strings.untitled })) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDelete = false
                }) { Text(strings.delete) }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text(strings.cancel) }
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
    val strings = LocalStrings.current
    var name by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.renameTitle) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(strings.name) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim()) },
                enabled = name.isNotBlank()
            ) { Text(strings.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}
