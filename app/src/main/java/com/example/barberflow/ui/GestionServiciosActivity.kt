package com.example.barberflow.ui

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import com.example.barberflow.R
import com.example.barberflow.adapters.ServicioAdapter
import com.example.barberflow.api.RetrofitClient
import com.example.barberflow.models.Servicio
import com.example.barberflow.models.ServicioCreate

class GestionServiciosActivity : AppCompatActivity() {

    private lateinit var rvServicios: RecyclerView
    private lateinit var adapter: ServicioAdapter
    private lateinit var fabAddServicio: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_servicios)

        rvServicios = findViewById(R.id.rvServicios)
        fabAddServicio = findViewById(R.id.fabAddServicio)

        rvServicios.layoutManager = LinearLayoutManager(this)

        adapter = ServicioAdapter(
            servicios = emptyList(),
            onEditClick = { servicio -> mostrarDialogoEditar(servicio) },
            onDeleteClick = { servicio -> mostrarDialogoEliminar(servicio) }
        )
        rvServicios.adapter = adapter

        cargarServicios()

        fabAddServicio.setOnClickListener {
            mostrarDialogoCrearServicio()
        }
    }

    private fun cargarServicios() {
        val api = RetrofitClient.getApi(this)
        lifecycleScope.launch {
            try {
                val lista = api.obtenerServicios()
                adapter.actualizarLista(lista)
            } catch (e: Exception) {
                Toast.makeText(this@GestionServiciosActivity, "Error al cargar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- FUNCIONES DE CREACIÓN ---
    private fun mostrarDialogoCrearServicio() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Añadir Nuevo Servicio")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val inputNombre = EditText(this).apply { hint = "Nombre (ej: Corte + Barba)" }
        val inputDuracion = EditText(this).apply {
            hint = "Duración en min (ej: 45)"
            setInputType(android.text.InputType.TYPE_CLASS_NUMBER)
        }
        val inputPrecio = EditText(this).apply {
            hint = "Precio (ej: 15.50)"
            setInputType(android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)
        }

        layout.addView(inputNombre)
        layout.addView(inputDuracion)
        layout.addView(inputPrecio)
        builder.setView(layout)

        builder.setPositiveButton("Guardar") { dialog, _ ->
            val nombre = inputNombre.text.toString().trim()
            val duracion = inputDuracion.text.toString().toIntOrNull()
            val precio = inputPrecio.text.toString().toDoubleOrNull()

            if (nombre.isNotEmpty() && duracion != null && precio != null) {
                crearServicioEnApi(nombre, duracion, precio)
            } else {
                Toast.makeText(this, "Por favor rellena todos los campos correctamente", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun crearServicioEnApi(nombre: String, duracion: Int, precio: Double) {
        val api = RetrofitClient.getApi(this)
        lifecycleScope.launch {
            try {
                val peticion = ServicioCreate(nombre, duracion, precio)
                api.crearServicio(peticion)
                Toast.makeText(this@GestionServiciosActivity, "Servicio guardado", Toast.LENGTH_SHORT).show()
                cargarServicios()
            } catch (e: Exception) {
                Toast.makeText(this@GestionServiciosActivity, "Error al crear: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- FUNCIONES DE ELIMINACIÓN ---
    private fun mostrarDialogoEliminar(servicio: Servicio) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Eliminar Servicio")
        builder.setMessage("¿Estás seguro de que quieres eliminar '${servicio.nombre}'?")

        builder.setPositiveButton("Eliminar") { dialog, _ ->
            eliminarServicioEnApi(servicio.id)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun eliminarServicioEnApi(id: Int) {
        val api = RetrofitClient.getApi(this)
        lifecycleScope.launch {
            try {
                api.eliminarServicio(id)
                Toast.makeText(this@GestionServiciosActivity, "Servicio eliminado", Toast.LENGTH_SHORT).show()
                cargarServicios()
            } catch (e: Exception) {
                Toast.makeText(this@GestionServiciosActivity, "Error al eliminar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- FUNCIONES DE EDICIÓN ---
    private fun mostrarDialogoEditar(servicio: Servicio) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar Servicio")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val inputNombre = EditText(this).apply { setText(servicio.nombre) }
        val inputDuracion = EditText(this).apply {
            // CORRECCIÓN: Usamos duracion_minutos
            setText(servicio.duracion_minutos.toString())
            setInputType(android.text.InputType.TYPE_CLASS_NUMBER)
        }
        val inputPrecio = EditText(this).apply {
            setText(servicio.precio.toString())
            setInputType(android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)
        }

        layout.addView(inputNombre)
        layout.addView(inputDuracion)
        layout.addView(inputPrecio)
        builder.setView(layout)

        builder.setPositiveButton("Actualizar") { dialog, _ ->
            val nombre = inputNombre.text.toString().trim()
            val duracion = inputDuracion.text.toString().toIntOrNull()
            val precio = inputPrecio.text.toString().toDoubleOrNull()

            if (nombre.isNotEmpty() && duracion != null && precio != null) {
                actualizarServicioEnApi(servicio.id, nombre, duracion, precio)
            } else {
                Toast.makeText(this, "Datos inválidos", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun actualizarServicioEnApi(id: Int, nombre: String, duracion: Int, precio: Double) {
        val api = RetrofitClient.getApi(this)
        lifecycleScope.launch {
            try {
                val peticion = ServicioCreate(nombre, duracion, precio)
                api.actualizarServicio(id, peticion)
                Toast.makeText(this@GestionServiciosActivity, "Servicio actualizado", Toast.LENGTH_SHORT).show()
                cargarServicios()
            } catch (e: Exception) {
                Toast.makeText(this@GestionServiciosActivity, "Error al actualizar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}