package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorDao {
    @Query("SELECT * FROM sensor_readings ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentReadings(limit: Int = 100): Flow<List<SensorReadingEntity>>

    @Query("SELECT * FROM sensor_readings WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    fun getReadingsSince(sinceTimestamp: Long): Flow<List<SensorReadingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: SensorReadingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(readings: List<SensorReadingEntity>)

    @Query("DELETE FROM sensor_readings WHERE timestamp < :cutoffTimestamp")
    suspend fun purgeOldReadings(cutoffTimestamp: Long)
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT COUNT(*) FROM alerts WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity)

    @Query("UPDATE alerts SET isRead = 1 WHERE id = :alertId")
    suspend fun markAsRead(alertId: Long)

    @Query("UPDATE alerts SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM alerts")
    suspend fun clearAll()
}

@Dao
interface DecisionDao {
    @Query("SELECT * FROM decisions ORDER BY timestamp DESC LIMIT :limit")
    fun getDecisions(limit: Int = 50): Flow<List<DecisionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecision(decision: DecisionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(decisions: List<DecisionEntity>)
}

@Dao
interface IrrigationDao {
    @Query("SELECT * FROM irrigation_events ORDER BY timestamp DESC LIMIT :limit")
    fun getEvents(limit: Int = 50): Flow<List<IrrigationEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: IrrigationEventEntity)
}
