package com.example.barberflow

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {
    // Abrimos la caja fuerte para buscar el token
    private val prefs = context.getSharedPreferences("BarberFlowPrefs", Context.MODE_PRIVATE)

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = prefs.getString("TOKEN", null)
        val requestBuilder = chain.request().newBuilder()

        // Si tenemos un token guardado, se lo inyectamos a la petición invisiblemente
        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}