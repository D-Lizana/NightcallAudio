# Núcleo de reproducción Media3

Estado: Fase 8 completada.

## Autoridad de reproducción

`PlaybackService` aloja ExoPlayer y MediaSession. Es la única autoridad sobre reproducción activa. La interfaz se comunica mediante `PlaybackController`, que implementa el contrato de dominio `PlaybackRepository`.

## Configuración de ExoPlayer

- Uso de audio `MEDIA` y contenido `MUSIC`.
- Audio Focus gestionado por ExoPlayer.
- Pausa automática ante `audio becoming noisy`.
- Saltos configurados en 10 segundos.
- MediaSession para notificación, bloqueo y controles externos.

## Controles

- Reproducir y pausar.
- Seleccionar una cola y una posición inicial.
- Posición temporal interactiva y limitada a la duración válida.
- Retroceder y avanzar 10 segundos.
- Canción siguiente.
- Canción anterior con regla de 3 segundos.
- Detener y limpiar la sesión.

## Regla Anterior

- Hasta 3.000 ms inclusive: intenta abrir la pista anterior.
- Después de 3.000 ms: reinicia la pista actual.
- Si no existe pista anterior, reinicia la primera canción.

La regla está aislada en `PreviousButtonPolicy` y cubierta por pruebas unitarias.

## Estado observable

`PlaybackState` expone:

- Cola y pista actual.
- Posición y duración.
- Reproduciendo o pausado.
- Estado `IDLE`, `BUFFERING`, `READY` o `ENDED`.
- Shuffle y repetición.
- Mensaje de error.

Durante la reproducción, la posición se publica cada 500 ms. El bucle se detiene al pausar y se cancela al cerrar el controlador.

## Metadatos

Cada MediaItem incluye identificador, URI, título, artista, álbum, carátula, pista y disco. Media3 utiliza estos valores en la sesión y las superficies del sistema.

## Pistas problemáticas

Cuando ExoPlayer informa de un archivo corrupto o incompatible:

- Si existe una pista siguiente, se prepara y reproduce automáticamente.
- Si no queda ninguna, se detiene el reproductor.
- La UI recibe un mensaje de error en español.

Esta política evita que una pista problemática cierre la aplicación o bloquee indefinidamente la cola.

## Ciclo de vida

- Al retirar la aplicación de recientes, el servicio continúa si la reproducción está activa.
- Si está pausado, vacío o finalizado, el servicio puede detenerse.
- ExoPlayer y MediaSession se liberan en `onDestroy`.
- El controlador cancela observadores y actualizaciones de progreso al cerrarse.

## Validación pendiente en dispositivo

La compilación, pruebas unitarias, lint y empaquetado están validados. En emulador o dispositivo deben comprobarse audio real, pantalla apagada, notificación, bloqueo, archivo corrupto y retirada desde recientes.
