package dev.fslab.pedidos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import dev.fslab.pedidos.model.Pedido
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import kotlinx.coroutines.delay

private val Verde = Color(0xFF14B822)
private val VerdeEscuro = Color(0xFF0E8A19)

@Composable
fun PedidoConfirmacaoScreen(
    pedido: Pedido,
    nomeRestaurante: String,
    onVoltarInicio: () -> Unit,
    onAcompanharPedido: () -> Unit = {}
) {
    val colors = LocalPedidosColors.current

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

        while (true) {
            pulseScale.animateTo(1.08f, animationSpec = tween(900, easing = FastOutSlowInEasing))
            pulseScale.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
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
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(pulseScale.value),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Verde.copy(alpha = 0.15f)))
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(colors = listOf(Verde, VerdeEscuro)))
                        .scale(checkScale.value),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            AnimatedVisibility(visible = showContent, enter = fadeIn(tween(500)) + scaleIn(tween(500))) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Pedido realizado!", color = colors.textPrimary, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Seu pedido foi enviado para\n$nomeRestaurante", color = colors.textPrimary.copy(alpha = 0.6f), fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(visible = showContent, enter = fadeIn(tween(600, delayMillis = 100))) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        ResumoRow("Nº do pedido", "#${pedido.id.takeLast(8).uppercase()}", colors, valueColor = Verde, bold = true)
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colors.textPrimary.copy(alpha = 0.07f))
                        
                        ResumoRow("Status", "Criado ✓", colors, valueColor = Verde)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colors.textPrimary.copy(alpha = 0.07f))

                        pedido.itens.forEach { item ->
                            ResumoRow(
                                label = "${item.quantidade}x ${item.pratoNome}",
                                value = "R$ ${String.format("%.2f", (item.precoUnitario * item.quantidade) + item.adicionais.sumOf { it.precoUnitario * it.quantidade })}",
                                colors = colors
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colors.textPrimary.copy(alpha = 0.07f))

                        if (pedido.totais.taxaEntrega > 0) {
                            ResumoRow("Taxa de entrega", "R$ ${String.format("%.2f", pedido.totais.taxaEntrega)}", colors)
                        } else {
                            ResumoRow("Entrega", "Grátis", colors, valueColor = Verde)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colors.textPrimary.copy(alpha = 0.07f))

                        ResumoRow("Total pago", "R$ ${String.format("%.2f", pedido.totais.total)}", colors, bold = true, labelColor = colors.textPrimary, valueColor = colors.textPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(visible = showContent, enter = fadeIn(tween(700, delayMillis = 200))) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onAcompanharPedido,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Verde)
                    ) {
                        Text("ACOMPANHAR PEDIDO", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, letterSpacing = 1.sp)
                    }

                    OutlinedButton(
                        onClick = onVoltarInicio,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Verde),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Verde)
                    ) {
                        Text("VOLTAR AO INÍCIO", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumoRow(
    label: String,
    value: String,
    colors: dev.fslab.pedidos.ui.theme.PedidosColors,
    bold: Boolean = false,
    labelColor: Color? = null,
    valueColor: Color? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = labelColor ?: colors.textPrimary.copy(alpha = 0.6f),
            fontSize = 14.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = valueColor ?: colors.textPrimary,
            fontSize = 14.sp,
            fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.SemiBold
        )
    }
}
