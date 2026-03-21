package dev.fslab.pedidos.network

import dev.fslab.pedidos.model.AuthResponse
import dev.fslab.pedidos.model.BasicResponse
import dev.fslab.pedidos.model.LoginRequest
import dev.fslab.pedidos.model.RecoverPasswordRequest
import dev.fslab.pedidos.model.RefreshRequest
import dev.fslab.pedidos.model.RegisterRequest
import dev.fslab.pedidos.model.RegisterResponse
import dev.fslab.pedidos.model.ResetPasswordRequest
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * AuthApi - Endpoints de autenticação da API app-pedidos
 */
interface AuthApi {

    /** POST /auth/login - Autenticar usuário */
    @POST("login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    /** POST /auth/register - Cadastrar novo usuário */
    @POST("signup")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    /** POST /auth/refresh - Renovar tokens */
    @POST("refresh")
    suspend fun refresh(@Body request: RefreshRequest): AuthResponse

    /** POST /auth/logout - Encerrar sessão */
    @POST("logout")
    suspend fun logout(@Header("Authorization") token: String)

    /** POST /auth/recover - Solicitar recuperação de senha */
    @POST("recover")
    suspend fun recoverPassword(@Body request: RecoverPasswordRequest): BasicResponse

    /** PATCH /auth/password/reset/code - Redefinir senha via código */
    @PATCH("password/reset")
    suspend fun resetPasswordByCode(
        @Query("token") token: String,
        @Body request: ResetPasswordRequest
    ): BasicResponse
}
