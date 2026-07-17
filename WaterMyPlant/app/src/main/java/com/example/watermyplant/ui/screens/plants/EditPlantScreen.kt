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
import com.example.watermyplant.data.model.Sensor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlantScreen(
    plantId: String,
    onEditClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: EditPlantViewModel = hiltViewModel()
) {
    val plant by viewModel.plant.collectAsState()
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val sensors by viewModel.sensors.collectAsState(initial = emptyList())
    var selectedSensor by remember { mutableStateOf<Sensor?>(null) }


    val editSuccess by viewModel.editSuccess.collectAsState()

    LaunchedEffect(plantId) {
        viewModel.loadPlant(plantId)
    }

    LaunchedEffect(viewModel.plant) {
        viewModel.plant.value?.let { plant ->
            name = plant.name
            type = plant.type
            description = plant.description ?: ""
        }
    }

    LaunchedEffect(editSuccess) {
        if (editSuccess) {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Plant") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(plant?.name ?: "Plant Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                label = { Text(plant?.type ?: "Plant Type") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(plant?.description ?: "Plant Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    // Display the sensorName or an empty string if nothing is selected
                    value = selectedSensor?.sensorName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Sensor") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    sensors.forEach { sensor ->
                        // Check if the sensor is already in use
                        val isUsed = !sensor.usedBy.isNullOrBlank()

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = sensor.sensorName,
                                        // If used, we dim the main text slightly
                                        color = if (isUsed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isUsed) {
                                        Text(
                                            text = " (${sensor.usedBy})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline // Grayish color
                                        )
                                    }
                                }
                            },
                            onClick = {
                                // Only allow selection if not used
                                if (!isUsed) {
                                    selectedSensor = sensor
                                    expanded = false
                                }
                            },
                            // This visually grays out the whole menu item and prevents interaction
                            enabled = !isUsed,
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Button(
                onClick = {
                    isLoading = true
                    error = null
                    viewModel.updatePlant(
                        plantId = plantId,
                        name = name.ifBlank { plant?.name ?: "" },
                        type = type.ifBlank { plant?.type ?: "" },
                        description = description.ifBlank { plant?.description }.takeIf { it?.isNotBlank() == true },
                        sensorName = selectedSensor?.sensorName
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && (name.isNotBlank() || type.isNotBlank() || description.isNotBlank() || selectedSensor != null)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Changes")
                }
            }
        }
    }
} 