package dev.fslab.pedidos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.pedidos.model.AvaliarPedidoRequest
import dev.fslab.pedidos.model.Pedido
import dev.fslab.pedidos.model.PedidoStatusRequest
import dev.fslab.pedidos.network.RetrofitClient
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

sealed class PedidoDetalhesUiState {
    object Loading : PedidoDetalhesUiState()
    data class Success(
        val pedido: Pedido,
        val atualizando: Boolean = false
    ) : PedidoDetalhesUiState()
    data class Error(val message: String) : PedidoDetalhesUiState()
}

class PedidoDetalhesViewModel : ViewModel() {

    private val api = RetrofitClient.pedidoApi
    private val avaliacaoApi = RetrofitClient.avaliacaoApi

    private val _uiState = MutableStateFlow<PedidoDetalhesUiState>(PedidoDetalhesUiState.Loading)
    val uiState: StateFlow<PedidoDetalhesUiState> = _uiState.asStateFlow()

    private val _isCancelling = MutableStateFlow(false)
    val isCancelling: StateFlow<Boolean> = _isCancelling.asStateFlow()

    private val _isAvaliando = MutableStateFlow(false)
    val isAvaliando: StateFlow<Boolean> = _isAvaliando.asStateFlow()
    
    private val _avaliacaoSucesso = MutableStateFlow(false)
    val avaliacaoSucesso: StateFlow<Boolean> = _avaliacaoSucesso.asStateFlow()

    private var mSocket: Socket? = null

    fun conectarSocket(pedidoId: String) {
        if (pedidoId.isBlank()) return
        
        try {
            // Nota: Em um ambiente de produção, esta URL deve vir do RetrofitClient ou de uma variavel de ambiente
            mSocket = IO.socket(RetrofitClient.BASE_URL)
            
            mSocket?.connect()

            mSocket?.on(Socket.EVENT_CONNECT) {
                mSocket?.emit("joinOrderRoom", pedidoId)
            }

            mSocket?.on("orderStatusUpdated") { args ->
                if (args.isNotEmpty()) {
                    try {
                        val data = args[0] as JSONObject
                        val novoStatus = data.getString("status")
                        
                        _uiState.update { currentState ->
                            if (currentState is PedidoDetalhesUiState.Success) {
                                currentState.copy(
                                    pedido = currentState.pedido.copy(status = novoStatus)
                                )
                            } else {
                                currentState
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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
                        conectarSocket(pedidoId) // Inicia a conexão socket ao carregar o pedido com sucesso
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

    fun refreshPedido(pedidoId: String) {
        val current = _uiState.value as? PedidoDetalhesUiState.Success
        if (current != null) {
            _uiState.value = current.copy(atualizando = true)
        }

        viewModelScope.launch {
            try {
                val response = api.obterPedido(pedidoId)
                if (response.isSuccessful) {
                    val pedido = response.body()?.data
                    if (pedido != null) {
                        _uiState.value = PedidoDetalhesUiState.Success(pedido, atualizando = false)
                    } else if (current != null) {
                        _uiState.value = current.copy(atualizando = false)
                    }
                } else if (current != null) {
                    _uiState.value = current.copy(atualizando = false)
                }
            } catch (e: Exception) {
                if (current != null) {
                    _uiState.value = current.copy(atualizando = false)
                }
            }
        }
    }

    fun cancelarPedido(pedidoId: String, onSucesso: () -> Unit = {}) {
        _isCancelling.value = true
        viewModelScope.launch {
            try {
                val response = api.atualizarStatus(pedidoId, PedidoStatusRequest("cancelado"))
                if (response.isSuccessful) {
                    carregarPedido(pedidoId)
                    onSucesso()
                }
            } catch (e: Exception) {
                // Erro silencioso ou log
            } finally {
                _isCancelling.value = false
            }
        }
    }

    fun confirmarEntrega(pedidoId: String, onSucesso: () -> Unit = {}) {
        _isCancelling.value = true
        viewModelScope.launch {
            try {
                val response = api.atualizarStatus(pedidoId, PedidoStatusRequest("entregue"))
                if (response.isSuccessful) {
                    carregarPedido(pedidoId)
                    onSucesso()
                }
            } catch (e: Exception) {
                // Erro silencioso ou log
            } finally {
                _isCancelling.value = false
            }
        }
    }

    fun avaliarPedido(pedidoId: String, nota: Int, descricao: String) {
        _isAvaliando.value = true
        viewModelScope.launch {
            try {
                val response = avaliacaoApi.avaliarPedido(
                    AvaliarPedidoRequest(
                        pedidoId = pedidoId,
                        nota = nota,
                        descricao = descricao.takeIf { it.isNotBlank() }
                    )
                )
                if (response.isSuccessful) {
                    _avaliacaoSucesso.value = true
                    carregarPedido(pedidoId)
                } else {
                    // Tratar erro
                }
            } catch (e: Exception) {
                // Tratar erro
            } finally {
                _isAvaliando.value = false
            }
        }
    }
    
    fun resetAvaliacao() {
        _avaliacaoSucesso.value = false
    }

    override fun onCleared() {
        super.onCleared()
        mSocket?.disconnect()
        mSocket?.off("orderStatusUpdated")
    }
}
