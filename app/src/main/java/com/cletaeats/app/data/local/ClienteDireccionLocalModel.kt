package com.cletaeats.app.data.local

import com.cletaeats.app.data.model.ClienteDireccionResponse

data class ClienteDireccionLocalModel(
    val localId: Long = 0,
    val remoteId: Long? = null,
    val clienteId: Long,
    val alias: String,
    val direccionTexto: String,
    val latitud: Double,
    val longitud: Double,
    val esPredeterminada: Boolean,
    val syncStatus: String = "SYNCED",
    val updatedAt: Long = System.currentTimeMillis()
)

fun ClienteDireccionLocalModel.toResponse(): ClienteDireccionResponse {
    return ClienteDireccionResponse(
        direccionId = remoteId ?: localId,
        alias = alias,
        direccionTexto = direccionTexto,
        latitud = latitud,
        longitud = longitud,
        esPredeterminada = esPredeterminada
    )
}

fun ClienteDireccionResponse.toLocalModel(
    clienteId: Long,
    syncStatus: String
): ClienteDireccionLocalModel {
    val id = direccionId ?: 0L

    return ClienteDireccionLocalModel(
        localId = id,
        remoteId = direccionId,
        clienteId = clienteId,
        alias = alias,
        direccionTexto = direccionTexto,
        latitud = latitud ?: 0.0,
        longitud = longitud ?: 0.0,
        esPredeterminada = esPredeterminada == true,
        syncStatus = syncStatus,
        updatedAt = System.currentTimeMillis()
    )
}