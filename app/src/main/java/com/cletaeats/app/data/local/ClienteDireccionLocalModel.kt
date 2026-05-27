package com.cletaeats.app.data.local

import com.cletaeats.app.data.model.ClienteDireccionResponse

data class ClienteDireccionLocalModel(
    val localId: Long = 0,
    val remoteId: Long?,
    val clienteId: Long,
    val alias: String,
    val direccionTexto: String,
    val latitud: Double,
    val longitud: Double,
    val esPredeterminada: Boolean,
    val syncStatus: String,
    val updatedAt: Long
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
    syncStatus: String = "SYNCED"
): ClienteDireccionLocalModel {
    return ClienteDireccionLocalModel(
        localId = 0,
        remoteId = direccionId,
        clienteId = clienteId,
        alias = alias,
        direccionTexto = direccionTexto,
        latitud = latitud,
        longitud = longitud,
        esPredeterminada = esPredeterminada,
        syncStatus = syncStatus,
        updatedAt = System.currentTimeMillis()
    )
}