package com.cletaeats.app.domain.session

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(
        "cletaeats_session",
        Context.MODE_PRIVATE
    )

    fun saveToken(token: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun saveUserData(
        usuarioId: Long?,
        clienteId: Long?,
        repartidorId: Long?,
        nombre: String?,
        correo: String?,
        rol: String?,
        fotoUrl: String? = null
    ) {
        prefs.edit()
            .putLong(KEY_USUARIO_ID, usuarioId ?: -1L)
            .putLong(KEY_CLIENTE_ID, clienteId ?: -1L)
            .putLong(KEY_REPARTIDOR_ID, repartidorId ?: -1L)
            .putString(KEY_NOMBRE, nombre)
            .putString(KEY_CORREO, correo)
            .putString(KEY_ROL, rol)
            .putString(KEY_FOTO_URL, fotoUrl?.takeIf { it.isNotBlank() })
            .apply()
    }

    fun getUsuarioId(): Long? {
        val value = prefs.getLong(KEY_USUARIO_ID, -1L)
        return if (value == -1L) null else value
    }

    fun getClienteId(): Long? {
        val value = prefs.getLong(KEY_CLIENTE_ID, -1L)
        return if (value == -1L) null else value
    }

    fun getRepartidorId(): Long? {
        val value = prefs.getLong(KEY_REPARTIDOR_ID, -1L)
        return if (value == -1L) null else value
    }

    fun getNombre(): String? {
        return prefs.getString(KEY_NOMBRE, null)
    }

    fun getCorreo(): String? {
        return prefs.getString(KEY_CORREO, null)
    }

    fun getRol(): String? {
        return prefs.getString(KEY_ROL, null)
    }

    fun getFotoUrl(): String? {
        return prefs.getString(KEY_FOTO_URL, null)
    }

    fun clearSession() {
        prefs.edit()
            .clear()
            .apply()
    }

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USUARIO_ID = "usuarioId"
        private const val KEY_CLIENTE_ID = "clienteId"
        private const val KEY_REPARTIDOR_ID = "repartidorId"
        private const val KEY_NOMBRE = "nombre"
        private const val KEY_CORREO = "correo"
        private const val KEY_ROL = "rol"
        private const val KEY_FOTO_URL = "fotoUrl"
    }
}