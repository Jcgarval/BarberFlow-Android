package com.example.barberflow

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// ==========================================
//    1. INTERFAZ DE LA API (Rutas del Servidor)
// ==========================================
interface BarberiaApi {

    // --- Rutas de Clientes ---
    @POST("/clientes")
    suspend fun crearCliente(@Body nuevoCliente: ClienteCreate): ClienteResponse

    @POST("/login")
    suspend fun login(@Body credenciales: LoginRequest): LoginResponse

    @GET("/citas")
    suspend fun obtenerCitas(@Query("cliente_id") clienteId: Int): List<Cita>

    @POST("/citas")
    fun crearCita(@Body nuevaCita: Cita): Call<Cita>

    @DELETE("/citas/{cita_id}")
    suspend fun eliminarCita(@Path("cita_id") citaId: Int): Response<Unit>

    // --- Rutas Comunes ---
    @GET("/barberos")
    suspend fun obtenerBarberos(): List<Barbero>

    @GET("/servicios")
    suspend fun obtenerServicios(): List<Servicio>

    // --- Rutas de Administrador ---
    @POST("/barberos")
    suspend fun crearBarbero(@Body barbero: BarberoCreate): BarberoResponse

    @POST("/servicios")
    suspend fun crearServicio(@Body servicio: ServicioCreate): ServicioResponse

    @GET("/citas")
    suspend fun obtenerTodasLasCitas(): List<Cita>

    @GET("/admin/citas/detalles")
    suspend fun obtenerCitasDetalladas(): List<CitaDetalle>
}

// ==========================================
//    2. MODELOS DE DATOS (Data Classes)
// ==========================================

// Clientes y Login
data class ClienteCreate(val nombre: String, val email: String, val password: String)
data class ClienteResponse(val id: Int, val nombre: String, val email: String, val rol: String)

// Barberos
data class BarberoCreate(val nombre: String)
data class BarberoResponse(val id: Int, val nombre: String)

// Servicios
data class ServicioCreate(val nombre: String, val duracion_minutos: Int, val precio: Double)
data class ServicioResponse(val id: Int, val nombre: String, val duracion_minutos: Int, val precio: Double)

// Citas
data class CitaDetalle(val id: Int, val fecha_hora: String, val cliente_nombre: String, val barbero_nombre: String, val servicio_nombre: String)