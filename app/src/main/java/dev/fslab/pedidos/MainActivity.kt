package dev.fslab.pedidos

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dev.fslab.pedidos.ui.screens.auth.CompletarPerfilScreen
import dev.fslab.pedidos.ui.screens.auth.EsqueciSenhaScreen
import dev.fslab.pedidos.ui.screens.auth.LoginScreen
import dev.fslab.pedidos.ui.screens.auth.CadastroScreen
import dev.fslab.pedidos.ui.screens.HomeScreen
import dev.fslab.pedidos.ui.screens.RestaurantesScreen
import dev.fslab.pedidos.ui.theme.PedidosTheme
import dev.fslab.pedidos.ui.viewmodel.AuthState
import dev.fslab.pedidos.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PedidosApp(activity = this)
        }
    }
}

/**
 * Google Web Client ID — usado pelo Credential Manager para solicitar o ID Token.
 * Este é o CLIENT_ID do tipo "Web application" configurado no Google Cloud Console.
 */
private const val GOOGLE_WEB_CLIENT_ID =
    "1053347409082-qb4s3d724bp69hs78kdt38s35brinr7n.apps.googleusercontent.com"

@Composable
fun PedidosApp(activity: ComponentActivity) {
    val systemDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(systemDark) }
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

    val coroutineScope = rememberCoroutineScope()

    PedidosTheme(darkTheme = isDarkTheme) {
        val navController = rememberNavController()

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "login",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("login") {
                    LaunchedEffect(authState) {
                        when (authState) {
                            is AuthState.Success -> {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            is AuthState.NeedsProfileCompletion -> {
                                navController.navigate("completar_perfil") {
                                    popUpTo("login") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            else -> {}
                        }
                    }

                    LoginScreen(
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = { isDarkTheme = !isDarkTheme },
                        onEsqueciSenha = { email ->
                            navController.navigate("esqueci_senha?email=$email")
                        },
                        onRegister = { navController.navigate("signup") },
                        onLogin = { email, senha, lembrarMe ->
                            authViewModel.loginUser(email, senha, lembrarMe)
                        },
                        onGoogleSignIn = {
                            coroutineScope.launch {
                                try {
                                    val credentialManager = CredentialManager.create(activity)
                                    val googleIdOption = GetGoogleIdOption.Builder()
                                        .setFilterByAuthorizedAccounts(false)
                                        .setServerClientId(GOOGLE_WEB_CLIENT_ID)
                                        .setAutoSelectEnabled(true)
                                        .build()

                                    val request = GetCredentialRequest.Builder()
                                        .addCredentialOption(googleIdOption)
                                        .build()

                                    val result = credentialManager.getCredential(
                                        context = activity,
                                        request = request
                                    )

                                    val credential = result.credential
                                    val googleIdTokenCredential =
                                        GoogleIdTokenCredential.createFrom(credential.data)
                                    val idToken = googleIdTokenCredential.idToken

                                    authViewModel.loginWithGoogle(idToken)
                                } catch (e: GetCredentialCancellationException) {
                                    // Usuário cancelou — não faz nada
                                    Log.d("PedidosApp", "Google Sign-In cancelado pelo usuário")
                                } catch (e: androidx.credentials.exceptions.NoCredentialException) {
                                    Log.e("PedidosApp", "Nenhuma conta do Google encontrada no dispositivo", e)
                                    android.widget.Toast.makeText(activity, "Nenhuma conta Google encontrada no dispositivo.", android.widget.Toast.LENGTH_LONG).show()
                                    authViewModel.clearError()
                                } catch (e: Exception) {
                                    Log.e("PedidosApp", "Erro no Google Sign-In", e)
                                    android.widget.Toast.makeText(activity, "Erro ao tentar usar Google Sign-in:\n${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                    authViewModel.clearError()
                                }
                            }
                        },
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onErrorDismiss = { authViewModel.clearError() }
                    )
                }
                composable(
                    route = "esqueci_senha?email={email}",
                    arguments = listOf(
                        navArgument("email") {
                            type = NavType.StringType
                            defaultValue = ""
                        }
                    )
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""
                    EsqueciSenhaScreen(
                        emailInicial = email,
                        onBackToLogin = { navController.popBackStack() },
                        onRecoverPassword = { emailInput, onSuccess, onError ->
                            authViewModel.recoverPassword(emailInput, onSuccess, onError)
                        },
                        onResetPassword = { token, novaSenha, onSuccess, onError ->
                            authViewModel.resetPasswordByCode(token, novaSenha, onSuccess, onError)
                        }
                    )
                }
                composable("signup") {
                    // Estado local para mensagem de erro e sucesso do cadastro
                    var cadastroError by remember { mutableStateOf<String?>(null) }
                    var cadastroSuccess by remember { mutableStateOf<String?>(null) }

                    // Redireciona ao login após exibir mensagem de sucesso
                    LaunchedEffect(cadastroSuccess) {
                        if (cadastroSuccess != null) {
                            kotlinx.coroutines.delay(2000L)
                            navController.popBackStack()
                        }
                    }

                    CadastroScreen(
                        onBackToLogin = { navController.popBackStack() },
                        onRegister = { nome, email, senha, cpf, telefone ->
                            cadastroError = null
                            cadastroSuccess = null
                            authViewModel.registerUser(
                                nome = nome,
                                email = email,
                                senha = senha,
                                cpf = cpf,
                                telefone = telefone,
                                onSuccess = {
                                    cadastroSuccess = "Conta criada com sucesso! Faça login."
                                    cadastroError = null
                                },
                                onError = { msg ->
                                    cadastroError = msg
                                    cadastroSuccess = null
                                }
                            )
                        },
                        isLoading = isLoading,
                        errorMessage = cadastroError,
                        successMessage = cadastroSuccess,
                        onErrorDismiss = {
                            cadastroError = null
                            authViewModel.clearError()
                        }
                    )
                }

                // ═══════════════════════════════════════════
                // COMPLETAR PERFIL (pós-login Google)
                // ═══════════════════════════════════════════
                composable("completar_perfil") {
                    var perfilError by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(authState) {
                        if (authState is AuthState.Success) {
                            navController.navigate("home") {
                                popUpTo("completar_perfil") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }

                    CompletarPerfilScreen(
                        onComplete = { cpf, telefone ->
                            perfilError = null
                            authViewModel.completeProfile(
                                cpf = cpf,
                                telefone = telefone,
                                onSuccess = {
                                    // AuthState.Success será emitido -> LaunchedEffect navega
                                },
                                onError = { msg ->
                                    perfilError = msg
                                }
                            )
                        },
                        onSkip = {
                            // Ir direto para Home sem completar perfil
                            navController.navigate("home") {
                                popUpTo("completar_perfil") { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        isLoading = isLoading,
                        errorMessage = perfilError,
                        onErrorDismiss = { perfilError = null }
                    )
                }

                composable("home") {
                    HomeScreen(
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate("login") {
                                popUpTo("login") { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onNavigateRestaurantes = {
                            navController.navigate("restaurantes") {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("restaurantes") {
                    RestaurantesScreen(
                        onNavigateHome = {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}
