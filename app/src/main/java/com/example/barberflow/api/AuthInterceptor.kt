package com.example.barberflow.api

import android.content.Context
import android.content.Intent
import com.example.barberflow.ui.LoginActivity
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("BarberFlowPrefs", Context.MODE_PRIVATE)

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = prefs.getString("TOKEN", null)
        val requestBuilder = chain.request().newBuilder()

        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val respuesta = chain.proceed(requestBuilder.build())

        // 401 con un token enviado = sesión caducada o token inválido
        if (respuesta.code() == 401 && token != null) {
            cerrarSesion()
        }
        return respuesta
    }

    private fun cerrarSesion() {
        // Si otra petición simultánea ya cerró la sesión, no hacemos nada
        if (prefs.getString("TOKEN", null) == null) return

        prefs.edit().clear().commit()

        val intent = Intent(appContext, LoginActivity::class.java).apply {
            // Vacía la pila de pantallas para que "Atrás" no vuelva a una pantalla sin sesión
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("SESION_CADUCADA", true)
        }
        appContext.startActivity(intent)
    }
}