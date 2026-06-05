package com.cletaeats.app.data.model

data class PedidoCreateRequest(
    val direccionEntrega: String,
    val distanciaKm: Double,
    val observaciones: String?,
    val medioPago: String,
    val tarjetaResumen: String?,
    val items: List<PedidoCreateItemRequest>
)

data class PedidoCreateItemRequest(
    val comboId: Long,
    val cantidad: Int
)

data class PedidoEstadoRequest(
    val estado: String
)

data class UploadImageResponse(
    val url: String
)

data class PedidoResponse(
    val pedidoId: Long?,
    val numeroPedido: String,
    val estado: String,
    val fechaPedido: String,
    val fechaEntrega: String?,
    val clienteId: Long?,
    val clienteNombre: String?,
    val restauranteId: Long?,
    val restauranteNombre: String?,
    val repartidorId: Long?,
    val repartidorNombre: String?,
    val direccionEntrega: String,
    val distanciaKm: Double,
    val tipoTarifaDia: String,
    val costoKmAplicado: Double,
    val observaciones: String?,
    val items: List<PedidoItemResponse>,
    val factura: FacturaResumenResponse?
)

data class PedidoItemResponse(
    val comboId: Long?,
    val numeroCombo: Int,
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotalLinea: Double
)

data class FacturaResumenResponse(
    val numeroFactura: String,
    val subtotal: Double,
    val costoTransporte: Double,
    val porcentajeIva: Double,
    val montoIva: Double,
    val montoTotal: Double,
    val estadoPago: String,
    val medioPago: String
)