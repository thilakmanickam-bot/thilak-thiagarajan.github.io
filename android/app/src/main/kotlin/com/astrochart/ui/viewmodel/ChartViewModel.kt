package com.astrochart.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.astrochart.core.interpret.Compatibility
import com.astrochart.core.interpret.CompatibilityResult
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

    private val _currentChartName = MutableStateFlow("")
    val currentChartName: StateFlow<String> = _currentChartName

    /** Saved charts, live from the database, for the saved-charts list screen. */
    val savedCharts: StateFlow<List<SavedChartEntity>> =
        repository.getSavedCharts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _compatibility = MutableStateFlow<CompatibilityResult?>(null)
    val compatibility: StateFlow<CompatibilityResult?> = _compatibility

    /** Loads two saved charts and computes their compatibility. */
    fun computeCompatibility(idA: Long, idB: Long) {
        viewModelScope.launch {
            val entityA = repository.getChartById(idA)
            val entityB = repository.getChartById(idB)
            val chartA = repository.getNatalChartById(idA)
            val chartB = repository.getNatalChartById(idB)
            _compatibility.value = if (chartA != null && chartB != null) {
                Compatibility.compute(
                    entityA?.name ?: "", chartA,
                    entityB?.name ?: "", chartB
                )
            } else null
        }
    }

    fun clearCompatibility() {
        _compatibility.value = null
    }

    fun setChart(chart: NatalChart, name: String) {
        _currentChart.value = chart
        _currentChartName.value = name
        _uiState.value = ChartUiState.Success
    }

    /** Loads a previously saved chart by id and makes it the current chart. */
    fun loadSavedChart(id: Long) {
        _uiState.value = ChartUiState.Loading
        viewModelScope.launch {
            val entity = repository.getChartById(id)
            val chart = repository.getNatalChartById(id)
            if (entity != null && chart != null) {
                _currentChart.value = chart
                _currentChartName.value = entity.name
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
            _currentChartName.value = "Sample — Natalie Wood"
            _uiState.value = ChartUiState.Success
        }
    }

    /** Renames a saved chart. The saved-charts list updates automatically. */
    fun renameSavedChart(id: Long, name: String) {
        viewModelScope.launch {
            repository.renameChart(id, name.trim())
        }
    }

    /** Deletes a saved chart. The saved-charts list updates automatically. */
    fun deleteSavedChart(id: Long) {
        viewModelScope.launch {
            repository.deleteChart(id)
        }
    }

    sealed class ChartUiState {
        object Idle : ChartUiState()
        object Loading : ChartUiState()
        object Success : ChartUiState()
        data class Error(val message: String) : ChartUiState()
    }
}
