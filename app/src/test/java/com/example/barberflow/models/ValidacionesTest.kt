package com.example.barberflow.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas unitarias de las validaciones de la recuperación de contraseña (sin emulador). */
class ValidacionesTest {

    @Test
    fun problemaDePassword_unaContrasenaCorrectaNoTieneProblemas() {
        assertNull(problemaDePassword("clave1234"))
    }

    @Test
    fun problemaDePassword_menosDe8CaracteresEsCorta() {
        assertEquals(ProblemaPassword.CORTA, problemaDePassword("abc123"))
        assertEquals(ProblemaPassword.CORTA, problemaDePassword(""))
    }

    @Test
    fun problemaDePassword_hacenFaltaUnaLetraYUnNumero() {
        assertEquals(ProblemaPassword.SIN_LETRA_O_NUMERO, problemaDePassword("solo-letras"))
        assertEquals(ProblemaPassword.SIN_LETRA_O_NUMERO, problemaDePassword("12345678"))
    }

    @Test
    fun problemaDePassword_ellimiteSonLos72BytesNoLosCaracteres() {
        assertNull(problemaDePassword("a1" + "x".repeat(70)))                         // 72 bytes: válida
        assertEquals(ProblemaPassword.LARGA, problemaDePassword("a1" + "x".repeat(71))) // 73 bytes
        assertEquals(ProblemaPassword.LARGA, problemaDePassword("ñ".repeat(37) + "1")) // 38 caracteres pero 75 bytes
    }

    @Test
    fun esCodigoValido_aceptaSeisDigitosIncluidosLosCerosIniciales() {
        assertTrue(esCodigoValido("123456"))
        assertTrue(esCodigoValido("012345"))
    }

    @Test
    fun esCodigoValido_rechazaLoQueNoSeanSeisDigitos() {
        assertFalse(esCodigoValido("12345"))
        assertFalse(esCodigoValido("1234567"))
        assertFalse(esCodigoValido("12ab56"))
        assertFalse(esCodigoValido(" 12345"))
        assertFalse(esCodigoValido(""))
    }
}