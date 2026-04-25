package com.cletaeats.app.domain.session

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("cletaeats_session", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("token", token).apply()
    }

    fun getToken(): String? = prefs.getString("token", null)

    fun saveUserData(
        usuarioId: Long?,
        clienteId: Long?,
        repartidorId: Long?,
        nombre: String?,
        correo: String?,
        rol: String?
    ) {
        prefs.edit()
            .putLong("usuarioId", usuarioId ?: -1L)
            .putLong("clienteId", clienteId ?: -1L)
            .putLong("repartidorId", repartidorId ?: -1L)
            .putString("nombre", nombre)
            .putString("correo", correo)
            .putString("rol", rol)
            .apply()
    }

    fun getUsuarioId(): Long? {
        val value = prefs.getLong("usuarioId", -1L)
        return if (value == -1L) null else value
    }

    fun getClienteId(): Long? {
        val value = prefs.getLong("clienteId", -1L)
        return if (value == -1L) null else value
    }

    fun getRepartidorId(): Long? {
        val value = prefs.getLong("repartidorId", -1L)
        return if (value == -1L) null else value
    }

    fun getNombre(): String? = prefs.getString("nombre", null)
    fun getCorreo(): String? = prefs.getString("correo", null)
    fun getRol(): String? = prefs.getString("rol", null)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}