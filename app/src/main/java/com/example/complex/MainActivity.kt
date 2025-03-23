package com.example.bitsapp

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Sensors
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private val sensorData = mutableStateMapOf<String, String>()
    private val historyData = mutableStateListOf<String>()

    // Add permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            directCallEmergencyNumber()
        } else {
            // Fallback to dialer if permission denied
            openPhoneDialer()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        setContent {
            AppNavigation(sensorData, historyData) { callEmergencyNumber() }
            startHistoryLogging()
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.getSensorList(Sensor.TYPE_ALL).forEach { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private var lastAccelValue: String = "No data"


    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val sensorType = when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> "Accelerometer"
                Sensor.TYPE_GYROSCOPE -> "Gyroscope"
                else -> it.sensor.name
            }
            
            val values = it.values.joinToString(", ") { value -> "%.3f".format(value) }
            sensorData[sensorType] = values

            // Store latest values
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                lastAccelValue = values
            }
        }
    }

    // ✅ Logs history every 10 sec but uses latest stored values
    @Composable
    private fun startHistoryLogging() {
        LaunchedEffect(Unit) {
            while (true) {
                delay(10000)
                historyData.add("Accelerometer: $lastAccelValue ")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun callEmergencyNumber() {
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            directCallEmergencyNumber()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    private fun directCallEmergencyNumber() {
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:8317540116")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to dialer if something goes wrong
            openPhoneDialer()
        }
    }

    private fun openPhoneDialer() {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:8317540116")
        }
        startActivity(intent)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(sensorData: Map<String, String>, historyData: List<String>, onSOSClick: () -> Unit) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: "home"

    ModalNavigationDrawer(
        drawerContent = { 
            DrawerContent(navController, drawerState, coroutineScope, currentRoute) 
        },
        drawerState = drawerState,
        gesturesEnabled = false,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text("Emergency Response",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold
                        ) 
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                Icons.Filled.Menu, 
                                contentDescription = "Menu", 
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF06796E),
                        actionIconContentColor = Color.White
                    ),
                    modifier = Modifier
                        .shadow(8.dp)
                        .background(MaterialTheme.colorScheme.background)
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                NavHost(navController, startDestination = "home") {
                    composable("home") { HomeScreen(sensorData, onSOSClick) }
                    composable("sensors") { SensorScreen(sensorData) }
                    composable("history") { HistoryScreen(historyData) }
                }
            }
        }
    }
}

@Composable
fun GradientBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F2027),  // Dark blue
                        Color(0xFF203A43),  // Medium blue
                        Color(0xFF2C5364)   // Light blue
                    ),
                    startY = 0f,
                    endY = 2000f,  // Increased from 1000f
                    tileMode = TileMode.Mirror  // Changed from Repeated
                )
            )
    ) {
        content()
    }
}

@Composable
fun HomeScreen(sensorData: Map<String, String>, onSOSClick: () -> Unit) {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sensor Data Display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Get accelerometer values
                val accelValues = sensorData["Accelerometer"]?.split(", ")?.mapNotNull { it.toFloatOrNull() }
                val magnitude = remember(accelValues) {
                    derivedStateOf {
                        accelValues?.let {
                            if (it.size == 3) {
                                Math.sqrt(
                                    it[0].toDouble().pow(2) +
                                    it[1].toDouble().pow(2) +
                                    it[2].toDouble().pow(2)
                                )
                            } else 0.0
                        } ?: 0.0
                    }
                }

                // Accelerometer display with conditional color
                Text(
                    text = "Accelerometer: ${sensorData["Accelerometer"] ?: "Waiting for data..."} m/s²",
                    color = if (magnitude.value > 15.0) Color.Red else Color.White ,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(4.dp)
                )
                
                // Always show gyroscope section
                Text(
                    text = "Gyroscope: ${sensorData["Gyroscope"] ?: "Waiting for data..."} rad/s",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(4.dp)
                )
            }

            // SOS Button
            Button(
                onClick = onSOSClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier
                    .padding(16.dp)
                    .height(80.dp)
                    .width(300.dp)
            ) {
                Text(
                    "SOS EMERGENCY CALL",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SensorScreen(sensorData: Map<String, String>) {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("All Sensor Data", 
                fontSize = 24.sp,
                color = Color.White,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            sensorData.forEach { (sensor, data) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.15f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "$sensor:", 
                            fontSize = 20.sp, 
                            color = Color.White
                        )
                        Text(
                            text = data, 
                            fontSize = 18.sp, 
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(historyData: List<String>) {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("History Data", 
                fontSize = 24.sp,
                color = Color.White,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            historyData.reversed().forEach { entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.15f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = entry, 
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerContent(
    navController: NavHostController,
    drawerState: DrawerState,
    coroutineScope: CoroutineScope,
    currentRoute: String
) {
    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Navigation Menu", 
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        val navItems = listOf(
            "home" to Icons.Filled.Home,
            "sensors" to Icons.Filled.Sensors,
            "history" to Icons.Filled.History
        )

        navItems.forEach { (route, icon) ->
            NavigationDrawerItem(
                icon = { Icon(icon, contentDescription = null) },
                label = { 
                    Text(
                        text = route.replaceFirstChar { it.titlecase() },
                        style = MaterialTheme.typography.bodyLarge
                    ) 
                },
                selected = currentRoute == route,
                onClick = {
                    coroutineScope.launch {
                        // First close the drawer
                        drawerState.close()
                        drawerState.close()
                        // Then navigate after a small delay
                        delay(50) // Allows drawer animation to start
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                modifier = Modifier.padding(vertical = 4.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedContainerColor = Color.Transparent.copy(alpha = 0.2f)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Spacer(modifier = Modifier.weight(0.1f))
    }
}
