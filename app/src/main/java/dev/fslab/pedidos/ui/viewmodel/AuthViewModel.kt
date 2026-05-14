package dev.fslab.pedidos.ui.viewmodel

import android.util.Log
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.pedidos.model.*
import dev.fslab.pedidos.network.RetrofitClient
import dev.fslab.pedidos.network.TokenManager
import dev.fslab.pedidos.utils.NetworkUtils
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estados possíveis da autenticação
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class NeedsProfileCompletion(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * AuthViewModel - Gerencia o estado de autenticação da aplicação
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "AuthViewModel"
        private val gson = Gson()
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _profileComplete = MutableStateFlow(true)
    val profileComplete: StateFlow<Boolean> = _profileComplete.asStateFlow()

    init {
        TokenManager.onSessionExpired = {
            viewModelScope.launch {
                Log.w(TAG, "Sessão expirada — fazendo logout automático")
                dev.fslab.pedidos.network.AuthPreferences.clear(getApplication())
                dev.fslab.pedidos.utils.LocationPreferences.clear(getApplication())
                _accessToken.value = null
                _currentUser.value = null
                _authState.value = AuthState.Error("Sessão expirada. Faça login novamente.")
            }
        }

        TokenManager.onTokensRefreshed = { newAccessToken ->
            viewModelScope.launch {
                Log.d(TAG, "Tokens renovados silenciosamente")
                _accessToken.value = newAccessToken
            }
        }

        checkSavedSession()
    }

    private fun checkSavedSession() {
        val context = getApplication<Application>()
        val savedToken = dev.fslab.pedidos.network.AuthPreferences.getRefreshToken(context)
        val cachedUserJson = dev.fslab.pedidos.network.AuthPreferences.getUser(context)

        // PASSO 1: Tentativa de login instantâneo via cache
        if (!cachedUserJson.isNullOrEmpty() && !savedToken.isNullOrEmpty()) {
            try {
                val cachedUser = gson.fromJson(cachedUserJson, User::class.java)
                _currentUser.value = cachedUser
                _authState.value = AuthState.Success(cachedUser)
                Log.d(TAG, "Login instantâneo via cache para: ${cachedUser.nome}")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao decodificar usuário do cache", e)
            }
        }

        // PASSO 2: Validação em background (ou login inicial se não houver cache)
        if (!savedToken.isNullOrEmpty()) {
            viewModelScope.launch {
                try {
                    val request = RefreshRequest(refreshToken = savedToken)
                    val response = RetrofitClient.authApi.refresh(request)
                    val remoteUser = response.getRemoteUser()

                    if (response.isSuccess() && remoteUser != null) {
                        handleAuthenticatedUser(remoteUser)
                        Log.d(TAG, "Sessão validada e atualizada com sucesso")
                    } else {
                        throw Exception(response.getErrorMessage().ifEmpty { "Token inválido" })
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Falha na validação da sessão salva: ${e.message}")
                    // Só desloga se não conseguirmos validar e o erro for crítico (ex: 401)
                    // Se for erro de rede, mantemos o estado de cache se já estivermos logados
                    if (_authState.value !is AuthState.Success) {
                        logout()
                    }
                }
            }
        } else {
            _authState.value = AuthState.Idle
        }
    }

    // ═══════════════════════════════════════════
    // LOGIN
    // ═══════════════════════════════════════════

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                val request = LoginRequest(email = email, senha = password)
                val response = RetrofitClient.authApi.login(request)
                val remoteUser = response.getRemoteUser()
                if (response.isSuccess() && remoteUser != null) {
                    handleAuthenticatedUser(remoteUser)
                } else {
                    val errorMessage = response.getErrorMessage().ifEmpty { "Credenciais inválidas" }
                    _authState.value = AuthState.Error(errorMessage)
                }
            } catch (e: retrofit2.HttpException) {
                val errorMessage = NetworkUtils.getErrorMessage(e.response()?.errorBody(), "Erro ao autenticar")
                _authState.value = AuthState.Error(errorMessage)
            } catch (e: java.net.UnknownHostException) {
                _authState.value = AuthState.Error("Sem conexão com a internet")
            } catch (e: java.net.SocketTimeoutException) {
                _authState.value = AuthState.Error("Tempo de conexão esgotado")
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Erro ao conectar: ${e.localizedMessage ?: "Tente novamente"}")
            }
        }
    }

    // ═══════════════════════════════════════════
    // LOGIN COM GOOGLE
    // ═══════════════════════════════════════════

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                val request = GoogleLoginRequest(idToken = idToken)
                val response = RetrofitClient.authApi.googleLogin(request)
                val remoteUser = response.getRemoteUser()

                if (response.isSuccess() && remoteUser != null) {
                    handleAuthenticatedUser(remoteUser)
                } else {
                    val errorMessage = response.getErrorMessage().ifEmpty { "Erro ao autenticar com Google" }
                    _authState.value = AuthState.Error(errorMessage)
                }
            } catch (e: retrofit2.HttpException) {
                val errorMessage = NetworkUtils.getErrorMessage(e.response()?.errorBody(), "Erro ao autenticar com Google")
                _authState.value = AuthState.Error(errorMessage)
            } catch (e: java.net.UnknownHostException) {
                _authState.value = AuthState.Error("Sem conexão com a internet")
            } catch (e: java.net.SocketTimeoutException) {
                _authState.value = AuthState.Error("Tempo de conexão esgotado")
            } catch (e: Exception) {
                Log.e(TAG, "Erro no login com Google", e)
                _authState.value = AuthState.Error("Erro ao conectar: ${e.localizedMessage ?: "Tente novamente"}")
            }
        }
    }

    // ═══════════════════════════════════════════
    // COMPLETAR PERFIL (CPF + TELEFONE)
    // ═══════════════════════════════════════════

    fun completeProfile(
        cpf: String,
        telefone: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = _currentUser.value?.id ?: run {
            onError("Usuário não encontrado na sessão")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val body = mapOf("cpf" to cpf, "telefone" to telefone)
                RetrofitClient.usuarioApi.atualizar(userId, body)

                _profileComplete.value = true
                _currentUser.value = _currentUser.value?.copy(
                    cpf = cpf,
                    telefone = telefone,
                    profileComplete = true
                )
                val updatedUser = _currentUser.value
                if (updatedUser != null) {
                    _authState.value = AuthState.Success(updatedUser)
                }
                onSuccess()
            } catch (e: retrofit2.HttpException) {
                _authState.value = AuthState.Idle
                val msg = NetworkUtils.getErrorMessage(e.response()?.errorBody(), "Erro ao atualizar perfil")
                onError(msg)
            } catch (e: java.net.UnknownHostException) {
                _authState.value = AuthState.Idle
                onError("Sem conexão com a internet")
            } catch (e: Exception) {
                _authState.value = AuthState.Idle
                Log.e(TAG, "Erro ao completar perfil", e)
                onError("Erro de conexão: ${e.localizedMessage ?: "Tente novamente"}")
            }
        }
    }

    // ═══════════════════════════════════════════
    // CADASTRO (REGISTER)
    // ═══════════════════════════════════════════

    fun registerUser(
        nome: String,
        email: String,
        senha: String,
        cpf: String,
        telefone: String,
        fotoPerfil: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                val request = RegisterRequest(
                    nome = nome,
                    email = email,
                    senha = senha,
                    cpf = cpf,
                    telefone = telefone,
                    fotoPerfil = fotoPerfil
                )
                val response = RetrofitClient.authApi.register(request)

                if (response.isSuccess()) {
                    _authState.value = AuthState.Idle
                    onSuccess()
                } else {
                    _authState.value = AuthState.Idle
                    onError(response.getErrorMessage().ifEmpty { "Erro ao criar conta" })
                }
            } catch (e: retrofit2.HttpException) {
                _authState.value = AuthState.Idle
                val msg = NetworkUtils.getErrorMessage(e.response()?.errorBody(), "Erro ao criar conta")
                onError(msg)
            } catch (e: java.net.UnknownHostException) {
                _authState.value = AuthState.Idle
                onError("Sem conexão com a internet")
            } catch (e: java.net.SocketTimeoutException) {
                _authState.value = AuthState.Idle
                onError("Tempo de conexão esgotado")
            } catch (e: Exception) {
                _authState.value = AuthState.Idle
                onError("Erro ao conectar: ${e.localizedMessage ?: "Tente novamente"}")
            }
        }
    }

    // ═══════════════════════════════════════════
    // RECUPERAÇÃO DE SENHA
    // ═══════════════════════════════════════════

    fun recoverPassword(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val request = RecoverPasswordRequest(email = email)
                RetrofitClient.authApi.recoverPassword(request)
                onSuccess()
            } catch (e: Exception) {
                // Retorna sucesso mesmo se e-mail não existir (segurança)
                onSuccess()
            }
        }
    }

    fun resetPasswordByCode(
        codigo: String,
        novaSenha: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val request = ResetPasswordRequest(senha = novaSenha)
                RetrofitClient.authApi.resetPasswordByCode(codigo, request)
                onSuccess()
            } catch (e: retrofit2.HttpException) {
                val errorMessage = when (e.code()) {
                    400 -> "Código inválido ou expirado"
                    404 -> "Código não encontrado"
                    else -> "Erro ao redefinir senha"
                }
                onError(errorMessage)
            } catch (e: Exception) {
                onError("Erro de conexão. Tente novamente.")
            }
        }
    }

    // ═══════════════════════════════════════════
    // LOGOUT
    // ═══════════════════════════════════════════

    fun logout() {
        val currentToken = TokenManager.getAccessToken()

        // Limpeza local síncrona
        TokenManager.clearTokens()
        dev.fslab.pedidos.network.AuthPreferences.clear(getApplication())
        dev.fslab.pedidos.utils.LocationPreferences.clear(getApplication())
        _accessToken.value = null
        _currentUser.value = null
        _profileComplete.value = true
        _authState.value = AuthState.Idle

        // Invalida token no servidor (best-effort)
        viewModelScope.launch {
            try {
                currentToken?.let { token ->
                    RetrofitClient.authApi.logout("Bearer $token")
                }
            } catch (_: Exception) {
                // Ignora — sessão local já foi encerrada
            }
        }
    }

    // ═══════════════════════════════════════════
    // UTILITÁRIOS
    // ═══════════════════════════════════════════

    private fun handleAuthenticatedUser(payload: RemoteUser) {
        if (!payload.hasValidSession()) {
            _authState.value = AuthState.Error("Sessão inválida retornada pela API")
            return
        }

        TokenManager.saveTokens(payload.accessToken, payload.refreshToken)
        dev.fslab.pedidos.network.AuthPreferences.saveRefreshToken(getApplication(), payload.refreshToken)

        _accessToken.value = payload.accessToken

        val user = payload.toUser()
        _currentUser.value = user
        _profileComplete.value = payload.profileComplete

        // Salva usuário no cache para login instantâneo na próxima abertura
        try {
            val userJson = gson.toJson(user)
            dev.fslab.pedidos.network.AuthPreferences.saveUser(getApplication(), userJson)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar usuário no cache", e)
        }

        if (!payload.profileComplete) {
            _authState.value = AuthState.NeedsProfileCompletion(user)
        } else {
            _authState.value = AuthState.Success(user)
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }
}
