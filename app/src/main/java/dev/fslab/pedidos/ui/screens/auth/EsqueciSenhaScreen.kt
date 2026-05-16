package dev.fslab.pedidos.ui.screens.auth

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
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.TextStyle
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
import dev.fslab.pedidos.ui.theme.PedidosColors

/**
 * Estados da tela de recuperação de senha
 */
private enum class RecuperarSenhaStep {
    EMAIL,      // Etapa 1: Digitar e-mail
    CODIGO,     // Etapa 2: Digitar código de 6 dígitos + nova senha
    SUCESSO     // Etapa 3: Senha alterada com sucesso
}

/**
 * EsqueciSenhaScreen - Tela de recuperação de senha
 *
 * Fluxo:
 * 1. Usuário digita e-mail → POST /recover (envia e-mail com código de 6 dígitos)
 * 2. Usuário recebe código por e-mail
 * 3. Usuário digita código + nova senha → PATCH /password/reset?token=CODIGO
 * 4. Sucesso → Volta ao login
 */
@Composable
fun EsqueciSenhaScreen(
    modifier: Modifier = Modifier,
    emailInicial: String = "",
    onBackToLogin: () -> Unit = {},
    onRecoverPassword: (email: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _ -> },
    onResetPassword: (token: String, novaSenha: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _, _ -> }
) {
    val colors = LocalPedidosColors.current

    // Estados
    var currentStep by remember { mutableStateOf(RecuperarSenhaStep.EMAIL) }
    var email by remember { mutableStateOf(emailInicial) }
    var codigo by remember { mutableStateOf("") }
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header com botão voltar
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
                                if (currentStep == RecuperarSenhaStep.CODIGO) {
                                    currentStep = RecuperarSenhaStep.EMAIL
                                    codigo = ""
                                    novaSenha = ""
                                    confirmarSenha = ""
                                    mostraErro = false
                                } else {
                                    onBackToLogin()
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
                        text = when (currentStep) {
                            RecuperarSenhaStep.EMAIL -> "Recuperar Senha"
                            RecuperarSenhaStep.CODIGO -> "Redefinir Senha"
                            RecuperarSenhaStep.SUCESSO -> "Senha Redefinida"
                        },
                        color = colors.textPrimary,
                        style = MaterialTheme.typography.displaySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ícone
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (currentStep) {
                            RecuperarSenhaStep.EMAIL -> Icons.Default.Email
                            RecuperarSenhaStep.CODIGO -> Icons.Default.Lock
                            RecuperarSenhaStep.SUCESSO -> Icons.Filled.LockReset
                        },
                        contentDescription = null,
                        tint = colors.textOnPrimary,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Subtítulo
                Text(
                    text = when (currentStep) {
                        RecuperarSenhaStep.EMAIL -> "Informe seu e-mail para receber o código"
                        RecuperarSenhaStep.CODIGO -> "Digite o código recebido por e-mail e sua nova senha"
                        RecuperarSenhaStep.SUCESSO -> "Sua senha foi alterada com sucesso!"
                    },
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )

                // Conteúdo baseado na etapa
                when (currentStep) {
                    RecuperarSenhaStep.EMAIL -> {
                        EtapaEmailContent(
                            email = email,
                            onEmailChange = { email = it; mostraErro = false },
                            isLoading = isLoading,
                            mostraErro = mostraErro,
                            mensagemErro = mensagemErro,
                            colors = colors,
                            onEnviar = {
                                // Validações no Screen
                                when {
                                    email.isBlank() -> {
                                        mostraErro = true
                                        mensagemErro = "Por favor, digite seu email"
                                    }
                                    !email.contains("@") -> {
                                        mostraErro = true
                                        mensagemErro = "Por favor, digite um email válido"
                                    }
                                    else -> {
                                        mostraErro = false
                                        isLoading = true
                                        onRecoverPassword(
                                            email.trim(),
                                            {
                                                isLoading = false
                                                currentStep = RecuperarSenhaStep.CODIGO
                                            },
                                            { error ->
                                                isLoading = false
                                                mostraErro = true
                                                mensagemErro = error
                                            }
                                        )
                                    }
                                }
                            }
                        )
                    }

                    RecuperarSenhaStep.CODIGO -> {
                        EtapaCodigoContent(
                            codigo = codigo,
                            onCodigoChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) { codigo = it; mostraErro = false } },
                            novaSenha = novaSenha,
                            onNovaSenhaChange = { novaSenha = it; mostraErro = false },
                            confirmarSenha = confirmarSenha,
                            onConfirmarSenhaChange = { confirmarSenha = it; mostraErro = false },
                            mostrarSenha = mostrarSenha,
                            onMostrarSenhaChange = { mostrarSenha = it },
                            isLoading = isLoading,
                            mostraErro = mostraErro,
                            mensagemErro = mensagemErro,
                            colors = colors,
                            onRedefinir = {
                                // Validações no Screen
                                when {
                                    codigo.length != 6 -> {
                                        mostraErro = true
                                        mensagemErro = "O código deve ter 6 dígitos"
                                    }
                                    novaSenha.length < 8 -> {
                                        mostraErro = true
                                        mensagemErro = "A senha deve ter pelo menos 8 caracteres"
                                    }
                                    !Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$").matches(novaSenha) -> {
                                        mostraErro = true
                                        mensagemErro = "A senha deve conter pelo menos 1 maiúscula, 1 minúscula e 1 número"
                                    }
                                    novaSenha != confirmarSenha -> {
                                        mostraErro = true
                                        mensagemErro = "As senhas não coincidem"
                                    }
                                    else -> {
                                        mostraErro = false
                                        isLoading = true
                                        onResetPassword(
                                            codigo.trim(),
                                            novaSenha,
                                            {
                                                isLoading = false
                                                currentStep = RecuperarSenhaStep.SUCESSO
                                            },
                                            { error ->
                                                isLoading = false
                                                mostraErro = true
                                                mensagemErro = error
                                            }
                                        )
                                    }
                                }
                            },
                            onVoltar = {
                                currentStep = RecuperarSenhaStep.EMAIL
                                codigo = ""
                                novaSenha = ""
                                confirmarSenha = ""
                                mostraErro = false
                            }
                        )
                    }

                    RecuperarSenhaStep.SUCESSO -> {
                        EtapaSucessoContent(
                            colors = colors,
                            onVoltarLogin = onBackToLogin
                        )
                    }
                }

                // Link voltar ao login (apenas nas etapas 1 e 2)
                if (currentStep != RecuperarSenhaStep.SUCESSO) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "← Voltar ao login",
                            fontSize = 12.sp,
                            color = colors.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onBackToLogin() }
                        )
                    }
                }
            }

            // Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "© 2026 RanGo. Todos os direitos reservados.",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

