package dev.fslab.pedidos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import dev.fslab.pedidos.model.Categoria
import dev.fslab.pedidos.model.Restaurante
import dev.fslab.pedidos.ui.theme.LocalPedidosColors
import dev.fslab.pedidos.ui.viewmodel.HomeUiState
import dev.fslab.pedidos.ui.viewmodel.HomeViewModel

import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import dev.fslab.pedidos.utils.LocationService

@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    onNavigateRestaurantes: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalPedidosColors.current
    val context = LocalContext.current
    
    val locationService = remember { LocationService(context) }
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Must launch coroutine to call suspend functions
                viewModel.viewModelScope.launch {
                    val location = locationService.getCurrentLocation()
                    if (location != null) {
                        viewModel.setLocation(location.city, location.state ?: "")
                    }
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val location = locationService.getCurrentLocation()
            if (location != null) {
                viewModel.setLocation(location.city, location.state ?: "")
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Using the dark surface color explicitly to match the design (or from theme if dynamic)
    val isLight = !androidx.compose.foundation.isSystemInDarkTheme()
    val bgColor = if(isLight) Color(0xFFF8F9FA) else Color(0xFF0A0E1A)
    val cardColor = if(isLight) Color.White else Color(0xFF161B2E)
    val textColors = if(isLight) Color.Black else Color.White

    val categoriesScrollState = rememberScrollState()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(cardColor, textColors, onNavigateRestaurantes)
        },
        containerColor = bgColor
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is HomeUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.message, color = Color.Red)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.carregarDados() }) {
                            Text("Tentar Novamente")
                        }
                    }
                }
                is HomeUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 0.dp, bottom = 80.dp) // extra padding for bottom nav overlapping
                    ) {
                        item {
                            HomeHeader(textColors, cardColor, state.locationCity, state.locationState)
                        }
                        item {
                            SearchBar(
                                query = state.searchQuery,
                                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                                cardColor = cardColor,
                                textColor = textColors
                            )
                        }
                        item {
                            CategoriesRow(
                                categorias = state.categorias,
                                selectedCategoriaId = state.selectedCategoriaId,
                                onCategoriaSelected = { viewModel.onCategoriaSelected(it) },
                                cardColor = cardColor,
                                textColor = textColors,
                                scrollState = categoriesScrollState
                            )
                        }
                        item {
                            SectionTitle("Recomendados", "Ver todos", textColors)
                        }
                        item {
                            RecomendadosRow(state.recomendados, cardColor, textColors)
                        }
                        item {
                            SectionTitle("Populares perto de você", "Ver todos", textColors)
                        }
                        items(state.populares) { restaurante ->
                            PopularItem(restaurante, cardColor, textColors)
                        }
                    }
                }
            }
        }
    }
}

