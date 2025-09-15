package com.dimitrije.hiddengems.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.UploadTask

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()

    fun registerWithEmail(email: String, password: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun loginWithEmail(email: String, password: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun registerWithFullData(
        name: String,
        surname: String,
        phone: String,
        email: String,
        password: String,
        photoUri: Uri?,
        context: Context,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val storageRef = storage.reference.child("users/$uid/profile.jpg")

                val finalStream = try {
                    if (photoUri != null) {
                        context.contentResolver.openInputStream(photoUri)
                    } else {
                        val fallbackUri = Uri.parse("android.resource://${context.packageName}/drawable/default_avatar")
                        context.contentResolver.openInputStream(fallbackUri)
                    }
                } catch (e: Exception) {
                    onError(e)
                    return@addOnSuccessListener
                }

                if (finalStream == null) {
                    onError(Exception("Cant load photo"))
                    return@addOnSuccessListener
                }

                storageRef.putStream(finalStream)
                    .continueWithTask { task ->
                        if (!task.isSuccessful) throw task.exception ?: Exception("Upload failed")
                        storageRef.downloadUrl
                    }
                    .addOnSuccessListener { photoUrl ->
                        val userData = mapOf(
                            "name" to name,
                            "surname" to surname,
                            "phone" to phone,
                            "email" to email,
                            "photoUrl" to photoUrl.toString(),
                            "joinedAt" to Timestamp.now()
                        )

                        firestore.collection("users").document(uid)
                            .set(userData)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener(onError)
                    }
                    .addOnFailureListener(onError)
            }
            .addOnFailureListener(onError)
    }
}