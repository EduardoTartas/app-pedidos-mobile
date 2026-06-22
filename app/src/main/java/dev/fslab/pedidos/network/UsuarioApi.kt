package dev.fslab.pedidos.network

import dev.fslab.pedidos.model.BasicResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.DELETE
import retrofit2.http.Path

/**
 * UsuarioApi - Endpoints de gerenciamento de usuário
 */
interface UsuarioApi {

    /** PATCH /usuarios/:id - Atualizar dados do usuário */
    @PATCH("usuarios/{id}")
    suspend fun atualizar(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Response<BasicResponse>

    /** PATCH /usuarios/:id/status - Desativar conta do usuário */
    @PATCH("usuarios/{id}/status")
    suspend fun desativar(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Response<BasicResponse>

    /** POST /usuarios/:id/foto - Upload de foto de perfil */
    @retrofit2.http.Multipart
    @retrofit2.http.POST("usuarios/{id}/foto")
    suspend fun uploadFoto(
        @Path("id") id: String,
        @retrofit2.http.Part file: okhttp3.MultipartBody.Part
    ): Response<BasicResponse>

    /** DELETE /usuarios/:id/foto - Remover foto de perfil */
    @retrofit2.http.DELETE("usuarios/{id}/foto")
    suspend fun deleteFoto(
        @Path("id") id: String
    ): Response<BasicResponse>
}
