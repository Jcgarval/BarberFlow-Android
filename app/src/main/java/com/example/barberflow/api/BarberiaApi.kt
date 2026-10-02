package com.example.barberflow.api

import com.example.barberflow.models.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface BarberiaApi {

    // --- Rutas de Clientes ---
    @POST("/clientes/")
    suspend fun crearCliente(@Body nuevoCliente: ClienteCreate): ClienteResponse

    @POST("/login")
    suspend fun login(@Body credenciales: LoginRequest): LoginResponse

    @GET("/citas/")
    suspend fun obtenerCitas(@Query("cliente_id") clienteId: Int): List<Cita>

    @POST("/citas/")
    fun crearCita(@Body nuevaCita: Cita): Call<Cita>

    @DELETE("/citas/{cita_id}")
    suspend fun eliminarCita(@Path("cita_id") citaId: Int): Response<Unit>

    // --- Disponibilidad y estados de cita ---
    @GET("/citas/disponibilidad")
    suspend fun obtenerDisponibilidad(
        @Query("barbero_id") barberoId: Int,
        @Query("servicio_id") servicioId: Int,
        @Query("fecha") fecha: String   // formato yyyy-MM-dd
    ): DisponibilidadResponse

    @PATCH("/citas/{cita_id}/estado")
    suspend fun cambiarEstadoCita(
        @Path("cita_id") citaId: Int,
        @Body cuerpo: EstadoUpdate
    ): Response<Cita>

    // --- Rutas Comunes ---
    @GET("/barberos/")
    suspend fun obtenerBarberos(): List<Barbero>

    @GET("/servicios/")
    suspend fun obtenerServicios(): List<Servicio>

    // --- Rutas de Administrador ---

    // Gestión de Barberos
    @POST("/barberos/")
    suspend fun crearBarbero(@Body barbero: BarberoCreate): BarberoResponse

    @PUT("/barberos/{barbero_id}")
    suspend fun actualizarBarbero(@Path("barbero_id") barberoId: Int, @Body barbero: BarberoCreate): BarberoResponse

    @DELETE("/barberos/{barbero_id}")
    suspend fun eliminarBarbero(@Path("barbero_id") barberoId: Int): Response<Unit>

    // Gestión de Servicios
    @POST("/servicios/")
    suspend fun crearServicio(@Body servicio: ServicioCreate): ServicioResponse

    @PUT("/servicios/{servicio_id}")
    suspend fun actualizarServicio(@Path("servicio_id") servicioId: Int, @Body servicio: ServicioCreate): ServicioResponse

    @DELETE("/servicios/{servicio_id}")
    suspend fun eliminarServicio(@Path("servicio_id") servicioId: Int): Response<Unit>

    // Gestión de Citas Generales
    @GET("/citas/")
    suspend fun obtenerTodasLasCitas(): List<Cita>

    @GET("/admin/citas/detalles")
    suspend fun obtenerCitasDetalladas(): List<CitaDetalle>
}
