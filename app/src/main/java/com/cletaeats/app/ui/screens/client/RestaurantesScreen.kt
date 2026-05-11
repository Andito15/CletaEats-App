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
import androidx.compose.material.icons.rounded.DinnerDining
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.cletaeats.app.data.model.RestauranteResponse
import com.cletaeats.app.data.remote.RetrofitProvider
import com.cletaeats.app.domain.cart.CartState
import com.cletaeats.app.ui.components.CletaScaffold
import com.cletaeats.app.ui.components.EmptyState
import com.cletaeats.app.ui.components.ErrorState
import com.cletaeats.app.ui.components.IconBubble
import com.cletaeats.app.ui.components.LoadingBox
import com.cletaeats.app.ui.components.StatusChip
import com.cletaeats.app.ui.theme.PrimaryDeep
import com.cletaeats.app.ui.theme.PrimaryGreen
import com.cletaeats.app.ui.theme.TextSoft
import kotlinx.coroutines.launch

@Composable
fun RestaurantesScreen(
    onBack: () -> Unit,
    onOpenRestaurant: (Long) -> Unit,
    onCart: () -> Unit
) {
    val context = LocalContext.current
    val api = remember { RetrofitProvider.createApiService(context) }
    val scope = rememberCoroutineScope()

    var restaurantes by remember { mutableStateOf<List<RestauranteResponse>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

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

    CletaScaffold(
        title = "Restaurantes",
        onBack = onBack,
        actions = {
            if (CartState.totalItems > 0) {
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

            IconButton(onClick = { cargarRestaurantes() }) {
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
                onRetry = { cargarRestaurantes() },
                modifier = modifier
            )

            restaurantes.isEmpty() -> EmptyState(
                icon = Icons.Rounded.Restaurant,
                title = "Sin restaurantes activos",
                message = "Activá restaurantes desde la web administrativa.",
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

                items(
                    items = restaurantes,
                    key = { restaurante -> restaurante.id ?: restaurante.nombre.hashCode().toLong() }
                ) { restaurante ->
                    RestauranteCard(
                        restaurante = restaurante,
                        onClick = {
                            restaurante.id?.let { restauranteId ->
                                CartState.setRestaurant(restaurante)
                                onOpenRestaurant(restauranteId)
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
private fun RestauranteCard(
    restaurante: RestauranteResponse,
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
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconBubble(
                    icon = Icons.Rounded.Restaurant
                )

                Column(
                    modifier = Modifier.weight(1f)
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
                            tint = TextSoft
                        )

                        Text(
                            text = "  ${restaurante.tipoComida}",
                            color = TextSoft,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                StatusChip(text = restaurante.estado)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                color = PrimaryGreen.copy(alpha = 0.07f),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = PrimaryGreen
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
}