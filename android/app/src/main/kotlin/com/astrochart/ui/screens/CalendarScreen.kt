package com.astrochart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
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

@Composable
fun CalendarScreen(
    month: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    location: LocationOption,
    onDaySelected: (LocalDate) -> Unit,
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
                Spacer(Modifier.height(8.dp))
                vratha.forEachIndexed { i, group ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = ps.vratha(group.key),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = group.dates.joinToString("  ·  ") { it.format(dayFmt) },
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoldDeep,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.1f)
                        )
                    }
                    if (i < vratha.lastIndex) SectionDivider()
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun DayCell(day: LocalDate, isToday: Boolean, mark: MoonMark, onClick: (LocalDate) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
            .clip(CircleShape)
            .then(if (isToday) Modifier.background(GoldDeep) else Modifier)
            .clickable { onClick(day) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isToday) OnGold else TextPrimary
        )
        if (mark != MoonMark.NONE) {
            Spacer(Modifier.height(2.dp))
            MoonDot(mark, isToday)
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
