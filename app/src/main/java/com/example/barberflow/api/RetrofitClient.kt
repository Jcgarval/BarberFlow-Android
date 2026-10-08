package com.example.barberflow.api

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // API desplegada en Render. Para probar contra tu propio equipo, cambia temporalmente esta línea:
    //   "http://10.0.2.2:8000/"        -> emulador de Android Studio
    //   "http://192.168.1.XX:8000/"    -> móvil físico (IP de tu equipo en la red local)
    // y añade android:usesCleartextTraffic="true" al <application> del AndroidManifest.xml (solo para desarrollo).
    private const val BASE_URL = "https://barberflow-api-cko3.onrender.com/"

    fun getApi(context: Context): BarberiaApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            // El plan gratuito de Render duerme el servicio tras un rato sin uso y la primera
            // petición puede tardar hasta un minuto: con los 10 s por defecto fallaría.
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(BarberiaApi::class.java)
    }
}
