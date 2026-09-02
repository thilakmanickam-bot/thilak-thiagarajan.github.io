package com.astrochart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import com.astrochart.ui.components.zodiacIconRes
import com.astrochart.ui.i18n.LocalLanguage
import com.astrochart.ui.i18n.RasiStrings
import com.astrochart.ui.theme.CardBorder
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
fun RasiSignsScreen(
    onPick: (Int) -> Unit,
    forcedColumns: Int? = null,
    modifier: Modifier = Modifier
) {
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
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            // Exactly two tiers, both evenly dividing the 12 signs (3x4 or
            // 6x2) — no uneven trailing row. `forcedColumns` lets a caller
            // (the tablet two-pane right pane) pin this to 3 regardless of
            // measured width, so it always reads "3x4" there as intended.
            val columns = forcedColumns ?: if (maxWidth < 520.dp) 3 else 6
            Column {
                signs.chunked(columns).forEachIndexed { rowIdx, rowSigns ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowSigns.forEachIndexed { colIdx, sign ->
                            val i = rowIdx * columns + colIdx
                            CelestialCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                                    .clickable { onPick(i) },
                                contentPadding = 14
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        painter = painterResource(zodiacIconRes(i)),
                                        contentDescription = null,
                                        tint = GoldDeep,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(Modifier.height(4.dp))
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
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ---- Horoscope for one sign & period -----------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RasiHoroscopeScreen(
    signIndex: Int,
    period: RasiPeriod,
    onPeriodChange: (RasiPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalLanguage.current
    val rs = remember(lang) { RasiStrings.forLanguage(lang) }
    val sign = ZodiacUtils.getAllSigns()[signIndex.coerceIn(0, 11)]
    val today = remember { LocalDate.now() }
    val paragraphs = remember(signIndex, period, lang, today) {
        RasiPalanText.horoscope(signIndex, period, today, lang)
    }
    val periods = remember { listOf(RasiPeriod.DAY, RasiPeriod.WEEK, RasiPeriod.MONTH, RasiPeriod.YEAR) }
    val periodLabels = listOf(rs.today, rs.weekly, rs.monthly, rs.yearly)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Icon(
            painter = painterResource(zodiacIconRes(signIndex)),
            contentDescription = null,
            tint = GoldLight,
            modifier = Modifier.size(40.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = Translations.signName(sign, lang),
            style = MaterialTheme.typography.headlineSmall,
            color = GoldLight
        )
        Spacer(Modifier.height(8.dp))
        ScrollableTabRow(
            selectedTabIndex = periods.indexOf(period).coerceAtLeast(0),
            containerColor = Color.Transparent,
            contentColor = GoldDeep,
            edgePadding = 0.dp
        ) {
            periods.forEachIndexed { i, p ->
                Tab(
                    selected = period == p,
                    onClick = { onPeriodChange(p) },
                    selectedContentColor = GoldDeep,
                    unselectedContentColor = TextMuted,
                    text = { Text(periodLabels[i], style = MaterialTheme.typography.titleSmall) }
                )
            }
        }
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
        Icon(
            painter = painterResource(zodiacIconRes(signIndex)),
            contentDescription = null,
            tint = GoldLight,
            modifier = Modifier.size(40.dp)
        )
        Spacer(Modifier.height(4.dp))
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

// ---- Tablet two-pane (Expanded width only) ------------------------------

/**
 * Rasi Palan on a wide (Expanded) window: a persistent left menu — the same
 * options as [RasiHubScreen] — and a right pane that shows the sign grid
 * (pinned to 3 columns, per the "3x4 ratio on the right" spec) until a sign
 * is tapped, then that sign's horoscope (with its own period toggle) or
 * info, without leaving this pane. Reuses [RasiSignsScreen], [RasiHoroscopeScreen]
 * and [RasiInfoScreen] unmodified; only [showGrid] is local, pane-scoped UI
 * state (which step the right pane is on), reset whenever the left menu
 * selection changes.
 */
@Composable
fun RasiPalanTwoPane(
    rasiSign: Int,
    onRasiSignChange: (Int) -> Unit,
    rasiInfoMode: Boolean,
    onInfoModeChange: (Boolean) -> Unit,
    rasiPeriod: RasiPeriod,
    onPeriodChange: (RasiPeriod) -> Unit,
    onAboutNakshatras: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalLanguage.current
    val rs = remember(lang) { RasiStrings.forLanguage(lang) }
    var showGrid by remember { mutableStateOf(true) }

    Row(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            HubRow(rs.today) { onPeriodChange(RasiPeriod.DAY); onInfoModeChange(false); showGrid = true }
            HubRow(rs.weekly) { onPeriodChange(RasiPeriod.WEEK); onInfoModeChange(false); showGrid = true }
            HubRow(rs.monthly) { onPeriodChange(RasiPeriod.MONTH); onInfoModeChange(false); showGrid = true }
            HubRow(rs.yearly) { onPeriodChange(RasiPeriod.YEAR); onInfoModeChange(false); showGrid = true }
            Spacer(Modifier.height(8.dp))
            HubRow(rs.aboutSigns) { onInfoModeChange(true); showGrid = true }
            HubRow(rs.aboutNakshatras, onClick = onAboutNakshatras) // full nav — nakshatra untouched
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(CardBorder)
        )
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            if (showGrid) {
                RasiSignsScreen(
                    onPick = { i -> onRasiSignChange(i); showGrid = false },
                    forcedColumns = 3
                )
            } else if (rasiInfoMode) {
                RasiInfoScreen(signIndex = rasiSign)
            } else {
                RasiHoroscopeScreen(signIndex = rasiSign, period = rasiPeriod, onPeriodChange = onPeriodChange)
            }
        }
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
        "Rahu" -> when (lang) {
            Language.TA -> "ராகு"; Language.ZH -> "罗睺"; Language.HI -> "राहु"
            Language.TE -> "రాహువు"; Language.KN -> "ರಾಹು"; Language.ML -> "രാഹു"
            Language.MR -> "राहू"; else -> "Rahu"
        }
        "Ketu" -> when (lang) {
            Language.TA -> "கேது"; Language.ZH -> "计都"; Language.HI -> "केतु"
            Language.TE -> "కేతువు"; Language.KN -> "ಕೇತು"; Language.ML -> "കേതു"
            Language.MR -> "केतू"; else -> "Ketu"
        }
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
