# INotes 📝

Aplicación de notas para Android con diseño **iOS 26 Liquid Glass** y experiencia inspirada en **Apple Notas (iPhone)**.

---

## ✨ Novedades y Funcionalidades (v2.0.0)

1. **Estética iOS 26 Liquid Glass:**
   - Píldoras, barras flotantes, menús contextuales y modales con refracción óptica real mediante `Kyant0/AndroidLiquidGlass` (`backdrop:1.0.6`), brillo especular reactivo al toque y animaciones con física de muelle (*spring*).
   - Soporte nativo para **Modo Claro y Oscuro**.
   - **Liquid Glass Lab / Ajustes:** panel interactivo para ajustar desenfoque, refracción, aberración cromática, opacidad y gestionar copias de seguridad.

2. **Editor Rico Estilo Apple Notas:**
   - **Barra de formato `Aa`:** título, subtítulo, cuerpo, **negrita**, *cursiva*, <u>subrayado</u>, ~~tachado~~, resaltado, monoespaciado, listas con viñetas y numeradas.
   - **Checklists interactivos:** casillas de verificación con auto-ordenación opcional de tareas completadas y contadores de progreso.
   - **Secciones plegables:** encabezados `#` y `##` colapsables.
   - **Tablas editables**, **fotos adjuntas** y **Garabatos** (lienzo de dibujo con pincel, rotulador, lápiz, borrador, selector de color y grosor).
   - **Detección inteligente:** `#etiquetas`, enlaces web (`https://...`), teléfonos, correos, enlaces internos entre notas (`>>`) y cálculo matemático en línea (`=`).

3. **Organización y Carpetas:**
   - Carpetas personalizadas (crear, renombrar y eliminar con reasignación segura a *"Notas"*).
   - **Carpetas inteligentes:** *Con tareas*, *Con dibujos y fotos* y *De hoy*.
   - Agrupación automática por fecha (**Hoy**, **Ayer**, **Esta semana**, **Mes y año**) y sección prioritaria **Fijadas**.
   - Vista en **Lista** o **Galería**, ordenación y búsqueda reactiva.

4. **Privacidad y Seguridad:**
   - Bloqueo individual de notas con **PIN y Biometría** (`androidx.biometric`), ocultando el contenido real tanto en la lista previa como al abrir la nota hasta desbloquearla.

5. **Papelera con Restauración Completa:**
   - Retención de 30 días con restauración individual directa, selección múltiple (restaurar o eliminar definitivamente) y botón de **Restaurar todo**.

6. **Exportación, Importar y Copia de Seguridad:**
   - Exportar a **PDF real / Imprimir** (`PdfDocument` / `PrintManager`) con dibujos e imágenes incluidos.
   - Recibir texto o archivos `.md` desde otras aplicaciones (`ACTION_SEND`).
   - **Copia de seguridad local (`.zip`)** desde Ajustes para exportar e importar todas las notas, carpetas, dibujos y fotos.

---

## 🛠️ Tecnologías

- **Sistema Operativo Mínimo:** Android 10 (API 29) · **Compile SDK:** API 36
- **Lenguaje:** Kotlin 2.3.10 · **JDK:** Java 17 / 21
- **UI:** Jetpack Compose (BOM `2024.10.01`), Material 3 y `io.github.kyant0:backdrop:1.0.6`
- **Biometría:** `androidx.biometric:biometric:1.1.0`
- **Base de Datos:** Room 2.7.1 con KSP 2.3.5 y migraciones seguras (`v1 -> v2`)
- **Arquitectura:** MVVM + Use Cases + `StateFlow`

---

## 💻 Compilación y Ejecución de Pruebas

### 1. Compilación del APK Debug

En Windows (PowerShell):
```powershell
.\gradlew.bat assembleDebug
```
El archivo ejecutable se genera en:
- `app/build/outputs/apk/debug/app-debug.apk`

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
