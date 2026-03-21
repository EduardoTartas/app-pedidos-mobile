package dev.fslab.pedidos.model

import com.google.gson.annotations.SerializedName

// ══════════════════════════════════════════════
// REQUEST models
// ══════════════════════════════════════════════

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("senha") val senha: String
)

data class RegisterRequest(
    @SerializedName("nome") val nome: String,
    @SerializedName("email") val email: String,
    @SerializedName("senha") val senha: String,
    @SerializedName("cpf") val cpf: String,
    @SerializedName("telefone") val telefone: String,
    @SerializedName("foto_perfil") val fotoPerfil: String? = null
)

data class RecoverPasswordRequest(
    @SerializedName("email") val email: String
)

data class ResetPasswordRequest(
    @SerializedName("senha") val senha: String
)

data class RefreshRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

// ══════════════════════════════════════════════
// RESPONSE models
// ══════════════════════════════════════════════

data class AuthResponse(
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: AuthResponseData? = null,
    @SerializedName("errors") val errors: List<String> = emptyList()
) {
    fun getRemoteUser(): RemoteUser? = data?.user
    fun isSuccess(): Boolean = getRemoteUser()?.hasValidSession() == true
    fun getErrorMessage(): String = errors.firstOrNull() ?: message
}

data class AuthResponseData(
    @SerializedName("user") val user: RemoteUser? = null
)

data class RemoteUser(
    @SerializedName(value = "accessToken", alternate = ["accesstoken", "token"])
    val accessToken: String = "",
    @SerializedName(value = "refreshtoken", alternate = ["refresh", "refreshToken", "refresh_token"])
    val refreshToken: String = "",
    @SerializedName(value = "_id", alternate = ["id"])
    val id: String = "",
    @SerializedName("nome") val nome: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("cpf") val cpf: String? = null,
    @SerializedName("telefone") val telefone: String? = null,
    @SerializedName("status") val status: String = "",
    @SerializedName("isAdmin") val isAdmin: Boolean = false,
    @SerializedName("foto_perfil") val fotoPerfil: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    fun hasValidSession(): Boolean = accessToken.isNotBlank() && refreshToken.isNotBlank()
}

data class RegisterResponse(
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: RemoteUser? = null,
    @SerializedName("errors") val errors: List<String> = emptyList()
) {
    fun isSuccess(): Boolean = data != null
    fun getErrorMessage(): String = errors.firstOrNull() ?: message
}

data class BasicResponse(
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: Any? = null,
    @SerializedName("errors") val errors: List<String> = emptyList()
)

data class ApiErrorResponse(
    @SerializedName("message") val message: String = "",
    @SerializedName("errors") val errors: List<String> = emptyList()
) {
    fun getErrorMessage(): String = errors.firstOrNull() ?: message
}
