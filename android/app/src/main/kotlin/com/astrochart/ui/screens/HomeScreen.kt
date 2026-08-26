package com.astrochart.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.astrochart.BuildConfig
import com.astrochart.Features
import com.astrochart.ui.components.EyebrowLabel
import com.astrochart.ui.components.GoldButton
import com.astrochart.ui.components.MoonPhaseRow
import com.astrochart.ui.components.OutlineGoldButton
import com.astrochart.ui.i18n.CompatibilityStrings
import com.astrochart.ui.i18n.LocalLanguage
import com.astrochart.ui.i18n.LocalStrings
import com.astrochart.ui.i18n.PanchangamStrings
import com.astrochart.ui.i18n.RasiStrings
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary
import java.time.Year

@Composable
fun HomeScreen(
    onNavigateToBirthInput: () -> Unit,
    onNavigateToSavedCharts: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToPanchangam: () -> Unit,
    onNavigateToCompatibility: () -> Unit,
    onNavigateToRasi: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val lang = LocalLanguage.current
    val title = buildAnnotatedString {
        withStyle(SpanStyle(color = TextPrimary)) { append(strings.homeTitleLine1 + "\n") }
        withStyle(SpanStyle(color = GoldDeep)) { append(strings.homeTitleLine2) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        MoonPhaseRow()

        Spacer(modifier = Modifier.height(28.dp))

        EyebrowLabel(text = strings.homeEyebrow, icon = Icons.Filled.AutoAwesome)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = strings.homeSubtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        GoldButton(
            text = strings.navCalculate,
            onClick = onNavigateToBirthInput,
            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlineGoldButton(
            text = strings.homeViewSaved,
            onClick = onNavigateToSavedCharts,
            modifier = Modifier.fillMaxWidth()
        )

        if (Features.CHAT_ENABLED) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlineGoldButton(
                text = strings.chatEntry,
                onClick = onNavigateToChat,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlineGoldButton(
            text = RasiStrings.forLanguage(lang).entry,
            onClick = onNavigateToRasi,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlineGoldButton(
            text = CompatibilityStrings.forLanguage(lang).entry,
            onClick = onNavigateToCompatibility,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlineGoldButton(
            text = strings.premiumEntry,
            onClick = onNavigateToPremium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        SwipeUpHandle(onOpen = onNavigateToPanchangam)

        Spacer(modifier = Modifier.height(24.dp))

        AppFooter(modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * A bottom affordance that reveals the panchangam/calendar features: swipe up
 * on it (or tap it) to open today's panchangam.
 */
@Composable
private fun SwipeUpHandle(onOpen: () -> Unit) {
    val lang = LocalLanguage.current
    val ps = remember(lang) { PanchangamStrings.forLanguage(lang) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .pointerInput(Unit) {
                var total = 0f
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (total < -48f) onOpen()
                        total = 0f
                    },
                    onVerticalDrag = { _, dy -> total += dy }
                )
            }
    ) {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowUp,
            contentDescription = null,
            tint = GoldDeep
        )
        Text(
            text = ps.swipeHint,
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}

/** App version and attribution shown at the bottom of the home screen. */
@Composable
private fun AppFooter(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Built by Techbyt",
            style = MaterialTheme.typography.labelSmall,
            color = GoldDeep
        )
        Text(
            text = "© ${Year.now().value} Techbyt. All rights reserved.",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}
