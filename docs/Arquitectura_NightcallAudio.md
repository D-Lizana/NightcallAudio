# Arquitectura de NightcallAudio

Estado: base arquitectónica de la Fase 3.

## Capas

### Dominio

Contiene modelos, reglas, contratos de repositorio y casos de uso. No depende de Android, Media3, MediaStore, Compose ni Room.

- `domain/model`: Track, MusicLibrary, Artist, Album, Genre, MusicFolder, Playlist y PlaybackState.
- `domain/repository`: contratos para biblioteca, reproducción, playlists y favoritos.
- `domain/usecase`: obtención y agrupación de biblioteca, búsqueda y políticas funcionales.

### Datos

Implementa los contratos del dominio para fuentes de datos concretas.

- `data/mediastore`: lectura de archivos musicales del dispositivo.
- `data/database`: se incorporará en la Fase 7 para Room.
- Los modelos específicos de una fuente de datos deben transformarse antes de salir de esta capa.

### Reproducción

Encapsula Media3.

- `PlaybackService`: propietario de ExoPlayer y MediaSession.
- `PlaybackController`: implementación Media3 de `PlaybackRepository`.
- La interfaz no debe acceder directamente a ExoPlayer ni al servicio.

### Presentación

Contiene Compose, estados de UI y ViewModels.

- Las pantallas reciben estado inmutable y callbacks.
- Los ViewModels dependen de casos de uso o contratos, nunca de MediaStore, Room o ExoPlayer.
- La actividad solo crea el árbol de Compose y obtiene las dependencias del contenedor.

## Inyección de dependencias

`NightcallAudioApplication` mantiene un `AppContainer` compartido durante el proceso. El contenedor crea repositorios y casos de uso. Los ViewModels reciben sus dependencias mediante una `ViewModelProvider.Factory`.

Esta solución manual evita introducir un framework antes de que la complejidad lo justifique y permite sustituir contratos por dobles durante las pruebas.

## Flujo de dependencias

```text
Compose → ViewModel → Caso de uso → Contrato de repositorio
                                      ↑
                      MediaStore / Media3 / Room
```

Las flechas de compilación siempre apuntan hacia el dominio. El dominio no conoce las implementaciones externas.

## Fuentes de verdad

- Biblioteca del dispositivo: MediaStore.
- Reproducción activa y cola en memoria: MediaSessionService.
- Playlists, favoritos y sesión persistida: Room, desde la Fase 7.
- Estado de una pantalla: su ViewModel.

## Reglas de evolución

- No pasar `Cursor`, `MediaItem`, entidades de Room ni otros tipos de infraestructura a la UI.
- No almacenar `Context`, actividades o composables en ViewModels.
- No duplicar el estado autoritativo del reproductor en varios ViewModels.
- Añadir reglas funcionales como casos de uso o políticas comprobables.
- Toda nueva implementación de repositorio debe poder sustituirse por un doble de prueba.
- Las operaciones de entrada/salida deben ejecutarse fuera del hilo principal.

## Estrategia de pruebas

- Dominio: pruebas unitarias puras.
- ViewModels: casos de uso y repositorios falsos.
- MediaStore, Media3 y Room: pruebas de integración.
- Compose: pruebas de estado y acciones de usuario.

Los dobles iniciales se encuentran en `app/src/test/java/com/nightcallaudio/testutil`.
