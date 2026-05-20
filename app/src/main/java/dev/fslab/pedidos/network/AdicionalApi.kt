package dev.fslab.pedidos.network

import dev.fslab.pedidos.model.AdicionalGruposResponse
import dev.fslab.pedidos.model.AdicionalOpcoesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface AdicionalApi {

    /** Lista todos os grupos de adicionais de um prato */
    @GET("adicionais/grupos/prato/{pratoId}")
    suspend fun listarGruposPorPrato(
        @Path("pratoId") pratoId: String
    ): Response<AdicionalGruposResponse>

    /** Lista todas as opções de um grupo */
    @GET("adicionais/opcoes/{grupoId}")
    suspend fun listarOpcoesPorGrupo(
        @Path("grupoId") grupoId: String
    ): Response<AdicionalOpcoesResponse>
}
