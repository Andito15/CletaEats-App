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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cletaeats.app.data.model.PedidoEstadoRequest
import com.cletaeats.app.data.model.PedidoResponse
import com.cletaeats.app.data.remote.RetrofitProvider
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

@Composable
fun RepartidorPedidosScreen(
    onBack: () -> Unit,
    onDetail: (Long) -> Unit
) {
    val context = LocalContext.current
    val api = remember { RetrofitProvider.createApiService(context) }
    val scope = rememberCoroutineScope()

    var pedidos by remember { mutableStateOf<List<PedidoResponse>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var updatingPedidoId by remember { mutableStateOf<Long?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun cargarPedidos() {
        scope.launch {
            loading = true
            error = null

            try {
                pedidos = api.listarMisPedidosRepartidor()
            } catch (e: Exception) {
                error = e.message ?: "No se pudieron cargar los pedidos asignados."
            } finally {
                loading = false
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

                pedidos = pedidos.map { pedido ->
                    if (pedido.pedidoId == pedidoId) actualizado else pedido
                }
            } catch (e: Exception) {
                error = e.message ?: "No se pudo actualizar el estado."
            } finally {
                updatingPedidoId = null
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarPedidos()
    }

    CletaScaffold(
        title = "Pedidos asignados",
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

            error != null && pedidos.isEmpty() -> ErrorState(
                message = error ?: "Error inesperado.",
                onRetry = { cargarPedidos() },
                modifier = modifier
            )

            pedidos.isEmpty() -> EmptyState(
                icon = Icons.Rounded.DeliveryDining,
                title = "Sin pedidos asignados",
                message = "Cuando tengas pedidos activos aparecerán aquí.",
                modifier = modifier.fillMaxSize()
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

                if (error != null) {
                    item {
                        Text(
                            text = error ?: "",
                            color = DangerRed,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                items(
                    items = pedidos,
                    key = { pedido -> pedido.pedidoId ?: pedido.numeroPedido.hashCode().toLong() }
                ) { pedido ->
                    RepartidorPedidoCard(
                        pedido = pedido,
                        updating = updatingPedidoId == pedido.pedidoId,
                        onDetail = {
                            pedido.pedidoId?.let(onDetail)
                        },
                        onEnCamino = {
                            pedido.pedidoId?.let { id ->
                                actualizarEstado(id, "EN_CAMINO")
                            }
                        },
                        onEntregado = {
                            pedido.pedidoId?.let { id ->
                                actualizarEstado(id, "ENTREGADO")
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun RepartidorPedidoCard(
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBubble(
                    icon = Icons.Rounded.DeliveryDining
                )

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
                        text = pedido.restauranteNombre ?: "Restaurante",
                        color = PrimaryDeep,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = pedido.clienteNombre ?: "Cliente",
                        color = TextSoft,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                StatusChip(text = pedido.estado)
            }

            InfoRow(
                icon = Icons.Rounded.Restaurant,
                text = "${pedido.items.sumOf { it.cantidad }} combo(s) · ${pedido.factura?.let { money(it.montoTotal) } ?: money(0.0)}"
            )

            InfoRow(
                icon = Icons.Rounded.LocationOn,
                text = pedido.direccionEntrega
            )

            InfoRow(
                icon = Icons.Rounded.Route,
                text = "${"%.2f".format(pedido.distanciaKm)} km · ${money(pedido.costoKmAplicado)}/km"
            )
            if (pedido.estado == "EN_CAMINO") {
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
                            modifier = Modifier.padding(4.dp),
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
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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