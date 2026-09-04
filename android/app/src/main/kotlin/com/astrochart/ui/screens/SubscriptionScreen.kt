package com.astrochart.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.astrochart.Features
import com.astrochart.ads.Premium
import com.astrochart.billing.BillingManager
import com.astrochart.billing.PRODUCT_ID_MONTHLY
import com.astrochart.billing.SubscriptionOption
import com.astrochart.ui.components.CelestialCard
import com.astrochart.ui.components.GoldButton
import com.astrochart.ui.i18n.LocalStrings
import com.astrochart.ui.i18n.UiStrings
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.OnGold
import com.astrochart.ui.theme.TextMuted
import kotlinx.coroutines.launch

/**
 * Halo Premium: the real purchase flow once [Features.BILLING_ENABLED] is on
 * (see `BillingManager`, `functions/src/billing.ts`); otherwise the original
 * "coming soon" teaser, so this screen degrades gracefully before the Play
 * Console products / Cloud Function are actually set up.
 */
@Composable
fun SubscriptionScreen(
    onNavigateToTester: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    var isPremium by remember { mutableStateOf(Premium.isActive(context)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Icon(
            imageVector = if (isPremium) Icons.Filled.CheckCircle else Icons.Filled.WorkspacePremium,
            contentDescription = null,
            tint = GoldDeep,
            modifier = Modifier.height(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isPremium) strings.premiumActiveTitle else strings.premiumHeadline,
            style = MaterialTheme.typography.headlineSmall,
            color = GoldDeep,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))

        when {
            isPremium -> {
                Text(
                    text = strings.premiumActiveDesc,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
            !Features.BILLING_ENABLED -> {
                ComingSoonBadge(text = strings.premiumComingSoon)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = strings.premiumSubtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                Text(
                    text = strings.premiumSubtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        PerkCard(
            icon = Icons.Filled.Block,
            title = strings.premiumPerkAdFree,
            description = strings.premiumPerkAdFreeDesc
        )
        Spacer(modifier = Modifier.height(12.dp))
        PerkCard(
            icon = Icons.Filled.AutoAwesome,
            title = strings.premiumPerkChat,
            description = strings.premiumPerkChatDesc
        )

        Spacer(modifier = Modifier.height(28.dp))

        if (Features.BILLING_ENABLED && !isPremium) {
            PurchaseSection(onPurchased = { isPremium = true })
        } else if (!Features.BILLING_ENABLED) {
            Text(
                text = strings.premiumNotifyNote,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }

        // Deliberately *not* gated on Features.BILLING_ENABLED. That flag is
        // false until the Play products exist, and testers are exactly the
        // people needed before then — gating this with the purchase flow would
        // hide it precisely when it is the only way in.
        if (!isPremium) {
            Spacer(modifier = Modifier.height(24.dp))
            CelestialCard(modifier = Modifier.clickable(onClick = onNavigateToTester)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Science, contentDescription = null, tint = GoldDeep)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = strings.testerEntry,
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldDeep
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.testerEntryDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/** The monthly/yearly picker + purchase button, shown once billing is live. */
@Composable
private fun PurchaseSection(onPurchased: () -> Unit) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val billingManager = remember { BillingManager(context) }

    var options by remember { mutableStateOf<List<SubscriptionOption>>(emptyList()) }
    var selectedProductId by remember { mutableStateOf(PRODUCT_ID_MONTHLY) }
    var purchasing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        options = billingManager.querySubscriptionOptions()
        if (options.isNotEmpty() && options.none { it.productId == selectedProductId }) {
            selectedProductId = options.first().productId
        }
    }

    if (options.isEmpty()) {
        CircularProgressIndicator(color = GoldDeep)
        return
    }

    options.forEach { option ->
        val label = "${periodLabel(option.billingPeriodIso, strings)} — ${option.formattedPrice}"
        ChoiceRow(
            label = label,
            selected = option.productId == selectedProductId,
            onSelect = { selectedProductId = option.productId }
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (error) {
        Text(
            text = strings.premiumPurchaseError,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    val activity = context as? Activity
    GoldButton(
        text = strings.premiumSubscribe,
        enabled = activity != null && !purchasing,
        loading = purchasing,
        onClick = {
            val option = options.firstOrNull { it.productId == selectedProductId } ?: return@GoldButton
            val hostActivity = activity ?: return@GoldButton
            purchasing = true
            error = false
            scope.launch {
                billingManager.launchPurchase(hostActivity, option) { purchase ->
                    scope.launch {
                        val verified = billingManager.acknowledgeAndVerify(purchase)
                        purchasing = false
                        if (verified?.active == true) onPurchased() else error = true
                    }
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

/** "Monthly"/"Yearly" from Play's ISO-8601 billing period (e.g. "P1M", "P1Y"). */
private fun periodLabel(billingPeriodIso: String, strings: UiStrings): String =
    when (billingPeriodIso) {
        "P1Y" -> strings.premiumYearly
        else -> strings.premiumMonthly
    }

/** Small gold pill, reused by the onboarding wizard's tier step. */
@Composable
internal fun ComingSoonBadge(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = OnGold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(GoldDeep)
            .padding(horizontal = 14.dp, vertical = 5.dp)
    )
}

@Composable
private fun PerkCard(icon: ImageVector, title: String, description: String) {
    CelestialCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = GoldDeep)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldDeep
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}
