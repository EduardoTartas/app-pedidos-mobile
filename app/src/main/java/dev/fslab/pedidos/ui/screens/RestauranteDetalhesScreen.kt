package dev.fslab.pedidos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.MotionDurationScale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import dev.fslab.pedidos.model.Prato
import dev.fslab.pedidos.model.Restaurante
import dev.fslab.pedidos.ui.viewmodel.DetalhesUiState
import dev.fslab.pedidos.ui.viewmodel.RestauranteDetalhesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RestauranteDetalhesScreen(
    restauranteId: String,
    bottomPadding: Dp = 0.dp,
    onBack: () -> Unit = {},
    viewModel: RestauranteDetalhesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val isLight = !androidx.compose.foundation.isSystemInDarkTheme()
    val bgColor = if (isLight) Color(0xFFF8F9FA) else Color(0xFF0A0E1A)
    val cardColor = if (isLight) Color.White else Color(0xFF161B2E)
    val textColor = if (isLight) Color.Black else Color.White

    LaunchedEffect(restauranteId) {
        viewModel.carregarDados(restauranteId)
    }

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = bgColor
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is DetalhesUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DetalhesUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = state.message,
                            color = textColor.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.carregarDados(restauranteId) }) {
                            Text("Tentar Novamente")
                        }
                    }
                }
                is DetalhesUiState.Success -> {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 0.dp, bottom = bottomPadding + 16.dp)
                    ) {
                        // ═══ HEADER: Foto de capa ═══
                        item {
                            DetalhesHeader(
                                restaurante = state.restaurante,
                                buscaVisivel = state.buscaVisivel,
                                topPadding = innerPadding.calculateTopPadding(),
                                onBack = onBack,
                                onToggleBusca = { viewModel.toggleBusca() }
                            )
                        }

                        // ═══ INFO ROW ═══
                        item {
                            DetalhesInfoRow(
                                restaurante = state.restaurante,
                                textColor = textColor
                            )
                        }

                        // ═══ BARRA DE BUSCA (expandível) ═══
                        item {
                            AnimatedVisibility(
                                visible = state.buscaVisivel,
                                enter = fadeIn() + slideInVertically(),
                                exit = fadeOut() + slideOutVertically()
                            ) {
                                DetalhesBuscaBar(
                                    query = state.textoBusca,
                                    onQueryChange = { viewModel.aoMudarBusca(it) },
                                    cardColor = cardColor,
                                    textColor = textColor
                                )
                            }
                        }

                        // ═══ CHIPS DE SEÇÃO ═══
                        stickyHeader {
                            // Fundo para o sticky header não sobrepor transparente
                            Box(modifier = Modifier.background(bgColor)) {
                                SecaoChipsRow(
                                    secoes = state.secoes,
                                    secaoSelecionada = state.secaoSelecionada,
                                    onSelecionarSecao = { secao ->
                                        viewModel.aoSelecionarSecao(secao)
                                        if (secao != null) {
                                            val sections = state.cardapioFiltrado.keys.toList()
                                            val sectionIndex = sections.indexOf(secao)
                                            if (sectionIndex >= 0) {
                                                var idx = 4 // Base offset (Header, Info, Busca, Chips)
                                                for (i in 0 until sectionIndex) {
                                                    idx += 1 // Título da seção
                                                    idx += state.cardapioFiltrado[sections[i]]?.size ?: 0
                                                }
                                                coroutineScope.launch(object : MotionDurationScale { override val scaleFactor = 2.5f }) {
                                                    lazyListState.animateScrollToItem(idx, scrollOffset = -60)
                                                }
                                            }
                                        } else {
                                            coroutineScope.launch(object : MotionDurationScale { override val scaleFactor = 2.5f }) {
                                                lazyListState.animateScrollToItem(0)
                                            }
                                        }
                                    },
                                    cardColor = cardColor,
                                    textColor = textColor
                                )
                            }
                        }

                        // ═══ PRATOS POR SEÇÃO ═══
                        if (state.cardapioFiltrado.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.RestaurantMenu,
                                            contentDescription = null,
                                            tint = textColor.copy(alpha = 0.25f),
                                            modifier = Modifier.size(56.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Nenhum prato encontrado",
                                            color = textColor.copy(alpha = 0.5f),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        } else {
                            state.cardapioFiltrado.forEach { (secao, pratos) ->
                                // Título da seção
                                item {
                                    SecaoTitulo(secao, textColor)
                                }
                                // Pratos da seção
                                items(pratos) { prato ->
                                    PratoItem(
                                        prato = prato,
                                        cardColor = cardColor,
                                        textColor = textColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// HEADER — Foto do restaurante + gradiente + botões
// ═══════════════════════════════════════════
@Composable
fun DetalhesHeader(
    restaurante: Restaurante,
    buscaVisivel: Boolean,
    topPadding: Dp,
    onBack: () -> Unit,
    onToggleBusca: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        // Foto de capa
        AsyncImage(
            model = restaurante.fotoRestaurante
                ?: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?q=80&w=900&auto=format&fit=crop",
            contentDescription = "Foto de ${restaurante.nome}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradiente escuro sobre a foto
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
        )

        // Botão voltar (topo-esquerda)
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(start = 12.dp, top = topPadding + 12.dp)
                .align(Alignment.TopStart)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Botão lupa (topo-direita)
        IconButton(
            onClick = onToggleBusca,
            modifier = Modifier
                .padding(end = 12.dp, top = topPadding + 12.dp)
                .align(Alignment.TopEnd)
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (buscaVisivel) Color(0xFF14B822).copy(alpha = 0.8f)
                    else Color.Black.copy(alpha = 0.5f)
                )
        ) {
            Icon(
                imageVector = if (buscaVisivel) Icons.Default.Close else Icons.Default.Search,
                contentDescription = "Buscar",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════
// INFO ROW — Nome, avaliação, tempo, taxa
// ═══════════════════════════════════════════
@Composable
fun DetalhesInfoRow(restaurante: Restaurante, textColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Nome do restaurante
        Text(
            text = restaurante.nome,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Info chips
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Avaliação
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = String.format("%.1f", restaurante.avaliacaoMedia.coerceAtLeast(0.0)),
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text("•", color = textColor.copy(alpha = 0.4f), fontSize = 14.sp)

            // Tempo de entrega
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "${restaurante.estimativaEntregaMin}-${restaurante.estimativaEntregaMax} min",
                color = textColor.copy(alpha = 0.7f),
                fontSize = 14.sp
            )

            Text("•", color = textColor.copy(alpha = 0.4f), fontSize = 14.sp)

            // Taxa de entrega
            Icon(
                imageVector = Icons.Default.TwoWheeler,
                contentDescription = null,
                tint = if (restaurante.taxaEntrega <= 0.0) Color(0xFF14B822) else textColor.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (restaurante.taxaEntrega <= 0.0) "Entrega Grátis"
                       else "R$ ${String.format("%.2f", restaurante.taxaEntrega)}",
                color = if (restaurante.taxaEntrega <= 0.0) Color(0xFF14B822) else textColor.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Categorias
        val cats = restaurante.categorias?.joinToString(" • ") { it.nome }
        if (!cats.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = cats,
                color = textColor.copy(alpha = 0.45f),
                fontSize = 12.sp
            )
        }
    }
}

// ═══════════════════════════════════════════
// BARRA DE BUSCA
// ═══════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalhesBuscaBar(
    query: String,
    onQueryChange: (String) -> Unit,
    cardColor: Color,
    textColor: Color
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(52.dp),
        placeholder = {
            Text(
                text = "Buscar no cardápio...",
                color = textColor.copy(alpha = 0.4f),
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = textColor.copy(alpha = 0.4f)
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
}

// ═══════════════════════════════════════════
// CHIPS DE SEÇÃO
// ═══════════════════════════════════════════
@Composable
fun SecaoChipsRow(
    secoes: List<String>,
    secaoSelecionada: String?,
    onSelecionarSecao: (String?) -> Unit,
    cardColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Chip "Todas"
        SecaoChip(
            label = "Todas",
            isSelected = secaoSelecionada == null,
            onClick = { onSelecionarSecao(null) },
            cardColor = cardColor,
            textColor = textColor
        )
        // Chips de cada seção
        secoes.forEach { secao ->
            SecaoChip(
                label = secao,
                isSelected = secaoSelecionada == secao,
                onClick = { onSelecionarSecao(secao) },
                cardColor = cardColor,
                textColor = textColor
            )
        }
    }
}

@Composable
fun SecaoChip(
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
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

// ═══════════════════════════════════════════
// TÍTULO DA SEÇÃO
// ═══════════════════════════════════════════
@Composable
fun SecaoTitulo(secao: String, textColor: Color) {
    Text(
        text = secao,
        color = textColor,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)
    )
}

// ═══════════════════════════════════════════
// ITEM DO PRATO
// ═══════════════════════════════════════════
@Composable
fun PratoItem(
    prato: Prato,
    cardColor: Color,
    textColor: Color
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coluna esquerda: nome, descrição, preço, botão +
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = prato.nome,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!prato.descricao.isNullOrBlank()) {
                    Text(
                        text = prato.descricao,
                        color = textColor.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "R$ ${String.format("%.2f", prato.preco)}",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    // Botão Adicionar sem preenchimento (estilo Outlined)
                    OutlinedButton(
                        onClick = { /* Futuro: adicionar ao carrinho */ },
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF14B822).copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF14B822)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adicionar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Foto do prato (direita)
            if (!prato.fotoPrato.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(12.dp))
                AsyncImage(
                    model = prato.fotoPrato,
                    contentDescription = "Foto de ${prato.nome}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(85.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
    }
}
