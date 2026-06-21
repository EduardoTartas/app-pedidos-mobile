package dev.fslab.pedidos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import dev.fslab.pedidos.model.Categoria
import dev.fslab.pedidos.model.Restaurante
import dev.fslab.pedidos.ui.components.CategoriasBottomSheet
import dev.fslab.pedidos.ui.components.ErrorStateComponent
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import dev.fslab.pedidos.ui.viewmodel.HomeUiState
import dev.fslab.pedidos.ui.viewmodel.HomeViewModel
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import dev.fslab.pedidos.utils.ServicoLocalizacao
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onLogout: () -> Unit = {},
    onNavigateDetalhes: (String) -> Unit = {},
    onNavigateToNovoEndereco: () -> Unit = {},
    onNavigateToRestaurantes: () -> Unit = {},
    onNavigateNotificacoes: () -> Unit = {},
    onRefresh: () -> Unit = {},
    unreadNotificationsCount: Int = 0,
    carrinhoTotalItens: Int = 0,
    carrinhoPrecoTotal: Double = 0.0,
    onVerCarrinho: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalPedidosColors.current
    val context = LocalContext.current

    // OTIMIZAÇÃO: Centralizando ImageLoader e habilitando suporte a hardware
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .allowHardware(true)
            .crossfade(true)
            .build()
    }

    val pullRefreshState = rememberPullToRefreshState()
    
    var showEnderecoSheet by remember { mutableStateOf(false) }
    var showCategoriasSheet by remember { mutableStateOf(false) }

    val servicoLocalizacao = remember { ServicoLocalizacao(context) }
    
    val launcherConfiguracaoLocalizacao = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                viewModel.viewModelScope.launch {
                    val localizacao = servicoLocalizacao.obterLocalizacaoAtual()
                    if (localizacao != null) {
                        viewModel.definirLocalizacao(localizacao.cidade, localizacao.estado ?: "")
                    }
                }
            }
        }
    )
    
    val launcherPermissaoLocalizacao = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { concedido ->
            if (concedido) {
                servicoLocalizacao.solicitarAtivacaoLocalizacao(
                    aoSucesso = {
                        viewModel.viewModelScope.launch {
                            val localizacao = servicoLocalizacao.obterLocalizacaoAtual()
                            if (localizacao != null) {
                                viewModel.definirLocalizacao(localizacao.cidade, localizacao.estado ?: "")
                            }
                        }
                    },
                    aoPrecisarPrompt = { intentSender ->
                        val intentSenderRequest = androidx.activity.result.IntentSenderRequest.Builder(intentSender).build()
                        launcherConfiguracaoLocalizacao.launch(intentSenderRequest)
                    },
                    aoFalhar = { }
                )
            }
        }
    )

    LaunchedEffect(Unit) {
        if (servicoLocalizacao.temPermissaoLocalizacao()) {
            servicoLocalizacao.solicitarAtivacaoLocalizacao(
                aoSucesso = {
                    viewModel.viewModelScope.launch {
                        val localizacao = servicoLocalizacao.obterLocalizacaoAtual()
                        if (localizacao != null) {
                            viewModel.definirLocalizacao(localizacao.cidade, localizacao.estado ?: "")
                        }
                    }
                },
                aoPrecisarPrompt = { intentSender ->
                    val intentSenderRequest = androidx.activity.result.IntentSenderRequest.Builder(intentSender).build()
                    launcherConfiguracaoLocalizacao.launch(intentSenderRequest)
                },
                aoFalhar = { }
            )
        } else {
            launcherPermissaoLocalizacao.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val bgColor = colors.background
    val cardColor = colors.surface
    val textColors = colors.textPrimary

    val categoriesScrollState = rememberScrollState()

    Scaffold(
        containerColor = bgColor
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is HomeUiState.Error -> {
                    ErrorStateComponent(
                        message = state.message,
                        onRetry = { viewModel.carregarDados(force = true) }
                    )
                }
                is HomeUiState.Success -> {
                    PullToRefreshBox(
                        isRefreshing = state.atualizando,
                        onRefresh = onRefresh,
                        state = pullRefreshState,
                        modifier = Modifier.fillMaxSize(),
                        indicator = {
                            PullToRefreshDefaults.Indicator(
                                modifier = Modifier.align(Alignment.TopCenter),
                                isRefreshing = state.atualizando,
                                state = pullRefreshState,
                                color = colors.primary,
                                containerColor = colors.background
                            )
                        }
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = bottomPadding + 16.dp)
                        ) {
                        item(key = "status_bar_spacer") {
                            Spacer(modifier = Modifier.statusBarsPadding())
                        }
                        item(key = "header") {
                            HomeHeader(
                                textColor = textColors, 
                                cardColor = cardColor, 
                                label = state.labelEndereco,
                                cidade = state.cidadeUsuario, 
                                estado = state.estadoUsuario,
                                onClick = { showEnderecoSheet = true },
                                onNotificationsClick = onNavigateNotificacoes,
                                unreadNotificationsCount = unreadNotificationsCount
                            )
                        }
                        item(key = "search_bar") {
                            BarraBusca(
                                busca = state.textoBusca,
                                aoMudarBusca = { viewModel.aoMudarTextoBusca(it) },
                                onGridClick = { showCategoriasSheet = true },
                                cardColor = cardColor,
                                textColor = textColors
                            )
                        }
                        item(key = "categories") {
                            CategoriesRow(
                                categorias = state.categorias,
                                categoriaSelecionadaId = state.categoriaSelecionadaId,
                                aoSelecionarCategoria = { viewModel.aoSelecionarCategoria(it) },
                                cardColor = cardColor,
                                textColor = textColors,
                                scrollState = categoriesScrollState,
                                imageLoader = imageLoader
                            )
                        }
                        item(key = "title_recomendados") {
                            SectionTitle("Recomendados", "Ver todos", textColors, onActionClick = onNavigateToRestaurantes)
                        }
                        item(key = "row_recomendados") {
                            RecomendadosRow(state.recomendados, cardColor, textColors, imageLoader, onItemClick = { onNavigateDetalhes(it.id) })
                        }
                        item(key = "title_populares") {
                            SectionTitle("Populares perto de você", "Ver todos", textColors, onActionClick = onNavigateToRestaurantes)
                        }
                        items(
                            items = state.populares,
                            key = { it.id }
                        ) { restaurante ->
                            PopularItem(restaurante, cardColor, textColors, imageLoader, onClick = { onNavigateDetalhes(restaurante.id) })
                        }
                    }
                    }

                    if (showEnderecoSheet) {
                        dev.fslab.pedidos.ui.components.EnderecosBottomSheet(
                            enderecos = state.enderecos,
                            selectedEnderecoId = state.enderecoSelecionadoId,
                            onDismiss = { showEnderecoSheet = false },
                            onNovoEnderecoClick = {
                                showEnderecoSheet = false
                                onNavigateToNovoEndereco()
                            },
                            onEnderecoSelected = { endereco ->
                                viewModel.selecionarEndereco(endereco)
                            }
                        )
                    }

                    if (showCategoriasSheet) {
                        dev.fslab.pedidos.ui.components.CategoriasBottomSheet(
                            categorias = state.categorias,
                            categoriaSelecionadaId = state.categoriaSelecionadaId,
                            onDismiss = { showCategoriasSheet = false },
                            onCategoriaSelected = { id ->
                                viewModel.aoSelecionarCategoria(id)
                            }
                        )
                    }
                }
            }

            // Barra do carrinho flutuante — fora do when{} para aparecer em todos os estados
            // e posicionada corretamente no Box pai (fillMaxSize)
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = bottomPadding)
            ) {
                dev.fslab.pedidos.ui.components.CarrinhoBar(
                    totalItens = carrinhoTotalItens,
                    precoTotal = carrinhoPrecoTotal,
                    onClick = onVerCarrinho
                )
            }
        }
    }
}

