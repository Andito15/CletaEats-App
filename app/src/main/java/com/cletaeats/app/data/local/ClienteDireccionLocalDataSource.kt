package com.cletaeats.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

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
            ORDER BY es_predeterminada DESC, alias ASC
            """.trimIndent(),
            arrayOf(clienteId.toString())
        )

        return cursor.use {
            it.toModelList()
        }
    }

    fun buscar(
        clienteId: Long,
        query: String
    ): List<ClienteDireccionLocalModel> {
        val db = helper.readableDatabase
        val like = "%${query.trim()}%"

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
            ORDER BY es_predeterminada DESC, alias ASC
            """.trimIndent(),
            arrayOf(
                clienteId.toString(),
                like,
                like
            )
        )

        return cursor.use {
            it.toModelList()
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
                it.toModel()
            } else {
                null
            }
        }
    }

    fun pendientesSync(): List<ClienteDireccionLocalModel> {
        val db = helper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT *
            FROM cliente_direcciones
            WHERE sync_status IN (
                'PENDING_CREATE',
                'PENDING_UPDATE',
                'PENDING_DELETE'
            )
            ORDER BY updated_at ASC
            """.trimIndent(),
            null
        )

        return cursor.use {
            it.toModelList()
        }
    }

    fun guardar(model: ClienteDireccionLocalModel): Long {
        val db = helper.writableDatabase

        return db.insertWithOnConflict(
            "cliente_direcciones",
            null,
            model.toContentValues(includeLocalId = model.localId > 0),
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun guardarTodas(models: List<ClienteDireccionLocalModel>) {
        val db = helper.writableDatabase

        db.beginTransaction()

        try {
            models.forEach { model ->
                db.insertWithOnConflict(
                    "cliente_direcciones",
                    null,
                    model.toContentValues(includeLocalId = model.localId > 0),
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun actualizar(model: ClienteDireccionLocalModel) {
        val db = helper.writableDatabase

        if (model.localId <= 0) {
            guardar(model)
            return
        }

        db.update(
            "cliente_direcciones",
            model.toContentValues(includeLocalId = false),
            "local_id = ?",
            arrayOf(model.localId.toString())
        )
    }

    fun quitarPredeterminadas(clienteId: Long) {
        val db = helper.writableDatabase

        db.execSQL(
            """
            UPDATE cliente_direcciones
            SET es_predeterminada = 0,
                sync_status = CASE
                    WHEN sync_status = 'PENDING_DELETE' THEN 'PENDING_DELETE'
                    WHEN remote_id IS NULL THEN 'PENDING_CREATE'
                    ELSE 'PENDING_UPDATE'
                END,
                updated_at = ?
            WHERE cliente_id = ?
              AND sync_status != 'PENDING_DELETE'
            """.trimIndent(),
            arrayOf(
                System.currentTimeMillis(),
                clienteId
            )
        )
    }

    fun limpiarCliente(clienteId: Long) {
        val db = helper.writableDatabase

        db.delete(
            "cliente_direcciones",
            "cliente_id = ?",
            arrayOf(clienteId.toString())
        )
    }

    fun marcarPendienteDelete(model: ClienteDireccionLocalModel) {
        actualizar(
            model.copy(
                syncStatus = "PENDING_DELETE",
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun eliminarDefinitivo(
        localId: Long,
        remoteId: Long?
    ) {
        val db = helper.writableDatabase

        if (remoteId == null) {
            db.delete(
                "cliente_direcciones",
                "local_id = ?",
                arrayOf(localId.toString())
            )
        } else {
            db.delete(
                "cliente_direcciones",
                "local_id = ? OR remote_id = ?",
                arrayOf(
                    localId.toString(),
                    remoteId.toString()
                )
            )
        }
    }

    fun reemplazarLocalPorRemoto(
        localId: Long,
        remoto: ClienteDireccionLocalModel
    ) {
        eliminarDefinitivo(
            localId = localId,
            remoteId = null
        )

        guardar(
            remoto.copy(
                syncStatus = "SYNCED",
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun marcarSincronizado(model: ClienteDireccionLocalModel) {
        actualizar(
            model.copy(
                syncStatus = "SYNCED",
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private fun ClienteDireccionLocalModel.toContentValues(
        includeLocalId: Boolean
    ): ContentValues {
        return ContentValues().apply {
            if (includeLocalId) {
                put("local_id", localId)
            }

            if (remoteId == null) {
                putNull("remote_id")
            } else {
                put("remote_id", remoteId)
            }

            put("cliente_id", clienteId)
            put("alias", alias)
            put("direccion_texto", direccionTexto)
            put("latitud", latitud)
            put("longitud", longitud)
            put("es_predeterminada", if (esPredeterminada) 1 else 0)
            put("sync_status", syncStatus)
            put("updated_at", updatedAt)
        }
    }

    private fun Cursor.toModelList(): List<ClienteDireccionLocalModel> {
        val result = mutableListOf<ClienteDireccionLocalModel>()

        while (moveToNext()) {
            result.add(toModel())
        }

        return result
    }

    private fun Cursor.toModel(): ClienteDireccionLocalModel {
        return ClienteDireccionLocalModel(
            localId = getLong(getColumnIndexOrThrow("local_id")),
            remoteId = getNullableLong("remote_id"),
            clienteId = getLong(getColumnIndexOrThrow("cliente_id")),
            alias = getString(getColumnIndexOrThrow("alias")),
            direccionTexto = getString(getColumnIndexOrThrow("direccion_texto")),
            latitud = getDouble(getColumnIndexOrThrow("latitud")),
            longitud = getDouble(getColumnIndexOrThrow("longitud")),
            esPredeterminada = getInt(getColumnIndexOrThrow("es_predeterminada")) == 1,
            syncStatus = getString(getColumnIndexOrThrow("sync_status")),
            updatedAt = getLong(getColumnIndexOrThrow("updated_at"))
        )
    }

    private fun Cursor.getNullableLong(
        columnName: String
    ): Long? {
        val index = getColumnIndexOrThrow(columnName)

        return if (isNull(index)) {
            null
        } else {
            getLong(index)
        }
    }
}