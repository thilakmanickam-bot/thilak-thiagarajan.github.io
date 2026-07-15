package com.astrochart.core.interpret

import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals

class AgeUtilTest {

    @Test
    fun years_countsCompletedYears() {
        val birth = LocalDateTime.of(1990, 7, 20, 14, 30)
        assertEquals(35, AgeUtil.years(birth, LocalDate.of(2025, 7, 20))) // exactly 35 years
        assertEquals(34, AgeUtil.years(birth, LocalDate.of(2025, 7, 19))) // day before birthday
        assertEquals(35, AgeUtil.years(birth, LocalDate.of(2025, 7, 21)))
    }

    @Test
    fun years_neverNegative() {
        val birth = LocalDateTime.of(2030, 1, 1, 0, 0)
        assertEquals(0, AgeUtil.years(birth, LocalDate.of(2025, 1, 1)))
    }
}
