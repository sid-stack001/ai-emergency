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

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private val sensorData = mutableStateMapOf<String, String>()
    private val historyData = mutableStateListOf<String>()

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
            val sensorName = it.sensor.name
            val values = it.values.joinToString(", ") { value -> "%.3f".format(value) }
            sensorData[sensorName] = values

            // ✅ Store latest values without slowing UI
            if (sensorName.contains("Accelerometer", ignoreCase = true)) {
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

    ModalNavigationDrawer(
        drawerContent = { DrawerContent(navController, drawerState, coroutineScope) },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Sensor App", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1976D3))
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
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
fun HomeScreen(sensorData: Map<String, String>, onSOSClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onSOSClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier // ✅ Full-screen red SOS button
                .padding(16.dp)
        ) {
            Text(
                "SOS EMERGENCY CALL",
                fontSize = 32.sp, // ✅ Bigger text
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun SensorScreen(sensorData: Map<String, String>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("All Sensor Data", fontSize = 24.sp)
        sensorData.forEach { (sensor, data) ->
            Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "$sensor:", fontSize = 20.sp, color = Color.Blue)
                    Text(text = data, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(historyData: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("History Data", fontSize = 24.sp)
        historyData.forEach { entry ->
            Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = entry, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun DrawerContent(navController: NavHostController, drawerState: DrawerState, coroutineScope: CoroutineScope) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Navigation", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            coroutineScope.launch { drawerState.close() }
            navController.navigate("home")
        }) { Text("Home") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            coroutineScope.launch { drawerState.close() }
            navController.navigate("sensors")
        }) { Text("Sensor Data") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            coroutineScope.launch { drawerState.close() }
            navController.navigate("history")
        }) { Text("History") }
    }
}
