package com.astrochart.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.astrochart.core.panchangam.HinduYear
import com.astrochart.core.panchangam.Panchangam
import com.astrochart.core.panchangam.PanchangamNames
import com.astrochart.ui.components.CelestialCard
import com.astrochart.ui.components.EyebrowLabel
import com.astrochart.ui.components.SectionDivider
import com.astrochart.ui.i18n.LocalLanguage
import com.astrochart.ui.i18n.PanchangamLocationStore
import com.astrochart.ui.i18n.SankalpaStrings
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary
import java.time.LocalDate
import java.time.ZoneId

/**
 * Sankalpa — where an intention is located in time before it is stated.
 *
 * The framing below is the whole reason this screen exists. A sankalpa is not a
 * note with a date on it: the tradition names the year, the sun's half-year, the
 * season, the lunar month, the fortnight, the lunar day, the weekday and the
 * place, and *then* states the resolve. Everything here comes from
 * [HinduYear] and [Panchangam] — nothing is tabulated, so no year table can go
 * stale.
 *
 * Two reckonings exist for the year and South India genuinely uses both, so the
 * samvatsara is shown twice whenever they disagree — for several weeks each
 * spring a Tamil almanac and a Telugu one print different names and **both are
 * right**. Silently picking one would be the wrong kind of tidy.
 */
@Composable
fun SankalpaScreen(modifier: Modifier = Modifier) {
    val lang = LocalLanguage.current
    val context = LocalContext.current
    val strings = remember(lang) { SankalpaStrings.forLanguage(lang) }

    val location = remember { PanchangamLocationStore.load(context) }
    val zone = remember(location) { ZoneId.of(location.zoneId) }
    val today = remember { LocalDate.now() }

    // One computation, several readings off it. Deliberately keyed on the day
    // and place: none of this changes more often than that.
    val framing = remember(today, location) {
        val day = Panchangam.compute(today, location.latitude, location.longitude, zone)
        val lunarMonth = HinduYear.lunarMonth(today, location.latitude, location.longitude, zone)
        Framing(
            solarYear = HinduYear.samvatsara(
                today, location.latitude, location.longitude, zone,
                HinduYear.YearReckoning.SOLAR
            ),
            lunarYear = HinduYear.samvatsara(
                today, location.latitude, location.longitude, zone,
                HinduYear.YearReckoning.LUNAR
            ),
            ayana = HinduYear.ayana(today, location.latitude, location.longitude, zone),
            ritu = HinduYear.ritu(lunarMonth.index),
            masa = lunarMonth,
            paksha = PanchangamNames.paksha(day.tithi.index).get(lang),
            tithi = PanchangamNames.tithiName(day.tithi.index).get(lang),
            vaara = PanchangamNames.weekdays[day.weekdayIndex].get(lang),
            place = location.displayName
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        CelestialCard(modifier = Modifier.fillMaxWidth(), contentPadding = 18) {
            EyebrowLabel(text = strings.framingHeading, icon = Icons.Filled.SelfImprovement)
            Spacer(modifier = Modifier.height(14.dp))

            FramingRow(strings.samvatsara, framing.samvatsaraText())
            FramingRow(strings.ayana, framing.ayana.name.lowercase().replaceFirstChar { it.uppercase() })
            FramingRow(strings.ritu, framing.ritu.name.lowercase().replaceFirstChar { it.uppercase() })
            FramingRow(strings.masa, framing.masaText())
            FramingRow(strings.paksha, framing.paksha)
            FramingRow(strings.tithi, framing.tithi)
            FramingRow(strings.vaara, framing.vaara)
            FramingRow(strings.place, framing.place)
        }

        Spacer(modifier = Modifier.height(20.dp))
        SectionDivider()
        Spacer(modifier = Modifier.height(20.dp))

        // Persistence lands in the next increment; until then the screen is
        // honest about being read-only rather than offering a button that
        // silently drops what you typed.
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = strings.empty,
                style = MaterialTheme.typography.titleMedium,
                color = GoldDeep,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = strings.emptyHint,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

/** One label/value line of the framing. */
@Composable
private fun FramingRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.4f)
        )
    }
}

/** The recited coordinates of this moment, resolved once. */
private data class Framing(
    val solarYear: String,
    val lunarYear: String,
    val ayana: HinduYear.Ayana,
    val ritu: HinduYear.Ritu,
    val masa: HinduYear.LunarMonth,
    val paksha: String,
    val tithi: String,
    val vaara: String,
    val place: String
) {
    /**
     * Both year names when the two reckonings disagree — which they do for the
     * weeks between Ugadi and Puthandu every spring. Showing one and hiding the
     * other would tell half the readership their almanac is wrong.
     */
    fun samvatsaraText(): String =
        if (solarYear == lunarYear) solarYear else "$solarYear / $lunarYear"

    /** Adhika months are repeated in the calendar, and the label has to say so. */
    fun masaText(): String = if (masa.isAdhika) "Adhika ${masa.name}" else masa.name
}
