package com.astrochart.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.astrochart.Features
import com.astrochart.core.i18n.Language
import com.astrochart.core.models.ChartStyle
import com.astrochart.core.panchangam.Panchangam
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
import com.astrochart.ui.theme.GoldLight
import com.astrochart.ui.theme.OnGold
import com.astrochart.ui.theme.TextMuted
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

    val finish = { OnboardingStore.markCompleted(context); onFinished() }
    // "Skip" advances one step rather than abandoning the wizard: every step
    // either has a sensible default already applied (language, chart style) or
    // is genuinely optional (sign-in, profile), so skipping ahead never leaves
    // the app in a state it can't run in.
    val advance: () -> Unit = {
        when (step) {
            OnboardingStep.SIGN_IN -> step = OnboardingStep.LANGUAGE
            OnboardingStep.LANGUAGE -> step = OnboardingStep.PROFILE
            OnboardingStep.PROFILE -> step = OnboardingStep.CHART_STYLE
            OnboardingStep.CHART_STYLE -> step = OnboardingStep.TIER
            OnboardingStep.TIER -> finish()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Spacer(Modifier.height(24.dp))
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (step) {
                OnboardingStep.SIGN_IN -> OnboardingSignInStep(onNext = advance)
                OnboardingStep.LANGUAGE -> OnboardingLanguageStep(
                    selected = language,
                    onSelect = { language = it; LanguageStore.save(context, it) },
                    onNext = advance
                )
                OnboardingStep.PROFILE -> OnboardingProfileStep(
                    onSaved = advance,
                    onSkip = advance,
                    // The wizard's own footer carries Skip; a second one inside
                    // the step would be two skip buttons on the same screen.
                    showSkip = false
                )
                OnboardingStep.CHART_STYLE -> OnboardingChartStyleStep(
                    selected = chartStyle,
                    onSelect = { chartStyle = it; ChartStyleStore.save(context, it) },
                    onNext = advance
                )
                OnboardingStep.TIER -> OnboardingTierStep(onFinish = finish)
            }
        }
        OnboardingFooter(
            current = step.ordinal,
            total = OnboardingStep.entries.size,
            // The last step's own "Get Started" is the way out; a Skip beside
            // it would be a second button doing the same thing.
            showSkip = step != OnboardingStep.TIER,
            onSkip = advance
        )
    }
}

/**
 * Bottom bar for the wizard: Skip, above a progress indicator that spans the
 * full width of the screen edge to edge.
 */
@Composable
private fun OnboardingFooter(
    current: Int,
    total: Int,
    showSkip: Boolean,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // The row keeps its height on the last step, where Skip is hidden, so
        // the progress bar doesn't jump up the screen on the final transition.
        Box(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (showSkip) {
                TextButton(onClick = onSkip) {
                    Text(
                        text = "Skip",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
        StepProgressBar(current = current, total = total)
    }
}

/**
 * Progress as a single gold dash sliding along a full-width track, one
 * step-width per step. Deliberately unpadded so it runs edge to edge, and
 * unrounded for the same reason — a pill shape would read as a floating
 * element rather than as the bottom edge of the screen.
 */
@Composable
private fun StepProgressBar(current: Int, total: Int, modifier: Modifier = Modifier) {
    val fraction by animateFloatAsState(
        targetValue = if (total <= 0) 0f else current.toFloat() / total,
        animationSpec = tween(durationMillis = 320),
        label = "onboardingProgress"
    )
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(CardBorder.copy(alpha = 0.25f))
    ) {
        Box(
            modifier = Modifier
                .offset(x = maxWidth * fraction)
                .width(if (total <= 0) maxWidth else maxWidth / total)
                .fillMaxHeight()
                .background(Brush.horizontalGradient(listOf(GoldLight, GoldDeep)))
        )
    }
}

/**
 * Vertically centres a step's content, and scrolls instead once the content is
 * taller than the viewport. [heightIn] against the measured viewport height is
 * what makes both true at once: inside a `verticalScroll` the column is
 * measured with unbounded height, so `Arrangement.Center` alone would have
 * nothing to centre within.
 */
@Composable
private fun OnboardingStepScaffold(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val viewportHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = viewportHeight)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

/**
 * The wizard's hero mark: the app icon inside a gold ring with a soft radial
 * glow behind it. Carries the "Halo" name and gives the first-run screens a
 * focal point that a bare tinted icon didn't.
 */
@Composable
private fun HaloHero(icon: ImageVector, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(132.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(GoldDeep.copy(alpha = 0.30f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(GoldDeep.copy(alpha = 0.10f))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(GoldLight, GoldDeep)),
                    shape = CircleShape
                )
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GoldDeep,
            modifier = Modifier.size(52.dp)
        )
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

    OnboardingStepScaffold {
        HaloHero(icon = Icons.Filled.AccountCircle)
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Welcome to Halo",
            style = MaterialTheme.typography.displaySmall,
            color = GoldDeep,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Your chart, your panchangam, your day — read the sky in your own language.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
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
            // The same detail the Account screen shows. This is the screen a
            // first-run tester actually hits, so swallowing the reason here
            // meant a bug report of "it doesn't work" and a second round trip
            // to reproduce it somewhere the detail is visible.
            Spacer(Modifier.height(6.dp))
            Text(
                text = (status as AccountViewModel.Status.Error).message,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(24.dp))
        GoldButton(text = "Continue", onClick = onNext, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))
    }
}

/**
 * `internal` rather than private so the UI tests can drive it directly: the
 * wizard opens on the sign-in step, which needs Firebase, so the only way to
 * cover the steps that *write the user's settings* is to compose them alone.
 * State is fully hoisted, so this is exactly how the wizard uses it.
 */
@Composable
internal fun OnboardingLanguageStep(
    selected: Language,
    onSelect: (Language) -> Unit,
    onNext: () -> Unit
) {
    OnboardingStepScaffold {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Choose your language",
            style = MaterialTheme.typography.headlineMedium,
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
 * are computed (via [Panchangam.moonRasiAndNakshatra], the sidereal
 * counterpart to its tropical [com.astrochart.core.models.NatalChart]) rather
 * than hand-picked. `internal` so Settings' "Change primary profile" flow can
 * reuse this exact same step, prefilled the same way.
 *
 * [showSkip] is false inside the wizard, whose footer already offers Skip, and
 * true for the Settings route, where this button is the only way back out.
 */
@Composable
internal fun OnboardingProfileStep(
    onSaved: () -> Unit,
    onSkip: () -> Unit,
    showSkip: Boolean = true
) {
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
                val (rasi, nak) = Panchangam.moonRasiAndNakshatra(
                    chart.birthData.dateTime, chart.birthData.timeZone
                )
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
        if (showSkip) {
            TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                Text("Skip for now", color = TextMuted)
            }
        }
    }
}

/** `internal` for the same reason as [OnboardingLanguageStep]. */
@Composable
internal fun OnboardingChartStyleStep(
    selected: ChartStyle,
    onSelect: (ChartStyle) -> Unit,
    onNext: () -> Unit
) {
    OnboardingStepScaffold {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "How should your chart look?",
            style = MaterialTheme.typography.headlineMedium,
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
    OnboardingStepScaffold {
        HaloHero(icon = Icons.Filled.WorkspacePremium)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Choose your plan",
            style = MaterialTheme.typography.headlineMedium,
            color = GoldDeep,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
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
