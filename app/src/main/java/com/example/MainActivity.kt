package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.SubmissionScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StartupViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: StartupViewModel by viewModels {
        StartupViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    viewModel: StartupViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    val savedReports by viewModel.savedReports.collectAsStateWithLifecycle()
    val activeReport by viewModel.activeReport.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val analysisStep by viewModel.analysisStep.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = "dashboard",
        modifier = modifier
    ) {
        composable("dashboard") {
            DashboardScreen(
                reports = savedReports,
                onSelectReport = { id ->
                    viewModel.selectReport(id)
                    navController.navigate("result")
                },
                onDeleteReport = { id ->
                    viewModel.deleteReport(id)
                },
                onNewFormClick = {
                    navController.navigate("submission")
                }
            )
        }

        composable("submission") {
            SubmissionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSubmit = { name, idea, problem, industry, customers, country, revenue, stage ->
                    viewModel.submitNewIdea(name, idea, problem, industry, customers, country, revenue, stage)
                    navController.navigate("result") {
                        popUpTo("dashboard")
                    }
                }
            )
        }

        composable("result") {
            ResultScreen(
                report = activeReport,
                isAnalyzing = isAnalyzing,
                analysisStep = analysisStep,
                errorMessage = errorMessage,
                onBackClick = {
                    viewModel.selectReport(null)
                    navController.popBackStack("dashboard", false)
                },
                onRerunClick = { report ->
                    viewModel.rerunAnalysis(report)
                },
                onClearError = {
                    viewModel.clearError()
                }
            )
        }
    }
}
