package com.example.barberflow.notificaciones

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas unitarias del cálculo de cuándo se avisa de una cita (se ejecutan en el ordenador). */
class RecordatoriosCitasTest {

    private val hora = 3_600_000L

    @Test
    fun retrasosDeRecordatorio_unaCitaLejanaTieneLosDosAvisos() {
        val resultado = retrasosDeRecordatorio(momentoCitaMillis = 48 * hora, ahoraMillis = 0)
        assertEquals(listOf(24 to 24 * hora, 1 to 47 * hora), resultado)
    }

    @Test
    fun retrasosDeRecordatorio_conDosHorasDeMargenSoloQuedaElAvisoDeUnaHora() {
        val resultado = retrasosDeRecordatorio(momentoCitaMillis = 2 * hora, ahoraMillis = 0)
        assertEquals(listOf(1 to 1 * hora), resultado)
    }

    @Test
    fun retrasosDeRecordatorio_conMediaHoraDeMargenNoHayAvisos() {
        assertTrue(retrasosDeRecordatorio(momentoCitaMillis = hora / 2, ahoraMillis = 0).isEmpty())
    }

    @Test
    fun retrasosDeRecordatorio_unaCitaPasadaNoTieneAvisos() {
        assertTrue(retrasosDeRecordatorio(momentoCitaMillis = 0, ahoraMillis = 5 * hora).isEmpty())
    }

    @Test
    fun retrasosDeRecordatorio_unAvisoQueCaeJustoAhoraSeDescarta() {
        assertTrue(retrasosDeRecordatorio(momentoCitaMillis = hora, ahoraMillis = 0).isEmpty())
    }
}
