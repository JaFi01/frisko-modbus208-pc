package com.example.frisko.networking

import com.example.frisko.model.Measurement
import com.example.frisko.model.FriskoMeasurements
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

class ModbusService {
    private var socket: Socket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var transactionId = 0

    companion object {
        // Mapa parametrów pomiarowych z dokumentacji
        val MEASUREMENT_MAP = mapOf(
            4248 to Measurement("temperatura_zewnetrzna", "Temperatura zewnętrzna", "", "°C", 4248, 10),
            4154 to Measurement("temperatura_srednia", "Temperatura średnia", "", "°C", 4154, 10),
            4065 to Measurement("temperatura_wewnetrzna", "Temperatura wewnętrzna", "", "°C", 4065, 10),
            //4243 to Measurement("zadana_s", "Zadana S", "", "°C", 4243, 10),
            4150 to Measurement("zadana_temperatura_wewnetrzna", "Zadana temperatura wewnętrzna", "", "°C", 4150, 10),
            4069 to Measurement("temperatura_cwu_gora", "Temperatura CWU góra", "", "°C", 4069, 10),
            //4068 to Measurement("temperatura_cwu_dol", "Temperatura CWU dół", "", "°C", 4068, 10),
            4121 to Measurement("zadana_temperatura_cwu", "Zadana temperatura CWU", "", "°C", 4121, 10),
            //4066 to Measurement("temperatura_podlogi", "Temperatura podłogi", "", "°C", 4066, 10),
            4067 to Measurement("temperatura_bufora", "Temperatura bufora", "", "°C", 4067, 10),
            4158 to Measurement("zadana_temperatura_bufora", "Zadana temperatura bufora", "", "°C", 4158, 10),
            4072 to Measurement("temperatura_zasilania_pc", "Temperatura zasilania PC", "", "°C", 4072, 10),
            4071 to Measurement("temperatura_wejsciowa_dz", "Temperatura wejściowa DZ", "", "°C", 4071, 10),
            4070 to Measurement("temperatura_wyjsciowa_dz", "Temperatura wyjściowa DZ", "", "°C", 4070, 10),
            //4197 to Measurement("temperatura_srednia_dz", "Temperatura średnia DZ", "", "°C", 4197, 10),
            //4073 to Measurement("wejscie_presostat", "Wejście presostat", "", "", 4073, 1),
            4236 to Measurement("wejscie_ferie", "Wejście ferie", "", "", 4236, 1),
            4237 to Measurement("wejscie_party", "Wejście party", "", "", 4237, 1)
        )

        val OUTPUT_MAP = mapOf(
            4216 to Measurement("pompa_dziennestrefa", "Pompa dzienne strefa", "", "", 4216, 1),
            4214 to Measurement("pompa_cwu", "Pompa CWU", "", "", 4214, 1),
            4100 to Measurement("silownik_zaworu", "Siłownik zaworu", "", "", 4100, 1),
            4232 to Measurement("bzc_cwu", "BZC CWU", "", "", 4232, 1),
            4231 to Measurement("bzc_co", "BZC CO", "", "", 4231, 1),
            4215 to Measurement("pompa_co", "Pompa CO", "", "", 4215, 1),
            4101 to Measurement("sprezarka", "Sprężarka", "", "", 4101, 1),
            4213 to Measurement("pompa_ccw", "Pompa CCW", "", "", 4213, 1),
            4099 to Measurement("alarm", "Alarm", "", "", 4099, 1)
        )
    }

    suspend fun connect(host: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            socket?.close()
            socket = Socket(host, port).apply {
                soTimeout = 5000 // 5 sekund timeout
            }
            inputStream = socket?.getInputStream()
            outputStream = socket?.getOutputStream()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            inputStream?.close()
            outputStream?.close()
            socket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream = null
            outputStream = null
            socket = null
        }
    }

    private fun createModbusRequest(startAddress: Int, count: Int, unitId: Int = 1): ByteArray {
        transactionId++
        val request = ByteBuffer.allocate(12)
        
        // MBAP Header
        request.putShort(transactionId.toShort()) // Transaction ID
        request.putShort(0) // Protocol ID (0 for Modbus)
        request.putShort(6) // Length (6 bytes following)
        request.put(unitId.toByte()) // Unit ID
        
        // PDU
        request.put(0x03.toByte()) // Function code (Read Holding Registers)
        request.putShort(startAddress.toShort()) // Starting address
        request.putShort(count.toShort()) // Quantity
        
        return request.array()
    }

    private suspend fun readHoldingRegister(address: Int, unitId: Int = 1): Int? = withContext(Dispatchers.IO) {
        try {
            val request = createModbusRequest(address, 1, unitId)
            outputStream?.write(request)
            outputStream?.flush()

            val response = ByteArray(11) // MBAP (7) + Function (1) + Byte count (1) + Data (2)
            var totalRead = 0
            while (totalRead < response.size) {
                val read = inputStream?.read(response, totalRead, response.size - totalRead) ?: -1
                if (read == -1) break
                totalRead += read
            }

            if (totalRead >= 11) {
                val buffer = ByteBuffer.wrap(response)
                buffer.position(7) // Skip MBAP header
                val functionCode = buffer.get()
                if (functionCode == 0x03.toByte()) {
                    val byteCount = buffer.get()
                    if (byteCount >= 2) {
                        // signed short (-32768 to 32767)
                        return@withContext buffer.getShort().toInt()
                    }
                }
            }
            null
        } catch (e: SocketTimeoutException) {
            null
        } catch (e: IOException) {
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun readAllMeasurements(): FriskoMeasurements = withContext(Dispatchers.IO) {
        val measurements = mutableListOf<Measurement>()
        val outputs = mutableListOf<Measurement>()
        var hasError = false
        val errors = mutableListOf<String>()

        try {
            // Odczyt parametrów pomiarowych
            for ((register, template) in MEASUREMENT_MAP) {
                try {
                    val rawValue = readHoldingRegister(register)
                    if (rawValue != null) {
                        val actualValue = if (template.divisor == 10) {
                            String.format("%.1f", rawValue / 10.0)
                        } else {
                            if (rawValue == 1) "ZWARTE" else "ROZWARTE"
                        }
                        measurements.add(template.copy(value = actualValue))
                    } else {
                        measurements.add(template.copy(value = "Błąd odczytu"))
                        hasError = true
                    }
                    // Krótka pauza między odczytami
                    kotlinx.coroutines.delay(50)
                } catch (e: Exception) {
                    measurements.add(template.copy(value = "Błąd: ${e.message}"))
                    hasError = true
                }
            }

            // Odczyt stanu wyjść
            for ((register, template) in OUTPUT_MAP) {
                try {
                    val rawValue = readHoldingRegister(register)
                    if (rawValue != null) {
                        val status = if (rawValue == 100) "WŁĄCZONA" else "WYŁĄCZONA"
                        outputs.add(template.copy(value = status))
                    } else {
                        outputs.add(template.copy(value = "Błąd odczytu"))
                        hasError = true
                    }
                    kotlinx.coroutines.delay(50)
                } catch (e: Exception) {
                    outputs.add(template.copy(value = "Błąd: ${e.message}"))
                    hasError = true
                }
            }

        } catch (e: Exception) {
            errors.add("Błąd komunikacji: ${e.message}")
            hasError = true
        }

        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        
        FriskoMeasurements(
            measurements = measurements,
            outputs = outputs,
            isConnected = socket?.isConnected == true,
            lastUpdate = currentTime,
            error = if (hasError && errors.isNotEmpty()) errors.joinToString(", ") else null
        )
    }
}