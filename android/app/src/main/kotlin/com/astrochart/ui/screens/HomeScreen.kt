package com.astrochart.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.astrochart.ui.components.EyebrowLabel
import com.astrochart.ui.components.GoldButton
import com.astrochart.ui.components.MoonPhaseRow
import com.astrochart.ui.components.OutlineGoldButton
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary

@Composable
fun HomeScreen(
    onNavigateToBirthInput: () -> Unit,
    onNavigateToSavedCharts: () -> Unit,
    onNavigateToSample: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = buildAnnotatedString {
        withStyle(SpanStyle(color = TextPrimary)) { append("Explore your\n") }
        withStyle(SpanStyle(color = GoldDeep)) { append("natal chart") }
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

        EyebrowLabel(text = "Your cosmic guide", icon = Icons.Filled.AutoAwesome)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enter your birth details and see your placements, aspects, and elemental balance — computed on your device.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        GoldButton(
            text = "Calculate My Chart",
            onClick = onNavigateToBirthInput,
            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlineGoldButton(
            text = "View Saved Charts",
            onClick = onNavigateToSavedCharts,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlineGoldButton(
            text = "Sample Chart",
            onClick = onNavigateToSample,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}
