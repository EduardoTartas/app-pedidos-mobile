package dev.fslab.pedidos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.pedidos.model.Prato
import dev.fslab.pedidos.model.Restaurante
import dev.fslab.pedidos.network.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════
// ESTADO DA TELA DE DETALHES
// ═══════════════════════════════════════════
sealed class DetalhesUiState {
    object Loading : DetalhesUiState()
    data class Success(
        val restaurante: Restaurante,
        val cardapioCompleto: Map<String, List<Prato>>,
        val cardapioFiltrado: Map<String, List<Prato>>,
        val secoes: List<String>,
        val secaoSelecionada: String? = null, // null = "Todas"
        val textoBusca: String = "",
        val buscaVisivel: Boolean = false
    ) : DetalhesUiState()
    data class Error(val message: String) : DetalhesUiState()
}

class RestauranteDetalhesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<DetalhesUiState>(DetalhesUiState.Loading)
    val uiState: StateFlow<DetalhesUiState> = _uiState.asStateFlow()

    private var buscaJob: Job? = null

    fun carregarDados(restauranteId: String) {
        viewModelScope.launch {
            _uiState.value = DetalhesUiState.Loading
            try {
                // Chamadas em paralelo
                val restauranteDeferred = async {
                    RetrofitClient.restauranteApi.buscarRestaurante(restauranteId)
                }
                val cardapioDeferred = async {
                    RetrofitClient.cardapioApi.buscarCardapio(restauranteId)
                }

                val restauranteResponse = restauranteDeferred.await()
                val cardapioResponse = cardapioDeferred.await()

                if (!restauranteResponse.isSuccessful || restauranteResponse.body()?.data == null) {
                    _uiState.value = DetalhesUiState.Error("Erro ao carregar detalhes do restaurante.")
                    return@launch
                }

                val restaurante = restauranteResponse.body()!!.data!!
                val cardapio = if (cardapioResponse.isSuccessful) {
                    cardapioResponse.body()?.data ?: emptyMap()
                } else {
                    emptyMap()
                }

                // Seções: pegar do restaurante ou das chaves do cardápio
                val secoes = restaurante.secoesCardapio?.takeIf { it.isNotEmpty() }
                    ?: cardapio.keys.toList()

                _uiState.value = DetalhesUiState.Success(
                    restaurante = restaurante,
                    cardapioCompleto = cardapio,
                    cardapioFiltrado = cardapio,
                    secoes = secoes
                )
            } catch (e: Exception) {
                _uiState.value = DetalhesUiState.Error(
                    e.localizedMessage ?: "Erro de conexão ao carregar restaurante."
                )
            }
        }
    }

    fun aoSelecionarSecao(secao: String?) {
        val current = _uiState.value as? DetalhesUiState.Success ?: return
        _uiState.value = current.copy(secaoSelecionada = secao)
    }

    fun aoMudarBusca(query: String) {
        val current = _uiState.value as? DetalhesUiState.Success ?: return
        _uiState.value = current.copy(textoBusca = query)

        buscaJob?.cancel()
        buscaJob = viewModelScope.launch {
            delay(300)
            val atualizado = _uiState.value as? DetalhesUiState.Success ?: return@launch
            _uiState.value = aplicarFiltros(atualizado)
        }
    }

    fun toggleBusca() {
        val current = _uiState.value as? DetalhesUiState.Success ?: return
        val novaVisibilidade = !current.buscaVisivel
        _uiState.value = if (novaVisibilidade) {
            current.copy(buscaVisivel = true)
        } else {
            // Ao fechar a busca, limpa o texto e reaplica filtros
            aplicarFiltros(current.copy(buscaVisivel = false, textoBusca = ""))
        }
    }

    private fun aplicarFiltros(state: DetalhesUiState.Success): DetalhesUiState.Success {
        var filtrado = state.cardapioCompleto

        // Removido o filtro de seção para permitir o scroll na tela
        // em vez de esconder os pratos das outras seções.

        // Filtrar por texto
        if (state.textoBusca.isNotBlank()) {
            filtrado = filtrado.mapValues { (_, pratos) ->
                pratos.filter { prato ->
                    prato.nome.contains(state.textoBusca, ignoreCase = true) ||
                    (prato.descricao?.contains(state.textoBusca, ignoreCase = true) == true)
                }
            }.filter { it.value.isNotEmpty() }
        }

        return state.copy(cardapioFiltrado = filtrado)
    }
}
