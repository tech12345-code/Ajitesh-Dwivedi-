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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.models.HourlyForecast
import com.example.data.models.IrrigationControlState
import com.example.data.models.WeatherState
import com.example.ui.theme.AgriGreenLight
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.SoilAmber
import com.example.ui.theme.WaterBlueLight
import com.example.ui.theme.WaterBluePrimary

@Composable
fun WeatherScreen(
    weatherState: WeatherState,
    controlState: IrrigationControlState,
    onToggleRainPause: (Boolean) -> Unit
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
                text = "Weather Intelligence",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Precipitation radar & evapotranspiration engine",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Weather Hero Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weather_hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (weatherState.rainProbability > 60) WaterBlueLight else AgriGreenLight.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Greenfield Agritech Farm",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = weatherState.condition,
                                fontSize = 13.sp,
                                color = AgriGreenLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Icon(
                            imageVector = if (weatherState.rainProbability > 60) Icons.Default.Cloud else Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = if (weatherState.rainProbability > 60) WaterBlueLight else Color(0xFFFBBF24),
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${weatherState.temperature.toInt()}°",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.padding(bottom = 8.dp)) {
                            Text(
                                text = "Rain Probability: ${weatherState.rainProbability}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (weatherState.rainProbability > 60) WaterBlueLight else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Humidity: ${weatherState.humidity.toInt()}% • Wind: ${weatherState.windSpeedKph.toInt()} km/h",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Smart Weather-Irrigation Engine Reasoning Box (Highlight of College Project)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weather_reasoning_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (controlState.isWeatherPaused)
                        WaterBluePrimary.copy(alpha = 0.2f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (controlState.isWeatherPaused) WaterBlueLight else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (controlState.isWeatherPaused) WaterBlueLight else AgriGreenLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Weather Engine Decision",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Weather pause toggle switch
                        Switch(
                            checked = controlState.rainPauseEnabled,
                            onCheckedChange = onToggleRainPause,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AgriGreenLight,
                                checkedTrackColor = AgriGreenPrimary
                            ),
                            modifier = Modifier.testTag("weather_pause_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (controlState.isWeatherPaused)
                            "🌧 Rain expected in 2 hours (${weatherState.rainProbability}% probability). Scheduled irrigation temporarily paused to conserve underground water reserves."
                        else
                            "☀ Sky clear with rain probability at ${weatherState.rainProbability}%. Standard subsurface root irrigation rules active.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (controlState.isWeatherPaused) WaterBlueLight else AgriGreenLight,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = weatherState.forecastSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Hourly Forecast Strip
            Text(
                text = "Hourly Projection",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                weatherState.forecastHours.forEach { item ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = item.timeLabel,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Icon(
                                imageVector = if (item.rainProb > 50) Icons.Default.Cloud else Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = if (item.rainProb > 50) WaterBlueLight else Color(0xFFFBBF24),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${item.temp.toInt()}°",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${item.rainProb}%",
                                fontSize = 10.sp,
                                color = if (item.rainProb > 40) WaterBlueLight else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
