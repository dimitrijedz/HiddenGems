package com.dimitrije.hiddengems.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dimitrije.hiddengems.model.Gem
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun DetailsScreen(gemId: String) {
    var gem by remember { mutableStateOf<Gem?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(gemId) {
        FirebaseFirestore.getInstance().collection("gems").document(gemId)
            .get()
            .addOnSuccessListener { doc ->
                gem = doc.toObject(Gem::class.java)
                isLoading = false
            }
            .addOnFailureListener {
                Log.e("DetailsScreen", "Failed to load gem: ${it.message}")
                isLoading = false
            }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (gem == null) {
        Text("Gem not found", modifier = Modifier.padding(16.dp))
        return
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(text = gem!!.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = gem!!.description)
        Spacer(modifier = Modifier.height(16.dp))

        AsyncImage(
            model = gem!!.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Location: ${gem!!.lat}, ${gem!!.lng}")
    }
}