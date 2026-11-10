# INotes 📝

Aplicación moderna de notas para Android con diseño y experiencia de usuario inspirados en la app **Notas de iOS (iPhone)**.

---

## 📱 Características principales

1. **Estética iOS Notes**:
   - Paleta de colores oficial con acento amarillo (`#EAA824`).
   - Tarjetas agrupadas con bordes redondeados y separadores sutiles estilo iOS.
   - Tipografía refinada y modo oscuro / claro automático según el sistema.
2. **Organización inteligente por fechas y estados**:
   - Sección destacada para notas **Fijadas**.
   - Agrupación automática temporal: **Hoy**, **Ayer**, **Últimos 7 días**, **Este mes** y **Anteriores**.
3. **Gestión completa de carpetas**:
   - Vista de carpetas: "Todas las notas", carpetas personalizadas y "Papelera".
   - Crear, renombrar y eliminar carpetas.
   - Contadores en tiempo real de notas por carpeta.
4. **Editor fluido**:
   - Edición de título y cuerpo.
   - Guardado automático reactivo (debounced) y botón "Listo".
   - Encabezado con fecha y hora exacta formateada.
5. **Papelera y reciclaje**:
   - Mover notas a la papelera sin perderlas.
   - Restaurar notas o eliminarlas definitivamente.
   - Opción de vaciar papelera con un toque.
6. **Búsqueda en tiempo real**:
   - Búsqueda instantánea en títulos y cuerpo de las notas.
7. **Acciones rápidas**:
   - Fijar/Desfijar notas.
   - Compartir notas como texto a través de cualquier app (WhatsApp, Telegram, correo, etc.).
8. **100% Local y Privado**:
   - Base de datos Room offline en el dispositivo. Sin cuentas, sin rastreo y sin conexión a internet requerida.

---

## 🛠️ Tecnologías y Arquitectura

- **Lenguaje:** Kotlin 1.9.22
- **UI:** Jetpack Compose con Material 3
- **Base de Datos:** Room 2.6.1 (con Kapt)
- **Asincronía y reactividad:** Kotlin Coroutines & StateFlow
- **Arquitectura:** MVVM (Model-View-ViewModel) con Clean Architecture (Use Cases)
- **Compilación:** Gradle 8.10.2, Android Gradle Plugin 8.4.2
- **JDK:** Java 17

---

## 📦 Descarga e Instalación del APK

El APK listo para instalar en cualquier móvil Android (Android 8.0 / API 26+) se encuentra generado en:

```
release/INotes-v1.0.0-debug.apk
```

O bien directamente tras compilar:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Cómo instalar en el móvil:
1. Copia el archivo `INotes-v1.0.0-debug.apk` a tu teléfono (por cable, Drive, Telegram, etc.).
2. Abre el archivo en el teléfono y selecciona **Instalar**.
3. ¡Listo para probar!

---

## 💻 Compilación desde código fuente

Requisitos:
- Java JDK 17
- Android SDK (API 34/35)

En Windows (PowerShell):
```powershell
.\gradlew.bat assembleDebug
```

Para ejecutar las pruebas unitarias:
```powershell
.\gradlew.bat testDebugUnitTest
```

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Consulta el archivo [LICENSE](LICENSE) para más detalles.
