package dev.fslab.pedidos.model

import com.google.gson.annotations.SerializedName

data class Categoria(
    @SerializedName("_id") val id: String,
    val nome: String,
    @SerializedName("icone_categoria") val iconeCategoria: String?,
    val ativo: Boolean
)

data class Restaurante(
    @SerializedName("_id") val id: String,
    val nome: String,
    val descricao: String?,
    @SerializedName("foto_restaurante") val fotoRestaurante: String?,
    val status: String,
    @SerializedName("avaliacao_media") val avaliacaoMedia: Double,
    @SerializedName("estimativa_entrega_min") val estimativaEntregaMin: Int,
    @SerializedName("estimativa_entrega_max") val estimativaEntregaMax: Int,
    @SerializedName("taxa_entrega") val taxaEntrega: Double,
    @SerializedName("categoria_ids") val categorias: List<Categoria>? = emptyList(),
    @SerializedName("secoes_cardapio") val secoesCardapio: List<String>? = emptyList()
)

data class Prato(
    @SerializedName("_id") val id: String,
    @SerializedName("restaurante_id") val restauranteId: String,
    val nome: String,
    @SerializedName("foto_prato") val fotoPrato: String?,
    val preco: Double,
    val descricao: String?,
    val secao: String?,
    val status: String
)

data class PaginatedResponse<T>(
    val docs: List<T>,
    val totalDocs: Int,
    val page: Int,
    val totalPages: Int
)

data class ListaRestaurantesResponse(
    val message: String,
    val data: PaginatedResponse<Restaurante>?
)

data class ListaCategoriasResponse(
    val message: String,
    val data: PaginatedResponse<Categoria>?
)

data class RestauranteDetalheResponse(
    val message: String,
    val data: Restaurante?
)

data class CardapioResponse(
    val message: String,
    val data: Map<String, List<Prato>>?
)

// ═══════════════════════════════════════════
// ADICIONAIS
// ═══════════════════════════════════════════

data class AdicionalGrupo(
    @SerializedName("_id") val id: String,
    val nome: String,
    val tipo: String,          // "adicional" | "variacao"
    val obrigatorio: Boolean,
    val min: Int,
    val max: Int,
    val ativo: Boolean
)

data class AdicionalOpcao(
    @SerializedName("_id") val id: String,
    @SerializedName("grupo_id") val grupoId: String,
    val nome: String,
    val preco: Double,
    @SerializedName("foto_adicional") val fotoAdicional: String?,
    val ativo: Boolean
)

data class AdicionalGruposResponse(
    val message: String,
    val data: List<AdicionalGrupo>?
)

data class AdicionalOpcoesResponse(
    val message: String,
    val data: List<AdicionalOpcao>?
)
