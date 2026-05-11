package com.cletaeats.app.ui.screens.client

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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
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
import com.cletaeats.app.data.model.ComboResponse
import com.cletaeats.app.data.model.RestauranteResponse
import com.cletaeats.app.data.remote.RetrofitProvider
import com.cletaeats.app.domain.cart.CartState
import com.cletaeats.app.ui.components.CletaScaffold
import com.cletaeats.app.ui.components.EmptyState
import com.cletaeats.app.ui.components.ErrorState
import com.cletaeats.app.ui.components.IconBubble
import com.cletaeats.app.ui.components.LoadingBox
import com.cletaeats.app.ui.components.money
import com.cletaeats.app.ui.theme.PrimaryDeep
import com.cletaeats.app.ui.theme.PrimaryGreen
import com.cletaeats.app.ui.theme.TextSoft
import kotlinx.coroutines.launch

@Composable
fun CombosScreen(
    restauranteId: Long,
    onBack: () -> Unit,
    onCheckout: () -> Unit
) {
    val context = LocalContext.current
    val api = remember { RetrofitProvider.createApiService(context) }
    val scope = rememberCoroutineScope()

    var restaurante by remember { mutableStateOf<RestauranteResponse?>(null) }
    var combos by remember { mutableStateOf<List<ComboResponse>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun cargarCombos() {
        scope.launch {
            loading = true
            error = null

            try {
                val restauranteResponse = api.obtenerRestaurante(restauranteId)
                val combosResponse = api.listarCombosPorRestaurante(
                    restauranteId = restauranteId,
                    soloActivos = true
                )

                restaurante = restauranteResponse
                combos = combosResponse
                CartState.setRestaurant(restauranteResponse)
            } catch (e: Exception) {
                error = e.message ?: "No se pudieron cargar los combos."
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(restauranteId) {
        cargarCombos()
    }

    CletaScaffold(
        title = restaurante?.nombre ?: "Combos",
        onBack = onBack,
        actions = {
            if (CartState.totalItems > 0) {
                IconButton(onClick = onCheckout) {
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

            IconButton(onClick = { cargarCombos() }) {
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
                onRetry = { cargarCombos() },
                modifier = modifier
            )

            combos.isEmpty() -> EmptyState(
                icon = Icons.Rounded.RestaurantMenu,
                title = "Sin combos activos",
                message = "Este restaurante todavía no tiene combos disponibles.",
                modifier = modifier.fillMaxSize()
            )

            else -> LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    restaurante?.let {
                        RestauranteHeader(restaurante = it)
                    }
                }

                items(
                    items = combos,
                    key = { combo -> combo.id ?: combo.numeroCombo.toLong() }
                ) { combo ->
                    ComboCard(combo = combo)
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun RestauranteHeader(
    restaurante: RestauranteResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryGreen
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = restaurante.nombre,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = restaurante.tipoComida,
                color = Color.White.copy(alpha = 0.86f),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = restaurante.direccion,
                color = Color.White.copy(alpha = 0.74f),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ComboCard(
    combo: ComboResponse
) {
    val cantidad = CartState.items
        .firstOrNull { item -> item.combo.id == combo.id }
        ?.cantidad ?: 0

    Card(
        modifier = Modifier.fillMaxWidth(),
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
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBubble(
                icon = Icons.Rounded.Fastfood
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Combo ${combo.numeroCombo}",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )

                Text(
                    text = combo.nombre,
                    color = PrimaryDeep,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = combo.descripcion,
                    color = TextSoft,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = money(combo.precio),
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilledIconButton(
                    onClick = { CartState.add(combo) }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Agregar"
                    )
                }

                if (cantidad > 0) {
                    Text(
                        text = cantidad.toString(),
                        color = PrimaryDeep,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = { CartState.decrease(combo.id) },
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "Quitar",
                            tint = PrimaryGreen
                        )
                    }
                }
            }
        }
    }
}