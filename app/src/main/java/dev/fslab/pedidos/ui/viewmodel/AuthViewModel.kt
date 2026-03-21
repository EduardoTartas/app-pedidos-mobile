package dev.fslab.pedidos.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.pedidos.model.*
import dev.fslab.pedidos.network.RetrofitClient
import dev.fslab.pedidos.network.TokenManager
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
    data class Error(val message: String) : AuthState()
}

/**
 * AuthViewModel - Gerencia o estado de autenticação da aplicação
 *
 * Responsável por:
 * - Login via API (POST /login)
 * - Cadastro (POST /signup)
 * - Recuperação de senha (POST /recover)
 * - Gerenciar tokens JWT e estado do usuário
 */
class AuthViewModel : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
        private val gson = Gson()
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    init {
        TokenManager.onSessionExpired = {
            viewModelScope.launch {
                Log.w(TAG, "Sessão expirada — fazendo logout automático")
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
                val errorBody = e.response()?.errorBody()?.string()
                val apiMessage = try {
                    gson.fromJson(errorBody, ApiErrorResponse::class.java)?.getErrorMessage()
                } catch (_: Exception) { null }

                val errorMessage = apiMessage ?: when (e.code()) {
                    401 -> "Email ou senha incorretos"
                    403 -> "Usuário inativo ou bloqueado"
                    404 -> "Usuário não encontrado"
                    500 -> "Erro no servidor. Tente novamente mais tarde."
                    else -> "Erro ao autenticar (${e.code()})"
                }
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
                val errorBody = e.response()?.errorBody()?.string()
                val msg = try {
                    gson.fromJson(errorBody, ApiErrorResponse::class.java)?.getErrorMessage()
                } catch (_: Exception) { null }

                val errorMessage = when (e.code()) {
                    400 -> msg ?: "Dados inválidos. Verifique os campos."
                    409 -> "Este e-mail já está cadastrado"
                    else -> msg ?: "Erro ao criar conta (${e.code()})"
                }
                onError(errorMessage)
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
        _accessToken.value = null
        _currentUser.value = null
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
        _accessToken.value = payload.accessToken

        val user = payload.toUser()
        _currentUser.value = user
        _authState.value = AuthState.Success(user)
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }
}
