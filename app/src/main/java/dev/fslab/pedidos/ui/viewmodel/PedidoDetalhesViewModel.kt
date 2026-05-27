package dev.fslab.pedidos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.pedidos.model.Pedido
import dev.fslab.pedidos.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PedidoDetalhesUiState {
    object Loading : PedidoDetalhesUiState()
    data class Success(val pedido: Pedido) : PedidoDetalhesUiState()
    data class Error(val message: String) : PedidoDetalhesUiState()
}

class PedidoDetalhesViewModel : ViewModel() {

    private val api = RetrofitClient.pedidoApi

    private val _uiState = MutableStateFlow<PedidoDetalhesUiState>(PedidoDetalhesUiState.Loading)
    val uiState: StateFlow<PedidoDetalhesUiState> = _uiState.asStateFlow()

    fun carregarPedido(pedidoId: String) {
        if (pedidoId.isBlank()) return
        
        _uiState.value = PedidoDetalhesUiState.Loading
        viewModelScope.launch {
            try {
                val response = api.obterPedido(pedidoId)
                if (response.isSuccessful) {
                    val pedido = response.body()?.data
                    if (pedido != null) {
                        _uiState.value = PedidoDetalhesUiState.Success(pedido)
                    } else {
                        _uiState.value = PedidoDetalhesUiState.Error("Pedido não encontrado.")
                    }
                } else {
                    _uiState.value = PedidoDetalhesUiState.Error("Erro ao carregar detalhes do pedido.")
                }
            } catch (e: Exception) {
                _uiState.value = PedidoDetalhesUiState.Error(
                    e.localizedMessage ?: "Erro de conexão ao buscar pedido."
                )
            }
        }
    }
}
