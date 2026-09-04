package com.astrochart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.astrochart.core.i18n.Language
import com.astrochart.core.i18n.Translations
import com.astrochart.core.interpret.KootaScore
import com.astrochart.core.interpret.Porutham
import com.astrochart.core.interpret.PoruthamResult
import com.astrochart.core.models.BirthData
import com.astrochart.core.models.ChartStyle
import com.astrochart.core.models.NatalChart
import com.astrochart.core.panchangam.Panchangam
import com.astrochart.core.panchangam.PanchangamNames
import com.astrochart.core.utils.ChartCalculator
import com.astrochart.data.LocationOption
import com.astrochart.data.db.entities.SavedMatchEntity
import com.astrochart.data.repository.SavedMatchRepository
import com.astrochart.ui.components.CelestialCard
import com.astrochart.ui.components.LabeledDropdown
import com.astrochart.ui.components.NatalWheel
import com.astrochart.ui.components.SearchableLocationField
import com.astrochart.ui.components.SectionDivider
import com.astrochart.ui.components.SouthIndianChartView
import com.astrochart.ui.export.MatchPdf
import com.astrochart.ui.export.MatchSheet
import com.astrochart.ui.i18n.LocalChartStyle
import com.astrochart.ui.i18n.LocalLanguage
import com.astrochart.ui.i18n.LocalStrings
import com.astrochart.ui.i18n.PoruthamStrings
import com.astrochart.ui.theme.CardBorder
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.Month
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle

private val PresentGreen = Color(0xFF3B9C5A)
private val AbsentRed = Color(0xFFD1495B)

/**
 * One person's inputs.
 *
 * Rasi and nakshatram can be chosen by hand, or worked out from a birth date,
 * time and place. [derived] holds the worked-out pair and, when it is present,
 * takes precedence over the hand-picked values — so filling in birth details
 * never silently disagrees with what the screen goes on to score.
 *
 * Every part of the birth instant starts unset. A defaulted date would let
 * someone pick only a birthplace and be handed a rasi for midnight on the 1st
 * of January, which looks like an answer and is not one.
 */
private class PersonInput {
    var name by mutableStateOf("")
    var pickedRasi by mutableStateOf<Int?>(null)
    var pickedNak by mutableStateOf<Int?>(null)
    var showBirthDetails by mutableStateOf(false)
    var day by mutableStateOf<Int?>(null)
    var month by mutableStateOf<Int?>(null)
    var year by mutableStateOf<Int?>(null)
    var hour by mutableStateOf<Int?>(null)
    var minute by mutableStateOf<Int?>(null)
    var location by mutableStateOf<LocationOption?>(null)
    var derived by mutableStateOf<Pair<Int, Int>?>(null)

    /**
     * Bumped by [clearBirthDetails]. SearchableLocationField keeps the typed
     * query in its own un-keyed `remember`, so without this the text of a
     * cleared birthplace would stay on screen with nothing selected behind it.
     */
    var clearToken by mutableStateOf(0)

    /** The birth instant, or null while any part of it is still unchosen. */
    val birthDateTime: LocalDateTime?
        get() {
            val y = year ?: return null
            val mo = month ?: return null
            val d = day ?: return null
            val h = hour ?: return null
            val mi = minute ?: return null
            // Choosing the 31st and then February leaves an impossible date
            // standing for one frame — the clamp that fixes it runs after the
            // composition that reads this. Treating that frame as "not yet a
            // date" is the difference between a redraw and a crash.
            return runCatching { LocalDateTime.of(y, mo, d, h, mi) }.getOrNull()
        }

    val zone: ZoneId?
        get() = location?.let { runCatching { ZoneId.of(it.zoneId) }.getOrNull() }

    val rasi: Int? get() = derived?.first ?: pickedRasi
    val nak: Int? get() = derived?.second ?: pickedNak

    /**
     * Everything a natal chart needs, or null when the birth details are
     * incomplete. Unlike [birthDateTime] this does want the coordinates: house
     * cusps and the ascendant depend on where on Earth you were standing.
     */
    fun birthData(): BirthData? {
        val at = birthDateTime ?: return null
        val place = location ?: return null
        val z = zone ?: return null
        return BirthData(
            dateTime = at,
            latitude = place.latitude,
            longitude = place.longitude,
            timeZone = z,
            locationName = place.displayName
        )
    }

    fun clearBirthDetails() {
        day = null; month = null; year = null; hour = null; minute = null
        location = null; derived = null
        clearToken++
    }

