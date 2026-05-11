package com.cletaeats.app.data.model

data class RestauranteResponse(
    val id: Long?,
    val nombre: String,
    val cedulaJuridica: String,
    val direccion: String,
    val tipoComida: String,
    val estado: String,
    val imagenUrl: String?,
    val latitud: Double?,
    val longitud: Double?
)