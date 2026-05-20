package com.cletaeats.app.ui.screens.client

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cletaeats.app.data.model.ComboResponse
import com.cletaeats.app.data.model.RestauranteResponse
import com.cletaeats.app.data.remote.RetrofitProvider
import com.cletaeats.app.domain.cart.CartState
import com.cletaeats.app.ui.components.CletaScaffold
import com.cletaeats.app.ui.components.EmptyState
import com.cletaeats.app.ui.components.ErrorState
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

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    restaurante?.let {
                        RestauranteHeader(restaurante = it)
                    }
                }

                val restauranteActual = restaurante

                if (restauranteActual != null) {
                    items(
                        items = combos,
                        key = { combo ->
                            "${restauranteActual.id}-${combo.id ?: combo.numeroCombo}"
                        }
                    ) { combo ->
                        ComboCard(
                            restaurante = restauranteActual,
                            combo = combo
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
}

@Composable
private fun RestauranteHeader(
    restaurante: RestauranteResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column {
            FoodImage(
                imageUrl = restaurante.imagenUrl,
                fallbackIcon = Icons.Rounded.RestaurantMenu,
                height = 150
            )

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = restaurante.nombre,
                    color = PrimaryDeep,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = restaurante.tipoComida,
                    color = PrimaryGreen,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = restaurante.direccion,
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
private fun ComboCard(
    restaurante: RestauranteResponse,
    combo: ComboResponse
) {
    val cantidad = CartState.items
        .firstOrNull { item ->
            item.restaurante.id == restaurante.id &&
                    item.combo.id == combo.id
        }
        ?.cantidad ?: 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column {
            FoodImage(
                imageUrl = combo.imagenUrl,
                fallbackIcon = Icons.Rounded.Fastfood,
                height = 105
            )

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Combo ${combo.numeroCombo}",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall
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
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = money(combo.precio),
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (cantidad > 0) {
                        IconButton(
                            onClick = {
                                CartState.decrease(
                                    comboId = combo.id,
                                    restauranteId = restaurante.id
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Remove,
                                contentDescription = "Quitar",
                                tint = PrimaryGreen
                            )
                        }

                        Text(
                            text = cantidad.toString(),
                            color = PrimaryDeep,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = {
                            CartState.add(
                                combo = combo,
                                restaurante = restaurante
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Agregar",
                            tint = PrimaryGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodImage(
    imageUrl: String?,
    fallbackIcon: ImageVector,
    height: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
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
                modifier = Modifier.size(46.dp)
            )
        }
    }
}