package com.astrochart.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.astrochart.core.i18n.Translations
import com.astrochart.core.panchangam.DayPanchangam
import com.astrochart.core.panchangam.Panchangam
import com.astrochart.core.panchangam.PanchangamElement
import com.astrochart.core.panchangam.PanchangamNames
import com.astrochart.core.panchangam.PanchangamSegment
import com.astrochart.core.panchangam.RasiPalan
import com.astrochart.core.utils.ZodiacUtils
import com.astrochart.data.LocationCatalog
import com.astrochart.data.LocationOption
import com.astrochart.ui.components.CelestialCard
import com.astrochart.ui.components.EyebrowLabel
import com.astrochart.ui.components.LabeledDropdown
import com.astrochart.ui.components.SectionDivider
import com.astrochart.ui.i18n.LocalLanguage
import com.astrochart.ui.i18n.PanchangamStrings
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PanchangamScreen(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    location: LocationOption,
    onLocationChange: (LocationOption) -> Unit,
    onOpenCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalLanguage.current
    val ps = remember(lang) { PanchangamStrings.forLanguage(lang) }
    val zone = remember(location) { ZoneId.of(location.zoneId) }
    val p = remember(date, location) {
        Panchangam.compute(date, location.latitude, location.longitude, zone)
    }
    val timeFmt = remember(lang) { DateTimeFormatter.ofPattern("hh:mm a", lang.locale) }
    val gregFmt = remember(lang) {
        DateTimeFormatter.ofPattern(if (lang.code == "zh") "yyyy年M月d日" else "d MMM yyyy", lang.locale)
    }
    fun LocalTime?.fmt(): String = this?.format(timeFmt) ?: "—"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Date navigation + Tamil date.
        CelestialCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onDateChange(date.minusDays(1)) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = GoldDeep)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = date.format(gregFmt),
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldDeep
                    )
                    Text(
                        text = PanchangamNames.weekdays[p.weekdayIndex].get(lang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                    Text(
                        text = "${PanchangamNames.tamilMonths[p.tamilMonthIndex].get(lang)} ${p.tamilDay}",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldDeep,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(onClick = { onDateChange(date.plusDays(1)) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = GoldDeep)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LabeledDropdown(
                    label = ps.location,
                    options = LocationCatalog.locations,
                    selected = location,
                    optionLabel = { it.displayName },
                    onSelected = onLocationChange,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onOpenCalendar) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = ps.monthCalendar, tint = GoldDeep)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Sunrise / sunset.
        CelestialCard {
            Row(modifier = Modifier.fillMaxWidth()) {
                LabelValue(ps.sunrise, p.sunrise.fmt(), Modifier.weight(1f))
                LabelValue(ps.sunset, p.sunset.fmt(), Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(12.dp))

        // Auspicious windows.
        CelestialCard {
            EyebrowLabel(text = ps.auspicious)
            Spacer(Modifier.height(8.dp))
            SegmentRow(ps.abhijit, p.abhijit, timeFmt)
            SegmentRow(ps.brahmaMuhurta, p.brahmaMuhurta, timeFmt)
        }

        Spacer(Modifier.height(12.dp))

        // Inauspicious windows.
        CelestialCard {
            EyebrowLabel(text = ps.inauspicious)
            Spacer(Modifier.height(8.dp))
            SegmentRow(ps.rahu, p.rahuKalam, timeFmt)
            SegmentRow(ps.yamagandam, p.yamagandam, timeFmt)
            SegmentRow(ps.gulikai, p.gulikai, timeFmt)
        }

        Spacer(Modifier.height(12.dp))

        // Panchangam elements.
        CelestialCard {
            EyebrowLabel(text = ps.title)
            Spacer(Modifier.height(8.dp))
            ElementRow(
                label = ps.tithi,
                name = "${PanchangamNames.tithiName(p.tithi.index).get(lang)} · ${PanchangamNames.paksha(p.tithi.index).get(lang)}",
                element = p.tithi, ps = ps, timeFmt = timeFmt
            )
            ElementRow(
                label = ps.nakshatra,
                name = PanchangamNames.nakshatras[p.nakshatra.index].get(lang),
                element = p.nakshatra, ps = ps, timeFmt = timeFmt
            )
            ElementRow(
                label = ps.yoga,
                name = PanchangamNames.yogas[p.yoga.index].get(lang),
                element = p.yoga, ps = ps, timeFmt = timeFmt
            )
            ElementRowRaw(
                label = ps.karana,
                text = "${PanchangamNames.karanaName(p.karanaHalf0).get(lang)}  " +
                    ps.untilTime(p.karanaEndsAt.format(timeFmt))
            )
        }

        Spacer(Modifier.height(12.dp))

        // Daily rasi palan (12 signs) — one clean row per sign so long Tamil
        // words wrap under the value column instead of colliding with the label.
        CelestialCard {
            EyebrowLabel(text = ps.rasiPalan)
            Spacer(Modifier.height(8.dp))
            val signs = remember { ZodiacUtils.getAllSigns() }
            val epoch = date.toEpochDay()
            signs.forEachIndexed { i, sign ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = Translations.signName(sign, lang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.width(96.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = RasiPalan.word(epoch, i, lang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldDeep,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (i < signs.lastIndex) SectionDivider()
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun LabelValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = GoldDeep)
    }
}

@Composable
private fun SegmentRow(label: String, segment: PanchangamSegment?, timeFmt: DateTimeFormatter) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        Text(
            text = if (segment != null) {
                "${segment.start.format(timeFmt)} – ${segment.end.format(timeFmt)}"
            } else "—",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
    }
}

@Composable
private fun ElementRow(
    label: String,
    name: String,
    element: PanchangamElement,
    ps: PanchangamStrings,
    timeFmt: DateTimeFormatter
) {
    val suffix = if (element.endsNextDay) " ${ps.nextDay}" else ""
    ElementRowRaw(label, "$name  " + ps.untilTime(element.endsAt.format(timeFmt) + suffix))
}

@Composable
private fun ElementRowRaw(label: String, text: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(text = text, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        SectionDivider()
    }
}

/** Overload used by the calendar screen to reuse the same computation. */
fun computePanchangam(date: LocalDate, location: LocationOption): DayPanchangam =
    Panchangam.compute(date, location.latitude, location.longitude, ZoneId.of(location.zoneId))
