package com.cletaeats.app.data.model

data class CalificacionRequest(
    val puntajeAmabilidad: Int,
    val puntajeTiempo: Int,
    val puntajePresentacion: Int,
    val comentario: String?
)

data class CalificacionResponse(
    val calificacionId: Long?,
    val pedidoId: Long?,
    val repartidorId: Long?,
    val clienteId: Long?,
    val puntajeAmabilidad: Int,
    val puntajeTiempo: Int,
    val puntajePresentacion: Int,
    val comentario: String?
)

data class QuejaRequest(
    val categoria: String,
    val descripcion: String
)

data class QuejaResponse(
    val quejaId: Long?,
    val pedidoId: Long?,
    val repartidorId: Long?,
    val clienteId: Long?,
    val categoria: String,
    val descripcion: String,
    val estado: String?
)