package dev.fslab.pedidos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.pedidos.model.Endereco
import dev.fslab.pedidos.model.EnderecoRequest
import dev.fslab.pedidos.network.RetrofitClient
import dev.fslab.pedidos.model.ViaCepResponse
import dev.fslab.pedidos.network.CepRetrofitClient
import dev.fslab.pedidos.utils.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EnderecoUiState {
    object Idle : EnderecoUiState()
    object Loading : EnderecoUiState()
    object CepLoading : EnderecoUiState()
    object Success : EnderecoUiState()
    data class Error(val message: String) : EnderecoUiState()
    data class CepLoaded(val data: ViaCepResponse) : EnderecoUiState()
}

class EnderecoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<EnderecoUiState>(EnderecoUiState.Idle)
    val uiState: StateFlow<EnderecoUiState> = _uiState.asStateFlow()

    private val _enderecos = MutableStateFlow<List<Endereco>>(emptyList())
    val enderecos: StateFlow<List<Endereco>> = _enderecos.asStateFlow()

    fun buscarCep(cep: String) {
        val cleanCep = cep.replace("-", "").replace(".", "").trim()
        if (cleanCep.length != 8) return

        viewModelScope.launch {
            _uiState.value = EnderecoUiState.CepLoading
            try {
                val response = CepRetrofitClient.cepApi.buscarCep(cleanCep)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.erro != true) {
                        _uiState.value = EnderecoUiState.CepLoaded(body)
                    } else {
                        _uiState.value = EnderecoUiState.Error("CEP não encontrado.")
                    }
                } else {
                    _uiState.value = EnderecoUiState.Error("Erro ao buscar CEP.")
                }
            } catch (e: Exception) {
                _uiState.value = EnderecoUiState.Error("Falha na conexão ao buscar CEP.")
            }
        }
    }

    fun buscarEnderecoPorCoordenadas(context: android.content.Context, lat: Double, lng: Double) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val geocoder = android.location.Geocoder(context, java.util.Locale("pt", "BR"))
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        addresses.firstOrNull()?.let { addr ->
                            val viaCepResponse = ViaCepResponse(
                                logradouro = addr.thoroughfare,
                                bairro = addr.subLocality,
                                localidade = addr.subAdminArea ?: addr.locality,
                                uf = getUf(addr.adminArea),
                                cep = addr.postalCode?.replace("-", "") ?: ""
                            )
                            _uiState.value = EnderecoUiState.CepLoaded(viaCepResponse)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    addresses?.firstOrNull()?.let { addr ->
                        val viaCepResponse = ViaCepResponse(
                            logradouro = addr.thoroughfare,
                            bairro = addr.subLocality,
                            localidade = addr.subAdminArea ?: addr.locality,
                            uf = getUf(addr.adminArea),
                            cep = addr.postalCode?.replace("-", "") ?: ""
                        )
                        _uiState.value = EnderecoUiState.CepLoaded(viaCepResponse)
                    }
                }
            } catch (e: Exception) {
                // Ignore geocoding errors so it doesn't crash
            }
        }
    }

    fun listarEnderecos(usuarioId: String) {
        viewModelScope.launch {
            _uiState.value = EnderecoUiState.Loading
            try {
                val response = RetrofitClient.enderecoApi.listarPorUsuario(usuarioId)
                if (response.isSuccessful) {
                    _enderecos.value = response.body()?.data ?: emptyList()
                    _uiState.value = EnderecoUiState.Idle
                } else {
                    val msg = NetworkUtils.getErrorMessage(response.errorBody(), "Não foi possível carregar os endereços.")
                    _uiState.value = EnderecoUiState.Error(msg)
                }
            } catch (e: Exception) {
                _uiState.value = EnderecoUiState.Error(e.localizedMessage ?: "Erro de conexão.")
            }
        }
    }

    fun criarEndereco(
        usuarioId: String,
        label: String,
        cep: String,
        rua: String,
        numero: String,
        bairro: String,
        complemento: String,
        cidade: String,
        estado: String,
        principal: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = EnderecoUiState.Loading
            try {
                val request = EnderecoRequest(
                    label = label,
                    cep = cep,
                    rua = rua,
                    numero = numero,
                    bairro = bairro,
                    complemento = complemento,
                    cidade = cidade,
                    estado = estado,
                    principal = principal
                )
                val response = RetrofitClient.enderecoApi.criarParaUsuario(usuarioId, request)
                if (response.isSuccessful) {
                    _uiState.value = EnderecoUiState.Success
                    onSuccess()
                } else {
                    val msg = NetworkUtils.getErrorMessage(response.errorBody(), "Erro ao salvar endereço.")
                    _uiState.value = EnderecoUiState.Error(msg)
                }
            } catch (e: Exception) {
                _uiState.value = EnderecoUiState.Error(e.localizedMessage ?: "Erro de conexão.")
            }
        }
    }

    fun atualizarEndereco(
        usuarioId: String,
        enderecoId: String,
        label: String,
        cep: String,
        rua: String,
        numero: String,
        bairro: String,
        complemento: String,
        cidade: String,
        estado: String,
        principal: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = EnderecoUiState.Loading
            try {
                val request = EnderecoRequest(
                    label = label,
                    cep = cep,
                    rua = rua,
                    numero = numero,
                    bairro = bairro,
                    complemento = complemento,
                    cidade = cidade,
                    estado = estado,
                    principal = principal
                )
                val response = RetrofitClient.enderecoApi.atualizar(usuarioId, enderecoId, request)
                if (response.isSuccessful) {
                    _uiState.value = EnderecoUiState.Success
                    onSuccess()
                } else {
                    val msg = NetworkUtils.getErrorMessage(response.errorBody(), "Erro ao atualizar endereço.")
                    _uiState.value = EnderecoUiState.Error(msg)
                }
            } catch (e: Exception) {
                _uiState.value = EnderecoUiState.Error(e.localizedMessage ?: "Erro de conexão.")
            }
        }
    }

    fun deletarEndereco(usuarioId: String, enderecoId: String) {
        viewModelScope.launch {
            _uiState.value = EnderecoUiState.Loading
            try {
                val response = RetrofitClient.enderecoApi.deletar(usuarioId, enderecoId)
                if (response.isSuccessful) {
                    listarEnderecos(usuarioId) // Recarrega a lista
                } else {
                    val msg = NetworkUtils.getErrorMessage(response.errorBody(), "Erro ao remover endereço.")
                    _uiState.value = EnderecoUiState.Error(msg)
                }
            } catch (e: Exception) {
                _uiState.value = EnderecoUiState.Error(e.localizedMessage ?: "Erro de conexão.")
            }
        }
    }

    private fun getUf(stateName: String?): String? {
        if (stateName == null) return null
        if (stateName.length == 2) return stateName.uppercase()
        
        val map = mapOf(
            "acre" to "AC", "alagoas" to "AL", "amapá" to "AP", "amapa" to "AP",
            "amazonas" to "AM", "bahia" to "BA", "ceará" to "CE", "ceara" to "CE",
            "distrito federal" to "DF", "espírito santo" to "ES", "espirito santo" to "ES",
            "goiás" to "GO", "goias" to "GO", "maranhão" to "MA", "maranhao" to "MA",
            "mato grosso" to "MT", "mato grosso do sul" to "MS", "minas gerais" to "MG",
            "pará" to "PA", "para" to "PA", "paraíba" to "PB", "paraiba" to "PB",
            "paraná" to "PR", "parana" to "PR", "pernambuco" to "PE", "piauí" to "PI",
            "piaui" to "PI", "rio de janeiro" to "RJ", "rio grande do norte" to "RN",
            "rio grande do sul" to "RS", "rondônia" to "RO", "rondonia" to "RO",
            "roraima" to "RR", "santa catarina" to "SC", "são paulo" to "SP",
            "sao paulo" to "SP", "sergipe" to "SE", "tocantins" to "TO"
        )
        return map[stateName.lowercase()] ?: stateName
    }

    fun resetState() {
        _uiState.value = EnderecoUiState.Idle
    }
}
