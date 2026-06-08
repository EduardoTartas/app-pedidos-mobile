package dev.fslab.pedidos.model

import com.google.gson.annotations.SerializedName

// ═══════════════════════════════════════════════════════
// REQUEST — Criação de pedido  (POST /pedidos)
// ═══════════════════════════════════════════════════════

/**
 * Snapshot do endereço de entrega enviado junto ao pedido.
 * A API armazena este objeto diretamente no pedido (não referência),
 * preservando o histórico mesmo que o usuário altere seu endereço depois.
 *
 * Mapeamento de campos: app usa "rua" internamente, mas a API espera "logradouro".
 */
data class EnderecoEntregaRequest(
    @SerializedName("logradouro") val logradouro: String,
    @SerializedName("numero")     val numero: String,
    @SerializedName("bairro")     val bairro: String,
    @SerializedName("cidade")     val cidade: String,
    @SerializedName("estado")     val estado: String,
    @SerializedName("cep")        val cep: String,
    @SerializedName("complemento") val complemento: String = "",
    @SerializedName("label")      val label: String = ""
) {
    companion object {
        /** Converte um [Endereco] (model de lista) para o snapshot de entrega */
        fun fromEndereco(e: Endereco) = EnderecoEntregaRequest(
            logradouro   = e.rua,
            numero       = e.numero,
            bairro       = e.bairro,
            cidade       = e.cidade,
            estado       = e.estado,
            cep          = e.cep.replace("-", "").replace(".", ""), // garante 8 dígitos sem traço
            complemento  = e.complemento,
            label        = e.label
        )
    }
}

data class CriarPedidoRequest(
    @SerializedName("restaurante_id")   val restauranteId: String,
    @SerializedName("itens")            val itens: List<ItemPedidoRequest>,
    @SerializedName("endereco_entrega") val enderecoEntrega: EnderecoEntregaRequest,
    @SerializedName("forma_pagamento")  val formaPagamento: String = "pix"
)

data class ItemPedidoRequest(
    @SerializedName("prato_id")   val pratoId: String,
    @SerializedName("quantidade") val quantidade: Int,
    @SerializedName("observacao") val observacao: String = "",
    @SerializedName("adicionais") val adicionais: List<AdicionalPedidoRequest> = emptyList()
)

data class AdicionalPedidoRequest(
    @SerializedName("opcao_id")  val opcaoId: String,
    @SerializedName("quantidade") val quantidade: Int = 1
)

data class PedidoStatusRequest(
    @SerializedName("status") val status: String
)

// ═══════════════════════════════════════════════════════
// RESPONSE — Dados retornados após criar o pedido
// ═══════════════════════════════════════════════════════

data class CriarPedidoResponse(
    @SerializedName("message") val message: String,
    @SerializedName("data")    val data: Pedido?
)

data class ListaPedidosResponse(
    @SerializedName("message") val message: String,
    @SerializedName("data")    val data: PaginatedResponse<Pedido>?
)

data class RestauranteSimplificado(
    @SerializedName("_id") val id: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("foto_restaurante") val fotoRestaurante: String?
)

data class ClienteSimplificado(
    @SerializedName("_id") val id: String,
    @SerializedName("nome") val nome: String,
    @SerializedName("email") val email: String,
    @SerializedName("telefone") val telefone: String?
)

data class Pedido(
    @SerializedName("_id") val id: String,
    @SerializedName("restaurante_id") val restauranteId: Any?, // Pode ser String ou RestauranteSimplificado
    @SerializedName("cliente_id") val clienteId: Any?,         // Pode ser String ou ClienteSimplificado
    @SerializedName("status") val status: String,
    @SerializedName("itens") val itens: List<ItemPedidoCriado>,
    @SerializedName("totais") val totais: TotaisPedido,
    @SerializedName("endereco_entrega") val enderecoEntrega: EnderecoEntregaResponse? = null,
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

/** Snapshot do endereço que vem na resposta da API */
data class EnderecoEntregaResponse(
    @SerializedName("logradouro")  val logradouro: String,
    @SerializedName("numero")      val numero: String,
    @SerializedName("bairro")      val bairro: String,
    @SerializedName("cidade")      val cidade: String,
    @SerializedName("estado")      val estado: String,
    @SerializedName("cep")         val cep: String,
    @SerializedName("complemento") val complemento: String = "",
    @SerializedName("label")       val label: String = ""
)

data class ItemPedidoCriado(
    @SerializedName("prato_id")       val pratoId: Any?,
    @SerializedName("prato_nome")     val pratoNome: String,
    @SerializedName("preco_unitario") val precoUnitario: Double,
    @SerializedName("quantidade")     val quantidade: Int,
    @SerializedName("observacao")     val observacao: String? = "",
    @SerializedName("adicionais")     val adicionais: List<AdicionalPedidoCriado>
)

data class AdicionalPedidoCriado(
    @SerializedName("opcao_id")      val opcaoId: Any?,
    @SerializedName("opcao_nome")    val opcaoNome: String,
    @SerializedName("preco_unitario") val precoUnitario: Double,
    @SerializedName("quantidade")    val quantidade: Int
)

data class TotaisPedido(
    @SerializedName("subtotal")     val subtotal: Double,
    @SerializedName("taxa_entrega") val taxaEntrega: Double,
    @SerializedName("total")        val total: Double
)

data class HistoricoStatus(
    @SerializedName("status") val status: String,
    @SerializedName("data")   val data: String?
)
