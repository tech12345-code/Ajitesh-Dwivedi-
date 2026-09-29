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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.UndergroundPipeVisualizer
import com.example.ui.theme.AgriGreenDark
import com.example.ui.theme.AgriGreenLight
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.SoilAmber
import com.example.ui.theme.WaterBlueLight
import com.example.ui.theme.WaterBluePrimary

@Composable
fun LandingScreen(
    onOpenDashboard: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onInstantDemoLogin: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Brand Header Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(AgriGreenPrimary.copy(alpha = 0.2f), WaterBluePrimary.copy(alpha = 0.2f))
                        )
                    )
                    .border(
                        1.dp,
                        AgriGreenLight.copy(alpha = 0.4f),
                        RoundedCornerShape(30.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = null,
                        tint = AgriGreenLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SMART FARM INNOVATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgriGreenLight,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Hero Title
            Text(
                text = "Grow Smarter.\nSave Water.",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 36.sp,
                    lineHeight = 42.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle
            Text(
                text = "Smart Underground Irrigation & Crop Monitoring System",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AgriGreenLight,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Autonomous root-zone moisture regulation, ESP32 telemetry streaming, and weather-aware water conservation.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Call-to-Actions
            Button(
                onClick = onOpenDashboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("open_dashboard_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgriGreenPrimary
                )
            ) {
                Text(
                    text = "Open Smart Dashboard",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onInstantDemoLogin,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("instant_demo_login_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = WaterBlueLight
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, WaterBlueLight)
                ) {
                    Text(
                        text = "⚡ Instant Demo",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                OutlinedButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("sign_in_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Sign In",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // System Architecture Flow Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Engineering Architecture",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = AgriGreenLight
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ArchStep("Sensors", "Soil, DHT22, LDR", Icons.Default.WaterDrop, AgriGreenLight)
                        ArchArrow()
                        ArchStep("ESP32", "Microcontroller", Icons.Default.Memory, WaterBlueLight)
                        ArchArrow()
                        ArchStep("Smart Engine", "Rule Logic", Icons.Default.AutoGraph, SoilAmber)
                        ArchArrow()
                        ArchStep("Pump", "12V Submersible", Icons.Default.PowerSettingsNew, Color(0xFFF87171))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Core Project Concept: Subsurface Irrigation Visualizer
            Text(
                text = "Underground Root-Zone Concept",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Water is delivered via perforated underground conduits directly to the plant parenchyma (root zone) rather than spraying the soil surface.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            UndergroundPipeVisualizer(
                isPumpActive = true,
                soilMoisture = 68f,
                waterTankLevel = 75f
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Key Innovation Features
            Text(
                text = "System Features",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            FeatureCard(
                icon = Icons.Default.WaterDrop,
                title = "Subsurface Root Delivery",
                description = "45% water savings compared to surface sprinklers. Eliminates evaporation and weed germination on topsoil."
            )
            FeatureCard(
                icon = Icons.Default.AutoGraph,
                title = "Smart Decision Engine",
                description = "Automated pump trigger when root moisture drops below threshold (e.g., 35%). Cutoff safety prevents over-saturation."
            )
            FeatureCard(
                icon = Icons.Default.Cloud,
                title = "Weather Intelligence",
                description = "Integrates rainfall radar predictions. Automatically pauses scheduled irrigation if rain is incoming."
            )
            FeatureCard(
                icon = Icons.Default.Security,
                title = "Low-Reservoir Protection",
                description = "Ultrasonic tank monitoring triggers emergency pump cutoff below 20% to prevent dry-running motor burnout."
            )
            FeatureCard(
                icon = Icons.Default.Memory,
                title = "ESP32 Hardware-Ready",
                description = "REST API bridge accepts standard JSON telemetry payloads from physical ESP32 Wi-Fi modules."
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom CTA
            Button(
                onClick = onOpenDashboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgriGreenPrimary
                )
            ) {
                Text(
                    text = "Launch Smart Farm App",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Smart Farm v2.4 • College Engineering Project Demo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ArchStep(title: String, subtitle: String, icon: ImageVector, tint: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(68.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f))
                .border(1.dp, tint.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}

@Composable
private fun ArchArrow() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.size(14.dp)
    )
}

@Composable
private fun FeatureCard(icon: ImageVector, title: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AgriGreenPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = AgriGreenLight,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
