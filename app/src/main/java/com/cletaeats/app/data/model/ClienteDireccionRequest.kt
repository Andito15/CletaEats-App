package com.cletaeats.app.data.model

data class ClienteDireccionRequest(
    val alias: String,
    val direccionTexto: String,
    val latitud: Double?,
    val longitud: Double?,
    val esPredeterminada: Boolean?
)