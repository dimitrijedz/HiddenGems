package com.dimitrije.hiddengems.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import com.dimitrije.hiddengems.service.LocationService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val locationService = LocationService(application.applicationContext)
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location

    fun startTracking() {
        locationService.startLocationUpdates { loc ->
            _location.value = loc
            updateLocationInFirestore(loc)
        }
    }

    private fun updateLocationInFirestore(location: Location) {
        val uid = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(uid)
        userRef.update(
            mapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "timestamp" to FieldValue.serverTimestamp()
            )
        )
    }
}