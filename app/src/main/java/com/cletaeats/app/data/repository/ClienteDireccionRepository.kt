package com.cletaeats.app.data.repository

import android.content.Context
import com.cletaeats.app.data.cloud.ClienteDireccionCloudDataSource
import com.cletaeats.app.data.local.ClienteDireccionLocalDataSource
import com.cletaeats.app.data.local.ClienteDireccionLocalModel
import com.cletaeats.app.data.local.toLocalModel
import com.cletaeats.app.data.local.toResponse
import com.cletaeats.app.data.model.ClienteDireccionRequest
import com.cletaeats.app.data.model.ClienteDireccionResponse
import com.cletaeats.app.data.remote.ApiService
import com.cletaeats.app.domain.datamode.DataMode
import com.cletaeats.app.domain.datamode.DataModeManager

class ClienteDireccionRepository(
    context: Context,
    private val api: ApiService
) {
    private val dataModeManager = DataModeManager(context.applicationContext)
    private val localDataSource = ClienteDireccionLocalDataSource(context.applicationContext)
    private val cloudDataSource = ClienteDireccionCloudDataSource()

    private fun currentMode(): DataMode {
        return dataModeManager.getMode()
    }

    suspend fun obtener(
        clienteId: Long,
        direccionId: Long
    ): ClienteDireccionResponse? {
        return when (currentMode()) {
            DataMode.API -> {
                listarDesdeApi(clienteId).firstOrNull { direccion ->
                    direccion.direccionId == direccionId
                }
            }

            DataMode.LOCAL -> {
                localDataSource.obtenerPorId(
                    clienteId = clienteId,
                    direccionId = direccionId
                )?.toResponse()
            }

            DataMode.CLOUD -> {
                cloudDataSource.listar(clienteId).firstOrNull { direccion ->
                    direccion.direccionId == direccionId
                }
            }
        }
    }

    suspend fun listar(
        clienteId: Long
    ): List<ClienteDireccionResponse> {
        return when (currentMode()) {
            DataMode.API -> listarDesdeApi(clienteId)

            DataMode.LOCAL -> listarDesdeLocal(clienteId)

            DataMode.CLOUD -> {
                val cloud = cloudDataSource.listar(clienteId)

                guardarApiEnLocal(
                    clienteId = clienteId,
                    direcciones = cloud
                )

                cloud
            }
        }
    }

    suspend fun buscar(
        clienteId: Long,
        query: String
    ): List<ClienteDireccionResponse> {
        return when (currentMode()) {
            DataMode.API -> {
                listarDesdeApi(clienteId).filter { direccion ->
                    direccion.alias.contains(query, ignoreCase = true) ||
                            direccion.direccionTexto.contains(query, ignoreCase = true)
                }
            }

            DataMode.LOCAL -> {
                localDataSource
                    .buscar(clienteId, query)
                    .map { direccion ->
                        direccion.toResponse()
                    }
            }

            DataMode.CLOUD -> {
                cloudDataSource.buscar(
                    clienteId = clienteId,
                    query = query
                )
            }
        }
    }

    suspend fun crear(
        clienteId: Long,
        request: ClienteDireccionRequest
    ): ClienteDireccionResponse {
        return when (currentMode()) {
            DataMode.API -> {
                val response = api.crearDireccion(
                    clienteId = clienteId,
                    request = request
                )

                guardarApiEnLocal(
                    clienteId = clienteId,
                    direcciones = listOf(response)
                )

                response
            }

            DataMode.LOCAL -> {
                crearLocal(
                    clienteId = clienteId,
                    request = request
                )
            }

            DataMode.CLOUD -> {
                val response = cloudDataSource.crear(
                    clienteId = clienteId,
                    request = request
                )

                localDataSource.guardar(
                    response.toLocalModel(
                        clienteId = clienteId,
                        syncStatus = "SYNCED"
                    )
                )

                response
            }
        }
    }

    suspend fun actualizar(
        clienteId: Long,
        direccionId: Long,
        request: ClienteDireccionRequest
    ): ClienteDireccionResponse {
        return when (currentMode()) {
            DataMode.API -> {
                val response = api.actualizarDireccion(
                    clienteId = clienteId,
                    direccionId = direccionId,
                    request = request
                )

                localDataSource.actualizar(
                    response.toLocalModel(
                        clienteId = clienteId,
                        syncStatus = "SYNCED"
                    )
                )

                response
            }

            DataMode.LOCAL -> {
                actualizarLocal(
                    clienteId = clienteId,
                    direccionId = direccionId,
                    request = request
                )
            }

            DataMode.CLOUD -> {
                val response = cloudDataSource.actualizar(
                    clienteId = clienteId,
                    direccionId = direccionId,
                    request = request
                )

                localDataSource.actualizar(
                    response.toLocalModel(
                        clienteId = clienteId,
                        syncStatus = "SYNCED"
                    )
                )

                response
            }
        }
    }

    suspend fun eliminar(
        clienteId: Long,
        direccionId: Long
    ) {
        when (currentMode()) {
            DataMode.API -> {
                api.eliminarDireccion(
                    clienteId = clienteId,
                    direccionId = direccionId
                )

                localDataSource.eliminarDefinitivo(
                    localId = direccionId,
                    remoteId = direccionId
                )
            }

            DataMode.LOCAL -> {
                val actual = localDataSource.obtenerPorId(
                    clienteId = clienteId,
                    direccionId = direccionId
                ) ?: return

                if (actual.remoteId == null) {
                    localDataSource.eliminarDefinitivo(
                        localId = actual.localId,
                        remoteId = actual.remoteId
                    )
                } else {
                    localDataSource.marcarPendienteDelete(actual)
                }
            }

            DataMode.CLOUD -> {
                cloudDataSource.eliminar(
                    clienteId = clienteId,
                    direccionId = direccionId
                )

                localDataSource.eliminarDefinitivo(
                    localId = direccionId,
                    remoteId = direccionId
                )
            }
        }
    }

    suspend fun marcarPredeterminada(
        clienteId: Long,
        direccionId: Long
    ): ClienteDireccionResponse {
        return when (currentMode()) {
            DataMode.API -> {
                val response = api.marcarDireccionPredeterminada(
                    clienteId = clienteId,
                    direccionId = direccionId
                )

                guardarApiEnLocal(
                    clienteId = clienteId,
                    direcciones = api.listarDirecciones(clienteId)
                )

                response
            }

            DataMode.LOCAL -> {
                val actual = localDataSource.obtenerPorId(
                    clienteId = clienteId,
                    direccionId = direccionId
                ) ?: throw IllegalStateException("Dirección local no encontrada.")

                localDataSource.quitarPredeterminadas(clienteId)

                val actualizado = actual.copy(
                    esPredeterminada = true,
                    syncStatus = if (actual.remoteId == null) {
                        "PENDING_CREATE"
                    } else {
                        "PENDING_UPDATE"
                    },
                    updatedAt = System.currentTimeMillis()
                )

                localDataSource.actualizar(actualizado)

                actualizado.toResponse()
            }

            DataMode.CLOUD -> {
                val response = cloudDataSource.marcarPredeterminada(
                    clienteId = clienteId,
                    direccionId = direccionId
                )

                guardarApiEnLocal(
                    clienteId = clienteId,
                    direcciones = cloudDataSource.listar(clienteId)
                )

                response
            }
        }
    }

    private suspend fun listarDesdeApi(
        clienteId: Long
    ): List<ClienteDireccionResponse> {
        val response = api.listarDirecciones(clienteId)

        guardarApiEnLocal(
            clienteId = clienteId,
            direcciones = response
        )

        return response
    }

    private fun listarDesdeLocal(
        clienteId: Long
    ): List<ClienteDireccionResponse> {
        return localDataSource
            .listarPorCliente(clienteId)
            .map { direccion ->
                direccion.toResponse()
            }
    }

    private fun crearLocal(
        clienteId: Long,
        request: ClienteDireccionRequest
    ): ClienteDireccionResponse {
        val esPredeterminada = request.safePredeterminada()

        if (esPredeterminada) {
            localDataSource.quitarPredeterminadas(clienteId)
        }

        val model = ClienteDireccionLocalModel(
            remoteId = null,
            clienteId = clienteId,
            alias = request.alias.trim(),
            direccionTexto = request.direccionTexto.trim(),
            latitud = request.safeLatitud(),
            longitud = request.safeLongitud(),
            esPredeterminada = esPredeterminada,
            syncStatus = "PENDING_CREATE",
            updatedAt = System.currentTimeMillis()
        )

        val localId = localDataSource.guardar(model)

        return model.copy(localId = localId).toResponse()
    }

    private fun actualizarLocal(
        clienteId: Long,
        direccionId: Long,
        request: ClienteDireccionRequest
    ): ClienteDireccionResponse {
        val actual = localDataSource.obtenerPorId(
            clienteId = clienteId,
            direccionId = direccionId
        ) ?: throw IllegalStateException("Dirección local no encontrada.")

        val esPredeterminada = request.safePredeterminada()

        if (esPredeterminada) {
            localDataSource.quitarPredeterminadas(clienteId)
        }

        val actualizado = actual.copy(
            alias = request.alias.trim(),
            direccionTexto = request.direccionTexto.trim(),
            latitud = request.safeLatitud(),
            longitud = request.safeLongitud(),
            esPredeterminada = esPredeterminada,
            syncStatus = if (actual.remoteId == null) {
                "PENDING_CREATE"
            } else {
                "PENDING_UPDATE"
            },
            updatedAt = System.currentTimeMillis()
        )

        localDataSource.actualizar(actualizado)

        return actualizado.toResponse()
    }

    private fun guardarApiEnLocal(
        clienteId: Long,
        direcciones: List<ClienteDireccionResponse>
    ) {
        localDataSource.limpiarCliente(clienteId)

        val locales = direcciones.map { direccion ->
            direccion.toLocalModel(
                clienteId = clienteId,
                syncStatus = "SYNCED"
            )
        }

        localDataSource.guardarTodas(locales)
    }

    private fun ClienteDireccionRequest.safeLatitud(): Double {
        return latitud ?: 0.0
    }

    private fun ClienteDireccionRequest.safeLongitud(): Double {
        return longitud ?: 0.0
    }

    private fun ClienteDireccionRequest.safePredeterminada(): Boolean {
        return esPredeterminada == true
    }
}