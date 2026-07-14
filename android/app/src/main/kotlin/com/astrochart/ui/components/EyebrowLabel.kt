package com.astrochart.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.astrochart.ui.theme.GoldDeep

/** Gold, uppercase, wide-tracked section label with an optional leading icon. */
@Composable
fun EyebrowLabel(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldDeep,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text.uppercase(),
            color = GoldDeep,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
