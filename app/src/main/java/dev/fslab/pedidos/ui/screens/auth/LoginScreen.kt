package dev.fslab.pedidos.ui.screens.auth

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fslab.pedidos.R
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import dev.fslab.pedidos.ui.theme.PedidosTheme

/** LoginScreen - Tela de autenticação (login) da aplicação Segue o padrão arquitetural. */
@Composable
fun LoginScreen(
        modifier: Modifier = Modifier,
        isDarkTheme: Boolean = false,
        onToggleTheme: () -> Unit = {},
        onEsqueciSenha: (String) -> Unit = {},
        onRegister: () -> Unit = {},
        onLogin: (email: String, senha: String) -> Unit = { _, _ -> },
        onGoogleSignIn: () -> Unit = {},
        isLoading: Boolean = false,
        errorMessage: String? = null,
        onErrorDismiss: () -> Unit = {}
) {
    val colors = LocalPedidosColors.current

    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var lembrarMe by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(colors.background).imePadding()) {
        Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Barra superior — apenas botão de tema
            Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onToggleTheme) {
                    Icon(
                            imageVector =
                                    if (isDarkTheme) Icons.Filled.LightMode
                                    else Icons.Filled.DarkMode,
                            contentDescription = if (isDarkTheme) "Modo claro" else "Modo escuro",
                            tint = colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ícone do app
            Box(
                    modifier =
                            Modifier.size(90.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(colors.primary),
                    contentAlignment = Alignment.Center
            ) {
                Image(
                        painter = painterResource(id = R.drawable.iguana_icon),
                        contentDescription = "Ícone RanGo",
                        modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Título
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                        text = "RanG",
                        color = colors.textPrimary,
                        style = MaterialTheme.typography.displaySmall
                )
                Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "O",
                        tint = colors.primary,
                        modifier = Modifier.size(28.dp).offset(x = (-4).dp).padding(bottom = 3.dp)
                )
            }

            // Subtítulo
            Text(
                    text = "Seu rango favorito está aqui",
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card branco com formulário
            Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    // Mensagem de erro — dentro do card, antes dos campos
                    if (!errorMessage.isNullOrBlank()) {
                        Surface(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = colors.errorBackground
                        ) {
                            Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                        text = errorMessage,
                                        fontSize = 13.sp,
                                        color = colors.errorText,
                                        modifier = Modifier.weight(1f)
                                )
                                Text(
                                        text = "✕",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.errorText,
                                        modifier = Modifier.clickable { onErrorDismiss() }
                                )
                            }
                        }
                    }

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
                                if (!errorMessage.isNullOrEmpty()) {
                                    onErrorDismiss()
                                }
                            },
                            placeholder = { Text("seu@email.com", color = colors.mediumGray) },
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
                            enabled = !isLoading,
                            colors =
                                    OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = colors.inputBorder,
                                            focusedBorderColor = colors.primary,
                                            cursorColor = colors.primary,
                                            focusedTextColor = colors.textInput,
                                            unfocusedTextColor = colors.textInput
                                    )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Senha
                    Text(
                            text = "Senha",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                            value = senha,
                            onValueChange = {
                                senha = it
                                if (!errorMessage.isNullOrEmpty()) {
                                    onErrorDismiss()
                                }
                            },
                            placeholder = { Text("••••••••", color = colors.mediumGray) },
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
                                            imageVector =
                                                    if (senhaVisivel) Icons.Filled.Visibility
                                                    else Icons.Filled.VisibilityOff,
                                            contentDescription =
                                                    if (senhaVisivel) "Ocultar senha"
                                                    else "Mostrar senha",
                                            tint = colors.iconGray
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation =
                                    if (senhaVisivel) VisualTransformation.None
                                    else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            enabled = !isLoading,
                            colors =
                                    OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = colors.inputBorder,
                                            focusedBorderColor = colors.primary,
                                            cursorColor = colors.primary,
                                            focusedTextColor = colors.textInput,
                                            unfocusedTextColor = colors.textInput
                                    )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lembrar-me e Esqueci a senha
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                    checked = lembrarMe,
                                    onCheckedChange = { lembrarMe = it },
                                    enabled = !isLoading,
                                    colors = CheckboxDefaults.colors(checkedColor = colors.primary)
                            )
                            Text(
                                    text = "Lembrar-me",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.textSecondary
                            )
                        }
                        Text(
                                text = "Esqueci a senha",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.primary,
                                modifier = Modifier.clickable { onEsqueciSenha(email) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botão Entrar
                    Button(
                            onClick = { onLogin(email, senha) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            enabled = !isLoading && email.isNotBlank() && senha.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = colors.textOnPrimary,
                                    strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                    text = "Entrar",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = colors.textOnPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Divisor "ou"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = colors.inputBorder
                        )
                        Text(
                            text = "  ou  ",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = colors.inputBorder
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botão "Entrar com Google"
                    OutlinedButton(
                        onClick = onGoogleSignIn,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading,
                        border = ButtonDefaults.outlinedButtonBorder(enabled = !isLoading)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Entrar com Google",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Não tem conta? Cadastre-se
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                                text = "Não tem conta?  ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary
                        )
                        Text(
                                text = "Cadastre-se",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.primary,
                                modifier = Modifier.clickable { onRegister() }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    PedidosTheme { LoginScreen() }
}
