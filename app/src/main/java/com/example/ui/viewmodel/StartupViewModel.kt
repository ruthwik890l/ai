package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.StartupAnalysis
import com.example.data.model.StartupReport
import com.example.data.repository.StartupRepository
import com.example.data.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StartupViewModel(private val repository: StartupRepository) : ViewModel() {

    val savedReports: StateFlow<List<StartupReport>> = repository.allReports
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activeReportId = MutableStateFlow<Long?>(null)
    val activeReportId = _activeReportId.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()

    private val _analysisStep = MutableStateFlow<String?>(null)
    val analysisStep = _analysisStep.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Combine list and active selection id. Very robust!
    val activeReport: StateFlow<StartupReport?> = combine(savedReports, _activeReportId) { reports, id ->
        reports.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectReport(id: Long?) {
        _activeReportId.value = id
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun deleteReport(id: Long) {
        viewModelScope.launch {
            repository.deleteReportById(id)
            if (_activeReportId.value == id) {
                _activeReportId.value = null
            }
        }
    }

    fun rerunAnalysis(report: StartupReport) {
        analyzeAndSave(
            id = report.id,
            startupName = report.startupName,
            idea = report.idea,
            problem = report.problem,
            industry = report.industry,
            targetCustomers = report.targetCustomers,
            country = report.country,
            revenueModel = report.revenueModel,
            stage = report.stage
        )
    }

    fun submitNewIdea(
        startupName: String,
        idea: String,
        problem: String,
        industry: String,
        targetCustomers: String,
        country: String,
        revenueModel: String,
        stage: String
    ) {
        analyzeAndSave(
            id = 0,
            startupName = startupName,
            idea = idea,
            problem = problem,
            industry = industry,
            targetCustomers = targetCustomers,
            country = country,
            revenueModel = revenueModel,
            stage = stage
        )
    }

    private fun analyzeAndSave(
        id: Long,
        startupName: String,
        idea: String,
        problem: String,
        industry: String,
        targetCustomers: String,
        country: String,
        revenueModel: String,
        stage: String
    ) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _errorMessage.value = null
            _analysisStep.value = "Saving idea draft to local database..."

            val initialReport = StartupReport(
                id = id,
                startupName = startupName,
                idea = idea,
                problem = problem,
                industry = industry,
                targetCustomers = targetCustomers,
                country = country,
                revenueModel = revenueModel,
                stage = stage,
                timestamp = System.currentTimeMillis(),
                analysisJson = null
            )

            val reportId = if (id == 0L) {
                repository.insertReport(initialReport)
            } else {
                repository.updateReport(initialReport)
                id
            }

            _activeReportId.value = reportId

            try {
                _analysisStep.value = "Gathering industry benchmarks..."
                kotlinx.coroutines.delay(500)
                _analysisStep.value = "Conducting competitor research matrices..."
                kotlinx.coroutines.delay(500)
                _analysisStep.value = "Formulating financial projection algorithms..."
                kotlinx.coroutines.delay(500)
                _analysisStep.value = "Consulting Gemini AI Validator engine..."

                val analysis = repository.validateStartupIdea(
                    name = startupName,
                    idea = idea,
                    problem = problem,
                    industry = industry,
                    targetCustomers = targetCustomers,
                    country = country,
                    revenueModel = revenueModel,
                    stage = stage
                )

                _analysisStep.value = "Structuring dynamic report profiles..."
                val jsonAdapter = RetrofitClient.moshiInstance.adapter(StartupAnalysis::class.java)
                val jsonString = jsonAdapter.toJson(analysis)

                val completedReport = initialReport.copy(
                    id = reportId,
                    validationScore = analysis.validationScore,
                    marketPotential = analysis.marketPotential,
                    competitionLevel = analysis.competitionLevel,
                    analysisJson = jsonString
                )
                repository.updateReport(completedReport)

            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred during validation"
            } finally {
                _isAnalyzing.value = false
                _analysisStep.value = null
            }
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StartupViewModel::class.java)) {
                val db = AppDatabase.getDatabase(application)
                val repository = StartupRepository(db.startupReportDao())
                @Suppress("UNCHECKED_CAST")
                return StartupViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
