package com.astrochart.data.repository

import android.content.Context
import com.astrochart.core.models.BirthData
import com.astrochart.core.models.NatalChart
import com.astrochart.core.utils.ChartCalculator
import com.astrochart.data.db.AstroChartDatabase
import com.astrochart.data.db.entities.SavedChartEntity
import com.astrochart.data.util.ChartJson
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

    /**
     * Loads a saved chart and reconstructs its [NatalChart]. Prefers the stored
     * JSON snapshot; if that is missing or unparseable (e.g. an older row), it
     * falls back to recomputing deterministically from the saved birth data.
     */
    suspend fun getNatalChartById(id: Long): NatalChart? {
        return withContext(Dispatchers.IO) {
            val entity = chartDao.getChartById(id) ?: return@withContext null
            deserializeChart(entity.chartJson) ?: recomputeFromEntity(entity)
        }
    }

    private fun recomputeFromEntity(entity: SavedChartEntity): NatalChart {
        val birthData = BirthData(
            dateTime = entity.birthDateTime,
            latitude = entity.latitude,
            longitude = entity.longitude,
            timeZone = ZoneId.of(entity.timeZone),
            locationName = entity.locationName
        )
        return ChartCalculator.calculateNatalChart(birthData)
    }

    suspend fun deleteChart(id: Long) {
        withContext(Dispatchers.IO) {
            chartDao.deleteChartById(id)
        }
    }

    suspend fun renameChart(id: Long, name: String) {
        withContext(Dispatchers.IO) {
            chartDao.renameChart(id, name)
        }
    }

    fun searchCharts(query: String): Flow<List<SavedChartEntity>> {
        return chartDao.searchCharts(query)
    }

    // ----- Cloud-sync helpers (local DB only; the Firestore side lives in
    // com.astrochart.auth.ProfileSync so this repository stays Firebase-free) -----

    /** All saved charts as a one-shot list, for reconciling against the cloud. */
    suspend fun allChartsOnce(): List<SavedChartEntity> =
        withContext(Dispatchers.IO) { chartDao.getAllChartsList() }

    suspend fun chartByRemoteId(remoteId: String): SavedChartEntity? =
        withContext(Dispatchers.IO) { chartDao.getChartByRemoteId(remoteId) }

    /** Insert or replace a chart row (used when pulling cloud charts down). */
    suspend fun upsertLocal(entity: SavedChartEntity): Long =
        withContext(Dispatchers.IO) { chartDao.insertChart(entity) }

    /** Record the Firestore id a local chart was pushed to. */
    suspend fun stampRemoteId(id: Long, remoteId: String) =
        withContext(Dispatchers.IO) { chartDao.setRemoteId(id, remoteId) }

    private fun serializeChart(chart: NatalChart): String {
        return ChartJson.toJson(chart)
    }

    private fun deserializeChart(json: String): NatalChart? {
        return ChartJson.fromJson(json)
    }
}
