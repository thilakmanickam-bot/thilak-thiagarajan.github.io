package com.astrochart.ui.i18n

import android.content.Context
import com.astrochart.data.LocationCatalog
import com.astrochart.data.LocationOption
import java.util.TimeZone

/**
 * Persists the city used for the panchangam — sunrise, rahu kalam and the rest
 * are location-specific, so this is not cosmetic.
 *
 * Until the user picks one, the city is taken from the device's own settings
 * rather than a fixed city on the other side of the world.
 */
object PanchangamLocationStore {
    private const val PREFS = "astro_prefs"
    private const val KEY_CITY = "panchangam_city"

    /** Where the app lands when the device's zone matches nothing catalogued. */
    private const val FALLBACK_CITY = "Chennai, India"

    /**
     * The catalogued city that best fits the device, for a user who has not
     * chosen one.
     *
     * Derived from the device's **time zone**, not from GPS: the app requests
     * no location permission, and the zone is what sunrise and the kalam
     * windows are computed against in any case. Getting the zone right matters
     * far more than which city inside it — two cities sharing a zone differ in
     * sunrise by minutes, two zones by hours — but a zone often spans several
     * catalogued cities, so within one the order is:
     *
     *  1. [FALLBACK_CITY], when it is in that zone. Asia/Kolkata covers a
     *     dozen Indian cities and this app's audience means Chennai, not
     *     whichever of them happens to sort first.
     *  2. The city the zone is named after — America/New_York should offer New
     *     York, not Miami, even though both keep that zone and Miami sorts
     *     earlier.
     *  3. Anything else in the zone.
     *
     * [zoneId] is a parameter so this is testable without touching the JVM's
     * default zone.
     */
    internal fun systemDefault(zoneId: String = TimeZone.getDefault().id): LocationOption {
        val fallback = LocationCatalog.byDisplayName(FALLBACK_CITY)
        if (fallback != null && fallback.zoneId == zoneId) return fallback

        val inZone = LocationCatalog.locations.filter { it.zoneId == zoneId }
        // "America/New_York" -> "new york", to be compared with the city name.
        val named = zoneId.substringAfterLast('/').replace('_', ' ').lowercase()
        return inZone.firstOrNull { it.city.lowercase() == named }
            ?: inZone.firstOrNull()
            ?: fallback
            ?: LocationCatalog.locations.first()
    }

    /**
     * The stored city, or the device's own if nothing has been stored.
     *
     * The default is read as null rather than a city name so a first run is
     * distinguishable from someone who deliberately chose the fallback city —
     * without that, the device default could never apply.
     */
    fun load(context: Context): LocationOption {
        val name = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_CITY, null)
            ?: return systemDefault()
        // A stored city can fall out of the catalog between releases; the
        // device default is a better answer than a city the user never chose.
        return LocationCatalog.byDisplayName(name) ?: systemDefault()
    }

    fun save(context: Context, displayName: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CITY, displayName)
            .apply()
    }
}
