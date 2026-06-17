package dev.fslab.pedidos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.pedidos.model.NotificationType
import dev.fslab.pedidos.model.NotificationUiModel
import dev.fslab.pedidos.model.Pedido
import dev.fslab.pedidos.repository.NotificationRepository
import dev.fslab.pedidos.utils.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

data class NotificationUiState(
    val notifications: List<NotificationUiModel> = emptyList(),
    val filteredNotifications: List<NotificationUiModel> = emptyList(),
    val selectedCategory: NotificationType? = null,
    val selectedNotification: NotificationUiModel? = null,
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val isMarkingAsRead: Boolean = false,
    val errorMessage: String? = null
)

class NotificationViewModel(
    private val repository: NotificationRepository = NotificationRepository()
) : ViewModel() {

    private var localNotifications: List<NotificationUiModel> = emptyList()

    private val _uiState = MutableStateFlow(NotificationUiState(isLoading = true))
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    fun carregarNotificacoes(silent: Boolean = false) {
        _uiState.value = _uiState.value.copy(
            isLoading = !silent,
            errorMessage = null
        )

        viewModelScope.launch {
            when (val result = repository.listarNotificacoes()) {
                is NetworkResult.Success -> {
                    publishState(
                        notifications = mergeLocalNotifications(result.data),
                        selectedCategory = _uiState.value.selectedCategory,
                        selectedNotificationId = _uiState.value.selectedNotification?.id
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isMarkingAsRead = false,
                        errorMessage = result.message
                    )
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    fun filtrarPorCategoria(category: NotificationType?) {
        val current = _uiState.value
        publishState(
            notifications = current.notifications,
            selectedCategory = category,
            selectedNotificationId = current.selectedNotification?.id
        )
    }

    fun selecionarNotificacao(notificationId: String?) {
        val current = _uiState.value
        publishState(
            notifications = current.notifications,
            selectedCategory = current.selectedCategory,
            selectedNotificationId = notificationId
        )
    }

    fun limparSelecao() {
        selecionarNotificacao(null)
    }

    fun marcarComoLida(id: String) {
        val current = _uiState.value
        val notification = current.notifications.find { it.id == id }

        selecionarNotificacao(id)

        if (notification?.isRead == true) return

        if (id.startsWith(LOCAL_NOTIFICATION_PREFIX)) {
            marcarNotificacaoLocalComoLida(id)
            return
        }

        _uiState.value = _uiState.value.copy(
            isMarkingAsRead = true,
            errorMessage = null
        )

        viewModelScope.launch {
            when (val result = repository.marcarComoLida(id)) {
                is NetworkResult.Success -> {
                    carregarNotificacoes(silent = true)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isMarkingAsRead = false,
                        errorMessage = result.message
                    )
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    fun registrarPedidoRealizado(
        pedido: Pedido,
        nomeRestaurante: String
    ): NotificationUiModel {
        val restaurante = nomeRestaurante
            .ifBlank { pedido.restauranteNome }
            .ifBlank { "o restaurante" }

        val notification = NotificationUiModel(
            id = "$LOCAL_NOTIFICATION_PREFIX${pedido.id}",
            title = "Pedido realizado!",
            description = "Pedido #${pedido.id.takeLast(8).uppercase()} enviado para $restaurante.",
            createdAt = pedido.criadoEm ?: Instant.now().toString(),
            isRead = false,
            type = NotificationType.ORDER
        )

        localNotifications = listOf(notification) +
            localNotifications.filterNot { it.id == notification.id }

        val current = _uiState.value
        publishState(
            notifications = mergeLocalNotifications(current.notifications),
            selectedCategory = current.selectedCategory,
            selectedNotificationId = notification.id
        )

        return notification
    }

    private fun marcarNotificacaoLocalComoLida(id: String) {
        localNotifications = localNotifications.map { notification ->
            if (notification.id == id) {
                notification.copy(isRead = true)
            } else {
                notification
            }
        }

        val current = _uiState.value
        publishState(
            notifications = mergeLocalNotifications(current.notifications),
            selectedCategory = current.selectedCategory,
            selectedNotificationId = id
        )
    }

    private fun mergeLocalNotifications(
        notifications: List<NotificationUiModel>
    ): List<NotificationUiModel> {
        val localIds = localNotifications.map { it.id }.toSet()
        return localNotifications + notifications.filterNot { it.id in localIds }
    }

    private fun publishState(
        notifications: List<NotificationUiModel>,
        selectedCategory: NotificationType?,
        selectedNotificationId: String?
    ) {
        val filteredNotifications = selectedCategory?.let { category ->
            notifications.filter { it.type == category }
        } ?: notifications

        _uiState.value = NotificationUiState(
            notifications = notifications,
            filteredNotifications = filteredNotifications,
            selectedCategory = selectedCategory,
            selectedNotification = notifications.find { it.id == selectedNotificationId },
            unreadCount = notifications.count { !it.isRead },
            isLoading = false,
            isMarkingAsRead = false,
            errorMessage = null
        )
    }

    companion object {
        private const val LOCAL_NOTIFICATION_PREFIX = "local-pedido-"
    }
}
