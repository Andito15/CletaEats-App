package com.cletaeats.app.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class CletaSQLiteHelper(
    context: Context
) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE cliente_direcciones (
                local_id INTEGER PRIMARY KEY AUTOINCREMENT,
                remote_id INTEGER,
                cliente_id INTEGER NOT NULL,
                alias TEXT NOT NULL,
                direccion_texto TEXT NOT NULL,
                latitud REAL NOT NULL,
                longitud REAL NOT NULL,
                es_predeterminada INTEGER NOT NULL DEFAULT 0,
                sync_status TEXT NOT NULL DEFAULT 'SYNCED',
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS cliente_direcciones")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "cletaeats_local.db"
        private const val DATABASE_VERSION = 1
    }
}