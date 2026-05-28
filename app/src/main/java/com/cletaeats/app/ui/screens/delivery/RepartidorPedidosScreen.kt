package com.cletaeats.app.ui.screens.delivery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cletaeats.app.data.model.PedidoEstadoRequest
import com.cletaeats.app.data.model.PedidoResponse
import com.cletaeats.app.data.remote.RetrofitProvider
import com.cletaeats.app.data.remote.toDeliveryMessage
import com.cletaeats.app.data.remote.toUserMessage
import com.cletaeats.app.domain.datamode.DataModeManager
import com.cletaeats.app.ui.components.DataModePickerDialog
import com.cletaeats.app.ui.components.EmptyState
import com.cletaeats.app.ui.components.ErrorState
import com.cletaeats.app.ui.components.IconBubble
import com.cletaeats.app.ui.components.LoadingBox
import com.cletaeats.app.ui.components.StatusChip
import com.cletaeats.app.ui.components.money
import com.cletaeats.app.ui.theme.BackgroundSoft
import com.cletaeats.app.ui.theme.DangerRed
import com.cletaeats.app.ui.theme.PrimaryDeep
import com.cletaeats.app.ui.theme.PrimaryGreen
import com.cletaeats.app.ui.theme.TextSoft
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.Locale
import java.io.IOException
import java.net.SocketTimeoutException

