package com.dimitrije.hiddengems.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object RatingRepository {

    fun submitRating(gemId: String, stars: Int, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ratingData = mapOf(
            "stars" to stars,
            "timestamp" to System.currentTimeMillis()
        )

        val ratingRef = FirebaseFirestore.getInstance()
            .collection("gems")
            .document(gemId)
            .collection("ratings")
            .document(userId)

        ratingRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                onFailure(Exception("User already rated this gem"))
            } else {
                ratingRef.set(ratingData, SetOptions.merge())
                    .addOnSuccessListener {
                        incrementUserScore(userId)
                        onSuccess()
                    }
                    .addOnFailureListener { onFailure(it) }
            }
        }.addOnFailureListener { onFailure(it) }
    }

    private fun incrementUserScore(userId: String) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .update("score", com.google.firebase.firestore.FieldValue.increment(1))
    }
}
