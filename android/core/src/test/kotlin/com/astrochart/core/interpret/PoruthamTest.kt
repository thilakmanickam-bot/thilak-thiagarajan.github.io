package com.astrochart.core.interpret

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PoruthamTest {

    private fun scoreOf(r: PoruthamResult, k: Koota): Int =
        r.scores.first { it.koota == k }.gained

    /**
     * Reference case from the Nithra-style match screen: a bride and groom who
     * are both Virgo (rasi 5) / Hasta (nakshatra 12) score exactly 26/40, with
     * Dina, Mahendra, StreeDeergha, Rajju and Nadi absent and every other koota
     * full. This pins the whole rule set.
     */
    @Test
    fun identicalVirgoHastaScores26() {
        val r = Porutham.compute(boyRasi = 5, boyNakshatra = 12, girlRasi = 5, girlNakshatra = 12)

        assertEquals(0, scoreOf(r, Koota.DINA))
        assertEquals(6, scoreOf(r, Koota.GANA))
        assertEquals(0, scoreOf(r, Koota.MAHENDRA))
        assertEquals(0, scoreOf(r, Koota.STREE_DEERGHA))
        assertEquals(4, scoreOf(r, Koota.YONI))
        assertEquals(7, scoreOf(r, Koota.RASI))
        assertEquals(5, scoreOf(r, Koota.RASI_ADHIPATHI))
        assertEquals(2, scoreOf(r, Koota.VASYA))
        assertEquals(0, scoreOf(r, Koota.RAJJU))
        assertEquals(1, scoreOf(r, Koota.VEDHA))
        assertEquals(1, scoreOf(r, Koota.VARNA))
        assertEquals(0, scoreOf(r, Koota.NADI))

        assertEquals(26, r.total)
        assertEquals(40, r.max)
        assertTrue(r.hasCriticalDosha) // Rajju + Nadi both absent
    }

    @Test
    fun maxIsForty() {
        assertEquals(40, Koota.TOTAL_MAX)
    }

    @Test
    fun presentMatchesPositiveScore() {
        val r = Porutham.compute(5, 12, 5, 12)
        r.scores.forEach { s ->
            assertEquals(s.gained > 0, s.present, "present flag for ${s.koota}")
        }
    }

    @Test
    fun nadiPresentForDifferentNadiStars() {
        // Ashwini (Aadi) vs Bharani (Madhya): different nadi -> full 8, no dosha.
        val r = Porutham.compute(boyRasi = 0, boyNakshatra = 0, girlRasi = 1, girlNakshatra = 1)
        assertEquals(8, scoreOf(r, Koota.NADI))
    }

    @Test
    fun rajjuPresentForDifferentGroups() {
        // Ashwini (foot) vs Bharani (waist): different rajju -> present.
        val r = Porutham.compute(0, 0, 1, 1)
        assertEquals(1, scoreOf(r, Koota.RAJJU))
        assertFalse(r.scores.first { it.koota == Koota.RAJJU }.present.not())
    }

    @Test
    fun bhakootDoshaForSixEight() {
        // Aries (0) and Virgo (5) are 6/8 apart -> Rasi koota dosha (0).
        val r = Porutham.compute(boyRasi = 0, boyNakshatra = 0, girlRasi = 5, girlNakshatra = 12)
        assertEquals(0, scoreOf(r, Koota.RASI))
    }

    @Test
    fun totalNeverExceedsMax() {
        for (br in 0..11) for (bn in 0..26) {
            val r = Porutham.compute(br, bn, (br + 3) % 12, (bn + 5) % 27)
            assertTrue(r.total in 0..40, "total ${r.total} out of range")
        }
    }
}
