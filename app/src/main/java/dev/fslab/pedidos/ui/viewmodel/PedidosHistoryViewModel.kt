package dev.fslab.pedidos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.pedidos.model.Pedido
import dev.fslab.pedidos.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PedidosHistoryUiState {
    object Loading : PedidosHistoryUiState()
    data class Success(val pedidos: List<Pedido>) : PedidosHistoryUiState()
    data class Error(val message: String) : PedidosHistoryUiState()
}

class PedidosHistoryViewModel : ViewModel() {

    private val api = RetrofitClient.pedidoApi

    private val _uiState = MutableStateFlow<PedidosHistoryUiState>(PedidosHistoryUiState.Loading)
    val uiState: StateFlow<PedidosHistoryUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        carregarPedidos()
    }

    fun carregarPedidos(isRefresh: Boolean = false) {
        if (isRefresh) _isRefreshing.value = true
        else _uiState.value = PedidosHistoryUiState.Loading

        viewModelScope.launch {
            try {
                val response = api.listarMeusPedidos(limit = 50)
                if (response.isSuccessful) {
                    val lista = response.body()?.data?.docs ?: emptyList()
                    _uiState.value = PedidosHistoryUiState.Success(lista)
                } else {
                    if (!isRefresh) {
                        _uiState.value = PedidosHistoryUiState.Error("Erro ao carregar histórico de pedidos.")
                    }
                }
            } catch (e: Exception) {
                if (!isRefresh) {
                    _uiState.value = PedidosHistoryUiState.Error(
                        e.localizedMessage ?: "Erro de conexão ao buscar pedidos."
                    )
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refresh() {
        carregarPedidos(isRefresh = true)
    }
}
