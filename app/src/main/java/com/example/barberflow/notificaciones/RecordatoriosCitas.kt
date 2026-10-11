package com.example.barberflow.notificaciones

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.barberflow.models.Cita
import com.example.barberflow.models.esCitaProxima
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val MILLIS_POR_HORA = 3_600_000L

/**
 * Para cada antelación (en horas) devuelve el par (horas, espera en milisegundos) hasta ese aviso.
 * Los avisos cuyo momento ya ha pasado se descartan: una cita reservada con 3 h de margen
 * solo tendrá el aviso de 1 h, y una reservada con 30 min de margen no tendrá ninguno.
 */
fun retrasosDeRecordatorio(
    momentoCitaMillis: Long,
    ahoraMillis: Long,
    horasAntes: List<Int> = RecordatoriosCitas.HORAS_ANTES
): List<Pair<Int, Long>> =
    horasAntes
        .map { horas -> horas to (momentoCitaMillis - horas * MILLIS_POR_HORA - ahoraMillis) }
        .filter { (_, espera) -> espera > 0 }

/** Programa y cancela los recordatorios locales de las citas del cliente con WorkManager. */
object RecordatoriosCitas {
    /** Cuánto antes de la cita se avisa. */
    val HORAS_ANTES = listOf(24, 1)

    private const val ETIQUETA = "recordatorio_cita"
    private const val FORMATO_ISO = "yyyy-MM-dd'T'HH:mm:ss"

    /**
     * Deja programados exactamente los avisos que corresponden a [citas]: borra los anteriores y crea
     * los de las citas activas y futuras. Se llama cada vez que la app descarga las citas del cliente,
     * así reservar, cancelar o un cambio hecho por el administrador se reflejan solos.
     */
    fun sincronizar(context: Context, citas: List<Cita>) {
        val appContext = context.applicationContext
        val gestor = WorkManager.getInstance(appContext)
        gestor.cancelAllWorkByTag(ETIQUETA)

        val preferencias = appContext.getSharedPreferences("BarberFlowPrefs", Context.MODE_PRIVATE)
        val idCliente = preferencias.getInt("CLIENTE_ID", -1)
        if (idCliente == -1) return

        val ahora = System.currentTimeMillis()
        citas.filter { esCitaProxima(it) }.forEach { cita ->
            val momentoCita = fechaIsoAMillis(cita.fecha_hora) ?: return@forEach
            retrasosDeRecordatorio(momentoCita, ahora).forEach { (horas, espera) ->
                val datos = workDataOf(
                    RecordatorioWorker.CLAVE_CLIENTE to idCliente,
                    RecordatorioWorker.CLAVE_CITA to cita.id,
                    RecordatorioWorker.CLAVE_FECHA to cita.fecha_hora,
                    RecordatorioWorker.CLAVE_SERVICIO to cita.servicio.nombre,
                    RecordatorioWorker.CLAVE_BARBERO to cita.barbero.nombre,
                    RecordatorioWorker.CLAVE_HORAS to horas
                )
                val peticion = OneTimeWorkRequestBuilder<RecordatorioWorker>()
                    .setInitialDelay(espera, TimeUnit.MILLISECONDS)
                    .setInputData(datos)
                    .addTag(ETIQUETA)
                    .build()
                gestor.enqueueUniqueWork("recordatorio_${cita.id}_$horas", ExistingWorkPolicy.REPLACE, peticion)
            }
        }
    }

    /** Cancela todos los avisos pendientes (por ejemplo, al cerrar sesión). */
    fun cancelarTodos(context: Context) {
        WorkManager.getInstance(context.applicationContext).cancelAllWorkByTag(ETIQUETA)
    }

    private fun fechaIsoAMillis(fechaIso: String): Long? = try {
        SimpleDateFormat(FORMATO_ISO, Locale.US).parse(fechaIso)?.time
    } catch (e: Exception) {
        null
    }
}
