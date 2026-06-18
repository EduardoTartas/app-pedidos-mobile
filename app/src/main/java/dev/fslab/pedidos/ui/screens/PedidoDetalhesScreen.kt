package dev.fslab.pedidos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.SvgDecoder
import dev.fslab.pedidos.model.Pedido
import dev.fslab.pedidos.ui.components.ErrorStateComponent
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import dev.fslab.pedidos.ui.viewmodel.PedidoDetalhesUiState
import dev.fslab.pedidos.ui.viewmodel.PedidoDetalhesViewModel

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

private val Verde = Color(0xFF14B822)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoDetalhesScreen(
    pedidoId: String,
    onBack: () -> Unit = {},
    viewModel: PedidoDetalhesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalPedidosColors.current
    val context = LocalContext.current
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(pedidoId) {
        viewModel.carregarPedido(pedidoId)
    }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .allowHardware(true)
            .crossfade(true)
            .build()
    }

    var showCancelDialog by remember { mutableStateOf(false) }
    var showAvaliacaoDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var nota by remember { mutableIntStateOf(5) }
    var descricaoAvaliacao by remember { mutableStateOf("") }

    val avaliacaoSucesso by viewModel.avaliacaoSucesso.collectAsState()

    LaunchedEffect(avaliacaoSucesso) {
        if (avaliacaoSucesso) {
            showAvaliacaoDialog = false
            showSuccessDialog = true
            viewModel.resetAvaliacao()
        }
    }
    // ...
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            containerColor = colors.surface,
            shape = RoundedCornerShape(28.dp),
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Verde),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("FECHAR", fontWeight = FontWeight.Black)
                }
            },
            title = null,
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Verde.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Verde,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Avaliação enviada!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Obrigado por ajudar o restaurante a melhorar!",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = colors.textSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            containerColor = colors.surface,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textSecondary,
            icon = { Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.Red, modifier = Modifier.size(40.dp)) },
            title = { Text("Cancelar Pedido?", fontWeight = FontWeight.Black, fontSize = 18.sp) },
            text = { Text("Deseja realmente cancelar este pedido? Esta ação não poderá ser desfeita.", textAlign = TextAlign.Center) },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, start = 12.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { showCancelDialog = false },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("MANTER", color = colors.textTertiary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { 
                            (uiState as? PedidoDetalhesUiState.Success)?.pedido?.let { viewModel.cancelarPedido(it.id) }
                            showCancelDialog = false 
                        },
                        modifier = Modifier.weight(1.6f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "CANCELAR AGORA", 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
            },
            dismissButton = null,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showAvaliacaoDialog) {
        val isAvaliando by viewModel.isAvaliando.collectAsState()
        AlertDialog(
            onDismissRequest = { if (!isAvaliando) showAvaliacaoDialog = false },
            containerColor = colors.surface,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textSecondary,
            title = { Text("Avaliar Pedido", fontWeight = FontWeight.Black, fontSize = 18.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text = { 
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Como foi sua experiência?", fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        for (i in 1..5) {
                            IconButton(onClick = { nota = i }) {
                                Icon(
                                    imageVector = if (i <= nota) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$i Estrelas",
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = descricaoAvaliacao,
                        onValueChange = { descricaoAvaliacao = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Opcional: Deixe um comentário...", fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Verde,
                            unfocusedBorderColor = colors.inputBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.avaliarPedido(pedidoId, nota, descricaoAvaliacao) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Verde),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isAvaliando
                ) {
                    if (isAvaliando) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("ENVIAR AVALIAÇÃO", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAvaliacaoDialog = false }, modifier = Modifier.fillMaxWidth(), enabled = !isAvaliando) {
                    Text("CANCELAR", color = colors.textTertiary, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text("Detalhes do Pedido", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.textPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is PedidoDetalhesUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = colors.primary)
                }
                is PedidoDetalhesUiState.Error -> {
                    ErrorStateComponent(
                        message = state.message,
                        onRetry = { viewModel.carregarPedido(pedidoId) }
                    )
                }
                is PedidoDetalhesUiState.Success -> {
                    val pedido = state.pedido
                    
                    PullToRefreshBox(
                        isRefreshing = state.atualizando,
                        onRefresh = { viewModel.refreshPedido(pedidoId) },
                        state = pullRefreshState,
                        modifier = Modifier.fillMaxSize(),
                        indicator = {
                            PullToRefreshDefaults.Indicator(
                                modifier = Modifier.align(Alignment.TopCenter),
                                isRefreshing = state.atualizando,
                                state = pullRefreshState,
                                containerColor = colors.background,
                                color = Verde
                            )
                        }
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            // ─── CABEÇALHO (RESTAURANTE + STATUS) ───
                        item {
                            PedidoHeader(pedido, imageLoader, colors)
                        }

                        // ─── ITENS ───
                        item {
                            SectionHeader("ITENS DO PEDIDO")
                        }
                        items(pedido.itens) { item ->
                            ItemLinha(item, colors)
                        }

                        // ─── RESUMO DE VALORES ───
                        item {
                            TotaisCard(pedido.totais, colors)
                        }

                        // ─── PAGAMENTO ───
                        item {
                            SectionHeader("PAGAMENTO")
                            PagamentoInfo(pedido.formaPagamento, colors)
                        }

                        // ─── TIMELINE DE STATUS ───
                        item {
                            SectionHeader("ACOMPANHAMENTO")
                            TimelineStatus(pedido.historicoStatus, colors)
                        }

                        // ─── FOOTER ID ───
                        item {
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "ID: ${pedido.id}",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp,
                                color = colors.textTertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // ─── BOTÃO CANCELAR (SE PENDENTE) ───
                        if (pedido.status == "criado" || pedido.status == "pendente") {
                            item {
                                val isCancelling by viewModel.isCancelling.collectAsState()
                                
                                Spacer(Modifier.height(24.dp))
                                Button(
                                    onClick = { showCancelDialog = true },

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = Color.Red
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f)),
                                    enabled = !isCancelling
                                ) {
                                    if (isCancelling) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.Red)
                                    } else {
                                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("CANCELAR PEDIDO", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                                Text(
                                    "Você só pode cancelar enquanto o restaurante não inicia o preparo.",
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 8.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 10.sp,
                                    color = colors.textTertiary
                                )
                            }
                        }

                        // ─── BOTÃO AVALIAR (SE ENTREGUE E NÃO AVALIADO) ───
                        if (pedido.status == "entregue") {
                            item {
                                Spacer(Modifier.height(24.dp))
                                if (pedido.avaliacaoId == null) {
                                    Button(
                                        onClick = { showAvaliacaoDialog = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp)
                                            .height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Verde),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                        Spacer(Modifier.width(8.dp))
                                        Text("AVALIAR PEDIDO", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    }
                                } else {
                                    // Avaliação já enviada
                                    val nota = (pedido.avaliacaoId as? Map<*, *>)?.get("nota")?.let { (it as Double).toInt() } ?: 5
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFEAB308).copy(alpha = 0.1f))
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Pedido Avaliado", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFEAB308))
                                            Text("Obrigado pelo seu feedback!", fontSize = 11.sp, color = colors.textSecondary)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            for (i in 1..5) {
                                                Icon(
                                                    imageVector = if (i <= nota) Icons.Default.Star else Icons.Default.StarBorder,
                                                    contentDescription = null,
                                                    tint = Color(0xFFEAB308),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    } // closes PullToRefreshBox
                }
            }
        }
    }
}

@Composable
private fun PedidoHeader(pedido: Pedido, imageLoader: ImageLoader, colors: dev.fslab.pedidos.ui.theme.PedidosColors) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = pedido.restauranteFoto,
                imageLoader = imageLoader,
                contentDescription = null,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(pedido.restauranteNome, fontSize = 18.sp, fontWeight = FontWeight.Black, color = colors.textPrimary)
                Text("Pedido #${pedido.id.takeLast(6).uppercase()}", fontSize = 13.sp, color = colors.textSecondary)
            }
            StatusBadgeCompact(pedido.status)
        }
    }
}

@Composable
private fun ItemLinha(item: dev.fslab.pedidos.model.ItemPedidoCriado, colors: dev.fslab.pedidos.ui.theme.PedidosColors) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "${item.quantidade}x ${item.pratoNome}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "R$ ${String.format("%.2f", item.precoUnitario * item.quantidade).replace(".", ",")}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }

        if (!item.observacao.isNullOrBlank()) {
            Surface(
                color = Color(0xFFFFB01E).copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = Color(0xFFFFB01E), modifier = Modifier.size(12.dp))
                    Text(
                        text = "Obs: ${item.observacao}",
                        fontSize = 11.sp,
                        color = Color(0xFFB47B00),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // Adicionais
        item.adicionais.forEach { adicional ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "+ ${adicional.quantidade}x ${adicional.opcaoNome}",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                if (adicional.precoUnitario > 0) {
                    Text(
                        text = "R$ ${String.format("%.2f", adicional.precoUnitario * adicional.quantidade).replace(".", ",")}",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), thickness = 0.5.dp, color = colors.textPrimary.copy(alpha = 0.05f))
}

@Composable
private fun TotaisCard(totais: dev.fslab.pedidos.model.TotaisPedido, colors: dev.fslab.pedidos.ui.theme.PedidosColors) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.textPrimary.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ResumoLinha("Subtotal", "R$ ${String.format("%.2f", totais.subtotal)}", colors)
            ResumoLinha("Taxa de entrega", if (totais.taxaEntrega <= 0) "Grátis" else "R$ ${String.format("%.2f", totais.taxaEntrega)}", colors, isGreen = totais.taxaEntrega <= 0)
            HorizontalDivider(color = colors.textPrimary.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Text("R$ ${String.format("%.2f", totais.total)}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = colors.primary)
            }
        }
    }
}

@Composable
private fun PagamentoInfo(forma: String?, colors: dev.fslab.pedidos.ui.theme.PedidosColors) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Verde.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Payment, contentDescription = null, tint = Verde, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(forma ?: "Não informado", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
    }
}