    /**
     * Refills this person from a saved row.
     *
     * The stored rasi and nakshatra go into the hand-picked slots so a match
     * saved without birth details reopens complete. Where birth details were
     * stored they are restored too, and [derived] then recomputes from them —
     * to the same pair, since it is the same arithmetic on the same instant.
     */
    fun restore(
        name: String,
        rasi: Int,
        nakshatra: Int,
        birthDateTime: LocalDateTime?,
        latitude: Double?,
        longitude: Double?,
        timeZone: String?,
        locationName: String?
    ) {
        this.name = name
        pickedRasi = rasi
        pickedNak = nakshatra
        if (birthDateTime == null || latitude == null || longitude == null || timeZone == null) {
            return
        }
        year = birthDateTime.year
        month = birthDateTime.monthValue
        day = birthDateTime.dayOfMonth
        hour = birthDateTime.hour
        minute = birthDateTime.minute
        location = locationFrom(locationName.orEmpty(), latitude, longitude, timeZone)
        showBirthDetails = true
    }
}

/**
 * Rebuilds a [LocationOption] from the single display name a saved match
 * stores. Splitting at the *last* ", " round-trips [LocationOption.displayName]
 * exactly, including city names that contain a comma of their own.
 */
internal fun locationFrom(
    displayName: String,
    latitude: Double,
    longitude: Double,
    zoneId: String
): LocationOption {
    val split = displayName.lastIndexOf(", ")
    return LocationOption(
        city = if (split >= 0) displayName.take(split) else displayName,
        country = if (split >= 0) displayName.substring(split + 2) else "",
        latitude = latitude,
        longitude = longitude,
        zoneId = zoneId
    )
}

/**
 * Canonical zodiac-sign order; index = rasi index (0 = Aries), matching
 * [com.astrochart.data.db.entities.SavedMatchEntity.groomRasi]. `internal` so
 * [SavedMatchesScreen] resolves a saved row's rasi the same way rather than
 * keeping a second copy of the list.
 */
internal val SIGN_ORDER = listOf(
    "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
    "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
)

/**
 * Marriage match-making: the user gives each partner's name, rasi and
 * nakshatram — by hand, or by entering birth details the app derives them from
 * — and the screen shows the South-Indian 40-point porutham with a per-koota
 * breakdown and present/absent verdicts.
 *
 * Still self-contained: the scoring is pure ([Porutham.compute]) and reads no
 * saved charts, so a match can always be run on two nakshatras alone. Natal
 * charts appear under the result only for a person whose birth details were
 * actually given, which is why each is nullable rather than assumed.
 */
