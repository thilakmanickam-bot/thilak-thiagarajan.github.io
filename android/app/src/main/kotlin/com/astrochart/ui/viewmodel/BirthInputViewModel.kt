package com.astrochart.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.astrochart.core.models.BirthData
import com.astrochart.core.models.NatalChart
import com.astrochart.data.repository.ChartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId

class BirthInputViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChartRepository(application.applicationContext)

    private val _uiState = MutableStateFlow<BirthInputUiState>(BirthInputUiState.Idle)
    val uiState: StateFlow<BirthInputUiState> = _uiState

    /**
     * Computes the chart for the given birth details and automatically saves it
     * under [name]. Coordinates and time zone come from the chosen location, so
     * the caller no longer supplies raw latitude/longitude by hand.
     */
    fun calculateAndSave(
        name: String,
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        latitude: Double,
        longitude: Double,
        timeZoneId: String,
        locationName: String,
        gender: String = ""
    ) {
        _uiState.value = BirthInputUiState.Loading

        viewModelScope.launch {
            try {
                val dateTime = LocalDateTime.of(year, month, day, hour, minute)
                val birthData = BirthData(
                    dateTime = dateTime,
                    latitude = latitude,
                    longitude = longitude,
                    timeZone = ZoneId.of(timeZoneId),
                    locationName = locationName,
                    gender = gender
                )

                val chart = repository.calculateChart(birthData)
                repository.saveChart(name.trim(), chart)
                _uiState.value = BirthInputUiState.Success(chart)
            } catch (e: Exception) {
                _uiState.value = BirthInputUiState.Error(e.message ?: "Could not calculate chart")
            }
        }
    }

    /** Resets state so a returning user sees a clean form, not the last result. */
    fun reset() {
        _uiState.value = BirthInputUiState.Idle
    }

    sealed class BirthInputUiState {
        object Idle : BirthInputUiState()
        object Loading : BirthInputUiState()
        data class Success(val chart: NatalChart) : BirthInputUiState()
        data class Error(val message: String) : BirthInputUiState()
    }
}