@Composable
fun HomeHeader(
    textColor: Color, 
    cardColor: Color, 
    label: String = "",
    cidade: String = "Sua localização", 
    estado: String = "",
    onClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    unreadNotificationsCount: Int = 0
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClick() }
                    .padding(4.dp)
            ) {
                Text(
                    text = "ENTREGAR EM",
                    color = textColor.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (label.isNotBlank()) {
                        Text(
                            text = "\"${label.uppercase()}\", ",
                            color = LocalPedidosColors.current.primary,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        text = if (estado.isNotEmpty()) "${cidade.uppercase()} ${estado.uppercase()}" else cidade.uppercase(),
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = LocalPedidosColors.current.primary,
                        modifier = Modifier.padding(start = 4.dp).size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(cardColor)
                .clickable { onNotificationsClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Notificações",
                tint = LocalPedidosColors.current.primary
            )
            if (unreadNotificationsCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(LocalPedidosColors.current.error)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraBusca(
    busca: String, 
    aoMudarBusca: (String) -> Unit, 
    onGridClick: () -> Unit,
    cardColor: Color, 
    textColor: Color
) {
    val colors = LocalPedidosColors.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        TextField(
            value = busca,
            onValueChange = aoMudarBusca,
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { focusManager.clearFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = {
                Text(
                    text = "Buscar restaurantes ou comida",
                    color = textColor.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = textColor.copy(alpha = 0.5f)
                )
            },
            trailingIcon = {
                IconButton(onClick = onGridClick) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = "Todas as categorias",
                        tint = colors.primary
                    )
                }
            },
            shape = RoundedCornerShape(28.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = cardColor,
                unfocusedContainerColor = cardColor,
                disabledContainerColor = cardColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
fun CategoriesRow(
    categorias: List<Categoria>,
    categoriaSelecionadaId: String?,
    aoSelecionarCategoria: (String?) -> Unit,
    cardColor: Color,
    textColor: Color,
    scrollState: androidx.compose.foundation.ScrollState,
    imageLoader: ImageLoader
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        categorias.forEach { cat ->
            CategoryChip(
                nome = cat.nome,
                isSelected = categoriaSelecionadaId == cat.id,
                onClick = { aoSelecionarCategoria(cat.id) },
                cardColor = cardColor,
                textColor = textColor,
                iconeUrl = cat.iconeCategoria,
                imageLoader = imageLoader
            )
        }
    }
}

@Composable
fun CategoryChip(
    nome: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    cardColor: Color,
    textColor: Color,
    iconeUrl: String? = null,
    imageLoader: ImageLoader
) {
    val colors = LocalPedidosColors.current
    val bgColor = if (isSelected) colors.primary else cardColor
    val color = if (isSelected) Color.White else textColor.copy(alpha = 0.8f)
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!iconeUrl.isNullOrEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(iconeUrl)
                    .crossfade(true)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = null,
                colorFilter = if (isSelected) androidx.compose.ui.graphics.ColorFilter.tint(Color.White) else null,
                modifier = Modifier.size(16.dp).padding(end = 4.dp)
            )
        } else {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.RestaurantMenu,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp).padding(end = 4.dp)
                )
            }
        }
        Text(
            text = nome,
            color = color,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SectionTitle(title: String, action: String, textColor: Color, onActionClick: () -> Unit = {}) {
    val colors = LocalPedidosColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = action,
            color = LocalPedidosColors.current.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onActionClick() }
        )
    }
}

