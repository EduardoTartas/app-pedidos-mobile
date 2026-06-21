package dev.fslab.pedidos.model

import com.google.gson.annotations.SerializedName

/** Payload enviado ao backend para registrar/atualizar dispositivo */
data class RegistrarDispositivoRequest(
    @SerializedName("tokenFcm") val tokenFcm: String,
    @SerializedName("plataforma") val plataforma: String = "android",
    @SerializedName("versaoApp") val versaoApp: String? = null
)

/** Resposta padrão da API */
data class RegistrarDispositivoResponse(
    @SerializedName("message") val message: String? = null
)
