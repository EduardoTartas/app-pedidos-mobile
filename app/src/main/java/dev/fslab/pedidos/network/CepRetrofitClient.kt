package dev.fslab.pedidos.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * CepRetrofitClient - Cliente específico para a API ViaCEP
 */
object CepRetrofitClient {
    private const val BASE_URL = "https://viacep.com.br/ws/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val cepApi: CepApi by lazy {
        retrofit.create(CepApi::class.java)
    }
}
