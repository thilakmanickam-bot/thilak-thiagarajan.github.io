package com.astrochart.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.astrochart.ui.components.CelestialCard
import com.astrochart.ui.i18n.LocalStrings
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.OnGold
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary

/**
 * Halo Premium landing page — a "coming soon" teaser for the paid tier
 * (ad-free + the AI astrologer). No purchase flow yet; this is the placeholder
 * the premium entry points navigate to.
 */
@Composable
fun SubscriptionScreen(modifier: Modifier = Modifier) {
    val strings = LocalStrings.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Icon(
            imageVector = Icons.Filled.WorkspacePremium,
            contentDescription = null,
            tint = GoldDeep,
            modifier = Modifier.height(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = strings.premiumHeadline,
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        ComingSoonBadge(text = strings.premiumComingSoon)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = strings.premiumSubtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted,
            textAlign = TextAlign.Center
        )

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
        Text(
            text = strings.premiumNotifyNote,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ComingSoonBadge(text: String) {
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
                    color = TextPrimary
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
