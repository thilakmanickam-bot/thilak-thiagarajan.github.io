package com.astrochart.ads

/**
 * AdMob ad-unit configuration.
 *
 * To serve **live, revenue-earning** ads, paste your real ad-unit IDs from the
 * AdMob console into [REAL_BANNER_UNIT_ID] and [REAL_INTERSTITIAL_UNIT_ID]
 * below. While those are blank, the app uses Google's official **test** units,
 * which render sample ads and earn nothing (never click real-looking ads on a
 * test unit, and never ship live traffic on a test unit).
 *
 * The AdMob **application ID** is set separately, via the `ADMOB_APP_ID` gradle
 * property in `android/gradle.properties`, because it is injected into the
 * manifest at build time.
 *
 * Ads only render when [com.astrochart.Features.ADS_ENABLED] is true and the
 * viewer is not on [Premium].
 */
object Ads {
    // ---- Paste your real AdMob ad-unit IDs here (leave blank to use test). ----
    private const val REAL_BANNER_UNIT_ID = ""
    private const val REAL_INTERSTITIAL_UNIT_ID = ""

    // Google's official test ad units — safe fallbacks, no revenue.
    private const val TEST_BANNER_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTERSTITIAL_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    /** Effective banner unit: your real one if set, else the test unit. */
    val BANNER_UNIT_ID: String get() = REAL_BANNER_UNIT_ID.ifBlank { TEST_BANNER_UNIT_ID }

    /** Effective interstitial unit: your real one if set, else the test unit. */
    val INTERSTITIAL_UNIT_ID: String get() = REAL_INTERSTITIAL_UNIT_ID.ifBlank { TEST_INTERSTITIAL_UNIT_ID }

    /** True while still on Google's test units (useful for banners/labels). */
    val usingTestAds: Boolean get() = REAL_BANNER_UNIT_ID.isBlank()
}

/**
 * Whether the current viewer has the ad-free Premium tier. There is no billing
 * yet (Premium is "coming soon"), so everyone is on the basic, ad-supported
 * tier. When the subscription ships, back [isActive] with the real entitlement.
 */
object Premium {
    const val isActive: Boolean = false
}
