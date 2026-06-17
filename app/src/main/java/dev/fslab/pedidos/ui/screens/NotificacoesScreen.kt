package dev.fslab.pedidos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.fslab.pedidos.model.NotificationType
import dev.fslab.pedidos.model.NotificationUiModel
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import dev.fslab.pedidos.ui.viewmodel.NotificationViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacoesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = viewModel()
) {
    val colors = LocalPedidosColors.current
    val uiState by viewModel.uiState.collectAsState()
    val errorMessage = uiState.errorMessage
    val selectedCount = uiState.selectedNotificationIds.size
    val isSelectionMode = selectedCount > 0

    LaunchedEffect(Unit) {
        viewModel.carregarNotificacoes()
    }

    val filtros = listOf(
        null to "Todas",
        NotificationType.ORDER to "Pedidos",
        NotificationType.PROMOTION to "Promoções",
        NotificationType.SYSTEM to "Sistema"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isSelectionMode) {
                            "$selectedCount selecionada${if (selectedCount > 1) "s" else ""}"
                        } else if (uiState.unreadCount > 0) {
                            "Notificações (${uiState.unreadCount})"
                        } else {
                            "Notificações"
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isSelectionMode) {
                                viewModel.limparSelecaoParaExclusao()
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSelectionMode) {
                                Icons.Filled.Close
                            } else {
                                Icons.AutoMirrored.Filled.ArrowBack
                            },
                            contentDescription = if (isSelectionMode) "Cancelar seleção" else "Voltar",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        TextButton(
                            onClick = viewModel::selecionarTodasFiltradasParaExclusao,
                            enabled = !uiState.isDeleting
                        ) {
                            Text(
                                text = "Todas",
                                color = Color(0xFF22C55E),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (uiState.isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(horizontal = 14.dp)
                                    .size(20.dp),
                                color = Color(0xFF22C55E),
                                strokeWidth = 2.dp
                            )
                        } else {
                            IconButton(onClick = viewModel::deletarNotificacoesSelecionadas) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Apagar selecionadas",
                                    tint = Color(0xFFFF6B7A)
                                )
                            }
                        }
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
                filtroSelecionado = uiState.selectedCategory,
                onFiltroSelecionado = viewModel::filtrarPorCategoria
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF22C55E))
                    }
                }

                errorMessage != null && uiState.notifications.isEmpty() -> {
                    NotificationStateMessage(
                        title = "Não foi possível carregar",
                        description = errorMessage,
                        actionLabel = "Tentar novamente",
                        onAction = { viewModel.carregarNotificacoes() }
                    )
                }

                uiState.filteredNotifications.isEmpty() -> {
                    NotificationStateMessage(
                        title = "Nenhuma notificação",
                        description = if (uiState.selectedCategory == null) {
                            "Você ainda não recebeu notificações."
                        } else {
                            "Não há notificações nesta categoria."
                        }
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        errorMessage?.let { message ->
                            item {
                                NotificationInlineError(
                                    message = message,
                                    onRetry = { viewModel.carregarNotificacoes(silent = true) }
                                )
                            }
                        }

                        items(
                            items = uiState.filteredNotifications,
                            key = { it.id }
                        ) { notificacao ->
                            NotificationItemCard(
                                icon = notificacao.type.icon,
                                title = notificacao.title,
                                description = notificacao.description,
                                date = notificacao.createdAtLabel(),
                                isUnread = !notificacao.isRead,
                                isSelectionMode = isSelectionMode,
                                isSelected = notificacao.id in uiState.selectedNotificationIds,
                                onClick = {
                                    if (isSelectionMode) {
                                        viewModel.alternarSelecaoParaExclusao(notificacao.id)
                                    } else {
                                        viewModel.marcarComoLida(notificacao.id)
                                    }
                                },
                                onLongClick = {
                                    viewModel.alternarSelecaoParaExclusao(notificacao.id)
                                }
                            )
                        }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationItemCard(
    icon: ImageVector,
    title: String,
    description: String,
    date: String,
    isUnread: Boolean,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val green = Color(0xFF22C55E)
    val cardColor = Color(0xFF161B2E)
    val shape = RoundedCornerShape(18.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isSelected) green else Color.Transparent,
                shape = shape
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = shape,
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

            Column(
                horizontalAlignment = Alignment.End
            ) {
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

                if (isSelectionMode) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = if (isSelected) green else Color.White.copy(alpha = 0.08f),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) green else Color.White.copy(alpha = 0.28f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selecionada",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private val NotificationType.icon: ImageVector
    get() = when (this) {
        NotificationType.ORDER -> Icons.Filled.TwoWheeler
        NotificationType.PROMOTION -> Icons.Filled.CardGiftcard
        NotificationType.SYSTEM -> Icons.Filled.Info
    }

private fun NotificationUiModel.createdAtLabel(): String {
    val createdInstant = runCatching { Instant.parse(createdAt) }.getOrNull()
        ?: return createdAt
    val minutes = ChronoUnit.MINUTES.between(createdInstant, Instant.now()).coerceAtLeast(0)

    return when {
        minutes < 1 -> "Agora"
        minutes < 60 -> "${minutes}min"
        minutes < 24 * 60 -> "${minutes / 60}h atrás"
        else -> createdInstant
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("dd MMM.", Locale.forLanguageTag("pt-BR")))
    }
}

@Composable
private fun NotificationStateMessage(
    title: String,
    description: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.68f),
                style = MaterialTheme.typography.bodyMedium
            )
            if (actionLabel != null) {
                TextButton(onClick = onAction) {
                    Text(
                        text = actionLabel,
                        color = Color(0xFF22C55E),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationInlineError(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3D1A2E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = Color(0xFFFF8A9B),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) {
                Text(
                    text = "Tentar",
                    color = Color(0xFF22C55E),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun NotificacoesFiltros(
    filtros: List<Pair<NotificationType?, String>>,
    filtroSelecionado: NotificationType?,
    onFiltroSelecionado: (NotificationType?) -> Unit
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
            filtros.forEach { (categoria, label) ->
                val selected = categoria == filtroSelecionado

                FilterChip(
                    selected = selected,
                    onClick = { onFiltroSelecionado(categoria) },
                    label = {
                        Text(
                            text = label,
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
