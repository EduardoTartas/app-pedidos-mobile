package dev.fslab.pedidos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fslab.pedidos.model.PedidoCriado
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import kotlinx.coroutines.delay

private val Verde = Color(0xFF14B822)
private val VerdeEscuro = Color(0xFF0E8A19)

// ═══════════════════════════════════════════════════════
// TELA DE CONFIRMAÇÃO DE PEDIDO
// ═══════════════════════════════════════════════════════
@Composable
fun PedidoConfirmacaoScreen(
    pedido: PedidoCriado,
    nomeRestaurante: String,
    onVoltarInicio: () -> Unit
) {
    val colors = LocalPedidosColors.current

    // Animações de entrada
    var showContent by remember { mutableStateOf(false) }
    val checkScale = remember { Animatable(0f) }
    val pulseScale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        delay(100)
        checkScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        showContent = true

        // Pulso contínuo sutil
        while (true) {
            pulseScale.animateTo(
                1.08f,
                animationSpec = tween(900, easing = FastOutSlowInEasing)
            )
            pulseScale.animateTo(
                1f,
                animationSpec = tween(900, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Ícone de check ──────────────────────────────
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(pulseScale.value),
                contentAlignment = Alignment.Center
            ) {
                // Halo externo
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Verde.copy(alpha = 0.15f))
                )
                // Círculo principal
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Verde, VerdeEscuro)
                            )
                        )
                        .scale(checkScale.value),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Título ──────────────────────────────────────
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500)) + scaleIn(tween(500))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Pedido realizado!",
                        color = colors.textPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Seu pedido foi enviado para\n$nomeRestaurante",
                        color = colors.textPrimary.copy(alpha = 0.6f),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Card com resumo ─────────────────────────────
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600, delayMillis = 100))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {

                        // ID do pedido
                        ResumoRow(
                            label = "Nº do pedido",
                            value = "#${pedido.id.takeLast(8).uppercase()}",
                            valueColor = Verde,
                            bold = true
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = colors.textPrimary.copy(alpha = 0.07f)
                        )

                        // Status
                        ResumoRow(
                            label = "Status",
                            value = "Criado ✓",
                            valueColor = Verde
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = colors.textPrimary.copy(alpha = 0.07f)
                        )

                        // Endereço de entrega
                        pedido.enderecoEntrega?.let { end ->
                            ResumoRow(
                                label = "Entregar em",
                                value = "${end.logradouro}, ${end.numero} - ${end.bairro}"
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            ResumoRow(
                                label = "",
                                value = "${end.cidade} - ${end.estado}"
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = colors.textPrimary.copy(alpha = 0.07f)
                            )
                        }

                        // Forma de pagamento
                        pedido.formaPagamento?.let { fp ->
                            val formaPagamentoLabel = when (fp) {
                                "cartao_credito" -> "Cartão de Crédito"
                                "cartao_debito"  -> "Cartão de Débito"
                                "pix"            -> "Pix"
                                "dinheiro"       -> "Dinheiro"
                                else             -> fp
                            }
                            ResumoRow(
                                label = "Pagamento",
                                value = formaPagamentoLabel
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = colors.textPrimary.copy(alpha = 0.07f)
                            )
                        }
                        pedido.itens.forEach { item ->
                            ResumoRow(
                                label = "${item.quantidade}x ${item.pratoNome}",
                                value = "R$ ${
                                    String.format(
                                        "%.2f",
                                        (item.precoUnitario * item.quantidade) +
                                                item.adicionais.sumOf { it.precoUnitario * it.quantidade }
                                    ).replace(".", ",")
                                }"
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = colors.textPrimary.copy(alpha = 0.07f)
                        )

                        // Taxa de entrega
                        if (pedido.totais.taxaEntrega > 0) {
                            ResumoRow(
                                label = "Taxa de entrega",
                                value = "R$ ${
                                    String.format("%.2f", pedido.totais.taxaEntrega).replace(".", ",")
                                }"
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        } else {
                            ResumoRow(
                                label = "Entrega",
                                value = "Grátis",
                                valueColor = Verde
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = colors.textPrimary.copy(alpha = 0.07f)
                        )

                        // Total
                        ResumoRow(
                            label = "Total pago",
                            value = "R$ ${
                                String.format("%.2f", pedido.totais.total).replace(".", ",")
                            }",
                            bold = true,
                            labelColor = colors.textPrimary,
                            valueColor = colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Botão voltar ao início ──────────────────────
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(700, delayMillis = 200))
            ) {
                Button(
                    onClick = onVoltarInicio,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Verde),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = "VOLTAR AO INÍCIO",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

// ── Helper composable ────────────────────────────────────
@Composable
private fun ResumoRow(
    label: String,
    value: String,
    bold: Boolean = false,
    labelColor: Color = LocalPedidosColors.current.textPrimary.copy(alpha = 0.6f),
    valueColor: Color = LocalPedidosColors.current.textPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = labelColor,
            fontSize = 14.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.SemiBold
        )
    }
}
