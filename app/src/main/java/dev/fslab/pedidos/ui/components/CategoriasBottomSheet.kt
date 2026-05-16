package dev.fslab.pedidos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fslab.pedidos.model.Categoria
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import dev.fslab.pedidos.ui.screens.CategoryChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriasBottomSheet(
    categorias: List<Categoria>,
    categoriaSelecionadaId: String?,
    onDismiss: () -> Unit,
    onCategoriaSelected: (String?) -> Unit
) {
    val colors = LocalPedidosColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.textSecondary.copy(alpha = 0.3f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = "Todas as Categorias",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categorias) { categoria ->
                    val isSelected = categoriaSelecionadaId == categoria.id
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) colors.primary else colors.surface)
                            .clickable {
                                onCategoriaSelected(categoria.id)
                                onDismiss()
                            }
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                    ) {
                        // Reutilizando a lógica de ícone do CategoryChip ou similar
                        // Aqui simplificamos para mostrar o nome, mas podemos adicionar o ícone depois
                        Text(
                            text = categoria.nome,
                            color = if (isSelected) Color.White else colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