@Composable
    fun HomeHeader(textColor: Color, cardColor: Color, city: String = "Sua localização", state: String = "") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
        Column {
            Text(
                text = "ENTREGAR EM",
                color = textColor.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = city,
                    color = Color(0xFF14B822),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (state.isNotEmpty()) {
                    Text(
                        text = ", $state",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = Color(0xFF14B822),
                    modifier = Modifier.padding(start = 4.dp).size(20.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(cardColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Notificações",
                tint = Color(0xFF14B822)
            )
            // Notification dot
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, cardColor: Color, textColor: Color) {
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = {
                Text(
                    text = "Buscar restaurantes ou comida",
                    color = textColor.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = textColor.copy(alpha = 0.5f)
                )
            },
            shape = RoundedCornerShape(28.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = cardColor,
                unfocusedContainerColor = cardColor,
                disabledContainerColor = cardColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
fun CategoriesRow(
    categorias: List<Categoria>,
    selectedCategoriaId: String?,
    onCategoriaSelected: (String?) -> Unit,
    cardColor: Color,
    textColor: Color,
    scrollState: androidx.compose.foundation.ScrollState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        categorias.forEach { cat ->
            CategoryChip(
                nome = cat.nome,
                isSelected = selectedCategoriaId == cat.id,
                onClick = { onCategoriaSelected(cat.id) },
                cardColor = cardColor,
                textColor = textColor,
                iconeUrl = cat.iconeCategoria
            )
        }
    }
}

    @Composable
    fun CategoryChip(
        nome: String,
        isSelected: Boolean,
        onClick: () -> Unit,
        cardColor: Color,
        textColor: Color,
        iconeUrl: String? = null
    ) {
        val bgColor = if (isSelected) Color(0xFF14B822) else cardColor
        val color = if (isSelected) Color.White else textColor.copy(alpha = 0.8f)

        val context = LocalContext.current
        val imageLoader = remember {
            ImageLoader.Builder(context)
                .components {
                    add(SvgDecoder.Factory())
                }
                .build()
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(bgColor)
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!iconeUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(iconeUrl)
                        .crossfade(true)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = null,
                    colorFilter = if (isSelected) androidx.compose.ui.graphics.ColorFilter.tint(Color.White) else null,
                    modifier = Modifier.size(16.dp).padding(end = 4.dp)
                )
            } else {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp).padding(end = 4.dp)
                    )
                }
            }
            Text(
                text = nome,
                color = color,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }

@Composable
fun SectionTitle(title: String, action: String, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = action,
            color = Color(0xFF14B822),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RecomendadosRow(restaurantes: List<Restaurante>, cardColor: Color, textColor: Color) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(restaurantes) { restaurante ->
            RecomendadoCard(restaurante, cardColor, textColor)
        }
    }
}

@Composable
fun RecomendadoCard(restaurante: Restaurante, cardColor: Color, textColor: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier.width(280.dp).height(240.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AsyncImage(
                    model = restaurante.fotoRestaurante ?: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?q=80&w=600&auto=format&fit=crop",
                    contentDescription = "Imagem do Restaurante",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Badge tempo
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${restaurante.estimativaEntregaMin}-${restaurante.estimativaEntregaMax} min",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = restaurante.nome,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF165B20).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = String.format("%.1f", restaurante.avaliacaoMedia.coerceAtLeast(4.0)),
                            color = Color(0xFF14B822),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star",
                            tint = Color(0xFF14B822),
                            modifier = Modifier.size(12.dp).padding(start = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                
                val cats = restaurante.categorias?.joinToString(" • ") { it.nome } ?: "Lanches • Bebidas"
                
                Text(
                    text = "$cats • Fast Food",
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TwoWheeler, // placeholder for moto
                        contentDescription = "Delivery",
                        tint = Color(0xFF14B822),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if(restaurante.taxaEntrega <= 0.0) "Entrega Grátis" else "R$ ${String.format("%.2f", restaurante.taxaEntrega)}",
                        color = Color(0xFF14B822),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PopularItem(restaurante: Restaurante, cardColor: Color, textColor: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = restaurante.fotoRestaurante ?: "https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=200&auto=format&fit=crop",
                    contentDescription = "Imagem do Restaurante",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Badge de nota sobre a imagem
                Row(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format("%.1f", restaurante.avaliacaoMedia.coerceAtLeast(4.0)),
                        color = Color(0xFFFFB800),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star",
                        tint = Color(0xFFFFB800),
                        modifier = Modifier.size(10.dp).padding(start = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = restaurante.nome,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val cats = restaurante.categorias?.joinToString(" • ") { it.nome } ?: "Culinária"

                Text(
                    text = cats,
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Time",
                        tint = textColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${restaurante.estimativaEntregaMin}-${restaurante.estimativaEntregaMax} min",
                        color = textColor.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.TwoWheeler, // placeholder for moto
                        contentDescription = "Delivery",
                        tint = if (restaurante.taxaEntrega <= 0.0) Color(0xFF14B822) else textColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if(restaurante.taxaEntrega <= 0.0) "Entrega Grátis" else "R$ ${String.format("%.2f", restaurante.taxaEntrega)}",
                        color = if (restaurante.taxaEntrega <= 0.0) Color(0xFF14B822) else textColor.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(cardColor: Color, textColor: Color, onNavigateRestaurantes: () -> Unit = {}) {
    NavigationBar(
        containerColor = cardColor.copy(alpha = 0.85f),
        contentColor = textColor,
        tonalElevation = 0.dp,
        modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Início") },
            label = { Text("Início", fontSize = 10.sp) },
            selected = true,
            onClick = { /* TODO */ },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF14B822),
                selectedTextColor = Color(0xFF14B822),
                indicatorColor = Color.Transparent,
                unselectedIconColor = textColor.copy(alpha = 0.5f),
                unselectedTextColor = textColor.copy(alpha = 0.5f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Storefront, contentDescription = "Restaurantes") },
            label = { Text("Restaurantes", fontSize = 10.sp) },
            selected = false,
            onClick = { onNavigateRestaurantes() },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = textColor.copy(alpha = 0.5f),
                unselectedTextColor = textColor.copy(alpha = 0.5f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Receipt, contentDescription = "Pedidos") },
            label = { Text("Pedidos", fontSize = 10.sp) },
            selected = false,
            onClick = { /* TODO */ },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = textColor.copy(alpha = 0.5f),
                unselectedTextColor = textColor.copy(alpha = 0.5f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Person, contentDescription = "Perfil") },
            label = { Text("Perfil", fontSize = 10.sp) },
            selected = false,
            onClick = { /* TODO */ },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = textColor.copy(alpha = 0.5f),
                unselectedTextColor = textColor.copy(alpha = 0.5f)
            )
        )
    }
}
