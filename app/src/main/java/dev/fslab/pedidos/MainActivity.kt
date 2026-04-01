package dev.fslab.pedidos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.fslab.pedidos.ui.screens.auth.EsqueciSenhaScreen
import dev.fslab.pedidos.ui.screens.auth.LoginScreen
import dev.fslab.pedidos.ui.screens.auth.CadastroScreen
import dev.fslab.pedidos.ui.screens.HomeScreen
import dev.fslab.pedidos.ui.theme.PedidosTheme
import dev.fslab.pedidos.ui.viewmodel.AuthState
import dev.fslab.pedidos.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PedidosApp()
        }
    }
}

@Composable
fun PedidosApp() {
    val systemDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(systemDark) }
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

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
                        if (authState is AuthState.Success) {
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }

                    LoginScreen(
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = { isDarkTheme = !isDarkTheme },
                        onEsqueciSenha = { email ->
                            navController.navigate("esqueci_senha?email=$email")
                        },
                        onRegister = { navController.navigate("cadastro") },
                        onLogin = { email, senha ->
                            authViewModel.loginUser(email, senha)
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
                composable("cadastro") {
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
                composable("home") {
                    HomeScreen(
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate("login") {
                                popUpTo("login") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}
