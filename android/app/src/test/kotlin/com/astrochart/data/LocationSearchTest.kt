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
}
