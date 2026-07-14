package com.astrochart.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.astrochart.core.models.NatalChart
import com.astrochart.data.LocationCatalog
import com.astrochart.data.LocationOption
import com.astrochart.data.TimeZoneCatalog
import com.astrochart.ui.components.LabeledDropdown
import com.astrochart.ui.viewmodel.BirthInputViewModel
import java.time.Month
import java.time.Year
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun BirthInputScreen(
    viewModel: BirthInputViewModel,
    onChartCalculated: (NatalChart, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var year by remember { mutableStateOf(2000) }
    var month by remember { mutableStateOf(1) }
    var day by remember { mutableStateOf(1) }
    var hour by remember { mutableStateOf(12) }
    var minute by remember { mutableStateOf(0) }
    var location by remember { mutableStateOf<LocationOption?>(null) }
    var timeZone by remember { mutableStateOf("UTC") }

    val years = remember { (1900..Year.now().value).toList().reversed() }
    val daysInMonth = remember(year, month) { YearMonth.of(year, month).lengthOfMonth() }
    // Keep the selected day valid when month/year change (e.g. 31 -> Feb).
    LaunchedEffect(daysInMonth) { if (day > daysInMonth) day = daysInMonth }
    val days = remember(daysInMonth) { (1..daysInMonth).toList() }
    val timeZones = remember(timeZone) { TimeZoneCatalog.zonesIncluding(timeZone) }

    val uiState by viewModel.uiState.collectAsState()
    val isLoading = uiState is BirthInputViewModel.BirthInputUiState.Loading
    val canSubmit = name.isNotBlank() && location != null && !isLoading

    LaunchedEffect(uiState) {
        (uiState as? BirthInputViewModel.BirthInputUiState.Success)?.let {
            onChartCalculated(it.chart, name.trim())
            viewModel.reset()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            singleLine = true,
            isError = name.isBlank(),
            supportingText = { if (name.isBlank()) Text("Required — the chart is saved under this name") },
            modifier = Modifier.fillMaxWidth()
        )

        SectionLabel("Date of birth")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LabeledDropdown(
                label = "Day",
                options = days,
                selected = day,
                optionLabel = { it.toString() },
                onSelected = { day = it },
                modifier = Modifier.weight(1f)
            )
            LabeledDropdown(
                label = "Month",
                options = (1..12).toList(),
                selected = month,
                optionLabel = { Month.of(it).getDisplayName(TextStyle.FULL, Locale.getDefault()) },
                onSelected = { month = it },
                modifier = Modifier.weight(1.4f)
            )
            LabeledDropdown(
                label = "Year",
                options = years,
                selected = year,
                optionLabel = { it.toString() },
                onSelected = { year = it },
                modifier = Modifier.weight(1.2f)
            )
        }

        SectionLabel("Time of birth (24-hour)")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LabeledDropdown(
                label = "Hour",
                options = (0..23).toList(),
                selected = hour,
                optionLabel = { it.toString().padStart(2, '0') },
                onSelected = { hour = it },
                modifier = Modifier.weight(1f)
            )
            LabeledDropdown(
                label = "Minute",
                options = (0..59).toList(),
                selected = minute,
                optionLabel = { it.toString().padStart(2, '0') },
                onSelected = { minute = it },
                modifier = Modifier.weight(1f)
            )
        }

        SectionLabel("Place of birth")
        LabeledDropdown(
            label = "Location",
            options = LocationCatalog.locations,
            selected = location,
            optionLabel = { it.displayName },
            onSelected = {
                location = it
                timeZone = it.zoneId
            },
            placeholder = "Select a city",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LabeledDropdown(
            label = "Time zone",
            options = timeZones,
            selected = timeZone,
            optionLabel = { it },
            onSelected = { timeZone = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        (uiState as? BirthInputViewModel.BirthInputUiState.Error)?.let {
            Text(
                text = it.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // The button is always present; it is only disabled until the form is
        // valid, so it never disappears on bad input.
        Button(
            onClick = {
                val loc = location ?: return@Button
                viewModel.calculateAndSave(
                    name = name,
                    year = year,
                    month = month,
                    day = day,
                    hour = hour,
                    minute = minute,
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    timeZoneId = timeZone,
                    locationName = loc.displayName
                )
            },
            enabled = canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Calculate My Chart")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Spacer(modifier = Modifier.height(20.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(8.dp))
}
