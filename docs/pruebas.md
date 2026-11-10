# Registro de Pruebas y Corrección de Crashes - INotes

## 1. Análisis de Causa Raíz de los Crashes Iniciales y Solución

Al abrir la aplicación inicialmente, se producían cierres inesperados (crashes) y fallos de compilación críticos causados por los siguientes factores:

| Causa Raíz | Diagnóstico | Solución Aplicada |
|------------|-------------|-------------------|
| **Declaración errónea en AndroidManifest.xml** | Atributo inexistente `android:applicationName=".INotesApplication"` y referencia incorrecta de actividad `android:name=".ui.main.MainActivity"`, provocando `ClassNotFoundException` en el arranque. | Corregido a `android:name=".INotesApplication"` y `android:name=".ui.MainActivity"`. |
| **Recursos de iconos inexistentes** | Faltaban los drawables/mipmaps `@mipmap/ic_launcher` e `@drawable/ic_launcher_foreground`, provocando fallos de empaquetado AAPT y crash por ResourceNotFound. | Creados los drawables vectoriales y mipmaps en `res/drawable/` y `res/mipmap/` con el icono de Notas. |
| **Errores de sintaxis XML en recursos** | `strings.xml` tenía atributos mal formados (`<string name"app_name">`), y `colors.xml` carecía de etiqueta de cierre `</resources>`. | Corregida la sintaxis en todos los archivos de valores XML. |
| **Importaciones obsoletas de Room y sintaxis no válida** | `AppDatabase.kt` usaba `android.arch.persistence.room.*` (obsoleto pre-AndroidX) y operador inválido `??=`. | Actualizado a `androidx.room.*` con patrón de inicialización `synchronized(this) { ... }`. |
| **Falta de configuración de Room Schema** | Falta de configuración de exportación de esquemas Room con Kapt para soporte de migraciones v1 y v2. | Añadido `kapt { arguments { arg("room.schemaLocation", "$projectDir/schemas") } }` y `exportSchema = true`. |
| **Falta de componentes de build y gradle-wrapper** | Ausencia del binario `gradle-wrapper.jar` y `android.useAndroidX=true` en `gradle.properties`. | Generado `gradle-wrapper.jar` compatible con Gradle 8.10.2 y configurado `gradle.properties`. |

---

## 2. Pruebas Automáticas JVM (Unit Tests)

Ejecutadas con `.\gradlew.bat test`:

| Clase de Prueba | Caso de Prueba | Estado | Descripción |
|-----------------|----------------|--------|-------------|
| `RoomMigrationTest` | `verifyDatabaseCreation` | **PASA** | Verifica que la base de datos Room se inicializa correctamente en memoria. |
| `RoomMigrationTest` | `verifyMigration1To2` | **PASA** | Valida la migración `MIGRATION_1_2` que añade el campo `deletedAt` para la papelera de 30 días. |
| `DateGroupingTest` | `testNotesGroupedByTodayYesterdayThisWeekAndMonth` | **PASA** | Comprueba la agrupación temporal: Hoy, Ayer, Esta semana y Mes/Año. |
| `GetAllNotesUseCaseTest` | `testGetAllNotesReturnsList` | **PASA** | Retorna flujo reactivo ordenado con notas fijadas primero y por fecha. |
| `PinNoteUseCaseTest` | `testPinToggle` | **PASA** | Alterna el estado `isPinned` de la nota correctamente. |
| `SearchNotesUseCaseTest` | `testSearchByTitleAndBody` | **PASA** | Filtra notas por coincidencia en título o cuerpo de texto. |
| `TrashUseCaseTest` | `testMoveToTrashAndRestore` | **PASA** | Mueve a papelera marcando `isDeleted=true` y restaura recuperando el estado activo. |
| `TrashUseCaseTest` | `testDeletePermanently` | **PASA** | Elimina permanentemente notas de la base de datos tras expiración. |

---

## 3. Pruebas Instrumentadas en Dispositivos Reales

