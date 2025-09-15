package com.dimitrije.hiddengems.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import androidx.navigation.NavHostController

import com.dimitrije.hiddengems.navigation.AppRoutes

@Composable
fun ProfileScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
    onNavigateToMap: () -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uid) {
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        userData = doc.data
                    } else {
                        errorMessage = "User not found"
                    }
                }
                .addOnFailureListener {
                    errorMessage = it.localizedMessage
                }
        } else {
            errorMessage = "Not logged in"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        userData?.let { data ->
            val name = data["name"] as? String ?: ""
            val surname = data["surname"] as? String ?: ""
            val email = data["email"] as? String ?: ""
            val phone = data["phone"] as? String ?: ""
            val photoUrl = data["photoUrl"] as? String ?: ""
            val joinedAt = data["joinedAt"] as? Timestamp

            Text("Name: $name")
            Text("Surname: $surname")
            Text("Email: $email")
            Text("Phone number: $phone")
            Text("Registered at: ${joinedAt?.toDate()}")

            Spacer(modifier = Modifier.height(16.dp))

            if (photoUrl.isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(photoUrl),
                    contentDescription = "User photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Error: $it", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { navController.navigate(AppRoutes.Map) }) {
            Text("Show map")
        }

        Button(onClick = {
            FirebaseAuth.getInstance().signOut()
            onLogout()
        }) {
            Text("Log out")
        }
    }
}