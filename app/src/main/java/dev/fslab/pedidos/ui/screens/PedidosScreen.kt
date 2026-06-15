package dev.fslab.pedidos.ui.screens

import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.SvgDecoder
import dev.fslab.pedidos.model.Pedido
import dev.fslab.pedidos.ui.components.ErrorStateComponent
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import dev.fslab.pedidos.ui.viewmodel.PedidosHistoryUiState
import dev.fslab.pedidos.ui.viewmodel.PedidosHistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidosScreen(
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onNavigateToPedidoDetalhes: (String) -> Unit = {},
    viewModel: PedidosHistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalPedidosColors.current
    val context = LocalContext.current
    val pullRefreshState = rememberPullToRefreshState()

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .allowHardware(true)
            .crossfade(true)
            .build()
    }

    // Atualiza a lista sempre que a tela for aberta
    LaunchedEffect(Unit) {
        viewModel.carregarPedidos()
    }

    Scaffold(
        containerColor = colors.background
    ) { innerPadding ->
        val isRefreshing by viewModel.isRefreshing.collectAsState()

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            state = pullRefreshState,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = isRefreshing,
                    state = pullRefreshState,
                    color = colors.primary,
                    containerColor = colors.background
                )
            },
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = uiState) {
                is PedidosHistoryUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = colors.primary)
                }
                is PedidosHistoryUiState.Error -> {
                    ErrorStateComponent(
                        message = state.message,
                        onRetry = { viewModel.carregarPedidos() }
                    )
                }
                is PedidosHistoryUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = bottomPadding + 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.statusBarsPadding())
                        }
                        
                        // Cabeçalho Estilizado (Não mais cru)
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp)
                            ) {
                                Text(
                                    text = "Meus Pedidos",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.textPrimary,
                                    letterSpacing = (-0.5).sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Acompanhe seu histórico e rastreio",
                                    fontSize = 14.sp,
                                    color = colors.textSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (state.pedidos.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    EmptyPedidos(colors)
                                }
                            }
                        } else {
                            items(state.pedidos, key = { it.id }) { pedido ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    PedidoCard(pedido, imageLoader, colors, onNavigateToPedidoDetalhes)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PedidoCard(
    pedido: Pedido, 
    imageLoader: ImageLoader, 
    colors: dev.fslab.pedidos.ui.theme.PedidosColors,
    onClick: (String) -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick(pedido.id) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Foto do restaurante
                AsyncImage(
                    model = pedido.restauranteFoto,
                    imageLoader = imageLoader,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        pedido.restauranteNome.replace("`", "'").replace("´", "'"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Pedido #${pedido.id.takeLast(6).uppercase()}",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
                
                StatusBadge(pedido.status, colors)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = colors.textPrimary.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(12.dp))
            
            // Resumo dos itens
            Text(
                text = pedido.itens.joinToString(", ") { "${it.quantidade}x ${it.pratoNome}" },
                fontSize = 13.sp,
                color = colors.textPrimary.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Feedback Visual de Avaliação
            if (pedido.status == "entregue") {
                Spacer(modifier = Modifier.height(14.dp))
                if (pedido.avaliacaoId == null) {
                    // Pendente de avaliação
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEAB308).copy(alpha = 0.1f))
                            .padding(vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFEAB308),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Avalie este pedido",
                            color = Color(0xFFEAB308),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    // Já avaliado
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.primary.copy(alpha = 0.1f))
                            .padding(vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pedido Avaliado",
                            color = colors.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total: R$ ${String.format("%.2f", pedido.totais.total).replace(".", ",")}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.textPrimary
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Detalhes", fontSize = 13.sp, color = colors.primary, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String, colors: dev.fslab.pedidos.ui.theme.PedidosColors) {
    val (label, bgColor, textColor) = when (status) {
        "criado" -> Triple("Pendente", Color(0xFFFFB01E).copy(alpha = 0.1f), Color(0xFFFFB01E))
        "em_preparo" -> Triple("Preparando", Color(0xFF3B82F6).copy(alpha = 0.1f), Color(0xFF3B82F6))
        "a_caminho" -> Triple("A caminho", Color(0xFF8B5CF6).copy(alpha = 0.1f), Color(0xFF8B5CF6))
        "entregue" -> Triple("Entregue", colors.primary.copy(alpha = 0.1f), colors.primary)
        "cancelado" -> Triple("Cancelado", Color(0xFFEF4444).copy(alpha = 0.1f), Color(0xFFEF4444))
        else -> Triple(status.uppercase(), Color.Gray.copy(alpha = 0.1f), Color.Gray)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyPedidos(colors: dev.fslab.pedidos.ui.theme.PedidosColors) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = colors.textPrimary.copy(alpha = 0.1f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Você ainda não fez nenhum pedido",
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            "Seus pedidos aparecerão aqui",
            color = colors.textSecondary,
            fontSize = 14.sp
        )
    }
}
