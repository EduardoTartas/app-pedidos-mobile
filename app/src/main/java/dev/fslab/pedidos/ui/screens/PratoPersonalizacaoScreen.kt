package dev.fslab.pedidos.ui.screens

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.fslab.pedidos.model.AdicionalOpcao
import dev.fslab.pedidos.ui.viewmodel.GrupoComOpcoes
import dev.fslab.pedidos.ui.viewmodel.PersonalizacaoUiState
import dev.fslab.pedidos.ui.viewmodel.PratoPersonalizacaoViewModel

private val Verde = Color(0xFF14B822)

@Composable
fun PratoPersonalizacaoScreen(
    onBack: () -> Unit,
    onAdicionarAoCarrinho: (dev.fslab.pedidos.ui.viewmodel.PersonalizacaoUiState.Success) -> Unit,
    viewModel: PratoPersonalizacaoViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val isLight = !isSystemInDarkTheme()
    val bgColor   = if (isLight) Color(0xFFF8F9FA) else Color(0xFF0D1117)
    val cardColor = if (isLight) Color(0xFFECEFF1) else Color(0xFF161B2E)
    val textColor = if (isLight) Color(0xFF111827) else Color.White
    val subText   = textColor.copy(alpha = 0.55f)

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        when (val state = uiState) {

            // ── LOADING / IDLE ──────────────────────────────
            is PersonalizacaoUiState.Loading,
            is PersonalizacaoUiState.Idle -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Verde
                )
            }

            // ── ERRO ────────────────────────────────────────
            is PersonalizacaoUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.message, color = subText, fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Verde)
                    ) { Text("Voltar") }
                }
            }

            // ── SUCESSO ─────────────────────────────────────
            is PersonalizacaoUiState.Success -> {
                val precoTotal = viewModel.precoTotal()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 110.dp)
                ) {

                    // Foto
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(270.dp)
                        ) {
                            AsyncImage(
                                model = state.prato.fotoPrato?.takeIf { it.isNotBlank() }
                                    ?: "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=800&auto=format&fit=crop",
                                contentDescription = state.prato.nome,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Gradiente para o fundo
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                Color.Transparent,
                                                bgColor.copy(alpha = 0.9f)
                                            )
                                        )
                                    )
                            )
                            // Botão fechar
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .statusBarsPadding()
                                    .padding(14.dp)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.55f))
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Voltar",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Info do prato
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = state.prato.nome,
                                color = textColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp
                            )
                            if (!state.prato.descricao.isNullOrBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = state.prato.descricao,
                                    color = subText,
                                    fontSize = 14.sp,
                                    lineHeight = 21.sp
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "A partir de R$ ${String.format("%.2f", state.prato.preco)}",
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    // Grupos de adicionais
                    state.grupos.forEach { gc ->
                        val selectedCount = state.selecoes[gc.grupo.id]?.size ?: 0

                        // Header do grupo com contador reativo
                        item {
                            PersonalizacaoGrupoHeader(
                                gc = gc,
                                selectedCount = selectedCount,
                                cardColor = cardColor,
                                textColor = textColor,
                                subText = subText
                            )
                        }

                        // Opções
                        items(gc.opcoes) { opcao ->
                            val selecionado =
                                state.selecoes[gc.grupo.id]?.contains(opcao.id) == true
                            PersonalizacaoOpcaoRow(
                                opcao = opcao,
                                isRadio = gc.grupo.max == 1,
                                selecionado = selecionado,
                                textColor = textColor,
                                subText = subText,
                                onClick = {
                                    viewModel.selecionar(gc.grupo.id, opcao.id, gc.grupo.max)
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                color = textColor.copy(alpha = 0.06f)
                            )
                        }

                        item { Spacer(Modifier.height(12.dp)) }
                    }
                }

                // Botão fixo no rodapé
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(Color.Transparent, bgColor, bgColor))
                        )
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = {
                            onAdicionarAoCarrinho(state)
                            viewModel.resetar()
                            onBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Verde)
                    ) {
                        Text(
                            "Adicionar  •  R$ ${String.format("%.2f", precoTotal)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// HEADER DO GRUPO — com contador dinâmico
// ═══════════════════════════════════════════
@Composable
private fun PersonalizacaoGrupoHeader(
    gc: GrupoComOpcoes,
    selectedCount: Int,
    cardColor: Color,
    textColor: Color,
    subText: Color
) {
    val completo = selectedCount >= gc.grupo.min && gc.grupo.min > 0
    val badgeBg  = if (completo) Verde.copy(alpha = 0.15f) else textColor.copy(alpha = 0.08f)
    val badgeText = if (completo) Verde else textColor.copy(alpha = 0.6f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardColor)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = gc.grupo.nome,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (gc.grupo.obrigatorio) "OBRIGATÓRIO" else "OPCIONAL",
                color = if (gc.grupo.obrigatorio) Color(0xFFEF4444) else subText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Contador: selectedCount/max  (ex: 0/3, 1/3, 1/1)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(badgeBg)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "$selectedCount/${gc.grupo.max}",
                color = badgeText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ═══════════════════════════════════════════
// LINHA DE OPÇÃO
// ═══════════════════════════════════════════
@Composable
private fun PersonalizacaoOpcaoRow(
    opcao: AdicionalOpcao,
    isRadio: Boolean,
    selecionado: Boolean,
    textColor: Color,
    subText: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = opcao.nome,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            if (opcao.preco > 0.0) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "+ R$ ${String.format("%.2f", opcao.preco)}",
                    color = subText,
                    fontSize = 13.sp
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        if (isRadio) RadioBall(selecionado) else CheckBall(selecionado)
    }
}

@Composable
private fun RadioBall(selecionado: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (selecionado) Verde else Color.Transparent)
            .border(2.dp, if (selecionado) Verde else Color.Gray.copy(0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (selecionado) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

@Composable
private fun CheckBall(selecionado: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (selecionado) Verde else Color.Transparent)
            .border(2.dp, if (selecionado) Verde else Color.Gray.copy(0.4f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (selecionado) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
