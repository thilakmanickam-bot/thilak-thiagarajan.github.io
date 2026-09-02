package com.astrochart.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.astrochart.ui.theme.LocalWindowSizeClass

/**
 * Caps content to a comfortable max width and centers it horizontally, so
 * phone screens (Compact width) fill exactly as before but no longer stretch
 * edge-to-edge on tablets/laptops. On Medium/Expanded windows the cap is
 * skipped entirely, so the full window width is usable — needed by wide
 * layouts like the Rasi Palan sign grid (up to 6 columns) and its two-pane
 * tablet layout, and a reasonable low-risk win for every other screen too
 * (less centered dead space on tablets). Applied once around the app's nav
 * content in `MainActivity`; reach for it again around any composable (e.g. a
 * chart) that needs its own tighter cap regardless of window size.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ResponsiveContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 600.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isCompact = LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Compact
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = if (isCompact) {
                Modifier.fillMaxHeight().widthIn(max = maxWidth).fillMaxWidth()
            } else {
                Modifier.fillMaxHeight().fillMaxWidth()
            },
            content = content
        )
    }
}
