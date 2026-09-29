package com.example.engine

import com.example.data.models.DemoScenario
import com.example.data.models.Esp32SensorPayload
import com.example.data.models.HourlyForecast
import com.example.data.models.SensorData
import com.example.data.models.WeatherState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sin
import kotlin.random.Random

/**
 * Realistic Simulation Engine for Smart Farm.
 * Models continuous physical dynamics:
 * - Soil transpiration and drying over time
 * - Underground irrigation pipe water delivery directly to root zone (+moisture, -tank level)
 * - Rain precipitation percolation (+moisture, +humidity, -temperature, -light)
 * - Diurnal thermal and solar cycles
 * - ESP32 telemetry packet streaming
 * - Developer controls for instant scenario triggering
 */
class SimulationEngine {

    private val _sensorData = MutableStateFlow(SensorData())
    val sensorData: StateFlow<SensorData> = _sensorData.asStateFlow()

    private val _weatherState = MutableStateFlow(createInitialWeather())
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()

    private val _activeScenario = MutableStateFlow(DemoScenario.NORMAL)
    val activeScenario: StateFlow<DemoScenario> = _activeScenario.asStateFlow()

    // Developer controls state
    private val _isSimulatedRaining = MutableStateFlow(false)
    val isSimulatedRaining: StateFlow<Boolean> = _isSimulatedRaining.asStateFlow()

    private var tickCount = 0L

    fun applyScenario(scenario: DemoScenario) {
        _activeScenario.value = scenario
        val current = _sensorData.value

        when (scenario) {
            DemoScenario.NORMAL -> {
                _isSimulatedRaining.value = false
                _sensorData.value = current.copy(
                    soilMoisture = 65.5f,
                    temperature = 27.5f,
                    humidity = 62f,
                    lightIntensity = 75f,
                    waterTankLevel = 82f,
                    isSensorOnline = true
                )
                _weatherState.value = _weatherState.value.copy(
                    condition = "Partly Cloudy",
                    temperature = 27.5f,
                    humidity = 62f,
                    rainProbability = 15,
                    isRainExpectedSoon = false,
                    forecastSummary = "Moderate temperature, optimal underground root conditions."
                )
            }
            DemoScenario.DRY_SOIL -> {
                _isSimulatedRaining.value = false
                _sensorData.value = current.copy(
                    soilMoisture = 26.0f, // Drops below the 35% trigger threshold
                    temperature = 31.2f,
                    humidity = 46f,
                    lightIntensity = 88f,
                    waterTankLevel = 75f,
                    isSensorOnline = true
                )
                _weatherState.value = _weatherState.value.copy(
                    condition = "Dry & Sunny",
                    temperature = 31.2f,
                    humidity = 46f,
                    rainProbability = 5,
                    isRainExpectedSoon = false,
                    forecastSummary = "Dry front active. Underground root zone requires hydration."
                )
            }
            DemoScenario.RAIN_EXPECTED -> {
                _isSimulatedRaining.value = true
                _sensorData.value = current.copy(
                    soilMoisture = 33.5f,
                    temperature = 22.8f,
                    humidity = 86f,
                    lightIntensity = 32f,
                    waterTankLevel = 80f,
                    isSensorOnline = true
                )
                _weatherState.value = _weatherState.value.copy(
                    condition = "Heavy Overcast & Downpour",
                    temperature = 22.8f,
                    humidity = 86f,
                    rainProbability = 88,
                    isRainExpectedSoon = true,
                    forecastSummary = "Rain predicted in 2 hours (88% probability). Smart Irrigation Engine pauses pump."
                )
            }
            DemoScenario.LOW_WATER -> {
                _isSimulatedRaining.value = false
                _sensorData.value = current.copy(
                    soilMoisture = 31.0f,
                    temperature = 29.5f,
                    humidity = 54f,
                    waterTankLevel = 14.0f, // Below 20% critical cutoff threshold
                    isSensorOnline = true
                )
                _weatherState.value = _weatherState.value.copy(
                    condition = "Clear",
                    forecastSummary = "Water tank reserves critically low (14%). Submersible pump safety cutoff engaged."
                )
            }
            DemoScenario.HIGH_TEMP -> {
                _isSimulatedRaining.value = false
                _sensorData.value = current.copy(
                    soilMoisture = 42.0f,
                    temperature = 37.2f, // High heat stress
                    humidity = 36f,
                    lightIntensity = 98f,
                    isSensorOnline = true
                )
                _weatherState.value = _weatherState.value.copy(
                    condition = "Intense Heatwave",
                    temperature = 37.2f,
                    humidity = 36f,
                    rainProbability = 0,
                    isRainExpectedSoon = false,
                    forecastSummary = "Intense heat detected (37.2°C). Soil transpiration accelerating."
                )
            }
            DemoScenario.SENSOR_OFFLINE -> {
                _sensorData.value = current.copy(
                    isSensorOnline = false
                )
                _weatherState.value = _weatherState.value.copy(
                    forecastSummary = "ESP32 sensor telemetry packet timed out. Fault-tolerant safety mode active."
                )
            }
        }
    }

