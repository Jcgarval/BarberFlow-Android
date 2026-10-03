package com.example.barberflow.ui

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentTransaction
import com.example.barberflow.R
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Pantalla principal del cliente: una barra de navegación inferior con 4 secciones.
 * Los fragmentos se crean una sola vez y se muestran/ocultan, así no se pierde lo que
 * el usuario ha elegido en "Reservar" al cambiar de pestaña.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var barraInferior: BottomNavigationView

    private val etiquetas = mapOf(
        R.id.nav_inicio to "inicio",
        R.id.nav_reservar to "reservar",
        R.id.nav_citas to "citas",
        R.id.nav_perfil to "perfil"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Margen superior para la barra de estado. El margen inferior lo gestiona la propia barra de navegación.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, barras.top, barras.right, 0)
            insets
        }

        barraInferior = findViewById(R.id.bottom_nav)

        if (savedInstanceState == null) {
            // Primera vez: creamos las 4 secciones y dejamos visible solo "Inicio"
            val inicio = InicioFragment()
            val reservar = ReservaFragment()
            val citas = CitasFragment()
            val perfil = PerfilFragment()
            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.contenedor_fragmentos, inicio, "inicio")
                .add(R.id.contenedor_fragmentos, reservar, "reservar")
                .add(R.id.contenedor_fragmentos, citas, "citas")
                .add(R.id.contenedor_fragmentos, perfil, "perfil")
                .hide(reservar)
                .hide(citas)
                .hide(perfil)
                .commitNow()
        }

        barraInferior.setOnItemSelectedListener { opcion ->
            mostrarSeccion(opcion.itemId)
            true
        }

        // Atrás: desde cualquier sección vuelve a "Inicio"; desde "Inicio" sale de la app
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (barraInferior.selectedItemId != R.id.nav_inicio) {
                    barraInferior.selectedItemId = R.id.nav_inicio
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun mostrarSeccion(idOpcion: Int) {
        val transaccion = supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        etiquetas.forEach { (id, etiqueta) ->
            val fragmento = supportFragmentManager.findFragmentByTag(etiqueta) ?: return@forEach
            if (id == idOpcion) transaccion.show(fragmento) else transaccion.hide(fragmento)
        }
        transaccion.commit()
    }

    /** Lo usan los fragmentos para cambiar de sección (por ejemplo, tras reservar una cita). */
    fun irA(idOpcion: Int) {
        barraInferior.selectedItemId = idOpcion
    }
}
