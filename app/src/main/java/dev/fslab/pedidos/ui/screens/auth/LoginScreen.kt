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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fslab.pedidos.R
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import dev.fslab.pedidos.ui.theme.PedidosTheme

/** LoginScreen - Base restaurada, com melhorias pontuais no formulário e tipografia UI/UX Pro Max. */
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

    var showTermosDialog by remember { mutableStateOf(false) }

    if (showTermosDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTermosDialog = false },
            containerColor = colors.surface,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textSecondary,
            title = {
                Text(
                    text = "Termos e Privacidade",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Termos de uso",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Ao usar o RanGo, você concorda em manter seus dados de cadastro corretos, usar a plataforma de forma responsável e respeitar restaurantes, entregadores e outros usuários. O RanGo conecta clientes a restaurantes parceiros para consulta de cardápios, criação de pedidos, acompanhamento de status, avaliações e notificações.\n\n" +
                               "Os preços, disponibilidade de produtos, prazos, taxas e condições de entrega podem variar conforme o restaurante. Depois de confirmar um pedido, alterações ou cancelamentos podem depender do estágio de preparo e das regras aplicáveis ao pedido.\n\n" +
                               "Não é permitido usar o app para fraudes, ofensas, tentativas de acesso indevido, pedidos falsos, violação de direitos de terceiros ou qualquer conduta que comprometa a segurança da plataforma. O RanGo pode limitar, suspender ou encerrar contas em caso de uso irregular.\n\n" +
                               "Podemos atualizar estes termos para refletir melhorias do serviço, novas funcionalidades ou exigências legais. Quando houver mudanças relevantes, você poderá ser avisado pelo app ou pelos canais de contato cadastrados.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Privacidade",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "O RanGo trata seus dados para operar o serviço de delivery com segurança e transparência. Podemos coletar dados de cadastro, contato, endereços, histórico de pedidos, avaliações, informações de acesso, preferências, notificações e dados necessários para suporte.\n\n" +
                               "Usamos essas informações para criar e proteger sua conta, processar pedidos, conectar você aos restaurantes, enviar comunicações importantes, melhorar a experiência, prevenir fraudes, cumprir obrigações legais e responder solicitações de ajuda.\n\n" +
                               "Seus dados podem ser compartilhados somente quando necessário com restaurantes envolvidos no pedido, prestadores de tecnologia, serviços de pagamento, suporte, armazenamento, análise de segurança ou autoridades competentes, sempre conforme a legislação aplicável.\n\n" +
                               "Você pode solicitar acesso, correção, atualização ou exclusão dos seus dados, além de tirar dúvidas sobre privacidade, pelo e-mail admin@delivery.com. Mantemos medidas técnicas e administrativas para proteger as informações contra acesso não autorizado, perda, alteração ou uso indevido.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTermosDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("ENTENDI", color = colors.textOnPrimary, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (isLoading && email.isBlank() && senha.isBlank()) {
        Box(
            modifier = modifier.fillMaxSize().background(colors.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                        modifier = Modifier.size(60.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(
                    color = colors.primary,
                    strokeWidth = 3.dp
                )
            }
        }
        return
    }

    Box(modifier = modifier.fillMaxSize().background(colors.background).systemBarsPadding().imePadding()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Spacer(modifier = Modifier.height(32.dp))

            // Título com ícone (Startup Bold + Location)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "RanG",
                    color = colors.textPrimary,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-2).sp
                )
                Icon(
                    painter = painterResource(id = R.drawable.pin),
                    contentDescription = "O",
                    tint = colors.primary,
                    modifier = Modifier
                        .size(48.dp)
                        .offset(x = (-4).dp)
                        .padding(bottom = 2.dp)
                )
            }

            // Subtítulo
            Text(
                text = "Seu rango favorito, rapidinho.",
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // Caixa de Form aprimorada (Card)
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(32.dp)) {
                    if (!errorMessage.isNullOrBlank()) {
                        val isEmailVerificationInfo = errorMessage.contains("verifique seu e-mail", ignoreCase = true)
                        val bgColor = if (isEmailVerificationInfo) colors.featureBlue.copy(alpha = 0.15f) else colors.errorBackground
                        val textColor = if (isEmailVerificationInfo) colors.featureBlue else colors.errorText

                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = bgColor
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = errorMessage,
                                    fontSize = 13.sp,
                                    color = textColor,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "✕",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    modifier = Modifier.clickable { onErrorDismiss() }
                                )
                            }
                        }
                    }

                    // Campo Email
                    CustomOutlinedField(
                        value = email,
                        label = "E-mail",
                        icon = Icons.Default.Email,
                        placeholder = "seu@email.com",
                        keyboardType = KeyboardType.Email,
                        onValueChange = {
                            email = it
                            if (!errorMessage.isNullOrEmpty()) onErrorDismiss()
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Senha
                    CustomOutlinedField(
                        value = senha,
                        label = "Senha",
                        icon = Icons.Default.Lock,
                        placeholder = "Digite sua senha",
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        passwordVisible = senhaVisivel,
                        onVisibilityChange = { senhaVisivel = it },
                        onValueChange = {
                            senha = it
                            if (!errorMessage.isNullOrEmpty()) onErrorDismiss()
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Termos e Privacidade",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.textTertiary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { showTermosDialog = true }.padding(vertical = 4.dp)
                        )
                        Text(
                            text = "Esqueci a senha",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onEsqueciSenha(email) }.padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botão Entrar
                    Button(
                        onClick = { onLogin(email, senha) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
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
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textOnPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = colors.inputBorder)
                        Text(
                            text = " ou ",
                            color = colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = colors.inputBorder)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = onGoogleSignIn,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading,
                        border = ButtonDefaults.outlinedButtonBorder(enabled = !isLoading).copy(
                            brush = androidx.compose.ui.graphics.SolidColor(colors.inputBorder)
                        )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continuar com Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Ainda não tem conta? ",
                            color = colors.textSecondary
                        )
                        Text(
                            text = "Cadastre-se",
                            color = colors.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onRegister() }
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
fun LoginScreenPreview() {
    PedidosTheme { LoginScreen() }
}
