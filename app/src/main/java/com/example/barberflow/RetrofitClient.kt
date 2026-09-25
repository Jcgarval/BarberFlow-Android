package com.example.barberflow

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Pon aquí tu IP (si usas móvil físico) o 10.0.2.2 (si usas el emulador)
    private const val BASE_URL = "http://192.168.1.23:8000/"

    fun getApi(context: Context): BarberiaApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(BarberiaApi::class.java)
    }
}