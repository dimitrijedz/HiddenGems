package com.dimitrije.hiddengems.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Email: ${user?.email ?: "Unknown"}")
        Text("UID: ${user?.uid ?: "Unknown"}")

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            FirebaseAuth.getInstance().signOut()
            onLogout()
        }) {
            Text("Log out")
        }
    }
}
