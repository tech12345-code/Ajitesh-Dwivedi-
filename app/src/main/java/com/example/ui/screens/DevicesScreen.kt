package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Esp32DeviceState
import com.example.data.models.Esp32SensorPayload
import com.example.data.models.SensorData
import com.example.ui.theme.AgriGreenLight
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.SoilAmber
import com.example.ui.theme.StatusAlert
import com.example.ui.theme.WaterBlueLight
import java.util.Locale

@Composable
fun DevicesScreen(
    esp32State: Esp32DeviceState,
    sensorData: SensorData,
    onSendTestPayload: (Esp32SensorPayload) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Header
            Text(
                text = "Device Management",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "ESP32 Controller & Connected Sensors",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ESP32 Main Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("esp32_device_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (sensorData.isSensorOnline) AgriGreenLight.copy(alpha = 0.5f) else StatusAlert
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AgriGreenPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = AgriGreenLight,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = esp32State.deviceId,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = esp32State.hardwareModel,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Online/Offline Status Indicator
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (sensorData.isSensorOnline) AgriGreenLight.copy(alpha = 0.15f) else StatusAlert.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (sensorData.isSensorOnline) AgriGreenLight else StatusAlert)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (sensorData.isSensorOnline) "ONLINE" else "DISCONNECTED",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sensorData.isSensorOnline) AgriGreenLight else StatusAlert
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Controller Telemetry Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DeviceMiniStat("Heartbeat", if (sensorData.isSensorOnline) "2s ago" else "Offline", Icons.Default.Sensors)
                        DeviceMiniStat("IP Address", esp32State.ipAddress, Icons.Default.Wifi)
                        DeviceMiniStat("Solar Battery", "${esp32State.batterySolarPct}%", Icons.Default.SolarPower)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Connected Hardware Sensors & Actuators
            Text(
                text = "Connected Sensors & Pinout",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            esp32State.connectedSensors.forEach { sensor ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = sensor.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${sensor.pin} • ${sensor.protocol}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active",
                            tint = if (sensorData.isSensorOnline) AgriGreenLight else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ESP32 REST API Payload Inspector (Meets user requirement #11 & #12)
            Text(
                text = "Hardware Ingestion JSON Payload",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Physical ESP32 firmware POSTs telemetry to endpoint /api/sensors/data",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            val livePayloadJson = """
{
  "deviceId": "${esp32State.deviceId}",
  "soilMoisture": ${String.format(Locale.US, "%.1f", sensorData.soilMoisture)},
  "temperature": ${String.format(Locale.US, "%.1f", sensorData.temperature)},
  "humidity": ${String.format(Locale.US, "%.1f", sensorData.humidity)},
  "light": ${String.format(Locale.US, "%.1f", sensorData.lightIntensity)},
  "waterLevel": ${String.format(Locale.US, "%.1f", sensorData.waterTankLevel)}
}
            """.trimIndent()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("esp32_payload_box"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF09120E)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3529))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "POST /api/sensors/data HTTP/1.1",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WaterBlueLight,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = livePayloadJson,
                        fontSize = 12.sp,
                        color = Color(0xFFA7F3D0),
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Simulated ESP32 POST Injector Button
            Button(
                onClick = {
                    onSendTestPayload(
                        Esp32SensorPayload(
                            deviceId = "ESP32-001-FARM",
                            soilMoisture = 34.0f,
                            temperature = 29.8f,
                            humidity = 58.0f,
                            light = 74.0f,
                            waterLevel = 76.0f
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("send_test_packet_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AgriGreenLight
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test Ingest Simulated ESP32 Packet", color = AgriGreenLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DeviceMiniStat(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = AgriGreenLight, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
