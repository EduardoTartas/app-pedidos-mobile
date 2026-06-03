package dev.fslab.pedidos.model

import com.google.gson.annotations.SerializedName

// ═══════════════════════════════════════════════════════
// REQUEST — Criação de pedido  (POST /pedidos)
// ═══════════════════════════════════════════════════════

data class CriarPedidoRequest(
    @SerializedName("restaurante_id") val restauranteId: String,
    val itens: List<ItemPedidoRequest>,
    @SerializedName("endereco_entrega") val enderecoEntrega: EnderecoEntregaRequest,
    @SerializedName("forma_pagamento") val formaPagamento: String = "pix"
)

data class ItemPedidoRequest(
    @SerializedName("prato_id") val pratoId: String,
    val quantidade: Int,
    val observacao: String = "",
    val adicionais: List<AdicionalPedidoRequest> = emptyList()
)

data class AdicionalPedidoRequest(
    @SerializedName("opcao_id") val opcaoId: String,
    val quantidade: Int = 1
)

data class PedidoStatusRequest(
    val status: String
)

data class EnderecoEntregaRequest(
    val logradouro: String,
    val numero: String,
    val bairro: String,
    val cidade: String,
    val estado: String,
    val cep: String, // Sem traço (backend regex: /^\d{8}$/)
    val complemento: String = "",
    val label: String = ""
)

// ═══════════════════════════════════════════════════════
// RESPONSE — Dados retornados após criar o pedido
// ═══════════════════════════════════════════════════════

data class CriarPedidoResponse(
    val message: String,
    val data: Pedido?
)

data class ListaPedidosResponse(
    val message: String,
    val data: PaginatedResponse<Pedido>?
)

data class RestauranteSimplificado(
    @SerializedName("_id") val id: String,
    val nome: String,
    @SerializedName("foto_restaurante") val fotoRestaurante: String?
)

data class ClienteSimplificado(
    @SerializedName("_id") val id: String,
    val nome: String,
    val email: String,
    val telefone: String?
)

data class Pedido(
    @SerializedName("_id") val id: String,
    @SerializedName("restaurante_id") val restauranteId: Any?, // Pode ser String ou RestauranteSimplificado
    @SerializedName("cliente_id") val clienteId: Any?,         // Pode ser String ou ClienteSimplificado
    val status: String,
    val itens: List<ItemPedidoCriado>,
    val totais: TotaisPedido,
    @SerializedName("forma_pagamento") val formaPagamento: String?,
    @SerializedName("historico_status") val historicoStatus: List<HistoricoStatus> = emptyList(),
    @SerializedName("createdAt") val criadoEm: String?
) {
    // Helpers para lidar com população dinâmica
    val restauranteNome: String
        get() = (restauranteId as? Map<*, *>)?.get("nome")?.toString() 
                ?: (restauranteId as? RestauranteSimplificado)?.nome 
                ?: "Restaurante"

    val restauranteFoto: String?
        get() = (restauranteId as? Map<*, *>)?.get("foto_restaurante")?.toString()
                ?: (restauranteId as? RestauranteSimplificado)?.fotoRestaurante
}

data class ItemPedidoCriado(
    @SerializedName("prato_id") val pratoId: Any?,
    @SerializedName("prato_nome") val pratoNome: String,
    @SerializedName("preco_unitario") val precoUnitario: Double,
    val quantidade: Int,
    val observacao: String? = "",
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
