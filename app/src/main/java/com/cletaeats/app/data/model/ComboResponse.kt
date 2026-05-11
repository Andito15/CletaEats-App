package com.cletaeats.app.data.model

data class ComboResponse(
    val id: Long?,
    val restauranteId: Long?,
    val restauranteNombre: String?,
    val numeroCombo: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val estado: String,
    val imagenUrl: String?
)