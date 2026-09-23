package com.example.barberflow

data class BarberoInfo(val nombre: String)
data class ServicioInfo(val nombre: String)

data class Cita(
    var cliente_id: Int,
    var barbero_id: Int,
    var servicio_id: Int,
    var fecha_hora: String,
    var barbero: BarberoInfo = BarberoInfo(""),
    var servicio: ServicioInfo = ServicioInfo("")
)