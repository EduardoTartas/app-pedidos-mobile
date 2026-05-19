package dev.fslab.pedidos.utils

import com.google.gson.Gson
import dev.fslab.pedidos.model.ApiErrorResponse
import okhttp3.ResponseBody

object NetworkUtils {
    private val gson = Gson()

    /**
     * parseError - Converte o corpo de erro da Retrofit para ApiErrorResponse
     */
    fun parseError(errorBody: ResponseBody?): ApiErrorResponse? {
        return try {
            val json = errorBody?.string()
            gson.fromJson(json, ApiErrorResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * getErrorMessage - Obtém uma mensagem amigável do erro da API
     */
    fun getErrorMessage(errorBody: ResponseBody?, defaultMessage: String): String {
        val errorResponse = parseError(errorBody)
        return errorResponse?.errors?.firstOrNull()?.message 
            ?: errorResponse?.message 
            ?: defaultMessage
    }
}
