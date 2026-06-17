package dev.fslab.pedidos.model

import com.google.gson.annotations.SerializedName

enum class NotificationType {
    @SerializedName(value = "PEDIDO_EM_PREPARO", alternate = ["em_preparo"])
    PEDIDO_EM_PREPARO,

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
    val type: NotificationType,
    val pedidoId: String? = null,
    val statusKey: String? = null
) {
    val date: String
        get() = createdAt

    val isUnread: Boolean
        get() = !isRead
}

data class NotificationApiModel(
    @SerializedName("_id")
    val id: String,
    @SerializedName("pedido_id")
    val pedidoId: String?,
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
        type = apiType.toNotificationType(),
        pedidoId = pedidoId,
        statusKey = apiType
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
    "em_preparo",
    "PEDIDO_EM_PREPARO" -> NotificationType.PEDIDO_EM_PREPARO

    "pedido_confirmado",
    "a_caminho",
    "entregue",
    "cancelado" -> NotificationType.ORDER

    "promocao",
    "promotion" -> NotificationType.PROMOTION

    else -> NotificationType.SYSTEM
}

fun NotificationType.isOrderRelated(): Boolean = when (this) {
    NotificationType.ORDER,
    NotificationType.PEDIDO_EM_PREPARO -> true
    NotificationType.PROMOTION,
    NotificationType.SYSTEM -> false
}

object NotificationMocks {
    private const val MOCK_PREPARING_ORDER_PREFIX = "mock-pedido-em-preparo-"

    fun pedidoEmPreparo(
        restaurantName: String,
        pedidoId: String = "1"
    ) = NotificationUiModel(
        id = "$MOCK_PREPARING_ORDER_PREFIX$pedidoId",
        title = "Seu pedido está sendo preparado",
        description = "O restaurante $restaurantName começou a preparar seu pedido.",
        createdAt = "Agora",
        isRead = false,
        type = NotificationType.PEDIDO_EM_PREPARO,
        pedidoId = pedidoId,
        statusKey = "em_preparo"
    )

    fun interfaceTestNotifications(
        restaurantName: String = "Burger House",
        pedidoId: String = "1"
    ) = listOf(
        pedidoEmPreparo(
            restaurantName = restaurantName,
            pedidoId = pedidoId
        )
    )

    fun isMockId(id: String): Boolean = id.startsWith(MOCK_PREPARING_ORDER_PREFIX)
}
