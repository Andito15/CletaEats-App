package com.cletaeats.app.data.local

import android.content.ContentValues
import android.content.Context

class ClienteDireccionLocalDataSource(
    context: Context
) {
    private val helper = CletaSQLiteHelper(context.applicationContext)

    fun listarPorCliente(clienteId: Long): List<ClienteDireccionLocalModel> {
        val db = helper.readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT *
        FROM cliente_direcciones
        WHERE cliente_id = ?
          AND sync_status != 'PENDING_DELETE'
        ORDER BY es_predeterminada DESC, updated_at DESC
        """.trimIndent(),
            arrayOf(clienteId.toString())
        )

        return cursor.use {
            buildList {
                while (it.moveToNext()) {
                    add(cursorToModel(it))
                }
            }
        }
    }

    fun buscar(
        clienteId: Long,
        query: String
    ): List<ClienteDireccionLocalModel> {
        val db = helper.readableDatabase
        val like = "%$query%"

        val cursor = db.rawQuery(
            """
        SELECT *
        FROM cliente_direcciones
        WHERE cliente_id = ?
          AND sync_status != 'PENDING_DELETE'
          AND (
            alias LIKE ?
            OR direccion_texto LIKE ?
          )
        ORDER BY es_predeterminada DESC, updated_at DESC
        """.trimIndent(),
            arrayOf(clienteId.toString(), like, like)
        )

        return cursor.use {
            buildList {
                while (it.moveToNext()) {
                    add(cursorToModel(it))
                }
            }
        }
    }

    fun guardar(model: ClienteDireccionLocalModel): Long {
        val db = helper.writableDatabase

        if (model.esPredeterminada) {
            quitarPredeterminadas(model.clienteId)
        }

        val values = ContentValues().apply {
            put("remote_id", model.remoteId)
            put("cliente_id", model.clienteId)
            put("alias", model.alias)
            put("direccion_texto", model.direccionTexto)
            put("latitud", model.latitud)
            put("longitud", model.longitud)
            put("es_predeterminada", if (model.esPredeterminada) 1 else 0)
            put("sync_status", model.syncStatus)
            put("updated_at", model.updatedAt)
        }

        return db.insert(
            "cliente_direcciones",
            null,
            values
        )
    }

    fun marcarPendienteDelete(model: ClienteDireccionLocalModel) {
        val db = helper.writableDatabase

        val values = ContentValues().apply {
            put("sync_status", "PENDING_DELETE")
            put("updated_at", System.currentTimeMillis())
        }

        if (model.remoteId != null) {
            db.update(
                "cliente_direcciones",
                values,
                "remote_id = ?",
                arrayOf(model.remoteId.toString())
            )
        } else {
            db.update(
                "cliente_direcciones",
                values,
                "local_id = ?",
                arrayOf(model.localId.toString())
            )
        }
    }

    fun eliminarDefinitivo(
        localId: Long,
        remoteId: Long?
    ) {
        val db = helper.writableDatabase

        if (remoteId != null) {
            db.delete(
                "cliente_direcciones",
                "remote_id = ?",
                arrayOf(remoteId.toString())
            )
        } else {
            db.delete(
                "cliente_direcciones",
                "local_id = ?",
                arrayOf(localId.toString())
            )
        }
    }

    fun reemplazarLocalPorRemoto(
        localId: Long,
        remoto: ClienteDireccionLocalModel
    ) {
        val db = helper.writableDatabase

        db.beginTransaction()

        try {
            db.delete(
                "cliente_direcciones",
                "local_id = ?",
                arrayOf(localId.toString())
            )

            guardar(remoto)

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun marcarSincronizado(model: ClienteDireccionLocalModel) {
        val db = helper.writableDatabase

        val values = ContentValues().apply {
            put("remote_id", model.remoteId)
            put("cliente_id", model.clienteId)
            put("alias", model.alias)
            put("direccion_texto", model.direccionTexto)
            put("latitud", model.latitud)
            put("longitud", model.longitud)
            put("es_predeterminada", if (model.esPredeterminada) 1 else 0)
            put("sync_status", "SYNCED")
            put("updated_at", System.currentTimeMillis())
        }

        if (model.remoteId != null) {
            db.update(
                "cliente_direcciones",
                values,
                "remote_id = ?",
                arrayOf(model.remoteId.toString())
            )
        } else {
            db.update(
                "cliente_direcciones",
                values,
                "local_id = ?",
                arrayOf(model.localId.toString())
            )
        }
    }

    fun guardarTodas(models: List<ClienteDireccionLocalModel>) {
        val db = helper.writableDatabase

        db.beginTransaction()

        try {
            models.forEach { model ->
                guardar(model)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun actualizar(model: ClienteDireccionLocalModel) {
        val db = helper.writableDatabase

        if (model.esPredeterminada) {
            quitarPredeterminadas(model.clienteId)
        }

        val values = ContentValues().apply {
            put("remote_id", model.remoteId)
            put("cliente_id", model.clienteId)
            put("alias", model.alias)
            put("direccion_texto", model.direccionTexto)
            put("latitud", model.latitud)
            put("longitud", model.longitud)
            put("es_predeterminada", if (model.esPredeterminada) 1 else 0)
            put("sync_status", model.syncStatus)
            put("updated_at", System.currentTimeMillis())
        }

        if (model.remoteId != null) {
            db.update(
                "cliente_direcciones",
                values,
                "remote_id = ?",
                arrayOf(model.remoteId.toString())
            )
        } else {
            db.update(
                "cliente_direcciones",
                values,
                "local_id = ?",
                arrayOf(model.localId.toString())
            )
        }
    }

    fun eliminar(
        localId: Long,
        remoteId: Long?
    ) {
        val db = helper.writableDatabase

        if (remoteId != null) {
            db.delete(
                "cliente_direcciones",
                "remote_id = ?",
                arrayOf(remoteId.toString())
            )
        } else {
            db.delete(
                "cliente_direcciones",
                "local_id = ?",
                arrayOf(localId.toString())
            )
        }
    }

    fun limpiarCliente(clienteId: Long) {
        val db = helper.writableDatabase

        db.delete(
            "cliente_direcciones",
            "cliente_id = ?",
            arrayOf(clienteId.toString())
        )
    }

    fun pendientesSync(): List<ClienteDireccionLocalModel> {
        val db = helper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT *
            FROM cliente_direcciones
            WHERE sync_status != 'SYNCED'
            ORDER BY updated_at ASC
            """.trimIndent(),
            emptyArray()
        )

        return cursor.use {
            buildList {
                while (it.moveToNext()) {
                    add(cursorToModel(it))
                }
            }
        }
    }

    fun obtenerPorId(
        clienteId: Long,
        direccionId: Long
    ): ClienteDireccionLocalModel? {
        val db = helper.readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT *
        FROM cliente_direcciones
        WHERE cliente_id = ?
          AND (
            local_id = ?
            OR remote_id = ?
          )
        LIMIT 1
        """.trimIndent(),
            arrayOf(
                clienteId.toString(),
                direccionId.toString(),
                direccionId.toString()
            )
        )

        return cursor.use {
            if (it.moveToFirst()) {
                cursorToModel(it)
            } else {
                null
            }
        }
    }

    fun quitarPredeterminadas(clienteId: Long) {
        val db = helper.writableDatabase

        val values = ContentValues().apply {
            put("es_predeterminada", 0)
        }

        db.update(
            "cliente_direcciones",
            values,
            "cliente_id = ?",
            arrayOf(clienteId.toString())
        )
    }

    private fun cursorToModel(
        cursor: android.database.Cursor
    ): ClienteDireccionLocalModel {
        return ClienteDireccionLocalModel(
            localId = cursor.getLong(cursor.getColumnIndexOrThrow("local_id")),
            remoteId = if (cursor.isNull(cursor.getColumnIndexOrThrow("remote_id"))) {
                null
            } else {
                cursor.getLong(cursor.getColumnIndexOrThrow("remote_id"))
            },
            clienteId = cursor.getLong(cursor.getColumnIndexOrThrow("cliente_id")),
            alias = cursor.getString(cursor.getColumnIndexOrThrow("alias")),
            direccionTexto = cursor.getString(cursor.getColumnIndexOrThrow("direccion_texto")),
            latitud = cursor.getDouble(cursor.getColumnIndexOrThrow("latitud")),
            longitud = cursor.getDouble(cursor.getColumnIndexOrThrow("longitud")),
            esPredeterminada = cursor.getInt(cursor.getColumnIndexOrThrow("es_predeterminada")) == 1,
            syncStatus = cursor.getString(cursor.getColumnIndexOrThrow("sync_status")),
            updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at"))
        )
    }
}