package com.dimitrije.hiddengems.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.dimitrije.hiddengems.model.Gem
import com.dimitrije.hiddengems.navigation.AppRoutes
import com.dimitrije.hiddengems.service.LocationService
import com.dimitrije.hiddengems.service.LocationTracker
import com.dimitrije.hiddengems.viewmodel.LocationViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun MapScreen(navController: NavHostController) {
    val context = LocalContext.current
    val locationViewModel: LocationViewModel = viewModel()
    val location by locationViewModel.location.collectAsState()

    val fineGranted = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val notificationsGranted = remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationOk = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val notificationsOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            permissions[Manifest.permission.POST_NOTIFICATIONS] == true
        else true

        fineGranted.value = locationOk
        notificationsGranted.value = notificationsOk

        if (locationOk) {
            locationViewModel.startTracking()
        }
    }

    val gemGroups = remember { mutableStateOf<Map<String, List<Gem>>>(emptyMap()) }
    val locationTracker = remember { mutableStateOf<LocationTracker?>(null) }
    val locationService = remember { LocationService(context) }
    var trackingStarted by rememberSaveable { mutableStateOf(false) }
    var filterRadius by rememberSaveable { mutableStateOf<Float?>(null) }

    LaunchedEffect(Unit) {
        loadAndRenderAllGems(context) { grouped ->
            gemGroups.value = grouped
            val allGems = grouped.values.flatten()
            locationTracker.value = LocationTracker(context, allGems, locationService)

            if (fineGranted.value && notificationsGranted.value && !trackingStarted) {
                locationTracker.value?.startTracking()
                trackingStarted = true
            }
        }
    }

    if (!fineGranted.value || !notificationsGranted.value) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Location and notification permissions are required to detect nearby gems.")
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = {
                val permissions = mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                permissionLauncher.launch(permissions.toTypedArray())
            }) {
                Text("Grant permissions")
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
                locationTracker.value?.stopTracking()
            }
        }

        val userLatLng = location?.let { LatLng(it.latitude, it.longitude) }
        val initialLatLng = userLatLng ?: LatLng(45.2517, 19.8369)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(initialLatLng, 15f)
        }

        LaunchedEffect(userLatLng) {
            userLatLng?.let {
                try {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
                } catch (_: Exception) {}
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

                    filterRadius?.let { radius ->
                        Circle(
                            center = it,
                            radius = radius.toDouble(),
                            strokeColor = MaterialTheme.colorScheme.primary,
                            fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    }
                }

                val colorMap = remember { mutableStateMapOf<String, Float>() }
                var colorIndex = 0f

                gemGroups.value.forEach { (userId, gems) ->
                    val color = colorMap.getOrPut(userId) {
                        val hue = (colorIndex * 45f) % 360f
                        colorIndex += 1f
                        hue
                    }

                    val filteredGems = if (filterRadius != null && userLatLng != null) {
                        gems.filter { gem ->
                            val distance = FloatArray(1)
                            Location.distanceBetween(
                                userLatLng.latitude,
                                userLatLng.longitude,
                                gem.lat,
                                gem.lng,
                                distance
                            )
                            distance[0] <= filterRadius!!
                        }
                    } else gems

                    filteredGems.forEach { gem ->
                        Marker(
                            state = MarkerState(position = LatLng(gem.lat, gem.lng)),
                            title = gem.title,
                            snippet = gem.description,
                            icon = BitmapDescriptorFactory.defaultMarker(color),
                            onClick = {
                                navController.navigate("details/${gem.id}")
                                true
                            }
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Filter gems by radius (meters)", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    var inputText by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = {
                            inputText = it
                            val parsed = it.toFloatOrNull()
                            filterRadius = if (parsed != null && parsed >= 50f) parsed else null
                        },
                        label = { Text("Enter radius") },
                        singleLine = true,
                        modifier = Modifier.width(200.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = filterRadius?.let { "${it.toInt()} meters" } ?: "Showing all gems",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Button(
                onClick = { locationTracker.value?.triggerTestNotification() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text("Notif. Test")
            }

            FloatingActionButton(
                onClick = { navController.navigate(AppRoutes.GemTable) },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 32.dp, end = 16.dp)
            ) {
                Icon(Icons.Default.TableChart, contentDescription = "Gem Table")
            }

            FloatingActionButton(
                onClick = { navController.navigate(AppRoutes.Leaderboard) },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 160.dp, end = 16.dp)
            ) {
                Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard")
            }


            FloatingActionButton(
                onClick = { navController.navigate(AppRoutes.AddGem) },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 96.dp, end = 16.dp)
            ) {
                Icon(Icons.Default.AddLocationAlt, contentDescription = "Add Gem")
            }
        }
    }
}

fun loadAndRenderAllGems(
    context: Context,
    onResult: (Map<String, List<Gem>>) -> Unit
) {
    FirebaseFirestore.getInstance().collection("gems")
        .get()
        .addOnSuccessListener { result ->
            val gems = result.documents.mapNotNull {
                try {
                    it.toObject(Gem::class.java)
                } catch (e: Exception) {
                    Log.e("MapScreen", "Parse error: ${e.message}")
                    null
                }
            }
            val grouped = gems.groupBy { it.createdBy }
            onResult(grouped)
        }
}