package dev.fslab.pedidos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import dev.fslab.pedidos.ui.viewmodel.EnderecoUiState
import dev.fslab.pedidos.ui.viewmodel.EnderecoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovoEnderecoScreen(
    usuarioId: String,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: EnderecoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalPedidosColors.current

    var label by remember { mutableStateOf("") }
    var cep by remember { mutableStateOf("") }
    var rua by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var bairro by remember { mutableStateOf("") }
    var complemento by remember { mutableStateOf("") }
    var cidade by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    var principal by remember { mutableStateOf(false) }

    val isFormValid = cep.replace("-", "").length == 8 && rua.isNotBlank() && numero.isNotBlank() && 
                     bairro.isNotBlank() && cidade.isNotBlank() && estado.isNotBlank()

    // Efeito para preencher campos quando o CEP for carregado
    LaunchedEffect(uiState) {
        if (uiState is EnderecoUiState.CepLoaded) {
            val data = (uiState as EnderecoUiState.CepLoaded).data
            rua = data.logradouro ?: ""
            bairro = data.bairro ?: ""
            cidade = data.localidade ?: ""
            estado = data.uf ?: ""
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Novo Endereço", 
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    scrolledContainerColor = colors.background
                )
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Preencha os dados para entrega",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )

                EnderecoTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = "Apelido (Ex: Casa, Trabalho)",
                    placeholder = "Opcional"
                )

                EnderecoTextField(
                    value = cep,
                    onValueChange = { 
                        val newCep = it.take(9)
                        cep = newCep
                        if (newCep.replace("-", "").length == 8) {
                            viewModel.buscarCep(newCep)
                        }
                    },
                    label = "CEP",
                    placeholder = "00000-000",
                    keyboardType = KeyboardType.Number,
                    isLoading = uiState is EnderecoUiState.CepLoading
                )

                EnderecoTextField(
                    value = rua,
                    onValueChange = { rua = it },
                    label = "Rua / Logradouro",
                    placeholder = "Nome da rua"
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EnderecoTextField(
                        value = numero,
                        onValueChange = { numero = it },
                        label = "Número",
                        placeholder = "123",
                        modifier = Modifier.weight(1f)
                    )
                    EnderecoTextField(
                        value = complemento,
                        onValueChange = { complemento = it },
                        label = "Complemento",
                        placeholder = "Apto 101",
                        modifier = Modifier.weight(1.5f)
                    )
                }

                EnderecoTextField(
                    value = bairro,
                    onValueChange = { bairro = it },
                    label = "Bairro",
                    placeholder = "Seu bairro"
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EnderecoTextField(
                        value = cidade,
                        onValueChange = { cidade = it },
                        label = "Cidade",
                        placeholder = "Sua cidade",
                        modifier = Modifier.weight(2f)
                    )
                    EnderecoTextField(
                        value = estado,
                        onValueChange = { estado = it },
                        label = "UF",
                        placeholder = "RO",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = principal,
                        onCheckedChange = { principal = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = colors.primary,
                            uncheckedColor = Color.White.copy(alpha = 0.5f)
                        )
                    )
                    Text(
                        text = "Definir como endereço principal",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.criarEndereco(
                            usuarioId = usuarioId,
                            label = label,
                            cep = cep,
                            rua = rua,
                            numero = numero,
                            bairro = bairro,
                            complemento = complemento,
                            cidade = cidade,
                            estado = estado,
                            principal = principal,
                            onSuccess = onSuccess
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = isFormValid && uiState !is EnderecoUiState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (uiState is EnderecoUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Salvar Endereço",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            if (uiState is EnderecoUiState.Error) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.resetState() }) {
                            Text("OK", color = colors.primary)
                        }
                    }
                ) {
                    Text((uiState as EnderecoUiState.Error).message)
                }
            }
        }
    }
}

@Composable
fun EnderecoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isLoading: Boolean = false
) {
    val colors = LocalPedidosColors.current
    val cardColor = colors.surface

    Column(modifier = modifier) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { 
                Text(
                    placeholder, 
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 14.sp
                ) 
            },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = colors.primary,
                        strokeWidth = 2.dp
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = cardColor,
                unfocusedContainerColor = cardColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = colors.primary,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
    }
}
