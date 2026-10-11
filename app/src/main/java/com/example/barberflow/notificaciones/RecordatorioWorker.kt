package com.example.barberflow.notificaciones

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.barberflow.R
import com.example.barberflow.models.formatearHora
import com.example.barberflow.ui.MainActivity

/** Muestra la notificación de un recordatorio de cita cuando llega su momento. */
class RecordatorioWorker(contexto: Context, parametros: WorkerParameters) : Worker(contexto, parametros) {

    override fun doWork(): Result {
        val contexto = applicationContext

        // Si se cerró la sesión (o entró otro cliente en el mismo móvil), no se avisa de citas ajenas
        val preferencias = contexto.getSharedPreferences("BarberFlowPrefs", Context.MODE_PRIVATE)
        val idCliente = inputData.getInt(CLAVE_CLIENTE, -1)
        val sesionActiva = preferencias.getString("TOKEN", null) != null &&
            preferencias.getInt("CLIENTE_ID", -1) == idCliente
        if (!sesionActiva) return Result.success()

        // Sin permiso de notificaciones (Android 13+) o desactivadas por el usuario: no hay nada que mostrar
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return Result.success()
        val gestor = NotificationManagerCompat.from(contexto)
        if (!gestor.areNotificationsEnabled()) return Result.success()

        crearCanal(contexto)

        val idCita = inputData.getInt(CLAVE_CITA, 0)
        val horas = inputData.getInt(CLAVE_HORAS, 1)
        val hora = formatearHora(inputData.getString(CLAVE_FECHA).orEmpty())
        val servicio = inputData.getString(CLAVE_SERVICIO).orEmpty()
        val barbero = inputData.getString(CLAVE_BARBERO).orEmpty()
        val texto = contexto.getString(
            if (horas >= 24) R.string.recordatorio_texto_24h else R.string.recordatorio_texto_1h,
            hora, servicio, barbero
        )

        val idNotificacion = idCita * 100 + horas
        val abrirApp = PendingIntent.getActivity(
            contexto,
            idNotificacion,
            Intent(contexto, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificacion = NotificationCompat.Builder(contexto, CANAL_ID)
            .setSmallIcon(R.drawable.ic_nav_citas)
            .setColor(ContextCompat.getColor(contexto, R.color.bf_gold))
            .setContentTitle(contexto.getString(R.string.recordatorio_titulo))
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()

        gestor.notify(idNotificacion, notificacion)
        return Result.success()
    }

    private fun crearCanal(contexto: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val canal = NotificationChannel(
            CANAL_ID,
            contexto.getString(R.string.recordatorio_canal_nombre),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = contexto.getString(R.string.recordatorio_canal_descripcion) }
        contexto.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    companion object {
        const val CANAL_ID = "recordatorios_citas"
        const val CLAVE_CLIENTE = "cliente_id"
        const val CLAVE_CITA = "cita_id"
        const val CLAVE_FECHA = "fecha_hora"
        const val CLAVE_SERVICIO = "servicio"
        const val CLAVE_BARBERO = "barbero"
        const val CLAVE_HORAS = "horas_antes"
    }
}
