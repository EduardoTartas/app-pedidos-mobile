package dev.fslab.pedidos.ui.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import dev.fslab.pedidos.ui.theme.PedidosTheme

private enum class RecuperarSenhaStep(val title: String, val subtitle: String) {
    EMAIL("Recuperar Senha", "Informe seu e-mail para receber o link de recuperação."),
    LINK_ENVIADO("E-mail Enviado", "Verifique sua caixa de entrada para continuar."),
    NOVA_SENHA("Redefinir Senha", "Crie uma nova senha para sua conta."),
    SUCESSO("Tudo pronto!", "Sua senha foi redefinida com sucesso.")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EsqueciSenhaScreen(
    modifier: Modifier = Modifier,
    emailInicial: String = "",
    tokenInicial: String? = null,
    onBackToLogin: () -> Unit = {},
    onRecoverPassword: (email: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _ -> },
    onResetPassword: (token: String, novaSenha: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _, _ -> },
    onValidateToken: (token: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _ -> }
) {
    val colors = LocalPedidosColors.current

    var currentStep by remember { mutableStateOf(if (!tokenInicial.isNullOrBlank()) RecuperarSenhaStep.NOVA_SENHA else RecuperarSenhaStep.EMAIL) }
    var email by remember { mutableStateOf(emailInicial) }
    val codigo = tokenInicial ?: ""
    var novaSenha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var mostrarSenha by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var mostraErro by remember { mutableStateOf(false) }
    var mensagemErro by remember { mutableStateOf("") }
    var isTokenValidating by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    androidx.compose.runtime.LaunchedEffect(tokenInicial) {
        if (!tokenInicial.isNullOrBlank()) {
            isTokenValidating = true
            onValidateToken(
                tokenInicial,
                {
                    isTokenValidating = false
                    currentStep = RecuperarSenhaStep.NOVA_SENHA
                },
                { error ->
                    isTokenValidating = false
                    mostraErro = true
                    mensagemErro = error
                    currentStep = RecuperarSenhaStep.EMAIL
                }
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .heightIn(min = screenHeight - 64.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header no topo da tela
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Botão voltar + Título
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable {
                                when (currentStep) {
                                    RecuperarSenhaStep.EMAIL -> onBackToLogin()
                                    RecuperarSenhaStep.LINK_ENVIADO -> { currentStep = RecuperarSenhaStep.EMAIL; mostraErro = false }
                                    RecuperarSenhaStep.NOVA_SENHA -> onBackToLogin()
                                    RecuperarSenhaStep.SUCESSO -> onBackToLogin()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Recuperação",
                        color = colors.textPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-1).sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape).background(if (currentStep.ordinal >= 0) colors.primary else colors.inputBorder))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape).background(if (currentStep.ordinal >= 2) colors.primary else colors.inputBorder))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape).background(if (currentStep.ordinal >= 3) colors.primary else colors.inputBorder))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Card Central
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header do Passo
                        if (currentStep != RecuperarSenhaStep.SUCESSO && currentStep != RecuperarSenhaStep.LINK_ENVIADO) {
                            Text(
                                text = currentStep.title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = currentStep.subtitle,
                                fontSize = 13.sp,
                                color = colors.textSecondary,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                            )
                        }

