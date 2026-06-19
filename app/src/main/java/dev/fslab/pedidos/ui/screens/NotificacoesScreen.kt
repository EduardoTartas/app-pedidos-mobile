package dev.fslab.pedidos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.fslab.pedidos.model.NotificationType
import dev.fslab.pedidos.model.NotificationUiModel
import dev.fslab.pedidos.ui.components.OrderOnTheWayNotificationCard
import dev.fslab.pedidos.ui.components.OrderPreparingNotificationCard
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import dev.fslab.pedidos.ui.viewmodel.NotificationViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel

private enum class OnTheWaySheetType {
    TRACKING,
    DETAILS
}

private data class OnTheWayNotificationSheet(
    val type: OnTheWaySheetType,
    val notification: NotificationUiModel
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificacoesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToPedidoDetalhes: (String) -> Unit = {},
    viewModel: NotificationViewModel = viewModel()
) {
    val colors = LocalPedidosColors.current
    val uiState by viewModel.uiState.collectAsState()
    val errorMessage = uiState.errorMessage
    val selectedCount = uiState.selectedNotificationIds.size
    val isSelectionMode = selectedCount > 0
    var activeOnTheWaySheet by remember { mutableStateOf<OnTheWayNotificationSheet?>(null) }
    val onTheWaySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.carregarNotificacoes()
    }

    val filtros = listOf(
        null to "Todas",
        NotificationType.ORDER to "Pedidos",
        NotificationType.PROMOTION to "Promoções",
        NotificationType.SYSTEM to "Sistema"
    )

    activeOnTheWaySheet?.let { sheet ->
        ModalBottomSheet(
            onDismissRequest = { activeOnTheWaySheet = null },
            sheetState = onTheWaySheetState,
            containerColor = Color(0xFF0F172A),
            contentColor = Color.White
        ) {
            when (sheet.type) {
                OnTheWaySheetType.TRACKING -> OnTheWayTrackingSheet(
                    notification = sheet.notification
                )
                OnTheWaySheetType.DETAILS -> OnTheWayDetailsSheet(
                    notification = sheet.notification
                )
            }
        }
    }

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
                                color = colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (uiState.isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(horizontal = 14.dp)
                                    .size(20.dp),
                                color = colors.primary,
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
                        CircularProgressIndicator(color = colors.primary)
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
                            val pedidoId = notificacao.pedidoIdFromNotification()
                            val onNotificationClick: () -> Unit = {
                                if (isSelectionMode) {
                                    viewModel.alternarSelecaoParaExclusao(notificacao.id)
                                } else {
                                    viewModel.marcarComoLida(notificacao.id)
                                    pedidoId?.let(onNavigateToPedidoDetalhes)
                                }
                            }
                            val onNotificationLongClick: () -> Unit = {
                                viewModel.alternarSelecaoParaExclusao(notificacao.id)
                            }

                            if (notificacao.isOnTheWayOrderNotification() && !isSelectionMode) {
                                OrderOnTheWayNotificationCard(
                                    courierName = notificacao.onTheWayCourierName(),
                                    restaurantName = notificacao.orderRestaurantName(defaultName = "Burger King"),
                                    modifier = Modifier.combinedClickable(
                                        onClick = onNotificationClick,
                                        onLongClick = onNotificationLongClick
                                    ),
                                    onTrackClick = {
                                        viewModel.marcarComoLida(notificacao.id)
                                        activeOnTheWaySheet = OnTheWayNotificationSheet(
                                            type = OnTheWaySheetType.TRACKING,
                                            notification = notificacao
                                        )
                                    },
                                    onDetailsClick = {
                                        viewModel.marcarComoLida(notificacao.id)
                                        activeOnTheWaySheet = OnTheWayNotificationSheet(
                                            type = OnTheWaySheetType.DETAILS,
                                            notification = notificacao
                                        )
                                    }
                                )
                            } else if (notificacao.isPreparingOrderNotification() && !isSelectionMode) {
                                OrderPreparingNotificationCard(
                                    restaurantName = notificacao.preparingRestaurantName(),
                                    modifier = Modifier.combinedClickable(
                                        onClick = onNotificationClick,
                                        onLongClick = onNotificationLongClick
                                    )
                                )
                            } else {
                                NotificationItemCard(
                                    icon = notificacao.type.icon,
                                    title = notificacao.title,
                                    description = notificacao.description,
                                    date = notificacao.createdAtLabel(),
                                    actionHint = if (pedidoId != null) "Toque para ver detalhes do pedido" else null,
                                    isUnread = !notificacao.isRead,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = notificacao.id in uiState.selectedNotificationIds,
                                    onClick = onNotificationClick,
                                    onLongClick = onNotificationLongClick
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
fun HighlightOrderNotificationCard(
    modifier: Modifier = Modifier,
    onTrackClick: () -> Unit = {},
    onDetailsClick: () -> Unit = {}
) {
    val colors = LocalPedidosColors.current
    val green = colors.primary
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
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
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
private fun OnTheWayTrackingSheet(
    notification: NotificationUiModel
) {
    val colors = LocalPedidosColors.current
    val green = colors.primary
    val restaurantName = notification.orderRestaurantName(defaultName = "Burger King")
    val courierName = notification.onTheWayCourierName()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        Text(
            text = "Acompanhar pedido",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Seu pedido do $restaurantName saiu para entrega.",
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(18.dp))

        DemoDeliveryMap()

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B2E)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DeliveryInfoRow(label = "Entregador", value = courierName)
                DeliveryInfoRow(label = "Restaurante", value = restaurantName)
                DeliveryInfoRow(label = "Chegada estimada", value = "10 minutos")
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Status do pedido",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        DeliveryTimelineStep(
            title = "Pedido confirmado",
            description = "O restaurante recebeu seu pedido.",
            isDone = true
        )
        DeliveryTimelineStep(
            title = "Em preparo",
            description = "A cozinha preparou os itens.",
            isDone = true
        )
        DeliveryTimelineStep(
            title = "Saiu para entrega",
            description = "O entregador está indo até você.",
            isDone = true,
            isCurrent = true
        )
        DeliveryTimelineStep(
            title = "Entregue",
            description = "Confirme o recebimento quando chegar.",
            isDone = false
        )
    }
}

@Composable
private fun OnTheWayDetailsSheet(
    notification: NotificationUiModel
) {
    val colors = LocalPedidosColors.current
    val green = colors.primary
    val restaurantName = notification.orderRestaurantName(defaultName = "Burger King")
    val courierName = notification.onTheWayCourierName()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Detalhes do pedido",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Resumo demonstrativo",
                    color = Color.White.copy(alpha = 0.58f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "A caminho",
                color = green,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(green.copy(alpha = 0.16f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B2E)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                DeliveryInfoRow(label = "Pedido", value = "#0002")
                DeliveryInfoRow(label = "Restaurante", value = restaurantName)
                DeliveryInfoRow(label = "Entregador", value = courierName)
                DeliveryInfoRow(label = "Previsão", value = "10 minutos")

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = Color.White.copy(alpha = 0.08f)
                )

                Text(
                    text = "Itens",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                OrderItemRow(quantity = "1x", name = "Combo Whopper", price = "R$ 31,90")
                OrderItemRow(quantity = "1x", name = "Batata média", price = "R$ 8,00")
                OrderItemRow(quantity = "1x", name = "Refrigerante", price = "R$ 3,00")

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = Color.White.copy(alpha = 0.08f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "R$ 42,90",
                        color = green,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoDeliveryMap(
    modifier: Modifier = Modifier
) {
    val colors = LocalPedidosColors.current
    val green = colors.primary
    val transition = rememberInfiniteTransition(label = "delivery-map")
    val progress by transition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "delivery-progress"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(168.dp)
            .background(Color(0xFF111827), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val routeStart = Offset(size.width * 0.12f, size.height * 0.72f)
            val routeMiddle = Offset(size.width * 0.48f, size.height * 0.45f)
            val routeEnd = Offset(size.width * 0.88f, size.height * 0.28f)

            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(size.width * 0.05f, size.height * 0.18f),
                end = Offset(size.width * 0.94f, size.height * 0.18f),
                strokeWidth = 10.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = 0.06f),
                start = Offset(size.width * 0.18f, size.height * 0.92f),
                end = Offset(size.width * 0.94f, size.height * 0.58f),
                strokeWidth = 12.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF334155),
                start = routeStart,
                end = routeMiddle,
                strokeWidth = 9.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF334155),
                start = routeMiddle,
                end = routeEnd,
                strokeWidth = 9.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = green,
                start = routeStart,
                end = routeMiddle,
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = green,
                start = routeMiddle,
                end = routeEnd,
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(color = green, radius = 7.dp.toPx(), center = routeStart)
            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = routeEnd)
            drawCircle(color = green.copy(alpha = 0.2f), radius = 14.dp.toPx(), center = routeEnd)
        }

        Box(
            modifier = Modifier
                .offset(
                    x = (maxWidth - 56.dp) * progress,
                    y = 95.dp - (58.dp * progress)
                )
                .size(42.dp)
                .background(green, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.TwoWheeler,
                contentDescription = "Moto em movimento",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = "Você",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun DeliveryInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.58f),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DeliveryTimelineStep(
    title: String,
    description: String,
    isDone: Boolean,
    isCurrent: Boolean = false
) {
    val colors = LocalPedidosColors.current
    val green = colors.primary
    val dotColor = if (isDone) green else Color.White.copy(alpha = 0.18f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(if (isCurrent) 28.dp else 24.dp)
                .background(dotColor.copy(alpha = if (isDone) 0.18f else 1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = green,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.62f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun OrderItemRow(
    quantity: String,
    name: String,
    price: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = quantity,
                color = LocalPedidosColors.current.primary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = name,
                color = Color.White.copy(alpha = 0.82f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = price,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationItemCard(
    icon: ImageVector,
    title: String,
    description: String,
    date: String,
    actionHint: String? = null,
    isUnread: Boolean,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalPedidosColors.current
    val green = colors.primary
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

                if (actionHint != null && !isSelectionMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = actionHint,
                        color = green,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
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
        NotificationType.PEDIDO_A_CAMINHO -> Icons.Filled.TwoWheeler
        NotificationType.PEDIDO_EM_PREPARO -> Icons.AutoMirrored.Filled.ReceiptLong
        NotificationType.ORDER -> Icons.AutoMirrored.Filled.ReceiptLong
        NotificationType.PROMOTION -> Icons.Filled.CardGiftcard
        NotificationType.SYSTEM -> Icons.Filled.Info
    }

private fun NotificationUiModel.pedidoIdFromNotification(): String? {
    pedidoId?.takeIf { it.isNotBlank() }?.let { return it }
    return id.removePrefix(LOCAL_ORDER_NOTIFICATION_PREFIX)
        .takeIf { id.startsWith(LOCAL_ORDER_NOTIFICATION_PREFIX) && it.isNotBlank() }
}

private const val LOCAL_ORDER_NOTIFICATION_PREFIX = "local-pedido-"

private fun NotificationUiModel.isPreparingOrderNotification(): Boolean =
    type == NotificationType.PEDIDO_EM_PREPARO ||
        statusKey == "em_preparo" ||
        title.contains("preparo", ignoreCase = true) ||
        description.contains("prepar", ignoreCase = true)

private fun NotificationUiModel.isOnTheWayOrderNotification(): Boolean =
    type == NotificationType.PEDIDO_A_CAMINHO ||
        statusKey == "a_caminho" ||
        title.contains("a caminho", ignoreCase = true) ||
        description.contains("a caminho", ignoreCase = true)

private fun NotificationUiModel.preparingRestaurantName(): String {
    return orderRestaurantName(defaultName = "Burger House")
}

private fun NotificationUiModel.orderRestaurantName(defaultName: String): String {
    restaurantName?.takeIf { it.isNotBlank() }?.let { return it }

    val patterns = listOf(
        Regex("restaurante\\s+(.+?)\\s+(começou|iniciou)", RegexOption.IGNORE_CASE),
        Regex("do\\s+(.+?)\\.", RegexOption.IGNORE_CASE)
    )

    return patterns.firstNotNullOfOrNull { pattern ->
        pattern.find(description)?.groupValues?.getOrNull(1)?.trim()
    }?.takeIf { it.isNotBlank() } ?: defaultName
}

private fun NotificationUiModel.onTheWayCourierName(): String {
    return Regex(
        pattern = "entregador\\s+(.+?)\\s+está\\s+a\\s+caminho",
        option = RegexOption.IGNORE_CASE
    ).find(description)
        ?.groupValues
        ?.getOrNull(1)
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: "Emerson"
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
                        color = LocalPedidosColors.current.primary,
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
                    color = LocalPedidosColors.current.primary,
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
    val colors = LocalPedidosColors.current
    val selectedColor = colors.primary
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
