# Flujo de permisos de NightcallAudio

Estado: Fase 5 completada.

## Acceso a audio

El permiso depende de la versión del sistema:

- Android 8 a Android 12L: `READ_EXTERNAL_STORAGE`.
- Android 13 y posteriores: `READ_MEDIA_AUDIO`.

## Estados

```text
No solicitado
    │ solicitar
    ├── concedido → cargar biblioteca
    └── denegado
          ├── se puede volver a solicitar → mostrar explicación
          └── no se puede solicitar → ofrecer abrir Ajustes
```

La aplicación registra localmente si ya solicitó el permiso. Esto permite distinguir la primera visita de una denegación permanente, ya que Android devuelve el mismo valor de `shouldShowRequestPermissionRationale` en ambos casos.

Al regresar a primer plano se comprueba nuevamente el permiso. Así se detectan tanto una concesión realizada desde Ajustes como una revocación durante el uso.

## Notificaciones

En Android 13 y posteriores, `POST_NOTIFICATIONS` se solicita al seleccionar la primera canción, que es cuando el usuario inicia una acción que puede continuar en segundo plano.

- No se solicita durante el arranque.
- Una denegación no bloquea la reproducción.
- No se vuelve a mostrar automáticamente después de una denegación.
- El intento de reproducción pendiente continúa después de cerrar el diálogo del sistema.

## Privacidad

La explicación visible indica que:

- Solo se accede a música y audio.
- No se solicitan fotos ni otros archivos.
- Los datos permanecen localmente en el dispositivo.

## Matriz manual pendiente

Estas comprobaciones requieren emulador o dispositivo:

1. Primera solicitud concedida en API 26.
2. Primera solicitud denegada en API 26.
3. Denegación permanente y apertura de Ajustes.
4. Concesión desde Ajustes y regreso a la aplicación.
5. Revocación desde Ajustes mientras la aplicación permanece abierta.
6. Primera solicitud de audio en API 33 o posterior.
7. Notificaciones concedidas al reproducir.
8. Notificaciones denegadas sin bloquear la reproducción.
9. Reinicio de la aplicación después de cada decisión.
