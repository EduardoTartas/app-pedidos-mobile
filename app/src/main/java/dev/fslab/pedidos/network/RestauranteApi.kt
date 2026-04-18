package dev.fslab.pedidos.network

import dev.fslab.pedidos.model.ListaRestaurantesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RestauranteApi {
    @GET("restaurantes")
    suspend fun listarRestaurantes(
        @Query("page") page: Int = 1,
        @Query("limite") limit: Int = 20,
        @Query("status") status: String? = null,
        @Query("nome") nome: String? = null,
        @Query("categoria") categoria: String? = null
    ): Response<ListaRestaurantesResponse>
}
