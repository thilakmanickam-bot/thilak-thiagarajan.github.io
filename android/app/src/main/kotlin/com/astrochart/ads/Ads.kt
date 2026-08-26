package com.astrochart.ads

/**
 * Ad configuration. These are Google's official **test** IDs — they render
 * sample ads and generate no revenue. Swap them for your real AdMob application
 * ID (in the manifest) and ad-unit IDs here when you're ready to serve live
 * inventory. Ads only show at all when [com.astrochart.Features.ADS_ENABLED] is
 * true and the viewer is not on [Premium].
 */
object Ads {
    /** Test AdMob application ID (also referenced from the manifest meta-data). */
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"

    /** Test banner ad-unit ID. */
    const val BANNER_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
}

/**
 * Whether the current viewer has the ad-free Premium tier. There is no billing
 * yet (Premium is "coming soon"), so everyone is on the basic, ad-supported
 * tier. When the subscription ships, back [isActive] with the real entitlement.
 */
object Premium {
    const val isActive: Boolean = false
}
