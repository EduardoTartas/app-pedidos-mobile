package dev.fslab.pedidos.network

import dev.fslab.pedidos.model.CriarPedidoRequest
import dev.fslab.pedidos.model.CriarPedidoResponse
import dev.fslab.pedidos.model.ListaPedidosResponse
import dev.fslab.pedidos.model.Pedido
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class PedidoResponse(
    val message: String,
    val data: Pedido?
)

interface PedidoApi {
    @POST("pedidos")
    suspend fun criarPedido(@Body body: CriarPedidoRequest): Response<CriarPedidoResponse>

    @GET("pedidos/meus")
    suspend fun listarMeusPedidos(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("status") status: String? = null
    ): Response<ListaPedidosResponse>

    @GET("pedidos/{id}")
    suspend fun obterPedido(
        @Path("id") id: String
    ): Response<PedidoResponse>
}

