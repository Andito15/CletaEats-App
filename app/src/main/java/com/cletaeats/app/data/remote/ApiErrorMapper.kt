package com.cletaeats.app.data.remote

import retrofit2.HttpException
import java.io.IOException
import org.json.JSONObject

fun Throwable.toUserMessage(): String {
    return when (this) {
        is HttpException -> {
            when (code()) {
                400 -> "La solicitud tiene datos inválidos. Revisá la información e intentá de nuevo."
                401 -> "Tu sesión expiró. Volvé a iniciar sesión."
                403 -> "No tenés permisos para realizar esta acción."
                404 -> "No se encontró la información solicitada."
                409 -> "No se pudo completar la operación por una regla de negocio."
                500 -> "Ocurrió un error en el servidor. Revisá que el backend esté funcionando correctamente."
                else -> "Error HTTP ${code()}. Intentá de nuevo."
            }
        }

        is IOException -> {
            "No se pudo conectar con el servidor. Revisá tu conexión o que el backend esté encendido."
        }

        else -> {
            message ?: "Ocurrió un error inesperado."
        }
    }
}

fun Throwable.toPedidoMessage(): String {
    return when (this) {
        is HttpException -> {
            when (code()) {
                409 -> "No hay repartidores disponibles para asignar el pedido."
                403 -> "Tu usuario no tiene permiso para crear pedidos."
                401 -> "Tu sesión expiró. Volvé a iniciar sesión."
                500 -> "El backend no pudo crear el pedido. Revisá la consola del servidor."
                else -> toUserMessage()
            }
        }

        else -> toUserMessage()
    }
}

fun Throwable.toTrackingMessage(): String {
    return when (this) {
        is HttpException -> {
            when (code()) {
                403 -> "No podés ver el tracking de este pedido."
                404 -> "No se encontró el pedido o aún no tiene repartidor asignado."
                409 -> "El pedido todavía no está en camino."
                500 -> "No se pudo consultar la ubicación. Revisá el backend o el paquete de tracking en Oracle."
                else -> toUserMessage()
            }
        }

        else -> toUserMessage()
    }
}

fun Throwable.toDeliveryMessage(): String {
    return when (this) {
        is HttpException -> {
            when (code()) {
                403 -> "Este pedido no está asignado a este repartidor."
                404 -> "No se encontró el pedido asignado."
                409 -> "No se pudo cambiar el estado del pedido."
                500 -> "No se pudo actualizar la entrega. Revisá el backend."
                else -> toUserMessage()
            }
        }

        else -> toUserMessage()
    }
}

fun Throwable.toRegisterMessage(): String {
    return when (this) {
        is HttpException -> {
            val body = response()?.errorBody()?.string()
            val backendMessage = body.extractBackendMessage()

            when (code()) {
                400 -> backendMessage ?: "La solicitud tiene datos inválidos. Revisá la información."
                401 -> "Tu sesión expiró. Volvé a iniciar sesión."
                403 -> "No tenés permisos para crear esta cuenta."

                409 -> {
                    when {
                        backendMessage?.contains("correo", ignoreCase = true) == true ->
                            "Ya existe una cuenta registrada con ese correo."

                        backendMessage?.contains("cedula", ignoreCase = true) == true ||
                                backendMessage?.contains("cédula", ignoreCase = true) == true ->
                            "Ya existe una cuenta registrada con esa cédula."

                        backendMessage?.contains("telefono", ignoreCase = true) == true ||
                                backendMessage?.contains("teléfono", ignoreCase = true) == true ->
                            "Ya existe una cuenta registrada con ese teléfono."

                        else ->
                            backendMessage ?: "Ya existe una cuenta con esos datos."
                    }
                }

                500 -> "Ocurrió un error en el servidor al crear la cuenta."
                else -> backendMessage ?: "Error HTTP ${code()}. Intentá de nuevo."
            }
        }

        is IOException -> {
            "No se pudo conectar con el servidor. Revisá tu conexión."
        }

        else -> {
            message ?: "No se pudo crear la cuenta."
        }
    }
}

private fun String?.extractBackendMessage(): String? {
    if (this.isNullOrBlank()) return null

    return try {
        val json = JSONObject(this)

        json.optString("message").ifBlank {
            json.optString("error").ifBlank {
                json.optString("detail").ifBlank {
                    json.optString("title").ifBlank {
                        null
                    }
                }
            }
        }
    } catch (_: Exception) {
        this
    }
}