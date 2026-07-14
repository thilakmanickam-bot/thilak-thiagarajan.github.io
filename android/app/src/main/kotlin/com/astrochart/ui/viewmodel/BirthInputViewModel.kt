package com.astrochart.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.astrochart.core.models.BirthData
import com.astrochart.core.models.NatalChart
import com.astrochart.core.utils.ChartCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId

class BirthInputViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<BirthInputUiState>(BirthInputUiState.Idle)
    val uiState: StateFlow<BirthInputUiState> = _uiState

    fun submitBirthData(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        latitude: Double,
        longitude: Double,
        timeZoneId: String,
        locationName: String
    ) {
        _uiState.value = BirthInputUiState.Loading

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val dateTime = LocalDateTime.of(year, month, day, hour, minute)
                val birthData = BirthData(
                    dateTime = dateTime,
                    latitude = latitude,
                    longitude = longitude,
                    timeZone = ZoneId.of(timeZoneId),
                    locationName = locationName
                )

                val chart = ChartCalculator.calculateNatalChart(birthData)
                _uiState.value = BirthInputUiState.Success(chart)
            } catch (e: Exception) {
                _uiState.value = BirthInputUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    sealed class BirthInputUiState {
        object Idle : BirthInputUiState()
        object Loading : BirthInputUiState()
        data class Success(val chart: NatalChart) : BirthInputUiState()
        data class Error(val message: String) : BirthInputUiState()
    }
}
