package com.example.frisko.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionScreen(
    defaultHost: String,
    defaultPort: Int,
    onConnect: (String, Int) -> Unit,
    onNavigateToData: () -> Unit
) {
    var host by remember { mutableStateOf(defaultHost) }
    var port by remember { mutableStateOf(defaultPort.toString()) }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ustawienia",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Frisko MR208-PC+",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Konfiguracja połączenia MODBUS TCP",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Connection Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Ustawienia połączenia",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                // IP Address Field
                OutlinedTextField(
                    value = host,
                    onValueChange = { 
                        host = it
                        showError = false
                    },
                    label = { Text("Adres IP urządzenia") },
                    placeholder = { Text("192.168.1.99") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "IP"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = showError,
                    supportingText = if (showError) {
                        { Text("Wprowadź poprawny adres IP") }
                    } else null
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Port Field
                OutlinedTextField(
                    value = port,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() }) {
                            port = it
                            showError = false
                        }
                    },
                    label = { Text("Port MODBUS TCP") },
                    placeholder = { Text("502") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = showError,
                    supportingText = if (showError) {
                        { Text("Port musi być liczbą") }
                    } else null
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Info Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "ℹ️ Informacje",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Port MODBUS TCP: domyślnie 502\n" +
                                   "• Upewnij się, że urządzenie jest dostępne w sieci\n" +
                                   "• Aplikacja odczytuje parametry pomiarowe i stany wyjść",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Connect Button
        Button(
            onClick = {
                val portInt = port.toIntOrNull()
                if (host.isNotBlank() && portInt != null && portInt > 0) {
                    onConnect(host, portInt)
                    onNavigateToData()
                } else {
                    showError = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "Połącz",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Połącz i przejdź do danych",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
