package com.astrochart.core.interpret

/**
 * South-Indian marriage matching ("Porutham" / Dasa-koota), scored out of 40.
 *
 * The match is computed from each partner's **rasi** (moon-sign, 0 = Aries) and
 * **nakshatra** (birth star, 0 = Ashwini) alone — no birth time is needed, which
 * matches how Tamil almanac apps collect the inputs. Each of the twelve kootas
 * contributes up to a fixed maximum; a koota is "present" when it scores above
 * zero and "absent" (often a dosha) when it scores zero.
 *
 * The rule set is validated end-to-end against the reference case of identical
 * partners (both Virgo / Hasta), which yields 26/40 with exactly
 * Dina/Mahendra/StreeDeergha/Rajju/Nadi absent and the rest full — see
 * PoruthamTest. Pure logic, no Android dependencies, fully unit-tested.
 */
enum class Koota(val key: String, val max: Int) {
    DINA("dina", 3),
    GANA("gana", 6),
    MAHENDRA("mahendra", 1),
    STREE_DEERGHA("streeDeergha", 1),
    YONI("yoni", 4),
    RASI("rasi", 7),
    RASI_ADHIPATHI("rasiAdhipathi", 5),
    VASYA("vasya", 2),
    RAJJU("rajju", 1),
    VEDHA("vedha", 1),
    VARNA("varna", 1),
    NADI("nadi", 8);

    companion object {
        /** The 40-point maximum, in the order shown on screen. */
        val TOTAL_MAX: Int = entries.sumOf { it.max }
    }
}

/** One koota's outcome. [present] is true when [gained] > 0. */
data class KootaScore(val koota: Koota, val gained: Int) {
    val present: Boolean get() = gained > 0
}

/** Full porutham result: the per-koota scores plus the 0..40 total. */
data class PoruthamResult(
    val scores: List<KootaScore>,
    val total: Int,
    val max: Int = Koota.TOTAL_MAX
) {
    /** Whether a critical dosha (Rajju or Nadi absent) is present. */
    val hasCriticalDosha: Boolean
        get() = scores.any { (it.koota == Koota.RAJJU || it.koota == Koota.NADI) && !it.present }
}

object Porutham {

    // --- Reference tables (index by 0-based nakshatra 0..26 unless noted) ---

    /** Gana: 0 = Deva, 1 = Manushya, 2 = Rakshasa. */
    private val GANA = intArrayOf(
        0, 1, 2, 1, 0, 1, 0, 0, 2,
        2, 1, 1, 0, 2, 0, 2, 0, 2,
        2, 1, 1, 0, 2, 2, 1, 1, 0
    )

    /** Nadi: 0 = Aadi (Vata), 1 = Madhya (Pitta), 2 = Antya (Kapha). */
    private val NADI = intArrayOf(
        0, 1, 2, 2, 1, 0, 0, 1, 2,
        2, 1, 0, 0, 1, 2, 2, 1, 0,
        0, 1, 2, 2, 1, 0, 0, 1, 2
    )

    /** Yoni animal code per nakshatra (14 animals). */
    private val YONI = intArrayOf(
        0, 1, 2, 3, 3, 4, 5, 2, 5,   // Ashwini..Ashlesha
        6, 6, 7, 8, 9, 8, 9, 10, 10, // Magha..Jyeshtha
        4, 11, 12, 11, 13, 0, 13, 7, 1 // Mula..Revati
    )

    /** Mortal-enemy yoni animal pairs (score 0). */
    private val YONI_ENEMIES = setOf(
        setOf(7, 9),   // Cow – Tiger
        setOf(1, 13),  // Elephant – Lion
        setOf(0, 8),   // Horse – Buffalo
        setOf(4, 10),  // Dog – Deer
        setOf(3, 12),  // Serpent – Mongoose
        setOf(11, 2),  // Monkey – Sheep
        setOf(5, 6)    // Cat – Rat
    )

