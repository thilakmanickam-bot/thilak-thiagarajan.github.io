package com.astrochart.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.astrochart.ui.theme.AstroBgBottom
import com.astrochart.ui.theme.CardBorder
import com.astrochart.ui.theme.GoldDeep

/** Circular dark disc with a gold ring and a centered gold line-icon. */
@Composable
fun IconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Int = 56
) {
    Surface(
        modifier = modifier.size(size.dp),
        shape = CircleShape,
        color = AstroBgBottom.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldDeep,
                modifier = Modifier.size((size * 0.42f).dp)
            )
        }
    }
}
