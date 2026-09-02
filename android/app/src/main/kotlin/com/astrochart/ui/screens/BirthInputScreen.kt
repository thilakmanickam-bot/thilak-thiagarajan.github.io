package com.astrochart.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.astrochart.core.models.NatalChart
import com.astrochart.data.LocationOption
import com.astrochart.data.TimeZoneCatalog
import com.astrochart.ui.components.EyebrowLabel
import com.astrochart.ui.components.GoldButton
import com.astrochart.ui.components.LabeledDropdown
import com.astrochart.ui.components.SearchableLocationField
import com.astrochart.ui.i18n.LocalLanguage
import com.astrochart.ui.i18n.LocalStrings
import com.astrochart.ui.theme.AstroError
import com.astrochart.ui.theme.CardBorder
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary
import com.astrochart.ui.viewmodel.BirthInputViewModel
import java.time.Month
import java.time.Year
import java.time.YearMonth
import java.time.format.TextStyle

@Composable
fun BirthInputScreen(
    viewModel: BirthInputViewModel,
    onChartCalculated: (NatalChart, String, LocationOption) -> Unit,
    initialName: String = "",
    initialGender: String = "",
    initialYear: Int = 2000,
    initialMonth: Int = 1,
    initialDay: Int = 1,
    initialHour: Int = 12,
    initialMinute: Int = 0,
    initialLocation: LocationOption? = null,
    initialTimeZone: String = "Asia/Singapore",
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val lang = LocalLanguage.current

    var name by remember { mutableStateOf(initialName) }
    var gender by remember { mutableStateOf(initialGender) } // canonical English: "", Female, Male, Other
    var year by remember { mutableStateOf(initialYear) }
    var month by remember { mutableStateOf(initialMonth) }
    var day by remember { mutableStateOf(initialDay) }
    var hour by remember { mutableStateOf(initialHour) }
    var minute by remember { mutableStateOf(initialMinute) }
    var location by remember { mutableStateOf(initialLocation) }
    var timeZone by remember { mutableStateOf(initialLocation?.zoneId ?: initialTimeZone) }

    // Canonical gender codes with localized labels for display.
    val genderOptions = listOf("Female", "Male", "Other")
    fun genderLabel(code: String): String = when (code) {
        "Female" -> strings.genderFemale
        "Male" -> strings.genderMale
        "Other" -> strings.genderOther
        else -> strings.genderUnset
    }

    val years = remember { (1900..Year.now().value).toList().reversed() }
    val daysInMonth = remember(year, month) { YearMonth.of(year, month).lengthOfMonth() }
    LaunchedEffect(daysInMonth) { if (day > daysInMonth) day = daysInMonth }
    val days = remember(daysInMonth) { (1..daysInMonth).toList() }
    val timeZones = remember(timeZone) { TimeZoneCatalog.zonesIncluding(timeZone) }

    val uiState by viewModel.uiState.collectAsState()
    val isLoading = uiState is BirthInputViewModel.BirthInputUiState.Loading
    val canSubmit = name.isNotBlank() && location != null && !isLoading
    // The location a calculation was submitted with, so the result callback
    // reports the exact place used even if `location` changes while loading.
    var submittedLocation by remember { mutableStateOf<LocationOption?>(null) }

    LaunchedEffect(uiState) {
        (uiState as? BirthInputViewModel.BirthInputUiState.Success)?.let {
            submittedLocation?.let { loc -> onChartCalculated(it.chart, name.trim(), loc) }
            viewModel.reset()
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GoldDeep,
        unfocusedBorderColor = CardBorder,
        errorBorderColor = AstroError,
        focusedLabelColor = GoldDeep,
        unfocusedLabelColor = TextMuted,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        cursorColor = GoldDeep
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        EyebrowLabel(text = strings.biEyebrow, icon = Icons.Filled.Person)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = strings.biHeading,
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(strings.name) },
            singleLine = true,
            isError = name.isBlank(),
            supportingText = { if (name.isBlank()) Text(strings.nameRequired) },
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LabeledDropdown(
            label = strings.gender,
            options = genderOptions,
            selected = gender.ifBlank { null },
            optionLabel = { genderLabel(it) },
            onSelected = { gender = it },
            placeholder = strings.genderUnset,
            modifier = Modifier.fillMaxWidth()
        )

        SectionLabel(strings.dob)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LabeledDropdown(
                label = strings.day,
                options = days,
                selected = day,
                optionLabel = { it.toString() },
                onSelected = { day = it },
                modifier = Modifier.weight(1f)
            )
            LabeledDropdown(
                label = strings.month,
                options = (1..12).toList(),
                selected = month,
                optionLabel = { Month.of(it).getDisplayName(TextStyle.SHORT, lang.locale) },
                onSelected = { month = it },
                modifier = Modifier.weight(1.4f)
            )
            LabeledDropdown(
                label = strings.year,
                options = years,
                selected = year,
                optionLabel = { it.toString() },
                onSelected = { year = it },
                modifier = Modifier.weight(1.2f)
            )
        }

        SectionLabel(strings.tob)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LabeledDropdown(
                label = strings.hour,
                options = (0..23).toList(),
                selected = hour,
                optionLabel = { it.toString().padStart(2, '0') },
                onSelected = { hour = it },
                modifier = Modifier.weight(1f)
            )
            LabeledDropdown(
                label = strings.minute,
                options = (0..59).toList(),
                selected = minute,
                optionLabel = { it.toString().padStart(2, '0') },
                onSelected = { minute = it },
                modifier = Modifier.weight(1f)
            )
        }

        SectionLabel(strings.pob)
        SearchableLocationField(
            label = strings.location,
            placeholder = strings.searchCityHint,
            selected = location,
            onSelected = {
                location = it
                timeZone = it.zoneId
            },
            noResultsText = strings.noLocationResults,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LabeledDropdown(
            label = strings.timeZone,
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
                color = AstroError,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        GoldButton(
            text = strings.navCalculate,
            onClick = {
                val loc = location ?: return@GoldButton
                submittedLocation = loc
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
                    locationName = loc.displayName,
                    gender = gender
                )
            },
            enabled = canSubmit,
            loading = isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Spacer(modifier = Modifier.height(20.dp))
    EyebrowLabel(text = text)
    Spacer(modifier = Modifier.height(10.dp))
}
