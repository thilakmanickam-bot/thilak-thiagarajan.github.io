package com.astrochart.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.astrochart.ui.theme.CardBorder

/** Short centered gold divider used under card titles. */
@Composable
fun SectionDivider(modifier: Modifier = Modifier, width: Int = 40) {
    HorizontalDivider(
        modifier = modifier
            .width(width.dp)
            .height(1.dp),
        color = CardBorder
    )
}
