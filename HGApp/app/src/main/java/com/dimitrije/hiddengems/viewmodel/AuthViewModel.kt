package com.dimitrije.hiddengems.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

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
}