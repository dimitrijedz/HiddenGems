package com.dimitrije.hiddengems.ui.screens

import com.dimitrije.hiddengems.ui.components.PasswordInput
import com.dimitrije.hiddengems.viewmodel.AuthViewModel

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        PasswordInput(password = password, onPasswordChange = { password = it })

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Missing data!"
                    return@Button
                }

                authViewModel.loginWithEmail(
                    email = email,
                    password = password,
                    onSuccess = onLoginSuccess,
                    onError = { errorMessage = it.localizedMessage }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        TextButton(onClick = onNavigateToRegister) {
            Text("New here? Register now!")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Error: $it", color = MaterialTheme.colorScheme.error)
        }
    }
}