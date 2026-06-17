package dev.fslab.pedidos.network

import dev.fslab.pedidos.model.ListaNotificacoesResponse
import dev.fslab.pedidos.model.NotificacaoResponse
import dev.fslab.pedidos.model.BasicResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificacaoApi {
    @GET("notificacoes")
    suspend fun listarNotificacoes(
        @Query("page") page: Int = 1,
        @Query("limite") limite: Int = 50
    ): Response<ListaNotificacoesResponse>

    @PATCH("notificacoes/{id}/lida")
    suspend fun marcarComoLida(
        @Path("id") id: String
    ): Response<NotificacaoResponse>

    @DELETE("notificacoes/{id}")
    suspend fun deletarNotificacao(
        @Path("id") id: String
    ): Response<BasicResponse>
}
