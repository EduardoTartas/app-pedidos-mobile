package dev.fslab.pedidos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.fslab.pedidos.ui.theme.LocalPedidosColors

data class NotificationUiModel(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val date: String,
    val isUnread: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacoesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalPedidosColors.current
    val filtros = listOf("Todas", "Pedidos", "Promoções")
    var filtroSelecionado by remember { mutableStateOf(filtros.first()) }
    val notificacoes = remember {
        listOf(
            NotificationUiModel(
                icon = Icons.Filled.CardGiftcard,
                title = "Cupom de R$ 20 disponível",
                description = "Aproveite seu cupom de desconto para jantar hoje! Válido para pedidos acima de R$ 60.",
                date = "2h atrás",
                isUnread = true
            ),
            NotificationUiModel(
                icon = Icons.Filled.CreditCard,
                title = "Reembolso processado",
                description = "O reembolso referente ao pedido #3245 foi aprovado.",
                date = "14 Ago.",
                isUnread = false
            )
        )
    }
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
                item {
                    HighlightOrderNotificationCard(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                items(
                    items = notificacoes,
                    key = { "${it.title}-${it.date}" }
                ) { notificacao ->
                    Column {
                        Spacer(modifier = Modifier.height(14.dp))
                        NotificationItemCard(
                            icon = notificacao.icon,
                            title = notificacao.title,
                            description = notificacao.description,
                            date = notificacao.date,
                            isUnread = notificacao.isUnread
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HighlightOrderNotificationCard(
    modifier: Modifier = Modifier,
    onTrackClick: () -> Unit = {},
    onDetailsClick: () -> Unit = {}
) {
    val colors = LocalPedidosColors.current
    val green = Color(0xFF22C55E)
    val cardColor = Color(0xFF161B2E)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(green.copy(alpha = 0.14f), RoundedCornerShape(12.dp)),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.TwoWheeler,
                        contentDescription = null,
                        tint = green,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Text(
                    text = "Agora",
                    color = green,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(green.copy(alpha = 0.14f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Seu pedido saiu para entrega!",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "O entregador Emerson está a caminho com seu pedido do Burger King.",
                color = Color.White.copy(alpha = 0.74f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { 0.68f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = green,
                trackColor = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onTrackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = green,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Acompanhar",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = onDetailsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color(0xFF64748B),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Detalhes",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun NotificationItemCard(
    icon: ImageVector,
    title: String,
    description: String,
    date: String,
    isUnread: Boolean,
    modifier: Modifier = Modifier
) {
    val green = Color(0xFF22C55E)
    val cardColor = Color(0xFF161B2E)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(green.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = green,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.68f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = date,
                    color = Color.White.copy(alpha = 0.48f),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )

                if (isUnread) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(green, CircleShape)
                    )
                }
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
