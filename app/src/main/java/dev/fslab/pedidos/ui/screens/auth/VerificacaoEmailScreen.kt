package dev.fslab.pedidos.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fslab.pedidos.ui.theme.LocalPedidosColors

@Composable
fun VerificacaoEmailScreen(
    token: String,
    onNavigateToLogin: () -> Unit,
    onVerifyEmail: (token: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit
) {
    val colors = LocalPedidosColors.current

    var isLoading by remember { mutableStateOf(true) }
    var success by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(token) {
        onVerifyEmail(
            token,
            {
                isLoading = false
                success = true
            },
            { error ->
                isLoading = false
                errorMessage = error
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = colors.primary)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Verificando seu e-mail...",
                        fontSize = 16.sp,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                } else if (success) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = colors.successBackground
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("✓", fontSize = 40.sp, color = colors.successText, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "E-mail Verificado!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sua conta foi ativada com sucesso. Você já pode acessar o aplicativo.",
                        fontSize = 14.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onNavigateToLogin,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text("Ir para o Login", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textOnPrimary)
                    }
                } else {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = colors.errorBackground
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("✕", fontSize = 40.sp, color = colors.errorText, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Erro na Verificação",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "O link é inválido ou já expirou.",
                        fontSize = 14.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onNavigateToLogin,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text("Voltar ao Login", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textOnPrimary)
                    }
                }
            }
        }
    }
}
