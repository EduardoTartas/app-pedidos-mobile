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
    @SerializedName("foto_restaurante") val fotoRestaurante: String?,
    val status: String,
    @SerializedName("avaliacao_media") val avaliacaoMedia: Double,
    @SerializedName("estimativa_entrega_min") val estimativaEntregaMin: Int,
    @SerializedName("estimativa_entrega_max") val estimativaEntregaMax: Int,
    @SerializedName("taxa_entrega") val taxaEntrega: Double,
    @SerializedName("categoria_ids") val categorias: List<Categoria>? = emptyList()
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
