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
import com.example.barberflow.adapters.ServicioAdapter
import com.example.barberflow.api.RetrofitClient
import com.example.barberflow.models.Servicio
import com.example.barberflow.models.ServicioCreate
import com.example.barberflow.models.formatearPrecio
import com.example.barberflow.models.mensajeDeError
import com.example.barberflow.models.parsearDecimal
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.Locale

class GestionServiciosActivity : AppCompatActivity() {

    private val api by lazy { RetrofitClient.getApi(this) }
    private lateinit var rvServicios: RecyclerView
    private lateinit var adapter: ServicioAdapter
    private lateinit var fabAddServicio: ExtendedFloatingActionButton
    private lateinit var tvVacio: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_servicios)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_gestion_servicios)) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar_servicios)
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

        rvServicios = findViewById(R.id.rvServicios)
        fabAddServicio = findViewById(R.id.fabAddServicio)
        tvVacio = findViewById(R.id.tvVacioServicios)

        rvServicios.layoutManager = LinearLayoutManager(this)
        adapter = ServicioAdapter(
            servicios = emptyList(),
            onEditClick = { servicio -> mostrarDialogoServicio(servicio) },
            onDeleteClick = { servicio -> mostrarDialogoEliminar(servicio) }
        )
        rvServicios.adapter = adapter

        fabAddServicio.setOnClickListener { mostrarDialogoServicio(null) }

        cargarServicios()
    }

    private fun avisar(mensaje: String) {
        Snackbar.make(findViewById(R.id.root_gestion_servicios), mensaje, Snackbar.LENGTH_LONG)
            .setAnchorView(fabAddServicio)
            .show()
    }

    private fun cargarServicios() {
        lifecycleScope.launch {
            try {
                val lista = api.obtenerServicios()
                adapter.actualizarLista(lista)
                tvVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                avisar(getString(R.string.servicios_no_se_pudieron_cargar_los_servicios))
            }
        }
    }

    // ---------- Crear y editar (mismo diálogo) ----------
    private fun mostrarDialogoServicio(servicio: Servicio?) {
        val vista = layoutInflater.inflate(R.layout.dialog_servicio, null)
        val campoNombre = vista.findViewById<TextInputLayout>(R.id.til_nombre_servicio)
        val campoDuracion = vista.findViewById<TextInputLayout>(R.id.til_duracion_servicio)
        val campoPrecio = vista.findViewById<TextInputLayout>(R.id.til_precio_servicio)
        val nombre = vista.findViewById<TextInputEditText>(R.id.et_nombre_servicio)
        val duracion = vista.findViewById<TextInputEditText>(R.id.et_duracion_servicio)
        val precio = vista.findViewById<TextInputEditText>(R.id.et_precio_servicio)

        if (servicio != null) {
            nombre.setText(servicio.nombre)
            duracion.setText(servicio.duracion_minutos.toString())
            precio.setText(String.format(Locale.US, "%.2f", servicio.precio))
        }
        nombre.doAfterTextChanged { campoNombre.error = null }
        duracion.doAfterTextChanged { campoDuracion.error = null }
        precio.doAfterTextChanged { campoPrecio.error = null }

        val dialogo = MaterialAlertDialogBuilder(this)
            .setTitle(if (servicio == null) getString(R.string.servicios_nuevo_servicio) else getString(R.string.servicios_editar_servicio))
            .setView(vista)
            .setPositiveButton(if (servicio == null) getString(R.string.comun_guardar) else getString(R.string.comun_actualizar), null)
            .setNegativeButton(R.string.comun_cancelar, null)
            .create()

        dialogo.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialogo.setOnShowListener {
            nombre.requestFocus()
            dialogo.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val textoNombre = nombre.text.toString().trim()
                val minutos = duracion.text.toString().trim().toIntOrNull()
                val euros = parsearDecimal(precio.text.toString())

                var valido = true
                if (textoNombre.isEmpty()) {
                    campoNombre.error = getString(R.string.comun_escribe_un_nombre); valido = false
                } else if (textoNombre.length > 60) {
                    campoNombre.error = getString(R.string.comun_maximo_60_caracteres); valido = false
                }
                if (minutos == null || minutos < 5 || minutos > 480) {
                    campoDuracion.error = getString(R.string.servicios_entre_5_y_480_min); valido = false
                }
                if (euros == null || euros < 0 || euros > 1000) {
                    campoPrecio.error = getString(R.string.servicios_precio_no_valido); valido = false
                }
                if (!valido || minutos == null || euros == null) return@setOnClickListener

                dialogo.dismiss()
                if (servicio == null) {
                    crearServicioEnApi(textoNombre, minutos, euros)
                } else {
                    actualizarServicioEnApi(servicio.id, textoNombre, minutos, euros)
                }
            }
        }
        dialogo.show()
    }

    private fun crearServicioEnApi(nombre: String, duracion: Int, precio: Double) {
        lifecycleScope.launch {
            try {
                api.crearServicio(ServicioCreate(nombre, duracion, precio))
                avisar(getString(R.string.servicios_servicio_guardado))
                cargarServicios()
            } catch (e: HttpException) {
                avisar(mensajeDeError(this@GestionServiciosActivity, e))
            } catch (e: Exception) {
                avisar(getString(R.string.comun_no_se_pudo_conectar_con_el_servidor))
            }
        }
    }

    private fun actualizarServicioEnApi(id: Int, nombre: String, duracion: Int, precio: Double) {
        lifecycleScope.launch {
            try {
                api.actualizarServicio(id, ServicioCreate(nombre, duracion, precio))
                avisar(getString(R.string.servicios_servicio_actualizado))
                cargarServicios()
            } catch (e: HttpException) {
                avisar(mensajeDeError(this@GestionServiciosActivity, e))
            } catch (e: Exception) {
                avisar(getString(R.string.comun_no_se_pudo_conectar_con_el_servidor))
            }
        }
    }

    // ---------- Eliminar ----------
    private fun mostrarDialogoEliminar(servicio: Servicio) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.servicios_eliminar_servicio)
            .setMessage(
                getString(R.string.servicios_quieres_eliminar_si_tiene_citas_asociadas_se, servicio.nombre)
            )
            .setPositiveButton(R.string.comun_eliminar) { _, _ -> eliminarServicioEnApi(servicio.id) }
            .setNegativeButton(R.string.comun_cancelar, null)
            .show()
    }

    private fun eliminarServicioEnApi(id: Int) {
        lifecycleScope.launch {
            try {
                val respuesta = api.eliminarServicio(id)
                if (respuesta.isSuccessful) {
                    avisar(respuesta.body()?.mensaje ?: getString(R.string.servicios_servicio_eliminado))
                    cargarServicios()
                } else {
                    avisar(mensajeDeError(this@GestionServiciosActivity, respuesta))
                }
            } catch (e: Exception) {
                avisar(getString(R.string.comun_no_se_pudo_conectar_con_el_servidor))
            }
        }
    }

    // ---------- Servicios dados de baja: ver y reactivar ----------
    private fun mostrarBajas() {
        lifecycleScope.launch {
            try {
                val bajas = api.obtenerServiciosInactivos()
                if (bajas.isEmpty()) {
                    avisar(getString(R.string.servicios_no_hay_servicios_dados_de_baja))
                    return@launch
                }
                val opciones = bajas
                    .map { getString(R.string.servicios_baja_item, it.nombre, it.duracion_minutos, formatearPrecio(it.precio)) }
                    .toTypedArray()
                MaterialAlertDialogBuilder(this@GestionServiciosActivity)
                    .setTitle(R.string.servicios_servicios_dados_de_baja)
                    .setItems(opciones) { _, posicion -> confirmarReactivacion(bajas[posicion]) }
                    .setNegativeButton(R.string.comun_cerrar, null)
                    .show()
            } catch (e: HttpException) {
                avisar(mensajeDeError(this@GestionServiciosActivity, e))
            } catch (e: Exception) {
                avisar(getString(R.string.comun_no_se_pudo_conectar_con_el_servidor))
            }
        }
    }

    private fun confirmarReactivacion(servicio: Servicio) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.servicios_reactivar_servicio)
            .setMessage(getString(R.string.servicios_quieres_que_vuelva_a_poder_reservarse, servicio.nombre))
            .setPositiveButton(R.string.comun_reactivar) { _, _ -> reactivarServicioEnApi(servicio.id) }
            .setNegativeButton(R.string.comun_cancelar, null)
            .show()
    }

    private fun reactivarServicioEnApi(id: Int) {
        lifecycleScope.launch {
            try {
                val servicio = api.reactivarServicio(id)
                avisar(getString(R.string.servicios_vuelve_a_estar_disponible, servicio.nombre))
                cargarServicios()
            } catch (e: HttpException) {
                avisar(mensajeDeError(this@GestionServiciosActivity, e))
            } catch (e: Exception) {
                avisar(getString(R.string.comun_no_se_pudo_conectar_con_el_servidor))
            }
        }
    }
}
