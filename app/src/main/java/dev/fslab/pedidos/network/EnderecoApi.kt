package dev.fslab.pedidos.network

import dev.fslab.pedidos.model.BasicResponse
import dev.fslab.pedidos.model.EnderecoListResponse
import dev.fslab.pedidos.model.EnderecoRequest
import dev.fslab.pedidos.model.EnderecoResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * EnderecoApi - Endpoints de gerenciamento de endereços
 */
interface EnderecoApi {

    /** GET /usuarios/:usuarioId/enderecos - Listar endereços do usuário */
    @GET("usuarios/{usuarioId}/enderecos")
    suspend fun listarPorUsuario(
        @Path("usuarioId") usuarioId: String
    ): Response<EnderecoListResponse>

    /** POST /usuarios/:usuarioId/enderecos - Criar novo endereço para o usuário */
    @POST("usuarios/{usuarioId}/enderecos")
    suspend fun criarParaUsuario(
        @Path("usuarioId") usuarioId: String,
        @Body request: EnderecoRequest
    ): Response<EnderecoResponse>

    /** PATCH /usuarios/:usuarioId/enderecos/:enderecoId - Atualizar endereço */
    @PATCH("usuarios/{usuarioId}/enderecos/{enderecoId}")
    suspend fun atualizar(
        @Path("usuarioId") usuarioId: String,
        @Path("enderecoId") enderecoId: String,
        @Body request: EnderecoRequest
    ): Response<EnderecoResponse>

    /** DELETE /usuarios/:usuarioId/enderecos/:enderecoId - Remover endereço */
    @DELETE("usuarios/{usuarioId}/enderecos/{enderecoId}")
    suspend fun deletar(
        @Path("usuarioId") usuarioId: String,
        @Path("enderecoId") enderecoId: String
    ): Response<BasicResponse>
}
