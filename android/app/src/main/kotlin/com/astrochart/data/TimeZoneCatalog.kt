package com.astrochart.data

/**
 * A curated list of common IANA time-zone ids for the time-zone dropdown.
 * The selected location's zone is used by default; this list lets the user
 * override it. Any zone not in the list can be appended via [zonesIncluding].
 */
object TimeZoneCatalog {

    val commonZones: List<String> = listOf(
        "UTC",
        "Pacific/Honolulu",
        "America/Anchorage",
        "America/Los_Angeles",
        "America/Denver",
        "America/Chicago",
        "America/New_York",
        "America/Toronto",
        "America/Mexico_City",
        "America/Bogota",
        "America/Lima",
        "America/Sao_Paulo",
        "America/Argentina/Buenos_Aires",
        "America/Santiago",
        "Atlantic/Reykjavik",
        "Europe/London",
        "Europe/Dublin",
        "Europe/Lisbon",
        "Europe/Paris",
        "Europe/Berlin",
        "Europe/Madrid",
        "Europe/Rome",
        "Europe/Amsterdam",
        "Europe/Zurich",
        "Europe/Vienna",
        "Europe/Stockholm",
        "Europe/Athens",
        "Europe/Istanbul",
        "Europe/Moscow",
        "Africa/Casablanca",
        "Africa/Lagos",
        "Africa/Cairo",
        "Africa/Nairobi",
        "Africa/Johannesburg",
        "Asia/Jerusalem",
        "Asia/Riyadh",
        "Asia/Dubai",
        "Asia/Karachi",
        "Asia/Kathmandu",
        "Asia/Kolkata",
        "Asia/Colombo",
        "Asia/Dhaka",
        "Asia/Bangkok",
        "Asia/Jakarta",
        "Asia/Ho_Chi_Minh",
        "Asia/Singapore",
        "Asia/Kuala_Lumpur",
        "Asia/Hong_Kong",
        "Asia/Shanghai",
        "Asia/Manila",
        "Asia/Seoul",
        "Asia/Tokyo",
        "Australia/Perth",
        "Australia/Sydney",
        "Australia/Melbourne",
        "Pacific/Auckland"
    )

    /** Returns the common zones, guaranteeing [zoneId] is present and first-class. */
    fun zonesIncluding(zoneId: String): List<String> =
        if (commonZones.contains(zoneId)) commonZones else listOf(zoneId) + commonZones
}
