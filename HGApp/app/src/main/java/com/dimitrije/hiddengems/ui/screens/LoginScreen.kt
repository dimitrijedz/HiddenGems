package com.dimitrije.hiddengems.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dimitrije.hiddengems.viewmodel.AuthViewModel
import com.dimitrije.hiddengems.ui.components.PasswordInput

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
            modifier = Modifier.fillMaxWidth()
        )

        PasswordInput(
            password = password,
            onPasswordChange = { password = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                authViewModel.loginWithEmail(
                    email = email,
                    password = password,
                    onSuccess = onLoginSuccess,
                    onError = { errorMessage = it.localizedMessage }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Prijavi se")
        }

        Button(
            onClick = {
                authViewModel.registerWithEmail(
                    email = email,
                    password = password,
                    onSuccess = onLoginSuccess,
                    onError = { errorMessage = it.localizedMessage }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registruj se")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Error: $it", color = MaterialTheme.colorScheme.error)
        }
    }
}