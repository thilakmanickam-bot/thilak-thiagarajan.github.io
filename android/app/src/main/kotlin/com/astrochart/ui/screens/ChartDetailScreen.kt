package com.astrochart.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import com.astrochart.core.i18n.Language
import com.astrochart.core.i18n.Translations
import com.astrochart.core.interpret.AgeUtil
import com.astrochart.core.interpret.ChartReading
import com.astrochart.core.interpret.ChineseZodiac
import com.astrochart.core.interpret.DailyReading
import com.astrochart.core.models.Aspect
import com.astrochart.core.models.ChartStyle
import com.astrochart.core.models.NatalChart
import com.astrochart.core.models.PlanetaryPosition
import com.astrochart.core.utils.AspectInterpretationProvider
import com.astrochart.ui.components.CelestialCard
import com.astrochart.ui.components.EyebrowLabel
import com.astrochart.ui.components.LocalBackgroundMotion
import com.astrochart.ui.components.NatalWheel
import com.astrochart.ui.components.SectionDivider
import com.astrochart.ui.components.SouthIndianChartView
import com.astrochart.ui.i18n.LocalChartStyle
import com.astrochart.ui.i18n.LocalLanguage
import com.astrochart.ui.i18n.LocalStrings
import com.astrochart.ui.i18n.UiStrings
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun birthFormatter(lang: Language): DateTimeFormatter =
    DateTimeFormatter.ofPattern(
        if (lang == Language.ZH) "yyyy年M月d日 HH:mm" else "d MMM yyyy, HH:mm",
        lang.locale
    )

/** Rebuilds a placement's degree label with the sign name localized. */
private fun localizedLabel(position: PlanetaryPosition, lang: Language): String {
    val deg = position.degree
    val min = position.minute.toString().padStart(2, '0')
    return "$deg°$min' ${Translations.signName(position.sign, lang)}"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChartDetailScreen(
    chart: NatalChart?,
    chartName: String,
    modifier: Modifier = Modifier
) {
    if (chart == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GoldDeep)
        }
        return
    }

    val strings = LocalStrings.current
    val tabs = listOf(
        strings.tabWheel, strings.tabPlacements, strings.tabAspects,
        strings.tabBalance, strings.tabReading
    )
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    // Feed the pager position into the animated background as a parallax offset.
    val motion = LocalBackgroundMotion.current
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage + pagerState.currentPageOffsetFraction }
            .collect { motion.parallax = it - 1f }
    }
    DisposableEffect(Unit) { onDispose { motion.parallax = 0f } }

    Column(modifier = modifier.fillMaxSize()) {
        ChartHeader(chart = chart, chartName = chartName)

        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            contentColor = GoldDeep,
            edgePadding = 12.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    selectedContentColor = GoldDeep,
                    unselectedContentColor = TextMuted,
                    text = { Text(title, style = MaterialTheme.typography.titleSmall) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> WheelTab(chart, chartName)
                1 -> PlacementsTab(chart)
                2 -> AspectsTab(chart)
                3 -> BalanceTab(chart)
                else -> ReadingTab(chart, chartName)
            }
        }
    }
}

