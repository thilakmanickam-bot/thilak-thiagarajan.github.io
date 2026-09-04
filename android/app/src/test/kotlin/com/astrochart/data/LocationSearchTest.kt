package com.astrochart.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationSearchTest {

    private val places = listOf(
        GeoPlace("Erode", "India", 11.3410, 77.7172, "Asia/Kolkata", 155000),
        GeoPlace("Erode Rural", "India", 11.4, 77.8, "Asia/Kolkata", 5000),
        GeoPlace("San José", "Costa Rica", 9.9281, -84.0907, "America/Costa_Rica", 340000),
        GeoPlace("Zürich", "Switzerland", 47.3769, 8.5417, "Europe/Zurich", 400000),
        GeoPlace("India Village", "Nigeria", 1.0, 2.0, "Africa/Lagos", 5000)
    )

    @Test
    fun parseLine_parsesWellFormedRow() {
        val place = LocationSearch.parseLine("Erode\tIndia\t11.341\t77.7172\tAsia/Kolkata\t155000")

        assertEquals(GeoPlace("Erode", "India", 11.341, 77.7172, "Asia/Kolkata", 155000), place)
    }

    @Test
    fun parseLine_rejectsMalformedOrIncompleteRows() {
        assertNull(LocationSearch.parseLine("Erode\tIndia\t11.341"))
        assertNull(LocationSearch.parseLine("\tIndia\t11.341\t77.7172\tAsia/Kolkata\t155000"))
        assertNull(LocationSearch.parseLine("Erode\tIndia\t11.341\t77.7172\t\t155000"))
        assertNull(LocationSearch.parseLine("Erode\tIndia\tnotanumber\t77.7172\tAsia/Kolkata\t155000"))
    }

    @Test
    fun normalize_isCaseAndDiacriticInsensitive() {
        assertEquals("zurich", LocationSearch.normalize("Zürich"))
        assertEquals("san jose", LocationSearch.normalize("San José"))
    }

    @Test
    fun rankMatches_prefixMatchesRankAboveSubstringMatches() {
        val results = LocationSearch.rankMatches(places, "erode", limit = 10)

        assertEquals(listOf("Erode", "Erode Rural"), results.map { it.name })
    }

    @Test
    fun rankMatches_matchesByCountryToo() {
        val results = LocationSearch.rankMatches(places, "india", limit = 10)

        assertEquals(setOf("Erode", "Erode Rural", "India Village"), results.map { it.name }.toSet())
    }

    @Test
    fun rankMatches_isDiacriticInsensitive() {
        val results = LocationSearch.rankMatches(places, "zurich", limit = 10)

        assertEquals(listOf("Zürich"), results.map { it.name })
    }

    @Test
    fun rankMatches_ordersEqualPrefixMatchesByPopulationDescending() {
        val bigCity = GeoPlace("Cortown", "Freedonia", 0.0, 0.0, "UTC", 1_000_000)
        val smallTown = GeoPlace("Corville", "Freedonia", 0.0, 0.0, "UTC", 2_000)

        val results = LocationSearch.rankMatches(listOf(smallTown, bigCity), "cor", limit = 10)

        assertEquals(listOf("Cortown", "Corville"), results.map { it.name })
    }

    @Test
    fun rankMatches_respectsLimit() {
        val results = LocationSearch.rankMatches(places, "e", limit = 1)

        assertTrue(results.size <= 1)
    }

    @Test
    fun rankMatches_blankQueryReturnsEmpty() {
        assertEquals(emptyList<GeoPlace>(), LocationSearch.rankMatches(places, "   ", limit = 10))
    }

    // ---- approximate matching ----

    private val karaikkudi =
        GeoPlace("Kāraikkudi", "India", 10.0665, 78.7784, "Asia/Kolkata", 106793)

    @Test
    fun rankMatches_findsAPlaceSpelledOneLetterShort() {
        // The reported bug: "karaikudi" and "karaikkudi" are both accepted
        // romanisations of the same town, one k apart, so an exact `contains`
        // finds nothing at all.
        val results = LocationSearch.rankMatches(listOf(karaikkudi), "karaikudi", limit = 10)

        assertEquals(listOf("Kāraikkudi"), results.map { it.name })
    }

    @Test
    fun rankMatches_stillFindsTheExactSpellingAndTheAccentedOne() {
        assertEquals(
            listOf("Kāraikkudi"),
            LocationSearch.rankMatches(listOf(karaikkudi), "karaikkudi", limit = 10).map { it.name }
        )
        assertEquals(
            listOf("Kāraikkudi"),
            LocationSearch.rankMatches(listOf(karaikkudi), "kāraikkudi", limit = 10).map { it.name }
        )
    }

    @Test
    fun rankMatches_matchesOneWordOfAMultiWordName() {
        // The full name is far outside the edit-distance tolerance; the match
        // has to come from the "Kāraikkudi" word alone.
        val junction = GeoPlace("Kāraikkudi Junction", "India", 10.0, 78.7, "Asia/Kolkata", 5000)

        val results = LocationSearch.rankMatches(listOf(junction), "karaikudi", limit = 10)

        assertEquals(listOf("Kāraikkudi Junction"), results.map { it.name })
    }

    @Test
    fun rankMatches_exactMatchesAlwaysOutrankApproximateOnes() {
        // Erode is an exact hit but tiny; Erede is a misspelling of a huge
        // city. Population must not promote the fuzzy hit above the exact one.
        val exactButSmall = GeoPlace("Erode", "India", 11.3, 77.7, "Asia/Kolkata", 1_000)
        val fuzzyButHuge = GeoPlace("Erede", "Italy", 45.0, 7.0, "Europe/Rome", 9_000_000)

        val results = LocationSearch.rankMatches(listOf(fuzzyButHuge, exactButSmall), "erode", limit = 10)

        assertEquals(listOf("Erode", "Erede"), results.map { it.name })
    }

    @Test
    fun rankMatches_doesNotFuzzyMatchShortQueries() {
        // Below four characters nearly everything is within an edit or two of
        // everything else, so fuzzy matching there would bury the real hits.
        val results = LocationSearch.rankMatches(places, "ero", limit = 10)

        assertEquals(setOf("Erode", "Erode Rural"), results.map { it.name }.toSet())
    }

    @Test
    fun rankMatches_findsAnApproximateMatchInAFullSizedDataset() {
        // The bundled asset is ~235k rows and every keystroke scans all of
        // them, so exercise the fuzzy path at that scale: one needle hidden
        // among 200k non-matches.
        val many = (1..200_000).map {
            GeoPlace("Place$it", "Country${it % 200}", 0.0, 0.0, "UTC", it)
        } + karaikkudi

        val elapsed = kotlin.system.measureTimeMillis {
            val results = LocationSearch.rankMatches(many, "karaikudi", limit = 20)
            assertEquals(listOf("Kāraikkudi"), results.map { it.name })
        }

        // A runaway guard, not a benchmark: it fires only on genuinely
        // pathological behaviour (a quadratic scan, or losing both the length
        // prefilter and the early row abort). A tighter bound would flake on
        // a slow CI runner without catching anything a reviewer wouldn't.
        assertTrue("rankMatches took ${elapsed}ms over 200k rows", elapsed < 10_000)
    }
}
