package com.example.fooddeliveryapp.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fooddeliveryapp.ProfileViewModel
import com.example.fooddeliveryapp.R
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Locale
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

data class LocationDetails(
    val geoPoint: GeoPoint,
    val address: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationMapScreen(
    navController: NavController,
    onLocationSelected: (String) -> Unit,
    profileViewModel: ProfileViewModel = viewModel(),
    source: String? = "profileEdit"
) {
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.apply {
            remove<String>("selectedDeliveryAddress")
        }
    }

    val source = navController.previousBackStackEntry
        ?.arguments
        ?.getString("source")

    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var selectedLocation by remember { mutableStateOf<LocationDetails?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }
    var searchQuery by remember { mutableStateOf("") }

    // Location permission states
    var showLocationPermissionDialog by remember { mutableStateOf(false) }
    var showLocationSettingsDialog by remember { mutableStateOf(false) }

    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            checkLocationSettingsAndGetLocation(context) { isEnabled ->
                if (!isEnabled) {
                    showLocationSettingsDialog = true
                } else {
                    scope.launch {
                        getCurrentLocation(context, mapView) { location ->
                            location?.let { locationDetails ->
                                selectedLocation = locationDetails
                                showSheet = true
                                scope.launch { sheetState.show() }
                            }
                        }
                    }
                }
            }
        } else {
            showLocationPermissionDialog = true
        }
    }

    LaunchedEffect(Unit) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.remove<String>("selectedDeliveryAddress")
    }

    // Initialize map
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE))
        mapView = MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)

            val bahrain = BoundingBox(
                26.417, // North
                50.933, // East
                25.567, // South
                50.267  // West
            )
            setScrollableAreaLimitDouble(bahrain)
            setMinZoomLevel(11.0)
            setMaxZoomLevel(19.0)

            isTilesScaledToDpi = true
            setZoomRounding(true)
            isHorizontalMapRepetitionEnabled = false
            isVerticalMapRepetitionEnabled = false
        }

        // Check location permission
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                checkLocationSettingsAndGetLocation(context) { isEnabled ->
                    if (!isEnabled) {
                        showLocationSettingsDialog = true
                    }
                }
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // Location Permission Dialog
    if (showLocationPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showLocationPermissionDialog = false },
            title = { Text("Location Permission Required") },
            text = { Text("This app needs location permission to show your current location. Please grant the permission in Settings.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLocationPermissionDialog = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Location Settings Dialog
    if (showLocationSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showLocationSettingsDialog = false },
            title = { Text("Location Services Required") },
            text = { Text("Please enable location services to use your current location.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLocationSettingsDialog = false
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                ) {
                    Text("Turn On")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Bottom Sheet
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            selectedLocation?.let { location ->
                LocationConfirmationCard(
                    navController = navController,
                    locationDetails = location,
                    onConfirm = {
                        // Update both ViewModel and Firestore
                        onLocationSelected(location.address)
                    }
                )
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        // Map at the bottom layer
        mapView?.let { map ->
            AndroidView(
                factory = { map },
                modifier = Modifier.fillMaxSize(),
                update = {
                    setupOpenStreetMap(map, context, geocoder) { locationDetails ->
                        selectedLocation = locationDetails
                        showSheet = true
                        scope.launch { sheetState.show() }
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search for a location...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        scope.launch {
                            searchLocation(mapView, context, searchQuery, geocoder) { locationDetails ->
                                selectedLocation = locationDetails
                                showSheet = true
                                scope.launch { sheetState.show() }
                            }
                        }
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFFFFA500),
                    unfocusedIndicatorColor = Color(0xFFFFA500),
                    focusedContainerColor = Color(0xFFF8F8F8),
                    unfocusedContainerColor = Color(0xFFF8F8F8),
                    cursorColor = Color(0xFFFFA500),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                shape = MaterialTheme.shapes.medium
            )

            // Current Location Button
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    selectedLocation?.let { location ->
                        onLocationSelected(location.address)
                        navController.popBackStack( route = "profileEdit", inclusive = false )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
            ) {
                Text("Confirm Location")
            }
        }
    }

}

@SuppressLint("DefaultLocale")
@Composable
fun LocationConfirmationCard(
    navController: NavController,
    locationDetails: LocationDetails,
    onConfirm: () -> Unit
) {
    val source = navController.previousBackStackEntry
        ?.arguments
        ?.getString("source") ?: "profileEdit"

    val safeAddress = locationDetails.address.ifEmpty {
        "Selected Location (${String.format("%.6f", locationDetails.geoPoint.latitude)}, " +
                "${String.format("%.6f", locationDetails.geoPoint.longitude)})"
    }

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
                safeAddress,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            /*Text(
                "Latitude: ${String.format("%.6f", locationDetails.geoPoint.latitude)}\n" +
                        "Longitude: ${String.format("%.6f", locationDetails.geoPoint.longitude)}",
                style = MaterialTheme.typography.bodyMedium
            )*/
            Text(
                text = locationDetails.address,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    when (source) {
                        "checkout" -> {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("selectedDeliveryAddress", safeAddress)
                        }
                        else -> {
                            onConfirm()
                        }
                    }
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
            ) {
                Text("Confirm Location")
            }
        }
    }
}



private fun setupOpenStreetMap(
    mapView: MapView,
    context: Context,
    geocoder: Geocoder,
    onLocationSelected: (LocationDetails) -> Unit
) {
    val startPoint = GeoPoint(26.221514, 50.580924) // Bahrain center
    mapView.controller.setZoom(13.0)
    mapView.controller.setCenter(startPoint)
    mapView.zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
    mapView.overlays.clear()

    // Create a draggable marker
    val marker = Marker(mapView).apply {
        position = startPoint
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        icon = ContextCompat.getDrawable(context, R.drawable.ic_location_pin)?.apply {
            setBounds(0, 0, 80, 80)
        }
        isDraggable = true
        title = "Drag to select location"
    }

    // Handle marker drag events
    marker.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
        override fun onMarkerDrag(marker: Marker) {
            mapView.invalidate()
        }

        override fun onMarkerDragEnd(marker: Marker) {
            val newPosition = marker.position
            getAddressFromLocation(geocoder, newPosition.latitude, newPosition.longitude) { address ->
                onLocationSelected(LocationDetails(newPosition, address))
            }
        }

        override fun onMarkerDragStart(marker: Marker) {
            // Optional: Handle drag start
        }
    })

    // Add the marker to the map
    mapView.overlays.add(marker)

    // Update marker position on map tap
    val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
            marker.position = p
            mapView.controller.animateTo(p)

            getAddressFromLocation(geocoder, p.latitude, p.longitude) { address ->
                onLocationSelected(LocationDetails(p, address))
            }
            return true
        }

        override fun longPressHelper(p: GeoPoint): Boolean = false
    })

    mapView.overlays.add(mapEventsOverlay)
    mapView.invalidate()
}

