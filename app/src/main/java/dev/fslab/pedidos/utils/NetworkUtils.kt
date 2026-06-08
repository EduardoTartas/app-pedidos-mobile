package dev.fslab.pedidos.utils

import com.google.gson.Gson
import dev.fslab.pedidos.model.ApiErrorResponse
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Representa o resultado de uma operação de rede
 */
sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val code: Int? = null) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}

object NetworkUtils {
    private val gson = Gson()

    /**
     * safeApiCall - O "Handler Global" para chamadas Retrofit.
     * Centraliza o tratamento de exceções de rede e erros HTTP.
     */
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>
    ): NetworkResult<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    NetworkResult.Success(body)
                } else {
                    NetworkResult.Error("Resposta do servidor vazia.")
                }
            } else {
                val message = getErrorMessage(response.errorBody(), "Erro desconhecido no servidor.")
                NetworkResult.Error(message, response.code())
            }
        } catch (e: Exception) {
            when (e) {
                is ConnectException, is UnknownHostException -> 
                    NetworkResult.Error("Sem conexão com o servidor. Verifique sua internet ou se a API está rodando.")
                is SocketTimeoutException -> 
                    NetworkResult.Error("Tempo de conexão esgotado.")
                is IOException -> 
                    NetworkResult.Error("Erro de comunicação com o servidor.")
                else -> 
                    NetworkResult.Error("Ocorreu um erro inesperado: ${e.localizedMessage}")
            }
        }
    }

    fun parseError(errorBody: ResponseBody?): ApiErrorResponse? {
        return try {
            val json = errorBody?.string()
            gson.fromJson(json, ApiErrorResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    fun getErrorMessage(errorBody: ResponseBody?, defaultMessage: String): String {
        val errorResponse = parseError(errorBody)
        return errorResponse?.errors?.firstOrNull()?.message 
            ?: errorResponse?.message 
            ?: defaultMessage
    }
}
