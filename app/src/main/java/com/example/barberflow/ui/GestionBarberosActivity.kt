package com.example.barberflow.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.barberflow.R
import com.example.barberflow.adapters.BarberoAdapter
import com.example.barberflow.api.RetrofitClient
import com.example.barberflow.models.Barbero
import com.example.barberflow.models.BarberoCreate
import com.example.barberflow.models.mensajeDeError
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import retrofit2.HttpException

class GestionBarberosActivity : AppCompatActivity() {

    private val api by lazy { RetrofitClient.getApi(this) }
    private lateinit var rvBarberos: RecyclerView
    private lateinit var adapter: BarberoAdapter
    private lateinit var fabAddBarbero: ExtendedFloatingActionButton
    private lateinit var tvVacio: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_barberos)

        // Evita que el contenido quede bajo la barra de estado en Android recientes
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_gestion_barberos)) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar_barberos)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.inflateMenu(R.menu.menu_gestion)
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_bajas) {
                mostrarBajas()
                true
            } else {
                false
            }
        }

        rvBarberos = findViewById(R.id.rvBarberos)
        fabAddBarbero = findViewById(R.id.fabAddBarbero)
        tvVacio = findViewById(R.id.tvVacioBarberos)

        rvBarberos.layoutManager = LinearLayoutManager(this)
        adapter = BarberoAdapter(
            barberos = emptyList(),
            onEditClick = { barbero -> mostrarDialogoBarbero(barbero) },
            onDeleteClick = { barbero -> mostrarDialogoEliminar(barbero) }
        )
        rvBarberos.adapter = adapter

        fabAddBarbero.setOnClickListener { mostrarDialogoBarbero(null) }

        cargarBarberos()
    }

    private fun avisar(mensaje: String) {
        Snackbar.make(findViewById(R.id.root_gestion_barberos), mensaje, Snackbar.LENGTH_LONG)
            .setAnchorView(fabAddBarbero)
            .show()
    }

    private fun cargarBarberos() {
        lifecycleScope.launch {
            try {
                val lista = api.obtenerBarberos()
                adapter.actualizarLista(lista)
                tvVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                avisar("No se pudieron cargar los barberos")
            }
        }
    }

    // ---------- Crear y editar (mismo diálogo) ----------
    private fun mostrarDialogoBarbero(barbero: Barbero?) {
        val vista = layoutInflater.inflate(R.layout.dialog_barbero, null)
        val campo = vista.findViewById<TextInputLayout>(R.id.til_nombre_barbero)
        val nombre = vista.findViewById<TextInputEditText>(R.id.et_nombre_barbero)

        if (barbero != null) {
            nombre.setText(barbero.nombre)
            nombre.setSelection(barbero.nombre.length)
        }
        nombre.doAfterTextChanged { campo.error = null }

        val dialogo = MaterialAlertDialogBuilder(this)
            .setTitle(if (barbero == null) "Nuevo barbero" else "Editar barbero")
            .setView(vista)
            .setPositiveButton(if (barbero == null) "Guardar" else "Actualizar", null) // se define abajo para poder NO cerrar si hay errores
            .setNegativeButton("Cancelar", null)
            .create()

        dialogo.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialogo.setOnShowListener {
            nombre.requestFocus()
            dialogo.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val texto = nombre.text.toString().trim()
                when {
                    texto.isEmpty() -> campo.error = "Escribe un nombre"
                    texto.length > 60 -> campo.error = "Máximo 60 caracteres"
                    barbero != null && texto == barbero.nombre -> dialogo.dismiss() // no ha cambiado nada
                    else -> {
                        dialogo.dismiss()
                        if (barbero == null) crearBarberoEnApi(texto) else actualizarBarberoEnApi(barbero.id, texto)
                    }
                }
            }
        }
        dialogo.show()
    }

    private fun crearBarberoEnApi(nombre: String) {
        lifecycleScope.launch {
            try {
                api.crearBarbero(BarberoCreate(nombre))
                avisar("Barbero guardado")
                cargarBarberos()
            } catch (e: HttpException) {
                avisar(mensajeDeError(e))
            } catch (e: Exception) {
                avisar("No se pudo conectar con el servidor")
            }
        }
    }

    private fun actualizarBarberoEnApi(id: Int, nuevoNombre: String) {
        lifecycleScope.launch {
            try {
                api.actualizarBarbero(id, BarberoCreate(nuevoNombre))
                avisar("Barbero actualizado")
                cargarBarberos()
            } catch (e: HttpException) {
                avisar(mensajeDeError(e))
            } catch (e: Exception) {
                avisar("No se pudo conectar con el servidor")
            }
        }
    }

    // ---------- Eliminar ----------
    private fun mostrarDialogoEliminar(barbero: Barbero) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar barbero")
            .setMessage(
                "¿Quieres eliminar a ${barbero.nombre}?\n\n" +
                    "Si tiene citas asociadas se dará de baja: no se podrá reservar con él, pero el historial se conserva."
            )
            .setPositiveButton("Eliminar") { _, _ -> eliminarBarberoEnApi(barbero.id) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarBarberoEnApi(id: Int) {
        lifecycleScope.launch {
            try {
                val respuesta = api.eliminarBarbero(id)
                if (respuesta.isSuccessful) {
                    // El servidor explica si se borró o se dio de baja
                    avisar(respuesta.body()?.mensaje ?: "Barbero eliminado")
                    cargarBarberos()
                } else {
                    avisar(mensajeDeError(respuesta))
                }
            } catch (e: Exception) {
                avisar("No se pudo conectar con el servidor")
            }
        }
    }

    // ---------- Barberos dados de baja: ver y reactivar ----------
    private fun mostrarBajas() {
        lifecycleScope.launch {
            try {
                val bajas = api.obtenerBarberosInactivos()
                if (bajas.isEmpty()) {
                    avisar("No hay barberos dados de baja")
                    return@launch
                }
                val nombres = bajas.map { it.nombre }.toTypedArray()
                MaterialAlertDialogBuilder(this@GestionBarberosActivity)
                    .setTitle("Barberos dados de baja")
                    .setItems(nombres) { _, posicion -> confirmarReactivacion(bajas[posicion]) }
                    .setNegativeButton("Cerrar", null)
                    .show()
            } catch (e: HttpException) {
                avisar(mensajeDeError(e))
            } catch (e: Exception) {
                avisar("No se pudo conectar con el servidor")
            }
        }
    }

    private fun confirmarReactivacion(barbero: Barbero) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Reactivar barbero")
            .setMessage("¿Quieres que ${barbero.nombre} vuelva a aparecer para reservar citas?")
            .setPositiveButton("Reactivar") { _, _ -> reactivarBarberoEnApi(barbero.id) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun reactivarBarberoEnApi(id: Int) {
        lifecycleScope.launch {
            try {
                val barbero = api.reactivarBarbero(id)
                avisar("${barbero.nombre} vuelve a estar activo")
                cargarBarberos()
            } catch (e: HttpException) {
                avisar(mensajeDeError(e))
            } catch (e: Exception) {
                avisar("No se pudo conectar con el servidor")
            }
        }
    }
}
