package com.astrochart.data.repository

import android.content.Context
import com.astrochart.core.models.BirthData
import com.astrochart.core.models.NatalChart
import com.astrochart.core.utils.ChartCalculator
import com.astrochart.data.db.AstroChartDatabase
import com.astrochart.data.db.entities.SavedChartEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId

class ChartRepository(private val context: Context) {

    private val database = AstroChartDatabase.getInstance(context)
    private val chartDao = database.savedChartDao()

    suspend fun calculateChart(birthData: BirthData): NatalChart {
        return withContext(Dispatchers.Default) {
            ChartCalculator.calculateNatalChart(birthData)
        }
    }

    suspend fun saveChart(name: String, chart: NatalChart): Long {
        return withContext(Dispatchers.IO) {
            val entity = SavedChartEntity(
                name = name,
                birthDateTime = chart.birthData.dateTime,
                latitude = chart.birthData.latitude,
                longitude = chart.birthData.longitude,
                timeZone = chart.birthData.timeZone.id,
                locationName = chart.birthData.locationName,
                createdAt = LocalDateTime.now(),
                chartJson = serializeChart(chart)
            )
            chartDao.insertChart(entity)
        }
    }

    fun getSavedCharts(): Flow<List<SavedChartEntity>> {
        return chartDao.getAllCharts()
    }

    suspend fun getChartById(id: Long): SavedChartEntity? {
        return withContext(Dispatchers.IO) {
            chartDao.getChartById(id)
        }
    }

    suspend fun deleteChart(id: Long) {
        withContext(Dispatchers.IO) {
            chartDao.deleteChartById(id)
        }
    }

    fun searchCharts(query: String): Flow<List<SavedChartEntity>> {
        return chartDao.searchCharts(query)
    }

    private fun serializeChart(chart: NatalChart): String {
        return chart.toString()
    }

    private fun deserializeChart(json: String): NatalChart? {
        return null
    }
}
