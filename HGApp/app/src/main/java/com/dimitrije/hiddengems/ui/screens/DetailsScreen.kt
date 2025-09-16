package com.dimitrije.hiddengems.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dimitrije.hiddengems.model.Gem
import com.dimitrije.hiddengems.repository.RatingRepository
import com.dimitrije.hiddengems.ui.components.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun DetailsScreen(gemId: String) {
    var gem by remember { mutableStateOf<Gem?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var userRating by remember { mutableIntStateOf(0) }
    var hasRated by remember { mutableStateOf(false) }
    var ratingMessage by remember { mutableStateOf("") }

    LaunchedEffect(gemId) {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        db.collection("gems").document(gemId)
            .get()
            .addOnSuccessListener { doc ->
                gem = doc.toObject(Gem::class.java)
                isLoading = false
            }
            .addOnFailureListener {
                Log.e("DetailsScreen", "Failed to load gem: ${it.message}")
                isLoading = false
            }

        if (userId != null) {
            db.collection("gems")
                .document(gemId)
                .collection("ratings")
                .document(userId)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        hasRated = true
                        userRating = doc.getLong("stars")?.toInt() ?: 0
                    }
                }
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

        Spacer(modifier = Modifier.height(24.dp))
        Text("Rating:", style = MaterialTheme.typography.titleMedium)

        RatingDropdown(
            selectedRating = if (hasRated) userRating else null,
            enabled = !hasRated,
            onRatingSelected = { stars ->
                userRating = stars

                RatingRepository.submitRating(
                    gemId = gemId,
                    stars = stars,
                    onSuccess = {
                        hasRated = true
                        ratingMessage = "Thanks for rating!"
                    },
                    onFailure = {
                        ratingMessage = it.message ?: "Rating failed"
                        userRating = 0
                    }
                )
            }
        )

        if (ratingMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(ratingMessage, color = MaterialTheme.colorScheme.primary)
        }
    }
}