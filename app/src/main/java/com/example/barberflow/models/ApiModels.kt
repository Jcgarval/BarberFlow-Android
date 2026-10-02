package com.example.barberflow.models

// Clientes
data class ClienteCreate(val nombre: String, val email: String, val password: String)
data class ClienteResponse(val id: Int, val nombre: String, val email: String, val rol: String)

// Barberos
data class BarberoCreate(val nombre: String)
data class BarberoResponse(val id: Int, val nombre: String)

// Servicios
data class ServicioCreate(val nombre: String, val duracion_minutos: Int, val precio: Double)
data class ServicioResponse(val id: Int, val nombre: String, val duracion_minutos: Int, val precio: Double)

// Citas (vista detallada del administrador)
data class CitaDetalle(
    val id: Int,
    val fecha_hora: String,
    val cliente_nombre: String,
    val barbero_nombre: String,
    val servicio_nombre: String,
    val estado: String? = null
)
