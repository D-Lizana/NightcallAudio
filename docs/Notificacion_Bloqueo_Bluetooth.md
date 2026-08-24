# Notificación, pantalla de bloqueo y Bluetooth

## Integración

La reproducción se publica mediante `MediaSessionService`. Media3 crea y mantiene
la notificación multimedia y expone la misma sesión a la pantalla de bloqueo y a
los controladores multimedia Bluetooth.

La notificación utiliza el canal `nightcallaudio_playback`, con el nombre visible
Â«Reproducción multimediaÂ», y muestra los metadatos incluidos en cada `MediaItem`:
título, artista, álbum y carátula cuando está disponible. Los controles normales
son anterior, reproducir/pausar y siguiente.

Se incorpora además la orden propia Â«DetenerÂ». Esta orden pausa el reproductor,
lo detiene, vacía sus elementos y provoca que el controlador limpie y persista la
cola vacía. De este modo la siguiente apertura no restaura una sesión que el
usuario había cerrado expresamente.

## Compatibilidad del sistema

- La sesión permanece sincronizada con la notificación y la pantalla de bloqueo.
- Los botones multimedia Bluetooth se traducen a las órdenes estándar de Media3.
- La aplicación declara un servicio en primer plano de tipo `mediaPlayback`.
- En Android 13 o posterior se solicita `POST_NOTIFICATIONS` mediante el flujo de permisos existente.
- Si el permiso de notificaciones está denegado, la reproducción iniciada por el
  usuario sigue usando el servicio multimedia conforme al comportamiento permitido
  por Android, aunque la notificación puede no aparecer en el panel normal.

## Comprobaciones manuales

1. Reproducir una pista y comprobar título, artista, carátula y controles.
2. Bloquear la pantalla y verificar anterior, pausa/reproducción y siguiente.
3. Pulsar Â«DetenerÂ», abrir la aplicación y comprobar que la cola está vacía.
4. Probar los botones de unos auriculares o altavoz Bluetooth.
5. Repetir en API 26, una versión intermedia y la versión objetivo.
6. Repetir en Android 13 o posterior con notificaciones permitidas y denegadas.
