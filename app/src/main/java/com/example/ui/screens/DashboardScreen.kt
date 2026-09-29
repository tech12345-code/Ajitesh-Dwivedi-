package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DecisionEntity
import com.example.data.models.DemoScenario
import com.example.data.models.FarmProfile
import com.example.data.models.IrrigationControlState
import com.example.data.models.MoistureStatus
import com.example.data.models.SensorData
import com.example.data.models.TankStatus
import com.example.data.models.WeatherState
import com.example.ui.components.SensorCard
import com.example.ui.components.UndergroundPipeVisualizer
import com.example.ui.theme.AgriGreenLight
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.SoilAmber
import com.example.ui.theme.StatusAlert
import com.example.ui.theme.WaterBlueLight
import com.example.ui.theme.WaterBluePrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    sensorData: SensorData,
    weatherState: WeatherState,
    controlState: IrrigationControlState,
    farmProfile: FarmProfile,
    activeScenario: DemoScenario,
    unreadAlertCount: Int,
    recentDecisions: List<DecisionEntity>,
    onOpenSimulationDialog: () -> Unit,
    onRequestPumpToggle: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToMonitoring: () -> Unit,
    onNavigateToIrrigation: () -> Unit,
    onNavigateToDecisions: () -> Unit
) {
    val moistureColor = when (sensorData.moistureStatus) {
        MoistureStatus.OPTIMAL -> AgriGreenLight
        MoistureStatus.DRY -> SoilAmber
        MoistureStatus.WET -> WaterBlueLight
    }

    val tankColor = when (sensorData.tankStatus) {
        TankStatus.NORMAL -> WaterBlueLight
        TankStatus.LOW -> SoilAmber
        TankStatus.CRITICAL_LOW -> StatusAlert
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Bar with Greeting, Lab Button, and Alert Bell
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good morning, Farmer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (sensorData.moistureStatus == MoistureStatus.OPTIMAL) "Your farm is healthy today." else "Attention needed in root zone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AgriGreenLight
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Simulation Lab Trigger Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onOpenSimulationDialog() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("simulation_lab_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = "Sim Lab",
                                tint = AgriGreenLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sim Lab",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Notifications Bell
                    IconButton(
                        onClick = onNavigateToAlerts,
                        modifier = Modifier.testTag("alerts_bell_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadAlertCount > 0) {
                                    Badge(containerColor = StatusAlert) {
                                        Text("$unreadAlertCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alerts",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Weather & Health Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = WaterBlueLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${weatherState.condition} • ${weatherState.temperature.toInt()}°C",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Rain Probability: ${weatherState.rainProbability}%",
                            fontSize = 11.sp,
                            color = if (weatherState.rainProbability > 60) WaterBlueLight else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Farm Health Score Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AgriGreenPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "94%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AgriGreenLight
                            )
                            Text(
                                text = "Farm Health",
                                fontSize = 9.sp,
                                color = AgriGreenLight
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active Scenario Banner
            if (activeScenario != DemoScenario.NORMAL) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SoilAmber.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SoilAmber.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🧪 ", fontSize = 14.sp)
                            Text(
                                text = "Active Scenario: ${activeScenario.title}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoilAmber
                            )
                        }
                        Text(
                            text = "Change",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WaterBlueLight,
                            modifier = Modifier.clickable { onOpenSimulationDialog() }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Pump Quick Status & Manual Toggle Row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pump_control_card"),
                colors = CardDefaults.cardColors(
                    containerColor = if (controlState.isPumpOn)
                        WaterBluePrimary.copy(alpha = 0.2f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(18.dp),
                border = if (controlState.isPumpOn)
                    androidx.compose.foundation.BorderStroke(1.5.dp, WaterBlueLight)
                else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (controlState.isPumpOn) WaterBluePrimary else MaterialTheme.colorScheme.surface
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Pump Status",
                                tint = if (controlState.isPumpOn) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Irrigation Pump: ${if (controlState.isPumpOn) "ON" else "OFF"}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (controlState.isPumpOn) WaterBlueLight else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (controlState.isPumpOn)
                                    "Flow: ${sensorData.waterFlowRateLpm.toInt()} L/min • Root delivery"
                                else
                                    "Mode: ${controlState.mode.name} • Threshold: ${controlState.moistureThreshold.toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = onRequestPumpToggle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (controlState.isPumpOn) StatusAlert else AgriGreenPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("dashboard_pump_toggle_button")
                    ) {
                        Text(
                            text = if (controlState.isPumpOn) "STOP" else "START",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Section: Real-time Telemetry Sensor Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Sensor Readings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "View Analytics →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AgriGreenLight,
                    modifier = Modifier.clickable { onNavigateToMonitoring() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1. Soil Moisture Card
            SensorCard(
                title = "Soil Moisture",
                value = "${sensorData.soilMoisture.toInt()}",
                unit = "%",
                icon = Icons.Default.WaterDrop,
                statusText = sensorData.moistureStatus.label,
                statusColor = moistureColor,
                minThreshold = "${controlState.moistureThreshold.toInt()}%",
                idealRange = "50% - 75%",
                progress = sensorData.soilMoisture / 100f,
                progressColor = moistureColor,
                trendUp = controlState.isPumpOn,
                trendText = if (controlState.isPumpOn) "+1.2%/min (Irrigating)" else "-0.1%/min (Uptake)",
                onClick = onNavigateToMonitoring
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Temperature & Humidity Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SensorCard(
                    title = "Temperature",
                    value = "${sensorData.temperature.toInt()}",
                    unit = "°C",
                    icon = Icons.Default.DeviceThermostat,
                    statusText = if (sensorData.temperature > 34) "Heat Stress" else "Optimal",
                    statusColor = if (sensorData.temperature > 34) StatusAlert else AgriGreenLight,
                    progress = (sensorData.temperature / 50f).coerceIn(0f, 1f),
                    progressColor = if (sensorData.temperature > 34) StatusAlert else AgriGreenLight,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMonitoring
                )

                SensorCard(
                    title = "Humidity",
                    value = "${sensorData.humidity.toInt()}",
                    unit = "%",
                    icon = Icons.Default.Cloud,
                    statusText = if (sensorData.humidity > 70) "Humid" else "Normal",
                    statusColor = WaterBlueLight,
                    progress = sensorData.humidity / 100f,
                    progressColor = WaterBlueLight,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMonitoring
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Light Intensity & Water Tank Level Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SensorCard(
                    title = "Light Intensity",
                    value = "${sensorData.lightIntensity.toInt()}",
                    unit = "%",
                    icon = Icons.Default.LightMode,
                    statusText = if (sensorData.lightIntensity > 70) "High Sun" else "Diffuse",
                    statusColor = Color(0xFFFBBF24),
                    progress = sensorData.lightIntensity / 100f,
                    progressColor = Color(0xFFFBBF24),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMonitoring
                )

                SensorCard(
                    title = "Water Tank",
                    value = "${sensorData.waterTankLevel.toInt()}",
                    unit = "%",
                    icon = Icons.Default.Water,
                    statusText = sensorData.tankStatus.label,
                    statusColor = tankColor,
                    minThreshold = "20%",
                    progress = sensorData.waterTankLevel / 100f,
                    progressColor = tankColor,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToIrrigation
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Underground Root Zone Irrigation Visualizer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Root-Zone Conduits",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Configure →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AgriGreenLight,
                    modifier = Modifier.clickable { onNavigateToIrrigation() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            UndergroundPipeVisualizer(
                isPumpActive = controlState.isPumpOn,
                soilMoisture = sensorData.soilMoisture,
                waterTankLevel = sensorData.waterTankLevel,
                flowRateLpm = sensorData.waterFlowRateLpm
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Recent Decisions Preview Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Smart Decisions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Full History →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AgriGreenLight,
                    modifier = Modifier.clickable { onNavigateToDecisions() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (recentDecisions.isNotEmpty()) {
                val latest = recentDecisions.first()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDecisions() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = latest.ruleType, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = latest.reason,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = latest.action,
                                style = MaterialTheme.typography.bodySmall,
                                color = AgriGreenLight
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
