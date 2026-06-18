package dev.fslab.pedidos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fslab.pedidos.model.Endereco
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import dev.fslab.pedidos.ui.viewmodel.EnderecoUiState
import dev.fslab.pedidos.ui.viewmodel.EnderecoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeusEnderecosScreen(
    usuarioId: String,
    viewModel: EnderecoViewModel,
    onBack: () -> Unit,
    onAddEndereco: () -> Unit,
    onEditEndereco: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val enderecos by viewModel.enderecos.collectAsState()
    val colors = LocalPedidosColors.current

    var enderecoParaDeletar by remember { mutableStateOf<Endereco?>(null) }

    LaunchedEffect(usuarioId) {
        viewModel.listarEnderecos(usuarioId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Meus Endereços",
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
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEndereco,
                containerColor = colors.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar Endereço")
            }
        },
        containerColor = colors.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.backgroundGradientEnd)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Subtítulo
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Gerencie seus locais para uma entrega mais rápida e precisa.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState is EnderecoUiState.Loading && enderecos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                } else if (enderecos.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.White.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhum endereço cadastrado",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(enderecos, key = { it.id ?: it.hashCode() }) { endereco ->
                            EnderecoCard(
                                endereco = endereco,
                                onEdit = { endereco.id?.let { onEditEndereco(it) } },
                                onDelete = { enderecoParaDeletar = endereco }
                            )
                        }
                    }
                }
            }
            
            enderecoParaDeletar?.let { endereco ->
                AlertDialog(
                    onDismissRequest = { enderecoParaDeletar = null },
                    containerColor = colors.surface,
                    titleContentColor = colors.textPrimary,
                    textContentColor = colors.textSecondary,
                    icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(40.dp)) },
                    title = { Text("Excluir Endereço?", fontWeight = FontWeight.Black, fontSize = 18.sp) },
                    text = { Text("Deseja realmente excluir este endereço? Essa ação não pode ser desfeita.", textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                    confirmButton = {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, start = 12.dp, end = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = { enderecoParaDeletar = null },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text("MANTER", color = colors.textTertiary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    endereco.id?.let { viewModel.deletarEndereco(usuarioId, it) }
                                    enderecoParaDeletar = null
                                },
                                modifier = Modifier.weight(1.6f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "EXCLUIR AGORA",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    },
                    dismissButton = {} // Replaced by confirmButton Row
                )
            }

            if (uiState is EnderecoUiState.Error) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
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
fun EnderecoCard(
    endereco: Endereco,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalPedidosColors.current

    // Decide icon based on label
    val labelLower = endereco.label.lowercase()
    val icon = when {
        labelLower.contains("casa") -> Icons.Filled.Home
        labelLower.contains("trabalho") || labelLower.contains("escritório") || labelLower.contains("empresa") -> Icons.Filled.Work
        else -> Icons.Filled.LocationOn
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícone à esquerda com fundo circular
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(colors.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Textos centrais
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = endereco.label.ifBlank { "Endereço" },
                        color = colors.primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    
                    if (endereco.principal) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Principal",
                            color = colors.textOnPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.primary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "${endereco.rua}, ${endereco.numero}",
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                val detalhes = buildString {
                    if (endereco.complemento.isNotBlank()) append("${endereco.complemento} • ")
                    append("${endereco.bairro}")
                }
                Text(
                    text = detalhes,
                    color = colors.textSecondary.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                val cidadeEstado = "${endereco.cidade} - ${endereco.estado}"
                Text(
                    text = cidadeEstado,
                    color = colors.textSecondary.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Botões de Ação ao lado da coluna de texto, horizontalmente
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(colors.primary.copy(alpha = 0.15f))
                        .clickable(onClick = onEdit),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar",
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Color.Red.copy(alpha = 0.15f))
                        .clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Remover",
                        tint = Color.Red.copy(alpha = 0.9f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
