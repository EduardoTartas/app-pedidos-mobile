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

// ═══════════════════════════════════════════
// ESTADO DOS FILTROS AVANÇADOS
// ═══════════════════════════════════════════
data class FiltrosAvancados(
    val status: String? = null,         // "aberto", "fechado" ou null (todos)
    val entregaGratis: Boolean = false,
    val avaliacaoMinima: Float = 0f,    // 0 = sem filtro, 1-5 = filtro
    val categoriaId: String? = null,    // ID da categoria selecionada
    val ordenarPor: String = "nome",    // "nome", "avaliacao_media", "taxa_entrega", "estimativa_entrega_min"
    val ordemDirecao: String = "asc"    // "asc" ou "desc"
)

sealed class RestaurantesUiState {
    object Loading : RestaurantesUiState()
    data class Success(
        val restaurantes: List<Restaurante>,
        val searchQuery: String = "",
        val selectedFilter: String = "Todos",
        val filtrosAvancados: FiltrosAvancados = FiltrosAvancados(),
        val categorias: List<Categoria> = emptyList(),
        val filtrosAtivos: Int = 0    // contagem de filtros ativos (para badge)
    ) : RestaurantesUiState()
    data class Error(val message: String) : RestaurantesUiState()
}

class RestaurantesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<RestaurantesUiState>(RestaurantesUiState.Loading)
    val uiState: StateFlow<RestaurantesUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var currentSearchQuery = ""
    private var currentFilter = "Todos"
    private var currentFiltrosAvancados = FiltrosAvancados()
    private var allCategorias: List<Categoria> = emptyList()

    val filters = listOf("Todos", "Melhores Avaliações", "Entrega Grátis")

    init {
        carregarDados()
    }

    fun carregarDados() {
        viewModelScope.launch {
            _uiState.value = RestaurantesUiState.Loading
            try {
                // Carregar categorias para o filtro avançado
                val categoriasResponse = RetrofitClient.categoriaApi.listarCategorias(limit = 100)
                if (categoriasResponse.isSuccessful) {
                    val catsBrutos = categoriasResponse.body()?.data?.docs ?: emptyList()
                    allCategorias = catsBrutos.filter { 
                        !it.nome.equals("Tudo", ignoreCase = true) 
                    }
                }
                buscarRestaurantes()
            } catch (e: Exception) {
                _uiState.value = RestaurantesUiState.Error(e.localizedMessage ?: "Erro de rede.")
            }
        }
    }

    private suspend fun buscarRestaurantes() {
        try {
            // Monta os parâmetros com base nos filtros rápidos + avançados
            val ordenar: String?
            val ordem: String?
            val entregaGratis: String?
            val avaliacaoMin: String?
            val status: String?
            val categoria: String?

            // Filtros rápidos (chips) sobrescrevem certas configurações
            when (currentFilter) {
                "Melhores Avaliações" -> {
                    ordenar = "avaliacao_media"
                    ordem = "desc"
                    entregaGratis = if (currentFiltrosAvancados.entregaGratis) "true" else null
                    avaliacaoMin = if (currentFiltrosAvancados.avaliacaoMinima > 0f)
                        currentFiltrosAvancados.avaliacaoMinima.toString() else null
                    status = currentFiltrosAvancados.status
                    categoria = currentFiltrosAvancados.categoriaId
                }
                "Entrega Grátis" -> {
                    ordenar = currentFiltrosAvancados.ordenarPor
                    ordem = currentFiltrosAvancados.ordemDirecao
                    entregaGratis = "true"
                    avaliacaoMin = if (currentFiltrosAvancados.avaliacaoMinima > 0f)
                        currentFiltrosAvancados.avaliacaoMinima.toString() else null
                    status = currentFiltrosAvancados.status
                    categoria = currentFiltrosAvancados.categoriaId
                }
                else -> {
                    // "Todos" — usa filtros avançados como estão
                    ordenar = currentFiltrosAvancados.ordenarPor
                    ordem = currentFiltrosAvancados.ordemDirecao
                    entregaGratis = if (currentFiltrosAvancados.entregaGratis) "true" else null
                    avaliacaoMin = if (currentFiltrosAvancados.avaliacaoMinima > 0f)
                        currentFiltrosAvancados.avaliacaoMinima.toString() else null
                    status = currentFiltrosAvancados.status
                    categoria = currentFiltrosAvancados.categoriaId
                }
            }

            val restaurantesResponse = RetrofitClient.restauranteApi.listarRestaurantes(
                limit = 30,
                nome = currentSearchQuery.ifBlank { null },
                ordenar = ordenar,
                ordem = ordem,
                entregaGratis = entregaGratis,
                avaliacaoMin = avaliacaoMin,
                status = status,
                categoria = categoria
            )

            if (restaurantesResponse.isSuccessful) {
                val restaurantes = restaurantesResponse.body()?.data?.docs ?: emptyList()

                _uiState.value = RestaurantesUiState.Success(
                    restaurantes = restaurantes,
                    searchQuery = currentSearchQuery,
                    selectedFilter = currentFilter,
                    filtrosAvancados = currentFiltrosAvancados,
                    categorias = allCategorias,
                    filtrosAtivos = contarFiltrosAtivos()
                )
            } else {
                _uiState.value = RestaurantesUiState.Error("Erro ao carregar restaurantes.")
            }
        } catch (e: Exception) {
            _uiState.value = RestaurantesUiState.Error(e.localizedMessage ?: "Erro de rede.")
        }
    }

    fun onSearchQueryChanged(query: String) {
        currentSearchQuery = query
        val currentState = _uiState.value as? RestaurantesUiState.Success
        if (currentState != null) {
            _uiState.value = currentState.copy(searchQuery = query)
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            buscarRestaurantes()
        }
    }

    fun onFilterSelected(filter: String) {
        if (currentFilter == filter) return
        currentFilter = filter

        val currentState = _uiState.value as? RestaurantesUiState.Success
        if (currentState != null) {
            _uiState.value = currentState.copy(selectedFilter = filter)
        } else {
            _uiState.value = RestaurantesUiState.Loading
        }

        viewModelScope.launch {
            buscarRestaurantes()
        }
    }

    // ═══════════════════════════════════════════
    // FILTROS AVANÇADOS (bottom sheet)
    // ═══════════════════════════════════════════

    fun aplicarFiltrosAvancados(filtros: FiltrosAvancados) {
        currentFiltrosAvancados = filtros
        // Reseta chip para "Todos" ao aplicar filtros avançados
        currentFilter = "Todos"

        _uiState.value = RestaurantesUiState.Loading
        viewModelScope.launch {
            buscarRestaurantes()
        }
    }

    fun limparFiltrosAvancados() {
        currentFiltrosAvancados = FiltrosAvancados()
        currentFilter = "Todos"

        _uiState.value = RestaurantesUiState.Loading
        viewModelScope.launch {
            buscarRestaurantes()
        }
    }

    private fun contarFiltrosAtivos(): Int {
        var count = 0
        if (currentFiltrosAvancados.status != null) count++
        if (currentFiltrosAvancados.entregaGratis) count++
        if (currentFiltrosAvancados.avaliacaoMinima > 0f) count++
        if (currentFiltrosAvancados.categoriaId != null) count++
        if (currentFiltrosAvancados.ordenarPor != "nome" || currentFiltrosAvancados.ordemDirecao != "asc") count++
        return count
    }
}
