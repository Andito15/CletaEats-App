package com.cletaeats.app.ui.screens.client

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.RemoveShoppingCart
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import com.cletaeats.app.data.model.ClienteDireccionResponse
import com.cletaeats.app.data.model.PedidoCreateRequest
import com.cletaeats.app.data.remote.RetrofitProvider
import com.cletaeats.app.domain.cart.CartState
import com.cletaeats.app.domain.session.SessionManager
import com.cletaeats.app.ui.components.CletaScaffold
import com.cletaeats.app.ui.components.EmptyState
import com.cletaeats.app.ui.components.ErrorState
import com.cletaeats.app.ui.components.LoadingBox
import com.cletaeats.app.ui.components.money
import com.cletaeats.app.ui.theme.DangerRed
import com.cletaeats.app.ui.theme.PrimaryDeep
import com.cletaeats.app.ui.theme.PrimaryGreen
import com.cletaeats.app.ui.theme.TextSoft
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onAddAddress: () -> Unit,
    onPedidoCreado: (Long) -> Unit
) {
    val context = LocalContext.current
    val api = remember { RetrofitProvider.createApiService(context) }
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    var direcciones by remember { mutableStateOf<List<ClienteDireccionResponse>>(emptyList()) }
    var selectedDireccion by remember { mutableStateOf<ClienteDireccionResponse?>(null) }
    var observaciones by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun cargarDirecciones() {
        scope.launch {
            loading = true
            error = null

            try {
                val clienteId = sessionManager.getClienteId()
                    ?: throw IllegalStateException("No se encontró el cliente en sesión.")

                val response = api.listarDirecciones(clienteId)

                direcciones = response
                selectedDireccion = response.firstOrNull { it.esPredeterminada }
                    ?: response.firstOrNull()
            } catch (e: Exception) {
                error = e.message ?: "No se pudieron cargar las direcciones."
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarDirecciones()
    }

    CletaScaffold(
        title = "Confirmar pedido",
        onBack = onBack
    ) { modifier ->
        when {
            CartState.items.isEmpty() -> EmptyState(
                icon = Icons.Rounded.RemoveShoppingCart,
                title = "Carrito vacío",
                message = "Agregá combos antes de confirmar.",
                modifier = modifier.fillMaxSize()
            )

            loading -> LoadingBox(modifier = modifier)

            error != null && direcciones.isEmpty() -> ErrorState(
                message = error ?: "Error inesperado.",
                onRetry = { cargarDirecciones() },
                modifier = modifier
            )

            else -> LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    CartSummaryCard()
                }

                item {
                    SectionTitle("Entrega")

                    if (direcciones.isEmpty()) {
                        EmptyAddressCard(onAddAddress = onAddAddress)
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            direcciones.forEach { direccion ->
                                AddressOption(
                                    direccion = direccion,
                                    selected = selectedDireccion?.direccionId == direccion.direccionId,
                                    onClick = { selectedDireccion = direccion }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = observaciones,
                        onValueChange = { observaciones = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Observaciones") },
                        placeholder = { Text("Ej. dejar en recepción") },
                        minLines = 2,
                        shape = RoundedCornerShape(18.dp)
                    )
                }

                item {
                    val distancia = selectedDireccion?.let { calcularDistanciaEntrega(it) } ?: 3.0

                    TotalsCard(distanciaKm = distancia)

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val direccion = selectedDireccion

                            if (direccion == null) {
                                error = "Seleccioná una dirección."
                                return@Button
                            }

                            val pedidoItems = CartState.toPedidoItems()

                            if (pedidoItems.isEmpty()) {
                                error = "El pedido no tiene combos válidos."
                                return@Button
                            }

                            scope.launch {
                                saving = true
                                error = null

                                try {
                                    val pedido = api.crearPedido(
                                        PedidoCreateRequest(
                                            direccionEntrega = direccion.direccionTexto,
                                            distanciaKm = distancia,
                                            observaciones = observaciones.ifBlank { null },
                                            items = pedidoItems
                                        )
                                    )

                                    CartState.clear()

                                    pedido.pedidoId?.let { pedidoId ->
                                        onPedidoCreado(pedidoId)
                                    }
                                } catch (e: Exception) {
                                    error = e.message ?: "No se pudo crear el pedido."
                                } finally {
                                    saving = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = !saving && direcciones.isNotEmpty(),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            contentColor = Color.White
                        )
                    ) {
                        if (saving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null
                            )

                            Text("  Confirmar")
                        }
                    }

                    if (error != null) {
                        Text(
                            text = error ?: "Error",
                            color = DangerRed,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    text: String
) {
    Text(
        text = text,
        color = PrimaryDeep,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(
            start = 4.dp,
            bottom = 4.dp
        )
    )
}

@Composable
private fun CartSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ShoppingCart,
                    contentDescription = null,
                    tint = PrimaryGreen
                )

                Text(
                    text = CartState.restaurante?.nombre ?: "Pedido",
                    color = PrimaryDeep,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider()

            CartState.items.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.combo.nombre,
                            color = PrimaryDeep,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${item.cantidad} × ${money(item.combo.precio)}",
                            color = TextSoft,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    IconButton(
                        onClick = { CartState.decrease(item.combo.id) }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "Quitar",
                            tint = PrimaryGreen
                        )
                    }

                    IconButton(
                        onClick = { CartState.add(item.combo) }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Agregar",
                            tint = PrimaryGreen
                        )
                    }

                    IconButton(
                        onClick = { CartState.remove(item.combo.id) }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Eliminar",
                            tint = DangerRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyAddressCard(
    onAddAddress: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "No tenés direcciones",
                color = PrimaryDeep,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Agregá una dirección antes de confirmar el pedido.",
                color = TextSoft
            )

            OutlinedButton(
                onClick = onAddAddress,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AddLocationAlt,
                    contentDescription = null
                )

                Text("  Agregar")
            }
        }
    }
}

@Composable
private fun AddressOption(
    direccion: ClienteDireccionResponse,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick
            )

            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = PrimaryGreen
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = direccion.alias,
                    color = PrimaryDeep,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = direccion.direccionTexto,
                    color = TextSoft,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TotalsCard(
    distanciaKm: Double
) {
    val costoTransporte = distanciaKm * 1000.0
    val base = CartState.subtotal + costoTransporte
    val iva = base * 0.13
    val total = base + iva

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ReceiptLong,
                    contentDescription = null,
                    tint = PrimaryGreen
                )

                Text(
                    text = "Resumen",
                    color = PrimaryDeep,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider()

            TotalRow("Subtotal", CartState.subtotal)
            TotalRow("Transporte aprox.", costoTransporte)
            TotalRow("IVA 13%", iva)

            Spacer(modifier = Modifier.height(4.dp))

            TotalRow(
                label = "Total aprox.",
                amount = total,
                strong = true
            )
        }
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

private fun calcularDistanciaEntrega(
    direccion: ClienteDireccionResponse
): Double {
    val restaurante = CartState.restaurante

    val restLat = restaurante?.latitud
    val restLon = restaurante?.longitud
    val dirLat = direccion.latitud
    val dirLon = direccion.longitud

    if (restLat == null || restLon == null || dirLat == 0.0 || dirLon == 0.0) {
        return 3.0
    }

    val earthRadius = 6371.0
    val dLat = Math.toRadians(dirLat - restLat)
    val dLon = Math.toRadians(dirLon - restLon)

    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(restLat)) *
            cos(Math.toRadians(dirLat)) *
            sin(dLon / 2).pow(2)

    val c = 2 * atan2(
        sqrt(a),
        sqrt(1 - a)
    )

    return (earthRadius * c).coerceAtLeast(0.5)
}