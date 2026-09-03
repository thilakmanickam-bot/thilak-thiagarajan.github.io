package com.astrochart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.astrochart.core.panchangam.MonthPanchangam
import com.astrochart.core.panchangam.MoonMark
import com.astrochart.core.panchangam.PanchangamNames
import com.astrochart.data.LocationOption
import com.astrochart.ui.components.CelestialCard
import com.astrochart.ui.components.EyebrowLabel
import com.astrochart.ui.components.SectionDivider
import com.astrochart.ui.i18n.LocalLanguage
import com.astrochart.ui.i18n.PanchangamStrings
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.OnGold
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

/**
 * @param remindersOn the observance keys the user has switched a reminder on
 *   for. Keys, not display names, so the state survives a language change.
 * @param remindersUnlocked whether the switches do anything. Off for a user
 *   without Premium, who still sees them — greyed rather than hidden, so the
 *   feature is discoverable and its absence is explained rather than silent.
 * @param onReminderChange fired only when [remindersUnlocked]; the caller owns
 *   persistence, which keeps this screen drivable from a test.
 */
@Composable
fun CalendarScreen(
    month: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    location: LocationOption,
    onDaySelected: (LocalDate) -> Unit,
    remindersOn: Set<String> = emptySet(),
    remindersUnlocked: Boolean = false,
    onReminderChange: (String, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val lang = LocalLanguage.current
    val ps = remember(lang) { PanchangamStrings.forLanguage(lang) }
    val locale = lang.locale
    val zone = remember(location) { ZoneId.of(location.zoneId) }
    val today = remember { LocalDate.now(zone) }
    val monthFmt = remember(lang) { DateTimeFormatter.ofPattern("MMMM yyyy", locale) }
    val dayFmt = remember(lang) { DateTimeFormatter.ofPattern("d EEE", locale) }

    val marks = remember(month, location) {
        MonthPanchangam.moonMarks(month, location.latitude, location.longitude, zone)
    }
    val vratha = remember(month, location) {
        MonthPanchangam.vrathaDays(month, location.latitude, location.longitude, zone)
    }
    val tamilLabel = remember(month, location) {
        val first = computePanchangam(month.atDay(1), location).tamilMonthIndex
        val last = computePanchangam(month.atEndOfMonth(), location).tamilMonthIndex
        val a = PanchangamNames.tamilMonths[first].get(lang)
        val b = PanchangamNames.tamilMonths[last].get(lang)
        if (first == last) a else "$a – $b"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        CelestialCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onMonthChange(month.minusMonths(1)) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = GoldDeep)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = month.format(monthFmt),
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldDeep
                    )
                    Text(
                        text = tamilLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldDeep,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(onClick = { onMonthChange(month.plusMonths(1)) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = GoldDeep)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        val weekdays = remember(lang) {
            (0..6).map { DayOfWeek.SUNDAY.plus(it.toLong()).getDisplayName(TextStyle.SHORT, locale) }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdays.forEach { w ->
                Text(
                    text = w,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(6.dp))

        val firstOfMonth = month.atDay(1)
        val lead = firstOfMonth.dayOfWeek.value % 7 // Sun=0..Sat=6
        val daysInMonth = month.lengthOfMonth()
        val cells = ArrayList<LocalDate?>()
        repeat(lead) { cells.add(null) }
        for (d in 1..daysInMonth) cells.add(month.atDay(d))
        while (cells.size % 7 != 0) cells.add(null)

        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            DayCell(
                                day = day,
                                isToday = day == today,
                                mark = marks[day.dayOfMonth] ?: MoonMark.NONE,
                                onClick = onDaySelected
                            )
                        }
                    }
                }
            }
        }

        if (vratha.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            CelestialCard {
                EyebrowLabel(text = ps.vrathaTitle)
                // Says why the switches are inert rather than leaving a row of
                // dead controls to be puzzled over.
                if (!remindersUnlocked) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = ps.remindersPremium,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                Spacer(Modifier.height(8.dp))
                vratha.forEachIndexed { i, group ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        // Centred, not top-aligned: a name long enough to wrap
                        // ("Amavasai (new moon)", "Sankatahara Chaturthi") left
                        // its date pinned beside the first line, reading as
                        // though it belonged to that line rather than the entry.
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ps.vratha(group.key),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            // The names are the long half and the dates the
                            // short one — usually "17 Thu" — yet the split was
                            // 1 : 1.1 the other way, which is what forced those
                            // two names onto a second line with the date column
                            // half empty beside them.
                            modifier = Modifier.weight(1.4f)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = group.dates.joinToString("  ·  ") { it.format(dayFmt) },
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoldDeep,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(4.dp))
                        ReminderSwitch(
                            on = group.key in remindersOn,
                            enabled = remindersUnlocked,
                            contentDescription = ps.vratha(group.key),
                            onChange = { onReminderChange(group.key, it) }
                        )
                    }
                    if (i < vratha.lastIndex) SectionDivider()
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

/**
 * The per-observance reminder switch: gold when on, an outline when off.
 *
 * Scaled down because it sits at the end of a row that already carries a name
 * and up to two dates; a full-size switch would squeeze both. Disabled for a
 * user without Premium — present but visibly inert, which is the point, and
 * Material already renders that state at reduced alpha.
 */
@Composable
private fun ReminderSwitch(
    on: Boolean,
    enabled: Boolean,
    contentDescription: String,
    onChange: (Boolean) -> Unit
) {
    Switch(
        checked = on,
        onCheckedChange = onChange,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = OnGold,
            checkedTrackColor = GoldDeep,
            checkedBorderColor = GoldDeep,
            uncheckedThumbColor = TextMuted,
            uncheckedTrackColor = Color.Transparent,
            uncheckedBorderColor = TextMuted
        ),
        modifier = Modifier
            .scale(0.72f)
            .semantics { this.contentDescription = contentDescription }
    )
}

/**
 * A Box, not a Column: the moon dot is positioned over the cell rather than
 * stacked under the number.
 *
 * As a centred Column, a cell carrying a dot was taller than its neighbours,
 * so centring the *stack* pushed the number upward — every new-moon and
 * full-moon date rode visibly higher than the rest of its week. Anchoring the
 * number to the cell's centre and the dot to its bottom edge means the numbers
 * share one baseline across the whole grid, marked or not, and the dots line
 * up with each other too.
 */
@Composable
private fun DayCell(day: LocalDate, isToday: Boolean, mark: MoonMark, onClick: (LocalDate) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
            .clip(CircleShape)
            .then(if (isToday) Modifier.background(GoldDeep) else Modifier)
            .clickable { onClick(day) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isToday) OnGold else TextPrimary
        )
        if (mark != MoonMark.NONE) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    // Clear of the clipped circle's edge, which narrows fast
                    // near the bottom on a day that is also today.
                    .padding(bottom = 3.dp)
            ) {
                MoonDot(mark, isToday)
            }
        }
    }
}

@Composable
private fun MoonDot(mark: MoonMark, isToday: Boolean) {
    val tint = if (isToday) OnGold else GoldDeep
    when (mark) {
        MoonMark.FULL -> Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .border(1.dp, tint, CircleShape)
        )
        MoonMark.NEW -> Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isToday) OnGold else Color(0xFF6B6480))
        )
        MoonMark.NONE -> Unit
    }
}
