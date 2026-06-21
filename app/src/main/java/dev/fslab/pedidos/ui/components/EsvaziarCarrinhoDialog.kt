package dev.fslab.pedidos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RemoveShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.fslab.pedidos.ui.theme.LocalPedidosColors

/**
 * Dialog para confirmar o esvaziamento do carrinho.
 * Aparece quando o usuário tenta remover o último item do carrinho.
 */
@Composable
fun EsvaziarCarrinhoDialog(
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    val colors = LocalPedidosColors.current

    Dialog(
        onDismissRequest = onCancelar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(32.dp),
            color = colors.background,
            tonalElevation = 0.dp,
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ícone no topo com fundo vermelho (erro/destrutivo)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(colors.errorBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RemoveShoppingCart,
                        contentDescription = null,
                        tint = colors.error,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Esvaziar carrinho?",
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Ao remover este item, seu carrinho ficará vazio e você perderá o pedido atual.\n\nDeseja mesmo remover?",
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Botões
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onConfirmar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.errorButton),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Sim, esvaziar",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = colors.textOnPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = onCancelar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.background,
                            contentColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = "Cancelar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
