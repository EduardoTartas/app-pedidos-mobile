package dev.fslab.pedidos.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Verde = Color(0xFF14B822)

/**
 * Dialog para conflito de restaurante.
 * Aparece quando o usuário tenta adicionar item de um restaurante
 * diferente do que já está no carrinho.
 */
@Composable
fun ConflitoRestauranteDialog(
    nomeRestauranteAtual: String,
    nomeRestauranteNovo: String,
    onSubstituir: () -> Unit,
    onCancelar: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val containerColor = if (isDark) Color(0xFF1A202C) else Color.White
    val titleColor = if (isDark) Color.White else Color(0xFF111827)
    val textColor = if (isDark) Color.White.copy(0.7f) else Color(0xFF374151)

    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = containerColor,
        shape = RoundedCornerShape(20.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Verde,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Carrinho ativo",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = titleColor
            )
        },
        text = {
            Text(
                text = "Você já tem itens de \"$nomeRestauranteAtual\" no carrinho. Deseja esvaziar o carrinho para adicionar itens de \"$nomeRestauranteNovo\"?",
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = textColor
            )
        },
        confirmButton = {
            Button(
                onClick = onSubstituir,
                colors = ButtonDefaults.buttonColors(containerColor = Verde),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Substituir carrinho",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(
                    text = "Cancelar",
                    color = Verde,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

