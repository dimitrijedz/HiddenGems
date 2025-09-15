package com.dimitrije.hiddengems.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.dimitrije.hiddengems.navigation.AppRoutes
import com.dimitrije.hiddengems.ui.components.PasswordInput
import com.dimitrije.hiddengems.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val success = authViewModel.registrationSuccess

    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(GetContent()) { uri ->
        photoUri = uri
    }

    // Ensure latest context and navController are used inside callbacks
    val currentContext by rememberUpdatedState(context)
    val currentNavController by rememberUpdatedState(navController)

    LaunchedEffect(success) {
        if (success) {
            Toast.makeText(context, "Registration successful. Please log in.", Toast.LENGTH_LONG).show()
            navController.navigate(AppRoutes.Login)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = surname,
            onValueChange = { surname = it },
            label = { Text("Surname") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        PasswordInput(password = password, onPasswordChange = { password = it })

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { imagePicker.launch("image/*") }) {
            Text("Choose photo")
        }

        photoUri?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "User photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (name.isBlank() || surname.isBlank() || phone.isBlank() || email.isBlank() || password.isBlank()) {
                    errorMessage = "Missing data"
                    return@Button
                }

                authViewModel.registerWithFullData(
                    name = name,
                    surname = surname,
                    phone = phone,
                    email = email,
                    password = password,
                    photoUri = photoUri,
                    context = context,
                    onSuccess = {
                        Toast.makeText(currentContext, "Registration successful. Please log in.", Toast.LENGTH_LONG).show()
                        currentNavController.navigate(AppRoutes.Login) {
                            popUpTo(AppRoutes.Register) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onError = { errorMessage = it.localizedMessage }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }

        TextButton(onClick = { navController.popBackStack() }) {
            Text("Back")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Error: $it", color = MaterialTheme.colorScheme.error)
        }
    }
}