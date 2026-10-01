package com.example.barberflow.ui

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import com.example.barberflow.R
import com.example.barberflow.adapters.BarberoAdapter
import com.example.barberflow.api.RetrofitClient
import com.example.barberflow.models.Barbero
import com.example.barberflow.models.BarberoCreate

class GestionBarberosActivity : AppCompatActivity() {

    private lateinit var rvBarberos: RecyclerView
    private lateinit var adapter: BarberoAdapter
    private lateinit var fabAddBarbero: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_barberos)

        rvBarberos = findViewById(R.id.rvBarberos)
        fabAddBarbero = findViewById(R.id.fabAddBarbero)

        rvBarberos.layoutManager = LinearLayoutManager(this)

        adapter = BarberoAdapter(
            barberos = emptyList(),
            onEditClick = { barbero ->
                // ¡NUEVO! Llamamos a la ventana de edición
                mostrarDialogoEditar(barbero)
            },
            onDeleteClick = { barbero ->
                mostrarDialogoEliminar(barbero)
            }
        )
        rvBarberos.adapter = adapter

        cargarBarberos()

        fabAddBarbero.setOnClickListener {
            mostrarDialogoCrearBarbero()
        }
    }

    private fun cargarBarberos() {
        val api = RetrofitClient.getApi(this)

        lifecycleScope.launch {
            try {
                val lista = api.obtenerBarberos()
                adapter.actualizarLista(lista)
            } catch (e: Exception) {
                Toast.makeText(this@GestionBarberosActivity, "Error al cargar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- FUNCIONES DE CREACIÓN ---

    private fun mostrarDialogoCrearBarbero() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Añadir Nuevo Barbero")

        val input = EditText(this)
        input.hint = "Ej: Carlos"
        builder.setView(input)

        builder.setPositiveButton("Guardar") { dialog, _ ->
            val nombre = input.text.toString().trim()
            if (nombre.isNotEmpty()) {
                crearBarberoEnApi(nombre)
            } else {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun crearBarberoEnApi(nombre: String) {
        val api = RetrofitClient.getApi(this)

        lifecycleScope.launch {
            try {
                val peticion = BarberoCreate(nombre)
                api.crearBarbero(peticion)
                Toast.makeText(this@GestionBarberosActivity, "Barbero guardado", Toast.LENGTH_SHORT).show()
                cargarBarberos()
            } catch (e: Exception) {
                Toast.makeText(this@GestionBarberosActivity, "Error al crear: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- FUNCIONES DE ELIMINACIÓN ---

    private fun mostrarDialogoEliminar(barbero: Barbero) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Eliminar Barbero")
        builder.setMessage("¿Estás seguro de que quieres eliminar a ${barbero.nombre}?")

        builder.setPositiveButton("Eliminar") { dialog, _ ->
            eliminarBarberoEnApi(barbero.id)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun eliminarBarberoEnApi(id: Int) {
        val api = RetrofitClient.getApi(this)

        lifecycleScope.launch {
            try {
                api.eliminarBarbero(id)
                Toast.makeText(this@GestionBarberosActivity, "Barbero eliminado", Toast.LENGTH_SHORT).show()
                cargarBarberos()
            } catch (e: Exception) {
                Toast.makeText(this@GestionBarberosActivity, "Error al eliminar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- NUEVAS FUNCIONES DE EDICIÓN ---

    private fun mostrarDialogoEditar(barbero: Barbero) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar Barbero")

        val input = EditText(this)
        // Pre-rellenamos el campo con el nombre actual
        input.setText(barbero.nombre)
        builder.setView(input)

        builder.setPositiveButton("Actualizar") { dialog, _ ->
            val nuevoNombre = input.text.toString().trim()

            // Solo actualizamos si el nombre no está vacío y si realmente lo ha cambiado
            if (nuevoNombre.isNotEmpty() && nuevoNombre != barbero.nombre) {
                actualizarBarberoEnApi(barbero.id, nuevoNombre)
            } else if (nuevoNombre.isEmpty()) {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun actualizarBarberoEnApi(id: Int, nuevoNombre: String) {
        val api = RetrofitClient.getApi(this)

        lifecycleScope.launch {
            try {
                // Reutilizamos el modelo BarberoCreate porque la estructura JSON que espera el backend es la misma (solo un campo "nombre")
                val peticion = BarberoCreate(nuevoNombre)

                // ATENCIÓN: Esta función debe existir en tu RetrofitClient (suele usar la anotación @PUT)
                api.actualizarBarbero(id, peticion)

                Toast.makeText(this@GestionBarberosActivity, "Barbero actualizado", Toast.LENGTH_SHORT).show()
                cargarBarberos()
            } catch (e: Exception) {
                Toast.makeText(this@GestionBarberosActivity, "Error al actualizar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}