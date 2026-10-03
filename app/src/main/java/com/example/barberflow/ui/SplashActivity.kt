package com.example.barberflow.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.barberflow.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Entrada suave: el logo aparece creciendo un poco y el texto se desvanece después
        val logo = findViewById<View>(R.id.splash_logo)
        val titulo = findViewById<View>(R.id.splash_titulo)
        val lema = findViewById<View>(R.id.splash_lema)
        logo.alpha = 0f
        logo.scaleX = 0.85f
        logo.scaleY = 0.85f
        titulo.alpha = 0f
        lema.alpha = 0f
        logo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(600).start()
        titulo.animate().alpha(1f).setStartDelay(300).setDuration(500).start()
        lema.animate().alpha(1f).setStartDelay(600).setDuration(500).start()

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