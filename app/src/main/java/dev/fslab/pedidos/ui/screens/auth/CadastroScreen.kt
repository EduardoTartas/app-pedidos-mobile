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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import dev.fslab.pedidos.ui.theme.PedidosTheme
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone


/**
 * CadastroScreen - Tela de cadastro de novo usuário
 * Segue o design do RanGo App Style Guide, usando cores do tema.
 */
@Composable
fun CadastroScreen(
    modifier: Modifier = Modifier,
    onBackToLogin: () -> Unit = {},
    onRegister: (nome: String, email: String, senha: String, cpf: String, telefone: String) -> Unit = { _, _, _, _, _ -> },
    isLoading: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null,
    onErrorDismiss: () -> Unit = {}
) {
    val colors = LocalPedidosColors.current

    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var confirmarSenhaVisivel by remember { mutableStateOf(false) }
    var aceitaTermos by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            colors.backgroundGradientStart,
            colors.backgroundGradientEnd
        )
    )

    // Validação local dos campos
    fun validarCampos(): Boolean {
        if (nome.isBlank() || email.isBlank() || cpf.isBlank() || telefone.isBlank() || senha.isBlank() || confirmarSenha.isBlank()) {
            localError = "Preencha todos os campos."
            return false
        }
        if (!email.contains("@") || !email.contains(".")) {
            localError = "Formato de e-mail inválido."
            return false
        }
        val cpfLimpo = cpf.replace(Regex("[^0-9]"), "")
        if (cpfLimpo.length != 11) {
            localError = "CPF deve conter 11 dígitos numéricos."
            return false
        }
        val telLimpo = telefone.replace(Regex("[^0-9]"), "")
        if (telLimpo.length !in 10..11) {
            localError = "Telefone deve conter 10 ou 11 dígitos."
            return false
        }
        if (senha.length < 8) {
            localError = "A senha deve ter pelo menos 8 caracteres."
            return false
        }
        val senhaRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")
        if (!senhaRegex.matches(senha)) {
            localError = "A senha deve conter pelo menos 1 letra maiúscula, 1 minúscula e 1 número."
            return false
        }
        if (senha != confirmarSenha) {
            localError = "As senhas não coincidem."
            return false
        }
        localError = null
        return true
    }

    val displayError = localError ?: errorMessage

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Header: Botão voltar + Título
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botão voltar com fundo circular
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.textOnPrimary.copy(alpha = 0.1f))
                        .clickable { onBackToLogin() },
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
                    text = "Criar Conta",
                    color = colors.textOnPrimary,
                    style = MaterialTheme.typography.displaySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Campo Nome Completo
            Text(
                text = "Nome Completo",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = nome,
                onValueChange = {
                    nome = it
                    localError = null
                    onErrorDismiss()
                },
                placeholder = {
                    Text("Digite seu nome completo", color = colors.mediumGray)
                },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.primary)
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

            // Campo E-mail
            Text(
                text = "E-mail",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    localError = null
                    onErrorDismiss()
                },
                placeholder = {
                    Text("seu@email.com", color = colors.mediumGray)
                },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.primary)
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

            Spacer(modifier = Modifier.height(20.dp))

            // Campo CPF
            Text(
                text = "CPF",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = cpf,
                onValueChange = {
                    // Só permite dígitos, limitado a 11
                    val filtered = it.filter { c -> c.isDigit() }.take(11)
                    cpf = filtered
                    localError = null
                    onErrorDismiss()
                },
                placeholder = {
                    Text("00000000000", color = colors.mediumGray)
                },
                leadingIcon = {
                    Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.primary)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

            // Campo Telefone
            Text(
                text = "Telefone",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = telefone,
                onValueChange = {
                    // Só permite dígitos, limitado a 11
                    val filtered = it.filter { c -> c.isDigit() }.take(11)
                    telefone = filtered
                    localError = null
                    onErrorDismiss()
                },
                placeholder = {
                    Text("11999999999", color = colors.mediumGray)
                },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.primary)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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

            // Campo Senha
            Text(
                text = "Senha",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = senha,
                onValueChange = {
                    senha = it
                    localError = null
                    onErrorDismiss()
                },
                placeholder = {
                    Text("••••••••", color = colors.mediumGray)
                },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.primary)
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
                text = "Confirmar Senha",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = confirmarSenha,
                onValueChange = {
                    confirmarSenha = it
                    localError = null
                    onErrorDismiss()
                },
                placeholder = {
                    Text("••••••••", color = colors.mediumGray)
                },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.primary)
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

            Spacer(modifier = Modifier.height(16.dp))

            // Aceitar termos
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = aceitaTermos,
                    onCheckedChange = { aceitaTermos = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.primary,
                        uncheckedColor = colors.mediumGray,
                        checkmarkColor = colors.textOnPrimary
                    )
                )
                Text(
                    text = "Li e aceito os ",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary
                )
                Text(
                    text = "Termos de Uso",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.primary,
                    modifier = Modifier.clickable { /* TODO */ }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mensagem de erro
            if (!displayError.isNullOrBlank()) {
                Text(
                    text = displayError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.error,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Mensagem de sucesso
            if (!successMessage.isNullOrBlank()) {
                Text(
                    text = successMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.success,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Botão CRIAR CONTA
            Button(
                onClick = {
                    if (validarCampos()) {
                        val cpfLimpo = cpf.replace(Regex("[^0-9]"), "")
                        val telLimpo = telefone.replace(Regex("[^0-9]"), "")
                        onRegister(nome.trim(), email.trim(), senha, cpfLimpo, telLimpo)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = aceitaTermos && !isLoading,
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
                        text = "CRIAR CONTA",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textOnPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Já tem uma conta? Faça login
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Já tem uma conta? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
                Text(
                    text = "Faça login",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.primary,
                    modifier = Modifier.clickable { onBackToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CadastroScreenPreview() {
    PedidosTheme {
        CadastroScreen()
    }
}
