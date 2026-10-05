package com.naze.launcher.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.naze.launcher.core.Constants
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeoutOrNull

data class GeoPoint(val latitude: Double, val longitude: Double)

/**
 * Deliberately a single-shot API: we ask for one fresh fix when the home screen
 * needs weather data, then stop. We never register a continuous location callback —
 * that's the #1 battery drain launchers get flagged for.
 */
class LocationProvider(private val context: Context) {

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission") // Guarded by hasLocationPermission() at call sites.
    suspend fun requestSingleLocation(): GeoPoint? {
        if (!hasLocationPermission()) return null

        val client = LocationServices.getFusedLocationProviderClient(context)
        val deferred = CompletableDeferred<GeoPoint?>()

        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY).build()
        return try {
            withTimeoutOrNull(Constants.LOCATION_REQUEST_TIMEOUT_MS) {
                client.getCurrentLocation(request, null)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            deferred.complete(GeoPoint(location.latitude, location.longitude))
                        } else {
                            deferred.complete(null)
                        }
                    }
                    .addOnFailureListener { deferred.complete(null) }
                deferred.await()
            }
        } catch (e: TimeoutCancellationException) {
            null
        }
    }
}
