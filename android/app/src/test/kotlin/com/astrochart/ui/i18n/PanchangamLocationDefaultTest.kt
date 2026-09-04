package com.astrochart.ui.i18n

import com.astrochart.data.LocationCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The panchangam's location is not cosmetic — sunrise, rahu kalam and the rest
 * are computed from it — so an untouched install landing on a city on the other
 * side of the world gives wrong times all day with no visible error.
 *
 * The device's time zone is the signal, since the app asks for no location
 * permission. These pin the choice for real zones rather than the mechanism.
 */
class PanchangamLocationDefaultTest {

    @Test
    fun anIndianDeviceGetsChennaiRatherThanWhicheverIndianCitySortsFirst() {
        // Asia/Kolkata covers a dozen catalogued cities; the fallback city
        // should win its own zone rather than losing to alphabetical order.
        val chosen = PanchangamLocationStore.systemDefault("Asia/Kolkata")

        assertEquals("Chennai, India", chosen.displayName)
    }

    @Test
    fun aDeviceElsewhereGetsACityInItsOwnZone() {
        listOf(
            "Asia/Singapore",
            "America/New_York",
            "Europe/London",
            "Australia/Sydney"
        ).forEach { zone ->
            val chosen = PanchangamLocationStore.systemDefault(zone)

            assertEquals("$zone should resolve inside its own zone", zone, chosen.zoneId)
        }
    }

    @Test
    fun aZoneNamedAfterACityOffersThatCity() {
        // America/New_York holds New York, Miami and Washington, and Miami
        // sorts first. Someone in New York should be offered New York.
        assertEquals("New York, USA", PanchangamLocationStore.systemDefault("America/New_York").displayName)
        assertEquals("London, UK", PanchangamLocationStore.systemDefault("Europe/London").displayName)
        assertEquals("Tokyo, Japan", PanchangamLocationStore.systemDefault("Asia/Tokyo").displayName)
    }

    @Test
    fun anUncataloguedZoneFallsBackRatherThanFailing() {
        // Antarctica is not in the catalog. Something has to be shown, and a
        // wrong-but-named city beats a crash or an empty screen.
        val chosen = PanchangamLocationStore.systemDefault("Antarctica/Troll")

        assertEquals("Chennai, India", chosen.displayName)
    }

    @Test
    fun theChosenCityIsAlwaysOneTheCatalogActuallyHas() {
        // systemDefault feeds a dropdown whose options come from the catalog;
        // returning anything outside it would show a selection that cannot be
        // reselected after the user changes it.
        listOf("Asia/Kolkata", "Asia/Tokyo", "Europe/Paris", "Nowhere/Invalid").forEach { zone ->
            val chosen = PanchangamLocationStore.systemDefault(zone)

            assertTrue(
                "$zone produced ${chosen.displayName}, which is not in the catalog",
                LocationCatalog.byDisplayName(chosen.displayName) != null
            )
        }
    }
}
