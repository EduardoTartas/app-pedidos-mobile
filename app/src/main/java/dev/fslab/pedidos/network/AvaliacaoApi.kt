package dev.fslab.pedidos.network

import dev.fslab.pedidos.model.AvaliacaoResponse
import dev.fslab.pedidos.model.AvaliarPedidoRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AvaliacaoApi {
    @POST("avaliacoes")
    suspend fun avaliarPedido(
        @Body request: AvaliarPedidoRequest
    ): Response<AvaliacaoResponse>
}
