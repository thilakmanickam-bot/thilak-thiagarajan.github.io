package com.astrochart.core.interpret

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

/** Age from a birth date, in completed years. */
object AgeUtil {
    fun years(birth: LocalDateTime, now: LocalDate = LocalDate.now()): Int =
        Period.between(birth.toLocalDate(), now).years.coerceAtLeast(0)
}
