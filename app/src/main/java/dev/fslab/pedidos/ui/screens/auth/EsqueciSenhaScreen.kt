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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import dev.fslab.pedidos.ui.theme.PedidosTheme

private enum class RecuperarSenhaStep(val title: String, val subtitle: String) {
    EMAIL("Recuperar Senha", "Informe seu e-mail para receber o código."),
    CODIGO("Redefinir Senha", "Digite o código recebido e a nova senha."),
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
    onResetPassword: (token: String, novaSenha: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _, _ -> }
) {
    val colors = LocalPedidosColors.current

    var currentStep by remember { mutableStateOf(if (!tokenInicial.isNullOrBlank()) RecuperarSenhaStep.CODIGO else RecuperarSenhaStep.EMAIL) }
    var email by remember { mutableStateOf(emailInicial) }
    var codigo by remember { mutableStateOf(tokenInicial ?: "") }
    var novaSenha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var mostrarSenha by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var mostraErro by remember { mutableStateOf(false) }
    var mensagemErro by remember { mutableStateOf("") }

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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
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
                            .background(colors.primary.copy(alpha = 0.1f))
                            .clickable {
                                when (currentStep) {
                                    RecuperarSenhaStep.EMAIL -> onBackToLogin()
                                    RecuperarSenhaStep.CODIGO -> { currentStep = RecuperarSenhaStep.EMAIL; mostraErro = false }
                                    RecuperarSenhaStep.SUCESSO -> onBackToLogin()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(20.dp)
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
                    Box(modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape).background(if (currentStep.ordinal >= 1) colors.primary else colors.inputBorder))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape).background(if (currentStep.ordinal >= 2) colors.primary else colors.inputBorder))
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                        if (currentStep != RecuperarSenhaStep.SUCESSO) {
                            Text(
                                text = currentStep.title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = if (currentStep == RecuperarSenhaStep.CODIGO && !tokenInicial.isNullOrBlank()) "Crie uma nova senha para sua conta." else currentStep.subtitle,
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
                                RecuperarSenhaStep.CODIGO -> {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        if (tokenInicial.isNullOrBlank()) {
                                            Text(text = "Código de Verificação", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary, modifier = Modifier.padding(bottom = 6.dp))
                                            OutlinedTextField(
                                                value = codigo, onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) { codigo = it; mostraErro = false } },
                                                placeholder = { Text("000000", fontSize = 18.sp, color = colors.mediumGray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                                textStyle = TextStyle(color = colors.textInput, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, letterSpacing = 8.sp),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    unfocusedBorderColor = colors.inputBorder, focusedBorderColor = colors.primary, cursorColor = colors.primary,
                                                    focusedTextColor = colors.textInput, unfocusedTextColor = colors.textInput,
                                                    unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent
                                                ),
                                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                                shape = RoundedCornerShape(16.dp), singleLine = true, enabled = !isLoading
                                            )
                                        }
                                        
                                        CustomOutlinedField(
                                            value = novaSenha, label = "Nova Senha", icon = Icons.Default.Lock, placeholder = "Mínimo 8 caracteres",
                                            keyboardType = KeyboardType.Password, isPassword = true,
                                            passwordVisible = mostrarSenha, onVisibilityChange = { mostrarSenha = it },
                                            onValueChange = { novaSenha = it; mostraErro = false }
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        CustomOutlinedField(
                                            value = confirmarSenha, label = "Confirmar Nova Senha", icon = Icons.Default.Lock, placeholder = "Digite novamente",
                                            keyboardType = KeyboardType.Password, isPassword = true,
                                            passwordVisible = mostrarSenha, onVisibilityChange = { mostrarSenha = it },
                                            onValueChange = { confirmarSenha = it; mostraErro = false }
                                        )
                                    }
                                }
                                RecuperarSenhaStep.SUCESSO -> {
                                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Surface(
                                            modifier = Modifier.size(64.dp).padding(bottom = 16.dp),
                                            shape = RoundedCornerShape(32.dp),
                                            color = colors.successBackground
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                Text(text = "✓", fontSize = 32.sp, color = colors.successText)
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
                                            onRecoverPassword(email.trim(), { isLoading = false; currentStep = RecuperarSenhaStep.CODIGO }, { error -> isLoading = false; mostraErro = true; mensagemErro = error })
                                        }
                                    }
                                    RecuperarSenhaStep.CODIGO -> {
                                        if (codigo.isBlank()) { mostraErro = true; mensagemErro = "O token ou código é inválido" }
                                        else if (novaSenha.length < 8) { mostraErro = true; mensagemErro = "A senha deve ter pelo menos 8 caracteres" }
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
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colors.textOnPrimary, strokeWidth = 2.dp)
                            } else {
                                Text(
                                    text = when (currentStep) {
                                        RecuperarSenhaStep.EMAIL -> "Enviar Código"
                                        RecuperarSenhaStep.CODIGO -> "Redefinir Senha"
                                        RecuperarSenhaStep.SUCESSO -> "Fazer Login"
                                    },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textOnPrimary
                                )
                            }
                        }

                        if (currentStep == RecuperarSenhaStep.CODIGO) {
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(onClick = {
                                currentStep = RecuperarSenhaStep.EMAIL
                                codigo = ""
                                novaSenha = ""
                                confirmarSenha = ""
                                mostraErro = false
                            }) {
                                Text(text = "Não recebeu? Voltar", fontSize = 13.sp, color = colors.primary)
                            }
                        }
                    }
                }

                // Link voltar ao login
                if (currentStep != RecuperarSenhaStep.SUCESSO) {
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
