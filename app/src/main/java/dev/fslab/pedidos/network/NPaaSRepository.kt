package dev.fslab.pedidos.network

import android.util.Log
import dev.fslab.pedidos.model.RegistrarDispositivoRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NPaaSRepository {
    private const val TAG = "NPaaSRepository"

    /**
     * Registra (ou atualiza) o token FCM no backend apos login bem-sucedido.
     * Chamada nao-blocante: erros sao apenas logados.
     */
    suspend fun registrarDispositivo(
        tokenFcm: String,
        versaoApp: String? = null
    ) = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.dispositivoApi.registrar(
                payload = RegistrarDispositivoRequest(
                    tokenFcm = tokenFcm,
                    plataforma = "android",
                    versaoApp = versaoApp
                )
            )
            if (response.isSuccessful) {
                Log.d(TAG, "Dispositivo registrado com sucesso")
            } else {
                Log.w(TAG, "Falha ao registrar dispositivo: HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Excecao ao registrar dispositivo: ${e.message}")
        }
    }

    /**
     * Desativa o token FCM no backend no logout.
     * Chamada nao-blocante: erros sao apenas logados.
     */
    suspend fun desativarToken(tokenFcm: String) = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.dispositivoApi.desativar(
                payload = mapOf("tokenFcm" to tokenFcm)
            )
            if (response.isSuccessful) {
                Log.d(TAG, "Token FCM desativado no backend")
            } else {
                Log.w(TAG, "Falha ao desativar token FCM no backend")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao desativar token: ${e.message}")
        }
    }
}
