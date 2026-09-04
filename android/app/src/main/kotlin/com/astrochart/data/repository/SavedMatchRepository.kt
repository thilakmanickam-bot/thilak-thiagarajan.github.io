package com.astrochart.data.repository

import android.content.Context
import com.astrochart.data.db.AstroChartDatabase
import com.astrochart.data.db.entities.SavedMatchEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Saved marriage matches, mirroring [ChartRepository]'s shape.
 *
 * Deliberately thinner than that one: a match stores only its inputs, so there
 * is no serialise/deserialise step and nothing to reconstruct — reopening one
 * re-runs [com.astrochart.core.interpret.Porutham.compute] on the two
 * (rasi, nakshatra) pairs. The only job here is to keep the Room calls off the
 * main thread.
 */
class SavedMatchRepository(context: Context) {

    private val dao = AstroChartDatabase.getInstance(context).savedMatchDao()

    /** Newest first. Emits again on every insert and delete. */
    fun observeAll(): Flow<List<SavedMatchEntity>> = dao.observeAll()

    suspend fun save(match: SavedMatchEntity): Long =
        withContext(Dispatchers.IO) { dao.insert(match) }

    suspend fun getById(id: Long): SavedMatchEntity? =
        withContext(Dispatchers.IO) { dao.getById(id) }

    suspend fun delete(id: Long) =
        withContext(Dispatchers.IO) { dao.deleteById(id) }
}
