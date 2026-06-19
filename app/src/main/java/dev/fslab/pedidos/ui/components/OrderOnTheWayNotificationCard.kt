package dev.fslab.pedidos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.fslab.pedidos.ui.theme.PedidosTheme

private val OnTheWayCardBackground = Color(0xFF161B2E)
private val OnTheWayGreen = Color(0xFF22C55E)
private val OnTheWayTrack = Color(0xFF334155)
private val OnTheWaySecondaryButton = Color(0xFF26324A)

@Composable
fun OrderOnTheWayNotificationCard(
    modifier: Modifier = Modifier,
    courierName: String = "Emerson",
    restaurantName: String = "Burger King",
    estimatedArrivalMinutes: Int = 10,
    progress: Float = 0.65f,
    onTrackClick: () -> Unit = {},
    onDetailsClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = OnTheWayCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(OnTheWayGreen.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.TwoWheeler,
                        contentDescription = "Pedido a caminho",
                        tint = OnTheWayGreen,
                        modifier = Modifier.size(25.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(OnTheWayGreen.copy(alpha = 0.16f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Agora",
                        color = OnTheWayGreen,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Pedido a caminho!",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "O entregador $courierName está a caminho com seu pedido do $restaurantName.",
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(18.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = OnTheWayGreen,
                trackColor = OnTheWayTrack
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(OnTheWayGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Chegada estimada: $estimatedArrivalMinutes minutos",
                    color = Color.White.copy(alpha = 0.86f),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onTrackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OnTheWayGreen,
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
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = OnTheWaySecondaryButton,
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

@Preview(
    name = "Order On The Way Notification Card",
    showBackground = true,
    backgroundColor = 0xFF0B1020
)
@Composable
private fun OrderOnTheWayNotificationCardPreview() {
    PedidosTheme(darkTheme = true) {
        OrderOnTheWayNotificationCard(
            modifier = Modifier.padding(16.dp)
        )
    }
}
