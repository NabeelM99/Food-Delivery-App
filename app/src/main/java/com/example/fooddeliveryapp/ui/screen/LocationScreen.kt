package com.example.fooddeliveryapp.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.fooddeliveryapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.BoundingBox
import java.util.Locale

data class LocationDetails(
    val geoPoint: GeoPoint,
    val address: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationMapScreen(navController: NavController) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }
    var searchQuery by remember { mutableStateOf("") }

    // Initialize map
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context,
            context.getSharedPreferences("osm_prefs",
                Context.MODE_PRIVATE))
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
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            selectedLocation?.let { location ->
                LocationConfirmationCard(
                    navController = navController,
                    locationDetails = location
                )
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search for a location...") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    scope.launch {
                        searchLocation(
                            mapView,
                            context,
                            searchQuery,
                            geocoder
                        ){ geoPoint ->
                            selectedLocation = geoPoint
                        }
                    }
                }
            )
        )

    Box(modifier = Modifier.fillMaxSize()) {
        mapView?.let { map ->
            AndroidView(
                factory = { map },
                modifier = Modifier.fillMaxSize(),
                update = {
                    setupOpenStreetMap(
                        map,
                        context,
                        geocoder
                    ) {
                        locationDetails ->
                        selectedLocation = locationDetails.geoPoint
                        showSheet = true
                        scope.launch { sheetState.show()
                        }
                    }
                }
            )
        }
    }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun LocationConfirmationCard(
    navController: NavController,
    locationDetails: LocationDetails
) {
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
                locationDetails.address,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Latitude: ${String.format("%.6f", locationDetails.geoPoint.latitude)}\n" +
                        "Longitude: ${String.format("%.6f", locationDetails.geoPoint.longitude)}",
                style = MaterialTheme.typography.bodyMedium
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

private fun setupOpenStreetMap(
    mapView: MapView,
    context: Context,
    geocoder: Geocoder,
    onLocationSelected: (LocationDetails) -> Unit
) {
    val startPoint = GeoPoint(26.221514, 50.580924) // Bahrain center
    mapView.controller.setZoom(13.0)
    mapView.controller.setCenter(startPoint)
    mapView.setBuiltInZoomControls(true)
    mapView.overlays.clear()

    val mapEventsOverlay = MapEventsOverlay(
        object : MapEventsReceiver {
        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
            mapView.overlays.removeAll { it is Marker }

            // Create and configure the marker with a larger size
            val marker = Marker(mapView).apply {
                position = p
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = ContextCompat.getDrawable(context, R.drawable.ic_location_pin)?.apply {
                }
                title = "Selected Location"
            }
            mapView.overlays.add(marker)
            mapView.controller.animateTo(p)

            // Get address using Geocoder
            try {
                val addresses = geocoder.getFromLocation(p.latitude, p.longitude, 1)
                val address = addresses?.firstOrNull()?.let { addr ->
                    buildString {
                        append(addr.getAddressLine(0) ?: "")
                        if (addr.locality != null) {
                            append(", ${addr.locality}")
                        }
                        if (addr.countryName != null) {
                            append(", ${addr.countryName}")
                        }
                    }
                } ?: "Unknown location"

                onLocationSelected(LocationDetails(p, address))
            } catch (e: Exception) {
                onLocationSelected(LocationDetails(p, "Location details not available"))
            }
            return true
        }

        override fun longPressHelper(p: GeoPoint): Boolean = false
    })

    mapView.overlays.add(mapEventsOverlay)
    mapView.invalidate()
}

private suspend fun searchLocation(
    mapView: MapView?,
    context: Context,
    locationName: String,
    geocoder: Geocoder,
    onLocationFound: (GeoPoint) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val addresses = geocoder.getFromLocationName(locationName, 1)
            val address = addresses?.firstOrNull()
            if (address != null) {
                val geoPoint = GeoPoint(address.latitude, address.longitude)
                withContext(Dispatchers.Main) {
                    mapView?.controller?.setZoom(15.0)
                    mapView?.controller?.animateTo(geoPoint)

                    // Clear existing markers
                    mapView?.overlays?.removeAll { it is Marker }

                    // Add new marker
                    val marker = Marker(mapView).apply {
                        position = geoPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = ContextCompat.getDrawable(context, R.drawable.ic_location_pin)
                        title = locationName
                    }
                    mapView?.overlays?.add(marker)
                    mapView?.invalidate()

                    onLocationFound(geoPoint)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}