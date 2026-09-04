package com.astrochart.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.astrochart.core.i18n.Language
import com.astrochart.core.i18n.Translations
import com.astrochart.core.panchangam.PanchangamNames
import com.astrochart.data.db.entities.SavedMatchEntity
import com.astrochart.ui.components.CelestialCard
import com.astrochart.ui.i18n.LocalLanguage
import com.astrochart.ui.i18n.LocalStrings
import com.astrochart.ui.i18n.PoruthamStrings
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary
import java.time.format.DateTimeFormatter

private fun savedAtFormatter(lang: Language): DateTimeFormatter =
    DateTimeFormatter.ofPattern(
        if (lang == Language.ZH) "yyyy年M月d日" else "d MMM yyyy",
        lang.locale
    )

/**
 * The matches a user chose to keep, newest first.
 *
 * Each row shows the stored score rather than recomputing it, which is the
 * whole reason `total` is denormalised onto [SavedMatchEntity]; opening a match
 * recomputes the twelve kootas from the two (rasi, nakshatra) pairs, so the
 * breakdown always reflects the current scoring rules even for an old row.
 *
 * Stateless, like [SavedChartsScreen] — the caller owns the list and the two
 * callbacks, so this composable can be driven straight from a test.
 */
@Composable
fun SavedMatchesScreen(
    matches: List<SavedMatchEntity>,
    onMatchSelected: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val ps = PoruthamStrings.forLanguage(LocalLanguage.current)

    if (matches.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = ps.noSavedMatches,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(matches, key = { it.id }) { match ->
            SavedMatchCard(
                match = match,
                onOpen = { onMatchSelected(match.id) },
                onDelete = { onDelete(match.id) }
            )
        }
    }
}

@Composable
private fun SavedMatchCard(
    match: SavedMatchEntity,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val lang = LocalLanguage.current
    val ps = PoruthamStrings.forLanguage(lang)
    val strings = LocalStrings.current
    var confirmDelete by remember { mutableStateOf(false) }

    CelestialCard {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${match.groomName} · ${match.brideName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldDeep,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = personLine(match.groomRasi, match.groomNakshatra, lang),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )
                Text(
                    text = personLine(match.brideRasi, match.brideNakshatra, lang),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = match.savedAt.format(savedAtFormatter(lang)),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Text(
                text = "${match.total}/40",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { confirmDelete = true }) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = strings.delete,
                    tint = TextMuted
                )
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(ps.deleteMatchConfirm) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) {
                    Text(strings.delete, color = GoldDeep)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(strings.cancel, color = TextMuted)
                }
            }
        )
    }
}

private fun personLine(rasi: Int, nakshatra: Int, lang: Language): String {
    val sign = Translations.signName(SIGN_ORDER[rasi], lang)
    val nak = PanchangamNames.nakshatras[nakshatra].get(lang)
    return "$sign · $nak"
}
