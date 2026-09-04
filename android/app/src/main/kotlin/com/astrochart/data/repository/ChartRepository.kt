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
     * Loads a saved chart and rebuilds its [NatalChart] **by recomputing it**
     * from the stored birth instant and place.
     *
     * The row also carries a `chartJson` snapshot of the chart as it was when it
     * was saved, and this used to prefer it. That made every saved chart immune
     * to fixing the engine: charts stored by 1.2.0 hold tropical signs, a Libra
     * ascendant from a formula that omitted the obliquity, and Uranus, Neptune
     * and Pluto from fabricated elements — and would have gone on showing all of
     * it after the corrected build was installed, because the corrected code
     * never ran.
     *
     * A chart is a pure function of the birth data, and the row stores all of
     * it, so recomputing is not a fallback here — it is the only honest answer.
     * The snapshot is still written by [saveChart]: [com.astrochart.auth.ProfileSync]
     * round-trips the whole entity through Firestore, and dropping the column
     * would cost a Room migration and a document-shape change for a field
     * nothing reads.
     */
    suspend fun getNatalChartById(id: Long): NatalChart? {
        return withContext(Dispatchers.IO) {
            val entity = chartDao.getChartById(id) ?: return@withContext null
            recomputeFromEntity(entity)
        }
    }

    /**
     * The chart for a saved row, recomputed from its birth data. `internal` so
     * the notification worker reads a saved chart the same way rather than
     * parsing the snapshot and naming a sign the chart screen disagrees with.
     */
    internal fun recomputeFromEntity(entity: SavedChartEntity): NatalChart {
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
}
