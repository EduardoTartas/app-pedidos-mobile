package dev.fslab.pedidos.network

import dev.fslab.pedidos.model.ViaCepResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * CepApi - Endpoint para busca de CEP via ViaCEP
 */
interface CepApi {
    @GET("{cep}/json/")
    suspend fun buscarCep(@Path("cep") cep: String): Response<ViaCepResponse>
}
