package com.cletaeats.app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.DinnerDining
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cletaeats.app.R
import com.cletaeats.app.data.model.RestauranteResponse
import com.cletaeats.app.data.remote.RetrofitProvider
import com.cletaeats.app.domain.cart.CartState
import com.cletaeats.app.domain.datamode.DataMode
import com.cletaeats.app.domain.datamode.DataModeManager
import com.cletaeats.app.domain.session.SessionManager
import com.cletaeats.app.ui.components.DataModePickerDialog
import com.cletaeats.app.ui.components.EmptyState
import com.cletaeats.app.ui.components.ErrorState
import com.cletaeats.app.ui.components.IconBubble
import com.cletaeats.app.ui.components.LoadingBox
import com.cletaeats.app.ui.theme.BackgroundSoft
import com.cletaeats.app.ui.theme.DangerRed
import com.cletaeats.app.ui.theme.PrimaryDeep
import com.cletaeats.app.ui.theme.PrimaryGreen
import com.cletaeats.app.ui.theme.TextSoft
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onOpenRestaurant: (Long) -> Unit,
    onCart: () -> Unit,
    onPerfil: () -> Unit,
    onMisPedidos: () -> Unit,
    onPedidosRepartidor: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val nombre = sessionManager.getNombre().orEmpty()
    val correo = sessionManager.getCorreo().orEmpty()
    val rol = sessionManager.getRol().orEmpty()
    val dataModeManager = remember { DataModeManager(context) }

    var dataMode by remember {
        mutableStateOf(dataModeManager.getMode())
    }

    var showModeDialog by remember {
        mutableStateOf(false)
    }

    var tempMode by remember {
        mutableStateOf(dataMode)
    }

    val logoutAction = {
        sessionManager.clearSession()
        onLogout()
    }

    HomeDrawerScaffold(
        nombre = nombre,
        correo = correo,
        rol = rol,
        dataMode = dataMode,
        onChangeDataMode = {
            tempMode = dataMode
            showModeDialog = true
        },
        onOpenRestaurant = onOpenRestaurant,
        onCart = onCart,
        onPerfil = onPerfil,
        onMisPedidos = onMisPedidos,
        onPedidosRepartidor = onPedidosRepartidor,
        onLogout = logoutAction
    )

    if (showModeDialog) {
        DataModePickerDialog(
            selectedMode = tempMode,
            onModeSelected = { mode ->
                tempMode = mode
            },
            onDismiss = {
                showModeDialog = false
            },
            onConfirm = {
                dataModeManager.saveMode(tempMode)
                dataMode = tempMode
                showModeDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeDrawerScaffold(
    nombre: String,
    correo: String,
    rol: String,
    dataMode: DataMode,
    onChangeDataMode: () -> Unit,
    onOpenRestaurant: (Long) -> Unit,
    onCart: () -> Unit,
    onPerfil: () -> Unit,
    onMisPedidos: () -> Unit,
    onPedidosRepartidor: () -> Unit,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val isDelivery = rol == "REPARTIDOR"

    fun closeAndGo(action: () -> Unit) {
        scope.launch { drawerState.close() }
        action()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_solo),
                        contentDescription = "CletaEats",
                        modifier = Modifier.size(76.dp)
                    )

                    Text(
                        text = nombre.ifBlank { if (isDelivery) "Repartidor" else "Cliente" },
                        color = PrimaryDeep,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = correo.ifBlank { "Sesión activa" },
                        color = TextSoft,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                NavigationDrawerItem(
                    label = { Text("Inicio") },
                    selected = true,
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                if (isDelivery) {
                    NavigationDrawerItem(
                        label = { Text("Pedidos") },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.DeliveryDining,
                                contentDescription = null
                            )
                        },
                        onClick = { closeAndGo(onPedidosRepartidor) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                } else {
                    NavigationDrawerItem(
                        label = { Text("Historial") },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.History,
                                contentDescription = null
                            )
                        },
                        onClick = { closeAndGo(onMisPedidos) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    NavigationDrawerItem(
                        label = { Text("Perfil") },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null
                            )
                        },
                        onClick = { closeAndGo(onPerfil) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                NavigationDrawerItem(
                    label = { Text("Modo de datos") },
                    selected = false,
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }
                        onChangeDataMode()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = {
                        Text(
                            text = "Salir",
                            color = DangerRed
                        )
                    },
                    selected = false,
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Logout,
                            contentDescription = null,
                            tint = DangerRed
                        )
                    },
                    onClick = onLogout,
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 12.dp
                    )
                )
            }
        }
    ) {
        Scaffold(
            containerColor = BackgroundSoft,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isDelivery) "CletaEats Rider" else "Inicio",
                            color = PrimaryDeep,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch { drawerState.open() }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "Menú"
                            )
                        }
                    },
                    actions = {
                        DataModeBadge(
                            mode = dataMode
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .widthIn(max = 140.dp)
                                .padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(18.dp)
                            )

                            Text(
                                text = nombre.ifBlank { if (isDelivery) "Repartidor" else "Cliente" },
                                color = PrimaryGreen,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (!isDelivery && CartState.totalItems > 0) {
                            IconButton(onClick = onCart) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text(CartState.totalItems.toString())
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ShoppingCart,
                                        contentDescription = "Carrito",
                                        tint = PrimaryGreen
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BackgroundSoft
                    )
                )
            }
        ) { padding ->
            if (isDelivery) {
                DeliveryHomeScreen(
                    nombre = nombre,
                    correo = correo,
                    onPedidos = onPedidosRepartidor,
                    modifier = Modifier.padding(padding)
                )
            } else {
                ClientRestaurantsHomeScreen(
                    nombre = nombre,
                    correo = correo,
                    onOpenRestaurant = onOpenRestaurant,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun ClientRestaurantsHomeScreen(
    nombre: String,
    correo: String,
    onOpenRestaurant: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val api = remember { RetrofitProvider.createApiService(context) }
    val scope = rememberCoroutineScope()

    var restaurantes by remember { mutableStateOf<List<RestauranteResponse>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var search by remember { mutableStateOf("") }
    var selectedTipo by remember { mutableStateOf<String?>(null) }

    fun cargarRestaurantes() {
        scope.launch {
            loading = true
            error = null

            try {
                restaurantes = api.listarRestaurantes(soloActivos = true)
            } catch (e: Exception) {
                error = e.message ?: "No se pudieron cargar los restaurantes."
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarRestaurantes()
    }

    val tipos = restaurantes
        .map { it.tipoComida.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()

    val restaurantesFiltrados = restaurantes.filter { restaurante ->
        val matchesSearch =
            search.isBlank() ||
                    restaurante.nombre.contains(search, ignoreCase = true) ||
                    restaurante.tipoComida.contains(search, ignoreCase = true) ||
                    restaurante.direccion.contains(search, ignoreCase = true)

        val matchesTipo =
            selectedTipo == null ||
                    restaurante.tipoComida.equals(selectedTipo, ignoreCase = true)

        matchesSearch && matchesTipo
    }

    when {
        loading -> LoadingBox(modifier = modifier)

        error != null -> ErrorState(
            message = error ?: "Error inesperado.",
            onRetry = { cargarRestaurantes() },
            modifier = modifier
        )

        restaurantes.isEmpty() -> EmptyState(
            icon = Icons.Rounded.Restaurant,
            title = "Sin restaurantes activos",
            message = "Activá restaurantes desde la web administrativa.",
            modifier = modifier.fillMaxSize()
        )

        else -> LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundSoft)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Spacer(modifier = Modifier.height(4.dp))
            }


            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {
                SearchCard(
                    search = search,
                    onSearchChange = { search = it },
                    onClear = { search = "" }
                )
            }

            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {
                FoodTypeFilters(
                    tipos = tipos,
                    selectedTipo = selectedTipo,
                    onSelect = { selectedTipo = it }
                )
            }

            if (restaurantesFiltrados.isEmpty()) {
                item(
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    EmptyState(
                        icon = Icons.Rounded.Search,
                        title = "Sin resultados",
                        message = "Probá con otro nombre o tipo de comida.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    )
                }
            } else {
                gridItems(
                    items = restaurantesFiltrados,
                    key = { restaurante ->
                        restaurante.id ?: restaurante.nombre.hashCode().toLong()
                    }
                ) { restaurante ->
                    RestauranteHomeCard(
                        restaurante = restaurante,
                        onClick = {
                            restaurante.id?.let { restauranteId ->
                                CartState.setRestaurant(restaurante)
                                onOpenRestaurant(restauranteId)
                            }
                        }
                    )
                }
            }

            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SearchCard(
    search: String,
    onSearchChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = search,
        onValueChange = onSearchChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = PrimaryGreen
            )
        },
        trailingIcon = {
            if (search.isNotBlank()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Limpiar",
                        tint = TextSoft
                    )
                }
            }
        },
        placeholder = {
            Text("Buscar restaurante o comida")
        }
    )
}

@Composable
private fun FoodTypeFilters(
    tipos: List<String>,
    selectedTipo: String?,
    onSelect: (String?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            FilterChip(
                selected = selectedTipo == null,
                onClick = { onSelect(null) },
                label = {
                    Text(
                        text = "Todo",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
            )
        }

        lazyRowItems(tipos) { tipo ->
            FilterChip(
                selected = selectedTipo == tipo,
                onClick = {
                    onSelect(
                        if (selectedTipo == tipo) null else tipo
                    )
                },
                label = {
                    Text(
                        text = tipo,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}


@Composable
private fun RestauranteHomeCard(
    restaurante: RestauranteResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column {
            RestaurantImage(
                imageUrl = restaurante.imagenUrl,
                fallbackIcon = Icons.Rounded.Restaurant
            )

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = restaurante.nombre,
                    color = PrimaryDeep,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DinnerDining,
                        contentDescription = null,
                        tint = TextSoft,
                        modifier = Modifier.size(18.dp)
                    )

                    Text(
                        text = " ${restaurante.tipoComida}",
                        color = TextSoft,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = restaurante.direccion,
                    color = TextSoft,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RestaurantImage(
    imageUrl: String?,
    fallbackIcon: ImageVector
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp)
            .background(PrimaryGreen.copy(alpha = 0.10f)),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = fallbackIcon,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(44.dp)
            )
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBubble(icon = icon)

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = PrimaryDeep,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = subtitle,
                    color = TextSoft,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun DeliveryHomeScreen(
    nombre: String,
    correo: String,
    onPedidos: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundSoft)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WelcomeCard(
            nombre = nombre.ifBlank { "Repartidor" },
            correo = correo,
            icon = Icons.Rounded.DeliveryDining,
            headline = "Ruta activa"
        )

        DashboardCard(
            title = "Pedidos",
            subtitle = "Disponibles, asignados y entregas activas.",
            icon = Icons.Rounded.DeliveryDining,
            onClick = onPedidos
        )
    }
}

@Composable
private fun DataModeBadge(
    mode: DataMode
) {
    val icon = when (mode) {
        DataMode.API -> Icons.Rounded.Storage
        DataMode.LOCAL -> Icons.Rounded.PhoneAndroid
        DataMode.CLOUD -> Icons.Rounded.Cloud
    }

    val label = when (mode) {
        DataMode.API -> "API"
        DataMode.LOCAL -> "SQLite"
        DataMode.CLOUD -> "Cloud"
    }

    Surface(
        color = PrimaryGreen.copy(alpha = 0.10f),
        shape = RoundedCornerShape(50),
        modifier = Modifier.padding(end = 6.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 5.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.height(15.dp)
            )

            Text(
                text = label,
                color = PrimaryGreen,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}
@Composable
private fun WelcomeCard(
    nombre: String,
    correo: String,
    icon: ImageVector,
    headline: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryGreen
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.15f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = headline,
                    color = Color.White.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.labelLarge
                )

                Text(
                    text = nombre,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                if (correo.isNotBlank()) {
                    Text(
                        text = correo,
                        color = Color.White.copy(alpha = 0.78f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
            }
        }
    }
}