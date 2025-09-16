package com.dimitrije.hiddengems.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import com.dimitrije.hiddengems.service.LocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val locationService = LocationService(application.applicationContext)

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    fun startTracking() {
        locationService.startLocationUpdates { loc ->
            _location.value = loc
        }
    }

    fun stopTracking() {
        locationService.stopLocationUpdates()
    }

    override fun onCleared() {
        stopTracking()
        super.onCleared()
    }
}
