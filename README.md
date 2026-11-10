# INotes

Aplicación de notas para Android con aspecto inspirado en la app Notas de iPhone.

## Descripción

INotes es una aplicación de toma de notas local y offline, diseñada para ofrecer una experiencia similar a las Notas de iPhone. Todas las notas se guardan localmente en el dispositivo, sin requerir cuentas, red o sincronización.

## Características

1. **Lista de notas agrupadas por fecha**: Hoy, Ayer, Esta semana, Mes y año
2. **Titulo grande al estilo iOS**: Se compacta al hacer scroll
3. **Editor de nota con título y cuerpo**: Guardado automático con debounce corto
4. **Carpetas**: Crear, renombrar y borrar (las notas se mueven a "Notas" al borrar carpeta)
5. **Fijar nota**: Mantiene la nota arriba de la lista
6. **Búsqueda por título y cuerpo**
7. **Borrar con gesto**: Papelera durante 30 días antes del borrado definitivo
8. **Modo claro y oscuro**: Según la preferencia del sistema
9. **Compartir nota**: Exportar como texto mediante el menú de compartir de Android

## Requisitos

- Android mínimo 10 (API 29)
- Kotlin 1.9.x
- JDK 11+

## Cómo compilar

```bash
# Desde la raíz del proyecto
cd app
./gradlew assembleDebug
```

La APK debug se generará en `app/build/outputs/apk/debug/app-debug.apk`.

## Estado del proyecto

En desarrollo activo. Versión 1.0.0.

## Capturas

_(Añadir capturas de pantalla aquí)_