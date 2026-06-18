package dev.fslab.pedidos.ui.viewmodel

import android.util.Log
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.pedidos.model.*
import dev.fslab.pedidos.network.RetrofitClient
import dev.fslab.pedidos.network.TokenManager
import dev.fslab.pedidos.network.AuthPreferences
import dev.fslab.pedidos.utils.NetworkUtils
import dev.fslab.pedidos.utils.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.gson.Gson

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class NeedsProfileCompletion(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "AuthViewModel"
    private val gson = Gson()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _profileComplete = MutableStateFlow<Boolean>(true)
    val profileComplete: StateFlow<Boolean> = _profileComplete.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    init {
        TokenManager.onSessionExpired = {
            viewModelScope.launch {
                logout()
            }
        }
        TokenManager.onTokensRefreshed = { access, refresh ->
            AuthPreferences.saveAccessToken(getApplication(), access)
            AuthPreferences.saveRefreshToken(getApplication(), refresh)
            _accessToken.value = access
        }
        checkSavedSession()
    }

    private fun checkSavedSession() {
        val context = getApplication<Application>()
        val savedAccessToken = AuthPreferences.getAccessToken(context)
        val savedRefreshToken = AuthPreferences.getRefreshToken(context)
        val cachedUserJson = AuthPreferences.getUser(context)

        // PASSO 1: Login instantâneo via cache
        if (!cachedUserJson.isNullOrEmpty() && !savedRefreshToken.isNullOrEmpty()) {
            try {
                val cachedUser = gson.fromJson(cachedUserJson, User::class.java)
                _currentUser.value = cachedUser

                if (!savedAccessToken.isNullOrEmpty()) {
                    TokenManager.saveTokens(savedAccessToken, savedRefreshToken)
                    _accessToken.value = savedAccessToken
                    _authState.value = AuthState.Success(cachedUser)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro cache", e)
            }
        }

        // PASSO 2: Validação via Handler Global
        if (!savedRefreshToken.isNullOrEmpty()) {
            viewModelScope.launch {
                val result = NetworkUtils.safeApiCall { 
                    RetrofitClient.authApi.refresh(RefreshRequest(refreshToken = savedRefreshToken)) 
                }

                when (result) {
                    is NetworkResult.Success -> {
                        val userPayload = result.data.getRemoteUser()
                        if (userPayload != null) {
                            handleAuthenticatedUser(userPayload)
                        } else {
                            logout()
                        }
                    }
                    is NetworkResult.Error -> {
                        // Se não temos cache, mostramos erro de conexão. Se temos, mantemos logado.
                        if (_authState.value !is AuthState.Success) {
                            if (result.code == 401) logout() 
                            else _authState.value = AuthState.Error(result.message)
                        }
                    }
                    else -> {}
                }
            }
        } else {
            _authState.value = AuthState.Idle
        }
    }

    fun loginUser(email: String, senha: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = NetworkUtils.safeApiCall { 
                RetrofitClient.authApi.login(LoginRequest(email, senha)) 
            }
            
            when (result) {
                is NetworkResult.Success -> {
                    val userPayload = result.data.getRemoteUser()
                    if (userPayload != null) {
                        handleAuthenticatedUser(userPayload)
                    } else {
                        _authState.value = AuthState.Error("Erro ao processar login")
                    }
                }
                is NetworkResult.Error -> _authState.value = AuthState.Error(result.message)
                else -> {}
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = NetworkUtils.safeApiCall { 
                RetrofitClient.authApi.googleLogin(GoogleLoginRequest(idToken)) 
            }
            
            when (result) {
                is NetworkResult.Success -> {
                    val userPayload = result.data.getRemoteUser()
                    if (userPayload != null) {
                        handleAuthenticatedUser(userPayload)
                    } else {
                        _authState.value = AuthState.Error("Erro ao processar login social")
                    }
                }
                is NetworkResult.Error -> _authState.value = AuthState.Error(result.message)
                else -> {}
            }
        }
    }

    fun registerUser(nome: String, email: String, senha: String, cpf: String, telefone: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = NetworkUtils.safeApiCall { 
                RetrofitClient.authApi.register(RegisterRequest(nome, email, senha, cpf, telefone)) 
            }
            
            _authState.value = AuthState.Idle
            when (result) {
                is NetworkResult.Success -> onSuccess()
                is NetworkResult.Error -> onError(result.message)
                else -> {}
            }
        }
    }

    fun recoverPassword(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = NetworkUtils.safeApiCall { 
                RetrofitClient.authApi.recoverPassword(RecoverPasswordRequest(email)) 
            }
            when (result) {
                is NetworkResult.Success -> onSuccess()
                is NetworkResult.Error -> onError(result.message)
                else -> {}
            }
        }
    }

    fun resetPasswordByCode(token: String, novaSenha: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = NetworkUtils.safeApiCall { 
                RetrofitClient.authApi.resetPasswordByCode(token, ResetPasswordRequest(novaSenha)) 
            }
            when (result) {
                is NetworkResult.Success -> onSuccess()
                is NetworkResult.Error -> onError(result.message)
                else -> {}
            }
        }
    }

    fun validatePasswordResetToken(token: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = NetworkUtils.safeApiCall { 
                RetrofitClient.authApi.validatePasswordResetToken(token) 
            }
            when (result) {
                is NetworkResult.Success -> onSuccess()
                is NetworkResult.Error -> onError(result.message)
                else -> {}
            }
        }
    }


    fun verifyEmail(token: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = NetworkUtils.safeApiCall { 
                RetrofitClient.authApi.verifyEmail(token) 
            }
            when (result) {
                is NetworkResult.Success -> onSuccess()
                is NetworkResult.Error -> onError(result.message)
                else -> {}
            }
        }
    }

    fun completeProfile(cpf: String, telefone: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val updateMap = mapOf("cpf" to cpf, "telefone" to telefone)
            val result = NetworkUtils.safeApiCall { 
                RetrofitClient.usuarioApi.atualizar(user.id, updateMap) 
            }
            
            when (result) {
                is NetworkResult.Success -> {
                    _profileComplete.value = true
                    val updatedUser = user.copy(cpf = cpf, telefone = telefone)
                    _currentUser.value = updatedUser
                    _authState.value = AuthState.Success(updatedUser)
                    onSuccess()
                }
                is NetworkResult.Error -> {
                    _authState.value = AuthState.NeedsProfileCompletion(user)
                    onError(result.message)
                }
                else -> {}
            }
        }
    }

    private fun handleAuthenticatedUser(payload: RemoteUser) {
        TokenManager.saveTokens(payload.accessToken, payload.refreshToken)
        AuthPreferences.saveAccessToken(getApplication(), payload.accessToken)
        AuthPreferences.saveRefreshToken(getApplication(), payload.refreshToken)
        
        val user = payload.toUser()
        _currentUser.value = user
        _profileComplete.value = payload.profileComplete
        _accessToken.value = payload.accessToken

        try {
            AuthPreferences.saveUser(getApplication(), gson.toJson(user))
        } catch (e: Exception) { }

        _authState.value = if (!payload.profileComplete) AuthState.NeedsProfileCompletion(user) else AuthState.Success(user)
    }

    fun logout() {
        TokenManager.clearTokens()
        AuthPreferences.clear(getApplication())
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) _authState.value = AuthState.Idle
    }
}
