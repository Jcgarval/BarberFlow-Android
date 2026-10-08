<p align="center">
  <img src="docs/logo.png" width="140" alt="Logo de BarberFlow">
</p>

<h1 align="center">BarberFlow · App Android</h1>

<p align="center">
  Aplicación nativa para reservar citas en una barbería y gestionarla desde un panel de administrador.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-Android-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Android-API%2024%2B-3DDC84?logo=android&logoColor=white" alt="Android API 24+">
  <img src="https://img.shields.io/badge/UI-Material%203-1F1F23" alt="Material 3">
  <a href="https://github.com/Jcgarval/BarberFlow"><img src="https://img.shields.io/badge/Backend-FastAPI-009688?logo=fastapi&logoColor=white" alt="Backend FastAPI"></a>
</p>

## Descripción

Cliente móvil del ecosistema **BarberFlow**. Se comunica por REST con un backend en FastAPI ([repositorio del backend](https://github.com/Jcgarval/BarberFlow)), desplegado en la nube, y ofrece dos experiencias según el rol del usuario: **cliente** y **administrador**.

## Funcionalidades

### Cliente
- **Inicio:** saludo personalizado y tarjeta con la **próxima cita** (día, hora, servicio, barbero y cuánto falta), con accesos rápidos para reservar o ver el historial.
- **Reservar:** eliges barbero, servicio y día, y la app muestra como botones las **horas libres** calculadas por el servidor según la duración del servicio y las citas ya reservadas.
- **Mis citas:** historial con el estado de cada cita (pendiente, confirmada, completada o cancelada). Las citas próximas aparecen primero y se pueden cancelar con confirmación.
- **Perfil:** datos de la cuenta y cierre de sesión.

### Administrador
- **Panel de control** con el resumen de citas activas y plantilla.
- **Agenda:** todas las citas ordenadas por fecha; se puede cambiar su estado o eliminarlas.
- **Gestión de barberos y servicios:** altas, ediciones y bajas con diálogos y validación en el propio formulario. Si un barbero o servicio tiene citas, se **da de baja en lugar de borrarse** (se conserva el historial) y se puede **reactivar** desde la opción "Bajas".

## Capturas


<p align="center">
  <img src="docs/capturas/inicio.png" width="22%" alt="Inicio">
  <img src="docs/capturas/reservar.png" width="22%" alt="Reservar">
  <img src="docs/capturas/mis-citas.png" width="22%" alt="Mis citas">
  <img src="docs/capturas/admin.png" width="22%" alt="Panel de administrador">
</p>


## Arquitectura

```
app/src/main/java/com/example/barberflow/
├── adapters/   # Adaptadores de RecyclerView (citas, agenda, barberos, servicios)
├── api/        # RetrofitClient, AuthInterceptor y definición de endpoints (BarberiaApi)
├── models/     # DTOs, estados de cita y utilidades de formato
└── ui/         # Splash, login, registro, panel de administrador y gestión;
                # MainActivity con navegación inferior y fragments (Inicio, Reservar, Mis citas, Perfil)
```

### Decisiones técnicas
- **Una sola actividad principal con navegación inferior** y fragments que se muestran u ocultan, para conservar el estado de cada sección al cambiar de pestaña.
- **Sesión con JWT:** el token se guarda en `SharedPreferences` y un interceptor de OkHttp lo añade a cada petición.
- **Horas libres calculadas en el servidor**, de modo que la app nunca ofrece huecos ocupados y las reglas (horarios, solapes, días de cierre) viven en un único sitio, cubiertas por las pruebas automáticas del backend.
- **Peticiones asíncronas con corrutinas** (`lifecycleScope`), cancelando la consulta anterior cuando el usuario cambia de opción.
- **Conexión por HTTPS** con la API desplegada y tiempos de espera ampliados, porque el plan gratuito del servidor se duerme tras un rato sin uso y la primera petición puede tardar hasta un minuto.
- **Material 3 con tema claro y oscuro**, colores definidos en el tema (nada de colores fijos en los layouts).
- **Textos en `strings.xml`**, listos para traducir.
- **Compatible con Android 7 (minSdk 24):** formateo de fechas sin `java.time`.

## Tecnologías

- Kotlin y Android Studio
- Retrofit 2 + Gson + OkHttp
- Corrutinas de Kotlin
- Material Components (Material 3), RecyclerView y Fragments
- SharedPreferences
- JUnit 4 para las pruebas unitarias

## Cómo ejecutarlo

Por defecto la app usa la API ya desplegada, así que no hace falta arrancar nada más.

1. Clona este repositorio y ábrelo con Android Studio:

   ```bash
   git clone https://github.com/Jcgarval/BarberFlow-Android.git
   ```

2. Sincroniza Gradle y ejecuta la app. La primera petición puede tardar hasta un minuto si el servidor estaba dormido.
3. Crea una cuenta desde la pantalla de registro. Para entrar como administrador hay que ascender un usuario con el script `crear_admin.py` del [backend](https://github.com/Jcgarval/BarberFlow).

### Usar tu propio servidor en local

1. Arranca el backend siguiendo las instrucciones de su repositorio. Si usas un móvil físico, el servidor debe escuchar en toda la red (`uvicorn main:app --host 0.0.0.0`) y ambos dispositivos deben estar en la misma red.
2. Edita la dirección en `app/src/main/java/com/example/barberflow/api/RetrofitClient.kt`:

   ```kotlin
   private const val BASE_URL = "http://10.0.2.2:8000/"   // emulador de Android Studio
   // private const val BASE_URL = "http://192.168.1.XX:8000/"   // móvil físico: la IP de tu equipo en la red local
   ```

3. Android bloquea por defecto el tráfico HTTP sin cifrar. Para desarrollo local, añade `android:usesCleartextTraffic="true"` a la etiqueta `<application>` de `app/src/main/AndroidManifest.xml` y quítalo antes de volver a apuntar a la API en producción.

## Pruebas

Las utilidades de formato (fechas, precios) y las reglas de las citas (estados, próxima cita, cancelación) tienen pruebas unitarias que se ejecutan en el ordenador, sin emulador:

```bash
./gradlew test        # en Windows: gradlew.bat test
```

También desde Android Studio: clic derecho sobre `app/src/test` y **Run Tests**.

## Próximas mejoras

- Recordatorios de cita con notificaciones.
- Pruebas automáticas de interfaz.

## Autor

**José Carlos** · [@Jcgarval](https://github.com/Jcgarval)
