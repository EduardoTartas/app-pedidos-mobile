package dev.fslab.pedidos.network

import android.util.Log
import com.google.gson.Gson
import dev.fslab.pedidos.model.AuthResponse
import dev.fslab.pedidos.model.RefreshRequest
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.TimeUnit

/**
 * TokenAuthenticator - Renovação automática de tokens em respostas 401
 *
 * Quando o servidor retorna 401 (Unauthorized), tenta renovar o access token
 * usando o refresh token. Se falhar, dispara onSessionExpired.
 */
class TokenAuthenticator : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"
        private const val MAX_RETRIES = 1
        private val gson = Gson()

        private val plainClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        Log.d(TAG, "401 recebido em: $path")

        if (path in setOf("/login", "/signup", "/google", "/refresh", "/recover", "/password/reset")) {
            Log.d(TAG, "401 em endpoint publico; refresh ignorado")
            return null
        }

        if (responseCount(response) > MAX_RETRIES) {
            Log.w(TAG, "Máximo de tentativas de refresh atingido")
            TokenManager.onSessionExpired?.invoke()
            return null
        }

        val currentRefreshToken = TokenManager.getRefreshToken()
        if (currentRefreshToken.isNullOrEmpty()) {
            Log.w(TAG, "Sem refresh token disponível")
            TokenManager.onSessionExpired?.invoke()
            return null
        }

        synchronized(this) {
            // Verifica se outra thread já fez o refresh
            val currentToken = TokenManager.getAccessToken()
            val requestToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")

            if (currentToken != null && currentToken != requestToken) {
                Log.d(TAG, "Token já atualizado por outra thread")
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            return try {
                val refreshBody = gson.toJson(RefreshRequest(refreshToken = currentRefreshToken))
                val mediaType = "application/json; charset=utf-8".toMediaType()

                val refreshRequest = Request.Builder()
                    .url(RetrofitClient.BASE_URL + "refresh")
                    .post(refreshBody.toRequestBody(mediaType))
                    .build()

                val refreshResponse = plainClient.newCall(refreshRequest).execute()

                if (refreshResponse.isSuccessful) {
                    val body = refreshResponse.body?.string()
                    val parsed = gson.fromJson(body, AuthResponse::class.java)
                    val refreshedUser = parsed?.getRemoteUser()

                    if (parsed?.isSuccess() == true && refreshedUser != null) {
                        TokenManager.saveTokens(refreshedUser.accessToken, refreshedUser.refreshToken)
                        TokenManager.onTokensRefreshed?.invoke(refreshedUser.accessToken)

                        Log.d(TAG, "Refresh bem-sucedido!")
                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${refreshedUser.accessToken}")
                            .build()
                    } else {
                        Log.w(TAG, "Refresh falhou: resposta inválida")
                        TokenManager.onSessionExpired?.invoke()
                        null
                    }
                } else {
                    Log.w(TAG, "Refresh HTTP ${refreshResponse.code}")
                    TokenManager.onSessionExpired?.invoke()
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro no refresh: ${e.message}")
                TokenManager.onSessionExpired?.invoke()
                null
            }
        }
    }

    // Conta o número de respostas anteriores para evitar loops infinitos
    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
