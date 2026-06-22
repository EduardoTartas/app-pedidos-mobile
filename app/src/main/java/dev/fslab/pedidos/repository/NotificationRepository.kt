package dev.fslab.pedidos.repository

import dev.fslab.pedidos.model.NotificationType
import dev.fslab.pedidos.model.NotificationUiModel
import dev.fslab.pedidos.model.Pedido
import dev.fslab.pedidos.network.NotificacaoApi
import dev.fslab.pedidos.network.PedidoApi
import dev.fslab.pedidos.network.RetrofitClient
import dev.fslab.pedidos.utils.NetworkResult
import dev.fslab.pedidos.utils.NetworkUtils

class NotificationRepository(
    private val api: NotificacaoApi = RetrofitClient.notificacaoApi,
    private val pedidoApi: PedidoApi = RetrofitClient.pedidoApi
) {
    suspend fun listarNotificacoes(): NetworkResult<List<NotificationUiModel>> {
        val notificationResult = NetworkUtils.safeApiCall { api.listarNotificacoes() }
        val pedidoResult = NetworkUtils.safeApiCall { pedidoApi.listarMeusPedidos(limit = 50) }

        val notifications = when (notificationResult) {
            is NetworkResult.Success -> notificationResult.data.data?.docs
                ?.map { it.toUiModel() }
                .orEmpty()
            is NetworkResult.Error,
            NetworkResult.Loading -> emptyList()
        }

        val orderNotifications = when (pedidoResult) {
            is NetworkResult.Success -> pedidoResult.data.data?.docs
                ?.map { it.toNotificationUiModel() }
                .orEmpty()
            is NetworkResult.Error,
            NetworkResult.Loading -> emptyList()
        }

        return when {
            notifications.isNotEmpty() || orderNotifications.isNotEmpty() -> {
                NetworkResult.Success(orderNotifications + notifications)
            }
            notificationResult is NetworkResult.Error -> notificationResult
            pedidoResult is NetworkResult.Error -> pedidoResult
            else -> NetworkResult.Success(emptyList())
        }
    }

    suspend fun marcarComoLida(id: String): NetworkResult<NotificationUiModel> {
        return when (val result = NetworkUtils.safeApiCall { api.marcarComoLida(id) }) {
            is NetworkResult.Success -> {
                val notification = result.data.data?.toUiModel()
                if (notification != null) {
                    NetworkResult.Success(notification)
                } else {
                    NetworkResult.Error("Notificação atualizada, mas a resposta veio vazia.")
                }
            }
            is NetworkResult.Error -> result
            NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    suspend fun deletarNotificacao(id: String): NetworkResult<Unit> {
        return when (val result = NetworkUtils.safeApiCall { api.deletarNotificacao(id) }) {
            is NetworkResult.Success -> NetworkResult.Success(Unit)
            is NetworkResult.Error -> result
            NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    private fun Pedido.toNotificationUiModel(): NotificationUiModel {
        val statusValue = status.lowercase()
        val restaurant = restauranteNomeOrNull?.takeIf { it.isNotBlank() }
        val type = statusValue.toOrderNotificationType()

        return NotificationUiModel(
            id = "local-pedido-$id",
            title = statusValue.toNotificationTitle(),
            description = statusValue.toNotificationDescription(restaurant),
            createdAt = historicoStatus.lastOrNull()?.data ?: criadoEm.orEmpty(),
            isRead = false,
            type = type,
            pedidoId = id,
            restaurantName = restaurant,
            statusKey = statusValue
        )
    }

    private fun String.toOrderNotificationType(): NotificationType = when (this) {
        "cancelado", "pedido_cancelado" -> NotificationType.PEDIDO_CANCELADO
        "entregue", "pedido_entregue", "concluido", "finalizado" -> NotificationType.PEDIDO_ENTREGUE
        "a_caminho", "pedido_a_caminho", "saiu_para_entrega", "saiu_entrega", "em_entrega" ->
            NotificationType.PEDIDO_A_CAMINHO
        "em_preparo", "pedido_em_preparo", "preparando", "aceito" -> NotificationType.PEDIDO_EM_PREPARO
        else -> NotificationType.PEDIDO_CONFIRMADO
    }

    private fun String.toNotificationTitle(): String = when (toOrderNotificationType()) {
        NotificationType.PEDIDO_CANCELADO -> "Pedido cancelado"
        NotificationType.PEDIDO_ENTREGUE -> "Pedido entregue!"
        NotificationType.PEDIDO_A_CAMINHO -> "Pedido a caminho!"
        NotificationType.PEDIDO_EM_PREPARO -> "Seu pedido está sendo preparado"
        else -> "Pedido confirmado"
    }

    private fun String.toNotificationDescription(restaurantName: String?): String {
        return when (toOrderNotificationType()) {
            NotificationType.PEDIDO_CANCELADO -> restaurantName
                ?.let { "Seu pedido de $it foi cancelado." }
                ?: "Seu pedido foi cancelado."
            NotificationType.PEDIDO_ENTREGUE -> restaurantName
                ?.let { "Seu pedido de $it foi entregue com sucesso." }
                ?: "Seu pedido foi entregue com sucesso."
            NotificationType.PEDIDO_A_CAMINHO -> restaurantName
                ?.let { "O entregador está a caminho com seu pedido da $it." }
                ?: "O entregador está a caminho com seu pedido."
            NotificationType.PEDIDO_EM_PREPARO -> restaurantName
                ?.let { "$it começou a preparar seu pedido." }
                ?: "Seu pedido começou a ser preparado."
            else -> restaurantName
                ?.let { "$it recebeu seu pedido." }
                ?: "Seu pedido foi recebido."
        }
    }
}
