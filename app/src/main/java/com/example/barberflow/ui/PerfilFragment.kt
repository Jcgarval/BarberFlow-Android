package com.example.barberflow.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.barberflow.R

/** Perfil del cliente: sus datos básicos y el cierre de sesión. */
class PerfilFragment : Fragment(R.layout.fragment_perfil) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferencias = requireContext().getSharedPreferences("BarberFlowPrefs", Context.MODE_PRIVATE)
        val nombre = preferencias.getString("CLIENTE_NOMBRE", "")?.trim().orEmpty()

        view.findViewById<TextView>(R.id.tv_perfil_nombre).text = nombre
        view.findViewById<TextView>(R.id.tv_perfil_inicial).text = nombre.take(1).uppercase()

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
            preferencias.edit().clear().apply()
            Toast.makeText(requireContext(), R.string.perfil_sesion_cerrada, Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }
}
