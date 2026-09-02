package com.astrochart.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.astrochart.core.i18n.Language
import com.astrochart.ui.components.SectionDivider
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary
import com.astrochart.ui.theme.fontFamilyForLanguage

/**
 * First-launch language chooser: lists every supported [Language] in its own
 * script so a new user can pick before seeing the app. [onSelect] fires with the
 * chosen language; [onSkip] keeps the default. Shown once, then never again.
 */
@Composable
fun LanguagePickerDialog(
    onSelect: (Language) -> Unit,
    onSkip: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onSkip,
        confirmButton = {
            TextButton(onClick = onSkip) { Text("Skip", color = TextMuted) }
        },
        title = { Text("Select Your Preferred Language", color = GoldDeep) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Choose your language to get started",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
                SectionDivider(modifier = Modifier.padding(vertical = 8.dp))
                Language.entries.forEach { l ->
                    Text(
                        text = l.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = fontFamilyForLanguage(l)),
                        color = TextPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(l) }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        }
    )
}
