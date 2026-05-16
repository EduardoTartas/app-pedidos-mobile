package dev.fslab.pedidos.network

import dev.fslab.pedidos.model.ListaCategoriasResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CategoriaApi {
    @GET("categorias")
    suspend fun listarCategorias(
        @Query("page") page: Int = 1,
        @Query("limite") limit: Int = 20
    ): Response<ListaCategoriasResponse>
}
