package com.astrochart.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * A completed marriage match, kept so it can be reopened later.
 *
 * **Only the inputs are stored, not the koota scores.** A porutham result is a
 * pure function of the two (rasi, nakshatra) pairs — see
 * [com.astrochart.core.interpret.Porutham.compute] — so persisting the twelve
 * per-koota outcomes would duplicate something already derivable, and would
 * silently go stale if a scoring rule were ever corrected. Reopening a saved
 * match recomputes it, so it always reflects the current rule set.
 *
 * [total] is the one deliberate exception: it is denormalised purely so the
 * saved-matches list can show each score without recomputing every row.
 *
 * The birth-detail columns are nullable because entering a birth date and
 * place is optional — the porutham itself needs only rasi and nakshatra. They
 * exist so a saved match can redraw each person's chart, which is only
 * possible for people whose full details were given.
 */
@Entity(tableName = "saved_matches")
data class SavedMatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val groomName: String,
    val brideName: String,

    /** 0-based: 0 = Aries for rasi, 0 = Ashwini for nakshatra. */
    val groomRasi: Int,
    val groomNakshatra: Int,
    val brideRasi: Int,
    val brideNakshatra: Int,

    /** Denormalised from [com.astrochart.core.interpret.PoruthamResult.total]. */
    val total: Int,

    val savedAt: LocalDateTime,

    val groomBirthDateTime: LocalDateTime? = null,
    val groomLatitude: Double? = null,
    val groomLongitude: Double? = null,
    val groomTimeZone: String? = null,
    val groomLocationName: String? = null,

    val brideBirthDateTime: LocalDateTime? = null,
    val brideLatitude: Double? = null,
    val brideLongitude: Double? = null,
    val brideTimeZone: String? = null,
    val brideLocationName: String? = null
) {
    /** True when this person's chart can be drawn from what was stored. */
    val groomHasBirthDetails: Boolean
        get() = groomBirthDateTime != null && groomLatitude != null &&
            groomLongitude != null && groomTimeZone != null

    val brideHasBirthDetails: Boolean
        get() = brideBirthDateTime != null && brideLatitude != null &&
            brideLongitude != null && brideTimeZone != null
}
