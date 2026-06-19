package dev.fslab.pedidos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.pedidos.BuildConfig
import dev.fslab.pedidos.model.NotificationMocks
import dev.fslab.pedidos.model.NotificationType
import dev.fslab.pedidos.model.NotificationUiModel
import dev.fslab.pedidos.model.Pedido
import dev.fslab.pedidos.model.isOrderRelated
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
    val isDeleting: Boolean = false,
    val selectedNotificationIds: Set<String> = emptySet(),
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
                        notifications = mergeLocalNotifications(withDebugInterfaceMocks(result.data)),
                        selectedCategory = _uiState.value.selectedCategory,
                        selectedNotificationId = _uiState.value.selectedNotification?.id
                    )
                }
                is NetworkResult.Error -> {
                    if (BuildConfig.DEBUG) {
                        publishState(
                            notifications = mergeLocalNotifications(withDebugInterfaceMocks(emptyList())),
                            selectedCategory = _uiState.value.selectedCategory,
                            selectedNotificationId = _uiState.value.selectedNotification?.id
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isMarkingAsRead = false,
                            isDeleting = false,
                            errorMessage = result.message
                        )
                    }
                }
                NetworkResult.Loading -> Unit
            }
        }
    }

    fun deletarNotificacao(id: String) {
        deletarNotificacoes(setOf(id))
    }

    fun deletarNotificacoesSelecionadas() {
        deletarNotificacoes(_uiState.value.selectedNotificationIds)
    }

    private fun deletarNotificacoes(ids: Set<String>) {
        if (ids.isEmpty()) return

        val localIds = ids.filter { isLocalOrMockNotification(it) }.toSet()
        val remoteIds = ids - localIds

        if (localIds.isNotEmpty()) {
            localNotifications = localNotifications.filterNot { it.id in localIds }
        }

        val current = _uiState.value
        _uiState.value = current.copy(
            isDeleting = true,
            errorMessage = null
        )

        viewModelScope.launch {
            val deletedRemoteIds = mutableSetOf<String>()
            var deleteError: String? = null

            for (remoteId in remoteIds) {
                when (val result = repository.deletarNotificacao(remoteId)) {
                    is NetworkResult.Success -> deletedRemoteIds += remoteId
                    is NetworkResult.Error -> {
                        deleteError = result.message
                        break
                    }
                    NetworkResult.Loading -> Unit
                }
            }

            val deletedIds = localIds + deletedRemoteIds
            val latest = _uiState.value
            publishState(
                notifications = latest.notifications.filterNot { it.id in deletedIds },
                selectedCategory = latest.selectedCategory,
                selectedNotificationId = latest.selectedNotification?.id?.takeIf { it !in deletedIds },
                selectedNotificationIds = latest.selectedNotificationIds - deletedIds
            )

            if (deleteError != null) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = deleteError
                )
            } else {
                limparSelecaoParaExclusao()
                carregarNotificacoes(silent = true)
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

    fun alternarSelecaoParaExclusao(id: String) {
        val current = _uiState.value
        val selectedIds = if (id in current.selectedNotificationIds) {
            current.selectedNotificationIds - id
        } else {
            current.selectedNotificationIds + id
        }

        _uiState.value = current.copy(selectedNotificationIds = selectedIds)
    }

    fun selecionarTodasFiltradasParaExclusao() {
        val current = _uiState.value
        val visibleIds = current.filteredNotifications.map { it.id }.toSet()
        if (visibleIds.isEmpty()) return

        val allVisibleSelected = visibleIds.all { it in current.selectedNotificationIds }
        val selectedIds = if (allVisibleSelected) {
            current.selectedNotificationIds - visibleIds
        } else {
            current.selectedNotificationIds + visibleIds
        }

        _uiState.value = current.copy(selectedNotificationIds = selectedIds)
    }

    fun limparSelecaoParaExclusao() {
        _uiState.value = _uiState.value.copy(selectedNotificationIds = emptySet())
    }

    fun marcarComoLida(id: String) {
        val current = _uiState.value
        val notification = current.notifications.find { it.id == id }

        selecionarNotificacao(id)

        if (notification?.isRead == true) return

        if (isLocalOrMockNotification(id)) {
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
        val restaurante = nomeRestaurante.ifBlank { "O restaurante" }

        val notification = NotificationUiModel(
            id = "$LOCAL_NOTIFICATION_PREFIX${pedido.id}",
            title = "Pedido realizado!",
            description = "Pedido #${pedido.id.takeLast(8).uppercase()} enviado para $restaurante.",
            createdAt = pedido.criadoEm ?: Instant.now().toString(),
            isRead = false,
            type = NotificationType.ORDER,
            pedidoId = pedido.id,
            restaurantName = restaurante,
            statusKey = "pedido_confirmado"
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
        val updatedNotifications = mergeLocalNotifications(current.notifications).map { notification ->
            if (notification.id == id) {
                notification.copy(isRead = true)
            } else {
                notification
            }
        }

        publishState(
            notifications = updatedNotifications,
            selectedCategory = current.selectedCategory,
            selectedNotificationId = id
        )
    }

    private fun removerNotificacaoLocal(id: String) {
        localNotifications = localNotifications.filterNot { it.id == id }

        val current = _uiState.value
        publishState(
            notifications = current.notifications.filterNot { it.id == id },
            selectedCategory = current.selectedCategory,
            selectedNotificationId = current.selectedNotification?.id?.takeIf { it != id }
        )
    }

    private fun mergeLocalNotifications(
        notifications: List<NotificationUiModel>
    ): List<NotificationUiModel> {
        val localIds = localNotifications.map { it.id }.toSet()
        return localNotifications + notifications.filterNot { it.id in localIds }
    }

    private fun withDebugInterfaceMocks(
        notifications: List<NotificationUiModel>
    ): List<NotificationUiModel> {
        if (!BuildConfig.DEBUG) return notifications

        val mockContext = resolvePreparingMockContext(localNotifications + notifications)
        val interfaceTestNotifications = NotificationMocks.interfaceTestNotifications(
            restaurantName = mockContext.restaurantName,
            pedidoId = mockContext.pedidoId
        )
        val existingIds = notifications.map { it.id }.toSet()
        val missingMocks = interfaceTestNotifications
            .filterNot { it.id in existingIds }

        return missingMocks + notifications
    }

    private fun isLocalOrMockNotification(id: String): Boolean {
        return id.startsWith(LOCAL_NOTIFICATION_PREFIX) ||
            NotificationMocks.isMockId(id)
    }

    private data class PreparingMockContext(
        val restaurantName: String,
        val pedidoId: String
    )

    private fun resolvePreparingMockContext(
        notifications: List<NotificationUiModel>
    ): PreparingMockContext {
        val confirmedOrderNotification = notifications.firstOrNull { notification ->
            notification.statusKey == "pedido_confirmado" ||
                notification.title.contains("confirmado", ignoreCase = true)
        }

        val restaurantName = confirmedOrderNotification
            ?.restaurantName
            ?.asValidRestaurantName()
            ?: confirmedOrderNotification?.restaurantNameFromConfirmedOrder()
            ?: notifications.firstNotNullOfOrNull { it.restaurantNameFromAnyOrderMessage() }
            ?: DEFAULT_MOCK_RESTAURANT_NAME

        val pedidoId = confirmedOrderNotification?.pedidoId
            ?: confirmedOrderNotification?.id
                ?.removePrefix(LOCAL_NOTIFICATION_PREFIX)
                ?.takeIf { it != confirmedOrderNotification.id && it.isNotBlank() }
            ?: DEFAULT_MOCK_PEDIDO_ID

        return PreparingMockContext(
            restaurantName = restaurantName,
            pedidoId = pedidoId
        )
    }

    private fun NotificationUiModel.restaurantNameFromConfirmedOrder(): String? {
        return Regex(
            pattern = "^(.+?)\\s+confirmou\\s+seu\\s+pedido",
            option = RegexOption.IGNORE_CASE
        ).find(description)
            ?.groupValues
            ?.getOrNull(1)
            ?.asValidRestaurantName()
    }

    private fun NotificationUiModel.restaurantNameFromAnyOrderMessage(): String? {
        restaurantName?.asValidRestaurantName()?.let { return it }

        val patterns = listOf(
            Regex("restaurante\\s+(.+?)\\s+(começou|iniciou|confirmou)", RegexOption.IGNORE_CASE),
            Regex("^(.+?)\\s+confirmou\\s+seu\\s+pedido", RegexOption.IGNORE_CASE),
            Regex("do\\s+(.+?)\\.", RegexOption.IGNORE_CASE)
        )

        return patterns.firstNotNullOfOrNull { pattern ->
            pattern.find(description)
                ?.groupValues
                ?.getOrNull(1)
                ?.asValidRestaurantName()
        }
    }

    private fun String.asValidRestaurantName(): String? {
        val value = trim()
        val normalized = value.lowercase()
        return value.takeIf {
            it.isNotBlank() &&
                normalized != "restaurante" &&
                normalized != "o restaurante"
        }
    }

    private fun publishState(
        notifications: List<NotificationUiModel>,
        selectedCategory: NotificationType?,
        selectedNotificationId: String?,
        selectedNotificationIds: Set<String> = _uiState.value.selectedNotificationIds
    ) {
        val notificationIds = notifications.map { it.id }.toSet()
        val validSelectedNotificationIds = selectedNotificationIds.intersect(notificationIds)
        val filteredNotifications = selectedCategory?.let { category ->
            notifications.filter { notification ->
                when (category) {
                    NotificationType.ORDER -> notification.type.isOrderRelated()
                    else -> notification.type == category
                }
            }
        } ?: notifications

        _uiState.value = NotificationUiState(
            notifications = notifications,
            filteredNotifications = filteredNotifications,
            selectedCategory = selectedCategory,
            selectedNotification = notifications.find { it.id == selectedNotificationId },
            unreadCount = notifications.count { !it.isRead },
            isLoading = false,
            isMarkingAsRead = false,
            isDeleting = false,
            selectedNotificationIds = validSelectedNotificationIds,
            errorMessage = null
        )
    }

    companion object {
        private const val LOCAL_NOTIFICATION_PREFIX = "local-pedido-"
        private const val DEFAULT_MOCK_RESTAURANT_NAME = "Restaurante parceiro"
        private const val DEFAULT_MOCK_PEDIDO_ID = "1"
    }
}
