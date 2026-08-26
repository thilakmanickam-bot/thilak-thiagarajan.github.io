package com.astrochart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.astrochart.core.panchangam.PanchangamNames
import com.astrochart.data.LocationOption
import com.astrochart.ui.components.CelestialCard
import com.astrochart.ui.i18n.LocalLanguage
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

@Composable
fun CalendarScreen(
    month: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    location: LocationOption,
    onDaySelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalLanguage.current
    val locale = lang.locale
    val today = remember { LocalDate.now(ZoneId.of(location.zoneId)) }
    val monthFmt = remember(lang) { DateTimeFormatter.ofPattern("MMMM yyyy", locale) }

    // Tamil month(s) spanned this Gregorian month, from the 1st and last day.
    val tamilLabel = remember(month, location) {
        val first = Panchangam_tamilMonth(month.atDay(1), location)
        val last = Panchangam_tamilMonth(month.atEndOfMonth(), location)
        val a = PanchangamNames.tamilMonths[first].get(lang)
        val b = PanchangamNames.tamilMonths[last].get(lang)
        if (first == last) a else "$a – $b"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
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
                        color = TextPrimary
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

        // Weekday header, Sunday-first.
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

        // Leading blanks so the 1st lands under its weekday (Sunday = column 0).
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
                        if (day != null) DayCell(day, day == today, onDaySelected)
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: LocalDate, isToday: Boolean, onClick: (LocalDate) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(3.dp)
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
    }
}

/** Tamil solar month index (0..11) for a date, via the panchangam engine. */
private fun Panchangam_tamilMonth(date: LocalDate, location: LocationOption): Int =
    computePanchangam(date, location).tamilMonthIndex
