package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.StartupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionScreen(
    onNavigateBack: () -> Unit,
    onSubmit: (String, String, String, String, String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var startupName by remember { mutableStateOf("") }
    var idea by remember { mutableStateOf("") }
    var problem by remember { mutableStateOf("") }
    var industry by remember { mutableStateOf("Tech / SaaS") }
    var targetCustomers by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("United States") }
    var revenueModel by remember { mutableStateOf("SaaS Subscriptions") }
    var stage by remember { mutableStateOf("Concept / Pre-seed") }

    val isFormValid = startupName.isNotBlank() && idea.isNotBlank() && problem.isNotBlank() && targetCustomers.isNotBlank()

    // Lists of chips for ease of selection instead of confusing nested spinners
    val industriesList = listOf("Tech / SaaS", "Artificial Intelligence", "FinTech", "HealthTech", "E-Commerce", "Web3 / Crypto")
    val revenueModelsList = listOf("SaaS Subscriptions", "Marketplace Transaction Fee", "Freemium & Upsell", "Ad-supported", "Enterprise Contract")
    val stagesList = listOf("Concept / Pre-seed", "Prototype / MVP", "Post-Launch", "Seed Stage", "Growth / Venture")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Validate Startup Idea", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Details Category Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Basic Concept", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = startupName,
                        onValueChange = { startupName = it },
                        label = { Text("Startup Name") },
                        placeholder = { Text("e.g. Acme SaaS Group") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("startup_name_field"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors()
                    )

                    OutlinedTextField(
                        value = idea,
                        onValueChange = { idea = it },
                        label = { Text("One-Sentence Idea") },
                        placeholder = { Text("e.g. AI-driven logistics tracker delivering precise fleet schedules.") },
                        leadingIcon = { Icon(Icons.Default.Lightbulb, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("startup_idea_field"),
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors()
                    )

                    OutlinedTextField(
                        value = problem,
                        onValueChange = { problem = it },
                        label = { Text("Problem Being Solved") },
                        placeholder = { Text("e.g. Middle-mile container logistics suffering from fragmented messaging.") },
                        leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("startup_problem_field"),
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                }
            }

            // Target Audience Category Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Industry & Demographics", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = targetCustomers,
                        onValueChange = { targetCustomers = it },
                        label = { Text("Target Customers") },
                        placeholder = { Text("e.g. Logistics managers at midsize shipping agencies") },
                        leadingIcon = { Icon(Icons.Default.People, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("target_customers_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text("Target Country / Market") },
                        placeholder = { Text("e.g. Global, US, EU") },
                        leadingIcon = { Icon(Icons.Default.Public, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("target_country_field"),
                        singleLine = true
                    )
                }
            }

            // Chip Matrices Cards (Industry, Revenue, Stage)
            SelectionGroupPanel(
                title = "Vertical / Industry Focus",
                options = industriesList,
                selectedOption = industry,
                onSelected = { industry = it }
            )

            SelectionGroupPanel(
                title = "Preferred Revenue Stream",
                options = revenueModelsList,
                selectedOption = revenueModel,
                onSelected = { revenueModel = it }
            )

            SelectionGroupPanel(
                title = "Maturity Stage",
                options = stagesList,
                selectedOption = stage,
                onSelected = { stage = it }
            )

            // Submit Button
            Button(
                onClick = {
                    if (isFormValid) {
                        onSubmit(
                            startupName.trim(),
                            idea.trim(),
                            problem.trim(),
                            industry,
                            targetCustomers.trim(),
                            country.trim(),
                            revenueModel,
                            stage
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(vertical = 4.dp)
                    .testTag("submit_idea_button"),
                enabled = isFormValid,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(imageVector = Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyze Startup Idea", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectionGroupPanel(
    title: String,
    options: List<String>,
    selectedOption: String,
    onSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option == selectedOption
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelected(option) },
                        label = { Text(option, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}
