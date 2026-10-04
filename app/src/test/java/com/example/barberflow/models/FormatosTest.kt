package com.example.barberflow.models

import com.example.barberflow.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Pruebas unitarias de las utilidades de formato y de las reglas de las citas (se ejecutan en el ordenador, sin emulador). */
class FormatosTest {

    // ------------------------------------------------------------------ ayudas
    /** Fecha ISO "yyyy-MM-dd'T'HH:mm:ss" a la hora indicada, desplazada [diasDesdeHoy] días. */
    private fun iso(diasDesdeHoy: Int, hora: Int, minuto: Int = 0): String {
        val calendario = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, diasDesdeHoy)
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
        }
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(calendario.time)
    }

    private fun cita(estado: String?, fecha: String) =
        Cita(cliente_id = 1, barbero_id = 1, servicio_id = 1, fecha_hora = fecha, estado = estado)

    // ------------------------------------------------------------------ fechas y horas
    @Test
    fun formatearFechaHora_conviertelaFechaIsoEnUnTextoLegible() {
        assertEquals("05/10/2026 10:00", formatearFechaHora("2026-10-05T10:00:00"))
    }

    @Test
    fun formatearFechaHora_siElTextoNoEsUnaFechaLoDevuelveTalCual() {
        assertEquals("no es una fecha", formatearFechaHora("no es una fecha"))
    }

    @Test
    fun formatearHora_devuelveSoloHoraYMinutos() {
        assertEquals("09:05", formatearHora("2026-10-05T09:05:00"))
        assertEquals("19:30", formatearHora("2026-10-05T19:30:00"))
    }

    @Test
    fun formatearHora_siElTextoNoEsUnaFechaLoDevuelveTalCual() {
        assertEquals("xx", formatearHora("xx"))
    }

    @Test
    fun formatearDiaLargo_escribeElDiaConLetraInicialMayuscula() {
        assertEquals("Lunes 5 de octubre", formatearDiaLargo("2026-10-05T10:00:00"))
        assertEquals("Viernes 25 de diciembre", formatearDiaLargo("2026-12-25T18:00:00"))
    }

    @Test
    fun formatearDiaLargo_siElTextoNoEsUnaFechaLoDevuelveTalCual() {
        assertEquals("???", formatearDiaLargo("???"))
    }

    // ------------------------------------------------------------------ días que faltan
    @Test
    fun diasHasta_cuentaDiasNaturales() {
        assertEquals(0, diasHasta(iso(0, 12)))
        assertEquals(1, diasHasta(iso(1, 9)))
        assertEquals(5, diasHasta(iso(5, 20)))
        assertEquals(-1, diasHasta(iso(-1, 12)))
    }

    @Test
    fun diasHasta_conUnTextoInvalidoDevuelveNull() {
        assertNull(diasHasta("no es una fecha"))
    }

    // ------------------------------------------------------------------ próxima cita
    @Test
    fun esCitaProxima_unaCitaFuturaPendienteOConfirmadaSiLoEs() {
        assertTrue(esCitaProxima(cita(EstadoCita.PENDIENTE, iso(1, 10))))
        assertTrue(esCitaProxima(cita(EstadoCita.CONFIRMADA, iso(1, 10))))
    }

    @Test
    fun esCitaProxima_unEstadoDesconocidoSeTrataComoActivo() {
        assertTrue(esCitaProxima(cita(null, iso(1, 10))))
    }

    @Test
    fun esCitaProxima_lasCanceladasYCompletadasNoCuentan() {
        assertFalse(esCitaProxima(cita(EstadoCita.CANCELADA, iso(1, 10))))
        assertFalse(esCitaProxima(cita(EstadoCita.COMPLETADA, iso(1, 10))))
    }

    @Test
    fun esCitaProxima_unaCitaPasadaNoEsProxima() {
        assertFalse(esCitaProxima(cita(EstadoCita.PENDIENTE, iso(-1, 10))))
    }

    // ------------------------------------------------------------------ precios
    @Test
    fun parsearDecimal_aceptaPuntoYComa() {
        assertEquals(15.5, parsearDecimal("15.50")!!, 0.0001)
        assertEquals(15.5, parsearDecimal("15,50")!!, 0.0001)
    }

    @Test
    fun parsearDecimal_ignoraLosEspaciosDeLosExtremos() {
        assertEquals(7.0, parsearDecimal("  7 ")!!, 0.0001)
    }

    @Test
    fun parsearDecimal_rechazaLoQueNoEsUnNumeroValido() {
        assertNull(parsearDecimal("abc"))
        assertNull(parsearDecimal(""))
        assertNull(parsearDecimal("NaN"))
        assertNull(parsearDecimal("Infinity"))
    }

    @Test
    fun formatearPrecio_usaComaDecimalYElSimboloDelEuro() {
        assertEquals("12,50 €", formatearPrecio(12.5))
        assertEquals("0,00 €", formatearPrecio(0.0))
        assertEquals("1234,50 €", formatearPrecio(1234.5))
    }

    // ------------------------------------------------------------------ estados
    @Test
    fun sePuedeCancelar_soloMientrasLaCitaSigueActiva() {
        assertTrue(EstadoCita.sePuedeCancelar(EstadoCita.PENDIENTE))
        assertTrue(EstadoCita.sePuedeCancelar(EstadoCita.CONFIRMADA))
        assertTrue(EstadoCita.sePuedeCancelar(null))
        assertFalse(EstadoCita.sePuedeCancelar(EstadoCita.COMPLETADA))
        assertFalse(EstadoCita.sePuedeCancelar(EstadoCita.CANCELADA))
    }

    @Test
    fun todos_incluyeLosCuatroEstadosEnOrden() {
        assertEquals(listOf("pendiente", "confirmada", "completada", "cancelada"), EstadoCita.todos)
    }

    @Test
    fun color_cadaEstadoTieneSuColorYLosDesconocidosSonPendientes() {
        assertEquals(R.color.bf_estado_confirmada, EstadoCita.color(EstadoCita.CONFIRMADA))
        assertEquals(R.color.bf_estado_completada, EstadoCita.color(EstadoCita.COMPLETADA))
        assertEquals(R.color.bf_estado_cancelada, EstadoCita.color(EstadoCita.CANCELADA))
        assertEquals(R.color.bf_estado_pendiente, EstadoCita.color(EstadoCita.PENDIENTE))
        assertEquals(R.color.bf_estado_pendiente, EstadoCita.color(null))
        assertEquals(R.color.bf_estado_pendiente, EstadoCita.color("otro"))
    }
}
