package com.astrochart.core.models

import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Birth data for natal chart computation.
 * @param dateTime Birth date and time in local timezone
 * @param latitude Geographic latitude (N positive, S negative)
 * @param longitude Geographic longitude (E positive, W negative)
 * @param timeZone Timezone identifier (e.g., "America/Los_Angeles")
 * @param gender Optional self-described gender; not used in computation, shown
 *   with the profile. Defaults to empty for backward compatibility.
 */
data class BirthData(
    val dateTime: LocalDateTime,
    val latitude: Double,
    val longitude: Double,
    val timeZone: ZoneId,
    val locationName: String = "",
    val gender: String = ""
) {
    fun toUTC(): LocalDateTime {
        return dateTime.atZone(timeZone).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()
    }
}
