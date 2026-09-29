package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.IrrigationControlState
import com.example.data.models.IrrigationMode
import com.example.data.models.SensorData
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
fun IrrigationScreen(
    sensorData: SensorData,
    controlState: IrrigationControlState,
    showConfirmDialog: Boolean,
    onSetMode: (IrrigationMode) -> Unit,
    onRequestPumpToggle: () -> Unit,
    onConfirmPumpStart: () -> Unit,
    onDismissConfirmDialog: () -> Unit,
    onSetMoistureThreshold: (Float) -> Unit,
    onRefillTank: () -> Unit
) {
    val timeFormatter = SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault())

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
                        text = "Irrigation Control",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Root-zone underground valve management",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (sensorData.waterTankLevel < 25f) {
                    OutlinedButton(
                        onClick = onRefillTank,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WaterBlueLight)
                    ) {
                        Icon(imageVector = Icons.Default.Opacity, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Refill Tank", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mode Selector Tabs (AUTO vs MANUAL)
            TabRow(
                selectedTabIndex = if (controlState.mode == IrrigationMode.AUTO) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                indicator = { tabPositions ->
                    val tabIdx = if (controlState.mode == IrrigationMode.AUTO) 0 else 1
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[tabIdx]),
                        color = AgriGreenLight
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("irrigation_mode_tabs")
            ) {
                Tab(
                    selected = controlState.mode == IrrigationMode.AUTO,
                    onClick = { onSetMode(IrrigationMode.AUTO) },
                    text = {
                        Text(
                            text = "AUTO MODE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (controlState.mode == IrrigationMode.AUTO) AgriGreenLight else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = controlState.mode == IrrigationMode.MANUAL,
                    onClick = { onSetMode(IrrigationMode.MANUAL) },
                    text = {
                        Text(
                            text = "MANUAL OVERRIDE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (controlState.mode == IrrigationMode.MANUAL) SoilAmber else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AUTO MODE PANEL
            if (controlState.mode == IrrigationMode.AUTO) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AgriGreenLight.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Auto Threshold Tuning",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AgriGreenPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${controlState.moistureThreshold.toInt()}% Target",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriGreenLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Soil moisture threshold: ${controlState.moistureThreshold.toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Slider(
                            value = controlState.moistureThreshold,
                            onValueChange = { onSetMoistureThreshold(it) },
                            valueRange = 20f..60f,
                            steps = 7,
                            colors = SliderDefaults.colors(
                                thumbColor = AgriGreenLight,
                                activeTrackColor = AgriGreenPrimary
                            ),
                            modifier = Modifier.testTag("moisture_threshold_slider")
                        )

                        // Logic explanation card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Automated Engine Logic:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriGreenLight
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "• IF Soil Moisture < ${controlState.moistureThreshold.toInt()}% → Pump ON (Subsurface delivery)\n• IF Soil Moisture >= ${(controlState.moistureThreshold + 25f).toInt()}% → Pump OFF (Saturated)\n• IF Rain Expected >= 70% → Weather Pause engaged",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // MANUAL MODE PANEL
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SoilAmber.copy(alpha = 0.1f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SoilAmber.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(SoilAmber)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MANUAL OVERRIDE ACTIVE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SoilAmber,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = "Automatic threshold triggers are paused. You have direct control over the 12V submersible pump relay.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Large Tactile Pump Control Button
                        Button(
                            onClick = onRequestPumpToggle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .testTag("manual_pump_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (controlState.isPumpOn) StatusAlert else AgriGreenPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (controlState.isPumpOn) "PUMP OFF (STOP IRRIGATION)" else "PUMP ON (START IRRIGATION)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Underground Cross-Section Visualizer
            Text(
                text = "Subsurface Conduits & Flow Rate",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            UndergroundPipeVisualizer(
                isPumpActive = controlState.isPumpOn,
                soilMoisture = sensorData.soilMoisture,
                waterTankLevel = sensorData.waterTankLevel,
                flowRateLpm = sensorData.waterFlowRateLpm
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Operational Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricMiniCard(
                    title = "Water Flow Rate",
                    value = if (controlState.isPumpOn) "${sensorData.waterFlowRateLpm.toInt()} L/min" else "0 L/min",
                    subtitle = if (controlState.isPumpOn) "Active Delivery" else "Valve Closed",
                    color = if (controlState.isPumpOn) WaterBlueLight else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                MetricMiniCard(
                    title = "Last Irrigation",
                    value = timeFormatter.format(Date(controlState.lastIrrigationTime)),
                    subtitle = "${controlState.lastIrrigationDurationSec}s cycle",
                    color = AgriGreenLight,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricMiniCard(
                    title = "Water Conserved",
                    value = "${controlState.totalWaterSavedLiters.toInt()} L",
                    subtitle = "vs surface sprinkling",
                    color = AgriGreenLight,
                    modifier = Modifier.weight(1f)
                )

                MetricMiniCard(
                    title = "Next Expected",
                    value = if (sensorData.soilMoisture < 45f) "Within 1 hour" else "Tomorrow morning",
                    subtitle = "Based on evapotranspiration",
                    color = WaterBlueLight,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Confirmation Dialog before starting manual pump
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = onDismissConfirmDialog,
                title = {
                    Text(
                        text = "Confirm Manual Pump Start",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "This will energize the 12V submersible pump relay and deliver water directly to the root zone at ~14.5 L/min.\n\nWater Tank Level: ${sensorData.waterTankLevel.toInt()}%\nCurrent Moisture: ${sensorData.soilMoisture.toInt()}%"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onConfirmPumpStart,
                        colors = ButtonDefaults.buttonColors(containerColor = AgriGreenPrimary)
                    ) {
                        Text("Start Pump", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissConfirmDialog) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun MetricMiniCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
