package com.example.barberflow

data class BarberoInfo(val nombre: String)
data class ServicioInfo(val nombre: String)

data class Barbero(val id: Int, val nombre: String)
data class Servicio(val id: Int, val nombre: String)

data class Cita(
    val id: Int = 0,
    var cliente_id: Int,
    var barbero_id: Int,
    var servicio_id: Int,
    var fecha_hora: String,
    var barbero: BarberoInfo = BarberoInfo(""),
    var servicio: ServicioInfo = ServicioInfo("")
)

data class ClienteCreate(val nombre: String, val telefono: String)
data class ClienteResponse(val id: Int, val nombre: String, val telefono: String)