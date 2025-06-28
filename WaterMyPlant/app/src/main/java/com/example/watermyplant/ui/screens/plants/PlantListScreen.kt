package com.example.watermyplant.ui.screens.plants

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.watermyplant.data.model.PlantWithLastWatered
import com.example.watermyplant.ui.components.ErrorMessage
import com.example.watermyplant.ui.components.LoadingScreen
import com.example.watermyplant.util.DateUtils
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantListScreen(
    onPlantClick: (String) -> Unit,
    onAddPlantClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: PlantListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val error by viewModel.error.collectAsState(initial = null)

    LaunchedEffect(key1 = "load_plants") {
        viewModel.loadPlants()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Plants") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPlantClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Plant")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                is PlantListUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is PlantListUiState.Error -> {
                    Text(
                        text = error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                is PlantListUiState.Success -> {
                    val plants = (uiState as PlantListUiState.Success).plants
                    if (plants.isEmpty()) {
                        Text(
                            text = "No plants yet. Add your first plant!",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(plants) { plantWithLastWatered ->
                                PlantItem(
                                    plantWithLastWatered = plantWithLastWatered,
                                    onClick = { onPlantClick(plantWithLastWatered.plant.id.toString()) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlantItem(
    plantWithLastWatered: PlantWithLastWatered,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = plantWithLastWatered.plant.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = plantWithLastWatered.plant.type,
                style = MaterialTheme.typography.bodyMedium
            )
            if (!plantWithLastWatered.plant.description.isNullOrBlank()) {
                Text(
                    text = plantWithLastWatered.plant.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            val daysSinceLastWatered = plantWithLastWatered.lastWatered?.let { ChronoUnit.DAYS.between(it, Instant.now()) } ?: -1
            val daysText = if (daysSinceLastWatered >= 0) {
                "($daysSinceLastWatered days ago)"
            } else {
                "(Never)"
            }
            Text(
                text = "Last watered: ${DateUtils.formatDate(plantWithLastWatered.lastWatered)} $daysText",
                style = MaterialTheme.typography.bodySmall,
                color = if (daysSinceLastWatered > 14) Color.Red else Color.Unspecified
            )
        }
    }
}

private fun formatDate(instant: Instant): String {
    val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
} 