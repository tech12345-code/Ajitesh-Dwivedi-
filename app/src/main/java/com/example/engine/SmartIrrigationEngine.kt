package com.example.engine

import com.example.data.models.IrrigationControlState
import com.example.data.models.IrrigationMode
import com.example.data.models.SensorData
import com.example.data.models.SmartDecision
import com.example.data.models.WeatherState

sealed class EngineAction {
    data class StartPump(val reason: String, val isAuto: Boolean) : EngineAction()
    data class StopPump(val reason: String, val isAuto: Boolean) : EngineAction()
    data class EmitAlert(
        val type: String,
        val severity: String,
        val title: String,
        val message: String
    ) : EngineAction()
    data class RecordDecision(
        val icon: String,
        val title: String,
        val reason: String,
        val action: String,
        val status: String
    ) : EngineAction()
    data class UpdateWeatherPause(val paused: Boolean, val reason: String) : EngineAction()
}

/**
 * Smart Irrigation Engine - Central decision rules for autonomous underground drip
 */
class SmartIrrigationEngine {

    private var lastMoistureAlertTime = 0L
    private var lastTankAlertTime = 0L
    private var lastTempAlertTime = 0L
    private var lastDecisionKey = ""

    fun evaluate(
        sensor: SensorData,
        weather: WeatherState,
        controlState: IrrigationControlState
    ): List<EngineAction> {
        val actions = mutableListOf<EngineAction>()
        val now = System.currentTimeMillis()

        if (!sensor.isSensorOnline) {
            actions.add(
                EngineAction.EmitAlert(
                    type = "SENSOR_OFFLINE",
                    severity = "WARNING",
                    title = "ESP32 Telemetry Disconnected",
                    message = "Controller heartbeat lost. Fallback safety rules applied."
                )
            )
            return actions
        }

        // 1. Weather Intelligence Check
        val shouldWeatherPause = controlState.rainPauseEnabled && weather.rainProbability >= 70
        if (shouldWeatherPause != controlState.isWeatherPaused) {
            val pauseReason = if (shouldWeatherPause) {
                "Rain probability is ${weather.rainProbability}%. Pausing underground irrigation to conserve reservoir water."
            } else {
                "Rain probability dropped to ${weather.rainProbability}%. Resuming standard irrigation schedule."
            }
            actions.add(EngineAction.UpdateWeatherPause(shouldWeatherPause, pauseReason))

            val decisionKey = "WEATHER_${shouldWeatherPause}_${weather.rainProbability / 10}"
            if (decisionKey != lastDecisionKey) {
                lastDecisionKey = decisionKey
                actions.add(
                    EngineAction.RecordDecision(
                        icon = if (shouldWeatherPause) "🌧" else "☀",
                        title = if (shouldWeatherPause) "Irrigation Paused by Weather Engine" else "Weather Clear - Routine Active",
                        reason = pauseReason,
                        action = if (shouldWeatherPause) "Hold valve closed, monitor rain accumulation" else "Ready for automatic threshold watering",
                        status = "APPLIED"
                    )
                )
            }
        }

        // 2. Tank Safety Cutoff
        if (sensor.waterTankLevel < 20f) {
            if (controlState.isPumpOn) {
                actions.add(
                    EngineAction.StopPump(
                        reason = "Safety Cutoff: Water tank level critically low (${sensor.waterTankLevel}%) to prevent pump impeller damage.",
                        isAuto = true
                    )
                )
            }
            if (now - lastTankAlertTime > 60_000L) {
                lastTankAlertTime = now
                actions.add(
                    EngineAction.EmitAlert(
                        type = "TANK_LOW",
                        severity = "CRITICAL",
                        title = "Low Water Tank Level",
                        message = "Reservoir is at ${sensor.waterTankLevel}%. Please refill water tank."
                    )
                )
                actions.add(
                    EngineAction.RecordDecision(
                        icon = "💧",
                        title = "Low Water Reserve Protection",
                        reason = "Tank volume depleted below 20% safety threshold.",
                        action = "Pump shutdown enforced. Awaiting tank refill.",
                        status = "ACTIVE"
                    )
                )
            }
            return actions
        }

        // 3. High Temperature Stress Check
        if (sensor.temperature > 34f && (now - lastTempAlertTime > 120_000L)) {
            lastTempAlertTime = now
            actions.add(
                EngineAction.EmitAlert(
                    type = "TEMP_HIGH",
                    severity = "WARNING",
                    title = "High Ambient Heat (${sensor.temperature}°C)",
                    message = "Subsurface drip keeps root zone cooled against thermal shock."
                )
            )
            actions.add(
                EngineAction.RecordDecision(
                    icon = "☀",
                    title = "Heat Stress Mitigation",
                    reason = "Ambient temperature is ${sensor.temperature}°C, accelerating crop transpiration.",
                    action = "Increased soil moisture polling interval to 2 seconds.",
                    status = "APPLIED"
                )
            )
        }

        // 4. Auto Mode Logic
        if (controlState.mode == IrrigationMode.AUTO) {
            // Trigger threshold
            if (sensor.soilMoisture < controlState.moistureThreshold) {
                if (shouldWeatherPause) {
                    // Rain pause takes priority
                    if (controlState.isPumpOn) {
                        actions.add(EngineAction.StopPump("Moisture low, but rainfall predicted. Saving water.", true))
                    }
                } else if (!controlState.isPumpOn) {
                    val reason = "Soil moisture (${sensor.soilMoisture}%) fell below target threshold (${controlState.moistureThreshold}%)."
                    actions.add(EngineAction.StartPump(reason, true))
                    actions.add(
                        EngineAction.RecordDecision(
                            icon = "🌱",
                            title = "Subsurface Irrigation Initiated",
                            reason = reason,
                            action = "Pump 12V relay energized. Underground pipes delivering water to root zone.",
                            status = "ACTIVE"
                        )
                    )
                    actions.add(
                        EngineAction.EmitAlert(
                            type = "MOISTURE_LOW",
                            severity = "INFO",
                            title = "Underground Irrigation Started",
                            message = "Delivering water directly to root depth (25 cm). Evaporation minimized."
                        )
                    )
                }
            } else if (sensor.soilMoisture >= (controlState.moistureThreshold + 25f).coerceAtMost(78f)) {
                // Soil is sufficiently hydrated
                if (controlState.isPumpOn) {
                    val reason = "Optimal soil moisture reached (${sensor.soilMoisture}%)."
                    actions.add(EngineAction.StopPump(reason, true))
                    actions.add(
                        EngineAction.RecordDecision(
                            icon = "✅",
                            title = "Optimal Moisture Level Achieved",
                            reason = reason,
                            action = "Pump stopped. Root zone saturation optimal.",
                            status = "APPLIED"
                        )
                    )
                }
            }
        }

        return actions
    }
}
