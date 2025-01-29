package com.example.fooddeliveryapp.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
/*import org.osmdroid.bonuspack.location.GeocoderNominatim
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay*/
import org.osmdroid.util.BoundingBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationMapScreen(navController: NavController) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var locationAddress by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }

    // Initialize map
    LaunchedEffect(Unit) {
        Configuration.getInstance()
            .load(context,
                context.getSharedPreferences
                    ("osm_prefs", Context.MODE_PRIVATE))
        mapView = MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK) // Ensure area names are visible
            setMultiTouchControls(true)

            val bahrain = BoundingBox(
                26.417, // North
                50.933, // East
                25.567, // South
                50.267  // West
            )

            setScrollableAreaLimitDouble(bahrain)
            setMinZoomLevel(11.0) // Adjust this value to control minimum zoom
            setMaxZoomLevel(19.0) // Maximum zoom level
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        // Fullscreen MapView
        mapView?.let { map ->
            AndroidView(
                factory = { map },
                modifier = Modifier.fillMaxSize(),
                update = {
                    setupOpenStreetMap(
                        map, context
                    ) { geoPoint ->
                        selectedLocation = geoPoint
                        scope.launch {
                            showSheet = true
                            sheetState.show()
                        }
                    }
                }
            )
        }
    }

    // Modal Bottom Sheet
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false },
            sheetState = sheetState
        ) {
            selectedLocation?.let { location ->
                LocationConfirmationCard(
                    navController = navController,
                    location = location,
                    address = locationAddress ?: "Location name not found"
                )
            }
        }
    }
}

@Composable
fun LocationConfirmationCard(
    navController: NavController,
    location: GeoPoint,
    address: String
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Selected Location: $address")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Coordinates: ${location.latitude}, ${location.longitude}",
                style = MaterialTheme.typography.bodySmall
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
    onLocationSelected: (GeoPoint) -> Unit
) {
    val startPoint = GeoPoint(26.0667, 50.5577) // Default to Bahrain
    mapView.controller.setZoom(15.0)
    mapView.controller.setCenter(startPoint)
    mapView.setBuiltInZoomControls(true)
    mapView.overlays.clear()

    val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
        @SuppressLint("UseCompatLoadingForDrawables")
        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
            mapView.overlays.removeAll { it is Marker }

            val marker = Marker(mapView).apply {
                position = p
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = context.getDrawable(R.drawable.ic_location_pin) // Add custom location icon
                title = "Selected Location"
            }
            mapView.overlays.add(marker)
            mapView.controller.animateTo(p)
            onLocationSelected(p)
            return true
        }

        override fun longPressHelper(p: GeoPoint): Boolean = false
    })

    mapView.overlays.add(mapEventsOverlay)
    mapView.invalidate()
}

// Function to get address from coordinates
/*private suspend fun getAddressFromLocation(geoPoint: GeoPoint): String? {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = GeocoderNominatim("FoodDeliveryApp")
            val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
            if (addresses.isNotEmpty()) {
                addresses[0].getAddressLine(0)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}*/
