package dev.fslab.pedidos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.pedidos.model.Categoria
import dev.fslab.pedidos.model.Endereco
import dev.fslab.pedidos.model.Restaurante
import dev.fslab.pedidos.model.RefreshRequest
import dev.fslab.pedidos.network.RetrofitClient
import dev.fslab.pedidos.utils.LocationPreferences
import dev.fslab.pedidos.utils.NetworkUtils
import dev.fslab.pedidos.utils.NetworkResult
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
            // Usando safeApiCall para evitar crashes se a API estiver offline
            val catsResult = NetworkUtils.safeApiCall { RetrofitClient.categoriaApi.listarCategorias(limit = 100) }
            val restsResult = NetworkUtils.safeApiCall { RetrofitClient.restauranteApi.listarRestaurantes(limit = 100) }
            
            val endsResult = if (!usuarioId.isNullOrEmpty()) {
                NetworkUtils.safeApiCall { RetrofitClient.enderecoApi.listarPorUsuario(usuarioId) }
            } else null

            // Processar Categorias
            if (catsResult is NetworkResult.Success) {
                val rawCats = catsResult.data.data?.docs ?: emptyList()
                todasCategorias = rawCats.sortedWith(compareByDescending { it.nome.equals("Tudo", ignoreCase = true) })
            }

            // Processar Restaurantes
            if (restsResult is NetworkResult.Success) {
                val rawRests = restsResult.data.data?.docs ?: emptyList()
                todosRestaurantes = rawRests.map { it.copy(nome = it.nome.replace("`", "'")) }
            }

            // Processar Endereços
            if (endsResult is NetworkResult.Success) {
                val novaLista = endsResult.data.data ?: emptyList()
                listaEnderecos = novaLista
                if (novaLista.isNotEmpty()) {
                    val savedId = LocationPreferences.getSelectedId(getApplication())
                    val idAindaExiste = listaEnderecos.any { it.id == savedId }
                    if (savedId == null || (!idAindaExiste && savedId != "gps_location")) {
                        val principal = listaEnderecos.find { it.principal } ?: listaEnderecos.firstOrNull()
                        principal?.let { LocationPreferences.saveSelectedId(getApplication(), it.id) }
                    }
                }
            }

            // Se falhou tudo (Erro de conexão) e não temos nada carregado, mostramos erro
            if (catsResult is NetworkResult.Error && restsResult is NetworkResult.Error && todasCategorias.isEmpty()) {
                _uiState.value = HomeUiState.Error(catsResult.message)
                return@launch // SAIR DA COROUTINE AQUI PARA NÃO CHAMAR PUBLISHSTATE
            } else {
                publishState()
            }
        }
    }

    private suspend fun publishState() {
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
            
            if (searchText.isNotBlank()) {
                val normalizedQuery = normalizarString(searchText)
                filtrados = filtrados.filter { normalizarString(it.nome).contains(normalizedQuery) }
            }

            // Ordenar: restaurantes abertos primeiro
            filtrados = filtrados.sortedByDescending { it.status == "aberto" }

            val fallbackId = if (selectedEndereco != null) selectedEndereco.id else "gps_location"
            
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

    private fun normalizarString(texto: String): String {
        val normalizada = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
        return normalizada.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "").lowercase()
    }
}
