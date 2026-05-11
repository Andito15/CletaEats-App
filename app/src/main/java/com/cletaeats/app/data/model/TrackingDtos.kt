package com.cletaeats.app.data.model

data class UbicacionRepartidorRequest(
    val pedidoId: Long?,
    val latitud: Double,
    val longitud: Double,
    val precisionMetros: Double?
)

data class UbicacionRepartidorResponse(
    val pedidoId: Long?,
    val repartidorId: Long?,
    val repartidorNombre: String?,
    val estadoPedido: String?,
    val latitud: Double?,
    val longitud: Double?,
    val precisionMetros: Double?,
    val ultimaUbicacionEn: String?
)