package com.astrochart.data

import android.content.Context
import android.util.Log
import java.text.Normalizer
import java.util.Locale
import java.util.zip.GZIPInputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Offline, worldwide birthplace search over the bundled `cities5000.tsv.gz`
 * asset (see [GeoPlace]) — lets users type any city/town/village rather than
 * being limited to the curated [LocationCatalog]. No network, no API key.
 *
 * The dataset is parsed once, lazily, off the main thread, and cached in
 * memory for the process lifetime.
 */
object LocationSearch {
    private const val ASSET_NAME = "cities5000.tsv.gz"
    private const val MIN_QUERY_LENGTH = 2
    private const val TAG = "LocationSearch"

    @Volatile
    private var cache: List<GeoPlace>? = null
    private val loadLock = Mutex()

    /**
     * Never throws: any failure reading/parsing the bundled dataset (or
     * during ranking) is logged and treated as "no results" rather than
     * crashing the caller — this runs off a debounced LaunchedEffect while
     * the user is still typing, so an uncaught exception here would take
     * down the whole app.
     */
    suspend fun search(
        context: Context,
        query: String,
        limit: Int = 20,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
        cpuDispatcher: CoroutineDispatcher = Dispatchers.Default
    ): List<GeoPlace> {
        if (query.trim().length < MIN_QUERY_LENGTH) return emptyList()
        return try {
            val places = places(context, ioDispatcher)
            withContext(cpuDispatcher) { rankMatches(places, query, limit) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Location search failed for query length ${query.length}", e)
            emptyList()
        }
    }

    private suspend fun places(context: Context, ioDispatcher: CoroutineDispatcher): List<GeoPlace> {
        cache?.let { return it }
        return loadLock.withLock {
            cache ?: withContext(ioDispatcher) { load(context) }.also { cache = it }
        }
    }

    private fun load(context: Context): List<GeoPlace> =
        context.assets.open(ASSET_NAME).use { raw ->
            GZIPInputStream(raw).bufferedReader(Charsets.UTF_8).use { reader ->
                reader.lineSequence().mapNotNull(::parseLine).toList()
            }
        }

    internal fun parseLine(line: String): GeoPlace? {
        val parts = line.split('\t')
        if (parts.size != 6) return null
        val name = parts[0]
        val country = parts[1]
        val latitude = parts[2].toDoubleOrNull() ?: return null
        val longitude = parts[3].toDoubleOrNull() ?: return null
        val zoneId = parts[4]
        if (name.isBlank() || zoneId.isBlank()) return null
        val population = parts[5].toIntOrNull() ?: 0
        return GeoPlace(name, country, latitude, longitude, zoneId, population)
    }

    /**
     * Places whose name or country contains [query] (diacritic/case
     * insensitive), ranked: name-prefix matches first, then by population
     * descending, so major cities surface before minor ones with the same
     * match strength.
     */
    internal fun rankMatches(places: List<GeoPlace>, query: String, limit: Int): List<GeoPlace> {
        val needle = normalize(query.trim())
        if (needle.isEmpty()) return emptyList()
        return places.asSequence()
            .filter { normalize(it.name).contains(needle) || normalize(it.country).contains(needle) }
            .sortedWith(
                compareByDescending<GeoPlace> { normalize(it.name).startsWith(needle) }
                    .thenByDescending { it.population }
            )
            .take(limit)
            .toList()
    }

    internal fun normalize(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(DIACRITIC_MARKS, "")
            .lowercase(Locale.ROOT)

    private val DIACRITIC_MARKS = Regex("\\p{Mn}+")
}
