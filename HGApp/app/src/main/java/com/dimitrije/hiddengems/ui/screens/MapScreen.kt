package com.dimitrije.hiddengems.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dimitrije.hiddengems.viewmodel.LocationViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.dimitrije.hiddengems.viewmodel.GemViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.ui.Alignment
import androidx.navigation.NavHostController
import com.dimitrije.hiddengems.navigation.AppRoutes

@Composable
fun MapScreen(navController: NavHostController)
{
    val context = LocalContext.current
    val activity = remember { context as? Activity }
    val locationViewModel: LocationViewModel = viewModel()
    val location by locationViewModel.location.collectAsState()
    val gemViewModel: GemViewModel = viewModel()
    val userGems by gemViewModel.userGems.collectAsState()

    val fineGranted = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        fineGranted.value = granted
        if (granted) {
            locationViewModel.startTracking()
        }
    }

    LaunchedEffect(fineGranted.value) {
        if (fineGranted.value) {
            gemViewModel.loadUserGems()
        }
    }

    if (!fineGranted.value) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Location permission is required to show the map and your position.")
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = {
                permissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }) {
                Text("Grant location permission")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }) {
                Text("Open app settings")
            }
        }

    } else {

        LaunchedEffect(Unit) {
            locationViewModel.startTracking()
        }
        DisposableEffect(Unit) {
            onDispose {
                locationViewModel.stopTracking()
            }
        }

        val userLatLng = location?.let { LatLng(it.latitude, it.longitude) }

        val initialLatLng = userLatLng ?: LatLng(45.2517, 19.8369)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(initialLatLng, 15f)
        }

        LaunchedEffect(userLatLng) {
            userLatLng?.let { latLng ->
                try {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                } catch (_: Exception) {
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true),
                uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = true)
            ) {
                userLatLng?.let {
                    Marker(state = MarkerState(position = it), title = "You are here")
                }
                userGems.forEach { gem ->
                    Marker(
                        state = MarkerState(position = LatLng(gem.lat, gem.lng)),
                        title = gem.title,
                        snippet = gem.description
                    )
                }
            }
            FloatingActionButton(
                onClick = { navController.navigate(AppRoutes.AddGem) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 96.dp, end = 16.dp)

            ) {
                Icon(Icons.Default.AddLocationAlt, contentDescription = "Add Gem")
            }
        }
    }
}