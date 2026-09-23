package com.example.barberflow

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Usamos una corrutina para esperar 2 segundos
        lifecycleScope.launch {
            delay(2000) // 2000 milisegundos = 2 segundos

            // Saltamos a la pantalla de Login (que decidirá si muestra interfaz o pasa de largo)
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)

            // Cerramos la Splash Screen para que si el usuario pulsa "Atrás", no vuelva aquí
            finish()
        }
    }
}