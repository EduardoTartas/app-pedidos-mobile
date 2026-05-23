package dev.fslab.pedidos.network

import dev.fslab.pedidos.model.CriarPedidoRequest
import dev.fslab.pedidos.model.CriarPedidoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PedidoApi {
    @POST("pedidos")
    suspend fun criarPedido(@Body body: CriarPedidoRequest): Response<CriarPedidoResponse>
}
