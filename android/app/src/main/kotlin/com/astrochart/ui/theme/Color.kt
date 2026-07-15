package com.astrochart.ui.theme

import androidx.compose.ui.graphics.Color

// Celestial palette (dark-only), derived from the reference design language.

// Background gradient: indigo-purple glow at top -> deep navy at bottom.
val AstroBgTop = Color(0xFF241A54)     // indigo glow
val AstroBgMid = Color(0xFF141138)     // mid navy
val AstroBgBottom = Color(0xFF08061C)  // near-black navy
val AstroGlow = Color(0xFF3A2A78)      // purple radial glow

// Gold / amber accent (gradient stops).
val GoldLight = Color(0xFFF6DFA0)
val GoldDeep = Color(0xFFD9A94E)

// Text.
val TextPrimary = Color(0xFFF5F3EE)    // warm near-white
val TextMuted = Color(0xFFBFB9D4)      // lavender-gray body
val OnGold = Color(0xFF1A1330)         // dark text on gold buttons

// Surfaces.
val CardFill = Color(0xFF1B1747)       // translucent-looking indigo card
val CardBorder = Color(0x59D9A94E)     // gold at ~35% alpha
val Star = Color(0xFFF5F3EE)           // star dots
val AstroError = Color(0xFFE5837A)     // soft rose for errors
