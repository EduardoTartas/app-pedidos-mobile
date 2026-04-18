package dev.fslab.pedidos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.pedidos.model.Categoria
import dev.fslab.pedidos.model.Restaurante
import dev.fslab.pedidos.network.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val categorias: List<Categoria>,
        val recomendados: List<Restaurante>,
        val populares: List<Restaurante>,
        val textoBusca: String = "",
        val categoriaSelecionadaId: String? = null,
        val cidadeUsuario: String = "Sua localização",
        val estadoUsuario: String = "",
        val atualizando: Boolean = false
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var todasCategorias: List<Categoria> = emptyList()
    private var todosRestaurantes: List<Restaurante> = emptyList()
    private var dadosCarregados = false
    private var trabalhoBusca: Job? = null
    
    private var textoBuscaAtual = ""
    private var categoriaAtualId: String? = null

    private var cidadeAtual = "Sua localização"
    private var estadoAtualUF = ""

    init {
        carregarDados()
    }

    fun carregarDados() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                // Obter categorias
                val categoriasResponse = RetrofitClient.categoriaApi.listarCategorias(limit = 100)
                if (categoriasResponse.isSuccessful) {
                    val catsBrutos = categoriasResponse.body()?.data?.docs ?: emptyList()
                    val catTudo = catsBrutos.find { it.nome.equals("Tudo", ignoreCase = true) }
                    todasCategorias = if (catTudo != null) {
                        listOf(catTudo) + catsBrutos.filter { it.id != catTudo.id }
                    } else {
                        catsBrutos
                    }
                    if (catTudo != null && categoriaAtualId == null) {
                        categoriaAtualId = catTudo.id
                    }
                }
                
                // Obter restaurantes
                val restaurantesResponse = RetrofitClient.restauranteApi.listarRestaurantes(limit = 100, nome = null, categoria = null)
                if (restaurantesResponse.isSuccessful) {
                    todosRestaurantes = restaurantesResponse.body()?.data?.docs ?: emptyList()
                    dadosCarregados = true
                } else {
                    _uiState.value = HomeUiState.Error("Erro ao carregar restaurantes.")
                    return@launch
                }
                
                aplicarFiltrosLocais()
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Erro desconhecido de conexão.")
            }
        }
    }

    fun atualizarDados() {
        val estadoAtual = _uiState.value as? HomeUiState.Success ?: return

        viewModelScope.launch {
            _uiState.value = estadoAtual.copy(atualizando = true)
            try {
                val categoriasResponse = RetrofitClient.categoriaApi.listarCategorias(limit = 100)
                if (categoriasResponse.isSuccessful) {
                    val catsBrutos = categoriasResponse.body()?.data?.docs ?: emptyList()
                    val catTudo = catsBrutos.find { it.nome.equals("Tudo", ignoreCase = true) }
                    todasCategorias = if (catTudo != null) {
                        listOf(catTudo) + catsBrutos.filter { it.id != catTudo.id }
                    } else {
                        catsBrutos
                    }
                    if (catTudo != null && categoriaAtualId == null) {
                        categoriaAtualId = catTudo.id
                    }
                }
                
                val restaurantesResponse = RetrofitClient.restauranteApi.listarRestaurantes(limit = 100, nome = null, categoria = null)
                if (restaurantesResponse.isSuccessful) {
                    todosRestaurantes = restaurantesResponse.body()?.data?.docs ?: emptyList()
                    dadosCarregados = true
                }
                
                aplicarFiltrosLocais()
            } catch (e: Exception) {
                aplicarFiltrosLocais()
            }
        }
    }

    private fun aplicarFiltrosLocais() {
        if (!dadosCarregados) return

        val catSelecionada = todasCategorias.find { it.id == categoriaAtualId }
        val idBuscaCategoria = if (catSelecionada?.nome.equals("Tudo", ignoreCase = true)) null else categoriaAtualId

        var filtrados = todosRestaurantes

        // Filtrar por categoria
        if (idBuscaCategoria != null) {
            filtrados = filtrados.filter { restaurante -> 
                restaurante.categorias?.any { cat -> cat.id == idBuscaCategoria } == true 
            }
        }

        // Filtrar por texto da busca
        if (textoBuscaAtual.isNotBlank()) {
            filtrados = filtrados.filter { 
                it.nome.contains(textoBuscaAtual, ignoreCase = true) 
            }
        }

        val recomendados = filtrados.take(5)
        val populares = if (filtrados.size > 5) filtrados.drop(5) else filtrados

        _uiState.value = HomeUiState.Success(
            categorias = todasCategorias,
            recomendados = recomendados,
            populares = populares,
            textoBusca = textoBuscaAtual,
            categoriaSelecionadaId = categoriaAtualId,
            cidadeUsuario = cidadeAtual,
            estadoUsuario = estadoAtualUF
        )
    }

    fun aoMudarTextoBusca(busca: String) {
        textoBuscaAtual = busca
        // Atualiza a UI imediatamente para não perder o foco
        val estadoAtual = _uiState.value as? HomeUiState.Success
        if (estadoAtual != null) {
            _uiState.value = estadoAtual.copy(textoBusca = busca)
        }

        // Debounce da busca
        trabalhoBusca?.cancel()
        trabalhoBusca = viewModelScope.launch {
            delay(300)
            aplicarFiltrosLocais()
        }
    }

    fun aoSelecionarCategoria(idCategoria: String?) {
        if (categoriaAtualId == idCategoria) return
        categoriaAtualId = idCategoria
        
        aplicarFiltrosLocais()
    }

    fun definirLocalizacao(cidade: String, estado: String) {
        cidadeAtual = cidade
        estadoAtualUF = estado
        
        val estadoAtual = _uiState.value as? HomeUiState.Success
        if (estadoAtual != null) {
            _uiState.value = estadoAtual.copy(
                cidadeUsuario = cidade,
                estadoUsuario = estado
            )
        }
    }
}
