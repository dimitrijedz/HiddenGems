package com.dimitrije.hiddengems.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SplashScreen(
    onAuthenticated: () -> Unit,
    onUnauthenticated: () -> Unit
) {
    LaunchedEffect(Unit) {
        val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
        if (isLoggedIn) onAuthenticated() else onUnauthenticated()
    }
}