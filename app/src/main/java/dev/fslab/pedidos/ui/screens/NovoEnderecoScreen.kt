package dev.fslab.pedidos.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import dev.fslab.pedidos.ui.viewmodel.EnderecoUiState
import dev.fslab.pedidos.ui.viewmodel.EnderecoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovoEnderecoScreen(
    usuarioId: String,
    enderecoId: String? = null,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: EnderecoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalPedidosColors.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val isEditMode = enderecoId != null
    val enderecoAtual = remember(enderecoId, viewModel.enderecos.value) {
        viewModel.enderecos.value.find { it.id == enderecoId }
    }

    var label by remember(enderecoAtual) { mutableStateOf(enderecoAtual?.label ?: "") }
    var cep by remember(enderecoAtual) { mutableStateOf(enderecoAtual?.cep ?: "") }
    var rua by remember(enderecoAtual) { mutableStateOf(enderecoAtual?.rua ?: "") }
    var numero by remember(enderecoAtual) { mutableStateOf(enderecoAtual?.numero ?: "") }
    var bairro by remember(enderecoAtual) { mutableStateOf(enderecoAtual?.bairro ?: "") }
    var complemento by remember(enderecoAtual) { mutableStateOf(enderecoAtual?.complemento ?: "") }
    var cidade by remember(enderecoAtual) { mutableStateOf(enderecoAtual?.cidade ?: "") }
    var estado by remember(enderecoAtual) { mutableStateOf(enderecoAtual?.estado ?: "") }
    var principal by remember(enderecoAtual) { mutableStateOf(enderecoAtual?.principal ?: false) }

    val isFormValid = cep.replace("-", "").length == 8 && rua.isNotBlank() && numero.isNotBlank() && 
                     bairro.isNotBlank() && cidade.isNotBlank() && estado.isNotBlank()

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    // Configuração inicial do Mapa
    val defaultLocation = LatLng(-23.5505, -46.6333) // São Paulo
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }

    // Permissão de Localização
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || 
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
                        viewModel.buscarEnderecoPorCoordenadas(context, it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) {
                // Ignore
            }
        }
    }

    fun pegarLocalizacaoAtual() {
        val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
        if (hasFineLocation || hasCoarseLocation) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
                        viewModel.buscarEnderecoPorCoordenadas(context, it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) {}
        } else {
            locationPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    // Carrega a localização atual assim que abre a tela
    LaunchedEffect(Unit) {
        val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFineLocation || hasCoarseLocation) {
            pegarLocalizacaoAtual()
        }
    }

    // Efeito para buscar endereço quando o mapa parar de mover
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val target = cameraPositionState.position.target
            viewModel.buscarEnderecoPorCoordenadas(context, target.latitude, target.longitude)
        }
    }

    // Efeito para preencher campos
    LaunchedEffect(uiState) {
        if (uiState is EnderecoUiState.CepLoaded) {
            val data = (uiState as EnderecoUiState.CepLoaded).data
            rua = data.logradouro ?: rua
            bairro = data.bairro ?: bairro
            cidade = data.localidade ?: cidade
            estado = data.uf ?: estado
            val novoCep = data.cep ?: ""
            if (novoCep.isNotBlank()) cep = novoCep
            viewModel.resetState()
        }
    }

    // Sheet Setup
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
    
    // O formulário deslizará e deixará 300dp do mapa visível (+/- 64dp de app bar)
    val screenHeight = configuration.screenHeightDp.dp
    val peekHeight = maxOf(400.dp, screenHeight - 364.dp) // Garante que a folha não suma em telas pequenas

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isEditMode) "Editar Endereço" else "Novo Endereço", 
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
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
        sheetPeekHeight = peekHeight,
        sheetContainerColor = colors.background,
        sheetShadowElevation = 16.dp, // Sombra para destacar o raio do bottom sheet!
        sheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        sheetDragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            )
        },
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp), // Espaço no fim da lista
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Detalhes da Entrega",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
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

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    EnderecoTextField(
                        value = numero,
                        onValueChange = { numero = it },
                        label = "Número",
                        placeholder = "Ex: 123",
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

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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

                EnderecoTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = "Apelido (Casa, Trabalho)",
                    placeholder = "Opcional"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
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
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (enderecoId != null) {
                            viewModel.atualizarEndereco(
                                usuarioId = usuarioId,
                                enderecoId = enderecoId,
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
                        } else {
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
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    enabled = isFormValid && uiState !is EnderecoUiState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        disabledContainerColor = colors.primary.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (uiState is EnderecoUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Text(
                            "SALVAR ENDEREÇO",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(80.dp)) // Espaço extra pra deslizar até o fim
            }
        },
        containerColor = colors.background
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Fundo: O Mapa (ocupa tudo, mas ajusta o topo pra não ficar sob a AppBar)
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false, compassEnabled = false),
                properties = MapProperties(isMyLocationEnabled = false),
                contentPadding = PaddingValues(bottom = peekHeight) // Ajusta o centro óptico do mapa!
            )
            
            // Container para o Pino e o FAB respeitando o innerPadding (que já inclui o bottom do sheet!)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Sombra do pino (Glow) sem fundo branco
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = null,
                    tint = colors.primary.copy(alpha = 0.4f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(42.dp)
                        .offset(y = (-18).dp)
                        .blur(6.dp)
                )
                
                // Pino central
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = "Pino",
                    tint = colors.primary,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(42.dp)
                        .offset(y = (-21).dp)
                )
                
                // Botão de Localização Atual
                FloatingActionButton(
                    onClick = { pegarLocalizacaoAtual() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp, end = 16.dp)
                        .size(56.dp),
                    containerColor = colors.primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Icon(Icons.Filled.MyLocation, "Localização Atual", modifier = Modifier.size(28.dp))
                }
            }

            if (uiState is EnderecoUiState.Error) {
                Snackbar(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = innerPadding.calculateTopPadding() + 16.dp, start = 16.dp, end = 16.dp),

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
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { 
                Text(
                    placeholder, 
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 15.sp
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
            shape = RoundedCornerShape(14.dp),
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
