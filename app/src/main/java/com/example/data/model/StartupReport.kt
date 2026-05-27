package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "startup_reports")
data class StartupReport(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startupName: String,
    val idea: String,
    val problem: String,
    val industry: String,
    val targetCustomers: String,
    val country: String,
    val revenueModel: String,
    val stage: String,
    val timestamp: Long = System.currentTimeMillis(),
    val validationScore: Int = 0,
    val marketPotential: String = "",
    val competitionLevel: String = "",
    val analysisJson: String? = null // Moshi-encoded StartupAnalysis JSON
)
