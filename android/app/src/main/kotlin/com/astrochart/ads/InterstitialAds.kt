package com.astrochart.ads

import android.app.Activity
import android.content.Context
import com.astrochart.Features
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * A single, reusable interstitial ad for the basic tier. Preload once, then show
 * it at a natural break (e.g. after a match calculation); it reloads itself for
 * next time. Every entry point is guarded by [Features.ADS_ENABLED] and
 * [Premium], so it is a silent no-op when ads are off, and it never throws.
 */
object InterstitialAds {

    private var ad: InterstitialAd? = null
    private var loading = false
    private var shownCount = 0

    /** Load an interstitial in the background if one isn't ready already. */
    fun preload(context: Context) {
        if (!Features.ADS_ENABLED || Premium.isActive(context) || ad != null || loading) return
        loading = true
        runCatching {
            InterstitialAd.load(
                context,
                Ads.INTERSTITIAL_UNIT_ID,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(loaded: InterstitialAd) {
                        ad = loaded
                        loading = false
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        ad = null
                        loading = false
                    }
                }
            )
        }.onFailure { loading = false }
    }

    /**
     * Show the interstitial roughly once every [everyNth] eligible moments so it
     * stays unobtrusive. Reloads afterwards. Returns true if an ad was shown.
     */
    fun maybeShow(activity: Activity, everyNth: Int = 2): Boolean {
        if (!Features.ADS_ENABLED || Premium.isActive(activity)) return false
        shownCount++
        if (shownCount % everyNth != 0) return false
        val current = ad ?: run { preload(activity); return false }
        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                ad = null
                preload(activity)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                ad = null
                preload(activity)
            }
        }
        ad = null
        runCatching { current.show(activity) }.onFailure { return false }
        return true
    }
}
