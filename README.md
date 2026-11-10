# INotes 📝

Aplicación de notas para Android con diseño y experiencia de usuario inspirados en la app oficial **Notas de iOS (iPhone)**.

---

## 📸 Capturas de Pantalla (Marcadores)

| Pantalla de Carpetas | Lista de Notas (Agrupada) | Editor de Nota |
|:---:|:---:|:---:|
| ![Pantalla de Carpetas](docs/screenshots/folders.png) | ![Lista de Notas](docs/screenshots/notes_list.png) | ![Editor de Nota](docs/screenshots/note_editor.png) |
| *Acceso rápido, carpetas y papelera* | *Agrupación temporal (Hoy, Ayer...) y cabecera dinámica* | *Guardado con debounce (1.5s), deshacer/rehacer y compartir* |

---

## 🚀 Funcionalidades (Alcance Cerrado)

1. **Lista de notas agrupada por fecha de modificación:**
   - Agrupación automática temporal: **Hoy**, **Ayer**, **Esta semana** y **Mes y año**.
   - Sección prioritaria para notas fijadas (**Fijadas**).
2. **Título grande estilo iOS:**
   - Cabecera con título en negrita que se compacta de forma fluida y suave al hacer scroll.
3. **Editor fluido con guardado automático:**
   - Campos independientes para título y cuerpo.
   - Guardado automático con debounce corto (1,5 segundos tras dejar de escribir) o botón de confirmación ("Listo").
   - Controles de Deshacer y Rehacer.
4. **Gestión completa de carpetas:**
   - Crear, renombrar y eliminar carpetas personalizadas.
   - **Regla de integridad:** Al borrar una carpeta, las notas asociadas se reasignan automáticamente a la carpeta principal *"Notas"*, evitando su pérdida accidental.
5. **Fijar notas arriba:**
   - Alternar fijado con chincheta amarilla para mantener las notas relevantes siempre visibles en la parte superior.
6. **Búsqueda reactiva:**
   - Filtrado instantáneo por texto en el título y en el cuerpo de las notas.
7. **Borrado con gesto y papelera de 30 días:**
   - Gesto de deslizar (swipe-to-dismiss) con confirmación para enviar a la papelera.
   - Retención durante 30 días con opción de restaurar o eliminar definitivamente.
8. **Modo Claro y Oscuro:**
   - Soporte nativo para cambio de tema dinámico según la configuración del sistema.
9. **Exportación y compartir:**
   - Exportar contenido de la nota como texto plano a través del menú de compartir nativo de Android (`Intent.ACTION_SEND`).
10. **100% Local y Privada:**
    - Sin necesidad de cuentas, sin conexión a internet y sin recolección de datos.

> [!NOTE]
> Elementos visuales auxiliares que aparecen en capturas de iOS (como dibujo a mano alzada o casillas de verificación interactivas) quedan fuera del alcance cerrado de esta versión inicial.

---

## 🛠️ Requisitos del Entorno y Tecnologías

- **Sistema Operativo Mínimo:** Android 10 (API 29)
- **Target SDK:** Android 14 / 15 (API 34/35)
- **Lenguaje:** Kotlin 1.9.22
- **JDK:** Java 17 (Eclipse Adoptium Temurin recomendado)
- **UI:** Jetpack Compose con Material 3
- **Base de Datos:** Room 2.6.1 (con exportación de esquemas y migraciones v1 -> v2)
- **Gestión de Estado:** ViewModel con `StateFlow` y Coroutines
- **Compilador / Build Tool:** Gradle 8.10.2 con Android Gradle Plugin 8.7.2

---

## 💻 Compilación y Ejecución de Pruebas

### 1. Compilación del APK Debug

En Windows (PowerShell):
```powershell
.\gradlew.bat assembleDebug
```
El archivo ejecutable se genera en:
- `app/build/outputs/apk/debug/app-debug.apk`
- `release/INotes-v1.0.0-debug.apk`

### 2. Ejecución de Pruebas Unitarias (JVM)
```powershell
.\gradlew.bat test
```

### 3. Ejecución de Pruebas Instrumentadas en Dispositivo
Con un dispositivo Android conectado mediante ADB:
```powershell
.\gradlew.bat connectedDebugAndroidTest
```

---

## 📊 Estado del Proyecto

- **Estado de compilación:** Estable (BUILD SUCCESSFUL).
- **Pruebas JVM:** 8/8 PASA.
- **Pruebas Instrumentadas:** 6/6 PASA (verificadas en Samsung SM-T580 y OnePlus CPH2579).
- **Control de versiones:** Rama `main` en [GitHub - Itsbatt/INotes](https://github.com/Itsbatt/INotes).
- Para un informe detallado de causas de crashes iniciales y matriz de verificación, consulta [`docs/pruebas.md`](docs/pruebas.md).

---

## 📄 Licencia

Este proyecto está distribuido bajo la licencia MIT a nombre de **Itsbatt**. Consulta el archivo [LICENSE](LICENSE) para más información.
