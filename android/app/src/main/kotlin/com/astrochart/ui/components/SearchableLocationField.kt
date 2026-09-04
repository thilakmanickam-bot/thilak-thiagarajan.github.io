package com.astrochart.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.astrochart.data.GeoPlace
import com.astrochart.data.LocationOption
import com.astrochart.data.LocationSearch
import com.astrochart.ui.theme.CardBorder
import com.astrochart.ui.theme.CardFill
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary
import kotlinx.coroutines.delay

private const val MIN_QUERY_LENGTH = 2
private const val DEBOUNCE_MS = 250L

/**
 * Editable, debounced typeahead over the worldwide [LocationSearch] dataset
 * (~235k cities/towns/villages, offline). Unlike the read-only
 * [LabeledDropdown], the anchor field is a normal text field: a place not
 * found in the dataset can still be typed freely, and the caller
 * (`BirthInputScreen`) keeps its time-zone override field as the fallback
 * for that case.
 *
 * Renders the suggestion list in normal layout flow (a [Surface] right
 * below the field, inside the caller's scrolling [Column]) rather than via
 * [androidx.compose.material3.ExposedDropdownMenu]'s separate popup window.
 * That popup was cutting the IME connection on some keyboards — the field
 * would render fine but backspace/typing stopped reaching it after the
 * first suggestion appeared — and, with the keyboard open, it could flip to
 * render *above* the field and cover earlier inputs instead of the list
 * appearing where the user is looking.
 */
@Composable
fun SearchableLocationField(
    label: String,
    placeholder: String,
    selected: LocationOption?,
    onSelected: (LocationOption) -> Unit,
    noResultsText: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf(selected?.displayName.orEmpty()) }
    var results by remember { mutableStateOf<List<GeoPlace>>(emptyList()) }
    var hasSearched by remember { mutableStateOf(false) }
    // Set right before a selection sets `query` to the picked place's own
    // display name, so the resulting re-search (which would just find that
    // same place again) doesn't reopen the list immediately after picking.
    var suppressNextSearch by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        if (suppressNextSearch) {
            suppressNextSearch = false
            return@LaunchedEffect
        }
        val trimmed = query.trim()
        if (trimmed.length < MIN_QUERY_LENGTH) {
            results = emptyList()
            hasSearched = false
            return@LaunchedEffect
        }
        delay(DEBOUNCE_MS)
        results = LocationSearch.search(context, trimmed)
        hasSearched = true
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldDeep,
                unfocusedBorderColor = CardBorder,
                focusedLabelColor = GoldDeep,
                unfocusedLabelColor = TextMuted,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = GoldDeep
            ),
            modifier = Modifier.fillMaxWidth()
        )

        val showSuggestions = query.trim().length >= MIN_QUERY_LENGTH && (results.isNotEmpty() || hasSearched)
        if (showSuggestions) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CardFill,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                if (results.isEmpty()) {
                    Text(
                        text = noResultsText,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState())
                    ) {
                        results.forEach { place ->
                            Text(
                                text = place.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        suppressNextSearch = true
                                        query = place.displayName
                                        results = emptyList()
                                        hasSearched = false
                                        onSelected(place.toLocationOption())
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
