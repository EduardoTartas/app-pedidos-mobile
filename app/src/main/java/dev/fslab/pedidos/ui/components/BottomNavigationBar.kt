package dev.fslab.pedidos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild
import dev.fslab.pedidos.ui.theme.LocalPedidosColors

/**
 * Destinos principais do app que aparecem na barra de navegação inferior.
 */
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("home", "Início", Icons.Outlined.Home),
    BottomNavItem("restaurantes", "Restaurantes", Icons.Outlined.Storefront),
    BottomNavItem("pedidos", "Pedidos", Icons.Outlined.Receipt),
    BottomNavItem("perfil", "Perfil", Icons.Outlined.Person),
)

/**
 * Barra de navegação inferior global do app com suporte a efeito Haze (glassmorphism).
 *
 * @param cardColor     Cor de fundo do card (surface).
 * @param textColor     Cor do texto principal.
 * @param hazeState     Estado do Haze para efeito de desfoque (opcional).
 * @param selectedRoute Rota atualmente selecionada para destacar o item correto.
 * @param onNavigate    Callback chamado ao clicar num item; recebe a rota destino.
 */
@Composable
fun BottomNavigationBar(
    cardColor: Color,
    textColor: Color,
    selectedRoute: String = "home",
    onNavigate: (String) -> Unit = {}
) {
    val colors = LocalPedidosColors.current

    NavigationBar(
        containerColor = cardColor.copy(alpha = 0.98f),
        contentColor = textColor,
        tonalElevation = 4.dp,
        modifier = Modifier
            .height(80.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 10.dp)
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = selectedRoute == item.route

            NavigationBarItem(
                modifier = Modifier.padding(horizontal = 4.dp),
                icon = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) colors.primary.copy(alpha = 0.14f)
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                selected = isSelected,
                onClick = {
                    if (!isSelected) onNavigate(item.route)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.primary,
                    selectedTextColor = colors.primary,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = textColor.copy(alpha = 0.5f),
                    unselectedTextColor = textColor.copy(alpha = 0.5f)
                )
            )
        }
    }
}
