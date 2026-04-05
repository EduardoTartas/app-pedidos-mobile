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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import dev.fslab.pedidos.ui.theme.PedidosTheme

/**
 * CompletarPerfilScreen — Tela exibida após login via Google quando CPF e telefone
 * ainda não foram preenchidos. O usuário pode completar agora ou pular.
 */
@Composable
fun CompletarPerfilScreen(
    modifier: Modifier = Modifier,
    onComplete: (cpf: String, telefone: String) -> Unit = { _, _ -> },
    onSkip: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onErrorDismiss: () -> Unit = {}
) {
    val colors = LocalPedidosColors.current

    var cpfField by remember { mutableStateOf(TextFieldValue("")) }
    var telefoneField by remember { mutableStateOf(TextFieldValue("")) }

    // ══════════════════════════════════════════════
    // Máscaras com controle de cursor
    // ══════════════════════════════════════════════

    fun formatCpf(digits: String): String {
        val d = digits.take(11)
        return buildString {
            d.forEachIndexed { i, c ->
                when (i) {
                    3, 6 -> append('.')
                    9 -> append('-')
                }
                append(c)
            }
        }
    }

    fun formatTelefone(digits: String): String {
        val d = digits.take(11)
        return buildString {
            d.forEachIndexed { i, c ->
                when (i) {
                    0 -> append('(')
                    2 -> append(") ")
                    7 -> append('-')
                }
                append(c)
            }
        }
    }

    fun onCpfChange(newValue: TextFieldValue) {
        val newDigits = newValue.text.filter { it.isDigit() }.take(11)
        val formatted = formatCpf(newDigits)
        cpfField = TextFieldValue(
            text = formatted,
            selection = TextRange(formatted.length)
        )
    }

    fun onTelefoneChange(newValue: TextFieldValue) {
        val newDigits = newValue.text.filter { it.isDigit() }.take(11)
        val formatted = formatTelefone(newDigits)
        telefoneField = TextFieldValue(
            text = formatted,
            selection = TextRange(formatted.length)
        )
    }

    // Extrai apenas dígitos para envio à API
    fun cpfDigits(): String = cpfField.text.filter { it.isDigit() }
    fun telefoneDigits(): String = telefoneField.text.filter { it.isDigit() }

    val cpfValid = cpfDigits().length == 11
    val telefoneValid = telefoneDigits().length in 10..11

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Ícone/emoji
            Text(
                text = "👋",
                fontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Quase lá!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Para fazer pedidos, precisamos de algumas informações.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Mensagem de erro
                    if (!errorMessage.isNullOrBlank()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
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

                    // Campo CPF
                    Text(
                        text = "CPF",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = cpfField,
                        onValueChange = { newValue ->
                            onCpfChange(newValue)
                            if (!errorMessage.isNullOrEmpty()) onErrorDismiss()
                        },
                        placeholder = { Text("000.000.000-00", color = colors.mediumGray) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Badge,
                                contentDescription = "CPF",
                                tint = colors.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Telefone
                    Text(
                        text = "Telefone",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = telefoneField,
                        onValueChange = { newValue ->
                            onTelefoneChange(newValue)
                            if (!errorMessage.isNullOrEmpty()) onErrorDismiss()
                        },
                        placeholder = { Text("(00) 00000-0000", color = colors.mediumGray) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Phone,
                                contentDescription = "Telefone",
                                tint = colors.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
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

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botão Continuar
                    Button(
                        onClick = { onComplete(cpfDigits(), telefoneDigits()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        enabled = !isLoading && cpfValid && telefoneValid
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = colors.textOnPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Continuar",
                                style = MaterialTheme.typography.titleLarge,
                                color = colors.textOnPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preencher depois
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = onSkip,
                            enabled = !isLoading
                        ) {
                            Text(
                                text = "Preencher depois",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Sem esses dados você não poderá finalizar pedidos.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CompletarPerfilScreenPreview() {
    PedidosTheme { CompletarPerfilScreen() }
}
