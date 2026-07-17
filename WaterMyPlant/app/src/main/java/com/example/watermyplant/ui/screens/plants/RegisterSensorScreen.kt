package com.example.watermyplant.ui.screens.plants

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.watermyplant.data.model.Board

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterSensorScreen(
    onSensorRegistered: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: RegisterSensorViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var sensorId by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    val sensorOptions = listOf("Moisture")
    var boardsExpanded by remember { mutableStateOf(false) }
    val boardsOptions by viewModel.boardsOptions.collectAsState(initial = emptyList())
    var selectedBoard by remember { mutableStateOf<Board?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val isGettingBoards by viewModel.isGettingBoards.collectAsState()
    val error by viewModel.error.collectAsState()
    val addSuccess by viewModel.addSuccess.collectAsState()

    var macAddress by remember { mutableStateOf("") }
    var boardName by remember { mutableStateOf("") }
    val boardError by viewModel.boardError.collectAsState()

    val scrollState = rememberScrollState()

    LaunchedEffect(isGettingBoards) {
        if (isGettingBoards) {
            viewModel.getRegisteredBoards()
        }
    }

    LaunchedEffect(addSuccess) {
        if (addSuccess) {
            onSensorRegistered()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register New Sensor") },
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Sensor Name") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            OutlinedTextField(
                value = sensorId,
                onValueChange = { sensorId = it },
                label = { Text("Sensor ID") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            ExposedDropdownMenuBox(
                expanded = boardsExpanded,
                onExpandedChange = { boardsExpanded = !boardsExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    // Display the sensorName or an empty string if nothing is selected
                    value = selectedBoard?.boardName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Board") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = boardsExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = boardsExpanded,
                    onDismissRequest = { boardsExpanded = false }
                ) {
                    boardsOptions.forEach { board ->
                        val isEmpty = board.macAddress == "null"

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = board.boardName,
                                        color = if (isEmpty) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isEmpty) {
                                        Text(
                                            text = "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline // Grayish color
                                        )
                                    }
                                }
                            },
                            onClick = {
                                if (!isEmpty) {
                                    selectedBoard = board
                                    boardsExpanded = false
                                }
                            },
                            enabled = !isEmpty,
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = type,
                    onValueChange = {}, // Read-only, so we leave this empty
                    readOnly = true,
                    label = { Text("Sensor Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor() // Important: anchors the menu to the text field
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    sensorOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                type = option
                                expanded = false
                            },
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
                    viewModel.registerSensor(
                        name = name,
                        sensorId = sensorId,
                        type = type,
                        boardMacAddress = selectedBoard?.macAddress ?: ""
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && name.isNotBlank() && type.isNotBlank() && selectedBoard != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Register Sensor")
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 4.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Text(text = "Register New Board")

            OutlinedTextField(
                value = macAddress,
                onValueChange = { macAddress = it },
                label = { Text("Mac Address") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            OutlinedTextField(
                value = boardName,
                onValueChange = { boardName = it },
                label = { Text("Board name") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            boardError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Button(
                onClick = {
                    viewModel.registerBoard(
                        macAddress = macAddress,
                        boardName = boardName
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && macAddress.isNotBlank() && boardName.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Register Board")
                }
            }
        }
    }
}