    /** Rajju group per nakshatra: zig-zag foot→head→foot (0..4). */
    private val RAJJU_WAVE = intArrayOf(0, 1, 2, 3, 4, 3, 2, 1, 0)
    private fun rajju(nak: Int): Int = RAJJU_WAVE[nak % 9]

    /** Mutual vedha (obstruction) nakshatra pairs. */
    private val VEDHA_PAIRS = setOf(
        setOf(0, 17), setOf(1, 16), setOf(2, 15), setOf(3, 14), setOf(4, 22),
        setOf(5, 21), setOf(6, 20), setOf(7, 19), setOf(8, 18), setOf(9, 26),
        setOf(10, 25), setOf(11, 24), setOf(12, 23)
    )

    // --- Rasi (0-based, 0 = Aries) tables ---

    /** Planetary lord per rasi. 0 Sun,1 Moon,2 Mars,3 Mercury,4 Jupiter,5 Venus,6 Saturn. */
    private val RASI_LORD = intArrayOf(2, 5, 3, 1, 0, 3, 5, 2, 4, 6, 6, 4)

    /** Varna per rasi: 3 Brahmin > 2 Kshatriya > 1 Vaishya > 0 Shudra. */
    private val VARNA = intArrayOf(2, 1, 0, 3, 2, 1, 0, 3, 2, 1, 0, 3)

    /** Natural planetary friends (Parashari), indexed by planet code 0..6. */
    private val PLANET_FRIENDS = arrayOf(
        setOf(1, 2, 4),    // Sun -> Moon, Mars, Jupiter
        setOf(0, 3),       // Moon -> Sun, Mercury
        setOf(0, 1, 4),    // Mars -> Sun, Moon, Jupiter
        setOf(0, 5),       // Mercury -> Sun, Venus
        setOf(0, 1, 2),    // Jupiter -> Sun, Moon, Mars
        setOf(3, 6),       // Venus -> Mercury, Saturn
        setOf(3, 5)        // Saturn -> Mercury, Venus
    )
    private val PLANET_ENEMIES = arrayOf(
        setOf(5, 6),       // Sun -> Venus, Saturn
        setOf<Int>(),      // Moon -> none
        setOf(3),          // Mars -> Mercury
        setOf(1),          // Mercury -> Moon
        setOf(3, 5),       // Jupiter -> Mercury, Venus
        setOf(0, 1),       // Venus -> Sun, Moon
        setOf(0, 1, 2)     // Saturn -> Sun, Moon, Mars
    )

    /** Vasya: signs each rasi holds sway over. */
    private val VASYA = arrayOf(
        setOf(4, 7), setOf(3, 6), setOf(5), setOf(7, 8), setOf(6), setOf(11, 2),
        setOf(9, 5), setOf(3, 4), setOf(11), setOf(10, 0), setOf(0), setOf(9)
    )

    /**
     * Compute the porutham for a bride/groom given each partner's 0-based rasi
     * (0 = Aries) and nakshatra (0 = Ashwini). The distinction between the two
     * partners only matters for the directional kootas (Dina, Mahendra,
     * StreeDeergha, Varna), which count from the girl's star/sign to the boy's.
     */
    fun compute(
        boyRasi: Int, boyNakshatra: Int,
        girlRasi: Int, girlNakshatra: Int
    ): PoruthamResult {
        val scores = listOf(
            KootaScore(Koota.DINA, dina(girlNakshatra, boyNakshatra)),
            KootaScore(Koota.GANA, gana(boyNakshatra, girlNakshatra)),
            KootaScore(Koota.MAHENDRA, mahendra(girlNakshatra, boyNakshatra)),
            KootaScore(Koota.STREE_DEERGHA, streeDeergha(girlNakshatra, boyNakshatra)),
            KootaScore(Koota.YONI, yoni(boyNakshatra, girlNakshatra)),
            KootaScore(Koota.RASI, bhakoot(boyRasi, girlRasi)),
            KootaScore(Koota.RASI_ADHIPATHI, grahaMaitri(boyRasi, girlRasi)),
            KootaScore(Koota.VASYA, vasya(boyRasi, girlRasi)),
            KootaScore(Koota.RAJJU, rajjuScore(boyNakshatra, girlNakshatra)),
            KootaScore(Koota.VEDHA, vedha(boyNakshatra, girlNakshatra)),
            KootaScore(Koota.VARNA, varna(boyRasi, girlRasi)),
            KootaScore(Koota.NADI, nadi(boyNakshatra, girlNakshatra))
        )
        return PoruthamResult(scores = scores, total = scores.sumOf { it.gained })
    }

