package dev.fslab.pedidos.model

import com.google.gson.annotations.SerializedName

enum class NotificationType {
    @SerializedName(value = "PEDIDO_EM_PREPARO", alternate = ["em_preparo"])
    PEDIDO_EM_PREPARO,

    @SerializedName(value = "PEDIDO_A_CAMINHO", alternate = ["a_caminho"])
    PEDIDO_A_CAMINHO,

    @SerializedName(value = "PEDIDO_CANCELADO", alternate = ["cancelado"])
    PEDIDO_CANCELADO,

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
    "preparando",
    "preparo",
    "aceito",
    "PEDIDO_EM_PREPARO" -> NotificationType.PEDIDO_EM_PREPARO

    "a_caminho",
    "saiu_para_entrega",
    "PEDIDO_A_CAMINHO" -> NotificationType.PEDIDO_A_CAMINHO

    "cancelado",
    "PEDIDO_CANCELADO" -> NotificationType.PEDIDO_CANCELADO

    "pedido_confirmado",
    "confirmado",
    "criado",
    "pendente",
    "entregue" -> NotificationType.ORDER

    "promocao",
    "promotion" -> NotificationType.PROMOTION

    else -> NotificationType.SYSTEM
}

fun NotificationType.isOrderRelated(): Boolean = when (this) {
    NotificationType.ORDER,
    NotificationType.PEDIDO_EM_PREPARO,
    NotificationType.PEDIDO_A_CAMINHO,
    NotificationType.PEDIDO_CANCELADO -> true
    NotificationType.PROMOTION,
    NotificationType.SYSTEM -> false
}

object NotificationMocks {
    private const val MOCK_PREPARING_ORDER_PREFIX = "mock-pedido-em-preparo-"
    private const val MOCK_ON_THE_WAY_ORDER_ID = "2"
    private const val MOCK_CANCELED_ORDER_PREFIX = "mock-pedido-cancelado-"

    fun pedidoEmPreparo(
        restaurantName: String?,
        pedidoId: String = "1"
    ) = NotificationUiModel(
        id = "$MOCK_PREPARING_ORDER_PREFIX$pedidoId",
        title = "Seu pedido está sendo preparado",
        description = restaurantName
            ?.takeIf { it.isNotBlank() }
            ?.let { "$it começou a preparar seu pedido." }
            ?: "Seu pedido começou a ser preparado.",
        createdAt = "Agora",
        isRead = false,
        type = NotificationType.PEDIDO_EM_PREPARO,
        pedidoId = pedidoId,
        restaurantName = restaurantName,
        statusKey = "em_preparo"
    )

    fun pedidoACaminho(
        courierName: String? = null,
        restaurantName: String? = null,
        pedidoId: String = MOCK_ON_THE_WAY_ORDER_ID
    ) = NotificationUiModel(
        id = MOCK_ON_THE_WAY_ORDER_ID,
        title = "Pedido a caminho!",
        description = buildOnTheWayDescription(courierName, restaurantName),
        createdAt = "Agora",
        isRead = false,
        type = NotificationType.PEDIDO_A_CAMINHO,
        pedidoId = pedidoId,
        restaurantName = restaurantName,
        statusKey = "a_caminho"
    )

    fun interfaceTestNotifications(
        restaurantName: String? = null,
        pedidoId: String = MOCK_ON_THE_WAY_ORDER_ID
    ) = listOf(
        pedidoCancelado(
            restaurantName = restaurantName,
            pedidoId = pedidoId
        ),
        pedidoACaminho(
            restaurantName = restaurantName,
            pedidoId = pedidoId
        )
    )

    fun isMockId(id: String): Boolean =
        id == MOCK_ON_THE_WAY_ORDER_ID ||
            id.startsWith(MOCK_PREPARING_ORDER_PREFIX) ||
            id.startsWith(MOCK_CANCELED_ORDER_PREFIX)

    fun pedidoCancelado(
        restaurantName: String? = null,
        pedidoId: String = "3"
    ) = NotificationUiModel(
        id = "$MOCK_CANCELED_ORDER_PREFIX$pedidoId",
        title = "Pedido cancelado",
        description = restaurantName
            ?.takeIf { it.isNotBlank() }
            ?.let { "O pedido realizado em $it foi cancelado." }
            ?: "Seu pedido foi cancelado.",
        createdAt = "Agora",
        isRead = false,
        type = NotificationType.PEDIDO_CANCELADO,
        pedidoId = pedidoId,
        restaurantName = restaurantName,
        statusKey = "cancelado"
    )

    private fun buildOnTheWayDescription(courierName: String?, restaurantName: String?): String {
        val courier = courierName?.takeIf { it.isNotBlank() }
        val restaurant = restaurantName?.takeIf { it.isNotBlank() }
        return when {
            courier != null && restaurant != null ->
                "O entregador $courier está a caminho com seu pedido da $restaurant."
            courier != null ->
                "O entregador $courier está a caminho com seu pedido."
            restaurant != null ->
                "O entregador está a caminho com seu pedido da $restaurant."
            else ->
                "O entregador está a caminho com seu pedido."
        }
    }
}
