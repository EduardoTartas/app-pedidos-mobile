package dev.fslab.pedidos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import dev.fslab.pedidos.model.Categoria
import dev.fslab.pedidos.model.Restaurante
import dev.fslab.pedidos.ui.components.ErrorStateComponent
import dev.fslab.pedidos.ui.viewmodel.FiltrosAvancados
import dev.fslab.pedidos.ui.viewmodel.RestaurantesUiState
import dev.fslab.pedidos.ui.viewmodel.RestaurantesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantesScreen(
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onNavigateDetalhes: (String) -> Unit = {},
    viewModel: RestaurantesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // OTIMIZAÇÃO: Centralizando ImageLoader
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .allowHardware(true)
            .crossfade(true)
            .build()
    }

    val isLight = !androidx.compose.foundation.isSystemInDarkTheme()
    val bgColor = if (isLight) Color(0xFFF8F9FA) else Color(0xFF0A0E1A)
    val cardColor = if (isLight) Color.White else Color(0xFF161B2E)
    val textColors = if (isLight) Color.Black else Color.White

    // Estado do Bottom Sheet de filtros
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = bgColor
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is RestaurantesUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is RestaurantesUiState.Error -> {
                    ErrorStateComponent(
                        message = state.message,
                        onRetry = { viewModel.carregarDados() }
                    )
                }
                is RestaurantesUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = bottomPadding + 16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.statusBarsPadding())
                        }
                        item {
                            RestaurantesHeader(textColors)
                        }
                        item {
                            RestaurantesSearchBar(
                                query = state.searchQuery,
                                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                                cardColor = cardColor,
                                textColor = textColors,
                                filtrosAtivos = state.filtrosAtivos,
                                onFilterClick = { showFilterSheet = true }
                            )
                        }
                        item {
                            RestaurantesFilterRow(
                                filters = viewModel.filters,
                                selectedFilter = state.selectedFilter,
                                onFilterSelected = { viewModel.onFilterSelected(it) },
                                cardColor = cardColor,
                                textColor = textColors
                            )
                        }
                        if (state.restaurantes.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 60.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.SearchOff,
                                            contentDescription = null,
                                            tint = textColors.copy(alpha = 0.3f),
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Nenhum restaurante encontrado",
                                            color = textColors.copy(alpha = 0.5f),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Tente ajustar os filtros",
                                            color = textColors.copy(alpha = 0.3f),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                        items(
                            items = state.restaurantes,
                            key = { it.id } // OTIMIZAÇÃO: Chave estável
                        ) { restaurante ->
                            RestauranteCard(restaurante, cardColor, textColors, imageLoader, onClick = { onNavigateDetalhes(restaurante.id) })
                        }
                    }

                    // Bottom Sheet de filtros avançados
                    if (showFilterSheet) {
                        FiltrosBottomSheet(
                            filtrosAtuais = state.filtrosAvancados,
                            categorias = state.categorias,
                            cardColor = cardColor,
                            textColor = textColors,
                            bgColor = bgColor,
                            onDismiss = { showFilterSheet = false },
                            onAplicar = { filtros ->
                                viewModel.aplicarFiltrosAvancados(filtros)
                                showFilterSheet = false
                            },
                            onLimpar = {
                                viewModel.limparFiltrosAvancados()
                                showFilterSheet = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// HEADER
// ═══════════════════════════════════════════
@Composable
fun RestaurantesHeader(textColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
    ) {
        Text(
            text = "Restaurantes",
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
    }
}

// ═══════════════════════════════════════════
// BARRA DE BUSCA (com badge de filtros ativos)
// ═══════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantesSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    cardColor: Color,
    textColor: Color,
    filtrosAtivos: Int = 0,
    onFilterClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            placeholder = {
                Text(
                    text = "Buscar em Restaurantes",
                    color = textColor.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = textColor.copy(alpha = 0.5f)
                )
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
        // Botão de filtro com badge
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (filtrosAtivos > 0) Color(0xFF14B822).copy(alpha = 0.15f) else cardColor)
                .then(
                    if (filtrosAtivos > 0) Modifier.border(1.dp, Color(0xFF14B822), RoundedCornerShape(14.dp))
                    else Modifier
                )
                .clickable { onFilterClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Filtros",
                tint = if (filtrosAtivos > 0) Color(0xFF14B822) else textColor.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp)
            )
            // Badge de filtros ativos
            if (filtrosAtivos > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF14B822)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filtrosAtivos.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// FILTROS RÁPIDOS (CHIPS)
// ═══════════════════════════════════════════
@Composable
fun RestaurantesFilterRow(
    filters: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    cardColor: Color,
    textColor: Color
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        filters.forEach { filter ->
            RestauranteFilterChip(
                label = filter,
                isSelected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                cardColor = cardColor,
                textColor = textColor
            )
        }
    }
}

@Composable
fun RestauranteFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    cardColor: Color,
    textColor: Color
) {
    val bgColor = if (isSelected) Color(0xFF14B822) else cardColor
    val color = if (isSelected) Color.White else textColor.copy(alpha = 0.8f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

// ═══════════════════════════════════════════
// BOTTOM SHEET DE FILTROS AVANÇADOS
// ═══════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltrosBottomSheet(
    filtrosAtuais: FiltrosAvancados,
    categorias: List<Categoria>,
    cardColor: Color,
    textColor: Color,
    bgColor: Color,
    onDismiss: () -> Unit,
    onAplicar: (FiltrosAvancados) -> Unit,
    onLimpar: () -> Unit
) {
    // Estado local editável dos filtros
    var status by remember { mutableStateOf(filtrosAtuais.status) }
    var entregaGratis by remember { mutableStateOf(filtrosAtuais.entregaGratis) }
    var avaliacaoMinima by remember { mutableFloatStateOf(filtrosAtuais.avaliacaoMinima) }
    var categoriaId by remember { mutableStateOf(filtrosAtuais.categoriaId) }
    var ordenarPor by remember { mutableStateOf(filtrosAtuais.ordenarPor) }
    var ordemDirecao by remember { mutableStateOf(filtrosAtuais.ordemDirecao) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = bgColor,
        contentColor = textColor,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Título
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtros",
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                TextButton(onClick = onLimpar) {
                    Text(
                        text = "Limpar tudo",
                        color = Color(0xFF14B822),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ═══════════════════════════════════════
            // SEÇÃO: STATUS DO RESTAURANTE
            // ═══════════════════════════════════════
            FilterSectionTitle("Status", textColor)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterOptionChip(
                    label = "Todos",
                    isSelected = status == null,
                    onClick = { status = null },
                    cardColor = cardColor,
                    textColor = textColor
                )
                FilterOptionChip(
                    label = "Aberto",
                    isSelected = status == "aberto",
                    onClick = { status = "aberto" },
                    cardColor = cardColor,
                    textColor = textColor
                )
                FilterOptionChip(
                    label = "Fechado",
                    isSelected = status == "fechado",
                    onClick = { status = "fechado" },
                    cardColor = cardColor,
                    textColor = textColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ═══════════════════════════════════════
            // SEÇÃO: ENTREGA GRÁTIS
            // ═══════════════════════════════════════
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardColor)
                    .clickable { entregaGratis = !entregaGratis }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TwoWheeler,
                        contentDescription = null,
                        tint = Color(0xFF14B822),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Somente Entrega Grátis",
                        color = textColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Switch(
                    checked = entregaGratis,
                    onCheckedChange = { entregaGratis = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF14B822),
                        uncheckedThumbColor = textColor.copy(alpha = 0.5f),
                        uncheckedTrackColor = cardColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ═══════════════════════════════════════
            // SEÇÃO: AVALIAÇÃO MÍNIMA
            // ═══════════════════════════════════════
            FilterSectionTitle("Avaliação mínima", textColor)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val opcoes = listOf(0f, 3f, 3.5f, 4f, 4.5f)
                opcoes.forEach { valor ->
                    val label = if (valor == 0f) "Todas" else "${valor}★"
                    FilterOptionChip(
                        label = label,
                        isSelected = avaliacaoMinima == valor,
                        onClick = { avaliacaoMinima = valor },
                        cardColor = cardColor,
                        textColor = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ═══════════════════════════════════════
            // SEÇÃO: CATEGORIA
            // ═══════════════════════════════════════
            if (categorias.isNotEmpty()) {
                FilterSectionTitle("Categoria", textColor)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterOptionChip(
                        label = "Todas",
                        isSelected = categoriaId == null,
                        onClick = { categoriaId = null },
                        cardColor = cardColor,
                        textColor = textColor
                    )
                    categorias.forEach { cat ->
                        FilterOptionChip(
                            label = cat.nome,
                            isSelected = categoriaId == cat.id,
                            onClick = { categoriaId = cat.id },
                            cardColor = cardColor,
                            textColor = textColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ═══════════════════════════════════════
            // SEÇÃO: ORDENAR POR
            // ═══════════════════════════════════════
            FilterSectionTitle("Ordenar por", textColor)
            Spacer(modifier = Modifier.height(8.dp))

            val opcoesOrdenacao = listOf(
                "nome" to "Nome",
                "avaliacao_media" to "Avaliação",
                "taxa_entrega" to "Taxa de entrega",
                "estimativa_entrega_min" to "Tempo de entrega"
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                opcoesOrdenacao.forEach { (valor, label) ->
                    FilterOptionChip(
                        label = label,
                        isSelected = ordenarPor == valor,
                        onClick = { ordenarPor = valor },
                        cardColor = cardColor,
                        textColor = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Direção da ordenação
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterOptionChip(
                    label = "↑ Crescente",
                    isSelected = ordemDirecao == "asc",
                    onClick = { ordemDirecao = "asc" },
                    cardColor = cardColor,
                    textColor = textColor
                )
                FilterOptionChip(
                    label = "↓ Decrescente",
                    isSelected = ordemDirecao == "desc",
                    onClick = { ordemDirecao = "desc" },
                    cardColor = cardColor,
                    textColor = textColor
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ═══════════════════════════════════════
            // BOTÃO APLICAR
            // ═══════════════════════════════════════
            Button(
                onClick = {
                    onAplicar(
                        FiltrosAvancados(
                            status = status,
                            entregaGratis = entregaGratis,
                            avaliacaoMinima = avaliacaoMinima,
                            categoriaId = categoriaId,
                            ordenarPor = ordenarPor,
                            ordemDirecao = ordemDirecao
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF14B822)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Aplicar Filtros",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ═══════════════════════════════════════════
// COMPONENTES AUXILIARES DOS FILTROS
// ═══════════════════════════════════════════
@Composable
fun FilterSectionTitle(title: String, textColor: Color) {
    Text(
        text = title,
        color = textColor.copy(alpha = 0.7f),
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp
    )
}

@Composable
fun FilterOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    cardColor: Color,
    textColor: Color
) {
    val bgColor = if (isSelected) Color(0xFF14B822) else cardColor
    val color = if (isSelected) Color.White else textColor.copy(alpha = 0.7f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}

// ═══════════════════════════════════════════
// CARD DO RESTAURANTE (Estilo Delivery Moderno)
// ═══════════════════════════════════════════
@Composable
fun RestauranteCard(
    restaurante: Restaurante, 
    cardColor: Color, 
    textColor: Color,
    imageLoader: ImageLoader,
    onClick: () -> Unit = {}
) {
    val subTextColor = textColor.copy(alpha = 0.6f)
    val starColor = Color(0xFFEAB308) // Amarelo/Dourado padrão de delivery
    val greenColor = Color(0xFF14B822)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
            .border(1.dp, textColor.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
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
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Informações principais
            Column(modifier = Modifier.weight(1f)) {
                // Nome e selo (se houver)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = restaurante.nome.replace("`", "'").replace("´", "'"),
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                
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
                    
                    val cats = restaurante.categorias?.joinToString(", ") { it.nome } ?: "Lanches"
                    Text(
                        text = cats,
                        color = subTextColor,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