Ejecutadas con `.\gradlew.bat connectedDebugAndroidTest` en dispositivos físicos conectados:
- **Samsung Galaxy Tab A (SM-T580)** - Android 10 (API 29)
- **Dispositivo OnePlus/Oppo (CPH2579)** - Android 15 (API 35)

| Clase de Prueba | Caso de Prueba | Estado | Dispositivos |
|-----------------|----------------|--------|--------------|
| `RoomDaoInstrumentedTest` | `insertAndGetNote` | **PASA** | SM-T580 (Android 10), CPH2579 (Android 15) |
| `RoomDaoInstrumentedTest` | `testPinNote` | **PASA** | SM-T580 (Android 10), CPH2579 (Android 15) |
| `RoomDaoInstrumentedTest` | `testTrashAndRestore` | **PASA** | SM-T580 (Android 10), CPH2579 (Android 15) |
| `RoomDaoInstrumentedTest` | `testFolderDeletionReassignsNotes` | **PASA** | SM-T580 (Android 10), CPH2579 (Android 15) |
| `NoteListInstrumentedTest` | `applicationContextLoadsSuccessfully` | **PASA** | SM-T580 (Android 10), CPH2579 (Android 15) |
| `NoteListInstrumentedTest` | `mainActivityLaunchesWithoutCrash` | **PASA** | SM-T580 (Android 10), CPH2579 (Android 15) |

---

## 4. Verificación Funcional Manual

Comprobaciones realizadas en dispositivos físicos (Android 10 y Android 15) con la APK debug compilada:

| Funcionalidad | Estado | Detalle Comprobado |
|---------------|--------|---------------------|
| Arranque de la app | **PASA** | La app inicia instantáneamente sin cierres forzados ni errores en Logcat. |
| Creación de nota | **PASA** | Pulsar el botón flotante / barra inferior abre el editor directamente. |
| Guardado automático con debounce | **PASA** | El título y cuerpo se guardan en Room automáticamente 1.5 s tras la última pulsación. |
| Edición y actualización | **PASA** | Modificaciones en tiempo real persisten y actualizan la fecha de modificación. |
| Fijado de notas (Pin) | **PASA** | La nota fijada se posiciona arriba de la lista con chincheta amarilla. |
| Búsqueda por título y cuerpo | **PASA** | El campo de búsqueda filtra de forma reactiva por título y cuerpo. |
| Gestión de carpetas (Crear/Renombrar/Borrar) | **PASA** | Creación y renombrado operativos; al borrar carpeta, sus notas pasan a la carpeta general "Notas" sin perderse. |
| Deslizar para eliminar y papelera de 30 días | **PASA** | Deslizar hacia la izquierda despliega acción de eliminar con diálogo de confirmación y traslado a papelera. |
| Recuperación desde la papelera | **PASA** | Las notas en papelera pueden restaurarse a su carpeta original. |
| Compartir nota | **PASA** | El botón de acción invoca el `ACTION_SEND` de Android con el texto plano de la nota. |
| Modo claro y oscuro | **PASA** | Los estilos, fondos y tarjetas se adaptan al tema del sistema operativo. |
| Encabezado iOS expansible/compactable | **PASA** | Título grande superior que reduce su tamaño al realizar scroll vertical. |

---

## 5. Alcance Comprobado vs No Comprobado

### Lo que SÍ se comprobó exhaustivamente:
- Compilación limpia con JDK 17 y Android Gradle Plugin 8.7.2.
- 8/8 pruebas unitarias JVM superadas.
- 6/6 pruebas instrumentadas superadas en dos dispositivos físicos (Android 10 y Android 15).
- Instalación de APK debug y ciclo de vida de la actividad sin fugas de memoria críticas ni crashes.
- Integridad relacional en SQLite/Room para notas y carpetas.

### Lo que NO se comprobó:
- Rendimiento masivo con más de 10.000 notas simultáneas en memoria.
- Dispositivos con arquitecturas x86 o emuladores de Android TV/Automotive (fuera de alcance de la app móvil).
- Funcionalidades deliberadamente excluidas por el alcance cerrado (sin sincronización en la nube, sin dibujo táctil, sin casillas de verificación interactivas ni adjuntos multimedia).
