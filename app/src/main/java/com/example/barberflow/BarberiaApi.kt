package com.example.barberflow

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface BarberiaApi {

    @POST("/citas")
    fun crearCita(@Body nuevaCita: Cita): Call<Cita>

    @GET("/citas")
    suspend fun obtenerCitas(): List<Cita>


    @GET("/barberos")
    suspend fun obtenerBarberos(): List<Barbero>

    @GET("/servicios")
    suspend fun obtenerServicios(): List<Servicio>
}