@Composable
private fun TimelineStatus(historico: List<dev.fslab.pedidos.model.HistoricoStatus>, colors: dev.fslab.pedidos.ui.theme.PedidosColors) {
    Column(modifier = Modifier.padding(20.dp)) {
        historico.asReversed().forEachIndexed { index, item ->
            val isLast = index == historico.size - 1
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(12.dp).clip(CircleShape).background(if (index == 0) Verde else colors.textTertiary.copy(alpha = 0.3f))
                    )
                    if (!isLast) {
                        Box(
                            modifier = Modifier.width(2.dp).fillMaxHeight().background(colors.textTertiary.copy(alpha = 0.1f))
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = mapearStatusLabel(item.status),
                        fontSize = 14.sp,
                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (index == 0) colors.textPrimary else colors.textSecondary
                    )
                    if (!item.data.isNullOrBlank()) {
                        Text(
                            text = formatarDataIso(item.data),
                            fontSize = 11.sp,
                            color = colors.textTertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    val colors = LocalPedidosColors.current
    Text(
        text = title,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        color = colors.textTertiary.copy(alpha = 0.6f),
        letterSpacing = 1.sp
    )
}

@Composable
private fun ResumoLinha(label: String, value: String, colors: dev.fslab.pedidos.ui.theme.PedidosColors, isGreen: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = colors.textSecondary)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isGreen) Verde else colors.textPrimary)
    }
}

