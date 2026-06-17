package dev.fslab.pedidos.repository

import dev.fslab.pedidos.model.NotificationUiModel
import dev.fslab.pedidos.network.NotificacaoApi
import dev.fslab.pedidos.network.RetrofitClient
import dev.fslab.pedidos.utils.NetworkResult
import dev.fslab.pedidos.utils.NetworkUtils

class NotificationRepository(
    private val api: NotificacaoApi = RetrofitClient.notificacaoApi
) {
    suspend fun listarNotificacoes(): NetworkResult<List<NotificationUiModel>> {
        return when (val result = NetworkUtils.safeApiCall { api.listarNotificacoes() }) {
            is NetworkResult.Success -> {
                val notifications = result.data.data?.docs
                    ?.map { it.toUiModel() }
                    .orEmpty()
                NetworkResult.Success(notifications)
            }
            is NetworkResult.Error -> result
            NetworkResult.Loading -> NetworkResult.Loading
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
}
