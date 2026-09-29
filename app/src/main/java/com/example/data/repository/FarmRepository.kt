package com.example.data.repository

import com.example.data.local.AlertEntity
import com.example.data.local.AppDatabase
import com.example.data.local.DecisionEntity
import com.example.data.local.IrrigationEventEntity
import com.example.data.local.SensorReadingEntity
import com.example.data.models.DemoScenario
import com.example.data.models.Esp32DeviceState
import com.example.data.models.Esp32SensorPayload
import com.example.data.models.FarmProfile
import com.example.data.models.IrrigationControlState
import com.example.data.models.IrrigationMode
import com.example.data.models.SensorData
import com.example.data.models.SmartDecision
import com.example.data.models.WeatherState
import com.example.engine.EngineAction
import com.example.engine.SimulationEngine
import com.example.engine.SmartIrrigationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FarmRepository(
    private val database: AppDatabase,
    private val scope: CoroutineScope
) {
    private val simulationEngine = SimulationEngine()
    private val smartEngine = SmartIrrigationEngine()

    val sensorData: StateFlow<SensorData> = simulationEngine.sensorData
    val weatherState: StateFlow<WeatherState> = simulationEngine.weatherState
    val activeScenario: StateFlow<DemoScenario> = simulationEngine.activeScenario

    private val _controlState = MutableStateFlow(IrrigationControlState())
    val controlState: StateFlow<IrrigationControlState> = _controlState.asStateFlow()

    private val _farmProfile = MutableStateFlow(FarmProfile())
    val farmProfile: StateFlow<FarmProfile> = _farmProfile.asStateFlow()

    private val _esp32State = MutableStateFlow(Esp32DeviceState())
    val esp32State: StateFlow<Esp32DeviceState> = _esp32State.asStateFlow()

    private val _toastEvent = MutableStateFlow<String?>(null)
    val toastEvent: StateFlow<String?> = _toastEvent.asStateFlow()

    // Room DB streams
    val alerts: Flow<List<AlertEntity>> = database.alertDao().getAllAlerts()
    val unreadAlertCount: Flow<Int> = database.alertDao().getUnreadCount()
    val decisions: Flow<List<DecisionEntity>> = database.decisionDao().getDecisions(30)
    val irrigationEvents: Flow<List<IrrigationEventEntity>> = database.irrigationDao().getEvents(20)
    val recentReadings: Flow<List<SensorReadingEntity>> = database.sensorDao().getRecentReadings(100)

    private var pumpStartedTimestamp = 0L

    init {
        // Seed historical data and initial decisions if database is newly initialized
        scope.launch(Dispatchers.IO) {
            seedInitialData()
        }

        // Start background telemetry tick loop (runs every 2 seconds)
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(2000L)
                val currentControl = _controlState.value
                val updatedReading = simulationEngine.tick(currentControl.isPumpOn)

                // Update device heartbeat
                _esp32State.value = _esp32State.value.copy(
                    isOnline = updatedReading.isSensorOnline,
                    lastHeartbeatSecAgo = if (updatedReading.isSensorOnline) 1 else 999
                )

                // Record reading to Room DB every 3 ticks (every 6 seconds) to prevent ballooning
                if (System.currentTimeMillis() % 6000L < 2100L) {
                    database.sensorDao().insertReading(
                        SensorReadingEntity(
                            timestamp = updatedReading.timestamp,
                            soilMoisture = updatedReading.soilMoisture,
                            temperature = updatedReading.temperature,
                            humidity = updatedReading.humidity,
                            light = updatedReading.lightIntensity,
                            waterLevel = updatedReading.waterTankLevel,
                            pumpStatus = currentControl.isPumpOn
                        )
                    )
                }

                // Evaluate smart rules
                val actions = smartEngine.evaluate(
                    sensor = updatedReading,
                    weather = weatherState.value,
                    controlState = currentControl
                )

                for (action in actions) {
                    handleEngineAction(action)
                }
            }
        }
    }

    private suspend fun handleEngineAction(action: EngineAction) {
        when (action) {
            is EngineAction.StartPump -> {
                startPumpInternal(action.reason, action.isAuto)
            }
            is EngineAction.StopPump -> {
                stopPumpInternal(action.reason, action.isAuto)
            }
            is EngineAction.EmitAlert -> {
                database.alertDao().insertAlert(
                    AlertEntity(
                        timestamp = System.currentTimeMillis(),
                        type = action.type,
                        severity = action.severity,
                        title = action.title,
                        message = action.message
                    )
                )
                _toastEvent.value = "⚠ ${action.title}"
            }
            is EngineAction.RecordDecision -> {
                database.decisionDao().insertDecision(
                    DecisionEntity(
                        timestamp = System.currentTimeMillis(),
                        ruleType = action.icon,
                        reason = action.reason,
                        action = action.action,
                        status = action.status
                    )
                )
            }
            is EngineAction.UpdateWeatherPause -> {
                _controlState.value = _controlState.value.copy(isWeatherPaused = action.paused)
                if (action.paused) {
                    _toastEvent.value = "🌧 Irrigation paused: Rainfall expected"
                }
            }
        }
    }

    private suspend fun startPumpInternal(reason: String, isAuto: Boolean) {
        if (_controlState.value.isPumpOn) return
        pumpStartedTimestamp = System.currentTimeMillis()
        _controlState.value = _controlState.value.copy(
            isPumpOn = true,
            flowRateLpm = 14.5f
        )
        _toastEvent.value = "💧 Underground irrigation started (${if (isAuto) "Auto" else "Manual"})"
    }

    private suspend fun stopPumpInternal(reason: String, isAuto: Boolean) {
        if (!_controlState.value.isPumpOn) return
        val durationSec = if (pumpStartedTimestamp > 0) {
            ((System.currentTimeMillis() - pumpStartedTimestamp) / 1000).toInt().coerceAtLeast(1)
        } else {
            120
        }
        val litersDelivered = (durationSec / 60f) * 14.5f

        _controlState.value = _controlState.value.copy(
            isPumpOn = false,
            flowRateLpm = 0f,
            lastIrrigationTime = System.currentTimeMillis(),
            lastIrrigationDurationSec = durationSec,
            totalWaterSavedLiters = _controlState.value.totalWaterSavedLiters + 24.5f
        )

        database.irrigationDao().insertEvent(
            IrrigationEventEntity(
                timestamp = System.currentTimeMillis(),
                durationSeconds = durationSec,
                waterVolumeLiters = (litersDelivered * 10).toInt() / 10f,
                triggerType = if (isAuto) "AUTO" else "MANUAL",
                reason = reason
            )
        )
        _toastEvent.value = "🌱 Irrigation completed: ${litersDelivered.toInt()}L delivered to root zone"
    }

    // Public UI Actions
    fun setIrrigationMode(mode: IrrigationMode) {
        _controlState.value = _controlState.value.copy(mode = mode)
        _toastEvent.value = "Switched to ${mode.name} Mode"
    }

    fun setPumpManual(start: Boolean) {
        scope.launch(Dispatchers.IO) {
            if (start) {
                if (sensorData.value.waterTankLevel < 15f) {
                    _toastEvent.value = "❌ Cannot start: Water tank critically low"
                    return@launch
                }
                startPumpInternal("Manual operator command via app", isAuto = false)
            } else {
                stopPumpInternal("Manual operator shutoff via app", isAuto = false)
            }
        }
    }

    fun setMoistureThreshold(threshold: Float) {
        _controlState.value = _controlState.value.copy(moistureThreshold = threshold)
        _toastEvent.value = "Auto threshold set to ${threshold.toInt()}%"
    }

    fun setRainPauseEnabled(enabled: Boolean) {
        _controlState.value = _controlState.value.copy(rainPauseEnabled = enabled)
    }

    fun applyScenario(scenario: DemoScenario) {
        simulationEngine.applyScenario(scenario)
        _toastEvent.value = "Applied scenario: ${scenario.title}"
    }

    fun simulateDrySoil() {
        simulationEngine.simulateDrySoil()
        _toastEvent.value = "🧪 Simulation: Dry Soil triggered (26%)"
    }

    fun simulateRain(raining: Boolean = true) {
        simulationEngine.simulateRain(raining)
        _toastEvent.value = "🧪 Simulation: Rain event ${if (raining) "active" else "cleared"}"
    }

    fun simulateHighTemperature() {
        simulationEngine.simulateHighTemperature()
        _toastEvent.value = "🧪 Simulation: High Temp Heatwave (37.2°C)"
    }

    fun simulateLowTank() {
        simulationEngine.simulateLowTank()
        _toastEvent.value = "🧪 Simulation: Low Water Tank (14%)"
    }

    fun simulateSensorOffline(offline: Boolean = true) {
        simulationEngine.simulateSensorOffline(offline)
        _toastEvent.value = if (offline) "🧪 Simulation: Sensor Offline" else "🧪 Simulation: Sensor Restored"
    }

    val isSimulatedRaining: StateFlow<Boolean> = simulationEngine.isSimulatedRaining

    fun refillWaterTank() {
        simulationEngine.refillTank()
        _toastEvent.value = "💧 Water tank refilled to 95%"
    }

    fun ingestEsp32Payload(payload: Esp32SensorPayload) {
        simulationEngine.ingestEsp32Payload(payload)
        _toastEvent.value = "ESP32 telemetry packet ingested from ${payload.deviceId}"
    }

    fun markAlertRead(alertId: Long) {
        scope.launch(Dispatchers.IO) {
            database.alertDao().markAsRead(alertId)
        }
    }

    fun markAllAlertsRead() {
        scope.launch(Dispatchers.IO) {
            database.alertDao().markAllAsRead()
        }
    }

    fun clearAllAlerts() {
        scope.launch(Dispatchers.IO) {
            database.alertDao().clearAll()
        }
    }

    fun updateFarmProfile(
        farmName: String,
        location: String,
        crop: String,
        area: Int,
        plants: Int,
        soil: String
    ) {
        _farmProfile.value = _farmProfile.value.copy(
            farmName = farmName,
            location = location,
            primaryCrop = crop,
            areaSqMeters = area,
            plantCount = plants,
            soilType = soil
        )
        _toastEvent.value = "Farm profile updated"
    }

    fun clearToast() {
        _toastEvent.value = null
    }

    private suspend fun seedInitialData() {
        val initialHistory = simulationEngine.generateHistoricalReadings(48)
        val entities = initialHistory.map {
            SensorReadingEntity(
                timestamp = it.timestamp,
                soilMoisture = it.soilMoisture,
                temperature = it.temperature,
                humidity = it.humidity,
                light = it.lightIntensity,
                waterLevel = it.waterTankLevel,
                pumpStatus = it.pumpActive
            )
        }
        database.sensorDao().insertAll(entities)

        // Seed sample decisions
        val now = System.currentTimeMillis()
        database.decisionDao().insertAll(
            listOf(
                DecisionEntity(
                    timestamp = now - 3600 * 1000L * 4,
                    ruleType = "🌱",
                    reason = "Soil moisture reached 32% (below 35% threshold)",
                    action = "Activated underground subsurface drip line for 180s",
                    status = "RESOLVED"
                ),
                DecisionEntity(
                    timestamp = now - 3600 * 1000L * 2,
                    ruleType = "🌧",
                    reason = "Rainfall radar detected 80% precipitation probability",
                    action = "Paused scheduled afternoon irrigation cycle. Conserved 180L",
                    status = "RESOLVED"
                ),
                DecisionEntity(
                    timestamp = now - 1800 * 1000L,
                    ruleType = "☀",
                    reason = "Midday solar intensity peaked at 88 klx",
                    action = "Underground drip delivery prevents leaf scorch and evaporation",
                    status = "APPLIED"
                )
            )
        )

        // Seed initial alerts
        database.alertDao().insertAlert(
            AlertEntity(
                timestamp = now - 3600 * 1000L * 3,
                type = "MOISTURE_LOW",
                severity = "INFO",
                title = "Root Zone Hydration Complete",
                message = "Delivered 43.5 Liters directly to root depth (25 cm). Evaporative loss < 3%.",
                isRead = true
            )
        )
        database.alertDao().insertAlert(
            AlertEntity(
                timestamp = now - 3600 * 1000L * 1,
                type = "RAIN_PAUSE",
                severity = "INFO",
                title = "Weather Intelligence Active",
                message = "Rainfall probability monitored. Smart valve adjusted.",
                isRead = false
            )
        )
    }
}
