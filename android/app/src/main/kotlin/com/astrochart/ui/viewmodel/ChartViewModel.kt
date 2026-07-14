package com.astrochart.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.astrochart.core.models.NatalChart
import com.astrochart.data.SampleData
import com.astrochart.data.db.entities.SavedChartEntity
import com.astrochart.data.repository.ChartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChartViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChartRepository(application.applicationContext)

    private val _uiState = MutableStateFlow<ChartUiState>(ChartUiState.Idle)
    val uiState: StateFlow<ChartUiState> = _uiState

    private val _currentChart = MutableStateFlow<NatalChart?>(null)
    val currentChart: StateFlow<NatalChart?> = _currentChart

    /** Saved charts, live from the database, for the saved-charts list screen. */
    val savedCharts: StateFlow<List<SavedChartEntity>> =
        repository.getSavedCharts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setChart(chart: NatalChart) {
        _currentChart.value = chart
        _uiState.value = ChartUiState.Success
    }

    /** Loads a previously saved chart by id and makes it the current chart. */
    fun loadSavedChart(id: Long) {
        _uiState.value = ChartUiState.Loading
        viewModelScope.launch {
            val chart = repository.getNatalChartById(id)
            if (chart != null) {
                _currentChart.value = chart
                _uiState.value = ChartUiState.Success
            } else {
                _uiState.value = ChartUiState.Error("Chart not found")
            }
        }
    }

    /** Computes and shows the bundled sample chart. */
    fun loadSampleChart() {
        _uiState.value = ChartUiState.Loading
        viewModelScope.launch {
            val chart = repository.calculateChart(SampleData.sampleBirthData())
            _currentChart.value = chart
            _uiState.value = ChartUiState.Success
        }
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
