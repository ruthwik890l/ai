package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StartupReport
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    reports: List<StartupReport>,
    onSelectReport: (Long) -> Unit,
    onDeleteReport: (Long) -> Unit,
    onNewFormClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = reports.size
    val averageScore = if (reports.isNotEmpty()) {
        reports.map { it.validationScore }.average().toInt()
    } else 0

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "AI Startup Validator",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "SaaS Venture Validation Hub",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewFormClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .testTag("analyze_idea_fab")
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Validate New Idea",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Stats Panel
            item {
                StatsPanelSection(totalCount = totalCount, averageScore = averageScore)
            }

            // List Title Header
            item {
                Text(
                    text = "Validation Portfolio",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (reports.isEmpty()) {
                item {
                    EmptyPortfolioPlaceholder(onNewFormClick = onNewFormClick)
                }
            } else {
                items(reports, key = { it.id }) { report ->
                    StartupReportListItem(
                        report = report,
                        onClick = { onSelectReport(report.id) },
                        onDelete = { onDeleteReport(report.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatsPanelSection(totalCount: Int, averageScore: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .padding(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Analyzed Ideas",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = "$totalCount",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )
        }

        VerticalDivider(
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
            modifier = Modifier
                .height(48.dp)
                .width(1.dp)
        )

        Column {
            Text(
                "Avg Score",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = if (totalCount > 0) "$averageScore%" else "N/A",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )
        }

        Box(
            modifier = Modifier
                .size(54.dp)
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun EmptyPortfolioPlaceholder(onNewFormClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.BusinessCenter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No validation models found",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Submit your innovative business idea for multi-point market potential validation.",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Button(
                onClick = onNewFormClick,
                modifier = Modifier.testTag("empty_state_action_btn")
            ) {
                Text("Analyze First Idea")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StartupReportListItem(
    report: StartupReport,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateFlowOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Validation Report?") },
            text = { Text("Are you sure you want to delete the analysis report for '${report.startupName}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("report_card_${report.id}")
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDeleteConfirm = true }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Validation Score Arch indicator
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        if (report.analysisJson != null) {
                            val score = report.validationScore
                            when {
                                score >= 85 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                score >= 60 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                else -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                            }
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (report.analysisJson != null) {
                    Text(
                        text = "${report.validationScore}",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = when {
                            report.validationScore >= 85 -> MaterialTheme.colorScheme.primary
                            report.validationScore >= 60 -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Draft and Analyzing",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = report.startupName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = DateFormat.getDateInstance(DateFormat.SHORT).format(Date(report.timestamp)),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = report.idea,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Stage badge
                    SuggestionChip(
                        onClick = {},
                        label = { Text(report.stage, fontSize = 9.sp) },
                        modifier = Modifier.height(20.dp)
                    )
                    // Market Potential Badge if analyzed
                    if (report.analysisJson != null && report.marketPotential.isNotEmpty()) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Potential: ${report.marketPotential}", fontSize = 9.sp) },
                            modifier = Modifier.height(20.dp)
                        )
                    } else if (report.analysisJson == null) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Failed Model", fontSize = 9.sp) },
                            modifier = Modifier.height(20.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                labelColor = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }
            }

            // Quick delete action
            IconButton(
                onClick = { showDeleteConfirm = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete from history",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// Helper function to build stateFlow inside Composable easily
fun <T> mutableStateFlowOf(value: T): MutableState<T> = mutableStateOf(value)
