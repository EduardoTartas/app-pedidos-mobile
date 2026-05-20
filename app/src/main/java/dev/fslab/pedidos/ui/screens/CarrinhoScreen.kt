package dev.fslab.pedidos.ui.screens

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

private val Verde = Color(0xFF14B822)

// ═══════════════════════════════════════════════════════
// TELA PRINCIPAL DO CARRINHO
// ═══════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarrinhoScreen(
    viewModel: CarrinhoViewModel,
    nomeRestaurante: String = "",
    enderecos: List<Endereco> = emptyList(),
    onBack: () -> Unit = {},
    onNavigateNovoEndereco: () -> Unit = {},
    onFinalizarPedido: () -> Unit = {},
    onVoltarAoRestaurante: () -> Unit = {}
) {
    val colors = LocalPedidosColors.current
    val itens by viewModel.itens.collectAsState()
    val enderecoSelecionado by viewModel.enderecoSelecionado.collectAsState()
    val formaPagamento by viewModel.formaPagamento.collectAsState()

    // Encontra o endereço efetivo para exibição:
    // Prioridade: endereço salvo no VM → principal → primeiro da lista
    val enderecoEfetivo = enderecoSelecionado
        ?: enderecos.find { it.principal }
        ?: enderecos.firstOrNull()

    // Sincroniza o endereço selecionado sempre que a lista muda.
    // Isso garante que após criar um novo endereço e voltar,
    // o novo endereço apareça disponível no bottom sheet.
    LaunchedEffect(enderecos) {
        if (enderecos.isEmpty()) return@LaunchedEffect

        val atualSelecionado = enderecoSelecionado
        when {
            // Nenhum endereço selecionado ainda → usa principal ou primeiro
            atualSelecionado == null -> {
                val principal = enderecos.find { it.principal } ?: enderecos.first()
                viewModel.selecionarEndereco(principal)
            }
            // O endereço selecionado não existe mais na lista atualizada
            // (ex: foi deletado) → reseleciona o principal
            enderecos.none { it.id == atualSelecionado.id } -> {
                val principal = enderecos.find { it.principal } ?: enderecos.first()
                viewModel.selecionarEndereco(principal)
            }
            // O endereço selecionado existe mas pode ter sido atualizado
            // → sincroniza com a versão atualizada da lista
            else -> {
                val atualizado = enderecos.find { it.id == atualSelecionado.id }
                if (atualizado != null && atualizado != atualSelecionado) {
                    viewModel.selecionarEndereco(atualizado)
                }
            }
        }
    }

    // Controla se o sheet de endereço deve ser reaberto ao voltar
    // da tela de novo endereço (navegação de volta)
    var showEnderecoSheet by remember { mutableStateOf(false) }
    var showPagamentoSheet by remember { mutableStateOf(false) }
    var reopenEnderecoSheetOnUpdate by remember { mutableStateOf(false) }

    // Estado para o modal de confirmação de remoção do último item
    var itemParaRemover by remember { mutableStateOf<dev.fslab.pedidos.model.ItemCarrinho?>(null) }
    var showConfirmacaoRemoverUltimo by remember { mutableStateOf(false) }

    // Função que decide se mostra confirmação ou remove diretamente
    fun onTentarRemover(item: dev.fslab.pedidos.model.ItemCarrinho) {
        if (itens.size == 1 && item.quantidade == 1) {
            // Último item: pede confirmação
            itemParaRemover = item
            showConfirmacaoRemoverUltimo = true
        } else {
            viewModel.removerItem(item.id)
        }
    }

    fun onTentarDecrementar(item: dev.fslab.pedidos.model.ItemCarrinho) {
        if (itens.size == 1 && item.quantidade == 1) {
            // Ao chegar em 0 no último item: pede confirmação
            itemParaRemover = item
            showConfirmacaoRemoverUltimo = true
        } else {
            viewModel.decrementarItem(item.id)
        }
    }

    LaunchedEffect(enderecos, reopenEnderecoSheetOnUpdate) {
        if (reopenEnderecoSheetOnUpdate && enderecos.isNotEmpty()) {
            showEnderecoSheet = true
            reopenEnderecoSheetOnUpdate = false
        }
    }


    val subtotal = itens.sumOf { it.precoTotal * it.quantidade }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            CarrinhoTopBar(
                nomeRestaurante = nomeRestaurante,
                onBack = onBack
            )
        }
    ) { innerPadding ->

        if (itens.isEmpty()) {
            // Estado vazio
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🛒",
                        fontSize = 60.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Seu carrinho está vazio",
                        color = colors.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Adicione itens do cardápio para continuar",
                        color = colors.textPrimary.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Verde),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Ver cardápio",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // ─── Seção de Endereço ───
                    item {
                        EnderecoSection(
                            endereco = enderecoEfetivo,
                            onAlterar = { showEnderecoSheet = true }
                        )
                    }

                    // ─── Divisor ───
                    item { HorizontalDivider(color = colors.textPrimary.copy(alpha = 0.07f)) }

                    // ─── Itens do Carrinho ───
                    items(itens, key = { it.id }) { item ->
                        CarrinhoItemRow(
                            item = item,
                            onIncrementar = { viewModel.incrementarItem(item.id) },
                            onDecrementar = { onTentarDecrementar(item) },
                            onRemover = { onTentarRemover(item) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = colors.textPrimary.copy(alpha = 0.05f)
                        )
                    }

                    // ─── Seção de Pagamento ───
                    item {
                        PagamentoSection(
                            formaPagamento = formaPagamento,
                            onAlterar = { showPagamentoSheet = true }
                        )
                    }

                    item { HorizontalDivider(color = colors.textPrimary.copy(alpha = 0.07f)) }

                    // ─── Resumo de Valores ───
                    item {
                        ResumoValores(subtotal = subtotal)
                    }
                }

                // ─── Botão Finalizar ───
                BotaoFinalizar(onClick = onFinalizarPedido)
            }
        }
    }

    // Modal de confirmação para remover o último item
    if (showConfirmacaoRemoverUltimo && itemParaRemover != null) {
        AlertDialog(
            onDismissRequest = {
                showConfirmacaoRemoverUltimo = false
                itemParaRemover = null
            },
            containerColor = Verde.copy(alpha = 0.97f).let {
                if (androidx.compose.foundation.isSystemInDarkTheme())
                    androidx.compose.ui.graphics.Color(0xFF1A202C)
                else
                    androidx.compose.ui.graphics.Color.White
            },
            icon = {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                    contentDescription = null,
                    tint = Verde,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Esvaziar carrinho?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = LocalPedidosColors.current.textPrimary
                )
            },
            text = {
                Text(
                    text = "Esse é o único item do seu carrinho. Ao removê-lo, você será redirecionado de volta ao restaurante.",
                    fontSize = 14.sp,
                    color = LocalPedidosColors.current.textPrimary.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removerItem(itemParaRemover!!.id)
                        showConfirmacaoRemoverUltimo = false
                        itemParaRemover = null
                        onVoltarAoRestaurante()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Verde),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Remover e voltar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmacaoRemoverUltimo = false
                        itemParaRemover = null
                    }
                ) {
                    Text("Cancelar", color = Verde, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Bottom Sheet de Endereço
    if (showEnderecoSheet) {
        EnderecosBottomSheet(
            enderecos = enderecos,
            selectedEnderecoId = enderecoEfetivo?.id,
            onDismiss = { showEnderecoSheet = false },
            onNovoEnderecoClick = {
                showEnderecoSheet = false
                // Sinaliza para reabrir o sheet quando a lista atualizar
                reopenEnderecoSheetOnUpdate = true
                onNavigateNovoEndereco()
            },
            onEnderecoSelected = { endereco ->
                viewModel.selecionarEndereco(endereco)
            }
        )
    }

    // Bottom Sheet de Pagamento
    if (showPagamentoSheet) {
        PagamentoBottomSheet(
            selectedForma = formaPagamento,
            onDismiss = { showPagamentoSheet = false },
            onFormaSelected = { forma ->
                viewModel.selecionarFormaPagamento(forma)
            }
        )
    }
}

// ═══════════════════════════════════════════════════════
// TOP BAR
// ═══════════════════════════════════════════════════════
@Composable
private fun CarrinhoTopBar(
    nomeRestaurante: String,
    onBack: () -> Unit
) {
    val colors = LocalPedidosColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Botão voltar
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Voltar",
                tint = colors.textPrimary
            )
        }

        // Título centralizado
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Meu Carrinho",
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            if (nomeRestaurante.isNotBlank()) {
                Text(
                    text = nomeRestaurante,
                    color = colors.textPrimary.copy(alpha = 0.55f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// SEÇÃO DE ENDEREÇO
// ═══════════════════════════════════════════════════════
@Composable
private fun EnderecoSection(
    endereco: Endereco?,
    onAlterar: () -> Unit
) {
    val colors = LocalPedidosColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = Verde,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "ENTREGAR EM:",
                color = colors.textPrimary.copy(alpha = 0.45f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            if (endereco != null) {
                Text(
                    text = "${endereco.rua}, ${endereco.numero} - ${endereco.bairro}",
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "Nenhum endereço selecionado",
                    color = colors.textPrimary.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Alterar",
            color = Verde,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onAlterar() }
        )
    }
}

// ═══════════════════════════════════════════════════════
// ITEM DO CARRINHO
// ═══════════════════════════════════════════════════════
@Composable
private fun CarrinhoItemRow(
    item: ItemCarrinho,
    onIncrementar: () -> Unit,
    onDecrementar: () -> Unit,
    onRemover: () -> Unit
) {
    val colors = LocalPedidosColors.current

    // Descrição das opções selecionadas
    val descricaoOpcoes = item.opcoesSelecionadas.joinToString(", ") { it.nome }.ifBlank {
        item.prato.descricao?.take(50) ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.prato.nome,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                if (descricaoOpcoes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = descricaoOpcoes,
                        color = colors.textPrimary.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "R$ ${String.format("%.2f", item.precoTotal * item.quantidade).replace(".", ",")}",
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Controle de quantidade
            QuantidadeControl(
                quantidade = item.quantidade,
                onMinus = onDecrementar,
                onPlus = onIncrementar
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botão remover
            IconButton(
                onClick = onRemover,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.textPrimary.copy(alpha = 0.06f))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remover",
                    tint = colors.textPrimary.copy(alpha = 0.45f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// CONTROLE DE QUANTIDADE ( — 1 + )
// ═══════════════════════════════════════════════════════
@Composable
private fun QuantidadeControl(
    quantidade: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    val colors = LocalPedidosColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surface)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        // Botão −
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Verde)
                .clickable { onMinus() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "−",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "$quantidade",
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Botão +
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Verde)
                .clickable { onPlus() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 18.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// SEÇÃO DE PAGAMENTO
// ═══════════════════════════════════════════════════════
@Composable
private fun PagamentoSection(
    formaPagamento: FormaPagamento,
    onAlterar: () -> Unit
) {
    val colors = LocalPedidosColors.current

    val icon: ImageVector = when (formaPagamento) {
        FormaPagamento.CARTAO  -> Icons.Default.CreditCard
        FormaPagamento.PIX     -> Icons.Default.QrCode
        FormaPagamento.DINHEIRO -> Icons.Default.AttachMoney
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Verde,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "PAGAMENTO:",
                color = colors.textPrimary.copy(alpha = 0.45f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = formaPagamento.label,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = "Alterar",
            color = Verde,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onAlterar() }
        )
    }
}

// ═══════════════════════════════════════════════════════
// RESUMO DE VALORES
// ═══════════════════════════════════════════════════════
@Composable
private fun ResumoValores(subtotal: Double) {
    val colors = LocalPedidosColors.current
    // Entrega grátis por enquanto (pode receber como parâmetro futuramente)
    val taxaEntrega = 0.0
    val total = subtotal + taxaEntrega

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Subtotal",
                color = colors.textPrimary.copy(alpha = 0.65f),
                fontSize = 14.sp
            )
            Text(
                text = "R$ ${String.format("%.2f", subtotal).replace(".", ",")}",
                color = colors.textPrimary.copy(alpha = 0.65f),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Entrega",
                color = colors.textPrimary.copy(alpha = 0.65f),
                fontSize = 14.sp
            )
            Text(
                text = if (taxaEntrega <= 0.0) "Grátis" else "R$ ${String.format("%.2f", taxaEntrega)}",
                color = if (taxaEntrega <= 0.0) Verde else colors.textPrimary.copy(alpha = 0.65f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        HorizontalDivider(color = colors.textPrimary.copy(alpha = 0.08f))

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total",
                color = colors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "R$ ${String.format("%.2f", total).replace(".", ",")}",
                color = colors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// BOTÃO FINALIZAR PEDIDO
// ═══════════════════════════════════════════════════════
@Composable
private fun BotaoFinalizar(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalPedidosColors.current.background)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Verde),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = "FINALIZAR PEDIDO",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                letterSpacing = 1.sp
            )
        }
    }
}
