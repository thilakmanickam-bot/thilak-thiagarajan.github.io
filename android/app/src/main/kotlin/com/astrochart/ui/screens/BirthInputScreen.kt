package com.astrochart.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.astrochart.core.models.NatalChart
import com.astrochart.ui.viewmodel.BirthInputViewModel

@Composable
fun BirthInputScreen(
    viewModel: BirthInputViewModel,
    onChartCalculated: (NatalChart) -> Unit,
    modifier: Modifier = Modifier
) {
    var year by remember { mutableStateOf("2000") }
    var month by remember { mutableStateOf("1") }
    var day by remember { mutableStateOf("1") }
    var hour by remember { mutableStateOf("12") }
    var minute by remember { mutableStateOf("0") }
    var latitude by remember { mutableStateOf("0.0") }
    var longitude by remember { mutableStateOf("0.0") }
    var timeZone by remember { mutableStateOf("America/New_York") }
    var location by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is BirthInputViewModel.BirthInputUiState.Success) {
            onChartCalculated((uiState as BirthInputViewModel.BirthInputUiState.Success).chart)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Birth Information",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = year,
            onValueChange = { year = it },
            label = { Text("Year") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = month,
            onValueChange = { month = it },
            label = { Text("Month (1-12)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = day,
            onValueChange = { day = it },
            label = { Text("Day") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = hour,
            onValueChange = { hour = it },
            label = { Text("Hour (0-23)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = minute,
            onValueChange = { minute = it },
            label = { Text("Minute (0-59)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Location",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text("Latitude") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = longitude,
            onValueChange = { longitude = it },
            label = { Text("Longitude") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (uiState) {
            is BirthInputViewModel.BirthInputUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            is BirthInputViewModel.BirthInputUiState.Error -> {
                Text(
                    text = "Error: ${(uiState as BirthInputViewModel.BirthInputUiState.Error).message}",
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> {
                Button(
                    onClick = {
                        try {
                            viewModel.submitBirthData(
                                year = year.toInt(),
                                month = month.toInt(),
                                day = day.toInt(),
                                hour = hour.toInt(),
                                minute = minute.toInt(),
                                latitude = latitude.toDouble(),
                                longitude = longitude.toDouble(),
                                timeZoneId = timeZone,
                                locationName = location
                            )
                        } catch (e: Exception) {
                            // Error handling
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Calculate Chart")
                }
            }
        }
    }
}
