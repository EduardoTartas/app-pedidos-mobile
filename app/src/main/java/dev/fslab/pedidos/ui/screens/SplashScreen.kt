package dev.fslab.pedidos.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.fslab.pedidos.R
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import dev.fslab.pedidos.ui.viewmodel.AuthState
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authState: AuthState,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit
) {
    val colors = LocalPedidosColors.current
    var startAnimation by remember { mutableStateOf(false) }
    
    // Animação de entrada (deslizar e escala)
    val offsetY by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 100.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "offsetY"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "alpha"
    )

    // Lógica de Redirecionamento - Mais agressiva para evitar travamentos
    LaunchedEffect(Unit) {
        startAnimation = true
    }

    LaunchedEffect(authState) {
        // Se já terminamos a animação inicial e o estado não é mais Loading, navegamos
        if (authState !is AuthState.Loading) {
            // Delay para verificar login e evitar travamentos para após as verificações de login, ir para a home ou login
            delay(600)
            when (authState) {
                is AuthState.Success -> onNavigateToHome()
                is AuthState.Idle, is AuthState.Error -> onNavigateToLogin()
                is AuthState.NeedsProfileCompletion -> onNavigateToCompleteProfile()
                else -> {}
            }
        }
    }

    // Interface da Splash - Seguindo o estilo das outras telas
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(y = offsetY)
                .scale(scale)
                .alpha(alpha)
        ) {
            // Contêiner Quadrado Arredondado (estilo Login)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iguana_icon),
                    contentDescription = "Logo",
                    modifier = Modifier.size(55.dp) // Logo um pouco menor
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Texto do App (estilo Login)
            Text(
                text = "RanGo",
                style = MaterialTheme.typography.displaySmall,
                color = colors.textPrimary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
    }
}
