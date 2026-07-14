package com.astrochart.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.astrochart.ui.theme.CardBorder
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.GoldLight
import com.astrochart.ui.theme.OnGold

/** Primary action: gold-gradient pill with dark text. Disabled dims, never hides. */
@Composable
fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    trailingIcon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .heightIn(min = 54.dp)
            .clip(CircleShape)
            .alpha(if (enabled) 1f else 0.45f)
            .background(Brush.horizontalGradient(listOf(GoldLight, GoldDeep)))
            .clickable(enabled = enabled && !loading, onClick = onClick)
            .padding(horizontal = 28.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = OnGold,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = text.uppercase(),
                color = OnGold,
                style = MaterialTheme.typography.labelLarge
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(10.dp))
                Icon(imageVector = trailingIcon, contentDescription = null, tint = OnGold)
            }
        }
    }
}

/** Secondary action: transparent pill with a gold border and gold text. */
@Composable
fun OutlineGoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .heightIn(min = 54.dp)
            .clip(CircleShape)
            .alpha(if (enabled) 1f else 0.45f)
            .border(BorderStroke(1.dp, CardBorder), CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 28.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text.uppercase(),
            color = GoldDeep,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
