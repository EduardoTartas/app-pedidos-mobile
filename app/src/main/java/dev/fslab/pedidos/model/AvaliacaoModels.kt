package dev.fslab.pedidos.model

import com.google.gson.annotations.SerializedName

data class AvaliarPedidoRequest(
    @SerializedName("pedido_id") val pedidoId: String,
    val nota: Int,
    val descricao: String? = null
)

data class AvaliacaoResponse(
    val message: String,
    val data: Avaliacao?
)

data class Avaliacao(
    @SerializedName("_id") val id: String,
    @SerializedName("pedido_id") val pedidoId: String,
    val nota: Int,
    val descricao: String?
)
