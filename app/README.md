# 💈 BarberFlow - Android Client

Aplicación móvil nativa para la gestión de reservas y clientes de una barbería. Este proyecto forma parte del ecosistema **BarberFlow**, actuando como el cliente frontend que se comunica mediante una arquitectura REST con un servidor backend en FastAPI. 

El proyecto ha sido refactorizado siguiendo los principios de **Clean Architecture** para separar las responsabilidades de red, modelos de datos y la interfaz de usuario.

## 📱 Pantallas y Características

*   **Autenticación y Sesiones:** Sistema de login que gestiona tokens de seguridad (JWT) guardados en `SharedPreferences` y redirecciona según el rol (Cliente o Administrador).
*   **Reserva de Citas:** Interfaz limpia e intuitiva mediante menús desplegables (`Spinner`) para seleccionar barberos y servicios dinámicos desde el servidor.
*   **Historial de Usuario:** Visualización del listado de citas confirmadas mediante `RecyclerView`, permitiendo a los clientes cancelar sus reservas.
*   **Panel de Administrador:** Herramientas de gestión interna (CRUD) para añadir, editar y eliminar barberos y servicios de la base de datos central.
*   **Diseño UI/UX:** Interfaz construida con XML (`LinearLayout`, `ConstraintLayout`), respetando márgenes y tipografías claras.

## 🏗️ Arquitectura del Proyecto

El código está estructurado en paquetes modulares para facilitar su escalabilidad y mantenimiento:
*   `api/`: Cliente centralizado (`RetrofitClient`) con interceptor para inyectar automáticamente el token de autorización, y definición de endpoints blindados contra redirecciones estrictas (307).
*   `models/`: Data classes (DTOs) para la comunicación JSON con el servidor.
*   `adapters/`: Lógica de renderizado de listas separada de las actividades visuales.
*   `ui/`: Actividades dedicadas exclusivamente a la gestión de la interfaz de usuario.

## 🛠️ Tecnologías Utilizadas

*   **Lenguaje:** Kotlin
*   **Entorno:** Android Studio
*   **Arquitectura de Red:** Retrofit2 + Gson Converter + OkHttp
*   **Asincronía:** Corrutinas (`lifecycleScope`) para peticiones HTTP en segundo plano sin bloquear el hilo principal.
*   **Almacenamiento Local:** `SharedPreferences` para persistencia de sesión segura.
*   **Vistas Dinámicas:** `RecyclerView` estructurado mediante el patrón `Adapter` y `ViewHolder` para renderizar el historial de datos JSON anidados.

## ⚙️ Requisitos Previos

Para hacer funcionar este cliente, necesitas tener el servidor de BarberFlow encendido:
1.  Clonar el repositorio del backend (FastAPI + SQLite).
2.  Ejecutar Uvicorn en tu máquina local.
3.  Conocer la dirección IP local de tu máquina (ej. 192.168.x.x) si usas un dispositivo físico, o usar la pasarela predeterminada si usas el emulador.

## 🚀 Configuración e Instalación

1. Clona este repositorio:

    git clone https://github.com/Jcgarval/BarberFlow.git

2. Abre el proyecto en Android Studio.
3. Dirígete al archivo de cliente centralizado en la ruta api/RetrofitClient.kt.
4. Modifica la constante principal con la IP donde se esté ejecutando tu servidor backend de Python:

    private const val BASE_URL = "http://TU_IP_LOCAL:8000/"

5. Sincroniza el proyecto con Gradle y ejecuta en tu emulador o dispositivo físico.
