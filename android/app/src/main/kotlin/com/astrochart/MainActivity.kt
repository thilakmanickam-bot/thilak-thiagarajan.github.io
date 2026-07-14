package com.astrochart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.astrochart.ui.screens.BirthInputScreen
import com.astrochart.ui.screens.ChartDetailScreen
import com.astrochart.ui.screens.HomeScreen
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
                onNavigateToBirthInput = { navController.navigate("birth_input") }
            )
        }

        composable("birth_input") {
            BirthInputScreen(
                viewModel = birthInputViewModel,
                onChartCalculated = {
                    navController.navigate("chart_detail") {
                        popUpTo("home")
                    }
                }
            )
        }

        composable("chart_detail") {
            ChartDetailScreen(
                chart = chartViewModel.currentChart.value,
                viewModel = chartViewModel
            )
        }
    }
}
