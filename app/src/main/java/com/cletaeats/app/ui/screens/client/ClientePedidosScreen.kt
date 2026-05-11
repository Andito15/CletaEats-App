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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.cletaeats.app.data.model.PedidoResponse
import com.cletaeats.app.data.remote.RetrofitProvider
import com.cletaeats.app.ui.components.CletaScaffold
import com.cletaeats.app.ui.components.EmptyState
import com.cletaeats.app.ui.components.ErrorState
import com.cletaeats.app.ui.components.IconBubble
import com.cletaeats.app.ui.components.LoadingBox
import com.cletaeats.app.ui.components.StatusChip
import com.cletaeats.app.ui.components.money
import com.cletaeats.app.ui.theme.PrimaryDeep
import com.cletaeats.app.ui.theme.PrimaryGreen
import com.cletaeats.app.ui.theme.TextSoft
import kotlinx.coroutines.launch

@Composable
fun ClientePedidosScreen(
    onBack: () -> Unit,
    onDetail: (Long) -> Unit,
    onRestaurants: () -> Unit
) {
    val context = LocalContext.current
    val api = remember { RetrofitProvider.createApiService(context) }
    val scope = rememberCoroutineScope()

    var pedidos by remember { mutableStateOf<List<PedidoResponse>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun cargarPedidos() {
        scope.launch {
            loading = true
            error = null

            try {
                pedidos = api.listarMisPedidosCliente()
            } catch (e: Exception) {
                error = e.message ?: "No se pudieron cargar los pedidos."
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarPedidos()
    }

    CletaScaffold(
        title = "Mis pedidos",
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

            error != null -> ErrorState(
                message = error ?: "Error inesperado.",
                onRetry = { cargarPedidos() },
                modifier = modifier
            )

            pedidos.isEmpty() -> Column(
                modifier = modifier.fillMaxSize()
            ) {
                EmptyState(
                    icon = Icons.Rounded.History,
                    title = "Sin pedidos todavía",
                    message = "Cuando confirmés pedidos, aparecerán aquí.",
                    modifier = Modifier.weight(1f)
                )

                OutlinedButton(
                    onClick = onRestaurants,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Restaurant,
                        contentDescription = null
                    )

                    Text("  Ver restaurantes")
                }
            }

            else -> LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                items(
                    items = pedidos,
                    key = { pedido -> pedido.pedidoId ?: pedido.numeroPedido.hashCode().toLong() }
                ) { pedido ->
                    PedidoCard(
                        pedido = pedido,
                        onClick = {
                            pedido.pedidoId?.let(onDetail)
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
fun PedidoCard(
    pedido: PedidoResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            IconBubble(
                icon = Icons.Rounded.ReceiptLong
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
                    text = "${pedido.items.sumOf { it.cantidad }} combo(s) · ${pedido.repartidorNombre ?: "Sin repartidor"}",
                    color = TextSoft,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = pedido.factura?.let { money(it.montoTotal) } ?: money(0.0),
                    color = PrimaryDeep,
                    fontWeight = FontWeight.SemiBold
                )
            }

            StatusChip(text = pedido.estado)
        }
    }
}