# Persistencia local con Room

Estado: Fase 7 completada.

## Configuración

- Room 2.8.4.
- KSP independiente 2.3.9, compatible con Kotlin integrado de AGP 9.3.
- Base de datos: `nightcall_audio.db`.
- Versión inicial del esquema: 1.
- Esquema JSON exportado a `app/schemas` y preparado para comprobar futuras migraciones.

## Tablas

### playlists

Guarda nombre y fechas de creación y modificación. El nombre utiliza comparación `NOCASE` y es único sin distinguir mayúsculas.

### playlist_tracks

Relaciona playlists con identificadores de MediaStore y conserva su posición. La clave compuesta impide añadir una canción dos veces a la misma playlist. La eliminación de una playlist borra estas relaciones en cascada, nunca el archivo musical.

### favorites

Guarda identificadores únicos de canciones favoritas y la fecha de incorporación.

### persisted_queue

Guarda posición actual, identificador de canción y posición original. La posición es la clave, por lo que una misma canción puede aparecer varias veces en la cola.

### playback_state

Contiene una única fila con índice actual, posición temporal, shuffle, repetición y fecha de actualización.

## Operaciones transaccionales

- Añadir una canción calculando su posición.
- Evitar duplicados de playlist mediante `INSERT IGNORE`.
- Retirar canciones y normalizar posiciones.
- Reordenar usando posiciones temporales para no violar el índice único durante un intercambio.
- Reemplazar completamente la cola.
- Guardar cola y estado dentro de una misma transacción.
- Eliminar relaciones de playlist en cascada.

## Repositorios

- `RoomPlaylistRepository` combina relaciones persistidas con las canciones vigentes de MediaStore.
- `RoomFavoritesRepository` expone favoritos mediante Flow.
- `RoomPlaybackPersistenceRepository` transforma filas en una sesión persistida de dominio.

Los repositorios están registrados en `AppContainer`. Las pantallas completas de administración se conectarán en la fase de playlists y favoritos; el almacenamiento ya está operativo.

## Referencias obsoletas

Cuando MediaStore entrega una biblioteca válida, se eliminan referencias a canciones que ya no existen. Las posiciones restantes de cada playlist se normalizan. Si la biblioteca está vacía tras una consulta correcta, también se limpian referencias musicales obsoletas, conservando las playlists vacías.

## Reglas de nombres

- Se eliminan espacios exteriores.
- No se admiten nombres vacíos.
- Máximo de 80 caracteres.
- No se admiten duplicados sin distinguir mayúsculas.

## Migraciones

`DatabaseMigrations.ALL` es el registro obligatorio para futuras migraciones. Antes de incrementar la versión deberán añadirse la migración, el nuevo esquema exportado y su prueba. No se utiliza `fallbackToDestructiveMigration`, por lo que una actualización nunca podrá borrar datos silenciosamente.

## Pruebas

Las pruebas instrumentadas en memoria comprueban:

- Prevención de canciones duplicadas en playlists.
- Reordenación estable.
- Eliminación en cascada.
- Favoritos sin duplicados.
- Cola con canciones duplicadas.
- Persistencia del estado de reproducción.

Estas pruebas están compiladas y deberán ejecutarse en el emulador junto con el resto de pruebas instrumentadas.
