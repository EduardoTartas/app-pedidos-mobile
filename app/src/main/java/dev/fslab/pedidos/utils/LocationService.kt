package dev.fslab.pedidos.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

data class LocalizacaoApp(
    val latitude: Double,
    val longitude: Double,
    val cidade: String = "Sua localização",
    val estado: String = ""
)

class ServicoLocalizacao(private val contexto: Context) {

    private val clienteLocalizacao: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(contexto)
    private val geocoder = Geocoder(contexto, Locale.getDefault())

    fun temPermissaoLocalizacao(): Boolean {
        val fine = ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun solicitarAtivacaoLocalizacao(
        aoSucesso: () -> Unit,
        aoPrecisarPrompt: (IntentSender) -> Unit,
        aoFalhar: (Exception) -> Unit
    ) {
        val requisicaoLocalizacao = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(requisicaoLocalizacao)
        val cliente: SettingsClient = LocationServices.getSettingsClient(contexto)
        val tarefa = cliente.checkLocationSettings(builder.build())

        tarefa.addOnSuccessListener {
            aoSucesso()
        }

        tarefa.addOnFailureListener { excecao ->
            if (excecao is ResolvableApiException) {
                try {
                    aoPrecisarPrompt(excecao.resolution.intentSender)
                } catch (sendEx: IntentSender.SendIntentException) {
                    aoFalhar(sendEx)
                }
            } else {
                aoFalhar(excecao)
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun obterLocalizacaoAtual(): LocalizacaoApp? = suspendCancellableCoroutine { continuacao ->
        if (!temPermissaoLocalizacao()) {
            continuacao.resume(null)
            return@suspendCancellableCoroutine
        }

        clienteLocalizacao.lastLocation.addOnSuccessListener { localizacao ->
            if (localizacao != null) {
                retomarLocalizacao(localizacao.latitude, localizacao.longitude, continuacao)
            } else {
                val requisicaoLocalizacao = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
                    .setMaxUpdates(1)
                    .build()

                val callbackLocalizacao = object : LocationCallback() {
                    override fun onLocationResult(resultado: LocationResult) {
                        clienteLocalizacao.removeLocationUpdates(this)
                        val loc = resultado.lastLocation
                        if (loc != null) {
                            retomarLocalizacao(loc.latitude, loc.longitude, continuacao)
                        } else {
                            if (continuacao.isActive) continuacao.resume(null)
                        }
                    }
                }
                clienteLocalizacao.requestLocationUpdates(requisicaoLocalizacao, callbackLocalizacao, Looper.getMainLooper())
            }
        }.addOnFailureListener {
            if (continuacao.isActive) continuacao.resume(null)
        }
    }

    private fun retomarLocalizacao(lat: Double, lng: Double, continuacao: kotlinx.coroutines.CancellableContinuation<LocalizacaoApp?>) {
        if (!continuacao.isActive) return
        try {
            val enderecos = geocoder.getFromLocation(lat, lng, 1)
            if (!enderecos.isNullOrEmpty()) {
                val endereco = enderecos[0]
                val cidade = endereco.subAdminArea ?: endereco.locality ?: "Sua localização"
                val estado = endereco.adminArea ?: ""
                val siglaEstado = mapaEstados[estado] ?: estado.take(2).uppercase()

                continuacao.resume(LocalizacaoApp(lat, lng, cidade, siglaEstado))
            } else {
                continuacao.resume(LocalizacaoApp(lat, lng))
            }
        } catch (e: Exception) {
            continuacao.resume(LocalizacaoApp(lat, lng))
        }
    }
    
    private val mapaEstados = mapOf(
        "Acre" to "AC", "Alagoas" to "AL", "Amapá" to "AP", "Amazonas" to "AM",
        "Bahia" to "BA", "Ceará" to "CE", "Distrito Federal" to "DF", "Espírito Santo" to "DF",
        "Goiás" to "GO", "Maranhão" to "MA", "Mato Grosso" to "MT", "Mato Grosso do Sul" to "MS",
        "Minas Gerais" to "MG", "Pará" to "PA", "Paraíba" to "PB", "Paraná" to "PR",
        "Pernambuco" to "PE", "Piauí" to "PI", "Rio de Janeiro" to "RJ", "Rio Grande do Norte" to "RN",
        "Rio Grande do Sul" to "RS", "Rondônia" to "RO", "Roraima" to "RR", "Santa Catarina" to "SC",
        "São Paulo" to "SP", "Sergipe" to "SE", "Tocantins" to "TO"
    )
}
