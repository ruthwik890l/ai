package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.RetrofitClient
import com.example.data.db.StartupReportDao
import com.example.data.model.StartupAnalysis
import com.example.data.model.StartupReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class StartupRepository(private val startupReportDao: StartupReportDao) {

    val allReports: Flow<List<StartupReport>> = startupReportDao.getAllReports()

    suspend fun getReportById(id: Long): StartupReport? = withContext(Dispatchers.IO) {
        startupReportDao.getReportById(id)
    }

    suspend fun insertReport(report: StartupReport): Long = withContext(Dispatchers.IO) {
        startupReportDao.insertReport(report)
    }

    suspend fun updateReport(report: StartupReport) = withContext(Dispatchers.IO) {
        startupReportDao.updateReport(report)
    }

    suspend fun deleteReportById(id: Long) = withContext(Dispatchers.IO) {
        startupReportDao.deleteReportById(id)
    }

    suspend fun deleteReport(report: StartupReport) = withContext(Dispatchers.IO) {
        startupReportDao.deleteReport(report)
    }

    suspend fun validateStartupIdea(
        name: String,
        idea: String,
        problem: String,
        industry: String,
        targetCustomers: String,
        country: String,
        revenueModel: String,
        stage: String
    ): StartupAnalysis = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("Gemini API key is not configured. Please enter a valid key in the AI Studio Secrets panel.")
        }

        val prompt = getPromptText(
            name = name,
            idea = idea,
            problem = problem,
            industry = industry,
            targetCustomers = targetCustomers,
            country = country,
            revenueModel = revenueModel,
            stage = stage
        )

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2
            )
        )

        // Try gemini-3.1-pro-preview first, fallback to gemini-3.5-flash
        var responseText = ""
        try {
            val response = RetrofitClient.service.generateContent(
                model = "gemini-3.1-pro-preview",
                apiKey = apiKey,
                request = request
            )
            responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
        } catch (e: Exception) {
            // Log or fallback
            try {
                val fallbackResponse = RetrofitClient.service.generateContent(
                    model = "gemini-3.5-flash",
                    apiKey = apiKey,
                    request = request
                )
                responseText = fallbackResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            } catch (fallbackEx: Exception) {
                throw Exception("Failed to call Gemini API: ${fallbackEx.message}. Primary model error: ${e.message}")
            }
        }

        if (responseText.isEmpty()) {
            throw Exception("Empty response returned from Gemini API")
        }

        val cleanedJson = cleanJsonString(responseText)
        try {
            val adapter = RetrofitClient.moshiInstance.adapter(StartupAnalysis::class.java)
            adapter.fromJson(cleanedJson) ?: throw Exception("Null parsed startup analysis")
        } catch (jsonEx: Exception) {
            throw Exception("Failed to parse AI response into structure. Raw Text: $cleanedJson. Exception: ${jsonEx.message}")
        }
    }

    private fun cleanJsonString(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.substringAfter("```json").substringBeforeLast("```")
        } else if (text.startsWith("```")) {
            text = text.substringAfter("```").substringBeforeLast("```")
        }
        return text.trim()
    }

    private fun getPromptText(
        name: String,
        idea: String,
        problem: String,
        industry: String,
        targetCustomers: String,
        country: String,
        revenueModel: String,
        stage: String
    ): String {
        return """
            You are an expert startup validator and venture capitalist analyst.
            Analyze the following startup idea and return a detailed report strictly in JSON format matching the schema rules below.
            
            Startup Profile:
            - Name: $name
            - Industry: $industry
            - Idea: $idea
            - Problem Solved: $problem
            - Target Customers: $targetCustomers
            - Market Country: $country
            - Preferential Revenue Model: $revenueModel
            - Development Stage: $stage
            
            Prepare a thorough, highly realistic analysis containing accurate numbers and lists, returning EXACTLY a JSON object matching this structure:
            
            {
              "validationScore": Int (from 0 to 100),
              "marketPotential": "High / Medium / Low",
              "competitionLevel": "High / Medium / Low",
              "swot": {
                "strengths": ["list of 4 strengths"],
                "weaknesses": ["list of 4 weaknesses"],
                "opportunities": ["list of 4 opportunities"],
                "threats": ["list of 4 threats"]
              },
              "targetAudienceAnalysis": {
                "demographics": "specific target segment traits",
                "painPoints": ["list of 3 key frustrations"],
                "behaviorPattern": "how they currently deal with this",
                "userPersonaName": "persona archetype name",
                "userPersonaQuote": "persona first hand frustration quote"
              },
              "businessModelSuggestions": ["3 recommended strategic plays"],
              "revenueModels": [
                { "model": "SaaS / Commission / Ad, etc.", "explanation": "specific justification and value point" }
              ],
              "mvpRoadmap": [
                { "phase": "Phase 1: Brand & Core", "title": "Minimal Value Feature Set", "features": ["feature 1", "feature 2"], "duration": "Weeks 1-4" },
                { "phase": "Phase 2: Engagement", "title": "Growth mechanics & Feedback", "features": ["feature 3"], "duration": "Weeks 5-8" },
                { "phase": "Phase 3: Automation", "title": "Integrations & scale", "features": ["feature 4"], "duration": "Weeks 9-12" }
              ],
              "risks": [
                { "risk": "critical threat description", "severity": "High / Medium / Low", "mitigation": "reasonable strategy" }
              ],
              "goToMarketStrategy": ["launch phase", "growth tactic 1", "referral loop", "channel growth"],
              "competitorResearch": {
                "directCompetitors": [
                  { "name": "Competitor X", "strengths": ["UX", "Pricing"], "weaknesses": ["Legacy", "Slow support"], "pricing": "$49/mo", "differentiation": "Ours is simpler and tailored" }
                ],
                "indirectCompetitors": [
                  { "name": "Excel / Manual", "strengths": ["Free"], "weaknesses": ["Error prone", "Offline"], "pricing": "Free", "differentiation": "Seamless automated logic" }
                ],
                "marketLeaders": [
                  { "name": "BigTech Leader Y", "strengths": ["Huge capital"], "weaknesses": ["Too bloated for startups"], "pricing": "$999/mo", "differentiation": "Niche specific workflows" }
                ]
              },
              "marketOpportunity": {
                "tam": "Annual spend estimate e.g. $10B",
                "sam": "Addressable subset e.g. $1.5B",
                "som": "Realistic target first 2 years e.g. $50M",
                "growthPotential": "e.g. Growing at some CAGR",
                "trendAnalysis": "key industry trends fueling this",
                "marketMaturity": "Status + explanation"
              },
              "pitchDeck": [
                { "slideNumber": 1, "title": "1. Problem", "bulletPoints": ["bullet points for problem slide", "bullet 2"] },
                { "slideNumber": 2, "title": "2. Solution", "bulletPoints": ["bullet points for solution slide", "bullet 2"] },
                { "slideNumber": 3, "title": "3. Market Opportunity", "bulletPoints": ["bullet points for market slide", "bullet 2"] },
                { "slideNumber": 4, "title": "4. Product", "bulletPoints": ["bullet points for product slide", "bullet 2"] },
                { "slideNumber": 5, "title": "5. Business Model", "bulletPoints": ["bullet points for business model slide", "bullet 2"] },
                { "slideNumber": 6, "title": "6. Competition", "bulletPoints": ["bullet points for competition slide", "bullet 2"] },
                { "slideNumber": 7, "title": "7. Traction / Roadmap", "bulletPoints": ["bullet points for traction slide", "bullet 2"] },
                { "slideNumber": 8, "title": "8. Financials", "bulletPoints": ["bullet points for financial slide", "bullet 2"] },
                { "slideNumber": 9, "title": "9. Team", "bulletPoints": ["bullet points for team slide", "bullet 2"] },
                { "slideNumber": 10, "title": "10. Ask / Vision", "bulletPoints": ["bullet points for ask slide", "bullet 2"] }
              ],
              "revenuePredictions": {
                "revenueStreams": ["stream 1", "stream 2"],
                "pricingSuggestions": ["basic plan price details", "pro tier price details"],
                "monthlyProjections": [
                  { "label": "Month 1", "revenueAmount": 0.0 },
                  { "label": "Month 2", "revenueAmount": 800.0 },
                  { "label": "Month 3", "revenueAmount": 2200.0 },
                  { "label": "Month 4", "revenueAmount": 4500.0 },
                  { "label": "Month 5", "revenueAmount": 9000.0 },
                  { "label": "Month 6", "revenueAmount": 15000.0 }
                ],
                "yearlyProjections": [
                  { "label": "Year 1", "revenueAmount": 60000.0 },
                  { "label": "Year 2", "revenueAmount": 240000.0 },
                  { "label": "Year 3", "revenueAmount": 900000.0 }
                ]
              }
            }
            
            Respond only with raw valid JSON. Do not include markdown code block characters like ```json.
        """.trimIndent()
    }
}
