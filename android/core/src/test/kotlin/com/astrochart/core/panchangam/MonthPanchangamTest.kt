package com.astrochart.core.panchangam

import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId
import kotlin.test.assertEquals

/**
 * Vratham-day scan for Chennai, August 2026, checked against a published Tamil
 * almanac (Nithra): Amavasai 12 · Pournami 27 · Ekadasi 9,23 · Sashti 4,18 ·
 * Chaturthi 16 · Sankatahara 2,31 · Pradosham 10,25 · Sivarathiri 11 ·
 * Thiruvonam 26.
 */
class MonthPanchangamTest {

    private val lat = 13.0827
    private val lon = 80.2707
    private val zone = ZoneId.of("Asia/Kolkata")

    private fun days(key: String): List<Int> {
        val month = YearMonth.of(2026, 8)
        val group = MonthPanchangam.vrathaDays(month, lat, lon, zone).firstOrNull { it.key == key }
        return group?.dates?.map { it.dayOfMonth } ?: emptyList()
    }

    @Test
    fun sunriseObservances_matchAlmanac() {
        assertEquals(listOf(12), days("amavasai"))
        assertEquals(listOf(9, 23), days("ekadasi"))
        assertEquals(listOf(4, 18), days("sashti"))
        assertEquals(listOf(16), days("chaturthi"))
        assertEquals(listOf(11), days("sivarathiri"))
        assertEquals(listOf(26), days("thiruvonam"))
    }

    @Test
    fun eveningObservances_useSunset_andMatchAlmanac() {
        assertEquals(listOf(27), days("pournami"))
        assertEquals(listOf(10, 25), days("pradosham"))
        assertEquals(listOf(2, 31), days("sankatahara"))
    }

    @Test
    fun moonMarks_flagNewAndFull() {
        val marks = MonthPanchangam.moonMarks(YearMonth.of(2026, 8), lat, lon, zone)
        assertEquals(MoonMark.NEW, marks[12])
        assertEquals(MoonMark.FULL, marks[27])
        // A plain day carries no marker.
        assertEquals(null, marks[15])
    }

    @Test
    fun everyDateFallsInsideTheMonth() {
        val month = YearMonth.of(2026, 8)
        MonthPanchangam.vrathaDays(month, lat, lon, zone).forEach { group ->
            group.dates.forEach { d ->
                assertEquals(month, YearMonth.from(d), "${group.key} date $d outside month")
            }
        }
    }
}
