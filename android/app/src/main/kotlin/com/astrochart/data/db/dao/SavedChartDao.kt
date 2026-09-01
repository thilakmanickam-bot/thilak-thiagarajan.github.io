package com.astrochart.data.db.dao

import androidx.room.*
import com.astrochart.data.db.entities.SavedChartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedChartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChart(chart: SavedChartEntity): Long

    @Update
    suspend fun updateChart(chart: SavedChartEntity)

    @Delete
    suspend fun deleteChart(chart: SavedChartEntity)

    @Query("SELECT * FROM saved_charts WHERE id = :chartId")
    suspend fun getChartById(chartId: Long): SavedChartEntity?

    @Query("SELECT * FROM saved_charts ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestChart(): SavedChartEntity?

    @Query("SELECT * FROM saved_charts ORDER BY createdAt DESC")
    fun getAllCharts(): Flow<List<SavedChartEntity>>

    /** One-shot list (not a Flow) — used by the account sync to reconcile with the cloud. */
    @Query("SELECT * FROM saved_charts ORDER BY createdAt DESC")
    suspend fun getAllChartsList(): List<SavedChartEntity>

    @Query("SELECT * FROM saved_charts WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getChartByRemoteId(remoteId: String): SavedChartEntity?

    /** Stamp a local chart with the Firestore id it was pushed to. */
    @Query("UPDATE saved_charts SET remoteId = :remoteId WHERE id = :chartId")
    suspend fun setRemoteId(chartId: Long, remoteId: String)

    @Query("SELECT * FROM saved_charts WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchCharts(query: String): Flow<List<SavedChartEntity>>

    @Query("DELETE FROM saved_charts WHERE id = :chartId")
    suspend fun deleteChartById(chartId: Long)

    @Query("UPDATE saved_charts SET name = :name WHERE id = :chartId")
    suspend fun renameChart(chartId: Long, name: String)
}
