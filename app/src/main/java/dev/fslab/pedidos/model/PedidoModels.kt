package dev.fslab.pedidos.model

import com.google.gson.annotations.SerializedName

// ═══════════════════════════════════════════════════════
// REQUEST — Criação de pedido  (POST /pedidos)
// ═══════════════════════════════════════════════════════

data class CriarPedidoRequest(
    @SerializedName("restaurante_id") val restauranteId: String,
    val itens: List<ItemPedidoRequest>
)

data class ItemPedidoRequest(
    @SerializedName("prato_id") val pratoId: String,
    val quantidade: Int,
    val adicionais: List<AdicionalPedidoRequest> = emptyList()
)

data class AdicionalPedidoRequest(
    @SerializedName("opcao_id") val opcaoId: String,
    val quantidade: Int = 1
)

// ═══════════════════════════════════════════════════════
// RESPONSE — Dados retornados após criar o pedido
// ═══════════════════════════════════════════════════════

data class CriarPedidoResponse(
    val message: String,
    val data: PedidoCriado?
)

data class PedidoCriado(
    @SerializedName("_id") val id: String,
    @SerializedName("restaurante_id") val restauranteId: Any?,   // pode vir como objeto populado
    @SerializedName("cliente_id") val clienteId: Any?,
    val status: String,
    val itens: List<ItemPedidoCriado>,
    val totais: TotaisPedido,
    @SerializedName("historico_status") val historicoStatus: List<HistoricoStatus>,
    @SerializedName("createdAt") val criadoEm: String?
)

data class ItemPedidoCriado(
    @SerializedName("prato_id") val pratoId: Any?,
    @SerializedName("prato_nome") val pratoNome: String,
    @SerializedName("preco_unitario") val precoUnitario: Double,
    val quantidade: Int,
    val adicionais: List<AdicionalPedidoCriado>
)

data class AdicionalPedidoCriado(
    @SerializedName("opcao_id") val opcaoId: Any?,
    @SerializedName("opcao_nome") val opcaoNome: String,
    @SerializedName("preco_unitario") val precoUnitario: Double,
    val quantidade: Int
)

data class TotaisPedido(
    val subtotal: Double,
    @SerializedName("taxa_entrega") val taxaEntrega: Double,
    val total: Double
)

data class HistoricoStatus(
    val status: String,
    val data: String?
)
