package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.data.models.WeatherState
import com.example.data.repository.FarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = FarmRepository(database, viewModelScope)

    // Telemetry & Control states
    val sensorData: StateFlow<SensorData> = repository.sensorData
    val weatherState: StateFlow<WeatherState> = repository.weatherState
    val controlState: StateFlow<IrrigationControlState> = repository.controlState
    val farmProfile: StateFlow<FarmProfile> = repository.farmProfile
    val esp32State: StateFlow<Esp32DeviceState> = repository.esp32State
    val activeScenario: StateFlow<DemoScenario> = repository.activeScenario
    val isSimulatedRaining: StateFlow<Boolean> = repository.isSimulatedRaining
    val toastMessage: StateFlow<String?> = repository.toastEvent

    // Room DB streams
    val alerts: StateFlow<List<AlertEntity>> = repository.alerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadAlertCount: StateFlow<Int> = repository.unreadAlertCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val decisions: StateFlow<List<DecisionEntity>> = repository.decisions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val irrigationEvents: StateFlow<List<IrrigationEventEntity>> = repository.irrigationEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentReadings: StateFlow<List<SensorReadingEntity>> = repository.recentReadings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Auth & Navigation State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _hasSeenLanding = MutableStateFlow(false)
    val hasSeenLanding: StateFlow<Boolean> = _hasSeenLanding.asStateFlow()

    // Developer Simulation Dialog
    private val _showSimDialog = MutableStateFlow(false)
    val showSimDialog: StateFlow<Boolean> = _showSimDialog.asStateFlow()

    // Pump Manual Confirmation Dialog
    private val _showPumpConfirmDialog = MutableStateFlow(false)
    val showPumpConfirmDialog: StateFlow<Boolean> = _showPumpConfirmDialog.asStateFlow()

    // Selected Navigation Tab (0: Dashboard, 1: Monitoring, 2: Irrigation, 3: Weather, 4: Decisions, 5: Alerts, 6: Farm, 7: Devices, 8: Settings)
    private val _currentNavIndex = MutableStateFlow(0)
    val currentNavIndex: StateFlow<Int> = _currentNavIndex.asStateFlow()

    fun setNavIndex(index: Int) {
        _currentNavIndex.value = index
    }

    fun completeLanding() {
        _hasSeenLanding.value = true
    }

    fun loginDemo() {
        _isLoggedIn.value = true
        _userEmail.value = "farmer@smartfarm.io"
        _hasSeenLanding.value = true
    }

    fun loginUser(email: String) {
        _isLoggedIn.value = true
        _userEmail.value = email.ifBlank { "operator@smartfarm.io" }
        _hasSeenLanding.value = true
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    fun openSimulationDialog() {
        _showSimDialog.value = true
    }

    fun closeSimulationDialog() {
        _showSimDialog.value = false
    }

    fun requestPumpToggle() {
        if (!controlState.value.isPumpOn) {
            // Require confirmation before starting pump manually
            _showPumpConfirmDialog.value = true
        } else {
            // Turning pump off can happen immediately
            repository.setPumpManual(false)
        }
    }

    fun confirmPumpStart() {
        _showPumpConfirmDialog.value = false
        repository.setPumpManual(true)
    }

    fun dismissPumpConfirm() {
        _showPumpConfirmDialog.value = false
    }

    fun setIrrigationMode(mode: IrrigationMode) {
        repository.setIrrigationMode(mode)
    }

    fun setMoistureThreshold(threshold: Float) {
        repository.setMoistureThreshold(threshold)
    }

    fun setRainPauseEnabled(enabled: Boolean) {
        repository.setRainPauseEnabled(enabled)
    }

    // Developer controls
    fun applyScenario(scenario: DemoScenario) {
        repository.applyScenario(scenario)
    }

    fun simulateDrySoil() {
        repository.simulateDrySoil()
    }

    fun simulateRain(active: Boolean = true) {
        repository.simulateRain(active)
    }

    fun simulateHighTemperature() {
        repository.simulateHighTemperature()
    }

    fun simulateLowTank() {
        repository.simulateLowTank()
    }

    fun simulateSensorOffline(offline: Boolean = true) {
        repository.simulateSensorOffline(offline)
    }

    fun refillWaterTank() {
        repository.refillWaterTank()
    }

    fun ingestEsp32Payload(payload: Esp32SensorPayload) {
        repository.ingestEsp32Payload(payload)
    }

    fun markAlertRead(alertId: Long) {
        repository.markAlertRead(alertId)
    }

    fun markAllAlertsRead() {
        repository.markAllAlertsRead()
    }

    fun clearAllAlerts() {
        repository.clearAllAlerts()
    }

    fun updateFarmProfile(
        name: String,
        location: String,
        crop: String,
        area: Int,
        plants: Int,
        soil: String
    ) {
        repository.updateFarmProfile(name, location, crop, area, plants, soil)
    }

    fun clearToast() {
        repository.clearToast()
    }
}
