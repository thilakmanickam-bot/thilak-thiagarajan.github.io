package com.astrochart.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.astrochart.Features
import com.astrochart.ads.Ads
import com.astrochart.ads.Premium
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * A banner ad for the basic (ad-supported) tier. Renders nothing when ads are
 * disabled by flag or the viewer is on Premium, so callers can place it
 * unconditionally. Uses the test ad unit until real AdMob IDs are dropped in.
 *
 * This is Scaffold's `bottomBar`, so Material3 routes the bottom system-bar
 * (navigation bar) inset here expecting it to be consumed — without
 * `.navigationBarsPadding()` on every branch (including the "renders
 * nothing" one below) that inset is silently dropped and every screen's
 * content clips under the 3-button nav bar.
 */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    if (!Features.ADS_ENABLED || Premium.isActive) {
        Spacer(modifier = modifier.fillMaxWidth().navigationBarsPadding())
        return
    }

    AndroidView(
        modifier = modifier.fillMaxWidth().navigationBarsPadding(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = Ads.BANNER_UNIT_ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
