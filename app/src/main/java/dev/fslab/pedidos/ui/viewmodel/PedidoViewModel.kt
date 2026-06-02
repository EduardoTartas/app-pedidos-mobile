package dev.fslab.pedidos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.pedidos.model.AdicionalPedidoRequest
import dev.fslab.pedidos.model.CriarPedidoRequest
import dev.fslab.pedidos.model.Endereco
import dev.fslab.pedidos.model.EnderecoEntregaRequest
import dev.fslab.pedidos.model.ItemCarrinho
import dev.fslab.pedidos.model.ItemPedidoRequest
import dev.fslab.pedidos.model.Pedido
import dev.fslab.pedidos.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════════
// Estado da criação do pedido
// ═══════════════════════════════════════════════════════
sealed class PedidoUiState {
    object Idle : PedidoUiState()
    object Loading : PedidoUiState()
    data class Success(val pedido: dev.fslab.pedidos.model.Pedido) : PedidoUiState()
    data class Error(val message: String) : PedidoUiState()
}

class PedidoViewModel : ViewModel() {

    private val api = RetrofitClient.pedidoApi

    private val _uiState = MutableStateFlow<PedidoUiState>(PedidoUiState.Idle)
    val uiState: StateFlow<PedidoUiState> = _uiState.asStateFlow()

    /**
     * Converte os itens do carrinho no formato aceito pela API e envia o pedido.
     */
    fun realizarPedido(
        restauranteId: String,
        itens: List<ItemCarrinho>,
        endereco: Endereco?,
        formaPagamento: String = "pix"
    ) {
        if (restauranteId.isBlank()) {
            _uiState.value = PedidoUiState.Error("Restaurante inválido.")
            return
        }
        if (itens.isEmpty()) {
            _uiState.value = PedidoUiState.Error("O carrinho está vazio.")
            return
        }
        if (endereco == null) {
            _uiState.value = PedidoUiState.Error("Selecione um endereço de entrega.")
            return
        }

        viewModelScope.launch {
            _uiState.value = PedidoUiState.Loading
            try {
                val itensMapeados = itens.map { itemCarrinho ->
                    // Monta a lista de adicionais agrupando por opcao_id
                    val adicionaisPorOpcao = mutableMapOf<String, Int>()
                    itemCarrinho.selecoes.values.flatten().forEach { opcaoId ->
                        adicionaisPorOpcao[opcaoId] =
                            (adicionaisPorOpcao[opcaoId] ?: 0) + 1
                    }
                    val adicionais = adicionaisPorOpcao.map { (opcaoId, qty) ->
                        AdicionalPedidoRequest(opcaoId = opcaoId, quantidade = qty)
                    }

                    ItemPedidoRequest(
                        pratoId = itemCarrinho.prato.id,
                        quantidade = itemCarrinho.quantidade,
                        observacao = itemCarrinho.observacao,
                        adicionais = adicionais
                    )
                }

                // Mapeia o endereço para o formato do snapshot da API
                val enderecoEntrega = EnderecoEntregaRequest(
                    logradouro = endereco.rua,
                    numero = endereco.numero,
                    bairro = endereco.bairro,
                    cidade = endereco.cidade,
                    estado = endereco.estado,
                    cep = endereco.cep.replace("-", "").replace(".", ""),
                    complemento = endereco.complemento,
                    label = endereco.label
                )

                val request = CriarPedidoRequest(
                    restauranteId = restauranteId,
                    itens = itensMapeados,
                    enderecoEntrega = enderecoEntrega,
                    formaPagamento = formaPagamento
                )

                val response = api.criarPedido(request)

                if (response.isSuccessful) {
                    val pedido = response.body()?.data
                    if (pedido != null) {
                        _uiState.value = PedidoUiState.Success(pedido)
                    } else {
                        _uiState.value = PedidoUiState.Error("Resposta inesperada do servidor.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    // Tenta extrair a mensagem da API (JSON {"message":"..."})
                    val mensagem = extrairMensagemErro(errorBody, response.code())
                    _uiState.value = PedidoUiState.Error(mensagem)
                }
            } catch (e: Exception) {
                _uiState.value = PedidoUiState.Error(
                    e.message ?: "Erro de conexão. Verifique sua internet."
                )
            }
        }
    }

    /** Volta o estado para Idle (usado ao fechar o diálogo de erro ou nova tentativa) */
    fun resetar() {
        _uiState.value = PedidoUiState.Idle
    }

    // ─── Helpers ─────────────────────────────────────────
    private fun extrairMensagemErro(body: String, code: Int): String {
        return try {
            val gson = com.google.gson.Gson()
            val obj = gson.fromJson(body, com.google.gson.JsonObject::class.java)
            obj?.get("message")?.asString
                ?: obj?.get("error")?.asString
                ?: "Erro $code ao criar pedido."
        } catch (_: Exception) {
            "Erro $code ao criar pedido."
        }
    }
}
