package com.astrochart.core.interpret

data class SignDetail(
    val element: String,
    val modality: String,
    val keywords: String,
    val glyph: String
)

/** Per-sign traits + zodiac glyph, reused by the reading engine and the chart wheel. */
object SignInfo {

    val details: Map<String, SignDetail> = mapOf(
        "Aries" to SignDetail("Fire", "Cardinal", "bold, direct, and pioneering", "♈"),
        "Taurus" to SignDetail("Earth", "Fixed", "steady, sensual, and grounded", "♉"),
        "Gemini" to SignDetail("Air", "Mutable", "curious, versatile, and talkative", "♊"),
        "Cancer" to SignDetail("Water", "Cardinal", "nurturing, protective, and intuitive", "♋"),
        "Leo" to SignDetail("Fire", "Fixed", "warm, expressive, and proud", "♌"),
        "Virgo" to SignDetail("Earth", "Mutable", "precise, practical, and analytical", "♍"),
        "Libra" to SignDetail("Air", "Cardinal", "balanced, relational, and diplomatic", "♎"),
        "Scorpio" to SignDetail("Water", "Fixed", "intense, private, and transformative", "♏"),
        "Sagittarius" to SignDetail("Fire", "Mutable", "adventurous, honest, and expansive", "♐"),
        "Capricorn" to SignDetail("Earth", "Cardinal", "disciplined, ambitious, and patient", "♑"),
        "Aquarius" to SignDetail("Air", "Fixed", "independent, inventive, and humane", "♒"),
        "Pisces" to SignDetail("Water", "Mutable", "imaginative, compassionate, and dreamy", "♓")
    )

    fun of(sign: String): SignDetail = details[sign] ?: details.getValue("Aries")
}