@Composable
private fun WheelTab(chart: NatalChart, chartName: String) {
    val style = LocalChartStyle.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        CelestialCard(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .widthIn(max = 460.dp),
            contentPadding = 16
        ) {
            when (style) {
                ChartStyle.WESTERN_WHEEL -> {
                    NatalWheel(chart = chart, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(14.dp))
                    WheelLegend()
                }
                ChartStyle.SOUTH_INDIAN -> {
                    SouthIndianChartView(
                        chart = chart,
                        chartName = chartName,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = LocalStrings.current.chartLegendSouthIndian,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        DailyReadingCard(chart)
    }
}

private fun dailyDateFmt(lang: Language): DateTimeFormatter =
    DateTimeFormatter.ofPattern(if (lang == Language.ZH) "M月d日" else "d MMM yyyy", lang.locale)

@Composable
private fun DailyReadingCard(chart: NatalChart) {
    val strings = LocalStrings.current
    val lang = LocalLanguage.current
    val sun = chart.planets.firstOrNull { it.name == "Sun" }?.sign
    val today = LocalDate.now()
    val data = remember(lang, sun, today) { DailyReading.build(today, lang, sun) }
    val dateText = remember(lang, today) { today.format(dailyDateFmt(lang)) }

    CelestialCard {
        EyebrowLabel(text = strings.dailyTitle + "  •  " + dateText)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = data.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        DailyRow(strings.dailyFocus, data.focus)
        DailyRow(strings.dailyGood, data.goodToDo)
        DailyRow(strings.dailyAvoid, data.avoid)
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            ColorSwatch(strings.dailyLuckyColor, data.luckyColorName, data.luckyColorHex)
            ColorSwatch(strings.dailyAvoidColor, data.avoidColorName, data.avoidColorHex)
        }
    }
}

@Composable
private fun DailyRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.width(112.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ColorSwatch(label: String, name: String, hex: Long) {
    Column {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(hex))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun WheelLegend() {
    val strings = LocalStrings.current
    // Stack the two colour keys vertically so the longer localized strings (e.g.
    // Tamil) wrap naturally instead of being squeezed into a narrow column.
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(strings.wheelLegendAxis,
            style = MaterialTheme.typography.bodySmall, color = TextMuted)
        Text(strings.wheelLegendSoft, style = MaterialTheme.typography.bodySmall, color = Color(0xFF8FB8C8))
        Text(strings.wheelLegendHard, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun ReadingTab(chart: NatalChart, chartName: String) {
    val lang = LocalLanguage.current
    val sections = remember(chart, chartName, lang) { ChartReading.build(chart, chartName, lang) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(sections) { section ->
            CelestialCard {
                EyebrowLabel(text = section.title)
                Spacer(modifier = Modifier.height(6.dp))
                section.paragraphs.forEachIndexed { i, para ->
                    if (i > 0) Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = para,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartHeader(chart: NatalChart, chartName: String) {
    val strings = LocalStrings.current
    val lang = LocalLanguage.current
    val sun = chart.planets.firstOrNull { it.name == "Sun" }
    val moon = chart.planets.firstOrNull { it.name == "Moon" }

    val birthYear = chart.birthData.dateTime.year
    val age = AgeUtil.years(chart.birthData.dateTime)
    val gender = chart.birthData.gender
    val zodiac = ChineseZodiac.name(birthYear, lang)
    val zodiacEmoji = ChineseZodiac.emoji(birthYear)

    CelestialCard(
        modifier = Modifier.padding(16.dp),
        contentPadding = 18
    ) {
        if (chartName.isNotBlank()) {
            Text(
                text = chartName,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(
            text = chart.birthData.dateTime.format(birthFormatter(lang)),
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
        if (chart.birthData.locationName.isNotBlank()) {
            Text(
                text = chart.birthData.locationName,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            sun?.let { KeyPlacement(strings.labelSun, Translations.signName(it.sign, lang), Modifier.weight(1f)) }
            moon?.let { KeyPlacement(strings.labelMoon, Translations.signName(it.sign, lang), Modifier.weight(1f)) }
            KeyPlacement(strings.labelRising, Translations.signName(chart.ascendant.sign, lang), Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        SectionDivider()
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            KeyPlacement(strings.labelAge, strings.ageValue(age), Modifier.weight(1f))
            if (gender.isNotBlank()) {
                KeyPlacement(strings.labelGender, localizedGender(gender, strings), Modifier.weight(1f))
            }
            KeyPlacement(strings.labelChineseZodiac, "$zodiacEmoji $zodiac", Modifier.weight(1f))
        }
    }
}

private fun localizedGender(code: String, strings: UiStrings): String = when (code) {
    "Female" -> strings.genderFemale
    "Male" -> strings.genderMale
    "Other" -> strings.genderOther
    else -> code
}

@Composable
private fun KeyPlacement(label: String, sign: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = sign,
            style = MaterialTheme.typography.titleMedium,
            color = GoldDeep,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PlacementsTab(chart: NatalChart) {
    val strings = LocalStrings.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { TabHeading(strings.angles) }
        item { PlacementRow(chart.ascendant) }
        item { PlacementRow(chart.midheaven) }
        item { TabHeading(strings.planets) }
        items(chart.planets) { planet -> PlacementRow(planet) }
    }
}

/** Localized body name: the two angles plus delegation to planet-name translation. */
private fun bodyName(name: String, lang: Language): String = when (name) {
    "Ascendant" -> when (lang) {
        Language.TA -> "லக்னம்"; Language.ZH -> "上升"; Language.HI -> "लग्न"
        Language.TE -> "లగ్నం"; Language.KN -> "ಲಗ್ನ"; Language.ML -> "ലഗ്നം"
        Language.MR -> "लग्न"; else -> "Ascendant"
    }
    "Midheaven" -> when (lang) {
        Language.TA -> "மத்திம வானம்"; Language.ZH -> "天顶"; Language.HI -> "मध्य आकाश"
        Language.TE -> "మధ్యాకాశం"; Language.KN -> "ಮಧ್ಯಾಕಾಶ"; Language.ML -> "മധ്യാകാശം"
        Language.MR -> "मध्य आकाश"; else -> "Midheaven"
    }
    else -> Translations.planetName(name, lang)
}

@Composable
private fun PlacementRow(position: PlanetaryPosition) {
    val strings = LocalStrings.current
    val lang = LocalLanguage.current
    CelestialCard(contentPadding = 14) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = bodyName(position.name, lang),
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(text = localizedLabel(position, lang), style = MaterialTheme.typography.bodyMedium, color = GoldDeep)
                Text(
                    text = strings.houseLabel(position.house),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun AspectsTab(chart: NatalChart) {
    val strings = LocalStrings.current
    if (chart.aspects.isEmpty()) {
        EmptyTab(strings.noAspects)
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(chart.aspects) { aspect -> AspectRow(aspect) }
    }
}

@Composable
private fun AspectRow(aspect: Aspect) {
    val strings = LocalStrings.current
    val lang = LocalLanguage.current
    val bodyA = Translations.planetName(aspect.bodyA, lang)
    val bodyB = Translations.planetName(aspect.bodyB, lang)
    val type = Translations.aspectType(aspect.type, lang)
    val interp = AspectInterpretationProvider.getInterpretation(aspect.bodyA, aspect.bodyB, aspect.type, lang)
    CelestialCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$bodyA $type $bodyB",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = strings.orb + " " + String.format(Locale.US, "%.1f°", aspect.orb),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
        if (interp.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = interp,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun BalanceTab(chart: NatalChart) {
    val strings = LocalStrings.current
    val lang = LocalLanguage.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { TabHeading(strings.elements) }
        items(chart.balance.elements.entries.toList()) { (element, count) ->
            BalanceRow(Translations.element(element, lang), count)
        }
        item { TabHeading(strings.modalities) }
        items(chart.balance.modalities.entries.toList()) { (modality, count) ->
            BalanceRow(Translations.modality(modality, lang), count)
        }
    }
}

@Composable
private fun BalanceRow(label: String, count: Int) {
    val strings = LocalStrings.current
    CelestialCard(contentPadding = 14) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
            Text(
                text = strings.bodyCount(count),
                style = MaterialTheme.typography.bodyMedium,
                color = GoldDeep
            )
        }
    }
}

@Composable
private fun TabHeading(text: String) {
    Column {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = GoldDeep
        )
        Spacer(modifier = Modifier.height(4.dp))
        SectionDivider()
        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
private fun EmptyTab(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
    }
}
