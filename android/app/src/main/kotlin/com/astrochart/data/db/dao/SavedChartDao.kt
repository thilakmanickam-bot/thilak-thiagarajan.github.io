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

    @Query("SELECT * FROM saved_charts ORDER BY createdAt DESC")
    fun getAllCharts(): Flow<List<SavedChartEntity>>

    @Query("SELECT * FROM saved_charts WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchCharts(query: String): Flow<List<SavedChartEntity>>

    @Query("DELETE FROM saved_charts WHERE id = :chartId")
    suspend fun deleteChartById(chartId: Long)
}
