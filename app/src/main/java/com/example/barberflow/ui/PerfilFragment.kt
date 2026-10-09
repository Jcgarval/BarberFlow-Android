package com.example.barberflow.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.barberflow.R
import com.example.barberflow.api.RetrofitClient
import com.example.barberflow.models.EliminarCuentaRequest
import com.example.barberflow.models.mensajeDeError
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Perfil del cliente: sus datos básicos, política de privacidad, cierre de sesión y borrado de la cuenta. */
class PerfilFragment : Fragment(R.layout.fragment_perfil) {

    private val api by lazy { RetrofitClient.getApi(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferencias = requireContext().getSharedPreferences("BarberFlowPrefs", Context.MODE_PRIVATE)
        val nombre = preferencias.getString("CLIENTE_NOMBRE", "")?.trim().orEmpty()

        view.findViewById<TextView>(R.id.tv_perfil_nombre).text = nombre
        view.findViewById<TextView>(R.id.tv_perfil_inicial).text = nombre.take(1).uppercase()

        // El correo se guarda al iniciar sesión; si la sesión es anterior a este cambio, simplemente no se muestra
        val correo = preferencias.getString("CLIENTE_EMAIL", "")?.trim().orEmpty()
        val textoCorreo = view.findViewById<TextView>(R.id.tv_perfil_email)
        if (correo.isBlank()) textoCorreo.visibility = View.GONE else textoCorreo.text = correo

        // Versión de la app (si no se puede leer, simplemente no se muestra)
        val versionTexto = view.findViewById<TextView>(R.id.tv_perfil_version)
        try {
            val version = requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0).versionName
            versionTexto.text = getString(R.string.perfil_version, version ?: "")
        } catch (e: Exception) {
            versionTexto.visibility = View.GONE
        }

        view.findViewById<View>(R.id.btn_perfil_cerrar_sesion).setOnClickListener {
            salirAlLogin(R.string.perfil_sesion_cerrada)
        }
        view.findViewById<View>(R.id.btn_perfil_privacidad).setOnClickListener { abrirPoliticaDePrivacidad() }
        view.findViewById<View>(R.id.btn_perfil_eliminar_cuenta).setOnClickListener { mostrarDialogoEliminarCuenta() }
    }

    /** Borra la sesión guardada y vuelve a la pantalla de login. */
    private fun salirAlLogin(@StringRes mensaje: Int) {
        val contexto = requireContext()
        contexto.getSharedPreferences("BarberFlowPrefs", Context.MODE_PRIVATE).edit().clear().apply()
        Toast.makeText(contexto, mensaje, Toast.LENGTH_SHORT).show()
        startActivity(Intent(contexto, LoginActivity::class.java))
        requireActivity().finish()
    }

    private fun abrirPoliticaDePrivacidad() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.perfil_url_privacidad))))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.perfil_sin_navegador, Toast.LENGTH_SHORT).show()
        }
    }

    // ---------- Eliminar la cuenta ----------
    private fun mostrarDialogoEliminarCuenta() {
        val vista = layoutInflater.inflate(R.layout.dialog_eliminar_cuenta, null)
        val campo = vista.findViewById<TextInputLayout>(R.id.til_password_eliminar)
        val password = vista.findViewById<TextInputEditText>(R.id.et_password_eliminar)
        password.doAfterTextChanged { campo.error = null }

        val dialogo = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.perfil_eliminar_titulo)
            .setMessage(R.string.perfil_eliminar_mensaje)
            .setView(vista)
            .setPositiveButton(R.string.perfil_eliminar_confirmar, null) // se define abajo para NO cerrar si hay errores
            .setNegativeButton(R.string.comun_cancelar, null)
            .create()

        dialogo.setOnShowListener {
            val botonEliminar = dialogo.getButton(DialogInterface.BUTTON_POSITIVE)
            val botonCancelar = dialogo.getButton(DialogInterface.BUTTON_NEGATIVE)
            botonEliminar.setOnClickListener {
                // Se recorta igual que en el registro y el login, para que coincida con la contraseña guardada
                val texto = password.text.toString().trim()
                if (texto.isEmpty()) {
                    campo.error = getString(R.string.perfil_eliminar_escribe_password)
                    return@setOnClickListener
                }
                botonEliminar.isEnabled = false
                botonCancelar.isEnabled = false
                dialogo.setCancelable(false)
                eliminarCuentaEnApi(texto, dialogo, campo) {
                    // Si falla, se vuelve a permitir reintentar o cancelar
                    botonEliminar.isEnabled = true
                    botonCancelar.isEnabled = true
                    dialogo.setCancelable(true)
                }
            }
        }
        dialogo.show()
    }

    private fun eliminarCuentaEnApi(
        password: String,
        dialogo: AlertDialog,
        campo: TextInputLayout,
        alFallar: () -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val respuesta = api.eliminarMiCuenta(EliminarCuentaRequest(password))
                if (respuesta.isSuccessful) {
                    dialogo.dismiss()
                    salirAlLogin(R.string.perfil_cuenta_eliminada)
                } else {
                    val mensaje = mensajeDeError(requireContext(), respuesta)
                    if (respuesta.code() == 400) {
                        campo.error = mensaje // contraseña incorrecta: se marca en el propio campo
                    } else {
                        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
                    }
                    alFallar()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                context?.let {
                    Toast.makeText(it, R.string.comun_no_se_pudo_conectar_con_el_servidor, Toast.LENGTH_LONG).show()
                }
                alFallar()
            }
        }
    }
}
