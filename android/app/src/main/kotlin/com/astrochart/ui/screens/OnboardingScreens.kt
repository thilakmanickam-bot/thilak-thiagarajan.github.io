package com.astrochart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.astrochart.Features
import com.astrochart.core.i18n.Language
import com.astrochart.core.models.ChartStyle
import com.astrochart.core.panchangam.Panchangam
import com.astrochart.core.panchangam.SolarLunar
import com.astrochart.data.LocationOption
import com.astrochart.ui.components.CelestialCard
import com.astrochart.ui.components.GoldButton
import com.astrochart.ui.i18n.ChartStyleStore
import com.astrochart.ui.i18n.LanguageStore
import com.astrochart.ui.i18n.OnboardingStore
import com.astrochart.ui.i18n.PrimaryProfile
import com.astrochart.ui.i18n.PrimaryProfileStore
import com.astrochart.ui.theme.CardBorder
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.OnGold
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary
import com.astrochart.ui.theme.fontFamilyForLanguage
import com.astrochart.ui.viewmodel.AccountViewModel
import com.astrochart.ui.viewmodel.BirthInputViewModel

/**
 * First-run (and first-launch-after-update) welcome wizard: sign-in, language,
 * primary profile, chart style, and a Basic/Premium overview. Rendered before
 * [AppNavigation][com.astrochart.AppNavigation] provides `LocalStrings`/
 * `LocalLanguage`, so — like [LanguagePickerDialog] — its copy is plain
 * hardcoded English; every choice is written straight to the same stores
 * Settings reads from, so it's immediately reflected and editable there too.
 */
private enum class OnboardingStep { SIGN_IN, LANGUAGE, PROFILE, CHART_STYLE, TIER }

@Composable
fun OnboardingWizard(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var step by remember {
        mutableStateOf(if (Features.AUTH_ENABLED) OnboardingStep.SIGN_IN else OnboardingStep.LANGUAGE)
    }
    var language by remember { mutableStateOf(LanguageStore.load(context)) }
    var chartStyle by remember { mutableStateOf(ChartStyleStore.load(context)) }

    Column(modifier = modifier.fillMaxSize()) {
        Spacer(Modifier.height(24.dp))
        StepDots(current = step.ordinal, total = OnboardingStep.entries.size)
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (step) {
                OnboardingStep.SIGN_IN -> OnboardingSignInStep(onNext = { step = OnboardingStep.LANGUAGE })
                OnboardingStep.LANGUAGE -> OnboardingLanguageStep(
                    selected = language,
                    onSelect = { language = it; LanguageStore.save(context, it) },
                    onNext = { step = OnboardingStep.PROFILE }
                )
                OnboardingStep.PROFILE -> OnboardingProfileStep(
                    onSaved = { step = OnboardingStep.CHART_STYLE },
                    onSkip = { step = OnboardingStep.CHART_STYLE }
                )
                OnboardingStep.CHART_STYLE -> OnboardingChartStyleStep(
                    selected = chartStyle,
                    onSelect = { chartStyle = it; ChartStyleStore.save(context, it) },
                    onNext = { step = OnboardingStep.TIER }
                )
                OnboardingStep.TIER -> OnboardingTierStep(
                    onFinish = { OnboardingStore.markCompleted(context); onFinished() }
                )
            }
        }
    }
}

@Composable
private fun StepDots(current: Int, total: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (i == current) GoldDeep else CardBorder)
            )
        }
    }
}

@Composable
private fun OnboardingSignInStep(onNext: () -> Unit) {
    val context = LocalContext.current
    val viewModel: AccountViewModel = viewModel()
    val account by viewModel.account.collectAsState()
    val status by viewModel.status.collectAsState()

    // A returning signed-in user (or a fresh success mid-wizard) skips straight ahead.
    LaunchedEffect(account) { if (account != null) onNext() }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = null,
            tint = GoldDeep,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Welcome to Halo",
            style = MaterialTheme.typography.headlineSmall,
            color = GoldDeep,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Sign in to back up your profile and charts across devices — or skip for now.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { viewModel.signInWithGoogle(context) },
            enabled = status !is AccountViewModel.Status.Working,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldDeep, contentColor = OnGold)
        ) {
            Text("Continue with Google")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { /* Facebook login coming soon */ },
            enabled = false,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Continue with Facebook", color = TextMuted)
        }
        Spacer(Modifier.height(6.dp))
        Text("Coming soon", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            enabled = false,
            label = { Text("Email or mobile number") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        Text("Coming soon", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        if (status is AccountViewModel.Status.Working) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator(color = GoldDeep)
        }
        if (status is AccountViewModel.Status.Error) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Sign-in failed. You can try again or skip for now.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(24.dp))
        GoldButton(text = "Continue", onClick = onNext, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun OnboardingLanguageStep(
    selected: Language,
    onSelect: (Language) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Choose your language",
            style = MaterialTheme.typography.headlineSmall,
            color = GoldDeep,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "You can change this anytime in Settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Language.entries.forEach { l ->
            ChoiceRow(
                label = l.displayName,
                selected = l == selected,
                onSelect = { onSelect(l) },
                fontFamily = fontFamilyForLanguage(l)
            )
        }
        Spacer(Modifier.height(24.dp))
        GoldButton(text = "Continue", onClick = onNext, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))
    }
}