@Composable
fun CompatibilityScreen(
    onNavigateToPremium: () -> Unit,
    onNavigateToSavedMatches: () -> Unit = {},
    initialMatchId: Long? = null,
    modifier: Modifier = Modifier
) {
    val lang = LocalLanguage.current
    val ps = remember(lang) { PoruthamStrings.forLanguage(lang) }
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val repository = remember(context) { SavedMatchRepository(context) }
    // The PDF has to be told which style to draw; on screen each chart card
    // reads this for itself.
    val chartStyle = LocalChartStyle.current

    val groom = remember { PersonInput() }
    val bride = remember { PersonInput() }
    var result by remember { mutableStateOf<PoruthamResult?>(null) }
    var shownGroom by remember { mutableStateOf("") }
    var shownBride by remember { mutableStateOf("") }
    var shownGroomRasi by remember { mutableStateOf(0) }
    var shownBrideRasi by remember { mutableStateOf(0) }
    var shownGroomNak by remember { mutableStateOf(0) }
    var shownBrideNak by remember { mutableStateOf(0) }
    // Null for anyone matched on a hand-picked rasi and nakshatram: there is no
    // birth moment to draw a chart from, and inventing one would be worse than
    // showing nothing.
    var shownGroomChart by remember { mutableStateOf<NatalChart?>(null) }
    var shownBrideChart by remember { mutableStateOf<NatalChart?>(null) }
    // The row this result is stored as, or null while it is unsaved. Also what
    // stops the same match being saved twice by a second tap.
    var savedId by remember { mutableStateOf<Long?>(null) }

    val ready = groom.rasi != null && groom.nak != null && bride.rasi != null && bride.nak != null

    // Shared by the Calculate button and by reopening a saved match, so a
    // reopened match shows exactly what a freshly computed one does.
    val showMatch: () -> Unit = {
        val groomRasi = groom.rasi
        val groomNak = groom.nak
        val brideRasi = bride.rasi
        val brideNak = bride.nak
        if (groomRasi != null && groomNak != null && brideRasi != null && brideNak != null) {
            result = Porutham.compute(groomRasi, groomNak, brideRasi, brideNak)
            shownGroom = groom.name.ifBlank { ps.groomName }
            shownBride = bride.name.ifBlank { ps.brideName }
            shownGroomRasi = groomRasi; shownBrideRasi = brideRasi
            shownGroomNak = groomNak; shownBrideNak = brideNak
            // Computed once, here, rather than in composition — the results
            // below recompose on scroll and on a chart-style change, and this
            // is the one genuinely expensive step.
            shownGroomChart = groom.birthData()?.let { ChartCalculator.calculateNatalChart(it) }
            shownBrideChart = bride.birthData()?.let { ChartCalculator.calculateNatalChart(it) }
            savedId = null
        }
    }

    LaunchedEffect(initialMatchId) {
        val id = initialMatchId ?: return@LaunchedEffect
        val match = repository.getById(id) ?: return@LaunchedEffect
        groom.restore(
            match.groomName, match.groomRasi, match.groomNakshatra,
            match.groomBirthDateTime, match.groomLatitude, match.groomLongitude,
            match.groomTimeZone, match.groomLocationName
        )
        bride.restore(
            match.brideName, match.brideRasi, match.brideNakshatra,
            match.brideBirthDateTime, match.brideLatitude, match.brideLongitude,
            match.brideTimeZone, match.brideLocationName
        )
        showMatch()
        // showMatch clears this, so it is set after: the row already exists and
        // reopening it must not offer to store a duplicate.
        savedId = id
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = ps.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onNavigateToSavedMatches, modifier = Modifier.fillMaxWidth()) {
            Text(ps.savedMatches, color = GoldDeep, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(4.dp))

        // Headings carry no emoji: the celestial-gold system in
        // docs/DESIGN_SYSTEM.md has its own vocabulary for this, and the
        // bride/groom glyphs rendered as whatever each device's font happened
        // to supply — full-colour, off-palette, and inconsistent between
        // phones.
        PersonCard(
            heading = ps.groomDetails,
            person = groom,
            namePlaceholder = ps.enterGroomName,
            ps = ps, lang = lang
        )
        Spacer(Modifier.height(16.dp))
        PersonCard(
            heading = ps.brideDetails,
            person = bride,
            namePlaceholder = ps.enterBrideName,
            ps = ps, lang = lang
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                showMatch()
                activity?.let { com.astrochart.ads.InterstitialAds.maybeShow(it) }
            },
            enabled = ready,
            colors = ButtonDefaults.buttonColors(containerColor = GoldDeep, contentColor = Color.White),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(ps.calculate, style = MaterialTheme.typography.titleMedium)
        }

        if (!ready) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = ps.fillAll,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        result?.let { r ->
            Spacer(Modifier.height(24.dp))
            ResultHeader(
                r, ps, lang,
                shownGroom, shownBride, shownGroomRasi, shownBrideRasi, shownGroomNak, shownBrideNak
            )
            shownGroomChart?.let {
                Spacer(Modifier.height(16.dp))
                PersonChartCard(shownGroom, it)
            }
            shownBrideChart?.let {
                Spacer(Modifier.height(16.dp))
                PersonChartCard(shownBride, it)
            }
            Spacer(Modifier.height(16.dp))
            KutaTable(r, ps)
            Spacer(Modifier.height(16.dp))
            r.scores.forEach { s ->
                KootaDetailCard(s, ps)
                Spacer(Modifier.height(10.dp))
            }
            SummaryCard(r, ps)
            Spacer(Modifier.height(16.dp))

            val alreadySaved = savedId != null
            OutlinedButton(
                onClick = {
                    if (!alreadySaved) {
                        scope.launch {
                            savedId = repository.save(
                                SavedMatchEntity(
                                    groomName = shownGroom,
                                    brideName = shownBride,
                                    groomRasi = shownGroomRasi,
                                    groomNakshatra = shownGroomNak,
                                    brideRasi = shownBrideRasi,
                                    brideNakshatra = shownBrideNak,
                                    total = r.total,
                                    savedAt = LocalDateTime.now(),
                                    groomBirthDateTime = groom.birthDateTime,
                                    groomLatitude = groom.location?.latitude,
                                    groomLongitude = groom.location?.longitude,
                                    groomTimeZone = groom.location?.zoneId,
                                    groomLocationName = groom.location?.displayName,
                                    brideBirthDateTime = bride.birthDateTime,
                                    brideLatitude = bride.location?.latitude,
                                    brideLongitude = bride.location?.longitude,
                                    brideTimeZone = bride.location?.zoneId,
                                    brideLocationName = bride.location?.displayName
                                )
                            )
                        }
                    }
                },
                enabled = !alreadySaved,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(
                    text = if (alreadySaved) ps.matchSaved else ps.saveMatch,
                    color = if (alreadySaved) TextMuted else GoldDeep,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    scope.launch {
                        val sheet = matchSheet(
                            ps, lang, chartStyle, r,
                            shownGroom, shownBride,
                            shownGroomRasi, shownBrideRasi,
                            shownGroomNak, shownBrideNak,
                            shownGroomChart, shownBrideChart
                        )
                        // Rendering a page and writing a file are both slow
                        // enough to drop frames on the main thread.
                        val file = withContext(Dispatchers.IO) { MatchPdf.write(context, sheet) }
                        context.startActivity(shareIntent(context, file, ps.exportPdf))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(ps.exportPdf, color = GoldDeep, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onNavigateToPremium,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(ps.askUniverse, color = GoldDeep, style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PersonCard(
    heading: String,
    person: PersonInput,
    namePlaceholder: String,
    ps: PoruthamStrings,
    lang: Language
) {
    // Recomputed only when the birth instant or its zone actually changes, so
    // the ephemeris isn't evaluated on every recomposition of the card.
    LaunchedEffect(person.birthDateTime, person.location?.zoneId) {
        val at = person.birthDateTime
        val zone = person.zone
        person.derived = if (at != null && zone != null) {
            Panchangam.moonRasiAndNakshatra(at, zone)
        } else {
            null
        }
    }

    CelestialCard {
        Text(heading, style = MaterialTheme.typography.titleMedium, color = GoldDeep, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        Text(ps.name, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        OutlinedTextField(
            value = person.name,
            onValueChange = { person.name = it },
            placeholder = { Text(namePlaceholder) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldDeep,
                unfocusedBorderColor = CardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = GoldDeep
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        BirthDetailsSection(person, ps)
        Spacer(Modifier.height(12.dp))

        val derived = person.derived
        if (derived != null) {
            // Showing the values rather than a disabled dropdown: a greyed-out
            // control invites tapping and then refuses, which reads as broken.
            DerivedValue(ps.rasi, Translations.signName(SIGN_ORDER[derived.first], lang), ps.derivedFromBirth)
            Spacer(Modifier.height(12.dp))
            DerivedValue(ps.nakshatram, PanchangamNames.nakshatras[derived.second].get(lang), ps.derivedFromBirth)
        } else {
            Text(ps.rasi, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            LabeledDropdown(
                label = ps.rasi,
                options = SIGN_ORDER.indices.toList(),
                selected = person.pickedRasi,
                optionLabel = { Translations.signName(SIGN_ORDER[it], lang) },
                onSelected = { person.pickedRasi = it },
                placeholder = ps.rasi,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            Text(ps.nakshatram, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            LabeledDropdown(
                label = ps.nakshatram,
                options = PanchangamNames.nakshatras.indices.toList(),
                selected = person.pickedNak,
                optionLabel = { PanchangamNames.nakshatras[it].get(lang) },
                onSelected = { person.pickedNak = it },
                placeholder = ps.nakshatram,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * The optional birth date, time and place. Collapsed by default: matching on a
 * rasi and nakshatram alone is the common case, and this is six more fields.
 */
@Composable
private fun BirthDetailsSection(person: PersonInput, ps: PoruthamStrings) {
    val strings = LocalStrings.current
    val lang = LocalLanguage.current

    val years = remember { (1900..Year.now().value).toList().reversed() }
    val daysInMonth = remember(person.year, person.month) {
        val y = person.year
        val m = person.month
        if (y != null && m != null) YearMonth.of(y, m).lengthOfMonth() else 31
    }
    // Picking the 31st and then February must not leave the 31st selected.
    LaunchedEffect(daysInMonth) {
        person.day?.let { if (it > daysInMonth) person.day = daysInMonth }
    }

    TextButton(
        onClick = { person.showBirthDetails = !person.showBirthDetails },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(ps.birthDetails, color = GoldDeep, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(6.dp))
        Icon(
            imageVector = if (person.showBirthDetails) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = null,
            tint = GoldDeep
        )
    }

    if (!person.showBirthDetails) return

    Text(
        text = ps.birthDetailsHint,
        style = MaterialTheme.typography.bodySmall,
        color = TextMuted,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(10.dp))

    Text(strings.dob, style = MaterialTheme.typography.bodySmall, color = TextMuted)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LabeledDropdown(
            label = strings.day,
            options = (1..daysInMonth).toList(),
            selected = person.day,
            optionLabel = { it.toString() },
            onSelected = { person.day = it },
            placeholder = strings.dropdownDefault,
            modifier = Modifier.weight(1f)
        )
        LabeledDropdown(
            label = strings.month,
            options = (1..12).toList(),
            selected = person.month,
            optionLabel = { Month.of(it).getDisplayName(TextStyle.SHORT, lang.locale) },
            onSelected = { person.month = it },
            placeholder = strings.dropdownDefault,
            modifier = Modifier.weight(1.4f)
        )
        LabeledDropdown(
            label = strings.year,
            options = years,
            selected = person.year,
            optionLabel = { it.toString() },
            onSelected = { person.year = it },
            placeholder = strings.dropdownDefault,
            modifier = Modifier.weight(1.2f)
        )
    }
    Spacer(Modifier.height(10.dp))

    Text(strings.tob, style = MaterialTheme.typography.bodySmall, color = TextMuted)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LabeledDropdown(
            label = strings.hour,
            options = (0..23).toList(),
            selected = person.hour,
            optionLabel = { it.toString().padStart(2, '0') },
            onSelected = { person.hour = it },
            placeholder = strings.dropdownDefault,
            modifier = Modifier.weight(1f)
        )
        LabeledDropdown(
            label = strings.minute,
            options = (0..59).toList(),
            selected = person.minute,
            optionLabel = { it.toString().padStart(2, '0') },
            onSelected = { person.minute = it },
            placeholder = strings.dropdownDefault,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(10.dp))

    Text(strings.pob, style = MaterialTheme.typography.bodySmall, color = TextMuted)
    key(person.clearToken) {
        SearchableLocationField(
            label = strings.location,
            placeholder = strings.searchCityHint,
            selected = person.location,
            onSelected = { person.location = it },
            noResultsText = strings.noLocationResults,
            modifier = Modifier.fillMaxWidth()
        )
    }

    // Only offered once something has been entered — otherwise it is a button
    // that clears nothing.
    if (person.location != null || person.day != null || person.hour != null) {
        TextButton(
            onClick = { person.clearBirthDetails() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(ps.clearBirthDetails, color = TextMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** A rasi or nakshatram the app worked out, shown in place of its dropdown. */
@Composable
private fun DerivedValue(label: String, value: String, caption: String) {
    Text(label, style = MaterialTheme.typography.bodySmall, color = TextMuted)
    Text(value, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    Text(caption, style = MaterialTheme.typography.bodySmall, color = GoldDeep)
}

@Composable
private fun ResultHeader(
    r: PoruthamResult,
    ps: PoruthamStrings,
    lang: Language,
    groom: String, bride: String,
    groomRasi: Int, brideRasi: Int,
    groomNak: Int, brideNak: Int
) {
    CelestialCard {
        Row(modifier = Modifier.fillMaxWidth()) {
            PersonSummary(ps.groomName, groom, Translations.signName(SIGN_ORDER[groomRasi], lang), PanchangamNames.nakshatras[groomNak].get(lang), Modifier.weight(1f))
            PersonSummary(ps.brideName, bride, Translations.signName(SIGN_ORDER[brideRasi], lang), PanchangamNames.nakshatras[brideNak].get(lang), Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        SectionDivider(modifier = Modifier.fillMaxWidth(), width = 200)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "${r.total}/${r.max}",
            fontSize = 46.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = ps.totalScore,
            style = MaterialTheme.typography.titleSmall,
            color = GoldDeep,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Assembles the export sheet from what the screen is already showing.
 *
 * Everything reaches [MatchPdf] already localized — koota names, the verdict,
 * the sign and nakshatram names — so the PDF never has to decide what anything
 * is called. `internal` so a test can build a sheet without a screen.
 */
internal fun matchSheet(
    ps: PoruthamStrings,
    lang: Language,
    style: ChartStyle,
    result: PoruthamResult,
    groomName: String,
    brideName: String,
    groomRasi: Int,
    brideRasi: Int,
    groomNak: Int,
    brideNak: Int,
    groomChart: NatalChart?,
    brideChart: NatalChart?
): MatchSheet = MatchSheet(
    title = ps.title,
    groom = MatchSheet.Person(
        role = ps.groomName,
        name = groomName,
        rasi = Translations.signName(SIGN_ORDER[groomRasi], lang),
        nakshatra = PanchangamNames.nakshatras[groomNak].get(lang),
        chart = groomChart
    ),
    bride = MatchSheet.Person(
        role = ps.brideName,
        name = brideName,
        rasi = Translations.signName(SIGN_ORDER[brideRasi], lang),
        nakshatra = PanchangamNames.nakshatras[brideNak].get(lang),
        chart = brideChart
    ),
    kootaHeading = ps.kuta,
    gainedHeading = ps.gained,
    maxHeading = ps.max,
    rows = result.scores.map { MatchSheet.Row(ps.kootaName(it.koota), it.gained, it.koota.max) },
    totalLabel = ps.totalScore,
    total = result.total,
    max = result.max,
    verdict = ps.summary(result.total, result.hasCriticalDosha),
    language = lang,
    style = style
)

/** ACTION_SEND for a file the app's FileProvider is allowed to hand out. */
private fun shareIntent(context: Context, file: File, chooserTitle: String): Intent {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        // The receiving app has no standing access to the provider; this grant
        // is what lets it read the one file, for the life of the Intent.
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return Intent.createChooser(send, chooserTitle)
}

/**
 * One person's natal chart, shown only when their birth details were given.
 *
 * Reads [LocalChartStyle] rather than taking a style parameter, so it follows
 * the Settings preference and redraws when it changes — the same wiring
 * [ChartDetailScreen] uses, and the reason a match does not need reopening
 * after switching style.
 */
@Composable
private fun PersonChartCard(name: String, chart: NatalChart) {
    val style = LocalChartStyle.current
    CelestialCard(
        modifier = Modifier.fillMaxWidth().widthIn(max = 460.dp),
        contentPadding = 16
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            color = GoldDeep,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        when (style) {
            ChartStyle.WESTERN_WHEEL -> NatalWheel(chart = chart, modifier = Modifier.fillMaxWidth())
            ChartStyle.SOUTH_INDIAN -> SouthIndianChartView(
                chart = chart,
                chartName = name,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PersonSummary(label: String, name: String, rasi: String, nak: String, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = GoldDeep)
        Text(name, style = MaterialTheme.typography.titleMedium, color = GoldDeep, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(rasi, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        Text(nak, style = MaterialTheme.typography.bodySmall, color = TextMuted)
    }
}

@Composable
private fun KutaTable(r: PoruthamResult, ps: PoruthamStrings) {
    CelestialCard {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(ps.kuta, style = MaterialTheme.typography.titleSmall, color = GoldDeep, modifier = Modifier.weight(2f))
            Text(ps.gained, style = MaterialTheme.typography.titleSmall, color = GoldDeep, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            Text(ps.max, style = MaterialTheme.typography.titleSmall, color = GoldDeep, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(6.dp))
        SectionDivider(modifier = Modifier.fillMaxWidth(), width = 200)
        Spacer(Modifier.height(6.dp))
        r.scores.forEach { s ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                Text(ps.kootaName(s.koota), style = MaterialTheme.typography.bodyLarge, color = TextPrimary, modifier = Modifier.weight(2f))
                Text("${s.gained}", style = MaterialTheme.typography.bodyLarge, color = if (s.present) PresentGreen else AbsentRed, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                Text("${s.koota.max}", style = MaterialTheme.typography.bodyLarge, color = TextMuted, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun KootaDetailCard(s: KootaScore, ps: PoruthamStrings) {
    CelestialCard {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = ps.kootaName(s.koota),
                style = MaterialTheme.typography.titleMedium,
                color = if (s.present) PresentGreen else AbsentRed,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            StatusPill(present = s.present, ps = ps)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = ps.description(s.koota, s.present),
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}

@Composable
private fun StatusPill(present: Boolean, ps: PoruthamStrings) {
    val bg = if (present) PresentGreen else AbsentRed
    Row(
        modifier = Modifier
            .background(color = bg, shape = RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (present) ps.present else ps.absent,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = if (present) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.width(16.dp)
        )
    }
}

@Composable
private fun SummaryCard(r: PoruthamResult, ps: PoruthamStrings) {
    CelestialCard {
        Text(
            text = ps.summary(r.total, r.hasCriticalDosha),
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            textAlign = TextAlign.Justify
        )
    }
}
