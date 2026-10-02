package com.example.barberflow.models

import android.content.res.ColorStateList
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.example.barberflow.R
import org.json.JSONObject
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

/** Estados posibles de una cita (deben coincidir con los del backend). */
object EstadoCita {
    const val PENDIENTE = "pendiente"
    const val CONFIRMADA = "confirmada"
    const val COMPLETADA = "completada"
    const val CANCELADA = "cancelada"

    val todos = listOf(PENDIENTE, CONFIRMADA, COMPLETADA, CANCELADA)

    fun etiqueta(estado: String?): String = when (estado) {
        CONFIRMADA -> "Confirmada"
        COMPLETADA -> "Completada"
        CANCELADA -> "Cancelada"
        else -> "Pendiente"
    }

    @ColorRes
    fun color(estado: String?): Int = when (estado) {
        CONFIRMADA -> R.color.bf_estado_confirmada
        COMPLETADA -> R.color.bf_estado_completada
        CANCELADA -> R.color.bf_estado_cancelada
        else -> R.color.bf_estado_pendiente
    }

    /** Una cita solo se puede cancelar mientras no esté completada ni cancelada. */
    fun sePuedeCancelar(estado: String?): Boolean = estado != COMPLETADA && estado != CANCELADA
}

/** Pinta una "etiqueta" redondeada con el texto y el color del estado. */
fun aplicarBadgeEstado(textView: TextView, estado: String?) {
    textView.text = EstadoCita.etiqueta(estado)
    val color = ContextCompat.getColor(textView.context, EstadoCita.color(estado))
    textView.backgroundTintList = ColorStateList.valueOf(color)
}

/** "2026-10-03T10:00:00" -> "03/10/2026 10:00". Funciona en todas las versiones de Android (minSdk 24). */
fun formatearFechaHora(fechaIso: String): String {
    return try {
        val entrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val salida = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        salida.format(entrada.parse(fechaIso)!!)
    } catch (e: Exception) {
        fechaIso
    }
}

/** Saca el mensaje que manda FastAPI en {"detail": "..."} o, si no hay, uno genérico. */
fun mensajeDeError(response: Response<*>): String {
    val detalle = try {
        JSONObject(response.errorBody()?.string() ?: "").optString("detail")
    } catch (e: Exception) {
        ""
    }
    return when {
        detalle.isNotBlank() && !detalle.startsWith("[") -> detalle
        response.code() == 401 -> "Tu sesión ha caducado. Vuelve a iniciar sesión."
        else -> "Error del servidor: ${response.code()}"
    }
}
