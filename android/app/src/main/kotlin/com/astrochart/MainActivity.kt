package com.astrochart

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.astrochart.core.i18n.Language
import com.astrochart.notify.NotificationScheduler
import com.astrochart.ui.components.CelestialBackground
import com.astrochart.ui.i18n.LanguageStore
import com.astrochart.ui.i18n.LocalLanguage
import com.astrochart.ui.i18n.LocalStrings
import com.astrochart.ui.i18n.UiStrings
import com.astrochart.ui.screens.BirthInputScreen
import com.astrochart.ui.screens.ChartDetailScreen
import com.astrochart.ui.screens.ChatScreen
import com.astrochart.ui.screens.HomeScreen
import com.astrochart.ui.screens.SavedChartsScreen
import com.astrochart.ui.theme.AstroChartTheme
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.TextPrimary
import com.astrochart.ui.viewmodel.BirthInputViewModel
import com.astrochart.ui.viewmodel.ChartViewModel
import com.astrochart.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationScheduler.ensureChannel(this)
        NotificationScheduler.scheduleDaily(this)
        setContent {
            AstroChartTheme {
                CelestialBackground {
                    RequestNotificationPermission()
                    AppNavigation()
                }
            }
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val chartViewModel: ChartViewModel = viewModel()
    val birthInputViewModel: BirthInputViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()

    val context = LocalContext.current
    var language by remember { mutableStateOf(LanguageStore.load(context)) }
    val strings = remember(language) { UiStrings.forLanguage(language) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val canGoBack = route != null && route != "home"

    CompositionLocalProvider(
        LocalStrings provides strings,
        LocalLanguage provides language
    ) {
    val title = when (route) {
        "birth_input" -> strings.navCalculate
        "saved_charts" -> strings.navSavedChartsTitle
        "chart_detail" -> strings.chartTitle
        "chat" -> strings.navChatTitle
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
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = GoldDeep
                )
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToBirthInput = { navController.navigate("birth_input") },
                    onNavigateToSavedCharts = { navController.navigate("saved_charts") },
                    onNavigateToChat = { navController.navigate("chat") }
                )
            }

            composable("birth_input") {
                BirthInputScreen(
                    viewModel = birthInputViewModel,
                    onChartCalculated = { chart, name ->
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
