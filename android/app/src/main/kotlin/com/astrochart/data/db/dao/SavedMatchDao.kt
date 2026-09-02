package com.astrochart.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.astrochart.data.db.entities.SavedMatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedMatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(match: SavedMatchEntity): Long

    /** Newest first, mirroring [SavedChartDao.getAllCharts]. */
    @Query("SELECT * FROM saved_matches ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<SavedMatchEntity>>

    @Query("SELECT * FROM saved_matches WHERE id = :matchId")
    suspend fun getById(matchId: Long): SavedMatchEntity?

    @Query("DELETE FROM saved_matches WHERE id = :matchId")
    suspend fun deleteById(matchId: Long)
}
