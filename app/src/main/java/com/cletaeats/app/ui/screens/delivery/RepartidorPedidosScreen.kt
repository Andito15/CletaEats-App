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
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.cletaeats.app.ui.components.CletaScaffold
import com.cletaeats.app.ui.components.EmptyState
import com.cletaeats.app.ui.components.ErrorState
import com.cletaeats.app.ui.components.IconBubble
import com.cletaeats.app.ui.components.LoadingBox
import com.cletaeats.app.ui.components.StatusChip
import com.cletaeats.app.ui.components.money
import com.cletaeats.app.ui.theme.DangerRed
import com.cletaeats.app.ui.theme.PrimaryDeep
import com.cletaeats.app.ui.theme.PrimaryGreen
import com.cletaeats.app.ui.theme.TextSoft
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun RepartidorPedidosScreen(
    onBack: () -> Unit,
    onDetail: (Long) -> Unit
) {
    val context = LocalContext.current
    val api = remember { RetrofitProvider.createApiService(context) }
    val scope = rememberCoroutineScope()

    var pedidosDisponibles by remember { mutableStateOf<List<PedidoResponse>>(emptyList()) }
    var misPedidos by remember { mutableStateOf<List<PedidoResponse>>(emptyList()) }

    var loading by remember { mutableStateOf(true) }
    var updatingPedidoId by remember { mutableStateOf<Long?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var pedidoParaEntregar by remember { mutableStateOf<PedidoResponse?>(null) }

    var tab by remember { mutableStateOf("DISPONIBLES") }

    fun cargarPedidos() {
        scope.launch {
            loading = true
            error = null

            try {
                misPedidos = api.listarMisPedidosRepartidor()

                pedidosDisponibles = try {
                    api.listarPedidosDisponiblesRepartidor()
                } catch (_: Exception) {
                    emptyList()
                }
            } catch (e: Exception) {
                error = e.toUserMessage()
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
                error = e.toDeliveryMessage()
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
            } catch (e: Exception) {
                error = e.toDeliveryMessage()
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

    CletaScaffold(
        title = "Repartidor",
        onBack = onBack,
        actions = {
            IconButton(onClick = { cargarPedidos() }) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Actualizar",
                    tint = PrimaryGreen
                )
            }
        }
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
                text = "${String.format(Locale.getDefault(), "%.2f", pedido.distanciaKm)} km"
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
    val isEntregado = pedido.estado == "ENTREGADO"
    val isEnCamino = pedido.estado == "EN_CAMINO"

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
                text = "${String.format(Locale.getDefault(), "%.2f", pedido.distanciaKm)} km · ${
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

                OutlinedButton(
                    onClick = onEnCamino,
                    enabled = !updating && !isEntregado && !isEnCamino,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    if (updating && !isEnCamino) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = PrimaryGreen
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.DeliveryDining,
                            contentDescription = "En camino",
                            tint = PrimaryGreen
                        )
                    }
                }

                Button(
                    onClick = onEntregado,
                    enabled = !updating && !isEntregado,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.White
                    )
                ) {
                    if (updating && !isEntregado) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Entregado"
                        )
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