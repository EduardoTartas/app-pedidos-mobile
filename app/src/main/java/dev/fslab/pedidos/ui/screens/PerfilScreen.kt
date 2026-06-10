package dev.fslab.pedidos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.fslab.pedidos.model.User
import dev.fslab.pedidos.ui.theme.LocalPedidosColors

@Composable
fun PerfilScreen(
    user: User?,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onEditProfile: () -> Unit = {}
) {
    val colors = LocalPedidosColors.current

    try {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = bottomPadding + 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Perfil",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(width = 3.dp, color = colors.primary, shape = CircleShape)
                    .background(colors.surface),
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
                        tint = colors.primary,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = user?.nome?.ifBlank { "Usuario" } ?: "Usuario",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = user?.email.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onEditProfile,
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text(
                    text = "Editar perfil",
                    color = colors.textOnPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PerfilInfoItem(label = "Telefone", value = user?.telefone.orEmpty())
                PerfilInfoItem(label = "CPF", value = user?.cpf.orEmpty())
            }
        }
    } catch (t: Throwable) {
        Log.e("PerfilScreen", "Erro ao renderizar PerfilScreen", t)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Erro ao carregar perfil",
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onEditProfile, colors = ButtonDefaults.buttonColors(containerColor = colors.primary)) {
                    Text(text = "Editar perfil", color = colors.textOnPrimary)
                }
            }
        }
    }
}

@Composable
private fun PerfilInfoItem(
    label: String,
    value: String
) {
    val colors = LocalPedidosColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value.ifBlank { "-" },
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textPrimary
        )
    }
}
