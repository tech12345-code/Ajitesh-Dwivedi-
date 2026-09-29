package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.DemoScenario
import com.example.data.models.IrrigationMode
import com.example.ui.MainViewModel
import com.example.ui.components.SimulationControlDialog
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DecisionCenterScreen
import com.example.ui.screens.DevicesScreen
import com.example.ui.screens.FarmScreen
import com.example.ui.screens.IrrigationScreen
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MonitoringScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WeatherScreen
import com.example.ui.theme.AgriGreenLight
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.SoilAmber
import com.example.ui.theme.StatusAlert
import com.example.ui.theme.WaterBlueLight
import com.example.ui.theme.WaterBluePrimary

enum class AppNavDestination(val index: Int, val title: String, val icon: ImageVector) {
    DASHBOARD(0, "Dashboard", Icons.Default.Dashboard),
    MONITORING(1, "Monitoring", Icons.Default.ShowChart),
    IRRIGATION(2, "Irrigation", Icons.Default.WaterDrop),
    WEATHER(3, "Weather", Icons.Default.Cloud),
    DECISIONS(4, "Decisions", Icons.Default.AutoAwesome),
    ALERTS(5, "Alerts", Icons.Default.Notifications),
    FARM(6, "My Farm", Icons.Default.Agriculture),
    DEVICES(7, "Devices", Icons.Default.Memory),
    SETTINGS(8, "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val hasSeenLanding by viewModel.hasSeenLanding.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val currentNavIndex by viewModel.currentNavIndex.collectAsStateWithLifecycle()

    val sensorData by viewModel.sensorData.collectAsStateWithLifecycle()
    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()
    val controlState by viewModel.controlState.collectAsStateWithLifecycle()
    val farmProfile by viewModel.farmProfile.collectAsStateWithLifecycle()
    val esp32State by viewModel.esp32State.collectAsStateWithLifecycle()
    val activeScenario by viewModel.activeScenario.collectAsStateWithLifecycle()
    val isSimulatedRaining by viewModel.isSimulatedRaining.collectAsStateWithLifecycle()

    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val unreadAlertCount by viewModel.unreadAlertCount.collectAsStateWithLifecycle()
    val decisions by viewModel.decisions.collectAsStateWithLifecycle()
    val recentReadings by viewModel.recentReadings.collectAsStateWithLifecycle()

    val showSimDialog by viewModel.showSimDialog.collectAsStateWithLifecycle()
    val showPumpConfirm by viewModel.showPumpConfirmDialog.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showMenuDialog by remember { mutableStateOf(false) }

    // Show toast message if emitted
    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    // 1. Landing Screen (First-time / Overview experience)
    if (!hasSeenLanding && !isLoggedIn) {
        LandingScreen(
            onOpenDashboard = { viewModel.loginDemo() },
            onNavigateToLogin = { viewModel.completeLanding() },
            onInstantDemoLogin = { viewModel.loginDemo() }
        )
        return
    }

    // 2. Login Screen
    if (!isLoggedIn) {
        BackHandler {
            // Return to landing
            // Handled via ViewModel state
        }
        LoginScreen(
            onLoginSuccess = { email -> viewModel.loginUser(email) },
            onDemoLogin = { viewModel.loginDemo() },
            onBackToLanding = { /* Toggle landing */ }
        )
        return
    }

    // Back handler for logged in state
    if (currentNavIndex != 0) {
        BackHandler {
            viewModel.setNavIndex(0) // Return to Dashboard
        }
    }

    // 3. Main Authenticated App
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SMART FARM",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = 1.sp
                        )
                        if (controlState.isPumpOn) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(WaterBluePrimary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PUMP ON",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { showMenuDialog = true },
                        modifier = Modifier.testTag("app_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Quick Simulation Lab Trigger
                    IconButton(
                        onClick = { viewModel.openSimulationDialog() },
                        modifier = Modifier.testTag("top_bar_sim_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "Sim Lab",
                            tint = AgriGreenLight
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // Main Bottom Navigation Bar
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                val primaryTabs = listOf(
                    AppNavDestination.DASHBOARD,
                    AppNavDestination.MONITORING,
                    AppNavDestination.IRRIGATION,
                    AppNavDestination.ALERTS,
                    AppNavDestination.FARM
                )

                primaryTabs.forEach { dest ->
                    val isSelected = currentNavIndex == dest.index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setNavIndex(dest.index) },
                        icon = {
                            if (dest == AppNavDestination.ALERTS && unreadAlertCount > 0) {
                                BadgedBox(badge = {
                                    Badge(containerColor = StatusAlert) {
                                        Text("$unreadAlertCount")
                                    }
                                }) {
                                    Icon(imageVector = dest.icon, contentDescription = dest.title)
                                }
                            } else {
                                Icon(imageVector = dest.icon, contentDescription = dest.title)
                            }
                        },
                        label = {
                            Text(
                                text = dest.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AgriGreenLight,
                            selectedTextColor = AgriGreenLight,
                            indicatorColor = AgriGreenPrimary.copy(alpha = 0.2f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentNavIndex) {
                0 -> DashboardScreen(
                    sensorData = sensorData,
                    weatherState = weatherState,
                    controlState = controlState,
                    farmProfile = farmProfile,
                    activeScenario = activeScenario,
                    unreadAlertCount = unreadAlertCount,
                    recentDecisions = decisions,
                    onOpenSimulationDialog = { viewModel.openSimulationDialog() },
                    onRequestPumpToggle = { viewModel.requestPumpToggle() },
                    onNavigateToAlerts = { viewModel.setNavIndex(5) },
                    onNavigateToMonitoring = { viewModel.setNavIndex(1) },
                    onNavigateToIrrigation = { viewModel.setNavIndex(2) },
                    onNavigateToDecisions = { viewModel.setNavIndex(4) }
                )
                1 -> MonitoringScreen(
                    sensorData = sensorData,
                    recentReadings = recentReadings
                )
                2 -> IrrigationScreen(
                    sensorData = sensorData,
                    controlState = controlState,
                    showConfirmDialog = showPumpConfirm,
                    onSetMode = { viewModel.setIrrigationMode(it) },
                    onRequestPumpToggle = { viewModel.requestPumpToggle() },
                    onConfirmPumpStart = { viewModel.confirmPumpStart() },
                    onDismissConfirmDialog = { viewModel.dismissPumpConfirm() },
                    onSetMoistureThreshold = { viewModel.setMoistureThreshold(it) },
                    onRefillTank = { viewModel.refillWaterTank() }
                )
                3 -> WeatherScreen(
                    weatherState = weatherState,
                    controlState = controlState,
                    onToggleRainPause = { viewModel.setRainPauseEnabled(it) }
                )
                4 -> DecisionCenterScreen(
                    decisions = decisions
                )
                5 -> AlertsScreen(
                    alerts = alerts,
                    onMarkRead = { viewModel.markAlertRead(it) },
                    onMarkAllRead = { viewModel.markAllAlertsRead() },
                    onClearAll = { viewModel.clearAllAlerts() }
                )
                6 -> FarmScreen(
                    farmProfile = farmProfile,
                    onUpdateProfile = { name, loc, crop, area, plants, soil ->
                        viewModel.updateFarmProfile(name, loc, crop, area, plants, soil)
                    }
                )
                7 -> DevicesScreen(
                    esp32State = esp32State,
                    sensorData = sensorData,
                    onSendTestPayload = { viewModel.ingestEsp32Payload(it) }
                )
                8 -> SettingsScreen(
                    userEmail = userEmail,
                    farmProfile = farmProfile,
                    controlState = controlState,
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = onToggleDarkTheme,
                    onToggleAutoMode = { enabled ->
                        viewModel.setIrrigationMode(if (enabled) IrrigationMode.AUTO else IrrigationMode.MANUAL)
                    },
                    onToggleWeatherPause = { viewModel.setRainPauseEnabled(it) },
                    onSetMoistureThreshold = { viewModel.setMoistureThreshold(it) },
                    onResetSimulation = { viewModel.applyScenario(DemoScenario.NORMAL) },
                    onLogout = { viewModel.logout() }
                )
            }
        }
    }

    // Quick Menu Sheet Dialog (gives 1-tap access to all 9 pages)
    if (showMenuDialog) {
        Dialog(onDismissRequest = { showMenuDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Smart Farm Navigation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showMenuDialog = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    AppNavDestination.values().forEach { dest ->
                        val isSel = currentNavIndex == dest.index
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.setNavIndex(dest.index)
                                    showMenuDialog = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel)
                                    AgriGreenPrimary.copy(alpha = 0.2f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = dest.icon,
                                    contentDescription = dest.title,
                                    tint = if (isSel) AgriGreenLight else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = dest.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) AgriGreenLight else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Developer Simulation Lab Dialog
    if (showSimDialog) {
        SimulationControlDialog(
            activeScenario = activeScenario,
            isRaining = isSimulatedRaining,
            onDismiss = { viewModel.closeSimulationDialog() },
            onSelectScenario = { scenario -> viewModel.applyScenario(scenario) },
            onSimulateDrySoil = { viewModel.simulateDrySoil() },
            onSimulateRain = { raining -> viewModel.simulateRain(raining) },
            onSimulateHighTemp = { viewModel.simulateHighTemperature() },
            onSimulateLowTank = { viewModel.simulateLowTank() },
            onSimulateOffline = { offline -> viewModel.simulateSensorOffline(offline) },
            onRefillTank = { viewModel.refillWaterTank() }
        )
    }
}
