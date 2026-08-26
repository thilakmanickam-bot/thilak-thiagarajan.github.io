package com.astrochart.ui.i18n

import android.content.Context
import com.astrochart.data.LocationCatalog
import com.astrochart.data.LocationOption

/**
 * Persists the city used for the panchangam (sunrise, rahu kalam, etc. are
 * location-specific). Defaults to Chennai, and falls back to it if a stored
 * city is no longer in the catalog.
 */
object PanchangamLocationStore {
    private const val PREFS = "astro_prefs"
    private const val KEY_CITY = "panchangam_city"
    private const val DEFAULT_CITY = "Chennai, India"

    private fun default(): LocationOption =
        LocationCatalog.byDisplayName(DEFAULT_CITY) ?: LocationCatalog.locations.first()

    fun load(context: Context): LocationOption {
        val name = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_CITY, DEFAULT_CITY)
        return LocationCatalog.byDisplayName(name ?: DEFAULT_CITY) ?: default()
    }

    fun save(context: Context, displayName: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CITY, displayName)
            .apply()
    }
}
