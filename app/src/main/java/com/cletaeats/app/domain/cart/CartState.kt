package com.cletaeats.app.domain.cart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cletaeats.app.data.model.ComboResponse
import com.cletaeats.app.data.model.PedidoCreateItemRequest
import com.cletaeats.app.data.model.RestauranteResponse

data class CartItem(
    val combo: ComboResponse,
    val cantidad: Int
)

object CartState {
    var restaurante by mutableStateOf<RestauranteResponse?>(null)
        private set

    val items = mutableStateListOf<CartItem>()

    val totalItems: Int
        get() = items.sumOf { it.cantidad }

    val subtotal: Double
        get() = items.sumOf { it.combo.precio * it.cantidad }

    fun setRestaurant(restauranteResponse: RestauranteResponse) {
        if (restaurante?.id != restauranteResponse.id) {
            clear()
            restaurante = restauranteResponse
        } else {
            restaurante = restauranteResponse
        }
    }

    fun add(combo: ComboResponse) {
        val current = items.indexOfFirst { it.combo.id == combo.id }
        if (current >= 0) {
            val item = items[current]
            items[current] = item.copy(cantidad = item.cantidad + 1)
        } else {
            items.add(CartItem(combo = combo, cantidad = 1))
        }
    }

    fun decrease(comboId: Long?) {
        val index = items.indexOfFirst { it.combo.id == comboId }
        if (index < 0) return

        val item = items[index]
        if (item.cantidad <= 1) {
            items.removeAt(index)
        } else {
            items[index] = item.copy(cantidad = item.cantidad - 1)
        }
    }

    fun remove(comboId: Long?) {
        items.removeAll { it.combo.id == comboId }
    }

    fun clear() {
        restaurante = null
        items.clear()
    }

    fun toPedidoItems(): List<PedidoCreateItemRequest> = items.mapNotNull { item ->
        item.combo.id?.let { comboId ->
            PedidoCreateItemRequest(
                comboId = comboId,
                cantidad = item.cantidad
            )
        }
    }
}