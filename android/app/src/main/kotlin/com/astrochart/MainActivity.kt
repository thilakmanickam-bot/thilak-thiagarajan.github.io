package com.astrochart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val chartViewModel: ChartViewModel = viewModel()
    val birthInputViewModel: BirthInputViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.fillMaxSize()
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
                onChartCalculated = { chart ->
                    chartViewModel.setChart(chart)
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
                }
            )
        }

        composable("chart_detail") {
            val chart by chartViewModel.currentChart.collectAsState()
            ChartDetailScreen(
                chart = chart,
                viewModel = chartViewModel
            )
        }
    }
}
