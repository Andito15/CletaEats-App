package com.cletaeats.app.navigation

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val DIRECCIONES = "direcciones"
    const val DIRECCION_FORM = "direccion_form"
    const val RESTAURANTES = "restaurantes"
    const val COMBOS = "combos/{restauranteId}"
    const val CHECKOUT = "checkout"
    const val CLIENTE_PEDIDOS = "cliente_pedidos"
    const val PEDIDO_DETALLE = "pedido_detalle/{pedidoId}"
    const val PEDIDO_TRACKING = "pedido_tracking/{pedidoId}"
    const val FEEDBACK = "feedback/{pedidoId}"
    const val REPARTIDOR_PEDIDOS = "repartidor_pedidos"

    fun combos(restauranteId: Long) = "combos/$restauranteId"
    fun pedidoDetalle(pedidoId: Long) = "pedido_detalle/$pedidoId"
    fun pedidoTracking(pedidoId: Long) = "pedido_tracking/$pedidoId"
    fun feedback(pedidoId: Long) = "feedback/$pedidoId"
}