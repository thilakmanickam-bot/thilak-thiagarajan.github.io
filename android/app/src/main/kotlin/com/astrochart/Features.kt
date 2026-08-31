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

    /**
     * Banner ads on the basic (ad-supported) tier. When true, a banner shows to
     * non-Premium viewers using the test ad unit in [com.astrochart.ads.Ads]
     * (swap in real AdMob IDs before serving live inventory). Set to false to
     * hide ads entirely.
     */
    const val ADS_ENABLED = true

    /**
     * Google Play in-app updates. When true, the app asks Play on launch whether
     * a newer version is live and, if so, shows Play's built-in flexible
     * "update available" flow so already-installed users can update to get new
     * features. Requires no backend and is a no-op for non-Play installs.
     */
    const val IN_APP_UPDATE_ENABLED = true

    /**
     * Account login (Google / Facebook) with cloud profile + saved-chart sync via
     * Firebase Auth + Firestore. Ships **disabled**: the Settings "Account" entry
     * and all Firebase calls are gated on this flag, so the placeholder
     * `google-services.json` is inert until a real Firebase project is configured.
     * Flip to `true` once the real `google-services.json` and console setup
     * (Google provider, Firestore rules, SHA-1 fingerprints) are in place.
     */
    const val AUTH_ENABLED = false

    /**
     * Facebook login inside the Account screen. Kept off until the Facebook
     * Developer app, the Firebase Facebook provider, the Facebook SDK + manifest
     * entries, and Facebook's app review are all completed. While false, the
     * Facebook button renders disabled with a "coming soon" hint.
     */
    const val FACEBOOK_LOGIN_ENABLED = false
}