    // Direct developer triggers
    fun simulateDrySoil() = applyScenario(DemoScenario.DRY_SOIL)
    fun simulateRain(isRainingNow: Boolean = true) {
        _isSimulatedRaining.value = isRainingNow
        applyScenario(DemoScenario.RAIN_EXPECTED)
    }
    fun simulateHighTemperature() = applyScenario(DemoScenario.HIGH_TEMP)
    fun simulateLowTank() = applyScenario(DemoScenario.LOW_WATER)
    fun simulateSensorOffline(offline: Boolean = true) {
        if (offline) {
            applyScenario(DemoScenario.SENSOR_OFFLINE)
        } else {
            applyScenario(DemoScenario.NORMAL)
        }
    }
    fun simulateTankRefill() {
        _sensorData.value = _sensorData.value.copy(waterTankLevel = 96.0f)
    }

    fun setCustomTelemetry(
        moisture: Float,
        temp: Float,
        humidity: Float,
        light: Float,
        tank: Float
    ) {
        _sensorData.value = _sensorData.value.copy(
            soilMoisture = moisture.coerceIn(0f, 100f),
            temperature = temp.coerceIn(-10f, 60f),
            humidity = humidity.coerceIn(0f, 100f),
            lightIntensity = light.coerceIn(0f, 100f),
            waterTankLevel = tank.coerceIn(0f, 100f),
            isSensorOnline = true
        )
    }

    /**
     * Executes one continuous simulation tick (called periodically in background).
     * Simulates physics of underground pipe delivery, plant root uptake,
     * rain percolation, and tank water depletion.
     */
    fun tick(isPumpRunning: Boolean): SensorData {
        tickCount++
        val prev = _sensorData.value
        if (!prev.isSensorOnline) return prev

        var moisture = prev.soilMoisture
        var tank = prev.waterTankLevel
        var temp = prev.temperature
        var hum = prev.humidity
        var light = prev.lightIntensity

        // 1. Irrigation pump effect (Underground drip delivers water directly to root depth)
        if (isPumpRunning) {
            moisture = (moisture + 1.25f).coerceAtMost(88f)
            tank = (tank - 0.38f).coerceAtLeast(0f)
        } else {
            // Natural root transpiration & dry down
            moisture = (moisture - 0.12f).coerceAtLeast(14f)
        }

        // 2. Weather & rain precipitation effect
        if (_isSimulatedRaining.value || _weatherState.value.isRainExpectedSoon) {
            // Rain percolating down from surface into root zone
            moisture = (moisture + 0.45f).coerceAtMost(92f)
            hum = (hum + 0.3f).coerceAtMost(98f)
            temp = (temp - 0.1f).coerceAtLeast(18f)
            light = (light - 0.5f).coerceAtLeast(20f)
        }

        // 3. Natural micro-variations & diurnal wave
        val wave = sin(tickCount * 0.08).toFloat() * 0.25f
        temp = (temp + (Random.nextFloat() - 0.48f) * 0.15f + wave * 0.05f).coerceIn(16f, 44f)
        hum = (hum + (Random.nextFloat() - 0.5f) * 0.3f).coerceIn(25f, 98f)
        light = (light + (Random.nextFloat() - 0.5f) * 0.6f).coerceIn(5f, 100f)

        val flowRate = if (isPumpRunning && tank > 0f) {
            14.2f + (Random.nextFloat() * 0.6f)
        } else {
            0f
        }

        val updated = prev.copy(
            soilMoisture = (moisture * 10).toInt() / 10f,
            waterTankLevel = (tank * 10).toInt() / 10f,
            temperature = (temp * 10).toInt() / 10f,
            humidity = (hum * 10).toInt() / 10f,
            lightIntensity = (light * 10).toInt() / 10f,
            pumpActive = isPumpRunning && tank > 0f,
            waterFlowRateLpm = flowRate,
            timestamp = System.currentTimeMillis()
        )

        _sensorData.value = updated
        return updated
    }

