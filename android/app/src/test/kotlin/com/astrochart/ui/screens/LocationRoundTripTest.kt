package com.astrochart.ui.screens

import com.astrochart.data.LocationOption
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * A saved match stores a birthplace as one display string plus its coordinates
 * and zone, not as a structured city/country pair. Reopening one has to rebuild
 * a [LocationOption] from that string, and the birthplace field then shows
 * whatever comes back — so a bad split is visible to the user, not internal.
 */
class LocationRoundTripTest {

    private fun roundTrip(city: String, country: String): String {
        val original = LocationOption(city, country, 10.0665, 78.7784, "Asia/Kolkata")
        return locationFrom(original.displayName, 10.0665, 78.7784, "Asia/Kolkata").displayName
    }

    @Test
    fun anOrdinaryPlaceComesBackUnchanged() {
        assertEquals("Kāraikkudi, India", roundTrip("Kāraikkudi", "India"))
    }

    @Test
    fun aCityNameContainingACommaSurvives() {
        // Splitting at the first ", " would return "Washington, DC" as the
        // country and drop the city — hence the split is at the last one.
        assertEquals("Washington, D.C., USA", roundTrip("Washington, D.C.", "USA"))
    }

    @Test
    fun aNameWithNoSeparatorIsTreatedAsTheCity() {
        // Rows written before a country was recorded, and any future caller
        // that stores a bare place name.
        val rebuilt = locationFrom("Singapore", 1.3521, 103.8198, "Asia/Singapore")

        assertEquals("Singapore", rebuilt.city)
        assertEquals("", rebuilt.country)
    }

    @Test
    fun theCoordinatesAndZoneAreCarriedStraightThrough() {
        val rebuilt = locationFrom("Chennai, India", 13.0827, 80.2707, "Asia/Kolkata")

        assertEquals(13.0827, rebuilt.latitude, 1e-9)
        assertEquals(80.2707, rebuilt.longitude, 1e-9)
        assertEquals("Asia/Kolkata", rebuilt.zoneId)
    }
}
