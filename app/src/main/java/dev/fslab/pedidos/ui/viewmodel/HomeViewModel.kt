package dev.fslab.pedidos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.pedidos.model.Categoria
import dev.fslab.pedidos.model.Endereco
import dev.fslab.pedidos.model.Restaurante
import dev.fslab.pedidos.network.RetrofitClient
import dev.fslab.pedidos.utils.LocationPreferences
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
        val labelEndereco: String = "",
        val cidadeUsuario: String = "Sua localização",
        val estadoUsuario: String = "",
        val atualizando: Boolean = false,
        val enderecos: List<Endereco> = emptyList(),
        val enderecoSelecionadoId: String? = null
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var todasCategorias = emptyList<Categoria>()
    private var todosRestaurantes = emptyList<Restaurante>()
    private var listaEnderecos = emptyList<Endereco>()
    
    private var searchText = ""
    private var selectedCategoryId: String? = null
    
    private var loadJob: Job? = null
    private var searchJob: Job? = null
    private var inicializado = false

    fun carregarDados(usuarioId: String? = null, silent: Boolean = false) {
        if (!silent && !inicializado) {
            _uiState.value = HomeUiState.Loading
        }
        inicializado = true

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                // Fetch de Categorias e Restaurantes
                val catsTask = RetrofitClient.categoriaApi.listarCategorias(limit = 100)
                val restsTask = RetrofitClient.restauranteApi.listarRestaurantes(limit = 100)
                
                if (catsTask.isSuccessful) todasCategorias = catsTask.body()?.data?.docs ?: emptyList()
                if (restsTask.isSuccessful) todosRestaurantes = restsTask.body()?.data?.docs ?: emptyList()

                // Gestão de Endereços
                if (!usuarioId.isNullOrEmpty()) {
                    val endResp = RetrofitClient.enderecoApi.listarPorUsuario(usuarioId)
                    if (endResp.isSuccessful) {
                        val novaLista = endResp.body()?.data ?: emptyList()
                        if (novaLista.isNotEmpty()) {
                            listaEnderecos = novaLista
                            
                            // LÓGICA DE SELEÇÃO:
                            val savedId = LocationPreferences.getSelectedId(getApplication())
                            val idAindaExiste = listaEnderecos.any { it.id == savedId }
                            
                            // SÓ definimos um novo se o disco estiver vazio ou o endereço salvo foi deletado
                            if (savedId == null || !idAindaExiste) {
                                val principal = listaEnderecos.find { it.principal } ?: listaEnderecos.firstOrNull()
                                principal?.let {
                                    LocationPreferences.saveSelectedId(getApplication(), it.id)
                                }
                            }
                        }
                    }
                }
                
                publishState()
            } catch (e: Exception) {
                if (_uiState.value !is HomeUiState.Success) {
                    _uiState.value = HomeUiState.Error("Falha na sincronização.")
                }
            }
        }
    }

    private fun publishState() {
        val currentSelectedId = LocationPreferences.getSelectedId(getApplication())
        val selectedEndereco = listaEnderecos.find { it.id == currentSelectedId }
        
        val catTudo = todasCategorias.find { it.nome.equals("Tudo", ignoreCase = true) }
        val effectiveCatId = if (selectedCategoryId == null) catTudo?.id else selectedCategoryId
        
        var filtrados = todosRestaurantes
        if (effectiveCatId != null && effectiveCatId != catTudo?.id) {
            filtrados = filtrados.filter { r -> r.categorias?.any { it.id == effectiveCatId } == true }
        }
        if (searchText.isNotBlank()) {
            filtrados = filtrados.filter { it.nome.contains(searchText, ignoreCase = true) }
        }

        _uiState.value = HomeUiState.Success(
            categorias = todasCategorias,
            recomendados = filtrados.take(5),
            populares = if (filtrados.size > 5) filtrados.drop(5) else filtrados,
            textoBusca = searchText,
            categoriaSelecionadaId = effectiveCatId,
            labelEndereco = selectedEndereco?.label ?: "",
            cidadeUsuario = selectedEndereco?.cidade ?: "Sua localização",
            estadoUsuario = selectedEndereco?.estado ?: "",
            enderecos = listaEnderecos,
            enderecoSelecionadoId = currentSelectedId,
            atualizando = false
        )
    }

    fun selecionarEndereco(endereco: Endereco) {
        // Grava no disco imediatamente
        LocationPreferences.saveSelectedId(getApplication(), endereco.id)
        publishState()
    }

    fun definirLocalizacao(cidade: String, estado: String) {
        LocationPreferences.saveSelectedId(getApplication(), "gps_location")
        val catTudo = todasCategorias.find { it.nome.equals("Tudo", ignoreCase = true) }
        
        _uiState.value = HomeUiState.Success(
            categorias = todasCategorias,
            recomendados = todosRestaurantes.take(5),
            populares = todosRestaurantes.drop(5),
            textoBusca = searchText,
            categoriaSelecionadaId = selectedCategoryId ?: catTudo?.id,
            labelEndereco = "",
            cidadeUsuario = cidade,
            estadoUsuario = estado,
            enderecos = listaEnderecos,
            enderecoSelecionadoId = "gps_location",
            atualizando = false
        )
    }

    fun atualizarDados(usuarioId: String? = null) {
        val estado = _uiState.value as? HomeUiState.Success
        if (estado != null) _uiState.value = estado.copy(atualizando = true)
        carregarDados(usuarioId, silent = true)
    }

    fun aoMudarTextoBusca(busca: String) {
        searchText = busca
        val estado = _uiState.value as? HomeUiState.Success
        if (estado != null) _uiState.value = estado.copy(textoBusca = busca)
        
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            publishState()
        }
    }

    fun aoSelecionarCategoria(id: String?) {
        selectedCategoryId = id
        publishState()
    }
}
