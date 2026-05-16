package dev.fslab.pedidos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.pedidos.model.Categoria
import dev.fslab.pedidos.model.Endereco
import dev.fslab.pedidos.model.Restaurante
import dev.fslab.pedidos.network.RetrofitClient
import dev.fslab.pedidos.utils.LocationPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                // Fetch paralelo usando async para ganhar tempo
                val catsDeferred = async { RetrofitClient.categoriaApi.listarCategorias(limit = 100) }
                val restsDeferred = async { RetrofitClient.restauranteApi.listarRestaurantes(limit = 100) }
                val endsDeferred = if (!usuarioId.isNullOrEmpty()) {
                    async { RetrofitClient.enderecoApi.listarPorUsuario(usuarioId) }
                } else null

                val catsTask = catsDeferred.await()
                val restsTask = restsDeferred.await()
                
                if (catsTask.isSuccessful) {
                    val rawCats = catsTask.body()?.data?.docs ?: emptyList()
                    todasCategorias = rawCats.sortedWith(compareByDescending { it.nome.equals("Tudo", ignoreCase = true) })
                }
                if (restsTask.isSuccessful) {
                    val rawRests = restsTask.body()?.data?.docs ?: emptyList()
                    // Correção visual: troca crase por apóstrofo para evitar fusão de caracteres (Ex: Paulo`s -> Paulo's)
                    todosRestaurantes = rawRests.map { rest ->
                        rest.copy(nome = rest.nome.replace("`", "'"))
                    }
                }

                // Gestão de Endereços
                if (endsDeferred != null) {
                    val endResp = endsDeferred.await()
                    if (endResp.isSuccessful) {
                        val novaLista = endResp.body()?.data ?: emptyList()
                        listaEnderecos = novaLista
                        
                        if (novaLista.isNotEmpty()) {
                            val savedId = LocationPreferences.getSelectedId(getApplication())
                            val idAindaExiste = listaEnderecos.any { it.id == savedId }
                            val isGpsLocation = savedId == "gps_location"
                            
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

    private suspend fun publishState() {
        // Movemos o processamento pesado de filtros para o Dispatcher Default (Worker Thread)
        withContext(Dispatchers.Default) {
            val currentSelectedId = LocationPreferences.getSelectedId(getApplication())
            val selectedEndereco = listaEnderecos.find { it.id == currentSelectedId } 
                ?: if (currentSelectedId != "gps_location") listaEnderecos.find { it.principal } ?: listaEnderecos.firstOrNull() 
                else null
            
            val catTudo = todasCategorias.find { it.nome.equals("Tudo", ignoreCase = true) }
            val effectiveCatId = if (selectedCategoryId == null) catTudo?.id else selectedCategoryId
            
            var filtrados = todosRestaurantes
            if (effectiveCatId != null && effectiveCatId != catTudo?.id) {
                filtrados = filtrados.filter { r -> r.categorias?.any { it.id == effectiveCatId } == true }
            }
            
            // OTIMIZAÇÃO: Busca insensível a acentos e maiúsculas
            if (searchText.isNotBlank()) {
                val normalizedQuery = normalizarString(searchText)
                filtrados = filtrados.filter { 
                    normalizarString(it.nome).contains(normalizedQuery) 
                }
            }

            val fallbackId = if (selectedEndereco != null) selectedEndereco.id else "gps_location"
            
            if (fallbackId == "gps_location" && currentSelectedId == null) {
                LocationPreferences.saveSelectedId(getApplication(), "gps_location")
            }

            // Voltamos para a Main Thread para atualizar a UI
            withContext(Dispatchers.Main) {
                _uiState.value = HomeUiState.Success(
                    categorias = todasCategorias,
                    recomendados = filtrados.take(5),
                    populares = filtrados,
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
        }
    }

    fun selecionarEndereco(endereco: Endereco) {
        LocationPreferences.saveSelectedId(getApplication(), endereco.id)
        viewModelScope.launch { publishState() }
    }

    fun definirLocalizacao(cidade: String, estado: String) {
        gpsCidade = cidade
        gpsEstado = estado
        viewModelScope.launch { publishState() }
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
        viewModelScope.launch { publishState() }
    }

    // Helper para remover acentos e deixar em minúsculo
    private fun normalizarString(texto: String): String {
        val normalizada = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
        return normalizada.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "").lowercase()
    }
}
