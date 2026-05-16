package dev.fslab.pedidos.network

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

/**
 * AuthInterceptor - Injeta o Bearer token em requisições autenticadas
 *
 * Pula a injeção para endpoints públicos (login, register, recover, refresh)
 * e para requisições que já possuem header Authorization.
 */
class AuthInterceptor : Interceptor {

    companion object {
        private val PUBLIC_PATHS = listOf(
            "login",
            "refresh",
            "recover",
            "password/reset",
            "signup"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        try {
            // Não injeta token em endpoints públicos
            if (PUBLIC_PATHS.any { path.contains(it) }) {
                return chain.proceed(request)
            }

            // Não sobrescreve Authorization existente
            if (request.header("Authorization") != null) {
                return chain.proceed(request)
            }

            val token = TokenManager.getAccessToken()
            val finalRequest = if (token != null) {
                request.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                request
            }
            
            return chain.proceed(finalRequest)

        } catch (e: Exception) {
            // Em vez de dar throw (que crasha o app), retornamos uma resposta sintética de erro
            // Isso permite que o safeApiCall trate como um erro comum sem derrubar a thread
            val errorJson = "{\"message\": \"Erro de rede: ${e.message}\", \"errors\": []}"
            return Response.Builder()
                .request(request)
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(503) // Service Unavailable
                .message("Sem conexão com o servidor: ${e.message}")
                .body(errorJson.toResponseBody("application/json".toMediaType()))
                .build()
        }
    }
}
