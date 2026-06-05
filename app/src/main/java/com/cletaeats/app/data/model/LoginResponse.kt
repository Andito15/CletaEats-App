package com.cletaeats.app.data.model

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val usuarioId: Long?,
    val clienteId: Long?,
    val repartidorId: Long?,
    val nombre: String?,
    val correo: String?,
    val rol: String?,
    val estado: String?,
    val fotoUrl: String? = null
)