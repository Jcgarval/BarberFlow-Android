package com.example.barberflow

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.Response

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

    @DELETE("/citas/{cita_id}")
    suspend fun eliminarCita(@Path("cita_id") citaId: Int): Response<Unit>

    @POST("login")
    suspend fun login(@Body credenciales: LoginRequest): LoginResponse

    // ==========================================
    //    NUEVAS RUTAS PARA EL ADMINISTRADOR
    // ==========================================

    @POST("/barberos")
    suspend fun crearBarbero(@Body barbero: BarberoCreate): BarberoResponse

    @POST("/servicios")
    suspend fun crearServicio(@Body servicio: ServicioCreate): ServicioResponse
}

// ==========================================
//    NUEVOS MODELOS PARA EL ADMINISTRADOR
// ==========================================
data class BarberoCreate(val nombre: String)
data class BarberoResponse(val id: Int, val nombre: String)

data class ServicioCreate(val nombre: String, val duracion_minutos: Int, val precio: Double)
data class ServicioResponse(val id: Int, val nombre: String, val duracion_minutos: Int, val precio: Double)