@Composable
private fun StatusBadgeCompact(status: String) {
    val (label, bgColor, textColor) = when (status) {
        "criado", "pendente" -> Triple("Pendente", Color(0xFFFFB01E).copy(alpha = 0.1f), Color(0xFFFFB01E))
        "em_preparo" -> Triple("Preparando", Color(0xFF3B82F6).copy(alpha = 0.1f), Color(0xFF3B82F6))
        "a_caminho" -> Triple("No caminho", Color(0xFF8B5CF6).copy(alpha = 0.1f), Color(0xFF8B5CF6))
        "entregue" -> Triple("Entregue", Color(0xFF14B822).copy(alpha = 0.1f), Color(0xFF14B822))
        "cancelado" -> Triple("Cancelado", Color(0xFFDC2626).copy(alpha = 0.1f), Color(0xFFDC2626))
        else -> Triple(status.uppercase(), Color.Gray.copy(alpha = 0.1f), Color.Gray)
    }
    Surface(color = bgColor, shape = RoundedCornerShape(8.dp)) {
        Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Black, color = textColor)
    }
}

private fun mapearStatusLabel(status: String): String = when (status) {
    "criado", "pendente" -> "Pedido realizado"
    "em_preparo" -> "Na cozinha"
    "a_caminho" -> "Saiu para entrega"
    "entregue" -> "Entregue"
    "cancelado" -> "Cancelado"
    else -> status.uppercase()
}

private fun formatarDataIso(iso: String): String {
    return try {
        // Formato simples para exemplo. Em prod usar java.time ou date-fns equivalents
        iso.substring(11, 16) + " • " + iso.substring(8, 10) + "/" + iso.substring(5, 7)
    } catch (e: Exception) {
        iso
    }
}
