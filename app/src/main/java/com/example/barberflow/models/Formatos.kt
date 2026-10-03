package com.example.barberflow.models

import android.content.Context
import android.content.res.ColorStateList
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.example.barberflow.R
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Estados posibles de una cita (deben coincidir con los del backend). */
object EstadoCita {
    const val PENDIENTE = "pendiente"
    const val CONFIRMADA = "confirmada"
    const val COMPLETADA = "completada"
    const val CANCELADA = "cancelada"

    val todos = listOf(PENDIENTE, CONFIRMADA, COMPLETADA, CANCELADA)

    /** Texto del estado leído de strings.xml (usar siempre esta versión). */
    fun etiqueta(context: Context, estado: String?): String = context.getString(
        when (estado) {
            CONFIRMADA -> R.string.estado_confirmada
            COMPLETADA -> R.string.estado_completada
            CANCELADA -> R.string.estado_cancelada
            else -> R.string.estado_pendiente
        }
    )

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
    textView.text = EstadoCita.etiqueta(textView.context, estado)
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

/** Saca el mensaje que manda FastAPI en {"detail": "..."} o, si no hay, uno genérico leído de strings.xml. */
fun mensajeDeError(context: Context, response: Response<*>): String {
    val detalle = try {
        JSONObject(response.errorBody()?.string() ?: "").optString("detail")
    } catch (e: Exception) {
        ""
    }
    return when {
        detalle.isNotBlank() && !detalle.startsWith("[") -> detalle
        response.code() == 422 -> context.getString(R.string.error_datos_no_validos)
        response.code() == 401 -> context.getString(R.string.error_sesion_caducada)
        else -> context.getString(R.string.error_servidor, response.code())
    }
}

/** Igual, para cuando Retrofit lanza HttpException (llamadas que devuelven el objeto directamente). */
fun mensajeDeError(context: Context, e: HttpException): String {
    val respuesta = e.response()
    return if (respuesta != null) mensajeDeError(context, respuesta) else context.getString(R.string.error_servidor, e.code())
}

/** Admite "15.50" y "15,50" (en español el teclado suele poner la coma). Devuelve null si no es un número válido. */
fun parsearDecimal(texto: String): Double? =
    texto.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }

/** 12.5 -> "12,50 €" */
fun formatearPrecio(precio: Double): String =
    String.format(Locale.forLanguageTag("es-ES"), "%.2f €", precio)

// ---------- Fechas para la pantalla de inicio ----------
private val LOCALE_ES: Locale = Locale.forLanguageTag("es-ES")
private const val FORMATO_ISO = "yyyy-MM-dd'T'HH:mm:ss"

private fun parsearFechaIso(fechaIso: String): Date? = try {
    SimpleDateFormat(FORMATO_ISO, Locale.US).parse(fechaIso)
} catch (e: Exception) {
    null
}

/** "2026-10-03T10:00:00" -> "Sábado 3 de octubre" */
fun formatearDiaLargo(fechaIso: String): String {
    val fecha = parsearFechaIso(fechaIso) ?: return fechaIso
    return SimpleDateFormat("EEEE d 'de' MMMM", LOCALE_ES).format(fecha).replaceFirstChar { it.uppercase() }
}

/** "2026-10-03T10:00:00" -> "10:00" */
fun formatearHora(fechaIso: String): String {
    val fecha = parsearFechaIso(fechaIso) ?: return fechaIso
    return SimpleDateFormat("HH:mm", Locale.US).format(fecha)
}

/** Días naturales que faltan hasta la fecha (0 = hoy, 1 = mañana). Null si la fecha no se entiende. */
fun diasHasta(fechaIso: String): Int? {
    val fecha = parsearFechaIso(fechaIso) ?: return null
    fun inicioDelDia(c: Calendar): Long {
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
    val destino = inicioDelDia(Calendar.getInstance().apply { time = fecha })
    val hoy = inicioDelDia(Calendar.getInstance())
    return Math.round((destino - hoy) / 86_400_000.0).toInt() // el redondeo absorbe los días de 23 o 25 h del cambio de hora
}

/** ¿La cita está activa (pendiente o confirmada) y todavía no ha pasado? */
fun esCitaProxima(cita: Cita): Boolean {
    val activa = cita.estado == null || cita.estado == EstadoCita.PENDIENTE || cita.estado == EstadoCita.CONFIRMADA
    val ahora = SimpleDateFormat(FORMATO_ISO, Locale.US).format(Date())
    return activa && cita.fecha_hora >= ahora
}
