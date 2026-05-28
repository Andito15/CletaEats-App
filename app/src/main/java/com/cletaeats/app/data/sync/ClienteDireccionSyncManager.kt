package com.cletaeats.app.data.sync

import android.content.Context
import com.cletaeats.app.data.cloud.ClienteDireccionCloudDataSource
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
            fallidas == 0 -> "Sincronización con Cloud completa: $sincronizadas cambio(s) enviados."
            else -> "Sincronización parcial con Cloud: $sincronizadas enviados, $fallidas fallidos."
        }
}

class ClienteDireccionSyncManager(
    context: Context,
    @Suppress("unused")
    private val api: ApiService
) {
    private val localDataSource = ClienteDireccionLocalDataSource(context.applicationContext)
    private val cloudDataSource = ClienteDireccionCloudDataSource()

    suspend fun sincronizarPendientes(): SyncResult {
        val pendientes = localDataSource.pendientesSync()

        var sincronizadas = 0
        var fallidas = 0

        pendientes.forEach { item ->
            try {
                when (item.syncStatus) {
                    "PENDING_CREATE" -> sincronizarCreateCloud(item)
                    "PENDING_UPDATE" -> sincronizarUpdateCloud(item)
                    "PENDING_DELETE" -> sincronizarDeleteCloud(item)
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

    private suspend fun sincronizarCreateCloud(
        item: ClienteDireccionLocalModel
    ) {
        val response = cloudDataSource.crear(
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

    private suspend fun sincronizarUpdateCloud(
        item: ClienteDireccionLocalModel
    ) {
        val direccionId = item.remoteId ?: item.localId

        val response = cloudDataSource.actualizar(
            clienteId = item.clienteId,
            direccionId = direccionId,
            request = item.toRequest()
        )

        val remoto = response.toLocalModel(
            clienteId = item.clienteId,
            syncStatus = "SYNCED"
        ).copy(
            localId = item.localId
        )

        localDataSource.marcarSincronizado(remoto)
    }

    private suspend fun sincronizarDeleteCloud(
        item: ClienteDireccionLocalModel
    ) {
        val direccionId = item.remoteId ?: item.localId

        cloudDataSource.eliminar(
            clienteId = item.clienteId,
            direccionId = direccionId
        )

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