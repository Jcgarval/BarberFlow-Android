package com.example.barberflow.models

/** Motivos por los que una contraseña nueva no es válida (mismas reglas que schemas.py en el backend). */
enum class ProblemaPassword { CORTA, LARGA, SIN_LETRA_O_NUMERO }

/** Devuelve null si la contraseña cumple las reglas, o el primer problema encontrado. */
fun problemaDePassword(password: String): ProblemaPassword? = when {
    password.length < 8 -> ProblemaPassword.CORTA
    password.toByteArray(Charsets.UTF_8).size > 72 -> ProblemaPassword.LARGA // límite de bcrypt
    !password.any { it.isLetter() } || !password.any { it.isDigit() } -> ProblemaPassword.SIN_LETRA_O_NUMERO
    else -> null
}

/** El código de recuperación son exactamente 6 dígitos (se admiten ceros a la izquierda). */
fun esCodigoValido(codigo: String): Boolean = codigo.length == 6 && codigo.all { it in '0'..'9' }