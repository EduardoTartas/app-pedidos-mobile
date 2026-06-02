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
import androidx.compose.ui.unit.dp
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
import dev.fslab.pedidos.ui.screens.CarrinhoScreen
import dev.fslab.pedidos.ui.screens.HomeScreen
import dev.fslab.pedidos.ui.screens.PedidoConfirmacaoScreen
import dev.fslab.pedidos.ui.screens.RestaurantesScreen
import dev.fslab.pedidos.ui.screens.RestauranteDetalhesScreen
import dev.fslab.pedidos.ui.screens.PratoPersonalizacaoScreen
import dev.fslab.pedidos.ui.screens.SplashScreen
import dev.fslab.pedidos.ui.theme.PedidosTheme
import dev.fslab.pedidos.ui.viewmodel.AuthState
import dev.fslab.pedidos.ui.viewmodel.AuthViewModel
import dev.fslab.pedidos.ui.viewmodel.CarrinhoViewModel
import dev.fslab.pedidos.ui.viewmodel.HomeUiState
import dev.fslab.pedidos.ui.viewmodel.PedidoUiState
import dev.fslab.pedidos.ui.viewmodel.PedidoViewModel
import dev.fslab.pedidos.ui.viewmodel.PratoPersonalizacaoViewModel
import kotlinx.coroutines.launch
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.graphics.Color
import dev.fslab.pedidos.ui.components.BottomNavigationBar
import dev.fslab.pedidos.ui.components.bottomNavItems
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PedidosApp(activity = this)
        }
    }
}

private const val GOOGLE_WEB_CLIENT_ID =
    "1053347409082-qb4s3d724bp69hs78kdt38s35brinr7n.apps.googleusercontent.com"

private val mainScreenRoutes = bottomNavItems.map { it.route }.toSet()
private val splashRoute = "splash"

