package dev.fslab.pedidos.ui.screens

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fslab.pedidos.model.Endereco
import dev.fslab.pedidos.model.ItemCarrinho
import dev.fslab.pedidos.ui.components.EnderecosBottomSheet
import dev.fslab.pedidos.ui.components.PagamentoBottomSheet
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import dev.fslab.pedidos.ui.viewmodel.CarrinhoViewModel
import dev.fslab.pedidos.ui.viewmodel.FormaPagamento
import dev.fslab.pedidos.ui.viewmodel.PedidoUiState

private val Verde = Color(0xFF14B822)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarrinhoScreen(
    viewModel: CarrinhoViewModel,
    nomeRestaurante: String = "",
    enderecos: List<Endereco> = emptyList(),
    onBack: () -> Unit = {},
    onNavigateNovoEndereco: () -> Unit = {},
    onFinalizarPedido: (Endereco, String) -> Unit = { _, _ -> },
    onVoltarAoRestaurante: () -> Unit = {},
    pedidoState: PedidoUiState = PedidoUiState.Idle,
    onDismissErro: () -> Unit = {}
) {
    val colors = LocalPedidosColors.current
    val itens by viewModel.itens.collectAsState()
    val enderecoSelecionado by viewModel.enderecoSelecionado.collectAsState()
    val formaPagamento by viewModel.formaPagamento.collectAsState()
    val taxaEntrega by viewModel.taxaEntrega.collectAsState()

    val enderecoEfetivo = enderecoSelecionado
        ?: enderecos.find { it.principal }
        ?: enderecos.firstOrNull()

    LaunchedEffect(enderecos) {
        if (enderecos.isEmpty()) return@LaunchedEffect
        if (enderecoSelecionado == null) {
            val principal = enderecos.find { it.principal } ?: enderecos.first()
            viewModel.selecionarEndereco(principal)
        }
    }

    val context = LocalContext.current

    // Feedback de erro
    LaunchedEffect(pedidoState) {
        if (pedidoState is PedidoUiState.Error) {
            android.widget.Toast.makeText(context, pedidoState.message, android.widget.Toast.LENGTH_LONG).show()
            onDismissErro()
        }
    }

    var showEnderecoSheet by remember { mutableStateOf(false) }
    var showPagamentoSheet by remember { mutableStateOf(false) }

    val subtotal = itens.sumOf { it.precoTotal * it.quantidade }
    val total = subtotal + taxaEntrega

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Finalizar Pedido",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        if (nomeRestaurante.isNotBlank()) {
                            Text(
                                nomeRestaurante,
                                fontSize = 12.sp,
                                color = colors.textSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
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
        if (itens.isEmpty()) {
            EmptyCarrinho(onBack, colors)
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // ─── ENDEREÇO ───
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Verde.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.LocationOn,
                                        contentDescription = null,
                                        tint = Verde,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Entrega em",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        enderecoEfetivo?.let { "${it.rua}, ${it.numero}" } ?: "Selecione um endereço",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    "Trocar",
                                    color = Verde,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier
                                        .clickable { showEnderecoSheet = true }
                                        .padding(8.dp)
                                )
                            }
                        }
                    }

                    // ─── ITENS ───
                    item {
                        Text(
                            "Itens do pedido",
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.textPrimary.copy(alpha = 0.4f),
                            letterSpacing = 0.5.sp
                        )
                    }

                    items(itens, key = { it.id }) { item ->
                        CarrinhoItemPremium(item, viewModel, colors)
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            thickness = 0.5.dp,
                            color = colors.inputBorder.copy(alpha = 0.5f)
                        )
                    }

                    // ─── PAGAMENTO ───
                    item {
                        Text(
                            "Forma de Pagamento",
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.textPrimary.copy(alpha = 0.4f),
                            letterSpacing = 0.5.sp
                        )
                        
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = when (formaPagamento) {
                                    FormaPagamento.CARTAO_CREDITO -> Icons.Default.CreditCard
                                    FormaPagamento.CARTAO_DEBITO -> Icons.Default.CreditCard
                                    FormaPagamento.PIX -> Icons.Default.QrCode
                                    FormaPagamento.DINHEIRO -> Icons.Default.AttachMoney
                                }
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Verde.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = null, tint = Verde, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(formaPagamento.label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                    Text("Pague ao receber ou via app", fontSize = 11.sp, color = colors.textSecondary)
                                }
                                Text(
                                    "Trocar",
                                    color = Verde,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier
                                        .clickable { showPagamentoSheet = true }
                                        .padding(8.dp)
                                )
                            }
                        }
                    }

                    // ─── RESUMO ───
                    item {
                        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                            ResumoLinha("Subtotal", "R$ ${String.format("%.2f", subtotal).replace(".", ",")}", colors)
                            ResumoLinha("Taxa de entrega", if (taxaEntrega <= 0) "Grátis" else "R$ ${String.format("%.2f", taxaEntrega).replace(".", ",")}", colors, isGreen = taxaEntrega <= 0)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text("Total", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                Text("R$ ${String.format("%.2f", total).replace(".", ",")}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
                            }
                        }
                    }
                }

                // ─── BOTÃO CONFIRMAR ───
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.background,
                    shadowElevation = 16.dp
                ) {
                    Button(
                        onClick = {
                            if (enderecoEfetivo != null) {
                                onFinalizarPedido(enderecoEfetivo, formaPagamento.apiValue)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .height(58.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Verde),
                        enabled = pedidoState !is PedidoUiState.Loading && enderecoEfetivo != null
                    ) {
                        if (pedidoState is PedidoUiState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("CONFIRMAR PEDIDO", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }

    // Bottom Sheets
    if (showEnderecoSheet) {
        EnderecosBottomSheet(
            enderecos = enderecos,
            selectedEnderecoId = enderecoEfetivo?.id,
            onDismiss = { showEnderecoSheet = false },
            onNovoEnderecoClick = {
                showEnderecoSheet = false
                onNavigateNovoEndereco()
            },
            onEnderecoSelected = { viewModel.selecionarEndereco(it); showEnderecoSheet = false }
        )
    }

    if (showPagamentoSheet) {
        PagamentoBottomSheet(
            selectedForma = formaPagamento,
            onDismiss = { showPagamentoSheet = false },
            onFormaSelected = { viewModel.selecionarFormaPagamento(it); showPagamentoSheet = false }
        )
    }
}

@Composable
private fun EmptyCarrinho(onBack: () -> Unit, colors: dev.fslab.pedidos.ui.theme.PedidosColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(colors.surface), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(48.dp), tint = colors.textTertiary.copy(alpha = 0.3f))
            }
            Spacer(Modifier.height(24.dp))
            Text("Seu carrinho está vazio", fontWeight = FontWeight.Black, fontSize = 20.sp, color = colors.textPrimary)
            Spacer(Modifier.height(8.dp))
            Text("Você ainda não adicionou pratos ao seu carrinho. Que tal dar uma olhada no cardápio?", textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 14.sp, color = colors.textSecondary)
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onBack, 
                colors = ButtonDefaults.buttonColors(containerColor = Verde),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("VOLTAR AO CARDÁPIO", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CarrinhoItemPremium(item: ItemCarrinho, viewModel: CarrinhoViewModel, colors: dev.fslab.pedidos.ui.theme.PedidosColors) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.prato.nome, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textPrimary)
            if (item.opcoesSelecionadas.isNotEmpty()) {
                Text(
                    item.opcoesSelecionadas.joinToString(", ") { it.nome },
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (item.observacao.isNotBlank()) {
                Text(
                    "Obs: ${item.observacao}",
                    fontSize = 11.sp,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.Medium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "R$ ${String.format("%.2f", item.precoTotal * item.quantidade).replace(".", ",")}",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = Verde
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surface)
                .border(1.dp, colors.inputBorder.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            IconButton(
                onClick = { viewModel.decrementarItem(item.id) }, 
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.textPrimary)
            }
            Text(
                "${item.quantidade}", 
                modifier = Modifier.padding(horizontal = 8.dp), 
                fontWeight = FontWeight.ExtraBold, 
                fontSize = 14.sp,
                color = colors.textPrimary
            )
            IconButton(
                onClick = { viewModel.incrementarItem(item.id) }, 
                modifier = Modifier.size(28.dp).background(Verde, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
            }
        }
    }
}

@Composable
private fun ResumoLinha(label: String, value: String, colors: dev.fslab.pedidos.ui.theme.PedidosColors, isGreen: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = colors.textSecondary)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isGreen) Verde else colors.textPrimary)
    }
}
