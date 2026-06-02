package dev.fslab.pedidos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.fslab.pedidos.model.AdicionalOpcao
import dev.fslab.pedidos.ui.viewmodel.GrupoComOpcoes
import dev.fslab.pedidos.ui.viewmodel.PersonalizacaoUiState
import dev.fslab.pedidos.ui.viewmodel.PratoPersonalizacaoViewModel

private val Verde = Color(0xFF14B822)

@Composable
fun PratoPersonalizacaoScreen(
    onBack: () -> Unit,
    onAdicionarAoCarrinho: (dev.fslab.pedidos.ui.viewmodel.PersonalizacaoUiState.Success, Int) -> Unit,
    viewModel: PratoPersonalizacaoViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val isLight = !isSystemInDarkTheme()
    val bgColor   = if (isLight) Color(0xFFF8F9FA) else Color(0xFF0D1117)
    val cardColor = if (isLight) Color(0xFFECEFF1) else Color(0xFF161B2E)
    val textColor = if (isLight) Color(0xFF111827) else Color.White
    val subText   = textColor.copy(alpha = 0.55f)

    Scaffold(
        containerColor = bgColor,
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (uiState is PersonalizacaoUiState.Success) {
                val state = uiState as PersonalizacaoUiState.Success
                val precoTotal = viewModel.precoTotal()

                // Barra Inferior Estilo iFood
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = bgColor,
                    shadowElevation = 24.dp,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding() // Espaço da barra de navegação do Android
                            .imePadding() // Garante que suba com o teclado se necessário, embora Scaffold já ajude
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Seletor de Quantidade
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, textColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.mudarQuantidade(-1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Menos", tint = Verde, modifier = Modifier.size(18.dp))
                            }
                            
                            Text(
                                text = state.quantidade.toString(),
                                modifier = Modifier.widthIn(min = 24.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textColor,
                                textAlign = TextAlign.Center
                            )
                            
                            IconButton(
                                onClick = { viewModel.mudarQuantidade(1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Mais", tint = Verde, modifier = Modifier.size(18.dp))
                            }
                        }

                        // Botão Adicionar
                        Button(
                            onClick = { onAdicionarAoCarrinho(state, state.quantidade) },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Verde),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Adicionar", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                                Text("R$ ${String.format("%.2f", precoTotal)}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is PersonalizacaoUiState.Loading,
                is PersonalizacaoUiState.Idle -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Verde
                    )
                }

                is PersonalizacaoUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.message, color = subText, fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = Verde)
                        ) { Text("Voltar") }
                    }
                }

                is PersonalizacaoUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        // Foto
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(270.dp)) {
                                AsyncImage(
                                    model = state.prato.fotoPrato?.takeIf { it.isNotBlank() }
                                        ?: "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=800&auto=format&fit=crop",
                                    contentDescription = state.prato.nome,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, bgColor.copy(alpha = 0.9f)))))
                                
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(14.dp).size(40.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.55f))
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        // Info do prato
                        item {
                            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                                Text(state.prato.nome, color = textColor, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                                if (!state.prato.descricao.isNullOrBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(state.prato.descricao, color = subText, fontSize = 14.sp, lineHeight = 21.sp)
                                }
                                Spacer(Modifier.height(12.dp))
                                Text("A partir de R$ ${String.format("%.2f", state.prato.preco)}", color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }

                        // Grupos
                        state.grupos.forEach { gc ->
                            val selectedCount = state.selecoes[gc.grupo.id]?.size ?: 0
                            item { PersonalizacaoGrupoHeader(gc, selectedCount, cardColor, textColor, subText) }
                            items(gc.opcoes) { opcao ->
                                val selecionado = state.selecoes[gc.grupo.id]?.contains(opcao.id) == true
                                PersonalizacaoOpcaoRow(opcao, gc.grupo.max == 1, selecionado, textColor, subText) {
                                    viewModel.selecionar(gc.grupo.id, opcao.id, gc.grupo.max)
                                }
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = textColor.copy(alpha = 0.06f))
                            }
                            item { Spacer(Modifier.height(12.dp)) }
                        }

                        // Observação
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Notes, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
                                    Text("Alguma observação?", color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = state.observacao,
                                    onValueChange = { viewModel.aoMudarObservacao(it) },
                                    modifier = Modifier.fillMaxWidth().height(120.dp), // Aumentado um pouco
                                    placeholder = { Text("Ex: Tirar a cebola, maionese à parte, etc.", fontSize = 13.sp, color = subText) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Verde,
                                        unfocusedBorderColor = textColor.copy(alpha = 0.1f),
                                        cursorColor = Verde
                                    ),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = textColor)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonalizacaoGrupoHeader(gc: GrupoComOpcoes, selectedCount: Int, cardColor: Color, textColor: Color, subText: Color) {
    val completo = selectedCount >= gc.grupo.min && gc.grupo.min > 0
    val badgeBg  = if (completo) Verde.copy(alpha = 0.15f) else textColor.copy(alpha = 0.08f)
    val badgeText = if (completo) Verde else textColor.copy(alpha = 0.6f)

    Row(
        modifier = Modifier.fillMaxWidth().background(cardColor).padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(gc.grupo.nome, color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(2.dp))
            Text(if (gc.grupo.obrigatorio) "OBRIGATÓRIO" else "OPCIONAL", color = if (gc.grupo.obrigatorio) Color(0xFFEF4444) else subText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(badgeBg).padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text("$selectedCount/${gc.grupo.max}", color = badgeText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PersonalizacaoOpcaoRow(opcao: AdicionalOpcao, isRadio: Boolean, selecionado: Boolean, textColor: Color, subText: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(textColor.copy(alpha = 0.04f)).border(1.dp, if (selecionado) Verde.copy(alpha = 0.3f) else textColor.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (!opcao.fotoAdicional.isNullOrBlank()) {
                AsyncImage(model = opcao.fotoAdicional, contentDescription = opcao.nome, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Icon(Icons.Default.Check, contentDescription = null, tint = textColor.copy(alpha = 0.1f), modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(opcao.nome, color = if (selecionado) Verde else textColor, fontSize = 15.sp, fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (opcao.preco > 0.0) {
                Spacer(Modifier.height(2.dp))
                Text("+ R$ ${String.format("%.2f", opcao.preco)}", color = if (selecionado) Verde.copy(alpha = 0.8f) else subText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        if (isRadio) RadioBall(selecionado) else CheckBall(selecionado)
    }
}

@Composable
private fun RadioBall(selecionado: Boolean) {
    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(if (selecionado) Verde else Color.Transparent).border(2.dp, if (selecionado) Verde else Color.Gray.copy(0.4f), CircleShape), contentAlignment = Alignment.Center) {
        if (selecionado) Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color.White))
    }
}

@Composable
private fun CheckBall(selecionado: Boolean) {
    Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(6.dp)).background(if (selecionado) Verde else Color.Transparent).border(2.dp, if (selecionado) Verde else Color.Gray.copy(0.4f), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
        if (selecionado) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
    }
}
