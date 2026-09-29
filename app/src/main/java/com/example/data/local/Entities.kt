package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_readings")
data class SensorReadingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val soilMoisture: Float,
    val temperature: Float,
    val humidity: Float,
    val light: Float,
    val waterLevel: Float,
    val pumpStatus: Boolean
)

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val type: String, // "MOISTURE_LOW", "TEMP_HIGH", "TANK_LOW", "RAIN_PAUSE", "PUMP_TIMEOUT", "SENSOR_OFFLINE"
    val severity: String, // "INFO", "WARNING", "CRITICAL"
    val title: String,
    val message: String,
    val isRead: Boolean = false
)

@Entity(tableName = "decisions")
data class DecisionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val ruleType: String,
    val reason: String,
    val action: String,
    val status: String // "APPLIED", "ACTIVE", "RESOLVED"
)

@Entity(tableName = "irrigation_events")
data class IrrigationEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val durationSeconds: Int,
    val waterVolumeLiters: Float,
    val triggerType: String, // "AUTO", "MANUAL", "WEATHER_DELAY"
    val reason: String
)
