package com.example.frisko.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frisko.model.Measurement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataScreen(
    measurements: List<Measurement>,
    outputs: List<Measurement>,
    isLoading: Boolean,
    isConnected: Boolean,
    lastUpdate: String,
    connectionStatus: String?,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onClearStatus: () -> Unit
) {
    val refreshRotation by animateFloatAsState(
        targetValue = if (isLoading) 360f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ), label = "refreshRotation"
    )

    LaunchedEffect(connectionStatus) {
        if (connectionStatus != null) {
            kotlinx.coroutines.delay(5000)
            onClearStatus()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Frisko MR208-PC+",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (lastUpdate.isNotEmpty()) {
                        Text(
                            text = "Ostatni odczyt: $lastUpdate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Powrót"
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Odśwież",
                        modifier = Modifier.graphicsLayer(rotationZ = refreshRotation)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        // Status Banner
        connectionStatus?.let { status ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        status.contains("Błąd") || status.contains("Nie udało") -> MaterialTheme.colorScheme.errorContainer
                        status.contains("Połączono") -> MaterialTheme.colorScheme.primaryContainer
                        status.contains("zakończony pomyślnie") -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when {
                            status.contains("Błąd") || status.contains("Nie udało") -> Icons.Default.Error
                            status.contains("zakończony pomyślnie") -> Icons.Default.CheckCircle
                            else -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = when {
                            status.contains("Błąd") || status.contains("Nie udało") -> MaterialTheme.colorScheme.onErrorContainer
                            status.contains("Połączono") -> MaterialTheme.colorScheme.onPrimaryContainer
                            status.contains("zakończony pomyślnie") -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodyMedium,
                        color = when {
                            status.contains("Błąd") || status.contains("Nie udało") -> MaterialTheme.colorScheme.onErrorContainer
                            status.contains("Połączono") -> MaterialTheme.colorScheme.onPrimaryContainer
                            status.contains("zakończony pomyślnie") -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }

        // Loading Indicator
        if (isLoading) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Odczytywanie danych...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Data Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header for measurements
            item {
                SectionHeader(
                    title = "Parametry pomiarowe",
                    subtitle = "${measurements.size} pomiarów"
                )
            }

            // Measurements
            items(measurements) { measurement ->
                MeasurementCard(measurement = measurement)
            }

            // Header for outputs
            if (outputs.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader(
                        title = "Stany wyjść",
                        subtitle = "${outputs.size} wyjść"
                    )
                }

                // Outputs
                items(outputs) { output ->
                    OutputCard(output = output)
                }
            }

            // Refresh button at bottom
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Odczytaj",
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer(rotationZ = refreshRotation)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Odczytaj",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun MeasurementCard(measurement: Measurement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = measurement.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (measurement.unit.isNotEmpty()) {
                    Text(
                        text = "Jednostka: ${measurement.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Text(
                text = "${measurement.value}${if (measurement.unit.isNotEmpty() && measurement.value.toDoubleOrNull() != null) " ${measurement.unit}" else ""}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    measurement.value.contains("Błąd") -> MaterialTheme.colorScheme.error
                    measurement.value == "ZWARTE" -> Color(0xFF4CAF50)
                    measurement.value == "ROZWARTE" -> Color(0xFF9E9E9E)
                    else -> MaterialTheme.colorScheme.primary
                },
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun OutputCard(output: Measurement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (output.value) {
                "WŁĄCZONA" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                "WYŁĄCZONA" -> Color(0xFF9E9E9E).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = output.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (output.value) {
                        "WŁĄCZONA" -> Color(0xFF4CAF50)
                        "WYŁĄCZONA" -> Color(0xFF9E9E9E)
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            ) {
                Text(
                    text = output.value,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
