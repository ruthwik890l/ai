package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StartupAnalysis(
    val validationScore: Int,
    val marketPotential: String,
    val competitionLevel: String,
    val swot: SwotAnalysis,
    val targetAudienceAnalysis: TargetAudienceAnalysis,
    val businessModelSuggestions: List<String>,
    val revenueModels: List<RevenueModelItem>,
    val mvpRoadmap: List<MvpPhase>,
    val risks: List<RiskItem>,
    val goToMarketStrategy: List<String>,
    val competitorResearch: CompetitorResearchUnit,
    val marketOpportunity: MarketOpportunityUnit,
    val pitchDeck: List<PitchDeckSlide>,
    val revenuePredictions: RevenuePredictionsUnit
)

@JsonClass(generateAdapter = true)
data class SwotAnalysis(
    val strengths: List<String>,
    val weaknesses: List<String>,
    val opportunities: List<String>,
    val threats: List<String>
)

@JsonClass(generateAdapter = true)
data class TargetAudienceAnalysis(
    val demographics: String,
    val painPoints: List<String>,
    val behaviorPattern: String,
    val userPersonaName: String,
    val userPersonaQuote: String
)

@JsonClass(generateAdapter = true)
data class RevenueModelItem(
    val model: String,
    val explanation: String
)

@JsonClass(generateAdapter = true)
data class MvpPhase(
    val phase: String,
    val title: String,
    val features: List<String>,
    val duration: String
)

@JsonClass(generateAdapter = true)
data class RiskItem(
    val risk: String,
    val severity: String, // High, Medium, Low
    val mitigation: String
)

@JsonClass(generateAdapter = true)
data class CompetitorItem(
    val name: String,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val pricing: String,
    val differentiation: String
)

@JsonClass(generateAdapter = true)
data class CompetitorResearchUnit(
    val directCompetitors: List<CompetitorItem>,
    val indirectCompetitors: List<CompetitorItem>,
    val marketLeaders: List<CompetitorItem>
)

@JsonClass(generateAdapter = true)
data class MarketOpportunityUnit(
    val tam: String, // Total Addressable Market
    val sam: String, // Serviceable Addressable Market
    val som: String, // Serviceable Obtainable Market
    val growthPotential: String,
    val trendAnalysis: String,
    val marketMaturity: String
)

@JsonClass(generateAdapter = true)
data class PitchDeckSlide(
    val slideNumber: Int,
    val title: String,
    val bulletPoints: List<String>
)

@JsonClass(generateAdapter = true)
data class RevenuePredictionsUnit(
    val revenueStreams: List<String>,
    val pricingSuggestions: List<String>,
    val monthlyProjections: List<ProjectionPoint>,
    val yearlyProjections: List<ProjectionPoint>
)

@JsonClass(generateAdapter = true)
data class ProjectionPoint(
    val label: String, // e.g., "Month 1", "Year 1"
    val revenueAmount: Double
)
