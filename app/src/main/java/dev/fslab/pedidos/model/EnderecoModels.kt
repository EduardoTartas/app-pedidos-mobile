package dev.fslab.pedidos.model

import com.google.gson.annotations.SerializedName

/**
 * Endereco - Representa um endereço no sistema
 */
data class Endereco(
    @SerializedName("_id") val id: String? = null,
    val label: String = "",
    val cep: String,
    val rua: String,
    val numero: String,
    val bairro: String,
    val complemento: String = "",
    val cidade: String,
    val estado: String,
    val principal: Boolean = false,
    @SerializedName("usuario_id") val usuarioId: String? = null
)

/**
 * EnderecoRequest - Payload para criação/atualização de endereço
 */
data class EnderecoRequest(
    val label: String,
    val cep: String,
    val rua: String,
    val numero: String,
    val bairro: String,
    val complemento: String = "",
    val cidade: String,
    val estado: String,
    val principal: Boolean = false
)

/**
 * EnderecoListResponse - Resposta da listagem de endereços
 */
data class EnderecoListResponse(
    val status: String,
    val data: List<Endereco>
)

/**
 * EnderecoResponse - Resposta de um único endereço (criação/detalhes)
 */
data class EnderecoResponse(
    val status: String,
    val data: Endereco
)
