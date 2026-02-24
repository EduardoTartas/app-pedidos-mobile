package dev.fslab.pedidos.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.fslab.pedidos.ui.theme.PedidosTheme
import dev.fslab.pedidos.ui.theme.LocalPedidosColors

/**
 * EsqueciSenhaScreen - Tela de recuperação de senha
 */
@Composable
fun EsqueciSenhaScreen(
    modifier: Modifier = Modifier,
    emailInicial: String = "",
    onBackToLogin: () -> Unit = {}
) {
    val colors = LocalPedidosColors.current

    var email by remember { mutableStateOf(emailInicial) }
    var enviado by remember { mutableStateOf(false) }

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
            // Botão voltar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(onClick = onBackToLogin) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = colors.textOnPrimary
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

            // Título
            Text(
                text = "Esqueceu a senha?",
                color = colors.textOnPrimary,
                style = MaterialTheme.typography.displaySmall
            )

            // Subtítulo
            Text(
                text = "Enviaremos um link de recuperação",
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
                    if (!enviado) {
                        // Instrução
                        Text(
                            text = "Digite o email cadastrado na sua conta para receber o link de redefinição de senha.",
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
                            onValueChange = { email = it },
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

                        Spacer(modifier = Modifier.height(24.dp))

                        // Botão Enviar
                        Button(
                            onClick = { enviado = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary
                            )
                        ) {
                            Text(
                                text = "Enviar link de recuperação",
                                style = MaterialTheme.typography.titleLarge,
                                color = colors.textOnPrimary
                            )
                        }
                    } else {
                        // Mensagem de sucesso
                        Spacer(modifier = Modifier.height(8.dp))

                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = "Email enviado",
                            tint = colors.success,
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Email enviado!",
                            style = MaterialTheme.typography.headlineLarge,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Verifique sua caixa de entrada e siga as instruções para redefinir sua senha.",
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Link voltar ao login
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
