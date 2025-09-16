package com.dimitrije.hiddengems.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.dimitrije.hiddengems.viewmodel.GemViewModel
import com.google.android.gms.location.LocationServices
import com.dimitrije.hiddengems.navigation.AppRoutes
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

@Composable
fun AddGemScreen(
    navController: NavHostController,
    viewModel: GemViewModel = viewModel()
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf<Location?>(null) }
    val imageUri = remember { mutableStateOf<Uri?>(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                fusedLocationClient.lastLocation.addOnSuccessListener {
                    location = it
                }
            }
        }
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> imageUri.value = uri }
    )

    LaunchedEffect(Unit) {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(permission)
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                location = it
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(text = "Add new Gem", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { imagePickerLauncher.launch("image/*") }) {
            Text("Choose a gem photo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                location?.let { loc ->
                    val fallbackImage = "https://res.cloudinary.com/your_cloud_name/image/upload/v1234567890/no_image.png"

                    if (imageUri.value != null) {
                        uploadImageToCloudinary(context, imageUri.value!!) { imageUrl ->
                            val finalUrl = imageUrl ?: fallbackImage
                            viewModel.saveGem(
                                title = title,
                                description = description,
                                lat = loc.latitude,
                                lng = loc.longitude,
                                imageUrl = finalUrl
                            )
                            navController.navigate(AppRoutes.Map) {
                                popUpTo(AppRoutes.AddGem) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    } else {
                        viewModel.saveGem(
                            title = title,
                            description = description,
                            lat = loc.latitude,
                            lng = loc.longitude,
                            imageUrl = fallbackImage
                        )
                        navController.navigate(AppRoutes.Map) {
                            popUpTo(AppRoutes.AddGem) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            },
            enabled = location != null && title.isNotBlank()
        ) {
            Text("Save Gem")
        }
    }
}

fun uploadImageToCloudinary(context: Context, uri: Uri, onResult: (String?) -> Unit) {
    val inputStream = try {
        context.contentResolver.openInputStream(uri)
    } catch (e: Exception) {
        onResult(null)
        return
    }

    val bytes = inputStream?.readBytes()
    if (bytes == null) {
        onResult(null)
        return
    }

    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "file", "image.jpg",
            RequestBody.create("image/*".toMediaTypeOrNull(), bytes)
        )
        .addFormDataPart("upload_preset", "hidden_gems_upload")
        .build()

    val request = Request.Builder()
        .url("https://api.cloudinary.com/v1_1/di4fagc5c/image/upload")
        .post(requestBody)
        .build()

    OkHttpClient().newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onResult(null)
        }

        override fun onResponse(call: Call, response: Response) {
            val body = response.body?.string()
            val url = try {
                JSONObject(body ?: "").optString("secure_url")
            } catch (e: Exception) {
                null
            }
            onResult(url)
        }
    })
}