package com.cletaeats.app.data.sync

import android.content.Context
import com.cletaeats.app.data.local.ClienteDireccionLocalDataSource
import com.cletaeats.app.data.local.ClienteDireccionLocalModel
import com.cletaeats.app.data.local.toLocalModel
import com.cletaeats.app.data.model.ClienteDireccionRequest
import com.cletaeats.app.data.remote.ApiService

data class SyncResult(
    val totalPendientes: Int,
    val sincronizadas: Int,
    val fallidas: Int
) {
    val message: String
        get() = when {
            totalPendientes == 0 -> "No hay cambios pendientes por sincronizar."
            fallidas == 0 -> "Sincronización completa: $sincronizadas cambio(s) enviados."
            else -> "Sincronización parcial: $sincronizadas enviados, $fallidas fallidos."
        }
}

class ClienteDireccionSyncManager(
    context: Context,
    private val api: ApiService
) {
    private val localDataSource = ClienteDireccionLocalDataSource(context.applicationContext)

    suspend fun sincronizarPendientes(): SyncResult {
        val pendientes = localDataSource.pendientesSync()

        var sincronizadas = 0
        var fallidas = 0

        pendientes.forEach { item ->
            try {
                when (item.syncStatus) {
                    "PENDING_CREATE" -> sincronizarCreate(item)
                    "PENDING_UPDATE" -> sincronizarUpdate(item)
                    "PENDING_DELETE" -> sincronizarDelete(item)
                }

                sincronizadas++
            } catch (_: Exception) {
                fallidas++
            }
        }

        return SyncResult(
            totalPendientes = pendientes.size,
            sincronizadas = sincronizadas,
            fallidas = fallidas
        )
    }

    private suspend fun sincronizarCreate(
        item: ClienteDireccionLocalModel
    ) {
        val response = api.crearDireccion(
            clienteId = item.clienteId,
            request = item.toRequest()
        )

        val remoto = response.toLocalModel(
            clienteId = item.clienteId,
            syncStatus = "SYNCED"
        )

        localDataSource.reemplazarLocalPorRemoto(
            localId = item.localId,
            remoto = remoto
        )
    }

    private suspend fun sincronizarUpdate(
        item: ClienteDireccionLocalModel
    ) {
        val remoteId = item.remoteId
            ?: throw IllegalStateException("No hay remoteId para actualizar.")

        val response = api.actualizarDireccion(
            clienteId = item.clienteId,
            direccionId = remoteId,
            request = item.toRequest()
        )

        val remoto = response.toLocalModel(
            clienteId = item.clienteId,
            syncStatus = "SYNCED"
        )

        localDataSource.marcarSincronizado(remoto)
    }

    private suspend fun sincronizarDelete(
        item: ClienteDireccionLocalModel
    ) {
        val remoteId = item.remoteId

        if (remoteId != null) {
            api.eliminarDireccion(
                clienteId = item.clienteId,
                direccionId = remoteId
            )
        }

        localDataSource.eliminarDefinitivo(
            localId = item.localId,
            remoteId = item.remoteId
        )
    }

    private fun ClienteDireccionLocalModel.toRequest(): ClienteDireccionRequest {
        return ClienteDireccionRequest(
            alias = alias,
            direccionTexto = direccionTexto,
            latitud = latitud,
            longitud = longitud,
            esPredeterminada = esPredeterminada
        )
    }
}