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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import dev.fslab.pedidos.ui.theme.PedidosTheme

private enum class CadastroStep(val title: String, val subtitle: String) {
    DADOS_PESSOAIS("Quem é você?", "Vamos começar com o básico."),
    CONTATO("Fale conosco", "Precisamos do seu contato."),
    SEGURANCA("Segurança", "Crie uma senha forte.")
}

@OptIn(ExperimentalAnimationApi::class)
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

    var currentStep by remember { mutableStateOf(CadastroStep.DADOS_PESSOAIS) }

    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var confirmarSenhaVisivel by remember { mutableStateOf(false) }
    var aceitaTermos by remember { mutableStateOf(false) }

    // Erros individuais por campo (Inline Validation)
    var nomeError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var cpfError by remember { mutableStateOf<String?>(null) }
    var telefoneError by remember { mutableStateOf<String?>(null) }
    var senhaError by remember { mutableStateOf<String?>(null) }
    var confirmarSenhaError by remember { mutableStateOf<String?>(null) }
    var termosError by remember { mutableStateOf<String?>(null) }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

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
                .heightIn(min = screenHeight - 64.dp), // descontando um pouco para as barras do sistema não causarem scroll desnecessário
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
                                    CadastroStep.DADOS_PESSOAIS -> onBackToLogin()
                                    CadastroStep.CONTATO -> { currentStep = CadastroStep.DADOS_PESSOAIS }
                                    CadastroStep.SEGURANCA -> { currentStep = CadastroStep.CONTATO }
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
                        text = "Criar Conta",
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

                        // Erro global do servidor apenas
                        if (!errorMessage.isNullOrBlank()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = colors.errorBackground
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                                    Text(text = errorMessage, fontSize = 12.sp, color = colors.errorText, modifier = Modifier.weight(1f))
                                    Text(text = "✕", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.errorText, modifier = Modifier.clickable { onErrorDismiss() })
                                }
                            }
                        }

                        if (!successMessage.isNullOrBlank() && errorMessage.isNullOrBlank()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                shape = RoundedCornerShape(16.dp),
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

                        // Animação de form
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
                                CadastroStep.DADOS_PESSOAIS -> {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        CustomOutlinedField(
                                            value = nome, label = "Nome Completo", icon = Icons.Default.Person, placeholder = "João da Silva",
                                            isError = nomeError != null, errorMessage = nomeError,
                                            onValueChange = { nome = it; nomeError = null; onErrorDismiss() }
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        CustomOutlinedField(
                                            value = email, label = "E-mail", icon = Icons.Default.Email, placeholder = "seu@email.com",
                                            keyboardType = KeyboardType.Email,
                                            isError = emailError != null, errorMessage = emailError,
                                            onValueChange = { email = it; emailError = null; onErrorDismiss() }
                                        )
                                    }
                                }
                                CadastroStep.CONTATO -> {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        CustomOutlinedField(
                                            value = cpf, label = "CPF", icon = Icons.Default.Badge, placeholder = "000.000.000-00",
                                            keyboardType = KeyboardType.Number,
                                            visualTransformation = MaskVisualTransformation("###.###.###-##"),
                                            isError = cpfError != null, errorMessage = cpfError,
                                            onValueChange = { cpf = it.filter { c -> c.isDigit() }.take(11); cpfError = null; onErrorDismiss() }
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        CustomOutlinedField(
                                            value = telefone, label = "Telefone", icon = Icons.Default.Phone, placeholder = "(11) 99999-9999",
                                            keyboardType = KeyboardType.Phone,
                                            visualTransformation = MaskVisualTransformation("(##) #####-####"),
                                            isError = telefoneError != null, errorMessage = telefoneError,
                                            onValueChange = { telefone = it.filter { c -> c.isDigit() }.take(11); telefoneError = null; onErrorDismiss() }
                                        )
                                    }
                                }
                                CadastroStep.SEGURANCA -> {
                                    val hasMinLen = senha.length >= 8
                                    val hasUpper = senha.any { it.isUpperCase() }
                                    val hasLower = senha.any { it.isLowerCase() }
                                    val hasNumber = senha.any { it.isDigit() }
                                    val hasSpecial = senha.any { !it.isLetterOrDigit() }

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        CustomOutlinedField(
                                            value = senha, label = "Senha", icon = Icons.Default.Lock, placeholder = "Crie sua senha",
                                            keyboardType = KeyboardType.Password, isPassword = true,
                                            passwordVisible = senhaVisivel, onVisibilityChange = { senhaVisivel = it },
                                            isError = senhaError != null, errorMessage = senhaError,
                                            onValueChange = { senha = it; senhaError = null; onErrorDismiss() }
                                        )
                                        
                                        PasswordStrengthIndicator(
                                            hasMinLen = hasMinLen,
                                            hasUpper = hasUpper,
                                            hasLower = hasLower,
                                            hasNumber = hasNumber,
                                            hasSpecial = hasSpecial
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        CustomOutlinedField(
                                            value = confirmarSenha, label = "Confirmar Senha", icon = Icons.Default.Lock, placeholder = "Repita a senha",
                                            keyboardType = KeyboardType.Password, isPassword = true,
                                            passwordVisible = confirmarSenhaVisivel, onVisibilityChange = { confirmarSenhaVisivel = it },
                                            isError = confirmarSenhaError != null, errorMessage = confirmarSenhaError,
                                            onValueChange = { confirmarSenha = it; confirmarSenhaError = null; onErrorDismiss() }
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = aceitaTermos,
                                                onCheckedChange = { aceitaTermos = it; termosError = null },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = colors.primary, 
                                                    uncheckedColor = if (termosError != null) colors.errorBackground else colors.mediumGray
                                                )
                                            )
                                            Text(text = "Li e aceito os ", fontSize = 12.sp, color = colors.textSecondary)
                                            Text(text = "Termos de Uso", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.primary, modifier = Modifier.clickable { })
                                        }
                                        if (termosError != null) {
                                            Text(text = termosError!!, color = colors.errorText, fontSize = 11.sp, modifier = Modifier.padding(start = 12.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Botão de Avançar / Criar Conta
                        Button(
                            onClick = {
                                when (currentStep) {
                                    CadastroStep.DADOS_PESSOAIS -> {
                                        var valid = true
                                        if (nome.isBlank()) { nomeError = "O nome é obrigatório"; valid = false }
                                        if (email.isBlank() || !email.contains("@")) { emailError = "E-mail inválido"; valid = false }
                                        if (valid) currentStep = CadastroStep.CONTATO
                                    }
                                    CadastroStep.CONTATO -> {
                                        var valid = true
                                        if (cpf.length != 11) { cpfError = "CPF incompleto"; valid = false }
                                        if (telefone.length < 10) { telefoneError = "Telefone incompleto"; valid = false }
                                        if (valid) currentStep = CadastroStep.SEGURANCA
                                    }
                                    CadastroStep.SEGURANCA -> {
                                        var valid = true
                                        val hasMinLen = senha.length >= 8
                                        val hasUpper = senha.any { it.isUpperCase() }
                                        val hasLower = senha.any { it.isLowerCase() }
                                        val hasNumber = senha.any { it.isDigit() }
                                        val hasSpecial = senha.any { !it.isLetterOrDigit() }
                                        
                                        if (!hasMinLen || !hasUpper || !hasLower || !hasNumber || !hasSpecial) {
                                            senhaError = "A senha não cumpre os requisitos acima"
                                            valid = false
                                        }
                                        if (senha != confirmarSenha || confirmarSenha.isBlank()) { 
                                            confirmarSenhaError = "Senhas não conferem"
                                            valid = false 
                                        }
                                        if (!aceitaTermos) { 
                                            termosError = "Você precisa aceitar os termos"
                                            valid = false 
                                        }
                                        
                                        if (valid) {
                                            onRegister(nome.trim(), email.trim(), senha, cpf, telefone)
                                        }
                                    }
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
                                    text = if (currentStep == CadastroStep.SEGURANCA) "Concluir Cadastro" else "Próximo",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textOnPrimary
                                )
                            }
                        }
                    }
                }

                // Link voltar ao login
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp)
                ) {
                    Text(text = "Já tem uma conta? ", fontSize = 14.sp, color = colors.textSecondary)
                    Text(
                        text = "Faça login",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                        modifier = Modifier.clickable { onBackToLogin() }
                    )
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

@Composable
fun PasswordStrengthIndicator(
    hasMinLen: Boolean,
    hasUpper: Boolean,
    hasLower: Boolean,
    hasNumber: Boolean,
    hasSpecial: Boolean
) {
    val colors = LocalPedidosColors.current
    val rulesMet = listOf(hasMinLen, hasUpper, hasLower, hasNumber, hasSpecial).count { it }

    val missingRules = mutableListOf<String>()
    if (!hasMinLen) missingRules.add("8+ caracteres")
    if (!hasUpper) missingRules.add("maiúscula")
    if (!hasLower) missingRules.add("minúscula")
    if (!hasNumber) missingRules.add("número")
    if (!hasSpecial) missingRules.add("símbolo")

    val barColor = when (rulesMet) {
        0, 1, 2 -> Color(0xFFF44336)
        3, 4 -> Color(0xFFFF9800)
        5 -> Color(0xFF4CAF50)
        else -> colors.inputBorder
    }

    val textMessage = when (rulesMet) {
        0 -> "Use 8+ caracteres, maiúscula, minúscula, número e símbolo."
        5 -> "Senha forte e válida!"
        else -> "Falta: ${missingRules.joinToString(", ")}"
    }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 1..5) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (i <= rulesMet) barColor else colors.inputBorder)
                )
            }
        }
        Text(
            text = textMessage,
            color = if (rulesMet == 5) Color(0xFF4CAF50) else colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = if (rulesMet == 5) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(top = 6.dp, start = 2.dp)
        )
    }
}

