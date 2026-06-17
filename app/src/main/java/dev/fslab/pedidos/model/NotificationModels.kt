package dev.fslab.pedidos.model

import com.google.gson.annotations.SerializedName

enum class NotificationType {
    @SerializedName("ORDER")
    ORDER,

    @SerializedName("PROMOTION")
    PROMOTION,

    @SerializedName("SYSTEM")
    SYSTEM
}

data class NotificationUiModel(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String,
    val title: String,
    val description: String,
    @SerializedName(value = "createdAt", alternate = ["created_at"])
    val createdAt: String,
    @SerializedName(value = "isRead", alternate = ["is_read"])
    val isRead: Boolean,
    val type: NotificationType
)

data class NotificationApiModel(
    @SerializedName("_id")
    val id: String,
    @SerializedName("titulo")
    val title: String,
    @SerializedName("mensagem")
    val description: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("lida_em")
    val readAt: String?,
    @SerializedName("tipo")
    val apiType: String
) {
    fun toUiModel() = NotificationUiModel(
        id = id,
        title = title,
        description = description,
        createdAt = createdAt,
        isRead = readAt != null,
        type = apiType.toNotificationType()
    )
}

data class ListaNotificacoesResponse(
    val message: String,
    val data: PaginatedResponse<NotificationApiModel>?
)

data class NotificacaoResponse(
    val message: String,
    val data: NotificationApiModel?
)

private fun String.toNotificationType(): NotificationType = when (this) {
    "pedido_confirmado",
    "em_preparo",
    "a_caminho",
    "entregue",
    "cancelado" -> NotificationType.ORDER

    "promocao",
    "promotion" -> NotificationType.PROMOTION

    else -> NotificationType.SYSTEM
}
