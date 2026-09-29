package com.example.data.models

/**
 * Real-time telemetry snapshot
 */
data class SensorData(
    val soilMoisture: Float = 62f, // 0 - 100%
    val temperature: Float = 28.5f, // Celsius
    val humidity: Float = 64f, // 0 - 100%
    val lightIntensity: Float = 76f, // 0 - 100% (or Lux)
    val waterTankLevel: Float = 78f, // 0 - 100%
    val pumpActive: Boolean = false,
    val waterFlowRateLpm: Float = 0f, // Liters per minute
    val timestamp: Long = System.currentTimeMillis(),
    val isSensorOnline: Boolean = true,
    val deviceId: String = "ESP32-001"
) {
    val moistureStatus: MoistureStatus
        get() = when {
            soilMoisture < 35f -> MoistureStatus.DRY
            soilMoisture > 75f -> MoistureStatus.WET
            else -> MoistureStatus.OPTIMAL
        }

    val tankStatus: TankStatus
        get() = when {
            waterTankLevel < 20f -> TankStatus.CRITICAL_LOW
            waterTankLevel < 40f -> TankStatus.LOW
            else -> TankStatus.NORMAL
        }
}

enum class MoistureStatus(val label: String) {
    DRY("Dry - Needs Water"),
    OPTIMAL("Optimal Zone"),
    WET("Saturated")
}

enum class TankStatus(val label: String) {
    NORMAL("Adequate"),
    LOW("Low Reserve"),
    CRITICAL_LOW("Refill Immediately")
}

/**
 * ESP32 Telemetry JSON payload structure matching POST /api/sensors/data
 */
data class Esp32SensorPayload(
    val deviceId: String,
    val soilMoisture: Float,
    val temperature: Float,
    val humidity: Float,
    val light: Float,
    val waterLevel: Float
)

/**
 * Irrigation modes & controls
 */
enum class IrrigationMode {
    AUTO,
    MANUAL
}

data class IrrigationControlState(
    val mode: IrrigationMode = IrrigationMode.AUTO,
    val isPumpOn: Boolean = false,
    val moistureThreshold: Float = 35f, // Auto trigger below this
    val rainPauseEnabled: Boolean = true,
    val isWeatherPaused: Boolean = false,
    val lastIrrigationTime: Long = System.currentTimeMillis() - 4 * 3600 * 1000L,
    val lastIrrigationDurationSec: Int = 180,
    val totalWaterSavedLiters: Float = 1420f,
    val flowRateLpm: Float = 14.5f
)

/**
 * Weather state & forecast
 */
data class WeatherState(
    val condition: String = "Partly Cloudy",
    val temperature: Float = 28.5f,
    val humidity: Float = 64f,
    val rainProbability: Int = 15, // %
    val windSpeedKph: Float = 12.4f,
    val uvIndex: Int = 6,
    val forecastSummary: String = "Clear conditions expected for the next 6 hours. Underground soil transpiration moderate.",
    val isRainExpectedSoon: Boolean = false,
    val forecastHours: List<HourlyForecast> = emptyList()
)

data class HourlyForecast(
    val timeLabel: String,
    val temp: Float,
    val rainProb: Int,
    val iconName: String
)

/**
 * Autonomous decision from Smart Irrigation Engine
 */
data class SmartDecision(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val icon: String = "🌱",
    val title: String,
    val reason: String,
    val action: String,
    val status: String = "APPLIED" // APPLIED, ACTIVE, PAUSED
)

/**
 * User & Farm details
 */
data class FarmProfile(
    val farmName: String = "Smart Farm Demo",
    val location: String = "Greenfield Agritech Block B",
    val primaryCrop: String = "Tomato (Solanum lycopersicum)",
    val cropStage: String = "Fruiting Stage",
    val rootDepthCm: Int = 25,
    val areaSqMeters: Int = 500,
    val plantCount: Int = 1250,
    val soilType: String = "Loamy Soil (High Drainage)",
    val optimalMoistureMin: Float = 50f,
    val optimalMoistureMax: Float = 75f
)

/**
 * Hardware specs & ESP32 controller state
 */
data class Esp32DeviceState(
    val deviceId: String = "ESP32-001-FARM",
    val hardwareModel: String = "ESP32 NodeMCU v1 (38-pin)",
    val firmwareVersion: String = "v2.4.1-agri-underground",
    val isOnline: Boolean = true,
    val ipAddress: String = "192.168.4.105",
    val macAddress: String = "24:0A:C4:8B:3F:12",
    val wifiRssi: Int = -58, // dBm
    val batterySolarPct: Int = 94,
    val lastHeartbeatSecAgo: Int = 4,
    val connectedSensors: List<SensorHardwareInfo> = listOf(
        SensorHardwareInfo("Capacitive Soil Moisture v1.2", "ADC Pin 34", "Calibrated 3.3V Analog", true),
        SensorHardwareInfo("DHT22 Temp & Humidity", "GPIO Pin 4", "Digital 1-Wire Protocol", true),
        SensorHardwareInfo("LDR Ambient Photocell", "ADC Pin 35", "Analog Divider", true),
        SensorHardwareInfo("Ultrasonic Tank Depth (HC-SR04)", "Trig: 12, Echo: 14", "Time-of-flight (cm)", true),
        SensorHardwareInfo("12V Submersible DC Pump Relay", "GPIO Pin 26", "Optocoupled Active-High Relay", true)
    )
)

data class SensorHardwareInfo(
    val name: String,
    val pin: String,
    val protocol: String,
    val isHealthy: Boolean
)

/**
 * Demo scenarios for live evaluations
 */
enum class DemoScenario(val title: String, val subtitle: String, val badge: String) {
    NORMAL("Normal Farm", "Moisture 68%, Temp 28°C, Healthy crops", "Optimal"),
    DRY_SOIL("Dry Soil Trigger", "Moisture 26% → Triggers Auto Irrigation", "Auto Start"),
    RAIN_EXPECTED("Rain Forecast", "Rain 88% → Pauses irrigation to save water", "Water Saver"),
    LOW_WATER("Low Water Tank", "Tank 14% → Cutoff safety & refill alert", "Safety Cutoff"),
    HIGH_TEMP("High Temperature", "Temp 36°C → Increased monitoring rate", "Heat Stress"),
    SENSOR_OFFLINE("Hardware Fault", "Simulates ESP32 sensor disconnect", "Fault Tolerant")
}
