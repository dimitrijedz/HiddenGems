package com.dimitrije.hiddengems.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.dimitrije.hiddengems.model.Gem
import com.dimitrije.hiddengems.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class LocationTracker(
    private val context: Context,
    private val gems: List<Gem>
) {
    private val notifiedGemIds = mutableSetOf<String>()
    private val locationClient = LocationServices.getFusedLocationProviderClient(context)

    private var lastLocation: Location? = null
    private var lastTriggerTime: Long = 0
    private val movementThreshold = 50f // meters
    private val debounceMillis = 10_000L // 10 seconds

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            val now = System.currentTimeMillis()

            val movedEnough = lastLocation?.distanceTo(location) ?: Float.MAX_VALUE > movementThreshold
            val timePassed = now - lastTriggerTime > debounceMillis

            if (movedEnough && timePassed) {
                lastLocation = location
                lastTriggerTime = now
                checkProximity(location)
            }
        }
    }

    fun startTracking() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateDistanceMeters(10f)
            .build()

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    fun stopTracking() {
        locationClient.removeLocationUpdates(locationCallback)
    }

    private fun checkProximity(location: Location) {
        gems.forEach { gem ->
            val distance = FloatArray(1)
            Location.distanceBetween(
                location.latitude,
                location.longitude,
                gem.lat,
                gem.lng,
                distance
            )

            if (distance[0] <= 50f && !notifiedGemIds.contains(gem.id)) {
                showProximityNotification(gem)
                notifiedGemIds.add(gem.id)
            }
        }
    }

    private fun showProximityNotification(gem: Gem) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
        userRef.update("score", FieldValue.increment(5))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "gem_proximity",
                "Gem Proximity Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "gem_proximity")
            .setSmallIcon(R.drawable.ic_gem)
            .setContentTitle("You're near a gem!")
            .setContentText(gem.title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(gem.id.hashCode(), notification)
    }

    fun triggerTestNotification() {
        val testGem = Gem(
            id = "test123",
            title = "Test Gem",
            description = "This is a hardcoded test notification.",
            lat = 0.0,
            lng = 0.0,
            imageUrl = "",
            createdBy = "system"
        )
        showProximityNotification(testGem)
    }
}