@Composable
fun RecomendadosRow(
    restaurantes: List<Restaurante>, 
    cardColor: Color, 
    textColor: Color,
    imageLoader: ImageLoader,
    onItemClick: (Restaurante) -> Unit = {}
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(restaurantes, key = { it.id }) { restaurante ->
            RecomendadoCard(restaurante, cardColor, textColor, imageLoader, onClick = { onItemClick(restaurante) })
        }
    }
}

@Composable
fun RecomendadoCard(
    restaurante: Restaurante, 
    cardColor: Color, 
    textColor: Color,
    imageLoader: ImageLoader,
    onClick: () -> Unit = {}
) {
    val subTextColor = textColor.copy(alpha = 0.6f)
    val starColor = Color(0xFFEAB308)
    val greenColor = Color(0xFF14B822)
    val isAberto = restaurante.status == "aberto"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .width(260.dp)
            .clickable { onClick() }
            .border(1.dp, textColor.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .alpha(if (isAberto) 1f else 0.5f)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                AsyncImage(
                    model = restaurante.fotoRestaurante?.takeIf { it.isNotBlank() } ?: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?q=80&w=400&auto=format&fit=crop",
                    imageLoader = imageLoader,
                    contentDescription = "Imagem de ${restaurante.nome}",
                    contentScale = ContentScale.Crop,
                    colorFilter = if (!isAberto) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
                    modifier = Modifier.fillMaxSize()
                )
                
                if (!isAberto) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
                    Text(
                        "FECHADO",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                } else {
                    // Overlay de proteção de contraste para a imagem original
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                                    startY = 150f
                                )
                            )
                    )
                }
            }
            
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = restaurante.nome,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Avaliação",
                            tint = starColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (restaurante.avaliacaoMedia > 0) String.format("%.1f", restaurante.avaliacaoMedia) else "Novo",
                            color = starColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                val cats = restaurante.categorias?.joinToString(" • ") { it.nome } ?: "Lanches"
                Text(
                    text = cats,
                    color = subTextColor,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${restaurante.estimativaEntregaMin}-${restaurante.estimativaEntregaMax} min",
                        color = subTextColor,
                        fontSize = 13.sp
                    )
                    Text("•", color = subTextColor, fontSize = 12.sp)
                    Text(
                        text = if(restaurante.taxaEntrega <= 0.0) "Entrega Grátis" else "R$ ${String.format("%.2f", restaurante.taxaEntrega)}",
                        color = if(restaurante.taxaEntrega <= 0.0) greenColor else subTextColor,
                        fontSize = 13.sp,
                        fontWeight = if(restaurante.taxaEntrega <= 0.0) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun PopularItem(
    restaurante: Restaurante, 
    cardColor: Color, 
    textColor: Color,
    imageLoader: ImageLoader,
    onClick: () -> Unit = {}
) {
    val subTextColor = textColor.copy(alpha = 0.6f)
    val starColor = Color(0xFFEAB308)
    val greenColor = Color(0xFF14B822)
    val isAberto = restaurante.status == "aberto"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
            .border(1.dp, textColor.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .alpha(if (isAberto) 1f else 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo do Restaurante
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(textColor.copy(alpha = 0.03f))
                    .border(1.dp, textColor.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = restaurante.fotoRestaurante?.takeIf { it.isNotBlank() } ?: "https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=200&auto=format&fit=crop",
                    imageLoader = imageLoader,
                    contentDescription = "Logo de ${restaurante.nome}",
                    contentScale = ContentScale.Crop,
                    colorFilter = if (!isAberto) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
                    modifier = Modifier.fillMaxSize()
                )
                if (!isAberto) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Informações principais
            Column(modifier = Modifier.weight(1f)) {
                // Nome
                Text(
                    text = restaurante.nome,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Avaliação e Categoria
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Avaliação",
                        tint = starColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (restaurante.avaliacaoMedia > 0) String.format("%.1f", restaurante.avaliacaoMedia) else "Novo",
                        color = starColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text("•", color = subTextColor, fontSize = 12.sp)
                    
                    val cats = restaurante.categorias?.joinToString(", ") { it.nome } ?: "Culinária"
                    Text(
                        text = cats,
                        color = subTextColor,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (!isAberto) {
                        Text("•", color = subTextColor, fontSize = 12.sp)
                        Text(
                            text = "FECHADO",
                            color = LocalPedidosColors.current.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Tempo e Taxa de Entrega
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${restaurante.estimativaEntregaMin}-${restaurante.estimativaEntregaMax} min",
                        color = subTextColor,
                        fontSize = 13.sp
                    )
                    Text("•", color = subTextColor, fontSize = 12.sp)
                    Text(
                        text = if (restaurante.taxaEntrega <= 0.0) "Entrega Grátis"
                               else "R$ ${String.format("%.2f", restaurante.taxaEntrega)}",
                        color = if (restaurante.taxaEntrega <= 0.0) greenColor else subTextColor,
                        fontSize = 13.sp,
                        fontWeight = if (restaurante.taxaEntrega <= 0.0) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }
    }
}
