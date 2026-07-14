package com.astrochart.core.utils

object ZodiacUtils {
    private val SIGNS = listOf(
        "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
        "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    )

    private val ELEMENTS = listOf("Fire", "Earth", "Air", "Water")
    private val MODALITIES = listOf("Cardinal", "Fixed", "Mutable")

    fun getSignFromLongitude(lon: Double): String {
        val normalized = ((lon % 360) + 360) % 360
        val signIndex = (normalized / 30).toInt()
        return SIGNS.getOrElse(signIndex) { "Aries" }
    }

    fun getElement(sign: String): String {
        val signIndex = SIGNS.indexOf(sign)
        return if (signIndex >= 0) ELEMENTS[signIndex % 4] else "Air"
    }

    fun getModality(sign: String): String {
        val signIndex = SIGNS.indexOf(sign)
        return if (signIndex >= 0) MODALITIES[signIndex % 3] else "Cardinal"
    }

    fun formatPosition(lon: Double): Pair<Int, Int> {
        val normalized = ((lon % 360) + 360) % 360
        val within = normalized - ((normalized / 30).toInt() * 30)
        val degrees = within.toInt()
        val minutes = ((within - degrees) * 60).toInt()
        return Pair(degrees, minutes)
    }

    fun getAllSigns(): List<String> = SIGNS
}
