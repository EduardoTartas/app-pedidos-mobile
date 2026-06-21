package dev.fslab.pedidos.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import dev.fslab.pedidos.model.User
import dev.fslab.pedidos.ui.components.DesativarContaDialog
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(
    user: User?,
    onBack: () -> Unit,
    onDeactivateAccount: (onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onAlterarSenha: (onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onSaveProfile: (nome: String, telefone: String, cpf: String?, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit
) {
    val colors = LocalPedidosColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun formatCpf(digits: String): String {
        val d = digits.filter { it.isDigit() }.take(11)
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
        val d = digits.filter { it.isDigit() }.take(11)
        return buildString {
            d.forEachIndexed { i, c ->
                when (i) {
                    0 -> append('(')
                    2 -> append(") ")
                    7 -> if (d.length == 11) append('-')
                }
                if (i == 6 && d.length == 10) append('-')
                append(c)
            }
        }
    }
    
    var editNome by remember { mutableStateOf(user?.nome ?: "") }
    var editTelefone by remember { mutableStateOf(TextFieldValue(formatTelefone(user?.telefone ?: ""))) }
    
    val isCpfRegistered = !user?.cpf.isNullOrBlank()
    var editCpf by remember { mutableStateOf(TextFieldValue(formatCpf(user?.cpf ?: ""))) }
    
    val readOnlyEmail = user?.email ?: ""
    
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var showConfirmSaveDialog by remember { mutableStateOf(false) }
    var showConfirmPasswordDialog by remember { mutableStateOf(false) }
    var showPasswordEmailSentDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> 
            if (uri != null) {
                Toast.makeText(context, "Foto selecionada. Upload em desenvolvimento.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Editar Perfil", 
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.textPrimary
                )
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            
            // Foto de Perfil
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(colors.mediumGray)
                        .border(width = 3.dp, color = colors.primary.copy(alpha = 0.55f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!user?.fotoPerfil.isNullOrBlank()) {
                        AsyncImage(
                            model = user?.fotoPerfil,
                            contentDescription = "Foto do usuario",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Foto do usuario",
                            tint = colors.iconGray,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { 
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.primary)
                        .border(width = 2.dp, color = colors.surface, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = "Alterar foto do usuario",
                        tint = colors.textOnPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Formulário em Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Informações Básicas",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (!errorMessage.isNullOrBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = colors.error.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = errorMessage!!,
                                    fontSize = 13.sp,
                                    color = colors.error,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { errorMessage = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Fechar erro", tint = colors.error)
                                }
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = editNome,
                        onValueChange = { editNome = it },
                        label = { Text("Nome Completo") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = colors.primary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.inputBorder
                        )
                    )

                    OutlinedTextField(
                        value = readOnlyEmail,
                        onValueChange = { },
                        label = { Text("E-mail") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = colors.iconGray) },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = colors.textSecondary,
                            disabledBorderColor = colors.inputBorder.copy(alpha = 0.5f),
                            disabledLabelColor = colors.textSecondary
                        )
                    )
                    
                    OutlinedTextField(
                        value = editCpf,
                        onValueChange = { newValue -> 
                            if (!isCpfRegistered) {
                                val newDigits = newValue.text.filter { it.isDigit() }.take(11)
                                val formatted = formatCpf(newDigits)
                                editCpf = TextFieldValue(
                                    text = formatted,
                                    selection = TextRange(formatted.length)
                                )
                            }
                        },
                        label = { Text("CPF") },
                        leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null, tint = if (isCpfRegistered) colors.iconGray else colors.primary) },
                        enabled = !isCpfRegistered,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.inputBorder,
                            disabledTextColor = colors.textSecondary,
                            disabledBorderColor = colors.inputBorder.copy(alpha = 0.5f),
                            disabledLabelColor = colors.textSecondary
                        )
                    )
                    
                    OutlinedTextField(
                        value = editTelefone,
                        onValueChange = { newValue -> 
                            val newDigits = newValue.text.filter { it.isDigit() }.take(11)
                            val formatted = formatTelefone(newDigits)
                            editTelefone = TextFieldValue(
                                text = formatted,
                                selection = TextRange(formatted.length)
                            )
                        },
                        label = { Text("Telefone") },
                        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = colors.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.inputBorder
                        )
                    )
                    
                    Button(
                        onClick = {
                            val justDigitsPhone = editTelefone.text.filter { it.isDigit() }
                            val justDigitsCpf = editCpf.text.filter { it.isDigit() }
                            
                            if (editNome.isBlank()) {
                                errorMessage = "O nome não pode estar vazio."
                            } else if (justDigitsPhone.length !in 10..11) {
                                errorMessage = "Telefone inválido."
                            } else if (!isCpfRegistered && justDigitsCpf.isNotEmpty() && justDigitsCpf.length != 11) {
                                errorMessage = "CPF deve ter 11 dígitos."
                            } else {
                                errorMessage = null
                                showConfirmSaveDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colors.textOnPrimary, strokeWidth = 2.dp)
                        } else {
                            Text("SALVAR ALTERAÇÕES", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            // Ações da Conta em Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Ações da Conta",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedButton(
                        onClick = { showConfirmPasswordDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.inputBorder)
                    ) {
                        Icon(Icons.Filled.LockReset, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ALTERAR SENHA", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    
                    OutlinedButton(
                        onClick = { showDeactivateDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red,
                            containerColor = Color.Red.copy(alpha = 0.05f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("DESATIVAR CONTA", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(30.dp))
        }

        if (showDeactivateDialog) {
            DesativarContaDialog(
                onDismiss = { showDeactivateDialog = false },
                onConfirm = { 
                    showDeactivateDialog = false
                    isLoading = true
                    onDeactivateAccount(
                        { isLoading = false },
                        { error ->
                            isLoading = false
                            errorMessage = error
                        }
                    )
                }
            )
        }

        if (showConfirmSaveDialog) {
            Dialog(onDismissRequest = { showConfirmSaveDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = colors.background,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(colors.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = colors.primary, modifier = Modifier.size(40.dp))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Confirmar Alterações", color = colors.textPrimary, fontWeight = FontWeight.Black, fontSize = 22.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Deseja salvar as alterações feitas no seu perfil?", color = colors.textSecondary, fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = {
                                showConfirmSaveDialog = false
                                isLoading = true
                                val justDigitsPhone = editTelefone.text.filter { it.isDigit() }
                                val justDigitsCpf = editCpf.text.filter { it.isDigit() }
                                val cpfToSave = if (!isCpfRegistered && justDigitsCpf.isNotEmpty()) justDigitsCpf else null
                                onSaveProfile(
                                    editNome.trim(), 
                                    justDigitsPhone, 
                                    cpfToSave,
                                    { isLoading = false; showSuccessDialog = true },
                                    { error -> isLoading = false; errorMessage = error }
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text("SALVAR", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(
                            onClick = { showConfirmSaveDialog = false },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text("CANCELAR", color = colors.textSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        if (showSuccessDialog) {
            Dialog(onDismissRequest = { showSuccessDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = colors.background,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(colors.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = colors.primary, modifier = Modifier.size(40.dp))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Sucesso!", color = colors.textPrimary, fontWeight = FontWeight.Black, fontSize = 22.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Seu perfil foi atualizado com sucesso.", color = colors.textSecondary, fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { showSuccessDialog = false },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text("OK", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        if (showConfirmPasswordDialog) {
            Dialog(onDismissRequest = { showConfirmPasswordDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = colors.background,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(colors.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.LockReset, contentDescription = null, tint = colors.primary, modifier = Modifier.size(40.dp))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Alterar Senha", color = colors.textPrimary, fontWeight = FontWeight.Black, fontSize = 22.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Deseja realmente alterar sua senha? Enviaremos um código de verificação para o seu e-mail.", color = colors.textSecondary, fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = {
                                showConfirmPasswordDialog = false
                                isLoading = true
                                onAlterarSenha(
                                    {
                                        isLoading = false
                                        showPasswordEmailSentDialog = true
                                    },
                                    { error ->
                                        isLoading = false
                                        errorMessage = error
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text("SIM", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(
                            onClick = { showConfirmPasswordDialog = false },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text("NÃO", color = colors.textSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        if (showPasswordEmailSentDialog) {
            Dialog(onDismissRequest = { showPasswordEmailSentDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = colors.background,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(colors.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Email, contentDescription = null, tint = colors.primary, modifier = Modifier.size(40.dp))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("E-mail Enviado!", color = colors.textPrimary, fontWeight = FontWeight.Black, fontSize = 22.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Verifique sua caixa de entrada e siga as instruções no e-mail para redefinir sua senha com segurança.", color = colors.textSecondary, fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { showPasswordEmailSentDialog = false },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text("OK", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
