package com.astrochart.data

/**
 * A worldwide place parsed from the bundled `assets/cities5000.tsv.gz`
 * dataset (GeoNames "cities5000" — every city/town/village with population
 * >= 5000, CC-BY 4.0, via the geonamescache project). Powers birthplace
 * search in [LocationSearch] so users can find places outside the curated
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
