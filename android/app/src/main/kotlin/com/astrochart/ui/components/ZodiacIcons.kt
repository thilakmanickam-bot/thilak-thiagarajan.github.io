package com.astrochart.ui.components

import androidx.annotation.DrawableRes
import com.astrochart.R

/**
 * Maps a zodiac sign index (0=Aries … 11=Pisces, matching
 * [com.astrochart.core.utils.ZodiacUtils.getAllSigns]) to its minimalist gold
 * line-icon drawable, for rendering above the sign name on the Rasi Palan
 * screens.
 */
@DrawableRes
fun zodiacIconRes(signIndex: Int): Int = when (signIndex.coerceIn(0, 11)) {
    0 -> R.drawable.ic_zodiac_aries
    1 -> R.drawable.ic_zodiac_taurus
    2 -> R.drawable.ic_zodiac_gemini
    3 -> R.drawable.ic_zodiac_cancer
    4 -> R.drawable.ic_zodiac_leo
    5 -> R.drawable.ic_zodiac_virgo
    6 -> R.drawable.ic_zodiac_libra
    7 -> R.drawable.ic_zodiac_scorpio
    8 -> R.drawable.ic_zodiac_sagittarius
    9 -> R.drawable.ic_zodiac_capricorn
    10 -> R.drawable.ic_zodiac_aquarius
    else -> R.drawable.ic_zodiac_pisces
}
