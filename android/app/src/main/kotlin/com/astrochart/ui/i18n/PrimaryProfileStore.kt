package com.astrochart.ui.i18n

import android.content.Context

/**
 * The person whose predictions the app foregrounds: their rasi seeds the Rasi
 * Palan hub and their daily reading drives the daily notification. Stored in the
 * shared app preferences. [rasi] is 0-based (0 = Aries) and [nakshatra] is
 * 0-based (0 = Ashwini), both derived from the birth details below rather than
 * hand-picked. The remaining fields are the full birth data behind that
 * derivation, kept so the profile can be re-edited (prefilled) later instead of
 * re-entered from scratch; they default for backward compatibility with
 * profiles saved before these fields existed.
 */
data class PrimaryProfile(
    val name: String,
    val rasi: Int,
    val nakshatra: Int,
    val gender: String = "",
    val year: Int = 2000,
    val month: Int = 1,
    val day: Int = 1,
    val hour: Int = 12,
    val minute: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timeZoneId: String = "UTC",
    val locationCity: String = "",
    val locationCountry: String = ""
)

object PrimaryProfileStore {
    private const val PREFS = "astro_prefs"
    private const val KEY_NAME = "primary_name"
    private const val KEY_RASI = "primary_rasi"
    private const val KEY_NAK = "primary_nak"
    private const val KEY_GENDER = "primary_gender"
    private const val KEY_YEAR = "primary_year"
    private const val KEY_MONTH = "primary_month"
    private const val KEY_DAY = "primary_day"
    private const val KEY_HOUR = "primary_hour"
    private const val KEY_MINUTE = "primary_minute"
    private const val KEY_LAT = "primary_lat"
    private const val KEY_LON = "primary_lon"
    private const val KEY_TZ = "primary_tz"
    private const val KEY_LOC_CITY = "primary_loc_city"
    private const val KEY_LOC_COUNTRY = "primary_loc_country"

    /** The saved primary profile, or null if the user has not set one. */
    fun load(context: Context): PrimaryProfile? {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val rasi = p.getInt(KEY_RASI, -1)
        val nak = p.getInt(KEY_NAK, -1)
        if (rasi !in 0..11 || nak !in 0..26) return null
        return PrimaryProfile(
            name = p.getString(KEY_NAME, "") ?: "",
            rasi = rasi,
            nakshatra = nak,
            gender = p.getString(KEY_GENDER, "") ?: "",
            year = p.getInt(KEY_YEAR, 2000),
            month = p.getInt(KEY_MONTH, 1),
            day = p.getInt(KEY_DAY, 1),
            hour = p.getInt(KEY_HOUR, 12),
            minute = p.getInt(KEY_MINUTE, 0),
            latitude = p.getFloat(KEY_LAT, 0f).toDouble(),
            longitude = p.getFloat(KEY_LON, 0f).toDouble(),
            timeZoneId = p.getString(KEY_TZ, "UTC") ?: "UTC",
            locationCity = p.getString(KEY_LOC_CITY, "") ?: "",
            locationCountry = p.getString(KEY_LOC_COUNTRY, "") ?: ""
        )
    }

    fun save(context: Context, profile: PrimaryProfile) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_NAME, profile.name)
            .putInt(KEY_RASI, profile.rasi)
            .putInt(KEY_NAK, profile.nakshatra)
            .putString(KEY_GENDER, profile.gender)
            .putInt(KEY_YEAR, profile.year)
            .putInt(KEY_MONTH, profile.month)
            .putInt(KEY_DAY, profile.day)
            .putInt(KEY_HOUR, profile.hour)
            .putInt(KEY_MINUTE, profile.minute)
            .putFloat(KEY_LAT, profile.latitude.toFloat())
            .putFloat(KEY_LON, profile.longitude.toFloat())
            .putString(KEY_TZ, profile.timeZoneId)
            .putString(KEY_LOC_CITY, profile.locationCity)
            .putString(KEY_LOC_COUNTRY, profile.locationCountry)
            .apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_NAME).remove(KEY_RASI).remove(KEY_NAK)
            .remove(KEY_GENDER).remove(KEY_YEAR).remove(KEY_MONTH).remove(KEY_DAY)
            .remove(KEY_HOUR).remove(KEY_MINUTE).remove(KEY_LAT).remove(KEY_LON)
            .remove(KEY_TZ).remove(KEY_LOC_CITY).remove(KEY_LOC_COUNTRY)
            .apply()
    }
}
