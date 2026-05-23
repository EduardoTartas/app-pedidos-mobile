package dev.fslab.pedidos.network

import dev.fslab.pedidos.model.ListaRestaurantesResponse
import dev.fslab.pedidos.model.RestauranteDetalheResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RestauranteApi {
    @GET("restaurantes")
    suspend fun listarRestaurantes(
        @Query("page") page: Int = 1,
        @Query("limite") limit: Int = 20,
        @Query("status") status: String? = null,
        @Query("nome") nome: String? = null,
        @Query("categoria") categoria: String? = null,
        @Query("ordenar") ordenar: String? = null,
        @Query("ordem") ordem: String? = null,
        @Query("entrega_gratis") entregaGratis: String? = null,
        @Query("avaliacao_min") avaliacaoMin: String? = null
    ): Response<ListaRestaurantesResponse>

    @GET("restaurantes/{id}")
    suspend fun buscarRestaurante(
        @Path("id") id: String
    ): Response<RestauranteDetalheResponse>
}