/**
 * Full birth-detail profile step, reusing [BirthInputScreen] so rasi/nakshatra
 * are computed (via [Panchangam.moonRasiAndNakshatraAtJd], the sidereal
 * counterpart to its tropical [com.astrochart.core.models.NatalChart]) rather
 * than hand-picked. `internal` so Settings' "Change primary profile" flow can
 * reuse this exact same step, prefilled the same way.
 */
@Composable
internal fun OnboardingProfileStep(onSaved: () -> Unit, onSkip: () -> Unit) {
    val context = LocalContext.current
    val viewModel: BirthInputViewModel = viewModel()
    val existing = remember { PrimaryProfileStore.load(context) }

    Column(modifier = Modifier.fillMaxSize()) {
        BirthInputScreen(
            viewModel = viewModel,
            modifier = Modifier.weight(1f),
            initialName = existing?.name ?: "",
            initialGender = existing?.gender ?: "",
            initialYear = existing?.year ?: 2000,
            initialMonth = existing?.month ?: 1,
            initialDay = existing?.day ?: 1,
            initialHour = existing?.hour ?: 12,
            initialMinute = existing?.minute ?: 0,
            initialLocation = existing?.takeIf { it.locationCity.isNotBlank() }?.let {
                LocationOption(it.locationCity, it.locationCountry, it.latitude, it.longitude, it.timeZoneId)
            },
            initialTimeZone = existing?.timeZoneId ?: "Asia/Singapore",
            onChartCalculated = { chart, name, loc ->
                val utc = chart.birthData.toUTC()
                val jdUt = SolarLunar.julianDayUt(utc.toLocalDate(), utc.hour + utc.minute / 60.0)
                val (rasi, nak) = Panchangam.moonRasiAndNakshatraAtJd(jdUt, utc.year)
                PrimaryProfileStore.save(
                    context,
                    PrimaryProfile(
                        name = name,
                        rasi = rasi,
                        nakshatra = nak,
                        gender = chart.birthData.gender,
                        year = chart.birthData.dateTime.year,
                        month = chart.birthData.dateTime.monthValue,
                        day = chart.birthData.dateTime.dayOfMonth,
                        hour = chart.birthData.dateTime.hour,
                        minute = chart.birthData.dateTime.minute,
                        latitude = chart.birthData.latitude,
                        longitude = chart.birthData.longitude,
                        timeZoneId = chart.birthData.timeZone.id,
                        locationCity = loc.city,
                        locationCountry = loc.country
                    )
                )
                onSaved()
            }
        )
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text("Skip for now", color = TextMuted)
        }
    }
}

@Composable
private fun OnboardingChartStyleStep(
    selected: ChartStyle,
    onSelect: (ChartStyle) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "How should your chart look?",
            style = MaterialTheme.typography.headlineSmall,
            color = GoldDeep,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "You can change this anytime in Settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        ChoiceRow(
            label = "Western (Wheel)",
            selected = selected == ChartStyle.WESTERN_WHEEL,
            onSelect = { onSelect(ChartStyle.WESTERN_WHEEL) }
        )
        ChoiceRow(
            label = "South Indian",
            selected = selected == ChartStyle.SOUTH_INDIAN,
            onSelect = { onSelect(ChartStyle.SOUTH_INDIAN) }
        )
        Row(
            modifier = Modifier.fillMaxWidth().alpha(0.45f).padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "North Indian — Coming soon",
                style = MaterialTheme.typography.bodyLarge,
                color = TextMuted,
                modifier = Modifier.padding(start = 40.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        GoldButton(text = "Continue", onClick = onNext, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun OnboardingTierStep(onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Icon(
            imageVector = Icons.Filled.WorkspacePremium,
            contentDescription = null,
            tint = GoldDeep,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Choose your plan",
            style = MaterialTheme.typography.headlineSmall,
            color = GoldDeep,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        TierCard(title = "Basic", subtitle = "Free, with occasional ads", selected = true)
        Spacer(Modifier.height(12.dp))
        TierCard(
            title = "Premium",
            subtitle = "Ad-free + the AI astrologer",
            selected = false,
            comingSoon = true
        )
        Spacer(Modifier.height(28.dp))
        GoldButton(text = "Get Started", onClick = onFinish, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun TierCard(title: String, subtitle: String, selected: Boolean, comingSoon: Boolean = false) {
    CelestialCard(modifier = if (comingSoon) Modifier.alpha(0.6f) else Modifier) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = GoldDeep)
                    if (comingSoon) {
                        Spacer(Modifier.width(8.dp))
                        ComingSoonBadge(text = "Coming soon")
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            if (selected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = GoldDeep)
            }
        }
    }
}
