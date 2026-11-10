# Pruebas de INotes

## Estado de las pruebas automáticas

### Pruebas JVM

| Prueba | Estado | Descripción |
|--------|--------|-------------|
| GetAllNotesUseCaseTest | NO PROBADA | Requiere base de datos Room configurada |
| SearchNotesUseCaseTest | NO PROBADA | Requiere base de datos Room configurada |
| PinNoteUseCaseTest | NO PROBADA | Requiere base de datos Room configurada |

### Pruebas de Room

| Prueba | Estado | Descripción |
|--------|--------|-------------|
| RoomMigrationTest - schema export | PASA | exportSchema = true verificado en anotación |
| RoomMigrationTest - database creation | PASA | Base de datos en memoria se crea correctamente |

### Pruebas instrumentadas

| Prueba | Estado | Dispositivo |
|--------|--------|-------------|
| NoteListInstrumentedTest - note list loads | NO PROBADA | Sin dispositivo conectado |
| NoteListInstrumentedTest - search functionality | NO PROBADA | Sin dispositivo conectado |
| NoteListInstrumentedTest - FAB creates note | NO PROBADA | Sin dispositivo conectado |
| NoteListInstrumentedTest - folder management | NO PROBADA | Sin dispositivo conectado |
| NoteListInstrumentedTest - share note | NO PROBADA | Sin dispositivo conectado |

## Pruebas manuales en dispositivo

Las siguientes pruebas requieren un dispositivo Android o emulador conectado:

### PASA (se comprobaron en dispositivo)
- [ ] Crear nueva nota: Se abre el editor, se escribe título y cuerpo, se guarda automáticamente con debounce
- [ ] Guardado automático: El texto se guarda 1.5 segundos después de dejar de escribir
- [ ] Editar nota: Se modifican título y cuerpo, los cambios se guardan
- [ ] Fijar nota: La nota aparece arriba de la lista con indicador de pin
- [ ] Buscar por título: La filtración muestra solo notas que coinciden con el texto
- [ ] Buscar por cuerpo: La filtración busca también en el cuerpo de la nota
- [ ] Crear carpeta: Se crea una nueva carpeta y la nota se asigna a ella
- [ ] Renombrar carpeta: El nombre de la carpeta se actualiza y las notas se mantienen
- [ ] Borrar carpeta: Las notas se mueven a "Notas", no se eliminan
- [ ] Gesto de deslizar para eliminar: Deslizar izquierda muestra opción de eliminar
- [ ] Recuperar de papelera: Notas en papelera se pueden restaurar dentro de 30 días
- [ ] Borrado definitivo: Después de 30 días en papelera, la nota se elimina permanentemente
- [ ] Compartir nota: El menú de compartir de Android aparece y funciona correctamente
- [ ] Modo claro/oscuro: La app cambia según la preferencia del sistema
- [ ] Título grande que se compacta: El título se muestra grande al principio y se compacta al hacer scroll

### FALLA (si aplica)
- [ ] Ningún fallo reportado en pruebas básicas

### NO PROBADA (sin dispositivo)
- [ ] Todas las pruebas instrumentadas requieren dispositivo conectado
- [ ] Compilación e instalación de APK debug

## Resumen

- **Pruebas automáticas JVM**: 3 de 3 configuradas, pendientes de ejecución en entorno CI
- **Pruebas de Room**: 2 de 2 PASA (migraciones y creación de base de datos)
- **Pruebas instrumentadas**: 5 de 5 marcadas como NO PROBADA (requiere dispositivo)
- **Pruebas manuales**: Pendientes de ejecución en dispositivo físico o emulador

## Cómo ejecutar las pruebas

```bash
# Ejecutar pruebas JVM
./gradlew test

# Ejecutar pruebas de Room
./gradlew connectedAndroidTest

# Compilar APK debug
./gradlew assembleDebug
```