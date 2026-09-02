package com.astrochart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.app.Activity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astrochart.core.i18n.Language
import com.astrochart.core.i18n.Translations
import com.astrochart.core.interpret.KootaScore
import com.astrochart.core.interpret.Porutham
import com.astrochart.core.interpret.PoruthamResult
import com.astrochart.core.panchangam.PanchangamNames
import com.astrochart.ui.components.CelestialCard
import com.astrochart.ui.components.LabeledDropdown
import com.astrochart.ui.components.SectionDivider
import com.astrochart.ui.i18n.LocalLanguage
import com.astrochart.ui.i18n.PoruthamStrings
import com.astrochart.ui.theme.CardBorder
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary

private val PresentGreen = Color(0xFF3B9C5A)
private val AbsentRed = Color(0xFFD1495B)

/** Canonical zodiac-sign order; index = rasi index (0 = Aries). */
private val SIGN_ORDER = listOf(
    "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
    "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
)

/**
 * Marriage match-making: the user enters each partner's name, rasi and
 * nakshatram, and the screen shows the South-Indian 40-point porutham with a
 * per-koota breakdown and present/absent verdicts. Fully self-contained — the
 * calculation is pure ([Porutham.compute]) and needs no saved charts, so it can
 * never crash on missing birth data.
 */
@Composable
fun CompatibilityScreen(
    onNavigateToPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalLanguage.current
    val ps = remember(lang) { PoruthamStrings.forLanguage(lang) }
    val activity = LocalContext.current as? Activity

    var boyName by remember { mutableStateOf("") }
    var boyRasi by remember { mutableStateOf<Int?>(null) }
    var boyNak by remember { mutableStateOf<Int?>(null) }
    var girlName by remember { mutableStateOf("") }
    var girlRasi by remember { mutableStateOf<Int?>(null) }
    var girlNak by remember { mutableStateOf<Int?>(null) }
    var result by remember { mutableStateOf<PoruthamResult?>(null) }
    var shownBoy by remember { mutableStateOf("") }
    var shownGirl by remember { mutableStateOf("") }
    var shownBoyRasi by remember { mutableStateOf(0) }
    var shownGirlRasi by remember { mutableStateOf(0) }
    var shownBoyNak by remember { mutableStateOf(0) }
    var shownGirlNak by remember { mutableStateOf(0) }

    val ready = boyRasi != null && boyNak != null && girlRasi != null && girlNak != null

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
        Spacer(Modifier.height(16.dp))

        PersonCard(
            heading = "👰‍♂️  ${ps.boyDetails}",
            name = boyName, onName = { boyName = it }, namePlaceholder = ps.enterBoyName,
            rasi = boyRasi, onRasi = { boyRasi = it },
            nak = boyNak, onNak = { boyNak = it },
            ps = ps, lang = lang
        )
        Spacer(Modifier.height(16.dp))
        PersonCard(
            heading = "👰  ${ps.girlDetails}",
            name = girlName, onName = { girlName = it }, namePlaceholder = ps.enterGirlName,
            rasi = girlRasi, onRasi = { girlRasi = it },
            nak = girlNak, onNak = { girlNak = it },
            ps = ps, lang = lang
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                if (ready) {
                    result = Porutham.compute(boyRasi!!, boyNak!!, girlRasi!!, girlNak!!)
                    shownBoy = boyName.ifBlank { ps.boyName }
                    shownGirl = girlName.ifBlank { ps.girlName }
                    shownBoyRasi = boyRasi!!; shownGirlRasi = girlRasi!!
                    shownBoyNak = boyNak!!; shownGirlNak = girlNak!!
                    activity?.let { com.astrochart.ads.InterstitialAds.maybeShow(it) }
                }
            },
            enabled = ready,
            colors = ButtonDefaults.buttonColors(containerColor = GoldDeep, contentColor = Color.White),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("💍  ${ps.calculate}", style = MaterialTheme.typography.titleMedium)
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
                shownBoy, shownGirl, shownBoyRasi, shownGirlRasi, shownBoyNak, shownGirlNak
            )
            Spacer(Modifier.height(16.dp))
            KutaTable(r, ps)
            Spacer(Modifier.height(16.dp))
            r.scores.forEach { s ->
                KootaDetailCard(s, ps)
                Spacer(Modifier.height(10.dp))
            }
            SummaryCard(r, ps)
            Spacer(Modifier.height(16.dp))
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
    name: String,
    onName: (String) -> Unit,
    namePlaceholder: String,
    rasi: Int?,
    onRasi: (Int) -> Unit,
    nak: Int?,
    onNak: (Int) -> Unit,
    ps: PoruthamStrings,
    lang: Language
) {
    CelestialCard {
        Text(heading, style = MaterialTheme.typography.titleMedium, color = GoldDeep, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        Text(ps.name, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        OutlinedTextField(
            value = name,
            onValueChange = onName,
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

        Text(ps.rasi, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        LabeledDropdown(
            label = ps.rasi,
            options = SIGN_ORDER.indices.toList(),
            selected = rasi,
            optionLabel = { Translations.signName(SIGN_ORDER[it], lang) },
            onSelected = onRasi,
            placeholder = ps.rasi,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        Text(ps.nakshatram, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        LabeledDropdown(
            label = ps.nakshatram,
            options = PanchangamNames.nakshatras.indices.toList(),
            selected = nak,
            optionLabel = { PanchangamNames.nakshatras[it].get(lang) },
            onSelected = onNak,
            placeholder = ps.nakshatram,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ResultHeader(
    r: PoruthamResult,
    ps: PoruthamStrings,
    lang: Language,
    boy: String, girl: String,
    boyRasi: Int, girlRasi: Int,
    boyNak: Int, girlNak: Int
) {
    CelestialCard {
        Row(modifier = Modifier.fillMaxWidth()) {
            PersonSummary(ps.boyName, boy, Translations.signName(SIGN_ORDER[boyRasi], lang), PanchangamNames.nakshatras[boyNak].get(lang), Modifier.weight(1f))
            PersonSummary(ps.girlName, girl, Translations.signName(SIGN_ORDER[girlRasi], lang), PanchangamNames.nakshatras[girlNak].get(lang), Modifier.weight(1f))
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
