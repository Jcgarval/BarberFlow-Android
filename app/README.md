# 💈 BarberFlow - Android Client

Aplicación móvil nativa para la gestión de reservas y clientes de una barbería. Este proyecto forma parte del ecosistema **BarberFlow**, actuando como el cliente frontend que se comunica mediante una arquitectura REST con un servidor backend en FastAPI.

## 📱 Pantallas y Características (En desarrollo)

*   **Reserva de Citas:** Interfaz limpia e intuitiva mediante menús desplegables (`Spinner`) para seleccionar barberos y servicios disponibles.
*   **Historial de Usuario:** Visualización del listado de citas confirmadas extrayendo los nombres reales cruzados desde la base de datos relacional.
*   **Diseño UI/UX:** Interfaz construida con `LinearLayout` estructurados, respetando márgenes (padding/margin) y tipografías claras para una experiencia de usuario fluida.

## 🛠️ Tecnologías Utilizadas

*   **Lenguaje:** Kotlin
*   **Entorno:** Android Studio
*   **Arquitectura de Red:** Retrofit2 + Gson Converter
*   **Asincronía:** Corrutinas (`lifecycleScope`) para peticiones HTTP en segundo plano sin bloquear el hilo principal.
*   **Vistas Dinámicas:** `RecyclerView` estructurado mediante el patrón `Adapter` y `ViewHolder` para renderizar el historial de datos JSON anidados.

## ⚙️ Requisitos Previos

Para hacer funcionar este cliente, necesitas tener el servidor de BarberFlow encendido:
1.  Clonar el repositorio del backend (FastAPI + SQLite).
2.  Ejecutar Uvicorn en tu máquina local.
3.  Conocer la dirección IP local de tu máquina (ej. `192.168.x.x`) si usas un dispositivo físico, o usar la pasarela predeterminada si usas el emulador.

## 🚀 Configuración e Instalación

1. Clona este repositorio:
   ```bash
   git clone https://github.com/TU_USUARIO/BarberFlow-Android.git
   ```
2. Abre el proyecto en Android Studio.
3. Ve al archivo donde configuras Retrofit (ej. `HistorialActivity.kt`).
4. Modifica la `baseUrl` con la IP donde se esté ejecutando tu servidor backend de Python:
   ```kotlin
   .baseUrl("http://TU_IP_LOCAL:8000/")
   ```
5. Sincroniza el proyecto con Gradle y ejecuta en tu emulador o dispositivo físico.