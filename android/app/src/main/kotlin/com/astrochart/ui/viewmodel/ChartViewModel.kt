package com.astrochart.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.astrochart.core.models.NatalChart
import com.astrochart.data.repository.ChartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChartViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChartRepository(application.applicationContext)

    private val _uiState = MutableStateFlow<ChartUiState>(ChartUiState.Idle)
    val uiState: StateFlow<ChartUiState> = _uiState

    private val _currentChart = MutableStateFlow<NatalChart?>(null)
    val currentChart: StateFlow<NatalChart?> = _currentChart

    fun setChart(chart: NatalChart) {
        _currentChart.value = chart
        _uiState.value = ChartUiState.Success
    }

    fun saveCurrentChart(name: String) {
        val chart = _currentChart.value ?: return
        _uiState.value = ChartUiState.Loading

        viewModelScope.launch {
            try {
                repository.saveChart(name, chart)
                _uiState.value = ChartUiState.Saved
            } catch (e: Exception) {
                _uiState.value = ChartUiState.Error(e.message ?: "Failed to save chart")
            }
        }
    }

    sealed class ChartUiState {
        object Idle : ChartUiState()
        object Loading : ChartUiState()
        object Success : ChartUiState()
        object Saved : ChartUiState()
        data class Error(val message: String) : ChartUiState()
    }
}
