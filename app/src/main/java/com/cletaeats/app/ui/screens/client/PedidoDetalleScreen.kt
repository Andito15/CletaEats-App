package com.cletaeats.app.ui.screens.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cletaeats.app.data.model.PedidoEstadoRequest
import com.cletaeats.app.data.model.PedidoResponse
import com.cletaeats.app.data.remote.RetrofitProvider
import com.cletaeats.app.data.remote.toDeliveryMessage
import com.cletaeats.app.data.remote.toUserMessage
import com.cletaeats.app.domain.session.SessionManager
import com.cletaeats.app.ui.components.CletaScaffold
import com.cletaeats.app.ui.components.ErrorState
import com.cletaeats.app.ui.components.LoadingBox
import com.cletaeats.app.ui.components.StatusChip
import com.cletaeats.app.ui.components.money
import com.cletaeats.app.ui.screens.delivery.DeliveryLocationSender
import com.cletaeats.app.ui.theme.DangerRed
import com.cletaeats.app.ui.theme.PrimaryDeep
import com.cletaeats.app.ui.theme.PrimaryGreen
import com.cletaeats.app.ui.theme.TextSoft
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun PedidoDetalleScreen(
    pedidoId: Long,
    onBack: () -> Unit,
    onTracking: (Long) -> Unit,
    onFeedback: (Long) -> Unit
) {
    val context = LocalContext.current
    val api = remember { RetrofitProvider.createApiService(context) }
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    val isDelivery = sessionManager.getRol() == "REPARTIDOR"

    var pedido by remember { mutableStateOf<PedidoResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var savingEstado by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun cargarPedido(showLoading: Boolean = true) {
        scope.launch {
            if (showLoading) {
                loading = true
            }

            error = null

            try {
                pedido = if (isDelivery) {
                    api.obtenerPedidoRepartidor(pedidoId)
                } else {
                    api.obtenerPedidoCliente(pedidoId)
                }
            } catch (e: Exception) {
                error = e.toUserMessage()
            } finally {
                loading = false
            }
        }
    }

    fun actualizarEstado(estado: String) {
        scope.launch {
            savingEstado = true
            error = null

            try {
                api.actualizarEstadoPedidoRepartidor(
                    pedidoId = pedidoId,
                    request = PedidoEstadoRequest(estado = estado)
                )

                pedido = api.obtenerPedidoRepartidor(pedidoId)
            } catch (e: Exception) {
                error = if (e is HttpException) {
                    val body = e.response()?.errorBody()?.string()
                    "HTTP ${e.code()}: ${body ?: e.message()}"
                } else {
                    e.message ?: e.toDeliveryMessage()
                }
            } finally {
                savingEstado = false
            }
        }
    }

    LaunchedEffect(pedidoId) {
        cargarPedido()
    }

    CletaScaffold(
        title = pedido?.numeroPedido ?: "Pedido",
        onBack = onBack,
        actions = {
            IconButton(
                onClick = {
                    cargarPedido()
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Actualizar",
                    tint = PrimaryGreen
                )
            }
        }
    ) { modifier ->
        when {
            loading -> {
                LoadingBox(modifier = modifier)
            }

            error != null && pedido == null -> {
                ErrorState(
                    message = error ?: "Error inesperado.",
                    onRetry = {
                        cargarPedido()
                    },
                    modifier = modifier
                )
            }

            pedido != null -> {
                PedidoDetailContent(
                    pedido = pedido!!,
                    isDelivery = isDelivery,
                    savingEstado = savingEstado,
                    error = error,
                    onEstado = { estado ->
                        actualizarEstado(estado)
                    },
                    onTracking = {
                        pedido!!.pedidoId?.let(onTracking)
                    },
                    onFeedback = {
                        pedido!!.pedidoId?.let(onFeedback)
                    },
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun PedidoDetailContent(
    pedido: PedidoResponse,
    isDelivery: Boolean,
    savingEstado: Boolean,
    error: String?,
    onEstado: (String) -> Unit,
    onTracking: () -> Unit,
    onFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado = pedido.estado.trim().uppercase()
    val isEnPreparacion = estado == "EN_PREPARACION"
    val isEnCamino = estado == "EN_CAMINO"
    val isEntregado = estado == "ENTREGADO"
    val isCancelado = estado == "CANCELADO"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isDelivery && isEnCamino) {
            pedido.pedidoId?.let { id ->
                DeliveryLocationSender(
                    pedidoId = id,
                    enabled = true
                )
            }
        }

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ReceiptLong,
                        contentDescription = null,
                        tint = PrimaryGreen
                    )

                    Text(
                        text = "  ${pedido.restauranteNombre ?: "Restaurante"}",
                        color = PrimaryDeep,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    StatusChip(text = pedido.estado)
                }

                InfoLine(
                    icon = Icons.Rounded.LocationOn,
                    text = pedido.direccionEntrega
                )

                InfoLine(
                    icon = Icons.Rounded.DeliveryDining,
                    text = pedido.repartidorNombre ?: "Repartidor por asignar"
                )
            }
        }

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Detalle",
                    color = PrimaryDeep,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider()

                pedido.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Combo #${item.numeroCombo} · ${item.nombre}",
                                color = PrimaryDeep,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "${item.cantidad} × ${money(item.precioUnitario)}",
                                color = TextSoft,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Text(
                            text = money(item.subtotalLinea),
                            color = PrimaryDeep,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        pedido.factura?.let { factura ->
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ReceiptLong,
                            contentDescription = null,
                            tint = PrimaryGreen
                        )

                        Text(
                            text = "  Factura ${factura.numeroFactura}",
                            color = PrimaryDeep,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider()

                    TotalRow("Subtotal", factura.subtotal)
                    TotalRow("Transporte", factura.costoTransporte)
                    TotalRow("IVA ${factura.porcentajeIva.toInt()}%", factura.montoIva)
                    TotalRow("Total", factura.montoTotal, strong = true)
                }
            }
        }

        if (!isDelivery && isEnCamino) {
            Button(
                onClick = onTracking,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Map,
                    contentDescription = null
                )

                Text("  Ver entrega")
            }
        }

        if (isDelivery && !isEntregado && !isCancelado) {
            if (isEnPreparacion || isEnCamino) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Entrega",
                            color = PrimaryDeep,
                            fontWeight = FontWeight.Bold
                        )

                        if (isEnPreparacion) {
                            Button(
                                onClick = {
                                    onEstado("EN_CAMINO")
                                },
                                enabled = !savingEstado,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryGreen,
                                    contentColor = Color.White
                                )
                            ) {
                                if (savingEstado) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.DeliveryDining,
                                        contentDescription = null
                                    )

                                    Text("  Iniciar viaje")
                                }
                            }
                        }

                        if (isEnCamino) {
                            Button(
                                onClick = {
                                    onEstado("ENTREGADO")
                                },
                                enabled = !savingEstado,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryGreen,
                                    contentColor = Color.White
                                )
                            ) {
                                if (savingEstado) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null
                                    )

                                    Text("  Terminar pedido")
                                }
                            }
                        }
                    }
                }
            }
        } else if (!isDelivery && isEntregado) {
            Button(
                onClick = onFeedback,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null
                )

                Text("  Calificar")
            }
        }

        if (error != null) {
            Text(
                text = error,
                color = DangerRed
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun InfoLine(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSoft
        )

        Text(
            text = text,
            color = TextSoft
        )
    }
}

@Composable
private fun TotalRow(
    label: String,
    amount: Double,
    strong: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = if (strong) PrimaryDeep else TextSoft,
            fontWeight = if (strong) FontWeight.Bold else FontWeight.Normal
        )

        Text(
            text = money(amount),
            color = PrimaryDeep,
            fontWeight = if (strong) FontWeight.Bold else FontWeight.Normal
        )
    }
}