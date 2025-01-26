package com.example.fooddeliveryapp.ui.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import org.osmdroid.config.Configuration as OsmConfiguration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.withContext
import android.location.Location

@Composable
fun LocationSelectionScreen(navController: NavController) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Select Your Location",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
                navController.navigate("locationMapScreen")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Filled.LocationOn, contentDescription = "Enable Location")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Enable Device Location")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                navController.navigate("manualLocationSetup")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Set Up Location Manually")
        }
    }
}

@Composable
fun LocationMapScreen(navController: NavController) {
    val context = LocalContext.current
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var currentAddress by remember { mutableStateOf("Fetching location...") }

    // Initialize OpenStreetMap configuration
    LaunchedEffect(Unit) {
        OsmConfiguration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
    }

    // Fetch current location
    LaunchedEffect(Unit) {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            // Check permissions before requesting location
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                val locationResult = withContext(Dispatchers.IO) {
                    val task: Task<Location> = fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null
                    )

                    try {
                        val location = task.await()
                        location
                    } catch (e: Exception) {
                        null
                    }
                }

                locationResult?.let { location ->
                    val geoPoint = GeoPoint(location.latitude, location.longitude)
                    currentLocation = geoPoint

                    // Geocode the address
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    currentAddress = addresses?.firstOrNull()?.getAddressLine(0) ?: "Location detected"
                }
            }
        } catch (e: Exception) {
            currentAddress = "Error fetching location"
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // OpenStreetMap View
        currentLocation?.let { location ->
            AndroidView(
                factory = { MapView(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                update = { mapView ->
                    mapView.setMultiTouchControls(true)
                    mapView.controller.setCenter(location)
                    mapView.controller.setZoom(15.0)

                    // Add marker
                    val marker = Marker(mapView)
                    marker.position = location
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(marker)
                }
            )
        }

        // Location Details Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentAddress,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        navController.navigate("homeScreen") {
                            popUpTo("homeScreen") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirm Location")
                }
            }
        }
    }
}