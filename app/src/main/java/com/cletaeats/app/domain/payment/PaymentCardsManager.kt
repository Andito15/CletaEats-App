package com.cletaeats.app.domain.payment

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class PaymentCard(
    val id: String,
    val titular: String,
    val marca: String,
    val ultimos4: String,
    val mes: String,
    val anio: String,
    val predeterminada: Boolean
) {
    val displayName: String
        get() = "$marca •••• $ultimos4"

    val vencimiento: String
        get() = "$mes/$anio"
}

object PaymentCardsManager {

    private const val PREFS_NAME = "cletaeats_payment_cards"
    private const val KEY_CARDS = "cards"

    fun getCards(context: Context): List<PaymentCard> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_CARDS, "[]") ?: "[]"

        return try {
            val array = JSONArray(raw)

            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)

                    add(
                        PaymentCard(
                            id = obj.optString("id"),
                            titular = obj.optString("titular"),
                            marca = obj.optString("marca"),
                            ultimos4 = obj.optString("ultimos4"),
                            mes = obj.optString("mes"),
                            anio = obj.optString("anio"),
                            predeterminada = obj.optBoolean("predeterminada")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addCard(
        context: Context,
        numero: String,
        titular: String,
        mes: String,
        anio: String
    ): PaymentCard {
        val cleanNumber = numero.filter { it.isDigit() }
        val cleanTitular = titular.trim().uppercase()
        val cleanMes = mes.filter { it.isDigit() }.padStart(2, '0').takeLast(2)
        val cleanAnio = anio.filter { it.isDigit() }.takeLast(2)

        if (cleanNumber.length < 13) {
            throw IllegalArgumentException("Número de tarjeta inválido.")
        }

        if (cleanTitular.length < 3) {
            throw IllegalArgumentException("Nombre del titular inválido.")
        }

        val mesNum = cleanMes.toIntOrNull()
        if (mesNum == null || mesNum !in 1..12) {
            throw IllegalArgumentException("Mes inválido.")
        }

        if (cleanAnio.length != 2) {
            throw IllegalArgumentException("Año inválido.")
        }

        val currentCards = getCards(context)
        val isFirst = currentCards.isEmpty()

        val newCard = PaymentCard(
            id = UUID.randomUUID().toString(),
            titular = cleanTitular,
            marca = detectBrand(cleanNumber),
            ultimos4 = cleanNumber.takeLast(4),
            mes = cleanMes,
            anio = cleanAnio,
            predeterminada = isFirst
        )

        saveCards(
            context = context,
            cards = currentCards + newCard
        )

        return newCard
    }

    fun setDefault(
        context: Context,
        cardId: String
    ) {
        val updated = getCards(context).map { card ->
            card.copy(
                predeterminada = card.id == cardId
            )
        }

        saveCards(context, updated)
    }

    fun deleteCard(
        context: Context,
        cardId: String
    ) {
        val filtered = getCards(context).filterNot { it.id == cardId }

        val updated = if (
            filtered.isNotEmpty() &&
            filtered.none { it.predeterminada }
        ) {
            filtered.mapIndexed { index, card ->
                card.copy(predeterminada = index == 0)
            }
        } else {
            filtered
        }

        saveCards(context, updated)
    }

    fun getDefaultCard(context: Context): PaymentCard? {
        val cards = getCards(context)

        return cards.firstOrNull { it.predeterminada }
            ?: cards.firstOrNull()
    }

    private fun saveCards(
        context: Context,
        cards: List<PaymentCard>
    ) {
        val array = JSONArray()

        cards.forEach { card ->
            array.put(
                JSONObject().apply {
                    put("id", card.id)
                    put("titular", card.titular)
                    put("marca", card.marca)
                    put("ultimos4", card.ultimos4)
                    put("mes", card.mes)
                    put("anio", card.anio)
                    put("predeterminada", card.predeterminada)
                }
            )
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CARDS, array.toString())
            .apply()
    }

    private fun detectBrand(number: String): String {
        return when {
            number.startsWith("4") -> "Visa"
            number.startsWith("5") -> "Mastercard"
            number.startsWith("34") || number.startsWith("37") -> "Amex"
            number.startsWith("6") -> "Discover"
            else -> "Tarjeta"
        }
    }
}