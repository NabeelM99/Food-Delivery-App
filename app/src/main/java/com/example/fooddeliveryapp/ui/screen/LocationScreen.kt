package com.example.fooddeliveryapp.ui.screen

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationMapScreen(navController: NavController) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }

    // Initialize map
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE))
        mapView = MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK) // Ensure area names are visible
            setMultiTouchControls(true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fullscreen MapView
        mapView?.let { map ->
            AndroidView(
                factory = { map },
                modifier = Modifier.fillMaxSize(),
                update = {
                    setupOpenStreetMap(map, context) { geoPoint ->
                        selectedLocation = geoPoint
                        showSheet = true
                        scope.launch { sheetState.show() }
                    }
                }
            )
        }
    }

    // Modal Bottom Sheet
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            selectedLocation?.let { location ->
                LocationConfirmationCard(navController, location)
            }
        }
    }
}

@Composable
fun LocationConfirmationCard(navController: NavController, location: GeoPoint) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Selected Location: ${location.latitude}, ${location.longitude}")

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
        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
            mapView.overlays.removeAll { it is Marker }

            val marker = Marker(mapView)
            marker.position = p
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
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
