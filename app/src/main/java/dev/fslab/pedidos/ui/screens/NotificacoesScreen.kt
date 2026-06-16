package dev.fslab.pedidos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.fslab.pedidos.ui.theme.LocalPedidosColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacoesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalPedidosColors.current
    val filtros = listOf("Todas", "Pedidos", "Promoções")
    var filtroSelecionado by remember { mutableStateOf(filtros.first()) }
    val onFiltroSelecionado: (String) -> Unit = { filtro ->
        filtroSelecionado = filtro
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Notificações",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colors.background,
                    scrolledContainerColor = colors.background,
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(innerPadding)
        ) {
            NotificacoesFiltros(
                filtros = filtros,
                filtroSelecionado = filtroSelecionado,
                onFiltroSelecionado = onFiltroSelecionado
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Estrutura pronta para receber notificações.
            }
        }
    }
}

@Composable
private fun NotificacoesFiltros(
    filtros: List<String>,
    filtroSelecionado: String,
    onFiltroSelecionado: (String) -> Unit
) {
    val selectedColor = Color(0xFF22C55E)
    val unselectedColor = Color(0xFF161B2E)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            filtros.forEach { filtro ->
                val selected = filtro == filtroSelecionado

                FilterChip(
                    selected = selected,
                    onClick = { onFiltroSelecionado(filtro) },
                    label = {
                        Text(
                            text = filtro,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    shape = RoundedCornerShape(50),
                    border = null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = selectedColor,
                        selectedLabelColor = Color.White,
                        containerColor = unselectedColor,
                        labelColor = Color.White.copy(alpha = 0.82f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
