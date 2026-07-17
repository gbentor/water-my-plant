package com.example.watermyplant.ui.screens.plants

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.watermyplant.util.DateUtils
import java.time.temporal.ChronoUnit
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plantId: String,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: PlantDetailViewModel = hiltViewModel()
) {
    val plant by viewModel.plant.collectAsState()
    val sensor by viewModel.sensor.collectAsState()
    val moistureData by viewModel.moistureData.collectAsState()
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
    val allMoistureValues = moistureData?.moisture ?: listOf()
    val currentMoisture: Double = moistureData?.moisture?.lastOrNull() ?: 0.0


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
                    };
                    IconButton(onClick = { plant?.id?.let { onDeleteClick(it.toString()) } }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Plant")
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
                            .padding(horizontal = 16.dp, vertical = 1.dp), // Reduced vertical padding
                        verticalArrangement = Arrangement.spacedBy(1.dp) // Global tight spacing
                    ) {
                        DetailItem("Type", plant?.type ?: "")
                        DetailItem("Description", plant?.description ?: "No description")
                        DetailItem(
                            label = "Sensor",
                            value = sensor?.let { "${it.sensorName} (${it.sensorType})" } ?: "No sensors used",
                            trailingContent = {
                                if (sensor != null) {
                                    // Determine color based on moisture level
                                    val statusColor = when {
                                        currentMoisture < 30.0 -> Color(0xFFE57373) // Thirsty (Red)
                                        currentMoisture > 80.0 -> Color(0xFF64B5F6) // Overwatered (Light Blue)
                                        else -> Color(0xFF81C784) // Happy (Green)
                                    }

                                    Surface(
                                        color = statusColor.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, statusColor)
                                    ) {
                                        Text(
                                            text = "$currentMoisture%",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = statusColor
                                        )
                                    }
                                }
                            }
                        )

                        if (allMoistureValues.isNotEmpty() && sensor != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                MoistureGraph(
                                    data = allMoistureValues,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }

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

                        OutlinedTextField(
                            value = wateringNotes,
                            onValueChange = { wateringNotes = it },
                            label = { Text("Notes (optional)", style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 18.dp, max = 52.dp), // Restricts the height to be more compact
                            textStyle = MaterialTheme.typography.bodyMedium,
                            singleLine = true // Keeps it to one line to save vertical space
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp) // Adds a gap between buttons
                        ) {
                            // Regular Water Button
                            Button(
                                onClick = { viewModel.waterPlant(plantId, false, wateringNotes.takeIf { it.isNotBlank() }) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Water", maxLines = 1)
                            }

                            // Fertilizer Water Button
                            Button(
                                onClick = { viewModel.waterPlant(plantId, true, wateringNotes.takeIf { it.isNotBlank() }) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50) // Green to signify fertilizer/growth
                                ),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Icon(Icons.Default.Eco, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Water Wih Fertilizer", maxLines = 1)
                            }
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
                .padding(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 4.dp),
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


@Composable
fun MoistureGraph(
    data: List<Double>,
    modifier: Modifier = Modifier,
    labelsX: List<String>? = null, // Now optional
    graphColor: Color = Color(0xFF2196F3)
) {
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    val labelStyle = MaterialTheme.typography.labelSmall
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    // Define the colors for the gradient (Top to Bottom)
    val moistureColors = listOf(
        Color(0xFF4CAF50), // Green (100%)
        Color(0xFFFFEB3B), // Yellow (50%)
        Color(0xFFF44336)  // Red (0%)
    )

    Column(modifier = modifier) {
        Row(modifier = Modifier.height(80.dp)) {
            // Y-Axis Labels
            Column(
                modifier = Modifier.fillMaxHeight().padding(end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("100", style = labelStyle, color = labelColor)
                Text("50", style = labelStyle, color = labelColor)
                Text("0", style = labelStyle, color = labelColor)
            }

            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val spacing = width / (data.size - 1).coerceAtLeast(1)

                    // Vertical Gradient Brush for the line and fill
                    val brush = Brush.verticalGradient(
                        colors = moistureColors,
                        startY = 0f,
                        endY = height
                    )

                    // 1. Draw Grid
                    for (i in 0..2) {
                        val y = height * i / 2
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    if (data.isNotEmpty()) {
                        val strokePath = Path().apply {
                            data.forEachIndexed { index, value ->
                                val x = index * spacing
                                val y = height - (value / 100f * height)
                                if (index == 0) moveTo(x, y.toFloat()) else lineTo(x, y.toFloat())
                            }
                        }

                        // 2. Draw the Fill (Using the same gradient but very transparent)
                        val fillPath = Path().apply {
                            addPath(strokePath)
                            lineTo(width, height)
                            lineTo(0f, height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = moistureColors.map { it.copy(alpha = 0.15f) },
                                startY = 0f,
                                endY = height
                            )
                        )

                        // 3. Draw the Line (Using the gradient brush)
                        drawPath(
                            path = strokePath,
                            brush = brush,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }

        // 3. Optional X-Axis Labels (Only shows if not null)
        labelsX?.let {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, top = 4.dp), // Start padding matches Y-axis width
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                it.forEach { label ->
                    Text(text = label, style = labelStyle, color = labelColor)
                }
            }
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    trailingContent: @Composable (RowScope.() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f) // Takes up available space
            )
            // If we provided extra content (like a badge), show it here
            trailingContent?.invoke(this)
        }
    }
}