@Composable
fun PedidosApp(activity: ComponentActivity) {
    val systemDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(systemDark) }
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

    // ViewModel de personalização com escopo de Activity (compartilhado entre telas)
    val personalizacaoViewModel: PratoPersonalizacaoViewModel = viewModel()

    // ViewModel do carrinho com escopo de Activity (persistência entre telas)
    val carrinhoViewModel: CarrinhoViewModel = viewModel()
    val carrinhoItens by carrinhoViewModel.itens.collectAsState()
    val carrinhoTotalItens = carrinhoItens.sumOf { it.quantidade }
    val carrinhoPrecoTotal = carrinhoItens.sumOf { it.precoTotal * it.quantidade }

    // ViewModel de pedidos com escopo de Activity
    val pedidoViewModel: PedidoViewModel = viewModel()

    // HomeViewModel com escopo de Activity — compartilhado entre home, carrinho e novo_endereco
    // IMPORTANTE: declarar aqui garante a mesma instância em todas as rotas.
    // Se declarado dentro de cada composable, cada rota teria sua própria instância isolada.
    val homeViewModel: dev.fslab.pedidos.ui.viewmodel.HomeViewModel = viewModel()
    val homeState by homeViewModel.uiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    PedidosTheme(darkTheme = isDarkTheme) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        
        // Trava reforçada: Só mostra se a rota atual estiver na lista e não for nula/splash
        val showBottomBar = currentRoute != null && 
                          currentRoute in mainScreenRoutes && 
                          currentRoute != splashRoute

        val cardColor = if (isDarkTheme) Color(0xFF161B2E) else Color.White
        val textColors = if (isDarkTheme) Color.White else Color.Black

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    BottomNavigationBar(
                        cardColor = cardColor,
                        textColor = textColors,
                        selectedRoute = currentRoute ?: "home",
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = splashRoute,
                enterTransition = {
                    fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { 300 }, animationSpec = tween(300))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { -300 }, animationSpec = tween(300))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { -300 }, animationSpec = tween(300))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { 300 }, animationSpec = tween(300))
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 0.dp)
            ) {
                composable(splashRoute) {
                    SplashScreen(
                        authState = authState,
                        onNavigateToHome = {
                            navController.navigate("home") {
                                popUpTo(splashRoute) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToLogin = {
                            navController.navigate("login") {
                                popUpTo(splashRoute) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToCompleteProfile = {
                            navController.navigate("completar_perfil") {
                                popUpTo(splashRoute) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

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
                        onLogin = { email, senha ->
                            authViewModel.loginUser(email, senha)
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
                    var cadastroError by remember { mutableStateOf<String?>(null) }
                    var cadastroSuccess by remember { mutableStateOf<String?>(null) }

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
                                onSuccess = { },
                                onError = { msg ->
                                    perfilError = msg
                                }
                            )
                        },
                        onSkip = {
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
                    val user by authViewModel.currentUser.collectAsState()
                    val userId = user?.id ?: ""

                    LaunchedEffect(userId) {
                        if (userId.isNotEmpty()) {
                            homeViewModel.carregarDados(userId)
                        }
                    }

                    HomeScreen(
                        viewModel = homeViewModel,
                        bottomPadding = innerPadding.calculateBottomPadding(),
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate("login") {
                                popUpTo("login") { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onNavigateDetalhes = { restauranteId ->
                            navController.navigate("restaurante/$restauranteId")
                        },
                        onNavigateToNovoEndereco = {
                            navController.navigate("novo_endereco")
                        },
                        onNavigateToRestaurantes = {
                            navController.navigate("restaurantes") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onRefresh = {
                            homeViewModel.atualizarDados(userId)
                        },
                        carrinhoTotalItens = carrinhoTotalItens,
                        carrinhoPrecoTotal = carrinhoPrecoTotal,
                        onVerCarrinho = { navController.navigate("carrinho") }
                    )
                }

                composable("restaurantes") {
                    RestaurantesScreen(
                        bottomPadding = innerPadding.calculateBottomPadding(),
                        onNavigateDetalhes = { restauranteId ->
                            navController.navigate("restaurante/$restauranteId")
                        }
                    )
                }

                composable("pedidos") {
                    val historyViewModel: dev.fslab.pedidos.ui.viewmodel.PedidosHistoryViewModel = viewModel()
                    dev.fslab.pedidos.ui.screens.PedidosScreen(
                        bottomPadding = innerPadding.calculateBottomPadding(),
                        onNavigateToPedidoDetalhes = { pedidoId ->
                            navController.navigate("pedido_detalhes/$pedidoId")
                        },
                        viewModel = historyViewModel
                    )
                }

                composable(
                    route = "pedido_detalhes/{pedidoId}",
                    arguments = listOf(navArgument("pedidoId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val pedidoId = backStackEntry.arguments?.getString("pedidoId") ?: ""
                    dev.fslab.pedidos.ui.screens.PedidoDetalhesScreen(
                        pedidoId = pedidoId,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "restaurante/{restauranteId}",
                    arguments = listOf(navArgument("restauranteId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val restauranteId = backStackEntry.arguments?.getString("restauranteId") ?: ""
                    val detalhesViewModel: dev.fslab.pedidos.ui.viewmodel.RestauranteDetalhesViewModel = viewModel()
                    val detalhesState by detalhesViewModel.uiState.collectAsState()

                    // Prato que o usuário clicou mas há conflito de restaurante
                    var pratoPendenteConflito by remember { mutableStateOf<dev.fslab.pedidos.model.Prato?>(null) }
                    val carrinhoItens by carrinhoViewModel.itens.collectAsState()
                    val carrinhoRestauranteId by carrinhoViewModel.restauranteId.collectAsState()
                    val carrinhoNomeRestaurante by carrinhoViewModel.nomeRestaurante.collectAsState()

                    // Salva o nome e ID do restaurante atual no carrinho assim que carrega
                    // (necessário para que o conflito saiba o nome do restaurante ativo)
                    LaunchedEffect(detalhesState) {
                        val success = detalhesState as? dev.fslab.pedidos.ui.viewmodel.DetalhesUiState.Success
                        if (success != null && carrinhoItens.isEmpty()) {
                            // Só define o restaurante do carrinho se o carrinho estiver vazio
                            // (se já tiver itens, o restaurante do carrinho é o anterior)
                            carrinhoViewModel.definirRestaurante(
                                nome = success.restaurante.nome,
                                id = success.restaurante.id,
                                taxa = success.restaurante.taxaEntrega
                            )
                        }
                    }

                    // Dialog premium de conflito de restaurante
                    val detalhesSuccess = detalhesState as? dev.fslab.pedidos.ui.viewmodel.DetalhesUiState.Success
                    if (pratoPendenteConflito != null && detalhesSuccess != null) {
                        dev.fslab.pedidos.ui.components.ConflitoRestauranteDialog(
                            nomeRestauranteAtual = carrinhoNomeRestaurante,
                            nomeRestauranteNovo = detalhesSuccess.restaurante.nome,
                            onSubstituir = {
                                carrinhoViewModel.limpar()
                                carrinhoViewModel.definirRestaurante(
                                    nome = detalhesSuccess.restaurante.nome,
                                    id = detalhesSuccess.restaurante.id,
                                    taxa = detalhesSuccess.restaurante.taxaEntrega
                                )
                                personalizacaoViewModel.carregarGrupos(pratoPendenteConflito!!)
                                pratoPendenteConflito = null
                                navController.navigate("personalizacao")
                            },
                            onCancelar = { pratoPendenteConflito = null }
                        )
                    }

                    RestauranteDetalhesScreen(
                        restauranteId = restauranteId,
                        bottomPadding = innerPadding.calculateBottomPadding(),
                        onBack = { navController.popBackStack() },
                        viewModel = detalhesViewModel,
                        onNavigatePersonalizacao = { prato ->
                            val nomeRestauranteNovo = detalhesSuccess?.restaurante?.nome ?: ""
                            val idRestauranteNovo = detalhesSuccess?.restaurante?.id ?: ""
                            val temConflito = carrinhoItens.isNotEmpty() &&
                                    carrinhoRestauranteId.isNotEmpty() &&
                                    carrinhoRestauranteId != idRestauranteNovo

                            if (temConflito) {
                                // Mostra o dialog na tela atual — sem navegar para personalizacao
                                pratoPendenteConflito = prato
                            } else {
                                // Mesmo restaurante ou carrinho vazio: vai direto
                                carrinhoViewModel.definirRestaurante(
                                    nome = nomeRestauranteNovo, 
                                    id = idRestauranteNovo,
                                    taxa = detalhesSuccess?.restaurante?.taxaEntrega ?: 0.0
                                )
                                personalizacaoViewModel.carregarGrupos(prato)
                                navController.navigate("personalizacao")
                            }
                        },
                        carrinhoTotalItens = carrinhoTotalItens,
                        carrinhoPrecoTotal = carrinhoPrecoTotal,
                        onVerCarrinho = { navController.navigate("carrinho") }
                    )
                }

                composable("personalizacao") {
                    // O conflito já foi tratado na tela de detalhes do restaurante.
                    // Aqui sabemos que é o mesmo restaurante ou o carrinho foi esvaziado.
                    PratoPersonalizacaoScreen(
                        onBack = { navController.popBackStack() },
                        onAdicionarAoCarrinho = { state, qtd ->
                            // Adiciona direto — sem verificação de conflito aqui
                            carrinhoViewModel.adicionarItem(
                                prato = state.prato,
                                selecoes = state.selecoes,
                                grupos = state.grupos,
                                observacao = state.observacao,
                                quantidade = qtd
                            )
                            personalizacaoViewModel.resetar()
                            navController.popBackStack()
                        },
                        viewModel = personalizacaoViewModel
                    )
                }

                composable("novo_endereco") {
                    val user by authViewModel.currentUser.collectAsState()
                    val userId = user?.id ?: ""

                    // Flag: indica que o endereço foi criado e estamos aguardando o reload
                    var aguardandoAtualizacao by remember { mutableStateOf(false) }

                    // Quando o reload terminar (Success com atualizando=false), navega de volta
                    LaunchedEffect(homeState, aguardandoAtualizacao) {
                        if (aguardandoAtualizacao) {
                            val success = homeState as? HomeUiState.Success
                            if (success != null && !success.atualizando) {
                                aguardandoAtualizacao = false
                                navController.popBackStack()
                            }
                        }
                    }

                    // Segurança: timeout de 5s para não travar na tela para sempre
                    LaunchedEffect(aguardandoAtualizacao) {
                        if (aguardandoAtualizacao) {
                            kotlinx.coroutines.delay(5000L)
                            if (aguardandoAtualizacao) {
                                aguardandoAtualizacao = false
                                navController.popBackStack()
                            }
                        }
                    }

                    if (userId.isNotEmpty()) {
                        dev.fslab.pedidos.ui.screens.NovoEnderecoScreen(
                            usuarioId = userId,
                            onBack = { navController.popBackStack() },
                            onSuccess = {
                                // Força reload usando a instância Activity-scoped
                                homeViewModel.atualizarDados(userId)
                                aguardandoAtualizacao = true
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            navController.navigate("login") {
                                popUpTo(0)
                            }
                        }
                    }
                }

                composable("carrinho") {
                    val user by authViewModel.currentUser.collectAsState()
                    val userId = user?.id ?: ""
                    val nomeRestaurante by carrinhoViewModel.nomeRestaurante.collectAsState()
                    val restauranteId by carrinhoViewModel.restauranteId.collectAsState()
                    val pedidoState by pedidoViewModel.uiState.collectAsState()

                    // Garante que os endereços estejam carregados
                    LaunchedEffect(userId) {
                        if (userId.isNotEmpty()) {
                            homeViewModel.carregarDados(userId)
                        }
                    }

                    // Navega para a tela de confirmação ao criar pedido com sucesso
                    LaunchedEffect(pedidoState) {
                        if (pedidoState is PedidoUiState.Success) {
                            // Navega para confirmação e remove carrinho do stack
                            navController.navigate("pedido_confirmacao") {
                                popUpTo("carrinho") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }

                    val enderecos = (homeState as? HomeUiState.Success)?.enderecos ?: emptyList()

                    CarrinhoScreen(
                        viewModel = carrinhoViewModel,
                        nomeRestaurante = nomeRestaurante,
                        enderecos = enderecos,
                        onBack = { navController.popBackStack() },
                        onNavigateNovoEndereco = {
                            navController.navigate("novo_endereco")
                        },
                        onFinalizarPedido = { end, pagamento ->
                            pedidoViewModel.realizarPedido(
                                restauranteId = restauranteId,
                                itens = carrinhoItens,
                                endereco = end,
                                formaPagamento = pagamento
                            )
                        },
                        onVoltarAoRestaurante = {
                            navController.popBackStack()
                        },
                        pedidoState = pedidoState,
                        onDismissErro = { pedidoViewModel.resetar() }
                    )
                }

                composable("pedido_confirmacao") {
                    val pedidoState by pedidoViewModel.uiState.collectAsState()
                    val nomeRestauranteSnapshot = remember { carrinhoViewModel.nomeRestaurante.value }
                    val pedido = (pedidoState as? PedidoUiState.Success)?.pedido

                    // Limpa o carrinho assim que entra na tela de confirmação, 
                    // mas já capturamos o nome do restaurante acima via snapshot
                    LaunchedEffect(Unit) {
                        carrinhoViewModel.limpar()
                    }

                    if (pedido != null) {
                        PedidoConfirmacaoScreen(
                            pedido = pedido,
                            nomeRestaurante = nomeRestauranteSnapshot,
                            onVoltarInicio = {
                                pedidoViewModel.resetar()
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onAcompanharPedido = {
                                val pid = pedido.id
                                pedidoViewModel.resetar()
                                navController.navigate("pedido_detalhes/$pid") {
                                    popUpTo("home") { inclusive = false }
                                }
                            }
                        )
                    } else {
                        // Se o estado for perdido (ex: process death), volta para home
                        LaunchedEffect(Unit) {
                            navController.navigate("home") {
                                popUpTo(0)
                            }
                        }
                    }
                }
            }
        }
    }
}
