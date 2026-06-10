package dev.fslab.pedidos.ui.screens

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

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .allowHardware(true)
            .crossfade(true)
            .build()
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text("Meus Pedidos", fontWeight = FontWeight.Black, fontSize = 22.sp, color = colors.textPrimary)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(bottom = bottomPadding)) {
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
                    if (state.pedidos.isEmpty()) {
                        EmptyPedidos(colors)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.pedidos, key = { it.id }) { pedido ->
                                PedidoCard(pedido, imageLoader, colors, onNavigateToPedidoDetalhes)
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
                        pedido.restauranteNome,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
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
                    Text("Detalhes", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.primary)
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
        "entregue" -> Triple("Entregue", Color(0xFF14B822).copy(alpha = 0.1f), Color(0xFF14B822))
        "cancelado" -> Triple("Cancelado", Color(0xFFDC2626).copy(alpha = 0.1f), Color(0xFFDC2626))
        else -> Triple(status.uppercase(), colors.textTertiary.copy(alpha = 0.1f), colors.textTertiary)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(24.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Black, color = textColor)
        }
    }
}

@Composable
private fun EmptyPedidos(colors: dev.fslab.pedidos.ui.theme.PedidosColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(colors.surface), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(48.dp), tint = colors.textTertiary.copy(alpha = 0.3f))
            }
            Spacer(Modifier.height(24.dp))
            Text("Nenhum pedido ainda", fontWeight = FontWeight.Black, fontSize = 20.sp, color = colors.textPrimary)
            Spacer(Modifier.height(8.dp))
            Text("Seus pedidos aparecerão aqui assim que você finalizar sua primeira compra.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 14.sp, color = colors.textSecondary)
        }
    }
}
