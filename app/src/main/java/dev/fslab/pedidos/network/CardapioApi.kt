package dev.fslab.pedidos.network

import dev.fslab.pedidos.model.CardapioResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CardapioApi {
    @GET("cardapio/{restauranteId}")
    suspend fun buscarCardapio(
        @Path("restauranteId") restauranteId: String
    ): Response<CardapioResponse>
}
