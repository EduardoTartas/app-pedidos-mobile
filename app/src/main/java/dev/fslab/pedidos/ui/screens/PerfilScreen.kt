package dev.fslab.pedidos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.fslab.pedidos.model.User
import dev.fslab.pedidos.ui.theme.LocalPedidosColors

@Composable
fun PerfilScreen(
    user: User?,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onEditProfile: () -> Unit = {},
    onNavigateNotificacoes: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val colors = LocalPedidosColors.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundGradientEnd)
            .statusBarsPadding()
    ) {
        val compactWidth = maxWidth < 360.dp
        val compactHeight = maxHeight < 760.dp
        val screenHorizontalPadding = if (compactWidth) 16.dp else 20.dp
        val headerHeight = if (compactHeight) 188.dp else 214.dp
        val avatarSize = if (compactHeight) 68.dp else 82.dp
        val cardHeight = if (compactHeight) 56.dp else 64.dp
        val headerToCardsSpacing = if (compactHeight) 12.dp else 20.dp
        val contentBottomPadding = if (compactHeight) 8.dp else 20.dp
        val displayName = when (val nome = user?.nome?.trim().orEmpty()) {
            "" -> "Usuario"
            "Admin Sistema" -> "Admin"
            else -> nome
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = bottomPadding + contentBottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = headerHeight)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(colors.surface)
                    .padding(horizontal = screenHorizontalPadding, vertical = if (compactHeight) 12.dp else 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(avatarSize + 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(avatarSize)
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

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                            .border(width = 1.5.dp, color = colors.surface, shape = CircleShape)
                            .clickable(onClick = onEditProfile),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Editar foto do usuario",
                            tint = colors.textOnPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (compactHeight) 8.dp else 12.dp))

                Text(
                    text = displayName,
                    style = if (compactHeight) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Editar perfil",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.primary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onEditProfile)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(headerToCardsSpacing))

            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(horizontal = screenHorizontalPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PerfilInfoItem(
                    title = "Meus endereços",
                    icon = Icons.Outlined.LocationOn,
                    compactWidth = compactWidth,
                    minHeight = cardHeight
                )
                PerfilInfoItem(
                    title = "Notificações",
                    icon = Icons.Outlined.Notifications,
                    compactWidth = compactWidth,
                    minHeight = cardHeight,
                    onClick = onNavigateNotificacoes
                )
                PerfilInfoItem(
                    title = "Ajuda",
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    compactWidth = compactWidth,
                    minHeight = cardHeight
                )
            }

            Spacer(modifier = Modifier.height(if (compactHeight) 10.dp else 18.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onLogout)
                    .padding(horizontal = 18.dp, vertical = if (compactHeight) 8.dp else 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "SAIR DA CONTA",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary.copy(alpha = 0.78f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun PerfilInfoItem(
    title: String,
    icon: ImageVector,
    compactWidth: Boolean,
    minHeight: Dp,
    onClick: () -> Unit = {}
) {
    val colors = LocalPedidosColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(
                width = 1.dp,
                color = colors.inputBorder.copy(alpha = if (colors.isDark) 0.45f else 0.65f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = if (compactWidth) 14.dp else 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = colors.textSecondary.copy(alpha = 0.55f),
            modifier = Modifier.size(18.dp)
        )
    }
}