private fun getAddressFromLocation(
    geocoder: Geocoder,
    latitude: Double,
    longitude: Double,
    callback: (String) -> Unit
) {
    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                val address = formatAddress(addresses.firstOrNull())
                callback(address)
            }
        } else {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val address = formatAddress(addresses?.firstOrNull())
            callback(address)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        callback("Location details not available")
    }
}

private fun formatAddress(address: android.location.Address?): String {
    return address?.let { addr ->
        buildString {
            append(addr.getAddressLine(0) ?: "")
            if (addr.locality != null && !addr.getAddressLine(0)?.contains(addr.locality)!!) {
                append(", ${addr.locality}")
            }
            if (addr.countryName != null && !addr.getAddressLine(0)?.contains(addr.countryName)!!) {
                append(", ${addr.countryName}")
            }
        }
    } ?: "Unknown location"
}

private fun checkLocationSettingsAndGetLocation(context: Context, callback: (Boolean) -> Unit) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    callback(isLocationEnabled)
}

private object BahrainBounds {
    const val NORTH = 26.417
    const val SOUTH = 25.567
    const val EAST = 50.933
    const val WEST = 50.267
}

private fun isLocationInBahrain(latitude: Double, longitude: Double): Boolean {
    return latitude in BahrainBounds.SOUTH..BahrainBounds.NORTH &&
            longitude in BahrainBounds.WEST..BahrainBounds.EAST
}

