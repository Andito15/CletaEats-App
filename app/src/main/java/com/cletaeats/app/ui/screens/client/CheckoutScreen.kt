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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.RemoveShoppingCart
import androidx.compose.material.icons.rounded.Restaurant
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cletaeats.app.data.model.ClienteDireccionResponse
import com.cletaeats.app.data.model.PedidoCreateRequest
import com.cletaeats.app.data.model.RestauranteResponse
import com.cletaeats.app.data.remote.RetrofitProvider
import com.cletaeats.app.data.remote.toPedidoMessage
import com.cletaeats.app.data.remote.toUserMessage
import com.cletaeats.app.domain.cart.CartRestaurantGroup
import com.cletaeats.app.domain.cart.CartState
import com.cletaeats.app.domain.payment.PaymentCard
import com.cletaeats.app.domain.payment.PaymentCardsManager
import com.cletaeats.app.domain.session.SessionManager
import com.cletaeats.app.ui.components.CletaScaffold
import com.cletaeats.app.ui.components.EmptyState
import com.cletaeats.app.ui.components.ErrorState
import com.cletaeats.app.ui.components.LoadingBox
import com.cletaeats.app.ui.components.money
import com.cletaeats.app.ui.theme.BackgroundSoft
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

    val tarjetas by remember {
        mutableStateOf(PaymentCardsManager.getCards(context))
    }

    var medioPago by remember {
        mutableStateOf(
            if (PaymentCardsManager.getDefaultCard(context) != null) {
                "TARJETA"
            } else {
                "EFECTIVO"
            }
        )
    }

    var selectedCardId by remember {
        mutableStateOf(PaymentCardsManager.getDefaultCard(context)?.id)
    }

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
                error = e.toUserMessage()
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
                    PaymentMethodSelector(
                        tarjetas = tarjetas,
                        medioPago = medioPago,
                        selectedCardId = selectedCardId,
                        onCash = {
                            medioPago = "EFECTIVO"
                            selectedCardId = null
                        },
                        onCard = { cardId ->
                            medioPago = "TARJETA"
                            selectedCardId = cardId
                        }
                    )
                }

                item {
                    val direccion = selectedDireccion
                    val groups = CartState.groups()

                    TotalsCard(
                        groups = groups,
                        direccion = direccion
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val selected = selectedDireccion

                            if (selected == null) {
                                error = "Seleccioná una dirección."
                                return@Button
                            }

                            val restaurantGroups = CartState.groups()

                            if (restaurantGroups.isEmpty()) {
                                error = "El carrito no tiene combos válidos."
                                return@Button
                            }

                            val selectedCard = tarjetas.firstOrNull { it.id == selectedCardId }

                            if (medioPago == "TARJETA" && selectedCard == null) {
                                error = "Seleccioná una tarjeta o pagá en efectivo."
                                return@Button
                            }

                            scope.launch {
                                saving = true
                                error = null

                                try {
                                    val pedidosCreados = restaurantGroups.map { group ->
                                        val distancia = calcularDistanciaEntrega(
                                            direccion = selected,
                                            restaurante = group.restaurante
                                        )

                                        api.crearPedido(
                                            PedidoCreateRequest(
                                                direccionEntrega = selected.direccionTexto,
                                                distanciaKm = distancia,
                                                observaciones = observaciones.ifBlank { null },
                                                medioPago = medioPago,
                                                tarjetaResumen = if (medioPago == "TARJETA") {
                                                    selectedCard?.displayName
                                                } else {
                                                    null
                                                },
                                                items = group.toPedidoItems()
                                            )
                                        )
                                    }

                                    CartState.clear()

                                    val primerPedidoId = pedidosCreados
                                        .firstOrNull()
                                        ?.pedidoId

                                    if (primerPedidoId != null) {
                                        onPedidoCreado(primerPedidoId)
                                    } else {
                                        error = "Se creó el pedido, pero no se recibió el ID."
                                    }
                                } catch (e: Exception) {
                                    error = e.toPedidoMessage()
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

                            Text(
                                text = if (CartState.totalRestaurantes > 1) {
                                    "  Confirmar ${CartState.totalRestaurantes} pedidos"
                                } else {
                                    "  Confirmar"
                                }
                            )
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
    val groups = CartState.groups()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Carrito",
                        color = PrimaryDeep,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${CartState.totalItems} combo(s) · ${CartState.totalRestaurantes} restaurante(s)",
                        color = TextSoft,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Divider()

            groups.forEach { group ->
                RestaurantGroupSummary(group = group)

                if (group != groups.last()) {
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun RestaurantGroupSummary(
    group: CartRestaurantGroup
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Restaurant,
                contentDescription = null,
                tint = PrimaryGreen
            )

            Text(
                text = "  ${group.restaurante.nombre}",
                color = PrimaryDeep,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        group.items.forEach { item ->
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
                    onClick = {
                        CartState.decrease(
                            comboId = item.combo.id,
                            restauranteId = item.restaurante.id
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = "Quitar",
                        tint = PrimaryGreen
                    )
                }

                IconButton(
                    onClick = {
                        CartState.add(
                            combo = item.combo,
                            restaurante = item.restaurante
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Agregar",
                        tint = PrimaryGreen
                    )
                }

                IconButton(
                    onClick = {
                        CartState.remove(
                            comboId = item.combo.id,
                            restauranteId = item.restaurante.id
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Eliminar",
                        tint = DangerRed
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Subtotal restaurante",
                modifier = Modifier.weight(1f),
                color = TextSoft,
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = money(group.subtotal),
                color = PrimaryGreen,
                fontWeight = FontWeight.Bold
            )
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
    groups: List<CartRestaurantGroup>,
    direccion: ClienteDireccionResponse?
) {
    val subtotal = groups.sumOf { it.subtotal }

    val transporte = if (direccion == null) {
        groups.size * 3000.0
    } else {
        groups.sumOf { group ->
            calcularDistanciaEntrega(
                direccion = direccion,
                restaurante = group.restaurante
            ) * 1000.0
        }
    }

    val base = subtotal + transporte
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

                Column {
                    Text(
                        text = "Resumen",
                        color = PrimaryDeep,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (groups.size > 1) {
                            "Se crearán ${groups.size} pedidos separados."
                        } else {
                            "Se creará 1 pedido."
                        },
                        color = TextSoft,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Divider()

            TotalRow("Subtotal", subtotal)
            TotalRow("Transporte aprox.", transporte)
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

@Composable
private fun PaymentMethodSelector(
    tarjetas: List<PaymentCard>,
    medioPago: String,
    selectedCardId: String?,
    onCash: () -> Unit,
    onCard: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Payments,
                    contentDescription = null,
                    tint = PrimaryGreen
                )

                Text(
                    text = "Método de pago",
                    color = PrimaryDeep,
                    fontWeight = FontWeight.Bold
                )
            }

            PaymentOptionRow(
                selected = medioPago == "EFECTIVO",
                icon = Icons.Rounded.Payments,
                title = "Efectivo",
                subtitle = "Pagar al recibir el pedido",
                onClick = onCash
            )

            tarjetas.forEach { card ->
                PaymentOptionRow(
                    selected = medioPago == "TARJETA" && selectedCardId == card.id,
                    icon = Icons.Rounded.CreditCard,
                    title = card.displayName,
                    subtitle = "${card.titular} · vence ${card.vencimiento}",
                    onClick = {
                        onCard(card.id)
                    }
                )
            }

            if (tarjetas.isEmpty()) {
                Text(
                    text = "No tenés tarjetas registradas. Podés pagar en efectivo o agregar una tarjeta desde Perfil.",
                    color = TextSoft,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun PaymentOptionRow(
    selected: Boolean,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                PrimaryGreen.copy(alpha = 0.10f)
            } else {
                BackgroundSoft
            }
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
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryGreen
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = PrimaryDeep,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle,
                    color = TextSoft,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun calcularDistanciaEntrega(
    direccion: ClienteDireccionResponse,
    restaurante: RestauranteResponse
): Double {
    val restLat = restaurante.latitud
    val restLon = restaurante.longitud
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