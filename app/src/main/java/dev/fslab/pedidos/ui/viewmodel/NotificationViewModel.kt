package dev.fslab.pedidos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.pedidos.model.NotificationType
import dev.fslab.pedidos.model.NotificationUiModel
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationUiState(
    val notifications: List<NotificationUiModel> = emptyList(),
    val filteredNotifications: List<NotificationUiModel> = emptyList(),
    val selectedCategory: NotificationType? = null,
    val selectedNotification: NotificationUiModel? = null,
    val unreadCount: Int = 0,
    val isLoading: Boolean = false
)

class NotificationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState(isLoading = true))
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        carregarNotificacoes()
    }

    fun carregarNotificacoes() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val notifications = mockNotifications()
            publishState(
                notifications = notifications,
                selectedCategory = _uiState.value.selectedCategory,
                selectedNotificationId = _uiState.value.selectedNotification?.id
            )
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
            isLoading = false
        )
    }

    private fun mockNotifications() = listOf(
        NotificationUiModel(
            id = "pedido-a-caminho",
            title = "Seu pedido saiu para entrega!",
            description = "O entregador Emerson está a caminho com seu pedido.",
            createdAt = Instant.now().minus(20, ChronoUnit.MINUTES).toString(),
            isRead = false,
            type = NotificationType.ORDER
        ),
        NotificationUiModel(
            id = "cupom-jantar-r20",
            title = "Cupom de R$ 20 disponível",
            description = "Aproveite seu cupom de desconto para jantar hoje! Válido para pedidos acima de R$ 60.",
            createdAt = Instant.now().minus(2, ChronoUnit.HOURS).toString(),
            isRead = false,
            type = NotificationType.PROMOTION
        ),
        NotificationUiModel(
            id = "reembolso-pedido-3245",
            title = "Reembolso processado",
            description = "O reembolso referente ao pedido #3245 foi aprovado.",
            createdAt = Instant.now().minus(3, ChronoUnit.DAYS).toString(),
            isRead = true,
            type = NotificationType.SYSTEM
        )
    )
}
