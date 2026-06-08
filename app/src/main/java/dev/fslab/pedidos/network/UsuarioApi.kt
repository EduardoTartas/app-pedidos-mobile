package dev.fslab.pedidos.network

import dev.fslab.pedidos.model.BasicResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
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
}