    /**
     * Ingestion entry point for actual or simulated ESP32 POST /api/sensors/data
     */
    fun ingestEsp32Payload(payload: Esp32SensorPayload) {
        _sensorData.value = _sensorData.value.copy(
            soilMoisture = payload.soilMoisture,
            temperature = payload.temperature,
            humidity = payload.humidity,
            lightIntensity = payload.light,
            waterTankLevel = payload.waterLevel,
            isSensorOnline = true,
            timestamp = System.currentTimeMillis(),
            deviceId = payload.deviceId
        )
    }

    fun refillTank() {
        _sensorData.value = _sensorData.value.copy(waterTankLevel = 95f)
    }

    private fun createInitialWeather(): WeatherState {
        return WeatherState(
            condition = "Partly Cloudy",
            temperature = 28.5f,
            humidity = 64f,
            rainProbability = 18,
            windSpeedKph = 11.2f,
            uvIndex = 6,
            forecastSummary = "Favorable conditions. Underground soil transpiration is steady.",
            isRainExpectedSoon = false,
            forecastHours = listOf(
                HourlyForecast("Now", 28.5f, 15, "cloud_sun"),
                HourlyForecast("+1h", 29.0f, 18, "sun"),
                HourlyForecast("+2h", 29.5f, 25, "cloud"),
                HourlyForecast("+4h", 28.0f, 35, "cloud"),
                HourlyForecast("+6h", 26.5f, 40, "cloud_rain"),
                HourlyForecast("+12h", 23.0f, 20, "cloud_sun")
            )
        )
    }

    fun generateHistoricalReadings(count: Int = 48): List<SensorData> {
        val list = mutableListOf<SensorData>()
        val now = System.currentTimeMillis()
        val interval = 30 * 60 * 1000L

        var m = 68f
        var t = 26f
        var h = 65f
        var l = 70f
        var w = 85f

        for (i in count downTo 0) {
            val time = now - (i * interval)
            val dayPhase = sin(i.toDouble() * 0.25).toFloat()
            t = 27f + (dayPhase * 5f) + (Random.nextFloat() * 1.5f)
            h = 60f - (dayPhase * 8f) + (Random.nextFloat() * 2f)
            l = (50f + (dayPhase * 35f)).coerceIn(10f, 95f)
            m = (62f + sin(i * 0.4).toFloat() * 12f).coerceIn(32f, 78f)
            w = (82f - (i * 0.15f)).coerceIn(40f, 95f)

            list.add(
                SensorData(
                    soilMoisture = (m * 10).toInt() / 10f,
                    temperature = (t * 10).toInt() / 10f,
                    humidity = (h * 10).toInt() / 10f,
                    lightIntensity = (l * 10).toInt() / 10f,
                    waterTankLevel = (w * 10).toInt() / 10f,
                    pumpActive = m < 38f,
                    timestamp = time
                )
            )
        }
        return list
    }
}
