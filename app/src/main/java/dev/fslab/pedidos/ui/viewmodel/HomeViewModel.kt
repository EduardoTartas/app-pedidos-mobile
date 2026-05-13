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
    
    private var gpsCidade: String? = null
    private var gpsEstado: String? = null

    fun isInicializado() = inicializado

    private var lastUserId: String? = null

    fun carregarDados(usuarioId: String? = null, silent: Boolean = false, force: Boolean = false) {
        // Se não for forced, Evita recarregamento de navegação se o mesmo usuário já está carregado
        if (!force && !silent && inicializado && lastUserId == usuarioId) {
            return
        }

        if (!silent && (!inicializado || force)) {
            _uiState.value = HomeUiState.Loading
        }
        inicializado = true
        lastUserId = usuarioId

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
                        listaEnderecos = novaLista
                        
                        if (novaLista.isNotEmpty()) {
                            // LÓGICA DE SELEÇÃO:
                            val savedId = LocationPreferences.getSelectedId(getApplication())
                            val idAindaExiste = listaEnderecos.any { it.id == savedId }
                            
                            // Preserva "gps_location"
                            val isGpsLocation = savedId == "gps_location"
                            
                            // SÓ definimos um novo se o disco estiver vazio ou o endereço salvo foi deletado
                            if (savedId == null || (!idAindaExiste && !isGpsLocation)) {
                                val principal = listaEnderecos.find { it.principal } ?: listaEnderecos.firstOrNull()
                                principal?.let {
                                    LocationPreferences.saveSelectedId(getApplication(), it.id)
                                }
                            }
                        } else {
                            LocationPreferences.saveSelectedId(getApplication(), "gps_location")
                        }
                    }
                } else {
                    listaEnderecos = emptyList()
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
        val selectedEndereco = listaEnderecos.find { it.id == currentSelectedId } ?: if (currentSelectedId != "gps_location") listaEnderecos.find { it.principal } ?: listaEnderecos.firstOrNull() else null
        
        val catTudo = todasCategorias.find { it.nome.equals("Tudo", ignoreCase = true) }
        val effectiveCatId = if (selectedCategoryId == null) catTudo?.id else selectedCategoryId
        
        var filtrados = todosRestaurantes
        if (effectiveCatId != null && effectiveCatId != catTudo?.id) {
            filtrados = filtrados.filter { r -> r.categorias?.any { it.id == effectiveCatId } == true }
        }
        if (searchText.isNotBlank()) {
            filtrados = filtrados.filter { it.nome.contains(searchText, ignoreCase = true) }
        }

        val fallbackId = if (selectedEndereco != null) selectedEndereco.id else "gps_location"
        // Só salva o GPS como default se a pessoa não tiver nenhum endereço salvo (nem principal) e tivermos caido no fallback
        if (fallbackId == "gps_location" && currentSelectedId == null) {
            LocationPreferences.saveSelectedId(getApplication(), "gps_location")
        }

        _uiState.value = HomeUiState.Success(
            categorias = todasCategorias,
            recomendados = filtrados.take(5),
            populares = if (filtrados.size > 5) filtrados.drop(5) else filtrados,
            textoBusca = searchText,
            categoriaSelecionadaId = effectiveCatId,
            labelEndereco = selectedEndereco?.label ?: "",
            cidadeUsuario = selectedEndereco?.cidade ?: gpsCidade ?: "Sua localização",
            estadoUsuario = selectedEndereco?.estado ?: gpsEstado ?: "",
            enderecos = listaEnderecos,
            enderecoSelecionadoId = fallbackId,
            atualizando = false
        )
    }

    fun selecionarEndereco(endereco: Endereco) {
        // Grava no disco imediatamente
        LocationPreferences.saveSelectedId(getApplication(), endereco.id)
        publishState()
    }

    fun definirLocalizacao(cidade: String, estado: String) {
        // Guardamos as informações do GPS em cache
        gpsCidade = cidade
        gpsEstado = estado
        
        publishState()
    }

    fun atualizarDados(usuarioId: String? = null) {
        val estado = _uiState.value as? HomeUiState.Success
        if (estado != null) _uiState.value = estado.copy(atualizando = true)
        carregarDados(usuarioId, silent = true, force = true)
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
