package com.astrochart.core.interpret

/** Life area each of the twelve houses governs. */
object HouseInfo {
    private val area: Map<Int, String> = mapOf(
        1 to "self, identity, and how you appear",
        2 to "money, resources, and values",
        3 to "communication, learning, and siblings",
        4 to "home, family, and roots",
        5 to "creativity, romance, and play",
        6 to "work, routines, and health",
        7 to "partnership and close relationships",
        8 to "intimacy, shared resources, and transformation",
        9 to "beliefs, higher learning, and travel",
        10 to "career, reputation, and public life",
        11 to "friendships, community, and hopes",
        12 to "the inner world, rest, and the unconscious"
    )

    fun of(house: Int): String = area[house] ?: "an important area of life"
}
