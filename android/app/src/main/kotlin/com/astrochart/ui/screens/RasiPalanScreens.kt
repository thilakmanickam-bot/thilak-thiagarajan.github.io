package com.astrochart.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.astrochart.core.i18n.Language
import com.astrochart.core.i18n.Translations
import com.astrochart.core.interpret.RasiInfo
import com.astrochart.core.interpret.RasiPalanText
import com.astrochart.core.interpret.RasiPeriod
import com.astrochart.core.panchangam.PanchangamNames
import com.astrochart.core.utils.ZodiacUtils
import com.astrochart.ui.components.CelestialCard
import com.astrochart.ui.components.EyebrowLabel
import com.astrochart.ui.components.SectionDivider
import com.astrochart.ui.i18n.LocalLanguage
import com.astrochart.ui.i18n.RasiStrings
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.GoldLight
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary
import java.time.LocalDate

// ---- Hub ----------------------------------------------------------------

@Composable
fun RasiHubScreen(
    onPeriod: (RasiPeriod) -> Unit,
    onAboutSigns: () -> Unit,
    onAboutNakshatras: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalLanguage.current
    val rs = remember(lang) { RasiStrings.forLanguage(lang) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        HubRow(rs.today, onClick = { onPeriod(RasiPeriod.DAY) })
        HubRow(rs.weekly, onClick = { onPeriod(RasiPeriod.WEEK) })
        HubRow(rs.monthly, onClick = { onPeriod(RasiPeriod.MONTH) })
        HubRow(rs.yearly, onClick = { onPeriod(RasiPeriod.YEAR) })
        Spacer(Modifier.height(8.dp))
        HubRow(rs.aboutSigns, onClick = onAboutSigns)
        HubRow(rs.aboutNakshatras, onClick = onAboutNakshatras)
    }
}

@Composable
private fun HubRow(label: String, onClick: () -> Unit) {
    CelestialCard(modifier = Modifier
        .padding(vertical = 6.dp)
        .clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            androidx.compose.material3.Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = GoldDeep
            )
        }
    }
}

// ---- Sign picker --------------------------------------------------------

@Composable
fun RasiSignsScreen(onPick: (Int) -> Unit, modifier: Modifier = Modifier) {
    val lang = LocalLanguage.current
    val rs = remember(lang) { RasiStrings.forLanguage(lang) }
    val signs = remember { ZodiacUtils.getAllSigns() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        EyebrowLabel(text = rs.chooseSign)
        Spacer(Modifier.height(12.dp))
        signs.chunked(3).forEachIndexed { rowIdx, triple ->
            Row(modifier = Modifier.fillMaxWidth()) {
                triple.forEachIndexed { colIdx, sign ->
                    val i = rowIdx * 3 + colIdx
                    CelestialCard(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .clickable { onPick(i) },
                        contentPadding = 14
                    ) {
                        Text(
                            text = Translations.signName(sign, lang),
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ---- Horoscope for one sign & period -----------------------------------

@Composable
fun RasiHoroscopeScreen(signIndex: Int, period: RasiPeriod, modifier: Modifier = Modifier) {
    val lang = LocalLanguage.current
    val rs = remember(lang) { RasiStrings.forLanguage(lang) }
    val sign = ZodiacUtils.getAllSigns()[signIndex.coerceIn(0, 11)]
    val today = remember { LocalDate.now() }
    val paragraphs = remember(signIndex, period, lang, today) {
        RasiPalanText.horoscope(signIndex, period, today, lang)
    }
    val periodLabel = when (period) {
        RasiPeriod.DAY -> rs.today; RasiPeriod.WEEK -> rs.weekly
        RasiPeriod.MONTH -> rs.monthly; RasiPeriod.YEAR -> rs.yearly
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = Translations.signName(sign, lang),
            style = MaterialTheme.typography.headlineSmall,
            color = GoldLight
        )
        Text(
            text = periodLabel,
            style = MaterialTheme.typography.titleMedium,
            color = TextMuted
        )
        Spacer(Modifier.height(12.dp))
        paragraphs.forEach { para ->
            CelestialCard(modifier = Modifier.padding(vertical = 6.dp)) {
                Text(text = para, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ---- Rasi info detail ---------------------------------------------------

@Composable
fun RasiInfoScreen(signIndex: Int, modifier: Modifier = Modifier) {
    val lang = LocalLanguage.current
    val rs = remember(lang) { RasiStrings.forLanguage(lang) }
    val info = RasiInfo.of(signIndex)
    val signs = ZodiacUtils.getAllSigns()
    val friendly = info.friendlySigns.joinToString("  •  ") { Translations.signName(signs[it], lang) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = Translations.signName(signs[signIndex.coerceIn(0, 11)], lang),
            style = MaterialTheme.typography.headlineSmall,
            color = GoldLight
        )
        Spacer(Modifier.height(12.dp))
        CelestialCard {
            EyebrowLabel(text = rs.character)
            Spacer(Modifier.height(6.dp))
            Text(text = info.description.get(lang), style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
        }
        Spacer(Modifier.height(12.dp))
        CelestialCard {
            InfoRow(rs.rulingPlanet, Translations.planetName(info.lord, lang))
            InfoRow(rs.friendlySigns, friendly)
            InfoRow(rs.luckyColor, info.color.get(lang))
            InfoRow(rs.luckyDay, PanchangamNames.weekdays[info.luckyDayIndex].get(lang))
            InfoRow(rs.luckyNumber, info.luckyNumber.toString())
            InfoRow(rs.deity, info.deity.get(lang))
            InfoRow(rs.gemstone, info.gemstone.get(lang), last = true)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String, last: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            modifier = Modifier.width(120.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
    }
    if (!last) SectionDivider()
}

// ---- Nakshatra reference list ------------------------------------------

private val VIMSHOTTARI_LORDS = listOf(
    "Ketu", "Venus", "Sun", "Moon", "Mars", "Rahu", "Jupiter", "Saturn", "Mercury"
)

private fun nakLord(index: Int, lang: Language): String {
    val key = VIMSHOTTARI_LORDS[index % 9]
    return when (key) {
        "Rahu" -> when (lang) { Language.EN -> "Rahu"; Language.TA -> "ராகு"; Language.ZH -> "罗睺" }
        "Ketu" -> when (lang) { Language.EN -> "Ketu"; Language.TA -> "கேது"; Language.ZH -> "计都" }
        else -> Translations.planetName(key, lang)
    }
}

@Composable
fun NakshatraListScreen(modifier: Modifier = Modifier) {
    val lang = LocalLanguage.current
    val rs = remember(lang) { RasiStrings.forLanguage(lang) }
    val nakshatras = PanchangamNames.nakshatras

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(nakshatras.size) { i ->
            CelestialCard(contentPadding = 14) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${i + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        modifier = Modifier.width(28.dp)
                    )
                    Text(
                        text = nakshatras[i].get(lang),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${rs.nakshatraLord}: ${nakLord(i, lang)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldDeep
                    )
                }
            }
        }
    }
}
