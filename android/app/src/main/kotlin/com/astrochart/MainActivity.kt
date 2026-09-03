package com.astrochart

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.launch
import com.astrochart.core.i18n.Language
import com.astrochart.notify.NotificationScheduler
import com.astrochart.update.InAppUpdate
import com.astrochart.ui.components.AdBanner
import com.astrochart.ui.components.CelestialBackground
import com.astrochart.ui.components.ResponsiveContainer
import com.astrochart.billing.BillingManager
import com.astrochart.ui.i18n.ChartStyleStore
import com.astrochart.ui.i18n.LanguageStore
import com.astrochart.ui.i18n.LocalChartStyle
import com.astrochart.ui.i18n.LocalLanguage
import com.astrochart.ui.i18n.LocalStrings
import com.astrochart.core.interpret.RasiPeriod
import com.astrochart.ui.i18n.OnboardingStore
import com.astrochart.ui.i18n.PoruthamStrings
import com.astrochart.ui.i18n.PanchangamLocationStore
import com.astrochart.ui.i18n.PanchangamStrings
import com.astrochart.ui.i18n.PrimaryProfileStore
import com.astrochart.ui.i18n.RasiStrings
import com.astrochart.ui.i18n.UiStrings
import com.astrochart.ui.screens.BirthInputScreen
import com.astrochart.ui.screens.CalendarScreen
import com.astrochart.ui.screens.ChartDetailScreen
import com.astrochart.ui.screens.CompatibilityScreen
import com.astrochart.ui.screens.ChatScreen
import com.astrochart.ui.screens.HomeScreen
import com.astrochart.ui.screens.LanguagePickerDialog
import com.astrochart.ui.screens.NakshatraListScreen
import com.astrochart.ui.screens.OnboardingProfileStep
import com.astrochart.ui.screens.OnboardingWizard
import com.astrochart.ui.screens.PanchangamScreen
import com.astrochart.ui.screens.RasiHoroscopeScreen
import com.astrochart.ui.screens.RasiHubScreen
import com.astrochart.ui.screens.RasiInfoScreen
import com.astrochart.ui.screens.RasiPalanTwoPane
import com.astrochart.ui.screens.RasiSignsScreen
import com.astrochart.data.repository.SavedMatchRepository
import com.astrochart.ui.screens.SavedChartsScreen
import com.astrochart.ui.screens.SavedMatchesScreen
import com.astrochart.ui.screens.SettingsScreen
import com.astrochart.ui.screens.AccountScreen
import com.astrochart.ui.screens.SubscriptionScreen
import java.time.LocalDate
import java.time.YearMonth
import com.astrochart.ui.theme.AppTheme
import com.astrochart.ui.theme.AstroChartTheme
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.LocalAppTheme
import com.astrochart.ui.theme.LocalWindowSizeClass
import com.astrochart.ui.theme.TextPrimary
import com.astrochart.ui.theme.ThemeStore
import com.astrochart.ui.theme.buildTypography
import com.astrochart.ui.theme.fontFamilyForLanguage
import com.astrochart.ui.viewmodel.BirthInputViewModel
import com.astrochart.ui.viewmodel.ChartViewModel
import com.astrochart.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {

    private var inAppUpdate: InAppUpdate? = null
    private lateinit var updateLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var billingManager: BillingManager? = null

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationScheduler.ensureChannel(this)
        NotificationScheduler.scheduleDaily(this)
        if (Features.ADS_ENABLED) {
            // Safe to call repeatedly; no-ops without Play services present.
            runCatching { MobileAds.initialize(this) }
            com.astrochart.ads.InterstitialAds.preload(this)
        }
        if (Features.BILLING_ENABLED) {
            // Re-verifies whatever subscription Play reports once per launch —
            // the app's whole entitlement-refresh mechanism (no RTDN/Pub-Sub;
            // see the billing plan). Failures are silent: PremiumStore simply
            // keeps its last-known state until the next successful refresh.
            billingManager = BillingManager(this).also {
                lifecycleScope.launch { it.refreshEntitlement() }
            }
        }
        updateLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { /* user accepted/declined the Play update; nothing more to do here */ }
        if (Features.IN_APP_UPDATE_ENABLED) {
            inAppUpdate = InAppUpdate(this).also { it.checkForUpdate(updateLauncher) }
        }
        setContent {
            val context = LocalContext.current
            var appTheme by remember { mutableStateOf(ThemeStore.load(context)) }
            val windowSizeClass = calculateWindowSizeClass(this@MainActivity)
            var showOnboarding by remember { mutableStateOf(OnboardingStore.shouldShow(context)) }
            // Text renders at the size it was designed at, everywhere, regardless
            // of the device's own accessibility font-size setting — only the
            // font-scale multiplier is pinned; real screen-density scaling
            // (dp/pixel density, rotation, tablet width) is untouched.
            val fixedFontDensity = Density(
                density = LocalDensity.current.density,
                fontScale = 1f
            )
            AstroChartTheme {
                CompositionLocalProvider(
                    LocalAppTheme provides appTheme,
                    LocalWindowSizeClass provides windowSizeClass,
                    LocalDensity provides fixedFontDensity
                ) {
                    CelestialBackground {
                        if (showOnboarding) {
                            OnboardingWizard(onFinished = { showOnboarding = false })
                        } else {
                            RequestNotificationPermission()
                            AppNavigation(
                                appTheme = appTheme,
                                onThemeChange = {
                                    appTheme = it
                                    ThemeStore.save(context, it)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        inAppUpdate?.onResume()
    }

    override fun onDestroy() {
        inAppUpdate?.unregister()
        super.onDestroy()
    }
}

@Composable
private fun RequestNotificationPermission() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* result handled by the OS; the worker re-checks before posting */ }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

/**
 * On an Expanded window, renders the persistent Rasi Palan two-pane layout
 * instead of [singlePane] — so all four Rasi routes ("rasi_hub"/"rasi_signs"/
 * "rasi_horoscope"/"rasi_info") share the same split view on tablets and the
 * `navController.navigate(...)` calls inside [singlePane] are simply never
 * reached there, keeping the app on one backstack entry while browsing Rasi
 * Palan. On Compact/Medium, renders [singlePane] unchanged (today's phone
 * behavior, byte-for-byte).
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun RasiSignsOrTwoPane(
    rasiSign: Int,
    onRasiSignChange: (Int) -> Unit,
    rasiInfoMode: Boolean,
    onInfoModeChange: (Boolean) -> Unit,
    rasiPeriod: RasiPeriod,
    onPeriodChange: (RasiPeriod) -> Unit,
    onAboutNakshatras: () -> Unit,
    singlePane: @Composable () -> Unit
) {
    if (LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Expanded) {
        RasiPalanTwoPane(
            rasiSign = rasiSign,
            onRasiSignChange = onRasiSignChange,
            rasiInfoMode = rasiInfoMode,
            onInfoModeChange = onInfoModeChange,
            rasiPeriod = rasiPeriod,
            onPeriodChange = onPeriodChange,
            onAboutNakshatras = onAboutNakshatras
        )
    } else {
        singlePane()
    }
}

/**
 * Matchmaking, optionally reopening a saved match. The id is a query parameter
 * rather than a path segment so `navigate("compatibility")` still resolves —
 * that is how the home screen reaches it, and an optional argument keeps both
 * entry points on one destination instead of two near-identical ones.
 */
private const val COMPATIBILITY_ROUTE = "compatibility?matchId={matchId}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    appTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    val navController = rememberNavController()
    val chartViewModel: ChartViewModel = viewModel()
    val birthInputViewModel: BirthInputViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()

    val context = LocalContext.current
    var language by remember { mutableStateOf(LanguageStore.load(context)) }
    var chartStyle by remember { mutableStateOf(ChartStyleStore.load(context)) }
    var panchangamDate by remember { mutableStateOf(LocalDate.now()) }
    var panchangamMonth by remember { mutableStateOf(YearMonth.now()) }
    var panchangamLocation by remember { mutableStateOf(PanchangamLocationStore.load(context)) }
    var primaryProfile by remember { mutableStateOf(PrimaryProfileStore.load(context)) }
    var showLangPicker by remember { mutableStateOf(!LanguageStore.hasChosen(context)) }
    var rasiPeriod by remember { mutableStateOf(RasiPeriod.DAY) }
    var rasiInfoMode by remember { mutableStateOf(false) }
    var rasiSign by remember { mutableStateOf(primaryProfile?.rasi ?: 0) }
    val strings = remember(language) { UiStrings.forLanguage(language) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val canGoBack = route != null && route != "home"

    CompositionLocalProvider(
        LocalStrings provides strings,
        LocalLanguage provides language,
        LocalChartStyle provides chartStyle
    ) {
    // Reactive to `language`: bundled fonts guarantee the same glyphs render
    // everywhere regardless of the device's own system font (see
    // LocalizedFonts.kt). Only typography is overridden — colorScheme/shapes
    // fall through to AstroChartTheme's outer MaterialTheme unchanged.
    MaterialTheme(typography = buildTypography(fontFamilyForLanguage(language))) {
    val title = when (route) {
        "birth_input" -> strings.navCalculate
        "saved_charts" -> strings.navSavedChartsTitle
        "chart_detail" -> strings.chartTitle
        "chat" -> strings.navChatTitle
        "settings" -> strings.navSettingsTitle
        "edit_profile" -> strings.settingsPrimary
        "premium" -> strings.navPremiumTitle
        "account" -> strings.navAccountTitle
        "panchangam" -> PanchangamStrings.forLanguage(language).title
        "calendar" -> PanchangamStrings.forLanguage(language).calendarTitle
        // The pattern, not the bare name: NavController reports a
        // destination's route template, and this one now carries an
        // optional matchId for reopening a saved match.
        "compatibility", COMPATIBILITY_ROUTE -> PoruthamStrings.forLanguage(language).title
        "saved_matches" -> PoruthamStrings.forLanguage(language).savedMatches
        "rasi_hub", "rasi_signs", "rasi_horoscope" -> RasiStrings.forLanguage(language).title
        "rasi_info" -> RasiStrings.forLanguage(language).aboutSigns
        "nakshatra_list" -> RasiStrings.forLanguage(language).aboutNakshatras
        else -> strings.appName
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (canGoBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = strings.back,
                                tint = GoldDeep
                            )
                        }
                    }
                },
                actions = {
                    if (route != "panchangam" && route != "calendar") {
                        IconButton(onClick = { navController.navigate("panchangam") }) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = PanchangamStrings.forLanguage(language).calendarLabel,
                                tint = GoldDeep
                            )
                        }
                    }
                    if (route != "settings") {
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = strings.settingsLabel,
                                tint = GoldDeep
                            )
                        }
                    }
                    LanguageSwitcher(
                        current = language,
                        label = strings.languageLabel,
                        onSelect = {
                            language = it
                            LanguageStore.save(context, it)
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = GoldDeep,
                    navigationIconContentColor = GoldDeep
                )
            )
        },
        bottomBar = { AdBanner() }
    ) { innerPadding ->
        ResponsiveContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToBirthInput = { navController.navigate("birth_input") },
                    onNavigateToSavedCharts = { navController.navigate("saved_charts") },
                    onNavigateToChat = { navController.navigate("chat") },
                    onNavigateToPremium = { navController.navigate("premium") },
                    onNavigateToPanchangam = { navController.navigate("panchangam") },
                    onNavigateToCompatibility = { navController.navigate("compatibility") },
                    onNavigateToRasi = { navController.navigate("rasi_hub") }
                )
            }

            composable("rasi_hub") {
                RasiSignsOrTwoPane(
                    rasiSign, { rasiSign = it }, rasiInfoMode, { rasiInfoMode = it },
                    rasiPeriod, { rasiPeriod = it }, { navController.navigate("nakshatra_list") }
                ) {
                    RasiHubScreen(
                        onPeriod = { rasiPeriod = it; rasiInfoMode = false; navController.navigate("rasi_signs") },
                        onAboutSigns = { rasiInfoMode = true; navController.navigate("rasi_signs") },
                        onAboutNakshatras = { navController.navigate("nakshatra_list") }
                    )
                }
            }

            composable("rasi_signs") {
                RasiSignsOrTwoPane(
                    rasiSign, { rasiSign = it }, rasiInfoMode, { rasiInfoMode = it },
                    rasiPeriod, { rasiPeriod = it }, { navController.navigate("nakshatra_list") }
                ) {
                    RasiSignsScreen(onPick = { i ->
                        rasiSign = i
                        navController.navigate(if (rasiInfoMode) "rasi_info" else "rasi_horoscope")
                    })
                }
            }

            composable("rasi_horoscope") {
                RasiSignsOrTwoPane(
                    rasiSign, { rasiSign = it }, rasiInfoMode, { rasiInfoMode = it },
                    rasiPeriod, { rasiPeriod = it }, { navController.navigate("nakshatra_list") }
                ) {
                    RasiHoroscopeScreen(rasiSign, rasiPeriod, onPeriodChange = { rasiPeriod = it })
                }
            }
            composable("rasi_info") {
                RasiSignsOrTwoPane(
                    rasiSign, { rasiSign = it }, rasiInfoMode, { rasiInfoMode = it },
                    rasiPeriod, { rasiPeriod = it }, { navController.navigate("nakshatra_list") }
                ) {
                    RasiInfoScreen(rasiSign)
                }
            }
            composable("nakshatra_list") { NakshatraListScreen() }

            composable(
                route = COMPATIBILITY_ROUTE,
                arguments = listOf(
                    navArgument("matchId") {
                        type = NavType.LongType
                        // -1 rather than a nullable argument: NavType.LongType
                        // cannot be null, and no real row id is negative.
                        defaultValue = -1L
                    }
                )
            ) { entry ->
                val matchId = entry.arguments?.getLong("matchId") ?: -1L
                CompatibilityScreen(
                    onNavigateToPremium = { navController.navigate("premium") },
                    onNavigateToSavedMatches = { navController.navigate("saved_matches") },
                    initialMatchId = matchId.takeIf { it >= 0 }
                )
            }

            composable("saved_matches") {
                val repository = remember(context) { SavedMatchRepository(context) }
                val scope = rememberCoroutineScope()
                // Remembered, not called per composition: observeAll() hands
                // back a fresh Flow each call, and collecting a new one on
                // every recomposition would restart the query each time.
                val matches by remember(repository) { repository.observeAll() }
                    .collectAsState(initial = emptyList())
                SavedMatchesScreen(
                    matches = matches,
                    onMatchSelected = { id -> navController.navigate("compatibility?matchId=$id") },
                    onDelete = { id -> scope.launch { repository.delete(id) } }
                )
            }

            composable("settings") {
                SettingsScreen(
                    currentStyle = chartStyle,
                    onStyleChange = {
                        chartStyle = it
                        ChartStyleStore.save(context, it)
                    },
                    currentLanguage = language,
                    onLanguageChange = {
                        language = it
                        LanguageStore.save(context, it)
                    },
                    currentTheme = appTheme,
                    onThemeChange = onThemeChange,
                    currentLocation = panchangamLocation,
                    onLocationChange = {
                        panchangamLocation = it
                        PanchangamLocationStore.save(context, it.displayName)
                    },
                    primary = primaryProfile,
                    onNavigateToEditProfile = { navController.navigate("edit_profile") },
                    onNavigateToPremium = { navController.navigate("premium") },
                    onNavigateToAccount = { navController.navigate("account") }
                )
            }

            composable("edit_profile") {
                OnboardingProfileStep(
                    onSaved = {
                        primaryProfile = PrimaryProfileStore.load(context)
                        primaryProfile?.let { rasiSign = it.rasi }
                        navController.popBackStack()
                    },
                    onSkip = { navController.popBackStack() }
                )
            }

            composable("premium") {
                SubscriptionScreen()
            }

            composable("account") {
                AccountScreen()
            }

            composable("panchangam") {
                PanchangamScreen(
                    date = panchangamDate,
                    onDateChange = { panchangamDate = it },
                    location = panchangamLocation,
                    onLocationChange = {
                        panchangamLocation = it
                        PanchangamLocationStore.save(context, it.displayName)
                    },
                    onOpenCalendar = {
                        panchangamMonth = YearMonth.from(panchangamDate)
                        navController.navigate("calendar")
                    }
                )
            }

            composable("calendar") {
                CalendarScreen(
                    month = panchangamMonth,
                    onMonthChange = { panchangamMonth = it },
                    location = panchangamLocation,
                    onDaySelected = { selected ->
                        panchangamDate = selected
                        navController.navigate("panchangam") {
                            popUpTo("panchangam") { inclusive = true }
                        }
                    }
                )
            }

            composable("birth_input") {
                BirthInputScreen(
                    viewModel = birthInputViewModel,
                    onChartCalculated = { chart, name, _ ->
                        chartViewModel.setChart(chart, name)
                        navController.navigate("chart_detail") {
                            popUpTo("home")
                        }
                    }
                )
            }

            composable("saved_charts") {
                val savedCharts by chartViewModel.savedCharts.collectAsState()
                SavedChartsScreen(
                    charts = savedCharts,
                    onChartSelected = { id ->
                        chartViewModel.loadSavedChart(id)
                        navController.navigate("chart_detail")
                    },
                    onRename = { id, name -> chartViewModel.renameSavedChart(id, name) },
                    onDelete = { id -> chartViewModel.deleteSavedChart(id) }
                )
            }

            composable("chart_detail") {
                val chart by chartViewModel.currentChart.collectAsState()
                val chartName by chartViewModel.currentChartName.collectAsState()
                ChartDetailScreen(
                    chart = chart,
                    chartName = chartName
                )
            }

            if (Features.CHAT_ENABLED) {
                composable("chat") {
                    ChatScreen(
                        viewModel = chatViewModel,
                        onNavigateToBirthInput = {
                            navController.navigate("birth_input") { popUpTo("home") }
                        }
                    )
                }
            }
        }
        }
    }

    if (showLangPicker) {
        LanguagePickerDialog(
            onSelect = {
                language = it
                LanguageStore.save(context, it)
                showLangPicker = false
            },
            onSkip = {
                LanguageStore.save(context, language)
                showLangPicker = false
            }
        )
    }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSwitcher(
    current: Language,
    label: String,
    onSelect: (Language) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(
            imageVector = Icons.Filled.Translate,
            contentDescription = label,
            tint = GoldDeep
        )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        Language.entries.forEach { lang ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = lang.displayName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = fontFamilyForLanguage(lang)),
                        color = if (lang == current) GoldDeep else TextPrimary
                    )
                },
                onClick = {
                    onSelect(lang)
                    expanded = false
                }
            )
        }
    }
}
