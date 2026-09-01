package com.astrochart.data

/**
 * A worldwide place parsed from the bundled `assets/cities500.dat` dataset
 * (GeoNames "cities500" — every city/town/village with population >= 500,
 * CC-BY 4.0, via the geonamescache project). Powers birthplace search in
 * [LocationSearch] so users can find places outside the curated
 * [LocationCatalog].
 */
data class GeoPlace(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val zoneId: String,
    val population: Int
) {
    val displayName: String get() = "$name, $country"

    fun toLocationOption(): LocationOption = LocationOption(
        city = name,
        country = country,
        latitude = latitude,
        longitude = longitude,
        zoneId = zoneId
    )
}
