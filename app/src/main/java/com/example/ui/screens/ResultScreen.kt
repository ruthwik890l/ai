package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.RetrofitClient
import com.example.data.model.StartupAnalysis
import com.example.data.model.StartupReport
import com.example.ui.components.MarketSizingChart
import com.example.ui.components.PdfExporter
import com.example.ui.components.RevenueChart
import com.example.ui.components.ScoreGauge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    report: StartupReport?,
    isAnalyzing: Boolean,
    analysisStep: String?,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onRerunClick: (StartupReport) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val parsedAnalysis = remember(report?.analysisJson) {
        report?.analysisJson?.let {
            try {
                RetrofitClient.moshiInstance.adapter(StartupAnalysis::class.java).fromJson(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = report?.startupName ?: "Report Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (report != null && parsedAnalysis != null) {
                        IconButton(
                            modifier = Modifier.testTag("pdf_export_button"),
                            onClick = {
                                val file = PdfExporter.exportReportToPdf(context, report, parsedAnalysis)
                                if (file != null) {
                                    Toast.makeText(context, "PDF Report exported successfully:\n${file.absolutePath}", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Failed to generate PDF Report", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "Export to PDF", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isAnalyzing -> {
                    AnalysisLoadingView(step = analysisStep ?: "Consulting Gemini AI Validator...")
                }
                errorMessage != null -> {
                    AnalysisErrorView(error = errorMessage, onDismiss = {
                        onClearError()
                        onBackClick()
                    }, onRetry = {
                        onClearError()
                        report?.let { onRerunClick(it) }
                    })
                }
                report != null && parsedAnalysis != null -> {
                    AnalysisDashboardView(report = report, analysis = parsedAnalysis, onRerunClick = { onRerunClick(report) })
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(54.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No analysis results found", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalysisLoadingView(step: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 6.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
            Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "AI Validation Engine Running",
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                slideInVertically { height -> height } + fadeIn() togetherWith
                        slideOutVertically { height -> -height } + fadeOut()
            },
            label = "StepAnimation"
        ) { currentStep ->
            Text(
                text = currentStep,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Our systems are evaluating TAM metrics, scanning local competitors, mapping MVP schedules, and generating slide structures...",
            fontSize = 11.sp,
            lineHeight = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun AnalysisErrorView(
    error: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Validation Interrupted",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = error,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onDismiss) {
                Text("Back to Hub")
            }
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Retry Verification")
            }
        }
    }
}

@Composable
fun AnalysisDashboardView(
    report: StartupReport,
    analysis: StartupAnalysis,
    onRerunClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Market", "Competitors", "MVP & Revenue", "Pitch Deck")

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 12.dp,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                0 -> OverviewTabSection(report = report, analysis = analysis, onRerunClick = onRerunClick)
                1 -> MarketTabSection(analysis = analysis)
                2 -> CompetitorsTabSection(analysis = analysis)
                3 -> MvpRevenueTabSection(analysis = analysis)
                4 -> PitchDeckTabSection(analysis = analysis)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OverviewTabSection(
    report: StartupReport,
    analysis: StartupAnalysis,
    onRerunClick: () -> Unit
) {
    // Score Badge section
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScoreGauge(score = analysis.validationScore, modifier = Modifier.size(160.dp))

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Market Potential", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        analysis.marketPotential,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Competitor Density", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        analysis.competitionLevel,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }

    // SWOT Interactive Panels
    Text("SWOT Assessment Grid", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SwotCard(
                title = "STRENGTHS",
                items = analysis.swot.strengths,
                headerColor = Color(0xFF2E7D32),
                backgroundColor = Color(0xFFE8F5E9),
                icon = Icons.Default.ThumbUp,
                modifier = Modifier.weight(1f)
            )
            SwotCard(
                title = "WEAKNESSES",
                items = analysis.swot.weaknesses,
                headerColor = Color(0xFFC62828),
                backgroundColor = Color(0xFFFFE9E9),
                icon = Icons.Default.ThumbDown,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SwotCard(
                title = "OPPORTUNITIES",
                items = analysis.swot.opportunities,
                headerColor = Color(0xFF1565C0),
                backgroundColor = Color(0xFFE8F0FE),
                icon = Icons.Default.Update,
                modifier = Modifier.weight(1f)
            )
            SwotCard(
                title = "THREATS",
                items = analysis.swot.threats,
                headerColor = Color(0xFFEF6C00),
                backgroundColor = Color(0xFFFFF3E0),
                icon = Icons.Default.TrendingDown,
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Suggested Strategies Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Business Model & Blueprint Suggestions", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            analysis.businessModelSuggestions.forEach { item ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Adjust, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 2.dp).size(14.dp))
                    Text(item, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }

    // Re-run
    TextButton(
        onClick = onRerunClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
    ) {
        Icon(Icons.Default.Refresh, contentDescription = null)
        Spacer(modifier = Modifier.width(6.dp))
        Text("Re-run AI Analysis Models")
    }
}

@Composable
fun SwotCard(
    title: String,
    items: List<String>,
    headerColor: Color,
    backgroundColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, headerColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = headerColor, modifier = Modifier.size(16.dp))
                Text(title, fontWeight = FontWeight.Black, fontSize = 12.sp, color = headerColor)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items.forEach { bullet ->
                    Text(
                        text = "• $bullet",
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun MarketTabSection(analysis: StartupAnalysis) {
    // TAM / SAM / SOM Concentric Chart
    MarketSizingChart(
        tam = analysis.marketOpportunity.tam,
        sam = analysis.marketOpportunity.sam,
        som = analysis.marketOpportunity.som,
        modifier = Modifier.fillMaxWidth()
    )

    // Auditing demography & persona quotes
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Target Audience Archetype", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Text(paragraphHeader("Demographics", analysis.targetAudienceAnalysis.demographics), fontSize = 12.sp)
            Text(paragraphHeader("Current Alternatives", analysis.targetAudienceAnalysis.behaviorPattern), fontSize = 12.sp)

            Text("Buyer Friction Pain Points:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            analysis.targetAudienceAnalysis.painPoints.forEach { item ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                    Text(item, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }

    // Persona Graphic Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
            }

            Column {
                Text(analysis.targetAudienceAnalysis.userPersonaName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                Text("Target Archetype Avatar Quote:", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "\"${analysis.targetAudienceAnalysis.userPersonaQuote}\"",
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    // Market summary
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Macro Trends & Competitiveness", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(paragraphHeader("Trend Vectors", analysis.marketOpportunity.trendAnalysis), fontSize = 12.sp)
            Text(paragraphHeader("Market Maturity", analysis.marketOpportunity.marketMaturity), fontSize = 12.sp)
        }
    }
}

@Composable
fun CompetitorsTabSection(analysis: StartupAnalysis) {
    Text("Competitive Landscape Ecosystem", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)

    CompetitorGroupingSection(title = "Direct Industry Competitors", list = analysis.competitorResearch.directCompetitors, badgeColor = MaterialTheme.colorScheme.error)
    CompetitorGroupingSection(title = "Indirect Alternatives", list = analysis.competitorResearch.indirectCompetitors, badgeColor = MaterialTheme.colorScheme.secondary)
    CompetitorGroupingSection(title = "Global Market Leaders", list = analysis.competitorResearch.marketLeaders, badgeColor = MaterialTheme.colorScheme.primary)
}

@Composable
fun CompetitorGroupingSection(
    title: String,
    list: List<com.example.data.model.CompetitorItem>,
    badgeColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = badgeColor, modifier = Modifier.padding(vertical = 4.dp))

        if (list.isEmpty()) {
            Text("No comparative competitors assessed for this grouping.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            list.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            SuggestionChip(onClick = {}, label = { Text(item.pricing, fontSize = 9.sp) })
                        }

                        Text("Strengths: " + item.strengths.joinToString(", "), fontSize = 11.sp, color = Color(0xFF2E7D32))
                        Text("Weaknesses: " + item.weaknesses.joinToString(", "), fontSize = 11.sp, color = Color(0xFFC62828))
                        Text(paragraphHeader("Competitive Wedge", item.differentiation), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MvpRevenueTabSection(analysis: StartupAnalysis) {
    // Beautiful Projections line graph
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        RevenueChart(
            projections = analysis.revenuePredictions.monthlyProjections,
            themeColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }

    // Revenue Models suggest card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Suggested Pricing Tiers & Models", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            analysis.revenueModels.forEach { item ->
                Column {
                    Text(item.model, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(item.explanation, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    // MVP Timeline Phases
    Text("MVP Schedule Roadmap", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)

    analysis.mvpRoadmap.forEach { phase ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(phase.phase, fontWeight = FontWeight.Black, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    SuggestionChip(onClick = {}, label = { Text(phase.duration, fontSize = 10.sp) })
                }
                Text(phase.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                phase.features.forEach { ft ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(ft, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }

    // Risks Assessment
    Text("Vulnerability & Risk Matrix", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)

    analysis.risks.forEach { risk ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(risk.risk, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = risk.severity,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (risk.severity.lowercase()) {
                            "high" -> Color.Red
                            "medium" -> Color(0xFFEF6C00)
                            else -> Color(0xFF2E7D32)
                        }
                    )
                }
                Text(paragraphHeader("Mitigation", risk.mitigation), fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun PitchDeckTabSection(analysis: StartupAnalysis) {
    var activeSlideIndex by remember { mutableStateOf(0) }
    val slideCount = analysis.pitchDeck.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .heightIn(260.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            val slide = analysis.pitchDeck.getOrNull(activeSlideIndex)
            if (slide != null) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "SLIDE ${slide.slideNumber} / $slideCount",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                        Icon(Icons.Default.SmartButton, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    }

                    Text(
                        slide.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        slide.bulletPoints.forEach { point ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    "•",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text = point,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Text("No slides generated", color = MaterialTheme.colorScheme.onPrimary)
            }

            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (activeSlideIndex > 0) activeSlideIndex-- },
                    enabled = activeSlideIndex > 0,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Previous slide")
                }

                Text(
                    "Switch Slides",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(
                    onClick = { if (activeSlideIndex < slideCount - 1) activeSlideIndex++ },
                    enabled = activeSlideIndex < slideCount - 1,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next slide")
                }
            }
        }
    }

    // Go to Market steps
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Recommended Go-To-Market Steps", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            analysis.goToMarketStrategy.forEachIndexed { idx, step ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${idx + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Text(step, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

private fun paragraphHeader(header: String, text: String): String {
    return "$header: $text"
}