    /** Count of stars from [from] to [to] inclusive, over the 27-star cycle (1..27). */
    private fun count(from: Int, to: Int): Int = ((to - from + 27) % 27) + 1

    private fun dina(girl: Int, boy: Int): Int {
        val rem = count(girl, boy) % 9
        return if (rem == 0 || rem % 2 == 0) 3 else 0
    }

    private fun gana(boy: Int, girl: Int): Int {
        val b = GANA[boy]; val g = GANA[girl]
        if (b == g) return 6
        val pair = setOf(b, g)
        return when (pair) {
            setOf(0, 1) -> 5 // Deva – Manushya
            setOf(1, 2) -> 3 // Manushya – Rakshasa
            else -> 0        // Deva – Rakshasa
        }
    }

    private fun mahendra(girl: Int, boy: Int): Int =
        if (count(girl, boy) in intArrayOf(4, 7, 10, 13, 16, 19, 22, 25)) 1 else 0

    private fun streeDeergha(girl: Int, boy: Int): Int =
        if (count(girl, boy) > 9) 1 else 0

    private fun yoni(boy: Int, girl: Int): Int {
        val a = YONI[boy]; val b = YONI[girl]
        return when {
            a == b -> 4
            setOf(a, b) in YONI_ENEMIES -> 0
            else -> 2
        }
    }

    private fun bhakoot(boy: Int, girl: Int): Int {
        val d = (girl - boy + 12) % 12
        // 2/12 (d = 1 or 11) and 6/8 (d = 5 or 7) are doshas.
        return if (d == 1 || d == 11 || d == 5 || d == 7) 0 else 7
    }

    private fun grahaMaitri(boy: Int, girl: Int): Int {
        val lb = RASI_LORD[boy]; val lg = RASI_LORD[girl]
        if (lb == lg) return 5
        val r1 = relation(lb, lg); val r2 = relation(lg, lb)
        return when (r1 + r2) {
            4 -> 5 // both friends
            3 -> 4 // friend + neutral
            2 -> 3 // both neutral (or friend+enemy averaged low-mid)
            1 -> 1 // neutral + enemy
            else -> 0 // both enemies
        }
    }

    /** 2 = friend, 1 = neutral, 0 = enemy (of [a] toward [b]). */
    private fun relation(a: Int, b: Int): Int = when (b) {
        in PLANET_FRIENDS[a] -> 2
        in PLANET_ENEMIES[a] -> 0
        else -> 1
    }

    private fun vasya(boy: Int, girl: Int): Int {
        if (boy == girl) return 2
        val boyHoldsGirl = girl in VASYA[boy]
        val girlHoldsBoy = boy in VASYA[girl]
        return when {
            boyHoldsGirl && girlHoldsBoy -> 2
            boyHoldsGirl || girlHoldsBoy -> 1
            else -> 0
        }
    }

    private fun rajjuScore(boy: Int, girl: Int): Int =
        if (rajju(boy) == rajju(girl)) 0 else 1

    private fun vedha(boy: Int, girl: Int): Int =
        if (setOf(boy, girl) in VEDHA_PAIRS) 0 else 1

    private fun varna(boy: Int, girl: Int): Int =
        if (VARNA[boy] >= VARNA[girl]) 1 else 0

    private fun nadi(boy: Int, girl: Int): Int =
        if (NADI[boy] == NADI[girl]) 0 else 8
}