                        // Erros
                        if (mostraErro) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = colors.errorBackground
                            ) {
                                Text(
                                    text = mensagemErro,
                                    fontSize = 12.sp,
                                    color = colors.errorText,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // Animacao Transicao
                        AnimatedContent(
                            targetState = currentStep,
                            transitionSpec = {
                                if (targetState.ordinal > initialState.ordinal) {
                                    slideInHorizontally(animationSpec = tween(300)) { it } with slideOutHorizontally(animationSpec = tween(300)) { -it }
                                } else {
                                    slideInHorizontally(animationSpec = tween(300)) { -it } with slideOutHorizontally(animationSpec = tween(300)) { it }
                                }
                            },
                            label = "Form Transition"
                        ) { step ->
                            when (step) {
                                RecuperarSenhaStep.EMAIL -> {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        CustomOutlinedField(
                                            value = email, label = "E-mail", icon = Icons.Default.Email, placeholder = "seu@email.com",
                                            keyboardType = KeyboardType.Email,
                                            onValueChange = { email = it; mostraErro = false }
                                        )
                                    }
                                }
                                RecuperarSenhaStep.LINK_ENVIADO -> {
                                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Surface(
                                            modifier = Modifier.size(64.dp).padding(bottom = 16.dp),
                                            shape = RoundedCornerShape(32.dp),
                                            color = colors.primary.copy(alpha = 0.1f)
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = colors.primary, modifier = Modifier.size(32.dp))
                                            }
                                        }
                                        Text(text = "Link a caminho!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.padding(bottom = 8.dp))
                                        Text(text = "Enviamos um link de recuperação para:\n$email", fontSize = 13.sp, color = colors.textSecondary, textAlign = TextAlign.Center)
                                    }
                                }
                                RecuperarSenhaStep.NOVA_SENHA -> {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        if (isTokenValidating) {
                                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                                CircularProgressIndicator(color = colors.primary)
                                            }
                                        } else {
                                            val hasMinLen = novaSenha.length >= 8
                                            val hasUpper = novaSenha.any { it.isUpperCase() }
                                            val hasLower = novaSenha.any { it.isLowerCase() }
                                            val hasNumber = novaSenha.any { it.isDigit() }
                                            val hasSpecial = novaSenha.any { !it.isLetterOrDigit() }

                                            CustomOutlinedField(
                                                value = novaSenha, label = "Nova Senha", icon = Icons.Default.Lock, placeholder = "Mínimo 8 caracteres",
                                                keyboardType = KeyboardType.Password, isPassword = true,
                                                passwordVisible = mostrarSenha, onVisibilityChange = { mostrarSenha = it },
                                                onValueChange = { novaSenha = it; mostraErro = false }
                                            )
                                            
                                            Box(modifier = Modifier.padding(bottom = 8.dp)) {
                                                PasswordStrengthIndicator(
                                                    hasMinLen = hasMinLen,
                                                    hasUpper = hasUpper,
                                                    hasLower = hasLower,
                                                    hasNumber = hasNumber,
                                                    hasSpecial = hasSpecial
                                                )
                                            }

                                            CustomOutlinedField(
                                                value = confirmarSenha, label = "Confirmar Nova Senha", icon = Icons.Default.Lock, placeholder = "Digite novamente",
                                                keyboardType = KeyboardType.Password, isPassword = true,
                                                passwordVisible = mostrarSenha, onVisibilityChange = { mostrarSenha = it },
                                                onValueChange = { confirmarSenha = it; mostraErro = false }
                                            )
                                        }
                                    }
                                }
                                RecuperarSenhaStep.SUCESSO -> {
                                    val checkScale = remember { androidx.compose.animation.core.Animatable(0f) }

                                    androidx.compose.runtime.LaunchedEffect(Unit) {
                                        kotlinx.coroutines.delay(100)
                                        checkScale.animateTo(
                                            targetValue = 1f,
                                            animationSpec = androidx.compose.animation.core.spring(
                                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                                stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                                            )
                                        )
                                    }

                                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .padding(bottom = 16.dp)
                                                .size(120.dp)
                                                .scale(checkScale.value),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFF14B822).copy(alpha = 0.15f)))
                                            Box(
                                                modifier = Modifier
                                                    .size(90.dp)
                                                    .clip(CircleShape)
                                                    .background(Brush.radialGradient(colors = listOf(Color(0xFF14B822), Color(0xFF0E8A19)))),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                                            }
                                        }
                                        Text(text = "Senha redefinida!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.padding(bottom = 8.dp))
                                        Text(text = "Sua senha foi alterada com sucesso. Agora você pode fazer login.", fontSize = 13.sp, color = colors.textSecondary, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Botão Ação
                        Button(
                            onClick = {
                                when (currentStep) {
                                    RecuperarSenhaStep.EMAIL -> {
                                        if (email.isBlank() || !email.contains("@")) { mostraErro = true; mensagemErro = "Digite um e-mail válido" }
                                        else {
                                            mostraErro = false
                                            isLoading = true
                                            onRecoverPassword(email.trim(), { isLoading = false; currentStep = RecuperarSenhaStep.LINK_ENVIADO }, { error -> isLoading = false; mostraErro = true; mensagemErro = error })
                                        }
                                    }
                                    RecuperarSenhaStep.LINK_ENVIADO -> {
                                        onBackToLogin()
                                    }
                                    RecuperarSenhaStep.NOVA_SENHA -> {
                                        val hasMinLen = novaSenha.length >= 8
                                        val hasUpper = novaSenha.any { it.isUpperCase() }
                                        val hasLower = novaSenha.any { it.isLowerCase() }
                                        val hasNumber = novaSenha.any { it.isDigit() }
                                        val hasSpecial = novaSenha.any { !it.isLetterOrDigit() }

                                        if (codigo.isBlank()) { mostraErro = true; mensagemErro = "O token ou código é inválido" }
                                        else if (!hasMinLen || !hasUpper || !hasLower || !hasNumber || !hasSpecial) { mostraErro = true; mensagemErro = "A senha não cumpre os requisitos exigidos" }
                                        else if (novaSenha != confirmarSenha) { mostraErro = true; mensagemErro = "As senhas não coincidem" }
                                        else {
                                            mostraErro = false
                                            isLoading = true
                                            onResetPassword(codigo.trim(), novaSenha, { isLoading = false; currentStep = RecuperarSenhaStep.SUCESSO }, { error -> isLoading = false; mostraErro = true; mensagemErro = error })
                                        }
                                    }
                                    RecuperarSenhaStep.SUCESSO -> onBackToLogin()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isLoading && !isTokenValidating
                        ) {
                            if (isLoading || isTokenValidating) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colors.textOnPrimary, strokeWidth = 2.dp)
                            } else {
                                Text(
                                    text = when (currentStep) {
                                        RecuperarSenhaStep.EMAIL -> "Enviar Link de Recuperação"
                                        RecuperarSenhaStep.LINK_ENVIADO -> "Voltar ao Login"
                                        RecuperarSenhaStep.NOVA_SENHA -> "Redefinir Senha"
                                        RecuperarSenhaStep.SUCESSO -> "Fazer Login"
                                    },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textOnPrimary
                                )
                            }
                        }

                        if (currentStep == RecuperarSenhaStep.LINK_ENVIADO) {
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(onClick = {
                                currentStep = RecuperarSenhaStep.EMAIL
                                mostraErro = false
                            }) {
                                Text(text = "Não recebeu? Tentar novamente", fontSize = 13.sp, color = colors.primary)
                            }
                        }
                    }
                }

                // Link voltar ao login
                if (currentStep != RecuperarSenhaStep.SUCESSO && currentStep != RecuperarSenhaStep.LINK_ENVIADO) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp)
                    ) {
                        Text(text = "Lembrou da senha? ", fontSize = 14.sp, color = colors.textSecondary)
                        Text(
                            text = "Faça login",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            modifier = Modifier.clickable { onBackToLogin() }
                        )
                    }
                }
            }

            // Footer
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "© 2026 RanGo. Todos os direitos reservados.", fontSize = 11.sp, color = colors.textSecondary)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EsqueciSenhaScreenPreview() {
    PedidosTheme {
        EsqueciSenhaScreen()
    }
}
