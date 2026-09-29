package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SensorReadingEntity
import com.example.ui.theme.AgriGreenLight
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.WaterBlueLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ChartMetric(val label: String, val unit: String, val color: Color) {
    MOISTURE("Soil Moisture", "%", AgriGreenLight),
    TEMPERATURE("Temperature", "°C", Color(0xFFF97316)),
    HUMIDITY("Humidity", "%", WaterBlueLight),
    LIGHT("Light", "%", Color(0xFFFBBF24)),
    WATER_TANK("Water Tank", "%", Color(0xFF38BDF8))
}

@Composable
fun InteractiveSensorChart(
    readings: List<SensorReadingEntity>,
    selectedMetric: ChartMetric,
    onSelectMetric: (ChartMetric) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTimeRange by remember { mutableStateOf("24h") } // "1h", "6h", "24h", "7d"
    var scrubbedIndex by remember { mutableStateOf<Int?>(null) }

    // Filter readings based on time range
    val now = System.currentTimeMillis()
    val cutoff = when (selectedTimeRange) {
        "1h" -> now - 3600 * 1000L
        "6h" -> now - 6 * 3600 * 1000L
        "24h" -> now - 24 * 3600 * 1000L
        "7d" -> now - 7 * 24 * 3600 * 1000L
        else -> now - 24 * 3600 * 1000L
    }

    val filteredList = readings.filter { it.timestamp >= cutoff }.sortedBy { it.timestamp }
    val dataPoints: List<Float> = if (filteredList.isNotEmpty()) {
        filteredList.map { entity ->
            when (selectedMetric) {
                ChartMetric.MOISTURE -> entity.soilMoisture
                ChartMetric.TEMPERATURE -> entity.temperature
                ChartMetric.HUMIDITY -> entity.humidity
                ChartMetric.LIGHT -> entity.light
                ChartMetric.WATER_TANK -> entity.waterLevel
            }
        }
    } else {
        // Fallback default sample curve
        listOf(62f, 64f, 61f, 58f, 55f, 48f, 42f, 34f, 65f, 68f, 66f, 64f)
    }

    val minVal = (dataPoints.minOrNull() ?: 0f).coerceAtLeast(0f)
    val maxVal = (dataPoints.maxOrNull() ?: 100f).coerceAtLeast(minVal + 10f)
    val avgVal = if (dataPoints.isNotEmpty()) dataPoints.average().toFloat() else 0f

    val currentPointValue = scrubbedIndex?.let { dataPoints.getOrNull(it) } ?: dataPoints.lastOrNull() ?: 0f
    val currentTimestamp = scrubbedIndex?.let { filteredList.getOrNull(it)?.timestamp } ?: System.currentTimeMillis()
    val timeFormatter = remember { SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_sensor_chart"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            selectedMetric.color.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with metric label & scrubbed value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = selectedMetric.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = timeFormatter.format(Date(currentTimestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format(Locale.US, "%.1f", currentPointValue),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = selectedMetric.color
                    )
                    Text(
                        text = " ${selectedMetric.unit}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Time range chips (1h, 6h, 24h, 7d)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("1h", "6h", "24h", "7d").forEach { range ->
                    val isSelected = range == selectedTimeRange
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTimeRange = range },
                        label = { Text(range, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = selectedMetric.color.copy(alpha = 0.2f),
                            selectedLabelColor = selectedMetric.color
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) selectedMetric.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.height(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Chart Area with Pointer Drag Detection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0D1714))
                    .pointerInput(dataPoints) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val count = dataPoints.size
                                if (count > 1) {
                                    val idx = ((offset.x / size.width) * (count - 1))
                                        .toInt()
                                        .coerceIn(0, count - 1)
                                    scrubbedIndex = idx
                                }
                            },
                            onDrag = { change, _ ->
                                val count = dataPoints.size
                                if (count > 1) {
                                    val idx = ((change.position.x / size.width) * (count - 1))
                                        .toInt()
                                        .coerceIn(0, count - 1)
                                    scrubbedIndex = idx
                                }
                            },
                            onDragEnd = { scrubbedIndex = null },
                            onDragCancel = { scrubbedIndex = null }
                        )
                    }
                    .pointerInput(dataPoints) {
                        detectTapGestures(
                            onTap = { offset ->
                                val count = dataPoints.size
                                if (count > 1) {
                                    val idx = ((offset.x / size.width) * (count - 1))
                                        .toInt()
                                        .coerceIn(0, count - 1)
                                    scrubbedIndex = idx
                                }
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.matchParentSize().padding(horizontal = 8.dp, vertical = 12.dp)) {
                    val w = size.width
                    val h = size.height

                    if (dataPoints.isEmpty()) return@Canvas

                    val range = (maxVal - minVal).coerceAtLeast(1f)
                    val stepX = w / (dataPoints.size - 1).coerceAtLeast(1)

                    // Draw subtle grid guidelines
                    for (ratio in listOf(0.25f, 0.5f, 0.75f)) {
                        val gy = h * ratio
                        drawLine(
                            color = Color(0xFF1E2E28),
                            start = Offset(0f, gy),
                            end = Offset(w, gy),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Build Bezier Smooth Path
                    val path = Path()
                    val fillPath = Path()

                    val points = dataPoints.mapIndexed { index, value ->
                        val x = index * stepX
                        val normalizedY = (value - minVal) / range
                        val y = h - (normalizedY * h)
                        Offset(x, y)
                    }

                    if (points.isNotEmpty()) {
                        path.moveTo(points[0].x, points[0].y)
                        fillPath.moveTo(points[0].x, h)
                        fillPath.lineTo(points[0].x, points[0].y)

                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val control1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                            val control2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)

                            path.cubicTo(control1.x, control1.y, control2.x, control2.y, p1.x, p1.y)
                            fillPath.cubicTo(control1.x, control1.y, control2.x, control2.y, p1.x, p1.y)
                        }

                        fillPath.lineTo(points.last().x, h)
                        fillPath.close()

                        // Draw Gradient Area under curve
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    selectedMetric.color.copy(alpha = 0.35f),
                                    selectedMetric.color.copy(alpha = 0.02f)
                                ),
                                startY = 0f,
                                endY = h
                            )
                        )

                        // Draw smooth line
                        drawPath(
                            path = path,
                            color = selectedMetric.color,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw scrub cursor if user is dragging
                        scrubbedIndex?.let { idx ->
                            if (idx in points.indices) {
                                val sp = points[idx]
                                // Vertical guideline
                                drawLine(
                                    color = Color.White.copy(alpha = 0.6f),
                                    start = Offset(sp.x, 0f),
                                    end = Offset(sp.x, h),
                                    strokeWidth = 1.5.dp.toPx()
                                )
                                // Highlight dot
                                drawCircle(
                                    color = Color.White,
                                    radius = 6.dp.toPx(),
                                    center = sp
                                )
                                drawCircle(
                                    color = selectedMetric.color,
                                    radius = 4.dp.toPx(),
                                    center = sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stat Summary Chips (Min, Avg, Max)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatBadge("Min", String.format(Locale.US, "%.1f%s", minVal, selectedMetric.unit))
                StatBadge("Avg", String.format(Locale.US, "%.1f%s", avgVal, selectedMetric.unit))
                StatBadge("Max", String.format(Locale.US, "%.1f%s", maxVal, selectedMetric.unit))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metric Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChartMetric.values().forEach { metric ->
                    val isCur = metric == selectedMetric
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isCur) metric.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                1.dp,
                                if (isCur) metric.color else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = metric.label.split(" ").last(),
                            fontSize = 10.sp,
                            fontWeight = if (isCur) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCur) metric.color else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label: ",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
