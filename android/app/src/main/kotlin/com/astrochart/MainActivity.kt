package com.astrochart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.astrochart.ui.screens.BirthInputScreen
import com.astrochart.ui.screens.ChartDetailScreen
import com.astrochart.ui.screens.HomeScreen
import com.astrochart.ui.screens.SavedChartsScreen
import com.astrochart.ui.theme.AstroChartTheme
import com.astrochart.ui.viewmodel.BirthInputViewModel
import com.astrochart.ui.viewmodel.ChartViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AstroChartTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val chartViewModel: ChartViewModel = viewModel()
    val birthInputViewModel: BirthInputViewModel = viewModel()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val title = when (route) {
        "birth_input" -> "Calculate My Chart"
        "saved_charts" -> "Saved Charts"
        "chart_detail" -> "Chart"
        else -> "AstroChart"
    }
    val canGoBack = route != null && route != "home"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (canGoBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Text("←", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
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
                    onNavigateToSample = {
                        chartViewModel.loadSampleChart()
                        navController.navigate("chart_detail")
                    }
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
        }
    }
}
