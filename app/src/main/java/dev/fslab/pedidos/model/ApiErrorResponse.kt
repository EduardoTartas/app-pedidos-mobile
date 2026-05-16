package dev.fslab.pedidos.model

import com.google.gson.annotations.SerializedName

/**
 * ApiErrorResponse - Mapeia a resposta de erro padrão da API app-pedidos
 */
data class ApiErrorResponse(
    @SerializedName("message") val message: String = "",
    @SerializedName("errors") val errors: List<ApiErrorDetail> = emptyList()
) {
    /**
     * getErrorMessage - Retorna a primeira mensagem de erro detalhada ou a mensagem principal
     */
    fun getErrorMessage(): String = errors.firstOrNull()?.message ?: message
}

data class ApiErrorDetail(
    @SerializedName("path") val path: String? = null,
    @SerializedName("message") val message: String = ""
)
