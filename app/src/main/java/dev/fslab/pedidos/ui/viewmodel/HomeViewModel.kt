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
        val searchQuery: String = "",
        val selectedCategoriaId: String? = null,
        val locationCity: String = "Sua localização",
        val locationState: String = ""
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var allCategorias: List<Categoria> = emptyList()
    private var searchJob: Job? = null
    
    private var currentSearchQuery = ""
    private var currentCategoriaId: String? = null

    private var currentCity = "Sua localização"
    private var currentStateUF = ""

    init {
        carregarDados()
    }

    fun carregarDados() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val categoriasResponse = RetrofitClient.categoriaApi.listarCategorias(limit = 10)
                if (categoriasResponse.isSuccessful) {
                    val rawCats = categoriasResponse.body()?.data?.docs ?: emptyList()
                    val tudoCat = rawCats.find { it.nome.equals("Tudo", ignoreCase = true) }
                    allCategorias = if (tudoCat != null) {
                        listOf(tudoCat) + rawCats.filter { it.id != tudoCat.id }
                    } else {
                        rawCats
                    }
                    if (tudoCat != null && currentCategoriaId == null) {
                        currentCategoriaId = tudoCat.id
                    }
                }
                
                buscarRestaurantes()
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Erro desconhecido de conexão.")
            }
        }
    }

    private suspend fun buscarRestaurantes() {
        try {
            val selectedCat = allCategorias.find { it.id == currentCategoriaId }
            val searchCatId = if (selectedCat?.nome.equals("Tudo", ignoreCase = true)) null else currentCategoriaId

            val restaurantesResponse = RetrofitClient.restauranteApi.listarRestaurantes(
                limit = 30,
                nome = currentSearchQuery.ifBlank { null },
                categoria = searchCatId
            )

            if (restaurantesResponse.isSuccessful) {
                val todosRestaurantes = restaurantesResponse.body()?.data?.docs ?: emptyList()

                // Regra exigida: Separar restaurantes (Recomendados / Populares)
                val recomendados = todosRestaurantes.take(5)
                val populares = if (todosRestaurantes.size > 5) todosRestaurantes.drop(5) else todosRestaurantes

                _uiState.value = HomeUiState.Success(
                    categorias = allCategorias,
                    recomendados = recomendados,
                    populares = populares,
                    searchQuery = currentSearchQuery,
                    selectedCategoriaId = currentCategoriaId,
                    locationCity = currentCity,
                    locationState = currentStateUF
                )
            } else {
                _uiState.value = HomeUiState.Error("Erro ao carregar restaurantes.")
            }
        } catch (e: Exception) {
            _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Erro de rede.")
        }
    }

    fun onSearchQueryChanged(query: String) {
        currentSearchQuery = query
        // Atualiza a UI imediatamente para nao perder o foco
        val currentState = _uiState.value as? HomeUiState.Success
        if (currentState != null) {
            _uiState.value = currentState.copy(searchQuery = query)
        }

        // Debounce da busca
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            _uiState.value = HomeUiState.Loading
            buscarRestaurantes()
        }
    }

    fun onCategoriaSelected(categoriaId: String?) {
        if (currentCategoriaId == categoriaId) return // Same category toggle behavior can be added
        currentCategoriaId = categoriaId
        
        val currentState = _uiState.value as? HomeUiState.Success
        if (currentState != null) {
            _uiState.value = currentState.copy(selectedCategoriaId = categoriaId)
        } else {
            _uiState.value = HomeUiState.Loading
        }
        
        viewModelScope.launch {
            buscarRestaurantes()
        }
    }

    fun setLocation(city: String, state: String) {
        currentCity = city
        currentStateUF = state
        
        val currentState = _uiState.value as? HomeUiState.Success
        if (currentState != null) {
            _uiState.value = currentState.copy(
                locationCity = city,
                locationState = state
            )
        }
        
        // Se a API suportar envio de cidade, você poderá modificar a chamada do buscarRestaurantes() aqui
    }
}
