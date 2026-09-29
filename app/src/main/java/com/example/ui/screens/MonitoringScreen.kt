package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SensorReadingEntity
import com.example.data.models.SensorData
import com.example.ui.components.ChartMetric
import com.example.ui.components.InteractiveSensorChart
import com.example.ui.theme.AgriGreenLight
import com.example.ui.theme.WaterBlueLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MonitoringScreen(
    sensorData: SensorData,
    recentReadings: List<SensorReadingEntity>
) {
    var selectedMetric by remember { mutableStateOf(ChartMetric.MOISTURE) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Live Sensor Telemetry",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Real-time historical analytics & trends",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Live Stream Indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AgriGreenLight.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AgriGreenLight)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STREAMING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgriGreenLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Architectural note card: Clean separation of MockSensorService & ESP32SensorService
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Architecture",
                        tint = WaterBlueLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Architectural design: MockSensorService implements the standard SensorService contract. Once ESP32 hardware is connected, ESP32SensorService replaces it without changing UI code.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Interactive Chart
            InteractiveSensorChart(
                readings = recentReadings,
                selectedMetric = selectedMetric,
                onSelectMetric = { selectedMetric = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Live Telemetry Log Table
            Text(
                text = "Recent Telemetry Packets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("telemetry_log_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Table Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Timestamp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Moisture", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AgriGreenLight)
                        Text("Temp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF97316))
                        Text("Tank", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WaterBlueLight)
                        Text("Pump", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    val sampleRows = recentReadings.take(8)

                    sampleRows.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(timeFmt.format(Date(row.timestamp)), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${row.soilMoisture.toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AgriGreenLight)
                            Text("${row.temperature.toInt()}°C", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${row.waterLevel.toInt()}%", fontSize = 11.sp, color = WaterBlueLight)
                            Text(
                                text = if (row.pumpStatus) "ON" else "OFF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (row.pumpStatus) AgriGreenLight else Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
