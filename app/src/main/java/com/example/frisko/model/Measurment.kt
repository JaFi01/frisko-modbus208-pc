package com.example.frisko.model

data class Measurement(
    val name: String,
    val displayName: String,
    val value: String,
    val unit: String,
    val register: Int,
    val divisor: Int = 1
)

data class ConnectionSettings(
    val host: String = "192.168.1.99",
    val port: Int = 502
)

data class FriskoMeasurements(
    val measurements: List<Measurement>,
    val outputs: List<Measurement>,
    val isConnected: Boolean = false,
    val lastUpdate: String = "",
    val error: String? = null
)
