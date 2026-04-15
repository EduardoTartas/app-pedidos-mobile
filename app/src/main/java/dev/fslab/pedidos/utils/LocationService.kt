package dev.fslab.pedidos.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

data class AppLocation(
    val latitude: Double,
    val longitude: Double,
    val city: String = "Sua localização",
    val state: String = ""
)

class LocationService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val geocoder = Geocoder(context, Locale.getDefault())

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): AppLocation? = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                resumeLocation(location.latitude, location.longitude, continuation)
            } else {
                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
                    .setMaxUpdates(1)
                    .build()

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        fusedLocationClient.removeLocationUpdates(this)
                        val loc = result.lastLocation
                        if (loc != null) {
                            resumeLocation(loc.latitude, loc.longitude, continuation)
                        } else {
                            if (continuation.isActive) continuation.resume(null)
                        }
                    }
                }
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            }
        }.addOnFailureListener {
            if (continuation.isActive) continuation.resume(null)
        }
    }

    private fun resumeLocation(lat: Double, lng: Double, continuation: kotlinx.coroutines.CancellableContinuation<AppLocation?>) {
        if (!continuation.isActive) return
        try {
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val city = address.subAdminArea ?: address.locality ?: "Sua localização"
                val state = address.adminArea ?: ""
                val abbrevState = stateMap[state] ?: state.take(2).uppercase()

                continuation.resume(AppLocation(lat, lng, city, abbrevState))
            } else {
                continuation.resume(AppLocation(lat, lng))
            }
        } catch (e: Exception) {
            continuation.resume(AppLocation(lat, lng))
        }
    }
    
    private val stateMap = mapOf(
        "Acre" to "AC", "Alagoas" to "AL", "Amapá" to "AP", "Amazonas" to "AM",
        "Bahia" to "BA", "Ceará" to "CE", "Distrito Federal" to "DF", "Espírito Santo" to "DF",
        "Goiás" to "GO", "Maranhão" to "MA", "Mato Grosso" to "MT", "Mato Grosso do Sul" to "MS",
        "Minas Gerais" to "MG", "Pará" to "PA", "Paraíba" to "PB", "Paraná" to "PR",
        "Pernambuco" to "PE", "Piauí" to "PI", "Rio de Janeiro" to "RJ", "Rio Grande do Norte" to "RN",
        "Rio Grande do Sul" to "RS", "Rondônia" to "RO", "Roraima" to "RR", "Santa Catarina" to "SC",
        "São Paulo" to "SP", "Sergipe" to "SE", "Tocantins" to "TO"
    )
}