/**
 * Conteúdo da Etapa 1: Digitar e-mail
 */
@Composable
private fun EtapaEmailContent(
    email: String,
    onEmailChange: (String) -> Unit,
    isLoading: Boolean,
    mostraErro: Boolean,
    mensagemErro: String,
    colors: PedidosColors,
    onEnviar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mensagem de erro
            if (mostraErro) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp),
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

            Text(
                text = "Digite o e-mail cadastrado na sua conta para receber o código de recuperação.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Campo: Email
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Text(
                    text = "E-mail",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    placeholder = { Text("seu@email.com", fontSize = 13.sp, color = colors.mediumGray) },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.primary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = colors.inputBorder,
                        focusedBorderColor = colors.primary,
                        cursorColor = colors.primary,
                        focusedTextColor = colors.textInput,
                        unfocusedTextColor = colors.textInput
                    )
                )
            }

            // Botão Enviar
            Button(
                onClick = onEnviar,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = colors.textOnPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Enviar Código",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textOnPrimary
                    )
                }
            }
        }
    }
}

/**
 * Conteúdo da Etapa 2: Digitar código de 6 dígitos + nova senha
 */
@Composable
private fun EtapaCodigoContent(
    codigo: String,
    onCodigoChange: (String) -> Unit,
    novaSenha: String,
    onNovaSenhaChange: (String) -> Unit,
    confirmarSenha: String,
    onConfirmarSenhaChange: (String) -> Unit,
    mostrarSenha: Boolean,
    onMostrarSenhaChange: (Boolean) -> Unit,
    isLoading: Boolean,
    mostraErro: Boolean,
    mensagemErro: String,
    colors: PedidosColors,
    onRedefinir: () -> Unit,
    onVoltar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mensagem de erro
            if (mostraErro) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp),
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

            // Campo: Código de 6 dígitos
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Código de Verificação",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = codigo,
                    onValueChange = onCodigoChange,
                    placeholder = { Text("000000", fontSize = 18.sp, color = colors.mediumGray) },
                    textStyle = TextStyle(
                        color = colors.textInput,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textInput,
                        unfocusedTextColor = colors.textInput,
                        cursorColor = colors.primary,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.inputBorder
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    enabled = !isLoading
                )
            }

            // Campo: Nova Senha
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Nova Senha",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = novaSenha,
                    onValueChange = onNovaSenhaChange,
                    placeholder = { Text("Mínimo 8 caracteres", fontSize = 13.sp, color = colors.mediumGray) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.primary)
                    },
                    trailingIcon = {
                        IconButton(onClick = { onMostrarSenhaChange(!mostrarSenha) }) {
                            Icon(
                                if (mostrarSenha) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = colors.iconGray
                            )
                        }
                    },
                    visualTransformation = if (mostrarSenha) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = colors.inputBorder,
                        focusedBorderColor = colors.primary,
                        cursorColor = colors.primary,
                        focusedTextColor = colors.textInput,
                        unfocusedTextColor = colors.textInput
                    )
                )
            }

            // Campo: Confirmar Senha
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Text(
                    text = "Confirmar Nova Senha",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = confirmarSenha,
                    onValueChange = onConfirmarSenhaChange,
                    placeholder = { Text("Digite novamente", fontSize = 13.sp, color = colors.mediumGray) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.primary)
                    },
                    visualTransformation = if (mostrarSenha) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = colors.inputBorder,
                        focusedBorderColor = colors.primary,
                        cursorColor = colors.primary,
                        focusedTextColor = colors.textInput,
                        unfocusedTextColor = colors.textInput
                    )
                )
            }

            // Botão Redefinir Senha
            Button(
                onClick = onRedefinir,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = colors.textOnPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Redefinir Senha",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textOnPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botão solicitar novo código
            TextButton(onClick = onVoltar) {
                Text(
                    text = "Não recebeu? Solicitar novo código",
                    fontSize = 12.sp,
                    color = colors.primary
                )
            }
        }
    }
}

/**
 * Conteúdo da Etapa 3: Sucesso
 */
@Composable
private fun EtapaSucessoContent(
    colors: PedidosColors,
    onVoltarLogin: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ícone de sucesso
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(32.dp),
                color = colors.successBackground
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "✓",
                        fontSize = 32.sp,
                        color = colors.successText
                    )
                }
            }

            // Mensagem de sucesso
            Text(
                text = "Senha redefinida!",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Sua senha foi alterada com sucesso. Agora você pode fazer login com a nova senha.",
                fontSize = 12.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Botão voltar ao login
            Button(
                onClick = onVoltarLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Fazer Login",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textOnPrimary
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EsqueciSenhaScreenPreview() {
    PedidosTheme {
        EsqueciSenhaScreen()
    }
}