@Composable
fun RepartidorPedidosScreen(
    onInicio: () -> Unit,
    onPerfil: () -> Unit,
    onLogout: () -> Unit,
    onDetail: (Long) -> Unit
) {
    val context = LocalContext.current
    val api = remember { RetrofitProvider.createApiService(context) }
    val scope = rememberCoroutineScope()

    val dataModeManager = remember { DataModeManager(context) }

    var dataMode by remember {
        mutableStateOf(dataModeManager.getMode())
    }

    var tempMode by remember {
        mutableStateOf(dataMode)
    }

    var showModeDialog by remember {
        mutableStateOf(false)
    }

    var pedidosDisponibles by remember { mutableStateOf<List<PedidoResponse>>(emptyList()) }
    var misPedidos by remember { mutableStateOf<List<PedidoResponse>>(emptyList()) }

    var loading by remember { mutableStateOf(true) }
    var updatingPedidoId by remember { mutableStateOf<Long?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var pedidoParaEntregar by remember { mutableStateOf<PedidoResponse?>(null) }

    var tab by remember { mutableStateOf("DISPONIBLES") }

    fun errorReal(e: Exception): String {
        return when (e) {
            is HttpException -> {
                val body = e.response()?.errorBody()?.string()
                "HTTP ${e.code()}: ${body ?: e.message()}"
            }

            is SocketTimeoutException -> {
                "Tiempo agotado consultando pedidos."
            }

            is IOException -> {
                "Error de red consultando pedidos: ${e.message}"
            }

            else -> {
                e.message ?: "Error inesperado consultando pedidos."
            }
        }
    }


    fun cargarPedidos() {
        scope.launch {
            loading = true
            error = null

            try {
                val mios = api.listarMisPedidosRepartidor()
                misPedidos = mios

                pedidosDisponibles = try {
                    api.listarPedidosDisponiblesRepartidor()
                } catch (e: Exception) {
                    // Si el backend devuelve 409 porque el repartidor está ocupado,
                    // no es un error de pantalla: simplemente no hay disponibles.
                    emptyList()
                }

                if (misPedidos.isNotEmpty()) {
                    tab = "MIOS"
                }
            } catch (e: Exception) {
                error = errorReal(e)
            } finally {
                loading = false
            }
        }
    }


    fun aceptarPedido(pedidoId: Long) {
        scope.launch {
            updatingPedidoId = pedidoId
            error = null

            try {
                val aceptado = api.aceptarPedidoRepartidor(pedidoId)

                pedidosDisponibles = pedidosDisponibles.filterNot {
                    it.pedidoId == pedidoId
                }

                misPedidos = listOf(aceptado) + misPedidos.filterNot {
                    it.pedidoId == pedidoId
                }

                tab = "MIOS"
                onDetail(pedidoId)
            } catch (e: Exception) {
                error = errorReal(e)
                cargarPedidos()
            } finally {
                updatingPedidoId = null
            }
        }
    }

    fun actualizarEstado(
        pedidoId: Long,
        estado: String
    ) {
        scope.launch {
            updatingPedidoId = pedidoId
            error = null

            try {
                val actualizado = api.actualizarEstadoPedidoRepartidor(
                    pedidoId = pedidoId,
                    request = PedidoEstadoRequest(estado = estado)
                )

                misPedidos = misPedidos.map { pedido ->
                    if (pedido.pedidoId == pedidoId) actualizado else pedido
                }

                pedidosDisponibles = pedidosDisponibles.filterNot {
                    it.pedidoId == pedidoId
                }

                tab = "MIOS"
            } catch (e: Exception) {
                error = if (e is HttpException) {
                    val body = e.response()?.errorBody()?.string()
                    "HTTP ${e.code()}: ${body ?: e.message()}"
                } else {
                    e.message ?: e.toDeliveryMessage()
                }
            } finally {
                updatingPedidoId = null
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarPedidos()
    }

    pedidoParaEntregar?.let { pedido ->
        AlertDialog(
            onDismissRequest = { pedidoParaEntregar = null },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = PrimaryGreen
                )
            },
            title = {
                Text("Marcar como entregado")
            },
            text = {
                Text(
                    text = "¿Confirmás que el pedido ${pedido.numeroPedido} ya fue entregado?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pedidoParaEntregar = null

                        pedido.pedidoId?.let { id ->
                            actualizarEstado(
                                pedidoId = id,
                                estado = "ENTREGADO"
                            )
                        }
                    }
                ) {
                    Text(
                        text = "Entregado",
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pedidoParaEntregar = null }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    RepartidorDrawerScaffold(
        title = "Pedidos",
        onInicio = onInicio,
        onPerfil = onPerfil,
        onCambiarModo = {
            tempMode = dataMode
            showModeDialog = true
        },
        onLogout = onLogout
    ) { modifier ->
        when {
            loading -> LoadingBox(modifier = modifier)

            error != null && misPedidos.isEmpty() && pedidosDisponibles.isEmpty() -> ErrorState(
                message = error ?: "Error inesperado.",
                onRetry = { cargarPedidos() },
                modifier = modifier
            )

            else -> LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                item {
                    DeliveryTabs(
                        tab = tab,
                        disponibles = pedidosDisponibles.size,
                        mios = misPedidos.size,
                        onDisponibles = { tab = "DISPONIBLES" },
                        onMios = { tab = "MIOS" }
                    )
                }

                if (error != null) {
                    item {
                        ErrorMiniCard(message = error.orEmpty())
                    }
                }

                if (tab == "DISPONIBLES") {
                    if (pedidosDisponibles.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Rounded.DeliveryDining,
                                title = "Sin pedidos disponibles",
                                message = "Cuando un cliente confirme un pedido aparecerá aquí.",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(320.dp)
                            )
                        }
                    } else {
                        items(
                            items = pedidosDisponibles,
                            key = { pedido ->
                                pedido.pedidoId ?: pedido.numeroPedido.hashCode().toLong()
                            }
                        ) { pedido ->
                            PedidoDisponibleCard(
                                pedido = pedido,
                                updating = updatingPedidoId == pedido.pedidoId,
                                onAccept = {
                                    pedido.pedidoId?.let { id ->
                                        aceptarPedido(id)
                                    }
                                },
                                onDetail = {
                                    pedido.pedidoId?.let(onDetail)
                                }
                            )
                        }
                    }
                } else {
                    if (misPedidos.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Rounded.DeliveryDining,
                                title = "Sin pedidos asignados",
                                message = "Aceptá un pedido disponible para iniciar una entrega.",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(320.dp)
                            )
                        }
                    } else {
                        items(
                            items = misPedidos,
                            key = { pedido ->
                                pedido.pedidoId ?: pedido.numeroPedido.hashCode().toLong()
                            }
                        ) { pedido ->
                            MiPedidoCard(
                                pedido = pedido,
                                updating = updatingPedidoId == pedido.pedidoId,
                                onDetail = {
                                    pedido.pedidoId?.let(onDetail)
                                },
                                onEnCamino = {
                                    pedido.pedidoId?.let { id ->
                                        actualizarEstado(
                                            pedidoId = id,
                                            estado = "EN_CAMINO"
                                        )
                                    }
                                },
                                onEntregado = {
                                    pedidoParaEntregar = pedido
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }

            }
        }
    }
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
private fun RepartidorDrawerScaffold(
    title: String,
    onInicio: () -> Unit,
    onPerfil: () -> Unit,
    onCambiarModo: () -> Unit,
    onLogout: () -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "CletaEats",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Text(
                    text = "Repartidor",
                    color = TextSoft,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                NavigationDrawerItem(
                    label = {
                        Text("Inicio")
                    },
                    selected = true,
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }

                        onInicio()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = {
                        Text("Perfil")
                    },
                    selected = false,
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }

                        onPerfil()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = {
                        Text("Modo de datos")
                    },
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

                        onCambiarModo()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = TextSoft.copy(alpha = 0.18f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = {
                        Text(
                            text = "Cerrar sesión",
                            color = DangerRed,
                            fontWeight = FontWeight.SemiBold
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
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }

                        onLogout()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        unselectedIconColor = DangerRed,
                        unselectedTextColor = DangerRed
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            color = PrimaryDeep,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "Menú",
                                tint = PrimaryGreen
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BackgroundSoft
                    )
                )
            },
            containerColor = BackgroundSoft
        ) { paddingValues ->
            content(
                Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun DeliveryTabs(
    tab: String,
    disponibles: Int,
    mios: Int,
    onDisponibles: () -> Unit,
    onMios: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FilterChip(
            selected = tab == "DISPONIBLES",
            onClick = onDisponibles,
            label = {
                Text("Disponibles ($disponibles)")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.DeliveryDining,
                    contentDescription = null
                )
            },
            modifier = Modifier.weight(1f)
        )

        FilterChip(
            selected = tab == "MIOS",
            onClick = onMios,
            label = {
                Text("Míos ($mios)")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null
                )
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PedidoDisponibleCard(
    pedido: PedidoResponse,
    updating: Boolean,
    onAccept: () -> Unit,
    onDetail: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDetail),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PedidoHeader(
                pedido = pedido,
                icon = Icons.Rounded.DeliveryDining
            )

            InfoRow(
                icon = Icons.Rounded.Restaurant,
                text = pedido.restauranteNombre ?: "Restaurante"
            )

            InfoRow(
                icon = Icons.Rounded.LocationOn,
                text = pedido.direccionEntrega
            )

            InfoRow(
                icon = Icons.Rounded.Route,
                text = "${String.format(Locale.US, "%.2f", pedido.distanciaKm)} km · ${
                    money(pedido.costoKmAplicado)
                }/km"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDetail,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Visibility,
                        contentDescription = "Ver"
                    )
                }

                Button(
                    onClick = onAccept,
                    enabled = !updating,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.White
                    )
                ) {
                    if (updating) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Aceptar"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiPedidoCard(
    pedido: PedidoResponse,
    updating: Boolean,
    onDetail: () -> Unit,
    onEnCamino: () -> Unit,
    onEntregado: () -> Unit
) {
    val estado = pedido.estado.uppercase()
    val isEntregado = estado == "ENTREGADO"
    val isEnPreparacion = estado == "EN_PREPARACION"
    val isEnCamino = estado == "EN_CAMINO"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDetail),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PedidoHeader(
                pedido = pedido,
                icon = Icons.Rounded.DeliveryDining
            )

            InfoRow(
                icon = Icons.Rounded.Restaurant,
                text = pedido.restauranteNombre ?: "Restaurante"
            )

            InfoRow(
                icon = Icons.Rounded.LocationOn,
                text = pedido.direccionEntrega
            )

            InfoRow(
                icon = Icons.Rounded.Route,
                text = "${String.format(Locale.US, "%.2f", pedido.distanciaKm)} km · ${
                    money(pedido.costoKmAplicado)
                }/km"
            )

            if (isEnCamino && !isEntregado) {
                pedido.pedidoId?.let { id ->
                    DeliveryLocationSender(
                        pedidoId = id,
                        enabled = true
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDetail,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Visibility,
                        contentDescription = "Ver"
                    )
                }

                if (isEnPreparacion) {
                    Button(
                        onClick = onEnCamino,
                        enabled = !updating,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            contentColor = Color.White
                        )
                    ) {
                        if (updating) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Iniciar")
                        }
                    }
                }

                if (isEnCamino) {
                    Button(
                        onClick = onEntregado,
                        enabled = !updating,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            contentColor = Color.White
                        )
                    ) {
                        if (updating) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Terminar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PedidoHeader(
    pedido: PedidoResponse,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBubble(icon = icon)

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = pedido.numeroPedido,
                color = PrimaryGreen,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = pedido.clienteNombre ?: "Cliente",
                color = PrimaryDeep,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        StatusChip(text = pedido.estado)
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSoft
        )

        Text(
            text = text,
            color = TextSoft,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ErrorMiniCard(
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DangerRed.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = DangerRed
            )

            Text(
                text = message,
                color = DangerRed,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}