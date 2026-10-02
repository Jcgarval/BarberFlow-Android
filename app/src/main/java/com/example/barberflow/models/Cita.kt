package com.example.barberflow.models

data class BarberoInfo(val nombre: String)
data class ServicioInfo(val nombre: String)

data class Barbero(val id: Int, val nombre: String)
data class Servicio(val id: Int, val nombre: String, val duracion_minutos: Int, val precio: Double)

data class Cita(
    val id: Int = 0,
    var cliente_id: Int,
    var barbero_id: Int,
    var servicio_id: Int,
    var fecha_hora: String,
    var barbero: BarberoInfo = BarberoInfo(""),
    var servicio: ServicioInfo = ServicioInfo(""),
    var estado: String? = EstadoCita.PENDIENTE
)

// Respuesta de GET /citas/disponibilidad
data class DisponibilidadResponse(
    val fecha: String,
    val barbero_id: Int,
    val servicio_id: Int,
    val duracion_minutos: Int,
    val franjas: List<String>   // horas de inicio libres, formato "HH:mm"
)

// Cuerpo de PATCH /citas/{id}/estado
data class EstadoUpdate(val estado: String)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val access_token: String,
    val token_type: String,
    val rol: String,
    val id: Int,
    val nombre: String
)
