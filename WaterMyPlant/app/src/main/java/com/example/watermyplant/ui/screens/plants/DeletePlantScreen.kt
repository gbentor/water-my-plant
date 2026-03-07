package com.example.watermyplant.ui.screens.plants

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeletePlantScreen(
    plantId: String,
    onDeleteClick: (String) -> Unit, // This will be called after successful deletion
    onBackClick: () -> Unit,
    viewModel: DeletePlantViewModel = hiltViewModel()
) {
    // We only need to observe the plant and the loading/error states
    val plant by viewModel.plant.collectAsState()
    val isLoading by remember { mutableStateOf(false) } // You might move this to ViewModel later
    val deleteSuccess by viewModel.deleteSuccess.collectAsState() // Assuming this tracks operation success

    LaunchedEffect(plantId) {
        viewModel.loadPlant(plantId)
    }

    // Navigate back or to the list once deletion is confirmed successful
    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            onDeleteClick(plantId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delete Plant") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center, // Center content for a cleaner "Dialog" feel
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Are you sure?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "You are about to delete ${plant?.name ?: "this plant"}. This action cannot be undone.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Delete Button
            Button(
                onClick = {
                    // Assuming you add a deletePlant method to your ViewModel
                    viewModel.deletePlant(plantId)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                enabled = !isLoading && plant != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onError)
                } else {
                    Text("Delete Plant")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cancel Button
            TextButton(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}