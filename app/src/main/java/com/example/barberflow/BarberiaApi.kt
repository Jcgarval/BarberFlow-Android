package com.example.barberflow

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BarberiaApi {

    @POST("/citas")
    fun crearCita(@Body nuevaCita: Cita): Call<Cita>

    @GET("/citas")
    suspend fun obtenerCitas(@Query("cliente_id") clienteId: Int): List<Cita>

    @GET("/barberos")
    suspend fun obtenerBarberos(): List<Barbero>

    @GET("/servicios")
    suspend fun obtenerServicios(): List<Servicio>

    @POST("/clientes")
    suspend fun crearCliente(@Body nuevoCliente: ClienteCreate): ClienteResponse

    @retrofit2.http.DELETE("/citas/{cita_id}")
    suspend fun eliminarCita(@retrofit2.http.Path("cita_id") citaId: Int): retrofit2.Response<Unit>
}