@SuppressLint("MissingPermission")
private suspend fun getCurrentLocation(
    context: Context,
    mapView: MapView?,
    onLocationFound: (LocationDetails?) -> Unit
) {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        onLocationFound(null)
        return
    }

    try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // Request fresh location
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(0)
            .setMaxUpdates(1)
            .build()

        withContext(Dispatchers.Main) {
            fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        //val geoPoint = GeoPoint(location.latitude, location.longitude)
                        // Check if location is within Bahrain
                        if (isLocationInBahrain(location.latitude, location.longitude)) {
                            val geoPoint = GeoPoint(location.latitude, location.longitude)

                            mapView?.controller?.setZoom(15.0)
                            mapView?.controller?.animateTo(geoPoint)

                            mapView?.overlays?.removeAll { it is Marker }
                            val marker = Marker(mapView).apply {
                                position = geoPoint
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                icon =
                                    ContextCompat.getDrawable(context, R.drawable.ic_location_pin)
                                        ?.apply {
                                            setBounds(0, 0, 80, 80)
                                        }
                                title = "Current Location"
                            }
                            mapView?.overlays?.add(marker)
                            mapView?.invalidate()

                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses =
                                geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            val address =
                                addresses?.firstOrNull()?.getAddressLine(0) ?: "Current Location"

                            onLocationFound(LocationDetails(geoPoint, address))
                        } else {
                            // If location is outside Bahrain, center on Bahrain
                            val defaultBahrainLocation =
                                GeoPoint(26.0667, 50.5577) // Manama coordinates
                            val locationDetails = LocationDetails(
                                defaultBahrainLocation,
                                "Default Location in Bahrain"
                            )
                            mapView?.controller?.setZoom(11.0)
                            mapView?.controller?.animateTo(defaultBahrainLocation)
                            onLocationFound(locationDetails)
                        }
                    }
                    fusedLocationClient.removeLocationUpdates(this)

                }
            }, Looper.getMainLooper())
        }
    } catch (e: Exception) {
        e.printStackTrace()
        // If there's an error, center on Bahrain
        val defaultBahrainLocation = GeoPoint(26.0667, 50.5577)
        onLocationFound(LocationDetails(defaultBahrainLocation, "Default Location in Bahrain"))
    }
}


@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun searchLocation(
    mapView: MapView?,
    context: Context,
    locationName: String,
    geocoder: Geocoder,
    onLocationFound: (LocationDetails) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            // Handle different API levels for Geocoder
            val addresses = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocationName("$locationName, Bahrain", 1) { addresses ->
                        continuation.resume(addresses) { }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName("$locationName, Bahrain", 1)
            }

            val address = addresses?.firstOrNull()
            if (address != null && isLocationInBahrain(address.latitude, address.longitude)) {
                val geoPoint = GeoPoint(address.latitude, address.longitude)
                withContext(Dispatchers.Main) {
                    mapView?.controller?.setZoom(15.0)
                    mapView?.controller?.animateTo(geoPoint)

                    mapView?.overlays?.removeAll { it is Marker }

                    val marker = Marker(mapView).apply {
                        position = geoPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = ContextCompat.getDrawable(context, R.drawable.ic_location_pin)
                        title = locationName
                    }
                    mapView?.overlays?.add(marker)
                    mapView?.invalidate()

                    val locationDetails = LocationDetails(
                        geoPoint,
                        address.getAddressLine(0) ?:"Unknown location"
                    )
                    onLocationFound(locationDetails)
                }

            }else {
                // If location is outside Bahrain or not found
                withContext(Dispatchers.Main) {
                    val defaultBahrainLocation = GeoPoint(26.0667, 50.5577)
                    onLocationFound(LocationDetails(defaultBahrainLocation, "Location not found in Bahrain"))
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                val defaultBahrainLocation = GeoPoint(26.0667, 50.5577)
                onLocationFound(LocationDetails(defaultBahrainLocation, "Error finding location"))
            }
        }
    }
}