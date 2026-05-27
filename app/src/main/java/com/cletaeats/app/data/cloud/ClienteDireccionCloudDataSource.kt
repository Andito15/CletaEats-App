package com.cletaeats.app.data.cloud

import com.cletaeats.app.data.model.ClienteDireccionRequest
import com.cletaeats.app.data.model.ClienteDireccionResponse
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ClienteDireccionCloudDataSource {

    private val db = FirebaseFirestore.getInstance()

    private fun collection(clienteId: Long) =
        db.collection("clientes")
            .document(clienteId.toString())
            .collection("direcciones")

    suspend fun listar(
        clienteId: Long
    ): List<ClienteDireccionResponse> {
        val snapshot = collection(clienteId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { document ->
            val direccionId = document.id.toLongOrNull()
                ?: document.getLong("direccionId")

            val alias = document.getString("alias")
                ?: return@mapNotNull null

            val direccionTexto = document.getString("direccionTexto")
                ?: return@mapNotNull null

            ClienteDireccionResponse(
                direccionId = direccionId,
                alias = alias,
                direccionTexto = direccionTexto,
                latitud = document.getDouble("latitud") ?: 0.0,
                longitud = document.getDouble("longitud") ?: 0.0,
                esPredeterminada = document.getBoolean("esPredeterminada") ?: false
            )
        }.sortedWith(
            compareByDescending<ClienteDireccionResponse> { direccion ->
                direccion.esPredeterminada == true
            }.thenBy { direccion ->
                direccion.alias
            }
        )
    }

    suspend fun buscar(
        clienteId: Long,
        query: String
    ): List<ClienteDireccionResponse> {
        val cleanQuery = query.trim()

        if (cleanQuery.isBlank()) {
            return listar(clienteId)
        }

        return listar(clienteId).filter { direccion ->
            direccion.alias.contains(cleanQuery, ignoreCase = true) ||
                    direccion.direccionTexto.contains(cleanQuery, ignoreCase = true)
        }
    }

    suspend fun crear(
        clienteId: Long,
        request: ClienteDireccionRequest
    ): ClienteDireccionResponse {
        val direccionId = System.currentTimeMillis()

        val esPredeterminada = request.esPredeterminada == true

        if (esPredeterminada) {
            quitarPredeterminadas(clienteId)
        }

        val alias = request.alias.trim()
        val direccionTexto = request.direccionTexto.trim()
        val latitud = request.latitud ?: 0.0
        val longitud = request.longitud ?: 0.0

        val data = mapOf(
            "direccionId" to direccionId,
            "alias" to alias,
            "direccionTexto" to direccionTexto,
            "latitud" to latitud,
            "longitud" to longitud,
            "esPredeterminada" to esPredeterminada,
            "updatedAt" to System.currentTimeMillis()
        )

        collection(clienteId)
            .document(direccionId.toString())
            .set(data)
            .await()

        return ClienteDireccionResponse(
            direccionId = direccionId,
            alias = alias,
            direccionTexto = direccionTexto,
            latitud = latitud,
            longitud = longitud,
            esPredeterminada = esPredeterminada
        )
    }

    suspend fun actualizar(
        clienteId: Long,
        direccionId: Long,
        request: ClienteDireccionRequest
    ): ClienteDireccionResponse {
        val esPredeterminada = request.esPredeterminada == true

        if (esPredeterminada) {
            quitarPredeterminadas(clienteId)
        }

        val alias = request.alias.trim()
        val direccionTexto = request.direccionTexto.trim()
        val latitud = request.latitud ?: 0.0
        val longitud = request.longitud ?: 0.0

        val data = mapOf(
            "direccionId" to direccionId,
            "alias" to alias,
            "direccionTexto" to direccionTexto,
            "latitud" to latitud,
            "longitud" to longitud,
            "esPredeterminada" to esPredeterminada,
            "updatedAt" to System.currentTimeMillis()
        )

        collection(clienteId)
            .document(direccionId.toString())
            .set(data)
            .await()

        return ClienteDireccionResponse(
            direccionId = direccionId,
            alias = alias,
            direccionTexto = direccionTexto,
            latitud = latitud,
            longitud = longitud,
            esPredeterminada = esPredeterminada
        )
    }

    suspend fun eliminar(
        clienteId: Long,
        direccionId: Long
    ) {
        collection(clienteId)
            .document(direccionId.toString())
            .delete()
            .await()
    }

    suspend fun marcarPredeterminada(
        clienteId: Long,
        direccionId: Long
    ): ClienteDireccionResponse {
        quitarPredeterminadas(clienteId)

        collection(clienteId)
            .document(direccionId.toString())
            .update(
                mapOf(
                    "esPredeterminada" to true,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .await()

        return listar(clienteId).first { direccion ->
            direccion.direccionId == direccionId
        }
    }

    private suspend fun quitarPredeterminadas(
        clienteId: Long
    ) {
        val snapshot = collection(clienteId)
            .get()
            .await()

        snapshot.documents.forEach { document ->
            document.reference.update(
                mapOf(
                    "esPredeterminada" to false,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
        }
    }
}