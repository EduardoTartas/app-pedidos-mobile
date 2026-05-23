package dev.fslab.pedidos.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
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
    onRefresh: () -> Unit = {},
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
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.message, color = LocalPedidosColors.current.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.carregarDados() }) {
                            Text("Tentar Novamente")
                        }
                    }
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
                                onClick = { showEnderecoSheet = true }
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
    onClick: () -> Unit = {}
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
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Notificações",
                tint = LocalPedidosColors.current.primary
            )
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
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        TextField(
            value = busca,
            onValueChange = aoMudarBusca,
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
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier.width(280.dp).height(240.dp).clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(restaurante.fotoRestaurante ?: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?q=80&w=400&auto=format&fit=crop")
                        .crossfade(true)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = "Imagem do Restaurante",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${restaurante.estimativaEntregaMin}-${restaurante.estimativaEntregaMax} min",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
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
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(LocalPedidosColors.current.successBackground.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = String.format("%.1f", restaurante.avaliacaoMedia.coerceAtLeast(4.0)),
                            color = LocalPedidosColors.current.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star",
                            tint = LocalPedidosColors.current.primary,
                            modifier = Modifier.size(12.dp).padding(start = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                
                val cats = restaurante.categorias?.joinToString(" • ") { it.nome } ?: "Lanches • Bebidas"
                
                Text(
                    text = "$cats • Fast Food",
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TwoWheeler,
                        contentDescription = "Delivery",
                        tint = LocalPedidosColors.current.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if(restaurante.taxaEntrega <= 0.0) "Entrega Grátis" else "R$ ${String.format("%.2f", restaurante.taxaEntrega)}",
                        color = LocalPedidosColors.current.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
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
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(restaurante.fotoRestaurante ?: "https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=200&auto=format&fit=crop")
                        .crossfade(true)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = "Imagem do Restaurante",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Row(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format("%.1f", restaurante.avaliacaoMedia.coerceAtLeast(4.0)),
                        color = LocalPedidosColors.current.featureOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star",
                        tint = LocalPedidosColors.current.featureOrange,
                        modifier = Modifier.size(10.dp).padding(start = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = restaurante.nome,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val cats = restaurante.categorias?.joinToString(" • ") { it.nome } ?: "Culinária"

                Text(
                    text = cats,
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Time",
                        tint = textColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${restaurante.estimativaEntregaMin}-${restaurante.estimativaEntregaMax} min",
                        color = textColor.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.TwoWheeler,
                        contentDescription = "Delivery",
                        tint = if (restaurante.taxaEntrega <= 0.0) LocalPedidosColors.current.primary else textColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if(restaurante.taxaEntrega <= 0.0) "Entrega Grátis" else "R$ ${String.format("%.2f", restaurante.taxaEntrega)}",
                        color = if (restaurante.taxaEntrega <= 0.0) LocalPedidosColors.current.primary else textColor.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
