package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AgriGreenLight
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.SoilAmber
import com.example.ui.theme.WaterBlueLight
import com.example.ui.theme.WaterBluePrimary

/**
 * Visual Representation of the College Project's Core Concept:
 * Subsurface Root-Zone Underground Drip Irrigation
 *
 * Water Tank (Surface) → 12V Pump → Underground PVC Pipe → Root Zone (25cm) → Plant
 */
@Composable
fun UndergroundPipeVisualizer(
    isPumpActive: Boolean,
    soilMoisture: Float,
    waterTankLevel: Float,
    flowRateLpm: Float = 14.5f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waterFlow")
    val flowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flowPhase"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("underground_visualizer_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPumpActive) AgriGreenLight.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isPumpActive) AgriGreenLight else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Underground Root-Zone Delivery",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isPumpActive) WaterBluePrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isPumpActive) "PUMP ACTIVE: ${flowRateLpm.toInt()} L/min" else "PUMP IDLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPumpActive) WaterBlueLight else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Cross-Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0F1A15))
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height

                    // Ground Level line at y = 35% of canvas
                    val groundY = h * 0.35f
                    val pipeY = h * 0.75f // Underground pipe depth (25cm)

                    // 1. Atmosphere / Sky (top 35%)
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF132A24), Color(0xFF1A3830)),
                            startY = 0f,
                            endY = groundY
                        ),
                        topLeft = Offset(0f, 0f),
                        size = Size(w, groundY)
                    )

                    // 2. Underground Soil Layers (65% bottom)
                    // Topsoil layer
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF261D15), Color(0xFF1A130E)),
                            startY = groundY,
                            endY = h
                        ),
                        topLeft = Offset(0f, groundY),
                        size = Size(w, h - groundY)
                    )

                    // Ground Surface Grass Line
                    drawLine(
                        color = Color(0xFF22C55E),
                        start = Offset(0f, groundY),
                        end = Offset(w, groundY),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // 3. Water Tank (Left side)
                    val tankX = w * 0.08f
                    val tankY = groundY - 50.dp.toPx()
                    val tankW = 54.dp.toPx()
                    val tankH = 65.dp.toPx()

                    // Tank body
                    drawRoundRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(tankX, tankY),
                        size = Size(tankW, tankH),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color(0xFF475569),
                        topLeft = Offset(tankX, tankY),
                        size = Size(tankW, tankH),
                        cornerRadius = CornerRadius(6.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Water Fill in Tank
                    val fillH = tankH * (waterTankLevel / 100f).coerceIn(0.05f, 1f)
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
                            startY = tankY + tankH - fillH,
                            endY = tankY + tankH
                        ),
                        topLeft = Offset(tankX + 3.dp.toPx(), tankY + tankH - fillH),
                        size = Size(tankW - 6.dp.toPx(), fillH - 3.dp.toPx()),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )

                    // 4. Plant Stem & Foliage above ground (Center-right x = w * 0.62)
                    val plantX = w * 0.62f
                    // Stem
                    drawLine(
                        color = Color(0xFF16A34A),
                        start = Offset(plantX, groundY),
                        end = Offset(plantX, groundY - 55.dp.toPx()),
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // Leaves
                    val leafPathLeft = Path().apply {
                        moveTo(plantX, groundY - 30.dp.toPx())
                        quadraticTo(
                            plantX - 25.dp.toPx(), groundY - 45.dp.toPx(),
                            plantX - 22.dp.toPx(), groundY - 20.dp.toPx()
                        )
                        close()
                    }
                    drawPath(leafPathLeft, Color(0xFF22C55E))

                    val leafPathRight = Path().apply {
                        moveTo(plantX, groundY - 40.dp.toPx())
                        quadraticTo(
                            plantX + 25.dp.toPx(), groundY - 55.dp.toPx(),
                            plantX + 22.dp.toPx(), groundY - 30.dp.toPx()
                        )
                        close()
                    }
                    drawPath(leafPathRight, Color(0xFF4ADE80))

                    // Tomato fruit badge
                    drawCircle(
                        color = Color(0xFFEF4444),
                        radius = 6.dp.toPx(),
                        center = Offset(plantX + 12.dp.toPx(), groundY - 35.dp.toPx())
                    )

                    // 5. Underground Root System (reaching down to pipe)
                    val rootColor = Color(0xFFD4A373)
                    // Primary tap root
                    drawLine(
                        color = rootColor,
                        start = Offset(plantX, groundY),
                        end = Offset(plantX, pipeY - 6.dp.toPx()),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // Lateral roots
                    drawLine(
                        color = rootColor,
                        start = Offset(plantX, groundY + 15.dp.toPx()),
                        end = Offset(plantX - 25.dp.toPx(), groundY + 35.dp.toPx()),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    drawLine(
                        color = rootColor,
                        start = Offset(plantX, groundY + 22.dp.toPx()),
                        end = Offset(plantX + 28.dp.toPx(), groundY + 45.dp.toPx()),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    drawLine(
                        color = rootColor,
                        start = Offset(plantX, groundY + 38.dp.toPx()),
                        end = Offset(plantX - 20.dp.toPx(), pipeY - 2.dp.toPx()),
                        strokeWidth = 1.2.dp.toPx()
                    )

                    // 6. Underground Moisture Diffusion Bubble around roots
                    val moistureAlpha = (soilMoisture / 100f).coerceIn(0.15f, 0.7f)
                    val bubbleRadius = (35.dp.toPx() + (if (isPumpActive) flowPhase * 12.dp.toPx() else 0f))
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF0284C7).copy(alpha = moistureAlpha),
                                Color(0xFF0369A1).copy(alpha = moistureAlpha * 0.4f),
                                Color.Transparent
                            ),
                            center = Offset(plantX, pipeY),
                            radius = bubbleRadius
                        ),
                        radius = bubbleRadius,
                        center = Offset(plantX, pipeY)
                    )

                    // 7. Pipe Routing: Tank → Pump → Down underground → Horizontal Subsurface line
                    val pipeStartX = tankX + tankW * 0.5f
                    val pipeStartY = tankY + tankH
                    val pipePath = Path().apply {
                        moveTo(pipeStartX, pipeStartY)
                        lineTo(pipeStartX, pipeY)
                        lineTo(w * 0.88f, pipeY)
                    }

                    // Outer pipe casing
                    drawPath(
                        path = pipePath,
                        color = Color(0xFF334155),
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Inner water flow path
                    val waterColor = if (isPumpActive) Color(0xFF38BDF8) else Color(0xFF1E293B)
                    drawPath(
                        path = pipePath,
                        color = waterColor,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 8. Animated Water Flow Particles when pump is ON
                    if (isPumpActive) {
                        for (i in 0..6) {
                            val t = (flowPhase + (i / 7f)) % 1f
                            val px = pipeStartX + (w * 0.88f - pipeStartX) * t
                            drawCircle(
                                color = Color.White,
                                radius = 2.5.dp.toPx(),
                                center = Offset(px, pipeY)
                            )
                        }

                        // Underground Emitter Driplets escaping pipe directly to root zone
                        for (emitterOffset in listOf(-18.dp.toPx(), 0f, 18.dp.toPx())) {
                            val dropY = pipeY - (flowPhase * 12.dp.toPx())
                            drawCircle(
                                color = Color(0xFF7DD3FC),
                                radius = 2.dp.toPx(),
                                center = Offset(plantX + emitterOffset, dropY)
                            )
                        }
                    }

                    // 9. Underground depth marker line & label
                    drawLine(
                        color = Color(0xFF64748B),
                        start = Offset(w * 0.92f, groundY),
                        end = Offset(w * 0.92f, pipeY),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Overlay labels
                Text(
                    text = "Water Tank (${waterTankLevel.toInt()}%)",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 12.dp, top = 8.dp)
                )

                Text(
                    text = "Soil Depth: 25 cm (Root Zone)",
                    color = Color(0xFFA7F3D0),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 8.dp)
                )

                Text(
                    text = "Moisture: ${soilMoisture.toInt()}%",
                    color = if (soilMoisture < 35f) SoilAmber else AgriGreenLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 12.dp, bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Engineering Concept Explanation Callout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Concept Info",
                    tint = AgriGreenLight,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Underground subsurface drip delivers water directly to root depth (20–30 cm). Zero surface evaporation, 45% water savings vs sprinklers.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
