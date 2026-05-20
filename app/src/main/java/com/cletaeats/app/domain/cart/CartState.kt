package com.cletaeats.app.domain.cart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cletaeats.app.data.model.ComboResponse
import com.cletaeats.app.data.model.PedidoCreateItemRequest
import com.cletaeats.app.data.model.RestauranteResponse

data class CartItem(
    val restaurante: RestauranteResponse,
    val combo: ComboResponse,
    val cantidad: Int
)

data class CartRestaurantGroup(
    val restaurante: RestauranteResponse,
    val items: List<CartItem>
) {
    val totalItems: Int
        get() = items.sumOf { it.cantidad }

    val subtotal: Double
        get() = items.sumOf { it.combo.precio * it.cantidad }

    fun toPedidoItems(): List<PedidoCreateItemRequest> {
        return items.mapNotNull { item ->
            item.combo.id?.let { comboId ->
                PedidoCreateItemRequest(
                    comboId = comboId,
                    cantidad = item.cantidad
                )
            }
        }
    }
}

object CartState {

    /**
     * Restaurante abierto actualmente en la pantalla de combos.
     * Ya no representa el único restaurante del carrito.
     */
    var restauranteActual by mutableStateOf<RestauranteResponse?>(null)
        private set

    val items = mutableStateListOf<CartItem>()

    val totalItems: Int
        get() = items.sumOf { it.cantidad }

    val subtotal: Double
        get() = items.sumOf { it.combo.precio * it.cantidad }

    val totalRestaurantes: Int
        get() = items
            .mapNotNull { it.restaurante.id }
            .distinct()
            .size

    val isEmpty: Boolean
        get() = items.isEmpty()

    fun setRestaurant(restauranteResponse: RestauranteResponse) {
        restauranteActual = restauranteResponse
    }

    fun add(
        combo: ComboResponse,
        restaurante: RestauranteResponse = restauranteActual
            ?: throw IllegalStateException("No hay restaurante seleccionado.")
    ) {
        val restauranteId = restaurante.id ?: return
        val comboId = combo.id ?: return

        val current = items.indexOfFirst { item ->
            item.restaurante.id == restauranteId &&
                    item.combo.id == comboId
        }

        if (current >= 0) {
            val item = items[current]
            items[current] = item.copy(
                cantidad = item.cantidad + 1
            )
        } else {
            items.add(
                CartItem(
                    restaurante = restaurante,
                    combo = combo,
                    cantidad = 1
                )
            )
        }
    }

    fun decrease(
        comboId: Long?,
        restauranteId: Long? = null
    ) {
        if (comboId == null) return

        val index = items.indexOfFirst { item ->
            item.combo.id == comboId &&
                    (restauranteId == null || item.restaurante.id == restauranteId)
        }

        if (index < 0) return

        val item = items[index]

        if (item.cantidad <= 1) {
            items.removeAt(index)
        } else {
            items[index] = item.copy(
                cantidad = item.cantidad - 1
            )
        }
    }

    fun remove(
        comboId: Long?,
        restauranteId: Long? = null
    ) {
        if (comboId == null) return

        items.removeAll { item ->
            item.combo.id == comboId &&
                    (restauranteId == null || item.restaurante.id == restauranteId)
        }
    }

    fun clearRestaurant(restauranteId: Long?) {
        if (restauranteId == null) return
        items.removeAll { it.restaurante.id == restauranteId }
    }

    fun clear() {
        restauranteActual = null
        items.clear()
    }

    fun groups(): List<CartRestaurantGroup> {
        return items
            .groupBy { it.restaurante.id }
            .values
            .mapNotNull { restaurantItems ->
                val restaurante = restaurantItems.firstOrNull()?.restaurante
                    ?: return@mapNotNull null

                CartRestaurantGroup(
                    restaurante = restaurante,
                    items = restaurantItems
                )
            }
    }

    /**
     * Para compatibilidad temporal con pantallas viejas.
     * Esto devuelve todos los items mezclados.
     * Para checkout multi-restaurante, usar groups().
     */
    fun toPedidoItems(): List<PedidoCreateItemRequest> {
        return items.mapNotNull { item ->
            item.combo.id?.let { comboId ->
                PedidoCreateItemRequest(
                    comboId = comboId,
                    cantidad = item.cantidad
                )
            }
        }
    }
}