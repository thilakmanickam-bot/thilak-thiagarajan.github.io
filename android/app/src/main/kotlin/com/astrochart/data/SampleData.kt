package com.astrochart.data

import com.astrochart.core.models.BirthData
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * A bundled sample so the app has something to show without user input.
 * Natalie Wood — July 20, 1938, 11:16 AM PST, San Francisco, CA (Rodden AA),
 * matching the natal-chart web project's reference data.
 */
object SampleData {
    fun sampleBirthData(): BirthData = BirthData(
        dateTime = LocalDateTime.of(1938, 7, 20, 11, 16),
        latitude = 37.7749,
        longitude = -122.4194,
        timeZone = ZoneId.of("America/Los_Angeles"),
        locationName = "San Francisco, CA (sample)"
    )
}
