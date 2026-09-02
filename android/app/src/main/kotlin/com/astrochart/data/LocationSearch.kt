package com.astrochart.data

import android.content.Context
import android.util.Log
import java.text.Normalizer
import java.util.Locale
import java.util.zip.GZIPInputStream
import kotlin.math.abs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Offline, worldwide birthplace search over the bundled `cities500.dat`
 * asset (see [GeoPlace]) — lets users type any city/town/village rather than
 * being limited to the curated [LocationCatalog]. No network, no API key.
 *
 * Uses GeoNames' "cities500" dataset (every place with population >= 500,
 * ~235k entries) rather than the smaller "cities5000" cut (population
 * >= 5000, ~69k entries): the whole point of this feature is finding small
 * towns and villages, which are almost always under 5000 people, so the
 * larger cut is the one that actually fixes "I can't find my birthplace."
 *
 * The asset is named `.dat`, not `.gz`: Android's AAPT build tool detects a
 * `.gz` suffix on a bundled asset and transparently decompresses it while
 * packaging, storing the result under the base filename with `.gz` stripped
 * — so a real `cities500.tsv.gz` asset silently turned into `cities500.tsv`
 * inside the built APK, and `context.assets.open("cities500.tsv.gz")` threw
 * `FileNotFoundException` on every call (caught by [search]'s safety net,
 * which is exactly why every query silently found nothing, even guaranteed
 * entries like Singapore). `.dat` is inert to AAPT, so the gzip bytes make
 * it into the APK untouched.
 *
 * The dataset is parsed once, lazily, off the main thread, and cached in
 * memory for the process lifetime.
 */
object LocationSearch {
    private const val ASSET_NAME = "cities500.dat"
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
     * Places whose name or country matches [query], diacritic- and
     * case-insensitively.
     *
     * Two tiers, in strict order:
     *  1. **Exact** — the normalized name or country *contains* the query.
     *     Name-prefix hits first, then by population descending, so major
     *     cities surface before minor ones of the same match strength. This
     *     is byte-for-byte the behaviour that existed before fuzzy matching,
     *     so no existing query reorders.
     *  2. **Approximate** — the query is within a small edit distance of the
     *     name (or of one word of it), ordered by distance then population.
     *     This is what makes "karaikudi" find "Kāraikkudi": both are accepted
     *     romanisations, one k apart, and an exact `contains` misses it.
     *
     * A fuzzy hit can never outrank an exact one.
     */
    internal fun rankMatches(places: List<GeoPlace>, query: String, limit: Int): List<GeoPlace> {
        val needle = normalize(query.trim())
        if (needle.isEmpty()) return emptyList()
        val maxDistance = fuzzyTolerance(needle.length)

        val exact = ArrayList<Ranked>()
        val fuzzy = ArrayList<Ranked>()
        // Reused across every place: sized by the query, which is fixed for
        // this call. Allocating two rows per distance computation instead
        // would mean tens of thousands of throwaway arrays per keystroke.
        val prevRow = IntArray(needle.length + 1)
        val currRow = IntArray(needle.length + 1)

        for (place in places) {
            // Normalized once per place, not once per predicate: this runs
            // over the whole ~235k-row dataset on every debounced keystroke,
            // and normalize() is NFD decomposition plus a regex replace.
            val name = normalize(place.name)
            if (name.contains(needle)) {
                exact += Ranked(place, if (name.startsWith(needle)) 0 else 1, 0)
                continue
            }
            if (normalize(place.country).contains(needle)) {
                exact += Ranked(place, 1, 0)
                continue
            }
            // Once `limit` exact matches exist, no fuzzy match can reach the
            // results, so stop paying for them. Purely a cost guard — the
            // outcome is identical either way.
            if (maxDistance > 0 && exact.size < limit) {
                val distance = approximateDistance(name, needle, maxDistance, prevRow, currRow)
                if (distance <= maxDistance) fuzzy += Ranked(place, 2, distance)
            }
        }

        exact.sortWith(compareBy<Ranked> { it.tier }.thenByDescending { it.place.population })
        if (exact.size >= limit) return exact.take(limit).map { it.place }

        fuzzy.sortWith(compareBy<Ranked> { it.distance }.thenByDescending { it.place.population })
        return (exact + fuzzy).take(limit).map { it.place }
    }

    private class Ranked(val place: GeoPlace, val tier: Int, val distance: Int)

    /**
     * How many single-character edits a name may differ from the query by.
     *
     * Zero below four characters: at that length almost everything is within
     * one or two edits of everything else, so fuzzy matching there returns
     * noise rather than the place the user meant. Short queries are already
     * well served by the exact substring pass, which matches prefixes as the
     * user types.
     */
    private fun fuzzyTolerance(queryLength: Int): Int = when {
        queryLength < 4 -> 0
        queryLength <= 6 -> 1
        else -> 2
    }

    /**
     * Edit distance from [needle] to [name], or to the closest single word of
     * [name] — so "karaikudi" still matches "Kāraikkudi Junction", where the
     * full name is far too long to be within tolerance. Returns
     * `maxDistance + 1` for anything further away.
     */
    private fun approximateDistance(
        name: String,
        needle: String,
        maxDistance: Int,
        prevRow: IntArray,
        currRow: IntArray
    ): Int {
        val whole = boundedDistance(name, 0, name.length, needle, maxDistance, prevRow, currRow)
        if (whole <= maxDistance || name.indexOf(' ') < 0) return whole

        var best = whole
        var start = 0
        while (start < name.length) {
            var end = name.indexOf(' ', start)
            if (end < 0) end = name.length
            if (end > start) {
                val d = boundedDistance(name, start, end, needle, maxDistance, prevRow, currRow)
                if (d < best) best = d
            }
            start = end + 1
        }
        return best
    }

    /**
     * Levenshtein distance between `name[from until to]` and [needle],
     * abandoned as soon as the whole row exceeds [maxDistance] (returning
     * `maxDistance + 1`).
     *
     * Two rolling rows supplied by the caller, and a length check before any
     * of that: a name whose length differs from the query by more than
     * [maxDistance] cannot possibly be within it, and that one comparison
     * rejects the overwhelming majority of the dataset for free.
     */
    private fun boundedDistance(
        name: String,
        from: Int,
        to: Int,
        needle: String,
        maxDistance: Int,
        prevRow: IntArray,
        currRow: IntArray
    ): Int {
        val n = to - from
        val m = needle.length
        if (abs(n - m) > maxDistance) return maxDistance + 1

        var prev = prevRow
        var curr = currRow
        for (j in 0..m) prev[j] = j

        for (i in 1..n) {
            curr[0] = i
            val a = name[from + i - 1]
            var rowBest = curr[0]
            for (j in 1..m) {
                val substitution = prev[j - 1] + if (a == needle[j - 1]) 0 else 1
                val deletion = prev[j] + 1
                val insertion = curr[j - 1] + 1
                val best = minOf(substitution, deletion, insertion)
                curr[j] = best
                if (best < rowBest) rowBest = best
            }
            if (rowBest > maxDistance) return maxDistance + 1
            val swap = prev
            prev = curr
            curr = swap
        }
        return prev[m]
    }

    internal fun normalize(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(DIACRITIC_MARKS, "")
            .lowercase(Locale.ROOT)

    private val DIACRITIC_MARKS = Regex("\\p{Mn}+")
}
