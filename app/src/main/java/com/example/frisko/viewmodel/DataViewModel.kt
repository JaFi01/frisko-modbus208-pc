package com.example.frisko.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frisko.model.ConnectionSettings
import com.example.frisko.model.FriskoMeasurements
import com.example.frisko.networking.ModbusService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DataViewModel : ViewModel() {
    private val modbusService = ModbusService()
    
    private val _connectionSettings = MutableStateFlow(ConnectionSettings())
    val connectionSettings: StateFlow<ConnectionSettings> = _connectionSettings.asStateFlow()
    
    private val _measurements = MutableStateFlow(
        FriskoMeasurements(
            measurements = emptyList(),
            outputs = emptyList()
        )
    )
    val measurements: StateFlow<FriskoMeasurements> = _measurements.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _connectionStatus = MutableStateFlow<String?>(null)
    val connectionStatus: StateFlow<String?> = _connectionStatus.asStateFlow()

    fun updateConnectionSettings(host: String, port: Int) {
        _connectionSettings.value = ConnectionSettings(host, port)
    }

    fun connectAndReadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _connectionStatus.value = "Łączenie..."
            
            try {
                val settings = _connectionSettings.value
                val connected = modbusService.connect(settings.host, settings.port)
                
                if (connected) {
                    _connectionStatus.value = "Połączono - odczytywanie danych..."
                    val data = modbusService.readAllMeasurements()
                    _measurements.value = data
                    
                    if (data.error != null) {
                        _connectionStatus.value = "Błędy podczas odczytu: ${data.error}"
                    } else {
                        _connectionStatus.value = "Odczyt zakończony pomyślnie"
                    }
                } else {
                    _connectionStatus.value = "Nie udało się połączyć z urządzeniem"
                }
            } catch (e: Exception) {
                _connectionStatus.value = "Błąd: ${e.message}"
            } finally {
                _isLoading.value = false
                modbusService.disconnect()
            }
        }
    }

    fun clearStatus() {
        _connectionStatus.value = null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            modbusService.disconnect()
        }
    }
}