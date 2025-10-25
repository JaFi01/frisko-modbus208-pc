package com.example.frisko

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.frisko.ui.screens.ConnectionScreen
import com.example.frisko.ui.screens.DataScreen
import com.example.frisko.ui.theme.FriskoTheme
import com.example.frisko.viewmodel.DataViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FriskoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FriskoApp()
                }
            }
        }
    }
}

@Composable
fun FriskoApp() {
    val navController = rememberNavController()
    val viewModel: DataViewModel = viewModel()
    
    val connectionSettings by viewModel.connectionSettings.collectAsState()
    val measurements by viewModel.measurements.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "connection"
    ) {
        composable("connection") {
            ConnectionScreen(
                defaultHost = connectionSettings.host,
                defaultPort = connectionSettings.port,
                onConnect = { host, port ->
                    viewModel.updateConnectionSettings(host, port)
                },
                onNavigateToData = {
                    navController.navigate("data")
                }
            )
        }
        
        composable("data") {
            DataScreen(
                measurements = measurements.measurements,
                outputs = measurements.outputs,
                isLoading = isLoading,
                isConnected = measurements.isConnected,
                lastUpdate = measurements.lastUpdate,
                connectionStatus = connectionStatus,
                onRefresh = {
                    viewModel.connectAndReadData()
                },
                onBack = {
                    navController.popBackStack()
                },
                onClearStatus = {
                    viewModel.clearStatus()
                }
            )
        }
    }
}