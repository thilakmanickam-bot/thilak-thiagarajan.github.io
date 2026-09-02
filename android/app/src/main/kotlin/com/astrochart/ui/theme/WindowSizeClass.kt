package com.astrochart.ui.theme

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.compositionLocalOf

/**
 * The activity's current window size class (Compact/Medium/Expanded), used to
 * decide when a screen should switch to a wider or two-pane layout. Provided
 * once in `MainActivity.setContent` from `calculateWindowSizeClass`, which
 * reads real window metrics — unlike a `BoxWithConstraints` read taken inside
 * [com.astrochart.ui.components.ResponsiveContainer]'s capped content tree,
 * this reflects the true window size regardless of any width cap applied
 * further down the composition.
 *
 * `compositionLocalOf` (not `staticCompositionLocalOf`): the value legitimately
 * changes across the activity's lifetime (rotation, a resizable/freeform
 * window, a foldable fold/unfold), and readers must recompose on that change.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass> {
    error("LocalWindowSizeClass not provided — set it in MainActivity.setContent")
}
