package dev.fslab.pedidos.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Key
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.fslab.pedidos.ui.theme.PedidosTheme
import dev.fslab.pedidos.ui.theme.LocalPedidosColors

/**
 * EsqueciSenhaScreen - Tela de recuperação de senha
 *
 * Fluxo em 3 etapas:
 *   Etapa 0 → Informar e-mail para receber o token de recuperação
 *   Etapa 1 → Informar o token recebido + nova senha
 *   Etapa 2 → Mensagem de sucesso
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

    var email by remember { mutableStateOf(emailInicial) }
    var token by remember { mutableStateOf("") }
    var novaSenha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var confirmarSenhaVisivel by remember { mutableStateOf(false) }

    // 0 = informar email, 1 = informar token+senha, 2 = sucesso
    var etapa by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            colors.primaryDark,
            colors.primary,
            colors.primary.copy(alpha = 0.7f)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header com botão voltar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 48.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.textOnPrimary.copy(alpha = 0.1f))
                            .clickable {
                                if (etapa == 1) {
                                    etapa = 0
                                    errorMessage = null
                                } else {
                                    onBackToLogin()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = colors.textOnPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = when (etapa) {
                            0 -> "Recuperar Senha"
                            1 -> "Redefinir Senha"
                            else -> "Senha Redefinida"
                        },
                        color = colors.textOnPrimary,
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Ícone
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LockReset,
                    contentDescription = "Recuperar senha",
                    tint = colors.textOnPrimary,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtítulo
            Text(
                text = when (etapa) {
                    0 -> "Enviaremos um código de recuperação"
                    1 -> "Informe o código e a nova senha"
                    else -> "Sua senha foi alterada com sucesso!"
                },
                color = colors.textOnPrimary.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card com formulário
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    when (etapa) {
                        // ═══════════════════════════════════════
                        // ETAPA 0 — Informar e-mail
                        // ═══════════════════════════════════════
                        0 -> {
                            Text(
                                text = "Digite o e-mail cadastrado na sua conta para receber o código de recuperação.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Campo Email
                            Text(
                                text = "Email",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    errorMessage = null
                                },
                                placeholder = {
                                    Text("seu@email.com", color = colors.mediumGray)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Email,
                                        contentDescription = "Email",
                                        tint = colors.primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = colors.inputBorder,
                                    focusedBorderColor = colors.primary,
                                    cursorColor = colors.primary,
                                    focusedTextColor = colors.textInput,
                                    unfocusedTextColor = colors.textInput
                                )
                            )

                            // Mensagem de erro
                            if (!errorMessage.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = errorMessage!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.error,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Botão Enviar
                            Button(
                                onClick = {
                                    if (email.isBlank() || !email.contains("@")) {
                                        errorMessage = "Informe um e-mail válido."
                                        return@Button
                                    }
                                    isLoading = true
                                    errorMessage = null
                                    onRecoverPassword(
                                        email.trim(),
                                        {
                                            isLoading = false
                                            etapa = 1
                                        },
                                        { msg ->
                                            isLoading = false
                                            errorMessage = msg
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading && email.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primary,
                                    disabledContainerColor = colors.primary.copy(alpha = 0.4f)
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = colors.textOnPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Enviar código",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = colors.textOnPrimary
                                    )
                                }
                            }
                        }

                        // ═══════════════════════════════════════
                        // ETAPA 1 — Informar token + nova senha
                        // ═══════════════════════════════════════
                        1 -> {
                            Text(
                                text = "Informe o código de recuperação recebido e defina sua nova senha.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Campo Token
                            Text(
                                text = "Código de Recuperação",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = token,
                                onValueChange = {
                                    token = it
                                    errorMessage = null
                                },
                                placeholder = {
                                    Text("Cole o código recebido", color = colors.mediumGray)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Key,
                                        contentDescription = "Código",
                                        tint = colors.primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = colors.inputBorder,
                                    focusedBorderColor = colors.primary,
                                    cursorColor = colors.primary,
                                    focusedTextColor = colors.textInput,
                                    unfocusedTextColor = colors.textInput
                                )
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Campo Nova Senha
                            Text(
                                text = "Nova Senha",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = novaSenha,
                                onValueChange = {
                                    novaSenha = it
                                    errorMessage = null
                                },
                                placeholder = {
                                    Text("••••••••", color = colors.mediumGray)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Lock,
                                        contentDescription = "Senha",
                                        tint = colors.primary
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                                        Icon(
                                            imageVector = if (senhaVisivel) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                            contentDescription = if (senhaVisivel) "Ocultar senha" else "Mostrar senha",
                                            tint = colors.iconGray
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = colors.inputBorder,
                                    focusedBorderColor = colors.primary,
                                    cursorColor = colors.primary,
                                    focusedTextColor = colors.textInput,
                                    unfocusedTextColor = colors.textInput
                                )
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Campo Confirmar Senha
                            Text(
                                text = "Confirmar Nova Senha",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = confirmarSenha,
                                onValueChange = {
                                    confirmarSenha = it
                                    errorMessage = null
                                },
                                placeholder = {
                                    Text("••••••••", color = colors.mediumGray)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Lock,
                                        contentDescription = "Confirmar senha",
                                        tint = colors.primary
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { confirmarSenhaVisivel = !confirmarSenhaVisivel }) {
                                        Icon(
                                            imageVector = if (confirmarSenhaVisivel) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                            contentDescription = if (confirmarSenhaVisivel) "Ocultar senha" else "Mostrar senha",
                                            tint = colors.iconGray
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (confirmarSenhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = colors.inputBorder,
                                    focusedBorderColor = colors.primary,
                                    cursorColor = colors.primary,
                                    focusedTextColor = colors.textInput,
                                    unfocusedTextColor = colors.textInput
                                )
                            )

                            // Mensagem de erro
                            if (!errorMessage.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = errorMessage!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.error,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Botão Redefinir Senha
                            Button(
                                onClick = {
                                    if (token.isBlank()) {
                                        errorMessage = "Informe o código de recuperação."
                                        return@Button
                                    }
                                    if (novaSenha.length < 8) {
                                        errorMessage = "A senha deve ter pelo menos 8 caracteres."
                                        return@Button
                                    }
                                    val senhaRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")
                                    if (!senhaRegex.matches(novaSenha)) {
                                        errorMessage = "A senha deve conter pelo menos 1 maiúscula, 1 minúscula e 1 número."
                                        return@Button
                                    }
                                    if (novaSenha != confirmarSenha) {
                                        errorMessage = "As senhas não coincidem."
                                        return@Button
                                    }
                                    isLoading = true
                                    errorMessage = null
                                    onResetPassword(
                                        token.trim(),
                                        novaSenha,
                                        {
                                            isLoading = false
                                            etapa = 2
                                        },
                                        { msg ->
                                            isLoading = false
                                            errorMessage = msg
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading && token.isNotBlank() && novaSenha.isNotBlank() && confirmarSenha.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primary,
                                    disabledContainerColor = colors.primary.copy(alpha = 0.4f)
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = colors.textOnPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Redefinir Senha",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = colors.textOnPrimary
                                    )
                                }
                            }
                        }

                        // ═══════════════════════════════════════
                        // ETAPA 2 — Sucesso
                        // ═══════════════════════════════════════
                        2 -> {
                            Spacer(modifier = Modifier.height(8.dp))

                            Icon(
                                imageVector = Icons.Filled.LockReset,
                                contentDescription = "Senha redefinida",
                                tint = colors.success,
                                modifier = Modifier
                                    .size(48.dp)
                                    .align(Alignment.CenterHorizontally)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Senha redefinida!",
                                style = MaterialTheme.typography.headlineLarge,
                                color = colors.textPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Sua senha foi alterada com sucesso. Agora você pode fazer login com a nova senha.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Botão Voltar ao Login
                            Button(
                                onClick = onBackToLogin,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primary
                                )
                            ) {
                                Text(
                                    text = "Voltar ao Login",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = colors.textOnPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Link voltar ao login (apenas nas etapas 0 e 1)
                    if (etapa < 2) {
                        Text(
                            text = "← Voltar ao login",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onBackToLogin() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
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
