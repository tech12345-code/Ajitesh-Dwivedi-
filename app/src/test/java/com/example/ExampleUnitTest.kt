package com.example

import com.example.data.models.DemoScenario
import com.example.data.models.IrrigationControlState
import com.example.data.models.SensorData
import com.example.data.models.WeatherState
import com.example.engine.EngineAction
import com.example.engine.SimulationEngine
import com.example.engine.SmartIrrigationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testSimulationEngineScenarioAndTick() {
        val engine = SimulationEngine()

        // Test normal scenario
        engine.applyScenario(DemoScenario.NORMAL)
        assertEquals(65.5f, engine.sensorData.value.soilMoisture, 0.5f)

        // Test dry soil trigger scenario
        engine.applyScenario(DemoScenario.DRY_SOIL)
        assertEquals(26.0f, engine.sensorData.value.soilMoisture, 0.5f)

        // Test pump running increases moisture and decreases tank
        val initialTank = engine.sensorData.value.waterTankLevel
        val initialMoisture = engine.sensorData.value.soilMoisture
        val afterTick = engine.tick(isPumpRunning = true)

        assertTrue(afterTick.soilMoisture > initialMoisture)
        assertTrue(afterTick.waterTankLevel < initialTank)
        assertTrue(afterTick.waterFlowRateLpm > 0f)
    }

    @Test
    fun testSmartIrrigationEngineDrySoilRule() {
        val smartEngine = SmartIrrigationEngine()
        val sensor = SensorData(soilMoisture = 28f, waterTankLevel = 75f)
        val weather = WeatherState(rainProbability = 10)
        val control = IrrigationControlState(isPumpOn = false, moistureThreshold = 35f)

        val actions = smartEngine.evaluate(sensor, weather, control)
        val hasStartPump = actions.any { it is EngineAction.StartPump }
        assertTrue("Pump should start when moisture is below threshold", hasStartPump)
    }

    @Test
    fun testSmartIrrigationEngineRainPauseRule() {
        val smartEngine = SmartIrrigationEngine()
        val sensor = SensorData(soilMoisture = 28f, waterTankLevel = 75f)
        val weather = WeatherState(rainProbability = 85) // Rain imminent
        val control = IrrigationControlState(isPumpOn = false, moistureThreshold = 35f, rainPauseEnabled = true)

        val actions = smartEngine.evaluate(sensor, weather, control)
        val hasWeatherPause = actions.any { it is EngineAction.UpdateWeatherPause && it.paused }
        assertTrue("Weather pause should be engaged when rain probability >= 70%", hasWeatherPause)
    }
}
