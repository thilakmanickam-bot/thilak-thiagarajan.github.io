package com.astrochart.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.astrochart.data.GeoPlace
import com.astrochart.data.LocationOption
import com.astrochart.data.LocationSearch
import com.astrochart.ui.theme.CardBorder
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary
import kotlinx.coroutines.delay

private const val MIN_QUERY_LENGTH = 2
private const val DEBOUNCE_MS = 250L

/**
 * Editable, debounced typeahead over the worldwide [LocationSearch] dataset
 * (~69k cities/towns/villages, offline). Unlike the read-only
 * [LabeledDropdown], the anchor field is a normal text field: a place not
 * found in the dataset can still be typed freely, and the caller
 * (`BirthInputScreen`) keeps its time-zone override field as the fallback
 * for that case.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    var expanded by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<GeoPlace>>(emptyList()) }
    var hasSearched by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
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

    val menuVisible = expanded && query.trim().length >= MIN_QUERY_LENGTH

    ExposedDropdownMenuBox(
        expanded = menuVisible,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                expanded = true
            },
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
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = menuVisible,
            onDismissRequest = { expanded = false }
        ) {
            if (results.isEmpty()) {
                if (hasSearched) {
                    DropdownMenuItem(text = { Text(noResultsText) }, onClick = {}, enabled = false)
                }
            } else {
                results.forEach { place ->
                    DropdownMenuItem(
                        text = {
                            Text(place.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        onClick = {
                            query = place.displayName
                            expanded = false
                            onSelected(place.toLocationOption())
                        }
                    )
                }
            }
        }
    }
}