@Composable
fun CustomOutlinedField(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    visualTransformation: VisualTransformation? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    onVisibilityChange: (Boolean) -> Unit = {},
    onValueChange: (String) -> Unit
) {
    val colors = LocalPedidosColors.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 13.sp, color = colors.mediumGray) },
            leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.primary) },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { onVisibilityChange(!passwordVisible) }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.iconGray
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else visualTransformation ?: VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            isError = isError,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = colors.inputBorder,
                focusedBorderColor = colors.primary,
                cursorColor = colors.primary,
                focusedTextColor = colors.textInput,
                unfocusedTextColor = colors.textInput,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                errorBorderColor = colors.errorBackground,
                errorCursorColor = colors.errorBackground,
                errorTextColor = colors.textInput,
                errorLeadingIconColor = colors.primary
            )
        )
        if (isError && errorMessage != null) {
            Text(text = errorMessage, color = colors.errorText, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CadastroScreenPreview() {
    PedidosTheme {
        CadastroScreen()
    }
}

class MaskVisualTransformation(private val mask: String) : VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): TransformedText {
        val maskPounds = mask.count { it == '#' }
        val trimmed = if (text.text.length > maskPounds) text.text.take(maskPounds) else text.text
        var out = ""
        var maskIndex = 0
        val originalToTransformed = mutableListOf<Int>()
        val transformedToOriginal = mutableListOf<Int>()

        for (i in trimmed.indices) {
            originalToTransformed.add(out.length)
            while (maskIndex < mask.length && mask[maskIndex] != '#') {
                out += mask[maskIndex]
                transformedToOriginal.add(i)
                maskIndex++
            }
            out += trimmed[i]
            transformedToOriginal.add(i)
            maskIndex++
        }

        originalToTransformed.add(out.length)
        if (trimmed.isNotEmpty()) {
            while (maskIndex < mask.length && mask[maskIndex] != '#') {
                out += mask[maskIndex]
                transformedToOriginal.add(trimmed.length)
                maskIndex++
            }
        }
        transformedToOriginal.add(trimmed.length)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return if (offset < 0) 0 
                else if (offset >= originalToTransformed.size) originalToTransformed.last() 
                else originalToTransformed[offset]
            }

            override fun transformedToOriginal(offset: Int): Int {
                return if (offset < 0) 0 
                else if (offset >= transformedToOriginal.size) transformedToOriginal.last() 
                else transformedToOriginal[offset]
            }
        }

        return TransformedText(androidx.compose.ui.text.AnnotatedString(out), offsetMapping)
    }
}
