package dev.fslab.pedidos.model

import com.google.gson.annotations.SerializedName

/**
 * ViaCepResponse - Representa a resposta da API ViaCEP
 */
data class ViaCepResponse(
    val cep: String? = null,
    val logradouro: String? = null,
    val complemento: String? = null,
    val bairro: String? = null,
    val localidade: String? = null,
    val uf: String? = null,
    val erro: Boolean? = null
)
