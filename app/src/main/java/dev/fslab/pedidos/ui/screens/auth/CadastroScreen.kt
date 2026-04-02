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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import dev.fslab.pedidos.ui.theme.PedidosTheme

/**
 * CadastroScreen - Tela de cadastro de novo usuário (rota /signup)
 * Segue o padrão arquitetural com validações locais no Screen.
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

    // Estado dos campos
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var confirmarSenhaVisivel by remember { mutableStateOf(false) }
    var aceitaTermos by remember { mutableStateOf(false) }

    // Erro local (validações no Screen)
    var mostraErro by remember { mutableStateOf(false) }
    var mensagemErro by remember { mutableStateOf("") }

    // Erro combinado: local ou vindo do ViewModel
    val displayError = if (mostraErro) mensagemErro else errorMessage

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
                            .clickable { onBackToLogin() },
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
                        text = "Criar Conta",
                        color = colors.textPrimary,
                        style = MaterialTheme.typography.displaySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Subtítulo
                Text(
                    text = "Cadastre-se para usar o RanGo",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Card com formulário
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Mensagem de erro
                        if (!displayError.isNullOrBlank()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = colors.errorBackground
                            ) {
                                Text(
                                    text = displayError,
                                    fontSize = 12.sp,
                                    color = colors.errorText,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // Mensagem de sucesso
                        if (!successMessage.isNullOrBlank() && displayError.isNullOrBlank()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = colors.successBackground
                            ) {
                                Text(
                                    text = successMessage,
                                    fontSize = 12.sp,
                                    color = colors.successText,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // Campo Nome Completo
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        ) {
                            Text(
                                text = "Nome Completo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = nome,
                                onValueChange = {
                                    nome = it
                                    mostraErro = false
                                    onErrorDismiss()
                                },
                                placeholder = { Text("Digite seu nome completo", fontSize = 13.sp, color = colors.mediumGray) },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.primary)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
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

                        // Campo E-mail
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
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
                                onValueChange = {
                                    email = it
                                    mostraErro = false
                                    onErrorDismiss()
                                },
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

                        // Campo CPF
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        ) {
                            Text(
                                text = "CPF",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = cpf,
                                onValueChange = {
                                    val filtered = it.filter { c -> c.isDigit() }.take(11)
                                    cpf = filtered
                                    mostraErro = false
                                    onErrorDismiss()
                                },
                                placeholder = { Text("00000000000", fontSize = 13.sp, color = colors.mediumGray) },
                                leadingIcon = {
                                    Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.primary)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                        // Campo Telefone
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        ) {
                            Text(
                                text = "Telefone",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = telefone,
                                onValueChange = {
                                    val filtered = it.filter { c -> c.isDigit() }.take(11)
                                    telefone = filtered
                                    mostraErro = false
                                    onErrorDismiss()
                                },
                                placeholder = { Text("11999999999", fontSize = 13.sp, color = colors.mediumGray) },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.primary)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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

                        // Campo Senha
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        ) {
                            Text(
                                text = "Senha",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = senha,
                                onValueChange = {
                                    senha = it
                                    mostraErro = false
                                    onErrorDismiss()
                                },
                                placeholder = { Text("Mínimo 8 caracteres", fontSize = 13.sp, color = colors.mediumGray) },
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
                                shape = RoundedCornerShape(8.dp),
                                visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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

                        // Campo Confirmar Senha
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        ) {
                            Text(
                                text = "Confirmar Senha",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = confirmarSenha,
                                onValueChange = {
                                    confirmarSenha = it
                                    mostraErro = false
                                    onErrorDismiss()
                                },
                                placeholder = { Text("Repita a senha", fontSize = 13.sp, color = colors.mediumGray) },
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
                                shape = RoundedCornerShape(8.dp),
                                visualTransformation = if (confirmarSenhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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

                        // Aceitar termos
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = aceitaTermos,
                                onCheckedChange = { aceitaTermos = it },
                                enabled = !isLoading,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = colors.primary,
                                    uncheckedColor = colors.mediumGray,
                                    checkmarkColor = colors.textOnPrimary
                                )
                            )
                            Text(
                                text = "Li e aceito os ",
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "Termos de Uso",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.primary,
                                modifier = Modifier.clickable { /* TODO */ }
                            )
                        }

                        // Botão CRIAR CONTA — Validações locais no Screen
                        Button(
                            onClick = {
                                when {
                                    nome.isBlank() -> {
                                        mostraErro = true
                                        mensagemErro = "Por favor, digite seu nome completo"
                                    }
                                    email.isBlank() || !email.contains("@") || !email.contains(".") -> {
                                        mostraErro = true
                                        mensagemErro = "Por favor, digite um email válido"
                                    }
                                    cpf.replace(Regex("[^0-9]"), "").length != 11 -> {
                                        mostraErro = true
                                        mensagemErro = "CPF deve conter 11 dígitos numéricos"
                                    }
                                    telefone.replace(Regex("[^0-9]"), "").let { it.length !in 10..11 } -> {
                                        mostraErro = true
                                        mensagemErro = "Telefone deve conter 10 ou 11 dígitos"
                                    }
                                    senha.length < 8 -> {
                                        mostraErro = true
                                        mensagemErro = "A senha deve ter pelo menos 8 caracteres"
                                    }
                                    !Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$").matches(senha) -> {
                                        mostraErro = true
                                        mensagemErro = "A senha deve conter pelo menos 1 maiúscula, 1 minúscula e 1 número"
                                    }
                                    senha != confirmarSenha -> {
                                        mostraErro = true
                                        mensagemErro = "As senhas não coincidem"
                                    }
                                    !aceitaTermos -> {
                                        mostraErro = true
                                        mensagemErro = "Você deve aceitar os Termos de Uso"
                                    }
                                    else -> {
                                        mostraErro = false
                                        val cpfLimpo = cpf.replace(Regex("[^0-9]"), "")
                                        val telLimpo = telefone.replace(Regex("[^0-9]"), "")
                                        onRegister(nome.trim(), email.trim(), senha, cpfLimpo, telLimpo)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = colors.textOnPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Criar Conta",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textOnPrimary
                                )
                            }
                        }
                    }
                }

                // Link voltar ao login
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "Já tem uma conta? ",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "Faça login",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primary,
                        modifier = Modifier.clickable { onBackToLogin() }
                    )
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CadastroScreenPreview() {
    PedidosTheme {
        CadastroScreen()
    }
}
