package com.astrochart.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.GoldLight

/** The three-phase gold moon motif (new / half / full) used on hero sections. */
@Composable
fun MoonPhaseRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Moon(fill = 0f)
        Moon(fill = 0.5f)
        Moon(fill = 1f)
    }
}

@Composable
private fun Moon(fill: Float) {
    Canvas(modifier = Modifier.size(14.dp)) {
        val r = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(color = GoldDeep, radius = r, center = center, style = Stroke(width = 1.5f))
        if (fill >= 1f) {
            drawCircle(color = GoldLight, radius = r, center = center)
        } else if (fill > 0f) {
            // Fill the right half for the "half" phase.
            clipRect(left = center.x, top = 0f, right = size.width, bottom = size.height, clipOp = ClipOp.Intersect) {
                drawCircle(color = GoldLight, radius = r, center = center)
            }
        }
    }
}
