package com.example.watermyplant.ui.screens.plants

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.watermyplant.data.model.WateringEvent
import com.example.watermyplant.ui.components.ConfirmationDialog
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
fun PlantDetailScreen(
    plantId: String,
    onEditClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: PlantDetailViewModel = hiltViewModel()
) {
    val plant by viewModel.plant.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val waterSuccess by viewModel.waterSuccess.collectAsState()
    val wateringEvents by viewModel.wateringEvents.collectAsState()
    var useFertilizer by remember { mutableStateOf(false) }
    var wateringNotes by remember { mutableStateOf("") }
    var editingEvent by remember { mutableStateOf<WateringEvent?>(null) }
    var editingFertilizer by remember { mutableStateOf(false) }
    var editingNotes by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<WateringEvent?>(null) }
    val context = LocalContext.current

    LaunchedEffect(plantId) {
        viewModel.loadPlant(plantId)
    }

    LaunchedEffect(waterSuccess) {
        if (waterSuccess) {
            viewModel.clearWaterSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plant?.name ?: "Plant Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { plant?.id?.let { onEditClick(it.toString()) } }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Plant")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Text(
                        text = error ?: "An error occurred",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                plant != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        DetailItem("Type", plant?.type ?: "")
                        DetailItem("Description", plant?.description ?: "No description")
                        val lastWatered = wateringEvents.maxByOrNull { it.wateredAt }?.wateredAt

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Watering History", style = MaterialTheme.typography.titleMedium)
                        if (wateringEvents.isEmpty()) {
                            Text("No watering events yet.", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                val sortedEvents = wateringEvents.sortedByDescending { it.wateredAt }
                                itemsIndexed(sortedEvents) { index, event ->
                                    val deltaText = if (index < sortedEvents.lastIndex) {
                                        val prev = sortedEvents[index + 1]
                                        val days = ChronoUnit.DAYS.between(prev.wateredAt, event.wateredAt)
                                        if (days > 0) "($days days)" else ""
                                    } else {
                                        "First event"
                                    }
                                    WateringEventItem(
                                        event = event,
                                        onEdit = {
                                            editingEvent = it
                                            editingFertilizer = it.fertilizerUsed
                                            editingNotes = it.notes ?: ""
                                        },
                                        onDelete = {
                                            showDeleteDialog = it
                                        },
                                        deltaText = deltaText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = useFertilizer,
                                onCheckedChange = { useFertilizer = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fertilizer used?")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = wateringNotes,
                            onValueChange = { wateringNotes = it },
                            label = { Text("Notes (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.waterPlant(plantId, useFertilizer, wateringNotes.takeIf { it.isNotBlank() }) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.WaterDrop,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Water Plant")
                        }
                    }
                }
            }
        }
        // Edit dialog
        if (editingEvent != null) {
            AlertDialog(
                onDismissRequest = { editingEvent = null },
                title = { Text("Edit Watering Event") },
                text = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = editingFertilizer,
                                onCheckedChange = { editingFertilizer = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fertilizer used?")
                        }
                        OutlinedTextField(
                            value = editingNotes,
                            onValueChange = { editingNotes = it },
                            label = { Text("Notes (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        editingEvent?.let {
                            viewModel.editWateringEvent(
                                eventId = it.id.toString(),
                                fertilizerUsed = editingFertilizer,
                                notes = editingNotes.takeIf { n -> n.isNotBlank() }
                            )
                        }
                        editingEvent = null
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingEvent = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
        // Delete dialog
        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete Watering Event") },
                text = { Text("Are you sure you want to delete this watering event?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog?.let {
                            viewModel.deleteWateringEvent(it.id.toString())
                        }
                        showDeleteDialog = null
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun WateringEventItem(event: WateringEvent, onEdit: (WateringEvent) -> Unit, onDelete: (WateringEvent) -> Unit, deltaText: String) {
    val backgroundColor = if (event.fertilizerUsed)
        Color(0xFFD0F5E8) // Light green
    else
        Color(0xFFD6E6FA) // Light blue
    val borderColor = if (event.fertilizerUsed)
        Color(0xFF4CAF50) // Green
    else
        Color(0xFF2196F3) // Blue

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp, horizontal = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .background(backgroundColor, shape = RoundedCornerShape(16.dp))
                .border(
                    width = 4.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = "Watered",
                tint = borderColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = DateUtils.formatDate(event.wateredAt) + " " + deltaText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
                if (!event.notes.isNullOrBlank()) {
                    Text(
                        text = "Notes: ${event.notes}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                }
                if (event.fertilizerUsed) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = "Fertilizer used",
                            tint = Color(0xFF388E3C),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Fertilizer used",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF388E3C)
                        )
                    }
                }
            }
            IconButton(onClick = { onEdit(event) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit event", tint = Color.Black)
            }
            IconButton(onClick = { onDelete(event) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete event", tint = Color.Black)
            }
        }
    }
} 