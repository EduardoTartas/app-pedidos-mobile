package dev.fslab.pedidos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
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
    viewModel: RestauranteDetalhesViewModel = viewModel(),
    onNavigatePersonalizacao: (Prato) -> Unit = {},
    carrinhoTotalItens: Int = 0,
    carrinhoPrecoTotal: Double = 0.0,
    onVerCarrinho: () -> Unit = {}
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
    val pullRefreshState = androidx.compose.material3.pulltorefresh.rememberPullToRefreshState()

    var showHorariosDialog by remember { mutableStateOf(false) }

    if (showHorariosDialog && uiState is DetalhesUiState.Success) {
        val horarios = (uiState as DetalhesUiState.Success).restaurante.horarioFuncionamento
        AlertDialog(
            onDismissRequest = { showHorariosDialog = false },
            containerColor = bgColor,
            titleContentColor = textColor,
            textContentColor = textColor.copy(alpha = 0.8f),
            title = { 
                Text(
                    text = "Horário de Funcionamento", 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 18.sp, 
                    textAlign = TextAlign.Center, 
                    modifier = Modifier.fillMaxWidth()
                ) 
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(), 
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val dias = listOf("segunda", "terca", "quarta", "quinta", "sexta", "sabado", "domingo")
                    val labels = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo")
                    
                    val cal = java.util.Calendar.getInstance()
                    val hojeIdx = when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
                        java.util.Calendar.MONDAY -> 0
                        java.util.Calendar.TUESDAY -> 1
                        java.util.Calendar.WEDNESDAY -> 2
                        java.util.Calendar.THURSDAY -> 3
                        java.util.Calendar.FRIDAY -> 4
                        java.util.Calendar.SATURDAY -> 5
                        java.util.Calendar.SUNDAY -> 6
                        else -> -1
                    }
                    
                    dias.forEachIndexed { index, dia ->
                        val hr = horarios?.find { it.dia == dia }
                        val isHoje = index == hojeIdx
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isHoje) Color(0xFF14B822).copy(alpha = 0.1f) else Color.Transparent)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = labels[index] + if (isHoje) " (Hoje)" else "", 
                                fontWeight = if (isHoje) FontWeight.Bold else FontWeight.Medium, 
                                fontSize = 14.sp,
                                color = if (isHoje) Color(0xFF14B822) else textColor.copy(alpha = 0.8f)
                            )
                            if (hr != null && !hr.fechado) {
                                Text(
                                    text = "${hr.abertura} às ${hr.fechamento}", 
                                    fontWeight = if (isHoje) FontWeight.Bold else FontWeight.Medium, 
                                    fontSize = 14.sp, 
                                    color = if (isHoje) Color(0xFF14B822) else textColor.copy(alpha = 0.8f)
                                )
                            } else {
                                Text(
                                    text = "Fechado", 
                                    fontWeight = if (isHoje) FontWeight.Bold else FontWeight.Medium, 
                                    fontSize = 14.sp, 
                                    color = if (isHoje) Color(0xFFEF4444) else textColor.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHorariosDialog = false },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14B822)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("FECHAR", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

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
                    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
                        isRefreshing = state.atualizando,
                        onRefresh = { viewModel.refreshDados(restauranteId) },
                        state = pullRefreshState,
                        modifier = Modifier.fillMaxSize(),
                        indicator = {
                            androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator(
                                modifier = Modifier.align(Alignment.TopCenter),
                                isRefreshing = state.atualizando,
                                state = pullRefreshState,
                                containerColor = bgColor,
                                color = Color(0xFF14B822)
                            )
                        }
                    ) {
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
                                textColor = textColor,
                                onVerHorarios = { showHorariosDialog = true }
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
                                        cardColor = bgColor,
                                        textColor = textColor,
                                        onAdicionar = { onNavigatePersonalizacao(prato) }
                                    )
                                }
                            }
                        }
                    }
                    } // closes PullToRefreshBox
                }
            }

            // Barra flutuante do carrinho
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
            .height(260.dp)
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
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetalhesInfoRow(restaurante: Restaurante, textColor: Color, onVerHorarios: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Nome do restaurante
        Text(
            text = restaurante.nome.replace("`", "'").replace("´", "'"),
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (!restaurante.descricao.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = restaurante.descricao,
                color = textColor.copy(alpha = 0.65f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Info chips (FlowRow to prevent clipping on smaller screens)
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
                text = if (restaurante.avaliacaoMedia > 0) String.format("%.1f", restaurante.avaliacaoMedia) else "Novo",
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
                text = if (restaurante.taxaEntrega <= 0.0) "Grátis"
                       else "R$ ${String.format("%.2f", restaurante.taxaEntrega)}",
                color = if (restaurante.taxaEntrega <= 0.0) Color(0xFF14B822) else textColor.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        val statusFunc = calcularStatus(restaurante.horarioFuncionamento)
        if (statusFunc != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onVerHorarios() }
                    .background(statusFunc.cor.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = statusFunc.cor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = statusFunc.texto,
                    color = statusFunc.cor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Ver horários",
                    tint = statusFunc.cor.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
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

data class StatusInfo(val texto: String, val cor: Color)

private fun calcularStatus(horarios: List<dev.fslab.pedidos.model.HorarioFuncionamento>?): StatusInfo? {
    if (horarios.isNullOrEmpty()) return null

    val cal = java.util.Calendar.getInstance()
    val diaNum = cal.get(java.util.Calendar.DAY_OF_WEEK)
    
    val diaString = when (diaNum) {
        java.util.Calendar.MONDAY -> "segunda"
        java.util.Calendar.TUESDAY -> "terca"
        java.util.Calendar.WEDNESDAY -> "quarta"
        java.util.Calendar.THURSDAY -> "quinta"
        java.util.Calendar.FRIDAY -> "sexta"
        java.util.Calendar.SATURDAY -> "sabado"
        java.util.Calendar.SUNDAY -> "domingo"
        else -> ""
    }

    val hoje = horarios.find { it.dia == diaString } ?: return null

    if (hoje.fechado) {
        return StatusInfo("Fechado hoje", Color(0xFFEF4444))
    }

    return try {
        val horaAtual = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val minAtual = cal.get(java.util.Calendar.MINUTE)
        val minutosAtuais = horaAtual * 60 + minAtual

        val (horaAbre, minAbre) = hoje.abertura.split(":").map { it.toInt() }
        val (horaFecha, minFecha) = hoje.fechamento.split(":").map { it.toInt() }
        
        val minutosAbertura = horaAbre * 60 + minAbre
        val minutosFechamento = horaFecha * 60 + minFecha

        if (minutosAtuais in minutosAbertura until minutosFechamento) {
            StatusInfo("Aberto até às ${hoje.fechamento}", Color(0xFF14B822))
        } else if (minutosAtuais < minutosAbertura) {
            StatusInfo("Abre às ${hoje.abertura}", Color(0xFFFBBF24))
        } else {
            StatusInfo("Fechado", Color(0xFFEF4444))
        }
    } catch (e: Exception) {
        StatusInfo("${hoje.abertura} - ${hoje.fechamento}", Color.Gray)
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
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    TextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { focusManager.clearFocus() }),
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
// ITEM DO PRATO — Premium e Visual
// ═══════════════════════════════════════════
@Composable
fun PratoItem(
    prato: Prato,
    cardColor: Color,
    textColor: Color,
    onAdicionar: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onAdicionar() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Coluna esquerda: nome, descrição, preço
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = prato.nome,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!prato.descricao.isNullOrBlank()) {
                    Text(
                        text = prato.descricao,
                        color = textColor.copy(alpha = 0.55f),
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "R$ ${String.format("%.2f", prato.preco)}",
                    color = Color(0xFF14B822),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }

            // Foto do prato (DIREITA)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(textColor.copy(alpha = 0.04f))
                    .border(
                        width = 1.dp,
                        color = textColor.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!prato.fotoPrato.isNullOrBlank()) {
                    AsyncImage(
                        model = prato.fotoPrato,
                        contentDescription = "Foto de ${prato.nome}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = textColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}
