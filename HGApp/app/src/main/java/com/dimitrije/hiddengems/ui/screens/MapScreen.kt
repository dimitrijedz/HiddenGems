package com.dimitrije.hiddengems.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dimitrije.hiddengems.viewmodel.LocationViewModel
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.Manifest


@Composable
fun MapScreen() {
    val locationViewModel: LocationViewModel = viewModel()
    val location by locationViewModel.location.collectAsState()

    LaunchedEffect(Unit) {
        locationViewModel.startTracking()
    }

    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1001
            )
        }
    }

    val userLatLng = location?.let { LatLng(it.latitude, it.longitude) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (userLatLng != null) {
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true),
                uiSettings = MapUiSettings(zoomControlsEnabled = true)
            ) {
                Marker(
                    state = MarkerState(position = userLatLng),
                    title = "You are here"
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Loading location...", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }
        }
    }
}