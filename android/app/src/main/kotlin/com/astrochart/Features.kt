package com.astrochart

/**
 * Simple compile-time feature flags. Flip a flag and rebuild to enable/disable
 * a feature across the app.
 */
object Features {
    /**
     * The "Ask the Universe" AI astrologer chat. Temporarily disabled for the
     * initial Play Store release; set to `true` to bring it back (its entry point
     * and navigation are gated on this flag).
     */
    const val CHAT_ENABLED = false
}
