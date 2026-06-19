package dev.fslab.pedidos.model

import com.google.gson.annotations.SerializedName

enum class NotificationType {
    @SerializedName(value = "PEDIDO_EM_PREPARO", alternate = ["em_preparo"])
    PEDIDO_EM_PREPARO,

    @SerializedName(value = "PEDIDO_A_CAMINHO", alternate = ["a_caminho"])
    PEDIDO_A_CAMINHO,

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
    val restaurantName: String? = null,
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
        restaurantName = null,
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

    "a_caminho",
    "PEDIDO_A_CAMINHO" -> NotificationType.PEDIDO_A_CAMINHO

    "pedido_confirmado",
    "entregue",
    "cancelado" -> NotificationType.ORDER

    "promocao",
    "promotion" -> NotificationType.PROMOTION

    else -> NotificationType.SYSTEM
}

fun NotificationType.isOrderRelated(): Boolean = when (this) {
    NotificationType.ORDER,
    NotificationType.PEDIDO_EM_PREPARO,
    NotificationType.PEDIDO_A_CAMINHO -> true
    NotificationType.PROMOTION,
    NotificationType.SYSTEM -> false
}

object NotificationMocks {
    private const val MOCK_PREPARING_ORDER_PREFIX = "mock-pedido-em-preparo-"
    private const val MOCK_ON_THE_WAY_ORDER_ID = "2"

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
        restaurantName = restaurantName,
        statusKey = "em_preparo"
    )

    fun pedidoACaminho(
        courierName: String = "Emerson",
        restaurantName: String = "Restaurante",
        pedidoId: String = MOCK_ON_THE_WAY_ORDER_ID
    ) = NotificationUiModel(
        id = MOCK_ON_THE_WAY_ORDER_ID,
        title = "Pedido a caminho!",
        description = "O entregador $courierName está a caminho com seu pedido do $restaurantName.",
        createdAt = "Agora",
        isRead = false,
        type = NotificationType.PEDIDO_A_CAMINHO,
        pedidoId = null,
        restaurantName = restaurantName,
        statusKey = "a_caminho"
    )

    fun interfaceTestNotifications(
        restaurantName: String = "Restaurante",
        pedidoId: String = MOCK_ON_THE_WAY_ORDER_ID
    ) = listOf(
        pedidoACaminho(
            restaurantName = restaurantName,
            pedidoId = pedidoId
        )
    )

    fun isMockId(id: String): Boolean =
        id == MOCK_ON_THE_WAY_ORDER_ID || id.startsWith(MOCK_PREPARING_ORDER_PREFIX)
}
