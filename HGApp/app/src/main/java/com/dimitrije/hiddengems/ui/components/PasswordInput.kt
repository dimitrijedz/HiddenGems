package com.dimitrije.hiddengems.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun PasswordInput(
    password: String,
    onPasswordChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Lozinka") },
        modifier = modifier
            .then(Modifier.fillMaxWidth().padding(vertical = 8.dp)),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            val description = if (passwordVisible) "Sakrij lozinku" else "Prikaži lozinku"
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = icon, contentDescription = description)
            }
        }
    )
}